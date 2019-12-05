package com.drant.FastCartMain;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;

public class recyclerAdapter extends RecyclerView.Adapter<recyclerAdapter.ExampleViewHolder> {
    private ArrayList<Item> mCart;

    public static class ExampleViewHolder extends RecyclerView.ViewHolder{
        public ImageView mImageView;
        public TextView mTextView1;
        public TextView mTextView2;

        public ExampleViewHolder(View itemView){
            super(itemView);
            mImageView = itemView.findViewById(R.id.imageView);
            mTextView1 = itemView.findViewById(R.id.Line1);
            mTextView2 = itemView.findViewById(R.id.Line2);

        }
    }

    public recyclerAdapter(ArrayList<Item> cart){
        mCart = cart;
    }


    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout_main, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(v);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull ExampleViewHolder holder, int position) {
        Item currentItem = mCart.get(position);

//        new DownloadImageTask(holder.mImageView).execute(currentItem.getImageRef());
        Bitmap itemImage = currentItem.getItemImage();
        if (itemImage == null) {
            new DownloadImageTask(holder.mImageView, currentItem).execute(currentItem.getImageRef());
        } else {
            holder.mImageView.setImageBitmap(currentItem.getItemImage());
        }

        holder.mTextView1.setText(currentItem.getName());
        holder.mTextView2.setText("$" + currentItem.getPrice().toString());
    }

    @Override
    public int getItemCount() {
        return mCart.size();
    }

    public void displayItems(ArrayList<Item> items){
        mCart = items;
        notifyDataSetChanged();
    }

    public Item getItemAtPos(int position) {
        Item itemAtPos = mCart.get(position);
        return itemAtPos;
    }

    public void clearCart(){
        mCart.clear();
        notifyDataSetChanged();
    }

    public String getTotalPrice(){
        BigDecimal sum = new BigDecimal(0);
        for (Item i: mCart){
            sum = sum.add(i.getPrice());
        }
        return "Cart Total: $" + sum.toString();
    }

    public void addItem(Item item){
        mCart.add(item);
        notifyDataSetChanged();
    }



}
