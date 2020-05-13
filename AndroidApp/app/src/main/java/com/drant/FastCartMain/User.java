package com.drant.FastCartMain;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;

import static com.drant.FastCartMain.FirebaseCallback.dbHandler;

public class User implements UpdateUserCallback {
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
        Log.i("items", "others12: " + instance + this.items.toString());

        this.userId = userId;
        this.setUserDoc();
        Log.i("console", this.userId);
    }

    String getUserId() {
        Log.i("items", "others1: " + instance + this.items.toString());
        return this.userId;
    }

    private void setUserDoc() {
        Log.i("items", "others2: " + instance + this.items.toString());
        this.userDocRef = DatabaseHandler.getInstance().saveUserDocument(this.userId);
    }

    DocumentReference getUserDoc() {
        Log.i("items", "others3: " + instance + this.items.toString());
        return this.userDocRef;
    }

    public void setTrolleyId(String trolleyId) {
        Log.i("items", "others4: " + instance + this.items.toString());
        this.trolleyId = trolleyId;
        this.setTrolleyDoc();
    }

    String getTrolleyId() {
        Log.i("items", "others5: " + instance + this.items.toString());
        return this.trolleyId;
    }

    private void setTrolleyDoc() {
        Log.i("items", "others6: " + instance + this.items.toString());
        this.trolleyDocRef = dbHandler.saveTrolleyDocument(this.trolleyId);
//        dbHandler.listenForItemChanges(User.this);
    }

    DocumentReference getTrolleyDoc() {
        Log.i("items", "others7: " + instance + this.items.toString());
        return this.trolleyDocRef;
    }

    void setItems(ArrayList<Item> items) {
        this.items = items;
        Log.i("items", "setting: " + instance + this.items.toString());
    }

    void setItems(ArrayList<Item> items, FirebaseCallback firebaseCallback) {
        this.items = items;
        Log.i("items", "setting: " + instance + this.items.toString());
        DatabaseHandler.getInstance().getItemsInLocalTrolley(firebaseCallback);

    }

    public ArrayList<Item> getItems() {
        Log.i("items", "getting: " + instance + this.items.toString());
        return this.items;
    }

    public void setItemDocuments(ArrayList<DocumentReference> itemDocuments) {
        Log.i("items", "others8: " + instance + this.items.toString());
        this.itemDocuments = itemDocuments;
        Log.i("items", "others8 after: " + instance + this.items.toString());

    }

    public ArrayList<DocumentReference> getItemDocuments() {
        Log.i("items", "others9: " + instance + this.items.toString());
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
        Log.i("items", "others10: " + instance + this.items.toString());
        Log.i("console", "update local items");
        Log.i("console", itemList.toString());
        this.setItems(itemList);
        Log.i("items", "others10: " + instance + this.items.toString());
    }

    @Override
    public void updateShoppingHist(ArrayList<Item> itemList, String total_price, Date timeOfTransaction){
        Log.i("items", "others11: " + instance + this.items.toString());
        Log.i("console", "update shopping hist");
    }
}
