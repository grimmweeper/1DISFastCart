package com.drant.FastCartMain;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;

public class Item {
    private String name;
    private BigDecimal price;
    private String imageRef;
//    private BigDecimal weight;
    private DocumentReference itemDocRef;
    private Bitmap itemImage;

    Item(String name, String price, String imageRef, DocumentReference itemDocRef){
        this.name = name;
        this.price = new BigDecimal(price);
        this.imageRef = imageRef;
        this.itemDocRef = itemDocRef;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getImageRef() {
        return imageRef;
    }

    public DocumentReference getItemDocRef() {
        return itemDocRef;
    }

    public Bitmap getItemImage() {
        return itemImage;
    }

    public void setItemImage(Bitmap itemImage) {
        this.itemImage = itemImage;
    }
}
