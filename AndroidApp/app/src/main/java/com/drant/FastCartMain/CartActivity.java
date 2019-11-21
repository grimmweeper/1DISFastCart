package com.drant.FastCartMain;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;



public class CartActivity extends ListActivity implements FirestoreCallback {

    TextView cartTotal;

    ArrayList<String> cartItem = new ArrayList<>();
    ArrayList<BigDecimal> cartPrice = new ArrayList<>();
    ArrayList<Integer> cartImage = new ArrayList<>();

    CartListAdaptor adapter;

//    int clickCounter = 0;

    @Override
    public void onItemsCallback(ArrayList<Item> allItems) {
        Log.i("console", "I've been called back");

        // get footer
        ListView listView = findViewById(android.R.id.list);
        LayoutInflater layoutinflater = getLayoutInflater();
        ViewGroup footer = (ViewGroup)layoutinflater.inflate(R.layout.listview_footer, listView, false);
        listView.addFooterView(footer);

        adapter = new CartListAdaptor(this, cartItem, cartPrice, cartImage);

        // todo LUOQI: change to compile cart when View Cart button is pressed
        cartTotal = (TextView) findViewById(R.id.cartTotal);

        for (Item currentItem : allItems) {
            adapter.add(currentItem.getName(), currentItem.getPrice());
            BigDecimal sum = new BigDecimal(0);
            Intent intent = getIntent();
            String message = intent.getStringExtra(sum.toString());
            cartTotal.setText(message);

        }
        setListAdapter(adapter);


    }

    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        DatabaseHandler dbHandler = DatabaseHandler.getInstance();
        Context thisActivityContext = getApplicationContext();
//        dbHandler.Read("users");
//        dbHandler.getProductDetails(this, "OcKH4TaO3BOo8NNYoEyD");
        dbHandler.getAllProductsDetails(this);


//        // get footer
//        ListView listView = findViewById(android.R.id.list);
//        LayoutInflater layoutinflater = getLayoutInflater();
//        ViewGroup footer = (ViewGroup)layoutinflater.inflate(R.layout.listview_footer, listView, false);
//        listView.addFooterView(footer);
//
//        adapter = new CartListAdaptor(this, cartItem, cartPrice, cartImage);
//
//        // todo LUOQI: change to compile cart when View Cart button is pressed
//        buttonAdd = findViewById(R.id.addBtn);
//        cartTotal = (TextView) findViewById(R.id.cartTotal);
//
//        buttonAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (clickCounter < 3){
//                    Item currentItem = allItems.get(clickCounter);
//                    System.out.println(allItems.get(clickCounter).getName());
////                    adapter.add(itemArray.get(clickCounter), priceArray.get(clickCounter));
//                    adapter.add(currentItem.getName(), currentItem.getPrice());
//                    clickCounter += 1;
//                }else{
//                    clickCounter = 0;
//                }
//
//                BigDecimal sum = new BigDecimal(0);
//                for(BigDecimal d : cartPrice)
//                    sum = sum.add(d);
//
//                // initialise the intent when View Cart button is clicked.
//                Intent intent = getIntent();
//                String message = intent.getStringExtra(sum.toString());
//                cartTotal.setText(message);
//            }
//        });
//
//        setListAdapter(adapter);
    }

    /* THIS CODE BELOW IS ALTERNATIVE TO setOnClickListener
    //    public void addItems(View view) {
    //        adapter.add(itemArray.get(clickCounter),
    //                priceArray.get(clickCounter));
    //        clickCounter += 1;
    //    }

     */
    public Double getCartTotal(ArrayList<Double> prices){
        double sum = 0;
        for(Double d : prices)
            sum += d;
        return sum;
    }

}




