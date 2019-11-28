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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;
//import static com.drant.FastCartMain.LoginActivity.userObject;

interface FirestoreCallback{
    DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    void onItemCallback(Item item);
    void itemValidationCallback(Boolean validItem);
//    void onItemsCallback(ArrayList<Item> allItems);
//    void onUserInfoCallback();
}

public class DatabaseHandler {
    private static DatabaseHandler instance = null;
    private FirebaseFirestore db;
    ListenerRegistration firebaseListener;
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
    void linkingFunction(DocumentReference mainDocRef, DocumentReference linkedDocRef, String fieldToUpdate) {
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
//        userObject.setTrolleyId(trolleyId);
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
//        userObject.setTrolleyId(null);
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
        // get product document reference to
        DocumentReference productDocRef = db.collection("products").document(barcode);
        Log.i("console", firestoreCallback.toString());
//        Log.i("console", userObject.getTrolleyDoc().toString());
//        Log.i("console", userObject.getTrolleyId());
        Log.i("console", productDocRef.toString());
        addItemToCart(firestoreCallback, productDocRef);
        productDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String price = document.getDouble("price").toString();
                        String weight = document.getDouble("weight").toString();
                        String imageRef = document.getString("img");
                        Item item = new Item(name, price, imageRef, weight);
                        firestoreCallback.onItemCallback(item);
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

    void addItemToCart(final FirestoreCallback firestoreCallback, final DocumentReference itemToAdd){
        DocumentReference trolleyDocRef = User.getInstance().getTrolleyDoc();
        trolleyDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<DocumentReference> itemDocuments = (ArrayList<DocumentReference>) document.get("items");
                        itemDocuments.add(itemToAdd);
                        trolleyDocRef
                                .update("items", itemDocuments)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("console", "Trolley has been successfully added to User!");
                                        startScanning(trolleyDocRef);
                                        listenForCorrectItem(firestoreCallback, trolleyDocRef);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i("console", "Error adding trolley to user.", e);
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

    void startScanning(DocumentReference trolleyDocRef) {
        trolleyDocRef
                .update("scanning", true)
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
    }

    void listenForCorrectItem(final FirestoreCallback firestoreCallback, DocumentReference trolleyDocRef) {
        firebaseListener = trolleyDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                        firebaseListener.remove();
                        resetTrolleyScanningStatus(trolleyDocRef);
                    }
                } else {
//                    Log.i("console", "Current data: null");
                    firestoreCallback.itemValidationCallback(null);
                }
            }
        });
    }

    void resetTrolleyScanningStatus(DocumentReference trolleyDocRef) {
        batch = db.batch();
        batch.update(trolleyDocRef, "scanning", false);
        batch.update(trolleyDocRef, "correct_item", false);
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.i("console", "Successfully reset");
            }
        });
    }

    // TODO: function to remove item from cart
    void removeItemToCart(final FirestoreCallback firestoreCallback, final DocumentReference trolleyDocRef, final DocumentReference itemToAdd){
        trolleyDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<DocumentReference> itemDocuments = (ArrayList<DocumentReference>) document.get("items");
                        itemDocuments.remove(itemToAdd);
                        trolleyDocRef
                                .update("items", itemDocuments)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("console", "Trolley has been successfully added to User!");
                                        startScanning(trolleyDocRef);
                                        listenForCorrectItem(firestoreCallback, trolleyDocRef);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i("console", "Error adding trolley to user.", e);
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

    // get all items in cart
    // TODO: make use of User Class
    void getProductDetails(final FirestoreCallback firestoreCallback, DocumentReference itemDocument){
//        String trolleyId = userObject.getTrolleyId();
//        String trolleyId = User.getInstance().getTrolleyId();
//        DocumentReference trolleyDocRef = db.collection("trolleys").document(trolleyId);
        itemDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String price = document.getDouble("price").toString();
                        String weight = document.getDouble("weight").toString();
                        String imageRef = document.getString("imageRef");
                        Item item = new Item(name, price, imageRef, weight);
                        firestoreCallback.onItemCallback(item);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    void getTrolleyProductsDetails(final FirestoreCallback firestoreCallback, DocumentReference trolleyDocRef){
        trolleyDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<DocumentReference> itemDocuments = (ArrayList<DocumentReference>) document.get("items");
//                        userObject.setItemDocuments(itemDocuments);
                        Log.i("console", itemDocuments.toString());
                        for (DocumentReference itemDocument : itemDocuments) {
                            Log.i("console", itemDocument.toString());
                            getProductDetails(firestoreCallback, itemDocument);
                        }
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

    void getUserProductsDetails(final FirestoreCallback firestoreCallback, String user_id){
        DocumentReference userDocRef = db.collection("users").document(user_id); // get user document reference from user_id
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        DocumentReference trolleyDocRef = document.getDocumentReference("trolley"); // get trolley document reference
                        getTrolleyProductsDetails(firestoreCallback, trolleyDocRef); // get products from trolley
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

    // TODO: remove all items in cart when checkout

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
    }
}

