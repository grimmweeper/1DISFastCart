package com.drant.FastCartMain.ui.purchasehistory;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drant.FastCartMain.R;

import java.util.ArrayList;
import java.util.Locale;

public class HistorySessionAdapter extends RecyclerView.Adapter<HistorySessionAdapter.HistoryHolder> {

    private Context context;
    private ArrayList<HistorySession> histsess;
    private CartHistoryAdapter verticalAdapter;

    public HistorySessionAdapter(Context context, ArrayList<HistorySession> histsess) {
        this.context = context;
        this.histsess = histsess;
    }

    @NonNull
    @Override
    public HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_session, parent, false);
        return new HistoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHolder holder, int position) {
        HistorySession historySession = histsess.get(position);
        holder.setDetails(historySession);

        //bind the inner recycler view to an adapter

        verticalAdapter = new CartHistoryAdapter(context,histsess.get(position).getCartlist());
        holder.recyclerViewVertical.setAdapter(verticalAdapter);

        holder.itemView.setBackgroundColor(Color.parseColor("#ffdbcf"));

        }

    @Override
    public int getItemCount() {
        return histsess.size();
    }

    class HistoryHolder extends RecyclerView.ViewHolder {

        private TextView txtLocation, txtDate, txtTotalPrice;
        private RecyclerView recyclerViewVertical;

        private LinearLayoutManager verticalManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false);

        HistoryHolder(final View itemView) {
            super(itemView);

            recyclerViewVertical = itemView.findViewById(R.id.verticalRecycler);
            recyclerViewVertical.setHasFixedSize(true);
            recyclerViewVertical.setNestedScrollingEnabled(true);
            recyclerViewVertical.setLayoutManager(verticalManager);
            recyclerViewVertical.setItemAnimator(new DefaultItemAnimator());

            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);


        }


        void setDetails(HistorySession historySession) {
            txtLocation.setText(historySession.getLocation());
            txtDate.setText(historySession.getDate());
            txtTotalPrice.setText(String.format(Locale.US, "Total: $%s", historySession.getTotalprice()));


        }
    }
}

