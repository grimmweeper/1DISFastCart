package com.drant.FastCartMain;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

import static com.drant.FastCartMain.FirebaseCallback.dbHandler;

public class User {
    private static String userId;
    private static String userName;
    private static DocumentReference userDocRef;
    private static String trolleyId;
    private static DocumentReference trolleyDocRef;
    private static ArrayList<Item> items = new ArrayList<Item>();
    private static ArrayList<DocumentReference> itemDocuments = new ArrayList<DocumentReference>();
    private static FirebaseUser firebaseUser;

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

    void setFirebaseUser(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
        this.setUserId(this.getFirebaseUser().getUid());
        this.setUserName(this.getFirebaseUser().getDisplayName());
    }

    FirebaseUser getFirebaseUser () {
        return this.firebaseUser;
    }

    void setUserId(String userId) {
        this.userId = userId;
        this.setUserDoc(this.getUserId());
    }

    String getUserId() {
        return this.userId;
    }

    public static void setUserName(String userName) {
        User.userName = userName;
    }

    public static String getUserName() {
        return userName;
    }

    private void setUserDoc(String uid) {
        this.userDocRef = DatabaseHandler.getInstance().saveUserDocument(uid);
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
