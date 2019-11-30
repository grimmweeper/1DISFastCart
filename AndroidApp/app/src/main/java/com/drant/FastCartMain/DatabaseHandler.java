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
//import static com.drant.FastCartMain.LoginActivity.userObject;

interface FirestoreCallback{
    DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    void onItemCallback(Item item);
    void itemValidationCallback(Boolean validItem);
}

public class DatabaseHandler {
    private static DatabaseHandler instance = null;
    private FirebaseFirestore db;
    ListenerRegistration correctItemListener;
    WriteBatch batch;

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
    void addItemToCart(final FirestoreCallback firestoreCallback, String barcode){
        // get product document reference from barcode
        DocumentReference productDocRef = db.collection("products").document(barcode);
        // call method to update firebase accordingly
        addItemToCart(firestoreCallback, productDocRef);
        // get item information from firebase to display information
        productDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String price = document.getDouble("price").toString();
//                        String weight = document.getDouble("weight").toString();
                        String imageRef = document.getString("img");
                        Item item = new Item(name, price, imageRef, productDocRef);
                        firestoreCallback.onItemCallback(item);
                        ArrayList<Item> newItemList = userObject.getItems();
                        newItemList.add(item);
                        userObject.setItems(newItemList);
                    } else {
                        Log.d(TAG, "No such document");
                        firestoreCallback.onItemCallback(null);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    firestoreCallback.onItemCallback(null);
                }
            }
        });
    }

    // update trolley in firebase
    private void addItemToCart(final FirestoreCallback firestoreCallback, final DocumentReference itemToAdd){
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
                        itemDocuments.add(itemToAdd);
                        startScanning(firestoreCallback, trolleyDocRef, itemDocuments);
                        userObject.setItemDocuments(itemDocuments);
                    } else {
                        Log.d(TAG, "No such document");
                        firestoreCallback.onItemCallback(null);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    firestoreCallback.onItemCallback(null);
                }
            }
        });
    }

    // remove item from cart
    void removeItemFromCart(final FirestoreCallback firestoreCallback, Item itemToRemove){
        // get product document reference from barcode
        DocumentReference itemDocRef = itemToRemove.getItemDocRef();
        // call method to update firebase accordingly
        removeItemFromCart(firestoreCallback, itemDocRef);
        ArrayList<Item> newItemList = userObject.getItems();
        newItemList.remove(itemToRemove);
        userObject.setItems(newItemList);
    }

    // update firebase respectively
    private void removeItemFromCart(final FirestoreCallback firestoreCallback, final DocumentReference itemDocRef){
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
                                    startScanning(firestoreCallback, trolleyDocRef, itemDocuments);
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
                        firestoreCallback.onItemCallback(null);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    firestoreCallback.onItemCallback(null);
                }
            }
        });
    }

    // update firebase to start rpi weight-checking
    private void startScanning(FirestoreCallback firestoreCallback, DocumentReference trolleyDocRef, ArrayList<DocumentReference> itemDocuments) {
        // create write batch to update firebase accordingly
        batch = db.batch();
        batch.update(trolleyDocRef, "items", itemDocuments);
        batch.update(trolleyDocRef, "scanning", true);
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listenForCorrectItem(firestoreCallback, trolleyDocRef);
                Log.i("console", "Scanning...");
            }
        });
    }

    // listen for correct item status from firebase
    private void listenForCorrectItem(final FirestoreCallback firestoreCallback, DocumentReference trolleyDocRef) {
        // attach listener to listen for change in correct_item field
        correctItemListener = trolleyDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    firestoreCallback.itemValidationCallback(null);
//                    Log.i("console", "Listen failed.", e);
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
//                    Log.i("console", "Current data: " + snapshot.getData());
                    Boolean itemValidationStatus = snapshot.getBoolean("correct_item");
                    firestoreCallback.itemValidationCallback(itemValidationStatus);
                    if (itemValidationStatus) {
                        // detach listener
                        correctItemListener.remove();
                        // reset fields to idle state
                        resetTrolleyScanningStatus(trolleyDocRef);
                    }
                } else {
//                    Log.i("console", "Current data: null");
                    firestoreCallback.itemValidationCallback(null);
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
    void cancelOperation(final FirestoreCallback firestoreCallback, Item item, Boolean cancelAdd) {
        correctItemListener.remove();
        DocumentReference trolleyDocRef = userObject.getTrolleyDoc();
        resetTrolleyScanningStatus(trolleyDocRef);
        ArrayList<DocumentReference> newItemDocuments = userObject.getItemDocuments();
        if (cancelAdd) {
            newItemDocuments.remove(item.getItemDocRef());
        } else {
            newItemDocuments.add(item.getItemDocRef());
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
        userObject.setItemDocuments(newItemDocuments);
    }

    void getItemsInTrolley (FirestoreCallback firestoreCallback) {
        ArrayList<Item> items = userObject.getItems();
        for (Item item : items) {
            firestoreCallback.onItemCallback(item);
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
        userObject.setUserId(null);
    }

    /*
    //function to read ALL documents in a collection
    void Read(String collection) {
        CollectionReference docRef = db.collection(collection);
        docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }


    //function to read a document
    void Read(String collection, String document) {
        DocumentReference docRef = db.collection(collection).document(document);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getDouble("cur_weight"));
                        Log.d(TAG, "DocumentSnapshot data: " + document.getDocumentReference("user"));

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }*/
}

