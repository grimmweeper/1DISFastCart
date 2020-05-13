package com.drant.FastCartMain;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 *
 */
public class DatabaseHandler {
    private static DatabaseHandler instance = null;
    private FirebaseFirestore db;
    ListenerRegistration correctItemListener;
    ListenerRegistration itemsChangeListener;
    ListenerRegistration illoplistener;
    WriteBatch batch;
    Boolean illopStatus;

    User userObject = User.getInstance();

    Item currentItem;
    ArrayList<Item> itemList;
    Map<String, ArrayList<Item>> mapOfLists;
    String totalPrice;
    Date timeOfTransaction;

    private DatabaseHandler(){
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
    }

    public static DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }

    // saving document references
    DocumentReference saveUserDocument(String uid){
        return db.collection("users").document(uid);
    }

    DocumentReference saveTrolleyDocument(String trolleyId){
        if (trolleyId != null) {
            return db.collection("trolleys").document(trolleyId);
        } else {
            return null;
        }

    }

    /* Base Firebase functions*/

    /**
     * Purpose: Register new patient into firestore
     * @param userId
     */
    void registeringNewUser(String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("trolley", null);
        db.collection("users").document(userId).set(data);
    }

    // updating trolley and user links
    private void linkingFunction(DocumentReference mainDocRef, DocumentReference linkedDocRef, String fieldToUpdate) {
        mainDocRef.update(fieldToUpdate, linkedDocRef).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("console", "Trolley has been successfully added to User!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("console", "Error adding trolley to user.", e);
                    }
                });
    }

    /**
     * Purpose: Link trolley and user locally
     * @param userId
     * @param trolleyId
     */
    public void linkTrolleyAndUser(String userId, String trolleyId){
        // saves trolley doc to userObject
        userObject.setTrolleyId(trolleyId);
        // get document references based on user id and trolley id
        DocumentReference trolleyDocRef = this.db.collection("trolleys").document(trolleyId);
        DocumentReference userDocRef = this.db.collection("users").document(userId);
        // call functions to link user and trolley
        linkingFunction(userDocRef, trolleyDocRef, "trolley");
        linkingFunction(trolleyDocRef, userDocRef, "user");
    }

    /**
     * Purpose: Unlink trolley and user locally (use case: on checkout)
     * @param userId
     * @param trolleyId
     */
    public void unlinkTrolleyAndUser(String userId, String trolleyId) {
        // set trolleyDocRef as null
        userObject.setTrolleyId(null);
        // get document references based on user id and trolley id
        DocumentReference trolleyDocRef = this.db.collection("trolleys").document(trolleyId);
        DocumentReference userDocRef = this.db.collection("users").document(userId);
        // call functions to link user and trolley
        linkingFunction(userDocRef, null, "trolley");
        linkingFunction(trolleyDocRef, null, "user");
    }

    /**
     * Purpose: Add items to local database
     * @param firebaseCallback
     * @param barcode
     */
    public void addItemToCart(final FirebaseCallback firebaseCallback, String barcode){
        // get product document reference from barcode
        DocumentReference productDocRef = db.collection("products").document(barcode);
        // get item information from firebase to display information
        productDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // call method to update firebase accordingly
                        addItemToCart(firebaseCallback, productDocRef);
                        Log.i("console", "adding1");
                        String name = document.getString("name");
                        String price = document.getDouble("price").toString();
                        String imageRef = document.getString("img");
                        Item item = new Item(name, price, imageRef, productDocRef);
                        firebaseCallback.onItemCallback(item);
                        currentItem = item;
                        ArrayList<Item> newItemList = userObject.getItems();
                        newItemList.add(item);
                        userObject.setItems(newItemList);
                    } else {
                        Log.d(TAG, "No such document");
                        firebaseCallback.onItemCallback(null);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    firebaseCallback.onItemCallback(null);
                }
            }
        });
    }

    /**
     * Purpose: Overloaded method - Add item to firebase database
     * @param firebaseCallback
     * @param itemToAdd
     */
    private void addItemToCart(final FirebaseCallback firebaseCallback, final DocumentReference itemToAdd){
        // get trolley document reference from userObject
        DocumentReference trolleyDocRef = userObject.getTrolleyDoc();
        // update firebase accordingly
        trolleyDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<DocumentReference> itemDocuments = (ArrayList<DocumentReference>) document.get("items");
                        if (itemDocuments == null) {
                            itemDocuments = new ArrayList<DocumentReference>();
                        }
                        itemDocuments.add(itemToAdd);
                        startScanning(firebaseCallback, trolleyDocRef, itemDocuments);
                        userObject.setItemDocuments(itemDocuments);
                    } else {
                        Log.d(TAG, "No such document");
                        firebaseCallback.displayItemsCallback(null);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    firebaseCallback.displayItemsCallback(null);
                }
            }
        });
    }

    // remove item from cart

    /**
     * Purpose: Remove item from local database
     * @param firebaseCallback
     * @param itemToRemove
     */
    void removeItemFromCart(final FirebaseCallback firebaseCallback, Item itemToRemove){
        // get product document reference from barcode
        DocumentReference itemDocRef = itemToRemove.getItemDocRef();
        // call method to update firebase accordingly
        removeItemFromCart(firebaseCallback, itemDocRef);
        ArrayList<Item> newItemList = userObject.getItems();
        newItemList.remove(itemToRemove);
        userObject.setItems(newItemList);
    }

    /**
     * Purpose: Overloaded method - updates firebase when we remove item from cart
     * Success: Call startScanning method to communicate with rpi
     * @param firebaseCallback
     * @param itemDocRef
     */
    private void removeItemFromCart(final FirebaseCallback firebaseCallback, final DocumentReference itemDocRef){
        DocumentReference trolleyDocRef = userObject.getTrolleyDoc();
        trolleyDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<DocumentReference> itemDocuments = (ArrayList<DocumentReference>) document.get("items");
                        itemDocuments.remove(itemDocRef);
                        trolleyDocRef
                                .update("removed_item", itemDocRef)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("console", "Removing item...");
                                        DocumentReference itemToRemove = itemDocRef;
                                        startScanning(firebaseCallback, trolleyDocRef, itemDocuments, itemToRemove);
                                        userObject.setItemDocuments(itemDocuments);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i("console", "Error removing item.", e);
                                    }
                                });
                    } else {
                        Log.d(TAG, "No such document");
                        firebaseCallback.displayItemsCallback(null);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    firebaseCallback.displayItemsCallback(null);
                }
            }
        });
    }

    // update firebase to start rpi weight-checking

    /**
     * Purpose: Scan for adding of item
     * @param firebaseCallback
     * @param trolleyDocRef
     * @param itemDocuments
     */
    private void startScanning(FirebaseCallback firebaseCallback, DocumentReference trolleyDocRef, ArrayList<DocumentReference> itemDocuments) {
        if (itemDocuments.isEmpty()) {
            itemDocuments = null;
        }
        // create write batch to update firebase accordingly
        batch = db.batch();

        batch.update(trolleyDocRef, "product_id", itemDocuments.get(itemDocuments.size() - 1));
        batch.update(trolleyDocRef, "scanning", true);
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listenForCorrectItem(firebaseCallback, trolleyDocRef);
                Log.i("console", "Scanning...");
            }
        });
    }

    /**
     * Purpose: Scan for removal of item
     * @param firebaseCallback
     * @param trolleyDocRef
     * @param itemDocuments
     * @param itemToRemove
     */
    private void startScanning(FirebaseCallback firebaseCallback, DocumentReference trolleyDocRef,
            ArrayList<DocumentReference> itemDocuments, DocumentReference itemToRemove) {
        if (itemDocuments.isEmpty()) {
            itemDocuments = null;
        }
        // create write batch to update firebase accordingly
        batch = db.batch();

        batch.update(trolleyDocRef, "removed_item", itemToRemove);
        batch.update(trolleyDocRef, "scanning", true);
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listenForCorrectItem(firebaseCallback, trolleyDocRef);
                Log.i("console", "Scanning...");
            }
        });
    }

    /**
     * Purpose: Listen for ILLOP signal on firebase
     * Success: Calls the checkIllopCallback, notification should appear in app to regain trolley
     *          to previous status
     * @param illopCallback
     */
    public void listenForIllop(final FirebaseCallback illopCallback) {
        Log.i("console", "attach illop listener");
        DocumentReference trolleyDocRef = userObject.getTrolleyDoc();
        if (trolleyDocRef != null) {
            illoplistener = trolleyDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                    if (e != null) {
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        illopStatus = snapshot.getBoolean("illop");
                        illopCallback.checkIllopCallback(illopStatus);
                    }
                }
            });
        }
    }

    public void detachListener(String type) {
        Log.i("console", "detach illop listener");
        if (type.equals("illop") && illoplistener != null) {
            illoplistener.remove();
        }
    }

    /**
     * Purpose: Listens for correct_item field in firebase to be true
     * Success: call firestoreCallback with itemValidationStatus as param
     * Error: call firestoreCallback with null as param
     * @param firebaseCallback
     * @param trolleyDocRef
     */
    public void listenForCorrectItem(final FirebaseCallback firebaseCallback, DocumentReference trolleyDocRef) {
        // attach listener to listen for change in correct_item field
        correctItemListener = trolleyDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    firebaseCallback.itemValidationCallback(null);
//                    Log.i("console", "Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
//                    Log.i("console", "Current data: " + snapshot.getData());
                    Boolean itemValidationStatus = snapshot.getBoolean("correct_item");
                    firebaseCallback.itemValidationCallback(itemValidationStatus);
                    if (itemValidationStatus) {
                        // detach listener
                        correctItemListener.remove();

                        // reset fields to idle state
                        resetTrolleyScanningStatus(trolleyDocRef);
                    }
                } else {
//                    Log.i("console", "Current data: null");
                    firebaseCallback.itemValidationCallback(null);
                }
            }
        });
    }

    /**
     * Purpose: Reset Trolley to idle state
     * Success: No visible output on app, trolley document's fields in firebase updates
     * @param trolleyDocRef
     */
    private void resetTrolleyScanningStatus(DocumentReference trolleyDocRef) {
        // create write batch and update accordingly
        batch = db.batch();
        batch.update(trolleyDocRef, "scanning", false);
        batch.update(trolleyDocRef, "correct_item", false);
        batch.update(trolleyDocRef, "removed_item", null);
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("console", "Successfully reset");
            }
        });
    }

    /**
     * Purpose: Cancel add/remove operations
     * @param firebaseCallback
     * @param cancelAdd
     */
    public void cancelOperation(final FirebaseCallback firebaseCallback, Boolean cancelAdd) {
        correctItemListener.remove();
        DocumentReference trolleyDocRef = userObject.getTrolleyDoc();
        resetTrolleyScanningStatus(trolleyDocRef);
        ArrayList<DocumentReference> newItemDocuments = userObject.getItemDocuments();
        ArrayList<Item> newItems = userObject.getItems();
        if (cancelAdd) {
            newItemDocuments.remove(currentItem.getItemDocRef());
            newItems.remove(currentItem);
        } else {
            newItemDocuments.add(currentItem.getItemDocRef());
            newItems.add(currentItem);
        }
        trolleyDocRef
                .update("items", newItemDocuments)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("console", "Scanning status successfully updated.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("console", "Error updating scanning status.", e);
                    }
                });
        userObject.setItems(newItems);
        userObject.setItemDocuments(newItemDocuments);
    }

    /**
     * Purpose: Get items in local trolley
     * @param firebaseCallback
     */
    public void getItemsInLocalTrolley (final FirebaseCallback firebaseCallback) {
        Log.i("console", "getting");
        try{
            ArrayList<Item> items = userObject.getItems();
            firebaseCallback.displayItemsCallback(items);
            Log.i("console", items.toString());
        } catch (NullPointerException nullPointerException){
            Log.i("console", nullPointerException.toString());
        }
    }

    /**
     * Purpose: clear fields of trolley and on local database
     */
    void checkOut() {
        Date checkoutDate = new Date();
        DocumentReference trolleyDocRef = userObject.getTrolleyDoc();
        DocumentReference userShoppingHistDocRef = userObject.getUserDoc().collection("shopping_hist").document(new SimpleDateFormat("yyyymmdd").format(checkoutDate));
        Log.i("checkout", checkoutDate.toString());
        Log.i("checkout", userShoppingHistDocRef.toString());
        resetTrolleyScanningStatus(trolleyDocRef);
        batch = db.batch();
        batch.update(trolleyDocRef, "items", null);
        batch.update(trolleyDocRef, "running_weight", 0);
//        batch.set(userShoppingHistDocRef, "time_of_transaction", checkoutDate);
        Log.i("checkout", userObject.getItems().toString());
//        batch.set(userShoppingHistDocRef, "");
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("console", "Checkout successful");
            }
        });
        unlinkTrolleyAndUser(userObject.getUserId(), userObject.getTrolleyId());
        userObject.setItems(new ArrayList<Item>());
        userObject.setItemDocuments(new ArrayList<DocumentReference>());
        userObject.setTrolleyId(null);
    }

    /**
     * Purpose: Get details on a single item on firebase
     * @param updateUserCallback
     * @param itemDocRef
     * @param itemList
     */
    private void getSingleFirebaseItem (UpdateUserCallback updateUserCallback, DocumentReference itemDocRef,ArrayList<Item> itemList) {
        itemDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d("getSingleFirebaseItem", "Called");
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String price = document.getDouble("price").toString();
                        String imageRef = document.getString("img");
                        Item item = new Item(name, price, imageRef, itemDocRef);
                        itemList.add(item);
                        updateUserCallback.updateLocalItems(itemList);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     * Purpose: Listen for item changes in firebase (future development: admin accounts)
     * Success: Calls updateUserCallback.updateLocalItems to update local items stored
     * @param updateUserCallback
     */
    void listenForItemChanges(UpdateUserCallback updateUserCallback) {
        DocumentReference trolleyDocRef = userObject.getTrolleyDoc();
        // attach listener to listen for change in correct_item field
        if (trolleyDocRef != null) {
            itemsChangeListener = trolleyDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    Log.i("ItemsListener", "Listening...");
                    if (e != null) {
                        Log.i("ItemsListener", "Listen failed.", e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        if (batch == null) {
                                ArrayList<DocumentReference> itemDocuments = (ArrayList<DocumentReference>) snapshot.get("items");
                                if (itemDocuments!=null) {
                                    Log.i("ItemsListener", itemDocuments.toString());
                                    userObject.setItemDocuments(itemDocuments);
                                    itemList = new ArrayList<Item>();
                                    for (DocumentReference itemDocRef : itemDocuments) {
                                        getSingleFirebaseItem(updateUserCallback, itemDocRef,itemList);
                                    }
                            } else Log.i("ItemsListener", "No items in cart");
                        } else { batch = null; }
                    }
                }
            });
        }
    }

    public void getShoppingHist(UpdateUserCallback updateUserCallback){
        CollectionReference userShoppingHistCollection = userObject.getUserDoc().collection("shopping_hist");
        userShoppingHistCollection
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String docId = document.getId();
                                Log.i("hist", docId);
                                Map<String, Object> itemMap = document.getData();
                                Log.i("hist", itemMap.toString());
                                itemList = new ArrayList<Item>();
                                for (Map.Entry<String, Object> entry : itemMap.entrySet()) {
                                    if (entry.getValue() instanceof Date) {
                                        timeOfTransaction = (Date) entry.getValue();
                                    } else if (entry.getValue() instanceof Map) {
                                        Log.i("hist", entry.getValue().toString());
                                        Map<String, Object> rawData = (Map) entry.getValue();
                                        String name = (String) rawData.get("name");
                                        String price = rawData.get("price").toString();
                                        String imageRef = (String) rawData.get("img");
                                        Item item = new Item(name, price, imageRef, null);
                                        itemList.add(item);
                                    } else {
                                        totalPrice = document.get("total_price").toString();
                                        Log.i("hist", "before: " + totalPrice);
                                    }
                                }
                                updateUserCallback.updateShoppingHist(itemList, totalPrice, timeOfTransaction);
                            }
                        } else {
                            Log.i("hist", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}