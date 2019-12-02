package com.drant.FastCartMain;

import java.math.BigDecimal;

public class Item {
    private String name;
    private BigDecimal price;
    private int imageRef;

    Item(String name, String price, int imageRef){
        this.name = name;
        this.price = new BigDecimal(price);
        this.imageRef = imageRef;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getImageRef() {
        return imageRef;
    }

}
