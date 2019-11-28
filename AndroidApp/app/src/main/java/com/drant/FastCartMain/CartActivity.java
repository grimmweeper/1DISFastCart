package com.drant.FastCartMain;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;

public class CartActivity extends ListActivity implements FirestoreCallback {

    TextView cartTotal;

    ArrayList<String> cartItem = new ArrayList<>();
    ArrayList<BigDecimal> cartPrice = new ArrayList<>();
    ArrayList<Integer> cartImage = new ArrayList<>();

    CartListAdaptor adapter;

    @Override
    public void onItemCallback(Item item) {
        cartTotal = (TextView) findViewById(R.id.cartTotal);
        adapter.add(item.getName(), item.getPrice());
        BigDecimal sum = new BigDecimal(0);
        Intent intent = getIntent();
        String message = intent.getStringExtra(sum.toString());
        cartTotal.setText(message);
        setListAdapter(adapter);
    }

    @Override
    public void itemValidationCallback(Boolean validItem){}

    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // get footer
        ListView listView = findViewById(android.R.id.list);
        LayoutInflater layoutinflater = getLayoutInflater();
        ViewGroup footer = (ViewGroup)layoutinflater.inflate(R.layout.listview_footer, listView, false);
        listView.addFooterView(footer);

        adapter = new CartListAdaptor(this, cartItem, cartPrice, cartImage);
//        dbHandler.Read("users");
//        dbHandler.getProductDetails(this, "OcKH4TaO3BOo8NNYoEyD");
//        dbHandler.linkTrolleyAndUser("vXjgwq9nsuMkapWsnnlcl0D32N22", "gjDLnPSnMAul7MR8dBaI");
        dbHandler.getUserProductsDetails(this, "erjaotT2n0ObxrqVRfrATEmqAJN2");
    }

//    public interface OnFragmentInteractionListener{
//
//    }

    /* THIS CODE BELOW IS ALTERNATIVE TO setOnClickListener
    //    public void addItems(View view) {
    //        adapter.add(itemArray.get(clickCounter),
    //                priceArray.get(clickCounter));
    //        clickCounter += 1;
    //    }

     */
//    public Double getCartTotal(ArrayList<Double> prices){
//        double sum = 0;
//        for(Double d : prices)
//            sum += d;
//        return sum;
//    }

}




