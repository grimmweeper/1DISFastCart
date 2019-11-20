package com.drant.FastCartMain;

import java.math.BigDecimal;

public class Items {
    private String name;
    private BigDecimal price;
    private String imageRef;

    Items(String name, String price, String imageRef){
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
}
