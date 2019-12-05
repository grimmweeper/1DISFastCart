package com.drant.FastCartMain;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static com.drant.FastCartMain.MainActivity.userObject;

public class DatabaseHandler {
    private static DatabaseHandler instance = null;
    private FirebaseFirestore db;
    ListenerRegistration correctItemListener;
    ListenerRegistration itemsChangeListener;
    WriteBatch batch;

    Item currentItem;
    ArrayList<Item> itemList;
//    Boolean updating = false;

//    private DocumentReference productDocRef;

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
        return db.collection("trolleys").document(trolleyId);
    }

    /* Base Firebase functions*/

    // register new patient into firestore
    void registeringNewUser(String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("trolley", null);
        db.collection("users").document(userId).set(data);
    }

    // updating trolley and user links
    private void linkingFunction(DocumentReference mainDocRef, DocumentReference linkedDocRef, String fieldToUpdate) {
        mainDocRef
                .update(fieldToUpdate, linkedDocRef)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
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

    // linking trolley and user
    void linkTrolleyAndUser(String userId, String trolleyId){
        // saves trolley doc to userObject
        userObject.setTrolleyId(trolleyId);
        // get document references based on user id and trolley id
        DocumentReference trolleyDocRef = this.db.collection("trolleys").document(trolleyId);
        DocumentReference userDocRef = this.db.collection("users").document(userId);
        // call functions to link user and trolley
        linkingFunction(userDocRef, trolleyDocRef, "trolley");
        linkingFunction(trolleyDocRef, userDocRef, "user");
        // TODO: callback to correct screen for linking of trolley
    }

    // unlink trolley and user
    void unlinkTrolleyAndUser(String userId, String trolleyId) {
        // set trolleyDocRef as null
        userObject.setTrolleyId(null);
        // get document references based on user id and trolley id
        DocumentReference trolleyDocRef = this.db.collection("trolleys").document(trolleyId);
        DocumentReference userDocRef = this.db.collection("users").document(userId);
        // call functions to link user and trolley
        linkingFunction(userDocRef, null, "trolley");
        linkingFunction(trolleyDocRef, null, "user");
        // TODO: callback to correct screen for unlinking of trolley
    }

    // add item to cart
    public void addItemToCart(final FirebaseCallback firebaseCallback, String barcode){
        // get product document reference from barcode
        DocumentReference productDocRef = db.collection("products").document(barcode);
        // call method to update firebase accordingly
        addItemToCart(firebaseCallback, productDocRef);
        // get item information from firebase to display information
        productDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.i("console", "adding1");
                        String name = document.getString("name");
                        String price = document.getDouble("price").toString();
//                        String weight = document.getDouble("weight").toString();
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

    // update trolley in firebase
    private void addItemToCart(final FirebaseCallback firebaseCallback, final DocumentReference itemToAdd){
        // get trolley document reference from userObject
        DocumentReference trolleyDocRef = User.getInstance().getTrolleyDoc();
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
    void removeItemFromCart(final FirebaseCallback firebaseCallback, Item itemToRemove){
        // get product document reference from barcode
        DocumentReference itemDocRef = itemToRemove.getItemDocRef();
        // call method to update firebase accordingly
        removeItemFromCart(firebaseCallback, itemDocRef);
        ArrayList<Item> newItemList = userObject.getItems();
        newItemList.remove(itemToRemove);
        userObject.setItems(newItemList);
    }

    // update firebase respectively
    private void removeItemFromCart(final FirebaseCallback firebaseCallback, final DocumentReference itemDocRef){
//        final DocumentReference itemToRemove = db.collection("products").document("8934677000358");
//        final DocumentReference trolleyDocRef = db.collection("trolleys").document("gjDLnPSnMAul7MR8dBaI");
        DocumentReference trolleyDocRef = User.getInstance().getTrolleyDoc();
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
                                    startScanning(firebaseCallback, trolleyDocRef, itemDocuments);
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
    private void startScanning(FirebaseCallback firebaseCallback, DocumentReference trolleyDocRef, ArrayList<DocumentReference> itemDocuments) {
        if (itemDocuments.isEmpty()) {
            itemDocuments = null;
        }
        // create write batch to update firebase accordingly
        batch = db.batch();
        batch.update(trolleyDocRef, "items", itemDocuments);
        batch.update(trolleyDocRef, "scanning", true);
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listenForCorrectItem(firebaseCallback, trolleyDocRef);
                Log.i("console", "Scanning...");
            }
        });
    }

    // listen for correct item status from firebase
    private void listenForCorrectItem(final FirebaseCallback firebaseCallback, DocumentReference trolleyDocRef) {
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

    // reset trolley to idle state
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

    // cancel add/remove item
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

    void checkOut() {
        DocumentReference trolleyDocRef = userObject.getTrolleyDoc();
        resetTrolleyScanningStatus(trolleyDocRef);
        batch = db.batch();
        batch.update(trolleyDocRef, "items", null);
        batch.update(trolleyDocRef, "running_weight", 0);
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("console", "Checkout successful");
            }
        });
        userObject.setItems(null);
        userObject.setItemDocuments(null);
        userObject.setTrolleyId(null);
    }

    /* Getters to get firebase data at the start */

//    void getItemsInFirebaseTrolley(UpdateUserCallback updateUserCallback) {
//        // get trolley document reference from userObject
//        DocumentReference trolleyDocRef = User.getInstance().getTrolleyDoc();
//        // update firebase accordingly
//        trolleyDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        ArrayList<DocumentReference> itemDocuments = (ArrayList<DocumentReference>) document.get("items");
//                        userObject.setItemDocuments(itemDocuments);
//                        itemList = new ArrayList<Item>();
//                        if (itemDocuments != null) {
//                            for (DocumentReference itemDocRef : itemDocuments) {
//                                getSingleFirebaseItem(itemDocRef);
//                            }
//                        }
//                        updateUserCallback.updateLocalItems(itemList);
//                    } else {
//                        Log.d(TAG, "No such document");
//                        updateUserCallback.updateLocalItems(null);
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                    updateUserCallback.updateLocalItems(null);
//                }
//            }
//        });
//    }

    void getSingleFirebaseItem (DocumentReference itemDocRef) {
        itemDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String price = document.getDouble("price").toString();
                        String imageRef = document.getString("img");
                        Item item = new Item(name, price, imageRef, itemDocRef);
                        itemList.add(item);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    /* Listeners to sync local updates wirh firebase*/

    void listenForItemChanges(UpdateUserCallback updateUserCallback) {
        DocumentReference trolleyDocRef = userObject.getTrolleyDoc();
        // attach listener to listen for change in correct_item field
        itemsChangeListener = trolleyDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.i("console", "Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    if (batch == null) {
                        Log.i("console", "Change made");
                        ArrayList<DocumentReference> itemDocuments = (ArrayList<DocumentReference>) snapshot.get("items");
                        userObject.setItemDocuments(itemDocuments);
                        itemList = new ArrayList<Item>();
                        if (itemDocuments != null) {
                            for (DocumentReference itemDocRef : itemDocuments) {
                                getSingleFirebaseItem(itemDocRef);
                            }
                        }
                        updateUserCallback.updateLocalItems(itemList);
                    } else {
                        batch = null;
                    }
                } else {
                    Log.i("console", "Current data: null");
                }
            }
        });
    }
}

