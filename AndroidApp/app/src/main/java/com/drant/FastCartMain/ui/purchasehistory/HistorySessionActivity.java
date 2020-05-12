package com.drant.FastCartMain.ui.purchasehistory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drant.FastCartMain.Item;
import com.drant.FastCartMain.LoginActivity;
import com.drant.FastCartMain.NavActivity;
import com.drant.FastCartMain.R;
import com.drant.FastCartMain.UpdateUserCallback;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class HistorySessionActivity extends AppCompatActivity implements UpdateUserCallback {

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
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();
        cartArrayList = new ArrayList<>();
        cartArrayList2 = new ArrayList<>();
        adapter = new HistorySessionAdapter(this, historyList);
        recyclerView.setAdapter(adapter);
        dbHandler.getShoppingHist(this);
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

    @Override
    public void updateLocalItems(ArrayList<Item> itemList){

    }

    @Override
    public void updateShoppingHist(ArrayList<Item> itemList, String totalPrice, Date timeOfTransaction){
//        for (Item item : itemList) {
            String pattern = "dd-MM-yyyy HH:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(timeOfTransaction);
            HistorySession historySession = new HistorySession(itemList, "SUTD", date, new BigDecimal(totalPrice).setScale(2,BigDecimal.ROUND_HALF_UP));
//            Log.i("hist", "update: " + totalPrice);
            historyList.add(historySession);
//        }
        adapter.notifyDataSetChanged();
    }

}
