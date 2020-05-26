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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private void unlinkTrolleyAndUser(String userId, String trolleyId) {
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
     * Purpose: Set in motion processes needed to add items
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
                        Map<String,Object> itemMap = document.getData();
                        itemMap.put("barcode", barcode);
                        Item item = new Item(itemMap);
                        // call method to update firebase accordingly
                        startScanning(productDocRef, true);
                        listenForCorrectItem(firebaseCallback, User.getInstance().getTrolleyDoc(), item, barcode, true);
                        firebaseCallback.onItemCallback(item);
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
     * Purpose: Add item to Firestore
     * @param barcode
     * @param item
     */
    private void addItemToFB(final Item item, final String barcode){
        // get trolley document reference from userObject
        DocumentReference trolleyItemDocRef = userObject.getTrolleyDoc().collection("items").document(barcode);

        // update firebase accordingly
        trolleyItemDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        trolleyItemDocRef.update("qty", documentSnapshot.getDouble("qty") + 1);
                    } else {
                        trolleyItemDocRef.set(item.getFBItem(false));
                    }
                }
            }
        });
    }

    // remove item from cart
    void removeItemFromCart (final FirebaseCallback firebaseCallback, String barcode) {
        startScanning(db.collection("products").document(barcode), false);
        listenForCorrectItem(firebaseCallback, User.getInstance().getTrolleyDoc(), null, barcode, false);
    }

    private void removeItemFromFB(String barcode) {
        DocumentReference itemDocRef = User.getInstance().getTrolleyDoc().collection("items").document(barcode);
        itemDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        double qty = documentSnapshot.getDouble("qty");
                        if (qty > 1) {
                            itemDocRef.update("qty", documentSnapshot.getDouble("qty") - 1);
                        } else {
                            itemDocRef.delete();
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                }
            }
        });
    }

    /**
     * Purpose: Scan for addition of item
     * @param itemDocRef
     */
    private void startScanning(DocumentReference itemDocRef, Boolean forAdding) {
        // create write batch to update firebase accordingly
        batch = db.batch();

        if (forAdding) {
            batch.update(User.getInstance().getTrolleyDoc(), "product_id", itemDocRef);
        } else {
            batch.update(User.getInstance().getTrolleyDoc(), "removed_item", itemDocRef);
        }
        batch.update(User.getInstance().getTrolleyDoc(), "scanning", true);
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
//                listenForCorrectItem(firebaseCallback, User.getInstance().getTrolleyDoc());
                Log.i("console", "Scanning...");
            }
        });
    }

    /**
     * Purpose: Listen for ILLOP signal on firebase
     * Success: Calls the checkIllopCallback, notification should appear in app to restore trolley to previous state
     *          to previous status
     * @param illopCallback
     */
    public void listenForIllop(final IllopCallback illopCallback) {
//        Log.i("console", "attach illop listener");
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

    /**
     * Purpose: Detach listeners as specified
     * @param type
     */
    public void detachListener(String type) {
        Log.i("console", "detach illop listener");
        if (type.equals("illop") && illoplistener != null) {
            illoplistener.remove();
        } else if (type.equals("cart") && itemsChangeListener != null) {
            itemsChangeListener.remove();
        }
    }

    /**
     * Purpose: Listens for correct_item field in firebase to be true
     * Success:
     *  - forAdding: calls addItemToFB
     *  - !forAdding: calls removeItemFromFB
     * Error: call firestoreCallback with null as param
     * @param firebaseCallback
     * @param trolleyDocRef
     * @param item
     * @param barcode
     * @param forAdding
     */
    private void listenForCorrectItem(final FirebaseCallback firebaseCallback, DocumentReference trolleyDocRef, Item item, String barcode, Boolean forAdding) {
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
                        if (forAdding) {
                            addItemToFB(item, barcode);
                        } else {
                            removeItemFromFB(barcode);
                        }
                        // reset fields to idle state
                        resetTrolleyScanningStatus();
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
     */
    private void resetTrolleyScanningStatus() {
        // create write batch and update accordingly
        batch = db.batch();
        batch.update(User.getInstance().getTrolleyDoc(), "scanning", false);
        batch.update(User.getInstance().getTrolleyDoc(), "correct_item", false);
        batch.update(User.getInstance().getTrolleyDoc(), "removed_item", null);
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("console", "Successfully reset");
            }
        });
    }

    /**
     * Purpose: Cancel operation to add item.
     * Success:
     *  - Firebase: scanning set to false
     *  - App:      alert dialog of item disappears
     */
    public void cancelAddingOperation() {
        correctItemListener.remove();
        resetTrolleyScanningStatus();
    }

    /**
     * Purpose: Get items in local trolley
     * @param firebaseCallback
     */
    void getItemsInLocalTrolley(final FirebaseCallback firebaseCallback) {
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
     * Purpose: Move items to purchase history + reset user and trolley
     */
    void checkOut() {
        Date checkoutDate = new Date();
        DocumentReference trolleyDocRef = userObject.getTrolleyDoc();
        DocumentReference userShoppingHistDocRef = userObject.getUserDoc().collection("shopping_hist").document(new SimpleDateFormat("YYYYMMdd-HHmmss").format(checkoutDate));
        resetTrolleyScanningStatus();

        batch = db.batch();
        batch.update(trolleyDocRef, "items", null);
        batch.update(trolleyDocRef, "running_weight", 0);

        Map<String,Object> shoppingHist = new HashMap<String,Object>();
        shoppingHist.put("time_of_transaction", checkoutDate);
        List<Map> allItems = new ArrayList<Map>();
        BigDecimal totalPrice = new BigDecimal("0");
        for (Item item : User.getInstance().getItems()) {
            Map itemMap = item.getFBItem(true);
            totalPrice = totalPrice.add(new BigDecimal(itemMap.get("price").toString()));
            allItems.add(item.getFBItem(true));
        }
        shoppingHist.put("items", allItems);
        shoppingHist.put("total_price", Double.parseDouble(totalPrice.toString()));
        batch.set(userShoppingHistDocRef, shoppingHist);
        Log.i("checkout", userObject.getItems().toString());

        trolleyDocRef.collection("items")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                batch.delete(trolleyDocRef.collection("items").document(document.getId()));
                            }
                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.i("console", "Checkout successful");
                                }
                            });
                        } else {
                            Log.i("hist", "Error getting documents: ", task.getException());
                        }
                    }
                });

        unlinkTrolleyAndUser(userObject.getFirebaseUser().getUid(), userObject.getTrolleyId());
    }

    /**
     * Purpose: Listen for item changes in firebase (future development: admin accounts)
     * Success: Calls updateUserCallback.updateLocalItems to update local items stored
     * @param firebaseCallback
     */
    void listenForCartChanges(final FirebaseCallback firebaseCallback) {
        if (User.getInstance().getTrolleyDoc() != null) {
            CollectionReference itemsCollection = userObject.getTrolleyDoc().collection("items");
            itemsChangeListener = itemsCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snapshots,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        System.err.println("Listen failed:" + e);
                        return;
                    }

                    ArrayList<Item> itemArrayList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : snapshots) {
                        double qty = documentSnapshot.getDouble("qty");
                        for (int i=0;i<qty;i++) {
                            Map<String,Object> itemMap = documentSnapshot.getData();
                            itemMap.put("barcode", documentSnapshot.getId());
                            itemArrayList.add(new Item (itemMap));
                        }
                    }
                    User.getInstance().setItems(itemArrayList, firebaseCallback);
                }
            });
        }

    }

    /**
     * Purpose: Get purchase history of User.
     * Success: Calls updateUserCallback.updateLocalItems to update local items stored
     * @param shoppingHistCallback
     */
    public void getShoppingHist(ShoppingHistCallback shoppingHistCallback){
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
                                Map<String, Object> shoppingHistSession = document.getData();
                                Log.i("hist", shoppingHistSession.toString());
                                itemList = new ArrayList<Item>();
                                totalPrice = shoppingHistSession.get("total_price").toString();
                                timeOfTransaction = (Date) shoppingHistSession.get("time_of_transaction");
                                itemList = new ArrayList<Item>();
                                for (Map<String,Object> itemMap : (List<Map<String,Object>>) shoppingHistSession.get("items")) {
                                    itemList.add(new Item(itemMap));
                                }
                                shoppingHistCallback.updateShoppingHist(itemList, totalPrice, timeOfTransaction);
                            }
                        } else {
                            Log.i("hist", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}