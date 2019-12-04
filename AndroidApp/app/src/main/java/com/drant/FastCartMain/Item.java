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
//        loadBitmap(imageRef);
//        loadImage(imageRef);
//        this.itemImage = loadImage(this.imageRef);
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

    //    void loadImage(String urlDisplay) {
//        Bitmap image = null;
//        try {
//            InputStream in = new java.net.URL(urlDisplay).openStream();
//            image = BitmapFactory.decodeStream(in);
//            this.itemImage = image;
//        } catch (Exception e) {
//            Log.i("console", e.toString() + "hi");
//            e.printStackTrace();
//        }
////        return itemImage;
//    }

    //    public BigDecimal getWeight() {
//        return weight;
//    }
//    public void loadBitmap(String url)
//    {
//        Bitmap bm = null;
//        InputStream is = null;
//        BufferedInputStream bis = null;
//        try
//        {
//            URLConnection conn = new URL(url).openConnection();
//            conn.connect();
//            is = conn.getInputStream();
//            bis = new BufferedInputStream(is, 8192);
//            bm = BitmapFactory.decodeStream(bis);
//            this.itemImage = bm;
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        finally {
//            if (bis != null)
//            {
//                try
//                {
//                    bis.close();
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//            }
//            if (is != null)
//            {
//                try
//                {
//                    is.close();
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        }
////        return bm;
//    }
}
