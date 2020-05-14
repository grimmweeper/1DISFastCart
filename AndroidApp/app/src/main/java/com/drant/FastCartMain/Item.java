package com.drant.FastCartMain;

import android.graphics.Bitmap;

import com.google.firebase.firestore.DocumentReference;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Item {
    private String name;
    private BigDecimal price;
    private String priceString;
    private String imageRef;
//    private BigDecimal weight;
    private DocumentReference itemDocRef;
    private Bitmap itemImage;
    private double qty;
    private String barcode;

    public Item(String name, String price, String imageRef, DocumentReference itemDocRef){
        this.name = name;
        this.priceString = price;
        this.price = new BigDecimal(price);
        this.imageRef = imageRef;
        this.itemDocRef = itemDocRef;
    }

    public Item(Map<String,Object> itemMap) {
        System.out.println(itemMap);
        this.name = (String) itemMap.get("name");
        this.priceString = itemMap.get("price").toString();
        this.price = new BigDecimal(this.priceString);
        this.imageRef = (String) itemMap.get("img");
        if (itemMap.containsKey("barcode")){
            this.barcode = (String) itemMap.get("barcode");
        }
    }

    public Map getFBItem(Boolean checkout) {
        Map<String,Object> FBItem = new HashMap<String,Object>();
        FBItem.put("name", this.name);
        FBItem.put("price", Double.parseDouble(this.priceString));
        FBItem.put("img", this.imageRef);
        if (!checkout) {
            FBItem.put("qty", 1);
        }
        return FBItem;
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

    public String getBarcode() {
        return this.barcode;
    }
}
