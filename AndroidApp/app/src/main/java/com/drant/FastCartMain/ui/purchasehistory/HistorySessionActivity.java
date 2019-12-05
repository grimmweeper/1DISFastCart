package com.drant.FastCartMain.ui.purchasehistory;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drant.FastCartMain.Item;
import com.drant.FastCartMain.R;

import java.math.BigDecimal;
import java.util.ArrayList;

public class HistorySessionActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private HistorySessionAdapter adapter;
    private ArrayList<HistorySession> historyList;
    private ArrayList<Item> cartArrayList;
    private ArrayList<Item> cartArrayList2;
    private ImageView backbuton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histsess);

        initView();

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("Purchase History");
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        historyList = new ArrayList<>();
        cartArrayList = new ArrayList<>();
        cartArrayList2 = new ArrayList<>();
        adapter = new HistorySessionAdapter(this, historyList);
        recyclerView.setAdapter(adapter);
        createListData();

    }


    //huiqing!! For now this portion is hardcoded. Idea is to add all the Items objects into an arraylist<Item>
    //the arraylist of items will be nested inside the first outer recyclerview (history session)
    //This arraylist<Item> is parsed in as argument into HistorySession class, which will be added into an arraylist<HistorySession>

    private void createListData() {
        //First recyclerview
        Item cart = new Item("if Coconut Water", new BigDecimal(1.6).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), new BigDecimal(0.750).setScale(3,BigDecimal.ROUND_HALF_UP).toString(), null);
        cartArrayList.add(cart);
        cart = new Item("Ruffles", new BigDecimal(2.40).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), new BigDecimal(0.500).setScale(3,BigDecimal.ROUND_HALF_UP).toString(), null);
        cartArrayList.add(cart);
        cart = new Item("Oreo", new BigDecimal(0.90).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), new BigDecimal(0.750).setScale(3,BigDecimal.ROUND_HALF_UP).toString(), null);
        cartArrayList.add(cart);
        cart = new Item("IndoMee", new BigDecimal(2.40).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), new BigDecimal(0.500).setScale(3,BigDecimal.ROUND_HALF_UP).toString(), null);
        cartArrayList.add(cart);

        HistorySession sess1 = new HistorySession(cartArrayList,"Bedok Mall", "01-DEC-19",new BigDecimal(7.3).setScale(2,BigDecimal.ROUND_HALF_UP));
        historyList.add(sess1);

        //Second recyclerview
        cart = new Item("Fisherman's Friend Lemon", new BigDecimal(1.95).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), new BigDecimal(0.750).setScale(3,BigDecimal.ROUND_HALF_UP).toString(), null);
        cartArrayList2.add(cart);
        cart = new Item("if Coconut Water", new BigDecimal(1.6).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), new BigDecimal(0.500).setScale(3,BigDecimal.ROUND_HALF_UP).toString(), null);
        cartArrayList2.add(cart);
        cart = new Item("Ribena", new BigDecimal(1.70).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), new BigDecimal(0.750).setScale(3,BigDecimal.ROUND_HALF_UP).toString(), null);
        cartArrayList2.add(cart);
        cart = new Item("Cup Noodles Tom Yum", new BigDecimal(2.40).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), new BigDecimal(0.500).setScale(3,BigDecimal.ROUND_HALF_UP).toString(), null);
        cartArrayList2.add(cart);

        HistorySession sess2 = new HistorySession(cartArrayList2,"Tampines 1", "04-DEC-19",new BigDecimal(7.65).setScale(2,BigDecimal.ROUND_HALF_UP));
        historyList.add(sess2);

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
