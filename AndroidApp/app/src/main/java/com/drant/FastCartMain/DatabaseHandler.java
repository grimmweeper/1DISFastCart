package com.drant.FastCartMain;

import android.support.annotation.NonNull;
import android.util.AtomicFile;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static com.drant.FastCartMain.LoginActivity.userObject;

interface FirestoreCallback{
    DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    void onItemCallback(Item item);
//    void onItemsCallback(ArrayList<Item> allItems);
//    void onUserInfoCallback();
}

public class DatabaseHandler {
    private static DatabaseHandler instance = null;
    private FirebaseFirestore db;
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
    }

    // TODO: function to remove all items from cart

    // add item to cart
    void addItemToCart(final FirestoreCallback firestoreCallback, String barcode){
        // get product document reference to
        DocumentReference productDocRef = db.collection("products").document(barcode);
//        addItemToCart(firestoreCallback, userObject.getTrolleyDoc(), productDocRef);
        productDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String price = document.getDouble("price").toString();
                        String weight = "0.0";
//                        String weight = document.getDouble("weight").toString();
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

    void addItemToCart(final FirestoreCallback firestoreCallback, final DocumentReference trolleyDocRef, final DocumentReference itemToAdd){
        trolleyDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<DocumentReference> itemDocuments = (ArrayList<DocumentReference>) document.get("items");
//                        userObject.setItemDocuments(itemDocuments);
                        Log.i("console", itemDocuments.toString());
                        itemDocuments.add(itemToAdd);
                        trolleyDocRef
                                .update("items", itemDocuments)
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

    // TODO: function to listen for weight change in cart
    // TODO: function to remove item from cart

    // get all items in cart
    void getProductDetails(final FirestoreCallback firestoreCallback, DocumentReference itemDocument){
        Log.i("console", "hi");
//        String trolleyId = userObject.getTrolleyId();
        String trolleyId = userObject.getTrolleyId();
        DocumentReference trolleyDocRef = db.collection("trolleys").document(trolleyId);
        itemDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists() && document.contains("name") && document.contains("price") && document.contains("img")) {
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
