package com.drant.FastCartMain;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;

import java.io.InputStream;
import java.lang.annotation.Documented;
import java.math.BigDecimal;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;

public class User implements UpdateUserCallback {
    private String userId;
    private DocumentReference userDocRef;
    private String trolleyId;
    private DocumentReference trolleyDocRef;
    private ArrayList<Item> items = new ArrayList<Item>();
    private ArrayList<DocumentReference> itemDocuments = new ArrayList<DocumentReference>();

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
        this.setTrolleyId("ZZafaKzVTvmlreT99wBL");
        //this.setTrolleyId("gjDLnPSnMAul7MR8dBaI");
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

    public void setTrolleyId(String trolleyId) {
        this.trolleyId = trolleyId;
        this.setTrolleyDoc();
    }

    String getTrolleyId() {
        return this.trolleyId;
    }

    private void setTrolleyDoc() {
        this.trolleyDocRef = dbHandler.saveTrolleyDocument(this.trolleyId);
        dbHandler.listenForItemChanges(User.this);
//        dbHandler.getItemsInFirebaseTrolley(User.this);
    }

    DocumentReference getTrolleyDoc() {
        return this.trolleyDocRef;
    }

    void setItems(ArrayList<Item> items) {
        //dbHandler.getItemsInFirebaseTrolley(User.this);
        this.items = items;
    }

    public ArrayList<Item> getItems() {
        return this.items;
    }

    public void setItemDocuments(ArrayList<DocumentReference> itemDocuments) {
        this.itemDocuments = itemDocuments;
    }

    public ArrayList<DocumentReference> getItemDocuments() {
        return this.itemDocuments;
    }

    public BigDecimal getCartTotal(){
        BigDecimal total = new BigDecimal("0.00");

        ArrayList<Item> userItems = this.getItems();
        for (Item i: userItems){
            total = total.add(i.getPrice());
        }
        return total;
    }

    @Override
    public void updateLocalItems(ArrayList<Item> itemList){//ArrayList<Item> Items, ArrayList<DocumentReference> ItemDocs) {
        Log.i("console", "update local items");
        Log.i("console", itemList.toString());
        this.setItems(itemList);
    }


}
