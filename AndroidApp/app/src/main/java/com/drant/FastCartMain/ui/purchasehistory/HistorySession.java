package com.drant.FastCartMain.ui.purchasehistory;

import android.util.Log;

import com.drant.FastCartMain.Item;

import java.math.BigDecimal;
import java.util.ArrayList;

public class HistorySession {

    private ArrayList<Item> cartlist;
    private String location;
    private String date;
    private BigDecimal totalprice;

    public HistorySession(ArrayList<Item> cartlist, String location, String date, BigDecimal totalprice) {
        this.cartlist = cartlist;
        this.location = location;
        this.date = date;
        this.totalprice = totalprice;
    }

    public ArrayList<Item> getCartlist() {
        return cartlist;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public BigDecimal getTotalprice() {
        return totalprice;
    }

    public void setCartlist(ArrayList<Item> cartlist) {
        this.cartlist = cartlist;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTotalprice(BigDecimal totalprice) {
        this.totalprice = totalprice;
    }
}
