package com.drant.FastCartMain;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

public class CartActivity extends ListActivity {

    Button buttonAdd;
    TextView cartTotal;

    BigDecimal price1 = new BigDecimal(1.60);
    BigDecimal price2 = new BigDecimal(2.95);
    BigDecimal price3 = new BigDecimal(4.60);

    ArrayList<String> itemArray = new ArrayList<String>(Arrays.asList("Apple", "Orange", "Pear"));
    ArrayList<BigDecimal> priceArray = new ArrayList<BigDecimal>(Arrays.asList(price1, price2, price3));
    ArrayList<Integer> imageArray = new ArrayList<>(Arrays.asList(R.drawable.fries, R.drawable.fries, R.drawable.fries));


    ArrayList<String> cartItem = new ArrayList<>();
    ArrayList<BigDecimal> cartPrice = new ArrayList<>();
    ArrayList<Integer> cartImage = new ArrayList<>();

    ListView listView;
    CartListAdaptor adapter;

    int clickCounter=0;

    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // get footer
        ListView listView = findViewById(android.R.id.list);
        LayoutInflater layoutinflater = getLayoutInflater();
        ViewGroup footer = (ViewGroup)layoutinflater.inflate(R.layout.listview_footer, listView, false);
        listView.addFooterView(footer);

        adapter = new CartListAdaptor(this, cartItem, cartPrice, cartImage);

        // todo LUOQI: change to compile cart when View Cart button is pressed
        buttonAdd = findViewById(R.id.addBtn);


        cartTotal = (TextView) findViewById(R.id.cartTotal);


        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickCounter < 3) {
                    adapter.add(itemArray.get(clickCounter), priceArray.get(clickCounter));
                    clickCounter += 1;
                }else{
                    clickCounter = 0;
                    adapter.add(itemArray.get(clickCounter), priceArray.get(clickCounter));
                }

                BigDecimal sum = new BigDecimal(0);
                for(BigDecimal d : cartPrice)
                    sum = sum.add(d);

                // initialise the intent when View Cart button is clicked.
                Intent intent = getIntent();
                String message = intent.getStringExtra(sum.toString());
                cartTotal.setText(message);
            }
        });

        setListAdapter(adapter);
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




