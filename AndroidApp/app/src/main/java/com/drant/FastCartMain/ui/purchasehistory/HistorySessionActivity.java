package com.drant.FastCartMain.ui.purchasehistory;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drant.FastCartMain.Item;
import com.drant.FastCartMain.LoginActivity;
import com.drant.FastCartMain.NavActivity;
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
        Item cart = new Item("if Coconut Water", new BigDecimal(1.6).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), "https://www.waangoo.com/content/images/thumbs/0008355_if-coconut-water-local-sensation_600.jpeg", null);
        cartArrayList.add(cart);
        cart = new Item("Ruffles", new BigDecimal(2.40).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), "https://i5.walmartimages.com/asr/03afef91-71f5-4583-afbc-ebaaa9061127_1.d7f42121cf8c11e55cb63061a407ca1a.jpeg", null);
        cartArrayList.add(cart);
        cart = new Item("Oreo", new BigDecimal(0.90).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), "https://grocermart.com/image/cache/data/Misc/Snacks/IMG_20321-1800x1800.jpg", null);
        cartArrayList.add(cart);
        cart = new Item("IndoMee", new BigDecimal(2.40).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), "https://www.tastefuldelights.com.au/193247-thickbox_default/Array.jpg", null);
        cartArrayList.add(cart);

        HistorySession sess1 = new HistorySession(cartArrayList,"SUTD", "06-DEC-19",new BigDecimal(7.3).setScale(2,BigDecimal.ROUND_HALF_UP));
        historyList.add(sess1);

        //Second recyclerview
        cart = new Item("Fisherman's Friend Lemon", new BigDecimal(1.95).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), "https://www.guardian.com.sg/medias/001641-1-zoom?context=bWFzdGVyfHJvb3R8NjU1NjJ8aW1hZ2UvanBlZ3xoZGMvaDFkLzkwNTM1ODg0ODgyMjIuanBnfGNhNDEzZjEyODk1ODA0YmMxZWJlZDhjMmNiNmNlYjAzOTFjZjcxZmRkZTZjMjU0NGIyNmNiZGUxYjJhZjhkYzE", null);
        cartArrayList2.add(cart);
        cart = new Item("if Coconut Water", new BigDecimal(1.6).setScale(2,BigDecimal.ROUND_HALF_UP).toString(),"https://www.waangoo.com/content/images/thumbs/0008355_if-coconut-water-local-sensation_600.jpeg", null);
        cartArrayList2.add(cart);
        cart = new Item("Ribena", new BigDecimal(1.70).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), "https://www.directwholesale.com.sg/pub/media/catalog/product/cache/image/1000x1320/e9c3970ab036de70892d86c6d221abfe/8/8/8885012290333_0194_1452534019286.jpg", null);
        cartArrayList2.add(cart);
        cart = new Item("Cup Noodles Tom Yum", new BigDecimal(2.40).setScale(2,BigDecimal.ROUND_HALF_UP).toString(), "https://d26vuck1qo29uk.cloudfront.net/uploads/products/Nissin-Cup-Noodles-Tom-Yam-Seafood-Flavour-75g-feed-.jpg", null);
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

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(HistorySessionActivity.this, NavActivity.class);
        startActivity(intent);
        finish();
    }

}
