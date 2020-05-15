package com.drant.FastCartMain;

import android.graphics.Bitmap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Item {
    private String name;
    private BigDecimal price;
    private String priceString;
    private String imageRef;
    private Bitmap itemImage;
    private String barcode;

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

    Map<String,Object> getFBItem(Boolean checkout) {
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

    Bitmap getItemImage() {
        return itemImage;
    }

    void setItemImage(Bitmap itemImage) {
        this.itemImage = itemImage;
    }

    String getBarcode() {
        return this.barcode;
    }
}
