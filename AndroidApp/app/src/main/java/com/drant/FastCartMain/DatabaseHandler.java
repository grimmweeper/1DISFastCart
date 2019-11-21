package com.drant.FastCartMain;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

interface FirestoreCallback{
//    void onItemCallback(Item item);
    void onItemsCallback(ArrayList<Item> allItems);
//    void onUserInfoCallback();
}

public class DatabaseHandler {
    private static DatabaseHandler instance = null;
    private FirebaseFirestore db;
    private DocumentReference productDocRef;

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

    // Product Functions
    void getProductDetails(final FirestoreCallback firestoreCallback, String barcode){
        productDocRef = db.collection("products").document(barcode);
        productDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
//                        firestoreCallback.onItemCallback(item);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
//        readData(firestoreCallback);
    }

    void getAllProductsDetails(final FirestoreCallback firestoreCallback){
        Log.i("console", "I've been called back");
        final ArrayList<Item> allItems = new ArrayList();
        CollectionReference docRef = db.collection("products");
        docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String name = document.getString("name");
                        String price = document.getDouble("price").toString();
                        String weight = document.getDouble("weight").toString();
                        String imageRef = document.getString("imageRef");
                        Item item = new Item(name, price, imageRef, weight);
                        allItems.add(item);
//                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                    firestoreCallback.onItemsCallback(allItems);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
//        productDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        String name = document.getString("name");
//                        String price = document.getDouble("price").toString();
//                        String weight = document.getDouble("weight").toString();
//                        String imageRef = document.getString("imageRef");
//                        Item item = new Item(name, price, imageRef, weight);
////                        firestoreCallback.onCallback(item);
//                    } else {
//                        Log.d(TAG, "No such document");
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });
    }

    // User Functions
    void linkTrolleyToUser(String userId, String trolleyId){
        // Create variables to be added
        DocumentReference trolleyDocRef = this.db.collection("trolleys").document(trolleyId);

        this.db.collection("users").document(userId)
                .update("trolley", trolleyDocRef)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("console", "DocumentSnapshot successfully added!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("console", "Error adding document", e);
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

