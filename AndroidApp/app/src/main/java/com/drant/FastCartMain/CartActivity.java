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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drant.FastCartMain.ui.scanitem.ScanItemFragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

public class CartActivity extends Fragment implements FirebaseCallback {

    private static final String TAG = "CartActivityFragment";

    private RecyclerView mRecyclerView;
    private recyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    TextView cartTotal;
    Button buttonAdd;
    Button buttonCheckout;

    //start w empty cart to fill
    ArrayList<Item> cart = new ArrayList<Item>();

    @Override
    public void onItemCallback(Item item){
        Log.i("console", "item callback");
    }

    @Override
    public void displayItemsCallback(ArrayList<Item> items) {
        Log.i("console", "display callback");
        mAdapter.displayItems(items);
        cartTotal.setText(getCartTotal(items));
    }

    @Override
    public void itemValidationCallback(Boolean validItem){
//        Log.i("console", "valid callback");
        if (validItem) {
            Toast.makeText(getActivity(), "Item removed from cart", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_cart, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new recyclerAdapter(cart);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                        recyclerAdapter.ExampleViewHolder itemViewHolder = (recyclerAdapter.ExampleViewHolder) viewHolder;
                        int position = itemViewHolder.getAdapterPosition();
                        Item item = mAdapter.getItemAtPos(position);
                        dbHandler.removeItemFromCart(CartActivity.this, item);
//                        mAdapter.removeItem(position);
                        cartTotal.setText(getCartTotal(cart));

                        Toast.makeText(getActivity(), "Item removed from cart", Toast.LENGTH_SHORT).show();
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        cartTotal = view.findViewById(R.id.cartTotal);

        buttonCheckout = (Button) view.findViewById(R.id.checkoutbtn);
        buttonCheckout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                AlertDialog alertCheckout = new AlertDialog.Builder(getActivity()).create();
                alertCheckout.setTitle("Checkout Success");
                alertCheckout.setMessage("Thank you for shopping with us.");
                alertCheckout.show();

                mAdapter.clearCart();
                cartTotal.setText(getCartTotal(cart));
            }
        });

        return view;
    }

    public String getCartTotal(ArrayList<Item> cart){
        BigDecimal sum = new BigDecimal(0);
        for (Item i: cart){
            sum = sum.add(i.getPrice());
        }

        return "Cart Total: $" + sum.toString();
    }

    @Override
    public void onResume(){
        super.onResume();
        dbHandler.getItemsInLocalTrolley(this);
    }
}




