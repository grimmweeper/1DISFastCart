package com.drant.FastCartMain;

import android.util.Log;

import com.drant.FastCartMain.ui.purchasehistory.HistorySession;
import com.google.firebase.firestore.DocumentReference;

import java.lang.annotation.Documented;
import java.math.BigDecimal;
import java.util.ArrayList;

public class User {
    private String userId;
    private DocumentReference userDocRef;
    private String trolleyId;
    private DocumentReference trolleyDocRef;
    private ArrayList<Item> items = new ArrayList<Item>();
    private ArrayList<DocumentReference> itemDocuments = new ArrayList<DocumentReference>();

    //huiqing: Instantiating arraylist of history sessions (have not call setter and getter yet)
    private ArrayList<HistorySession> historysessions = new ArrayList<>();

    private static User user = new User();
//    private User user;
//    private DocumentReference productDocRef;

//    private User(){
//        // Access a Cloud Firestore instance from your Activity
//        user = user.getInstance();
//    }

    public static User getInstance() {
        return user;
    }

    DatabaseHandler dbHandler = DatabaseHandler.getInstance();

    void setUserId(String userId) {
        this.userId = userId;
        this.setUserDoc();
        // TODO: Hardcoded - REMOVE
        this.setTrolleyId("gjDLnPSnMAul7MR8dBaI");

        Log.i("console", this.trolleyId);
    }

    String getUserId() {
        return this.userId;
    }

    private void setUserDoc() {
        this.userDocRef = dbHandler.saveUserDocument(this.userId);
    }

    DocumentReference getUserDoc() {
        return this.userDocRef;
    }

    void setTrolleyId(String trolleyId) {
        this.trolleyId = trolleyId;
        this.setTrolleyDoc();
    }

    String getTrolleyId() {
        return this.trolleyId;
    }

    private void setTrolleyDoc() {
        this.trolleyDocRef = dbHandler.saveTrolleyDocument(this.trolleyId);
    }

    DocumentReference getTrolleyDoc() {
        return this.trolleyDocRef;
    }

    void setItems(ArrayList<Item> items) {
        this.items = items;
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

//    User(String userId){
//        this.setUserId(userId);
//    }

//    User(String user_id, String trolley_id){
//        this.setUser_id(user_id);
//        this.setTrolley_id(trolley_id);
//        this.items = getItemsFromDB();
//    }
//
//    User(String user_id, String trolley_id, ArrayList<Item> items){
//        this.setUser_id(user_id);
//        this.setTrolley_id(trolley_id);
//        this.setItems(items);
//    }

//    ArrayList getItemsFromDB(){
//        ArrayList itemsList = new ArrayList<Item>();
//        return this.itemsList;
//    }

    public BigDecimal getCartTotal(){
        BigDecimal total = new BigDecimal("0.00");

        ArrayList<Item> userItems = this.getItems();
        for (Item i: userItems){
            total = total.add(i.getPrice());
        }
        return total;
    }
}
