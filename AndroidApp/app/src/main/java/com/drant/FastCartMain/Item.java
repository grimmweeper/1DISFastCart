package com.drant.FastCartMain;

import android.graphics.Bitmap;

import com.google.firebase.firestore.DocumentReference;

import java.math.BigDecimal;
import java.util.Map;

public class Item {
    private String name;
    private BigDecimal price;
    private String imageRef;
//    private BigDecimal weight;
    private DocumentReference itemDocRef;
    private Bitmap itemImage;

    public Item(String name, String price, String imageRef, DocumentReference itemDocRef){
        this.name = name;
        this.price = new BigDecimal(price);
        this.imageRef = imageRef;
        this.itemDocRef = itemDocRef;
    }

    public Item(Map<String,Object> itemMap) {
        this.name = (String) itemMap.get("name");
        this.price = new BigDecimal(itemMap.get("price").toString());
        this.imageRef = (String) itemMap.get("img");
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
