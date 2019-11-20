package com.drant.FastCartMain;

import java.math.BigDecimal;

public class Item {
    private String name;
    private BigDecimal price;
    private String imageRef;
    private BigDecimal weight;

    Item(String name, String price, String imageRef, String weight){
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

    public String getImageRef() {
        return imageRef;
    }

    public BigDecimal getWeight() {
        return weight;
    }
}
