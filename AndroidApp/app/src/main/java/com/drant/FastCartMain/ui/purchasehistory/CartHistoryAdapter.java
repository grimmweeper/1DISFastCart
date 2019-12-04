package com.drant.FastCartMain.ui.purchasehistory;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.drant.FastCartMain.DownloadImageTask;
import com.drant.FastCartMain.Item;
import com.drant.FastCartMain.R;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CartHistoryAdapter extends RecyclerView.Adapter<CartHistoryAdapter.CartHolder> {

    private Context context;
    private ArrayList<Item> carts;

    public CartHistoryAdapter(Context context, ArrayList<Item> carts) {
        this.context = context;
        this.carts = carts;
    }

    @NonNull
    @Override
    public CartHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new CartHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartHolder holder, int position) {
        Item cart = carts.get(position);
        holder.setDetails(cart);


    }

    @Override
    public int getItemCount() {
        return carts.size();
    }

    class CartHolder extends RecyclerView.ViewHolder {

        private TextView txtName, txtPrice, txtWeight;
        private ImageView imgSrc;

        CartHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            imgSrc = itemView.findViewById(R.id.imgSrc);

        }


        void setDetails(Item cart) {
            txtName.setText(cart.getName());
            txtPrice.setText(String.format(Locale.US, "$%s", cart.getPrice()));

            //I don't know if this is called correctly
            new DownloadImageTask(imgSrc).execute(cart.getImageRef());

        }
    }
}
