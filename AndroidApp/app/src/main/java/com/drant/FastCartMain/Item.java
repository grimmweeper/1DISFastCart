package com.drant.FastCartMain;

import com.google.firebase.firestore.DocumentReference;

import java.math.BigDecimal;

public class Item {
    private String name;
    private BigDecimal price;
    private String imageRef;
//    private BigDecimal weight;
    private DocumentReference itemDocRef;

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

    //    public BigDecimal getWeight() {
//        return weight;
//    }
}
