package com.drant.FastCartMain;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

import static com.drant.FastCartMain.FirebaseCallback.dbHandler;

public class User {
    private static String userId;
    private static DocumentReference userDocRef;
    private static String trolleyId;
    private static DocumentReference trolleyDocRef;
    private static ArrayList<Item> items = new ArrayList<Item>();
    private static ArrayList<DocumentReference> itemDocuments = new ArrayList<DocumentReference>();

    private static User instance;

    void createNewUser() {
        instance = new User();
    }

    public static User getInstance() {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }

    void setUserId(String userId) {
        this.userId = userId;
        this.setUserDoc();
    }

    String getUserId() {
        return this.userId;
    }

    private void setUserDoc() {
        this.userDocRef = DatabaseHandler.getInstance().saveUserDocument(this.userId);
    }

    DocumentReference getUserDoc() {
        return this.userDocRef;
    }

    public void setTrolleyId(String trolleyId) {
        this.trolleyId = trolleyId;
        this.setTrolleyDoc();
    }

    String getTrolleyId() {
        return this.trolleyId;
    }

    private void setTrolleyDoc() {
        this.trolleyDocRef = dbHandler.saveTrolleyDocument(this.trolleyId);
//        dbHandler.listenForItemChanges(User.this);
    }

    DocumentReference getTrolleyDoc() {
        return this.trolleyDocRef;
    }

    void setItems(ArrayList<Item> items, FirebaseCallback firebaseCallback) {
        this.items = items;
        DatabaseHandler.getInstance().getItemsInLocalTrolley(firebaseCallback);

    }

    ArrayList<Item> getItems() {
        return this.items;
    }

    public void setItemDocuments(ArrayList<DocumentReference> itemDocuments) {
        this.itemDocuments = itemDocuments;

    }

    public ArrayList<DocumentReference> getItemDocuments() {
        return this.itemDocuments;
    }
}
