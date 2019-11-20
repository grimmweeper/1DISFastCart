package com.drant.FastCartMain;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;

public class User {
    private String user_id;
    private String trolley_id;
    private ArrayList<Items> items;

    private void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    private String getUser_id() {
        return user_id;
    }

    private void setTrolley_id(String trolley_id) {
        this.trolley_id = trolley_id;
    }

    private String getTrolley_id() {
        return trolley_id;
    }

    private void setItems(ArrayList<Items> items) {
        this.items = items;
    }

    private ArrayList<Items> getItems() {
        return items;
    }

    User(String user_id){
        this.setUser_id(user_id);
        this.setTrolley_id(trolley_id);
        this.items = getItemsFromDB();
    }

    User(String user_id, String trolley_id){
        this.setUser_id(user_id);
        this.setTrolley_id(trolley_id);
        this.items = getItemsFromDB();
    }

    User(String user_id, String trolley_id, ArrayList<Items> items){
        this.setUser_id(user_id);
        this.setTrolley_id(trolley_id);
        this.setItems(items);
    }

    ArrayList getItemsFromDB(){
        ArrayList itemsList = new ArrayList<Items>();
        return itemsList;
    }

    public BigDecimal getCartTotal(){
        BigDecimal total = new BigDecimal("0.00");

        ArrayList<Items> userItems = this.getItems();
        for (Items i: userItems){
            total = total.add(i.getPrice());
        }
        return total;
    }
}
