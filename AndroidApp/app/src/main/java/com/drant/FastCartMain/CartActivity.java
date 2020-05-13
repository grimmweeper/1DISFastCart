package com.drant.FastCartMain;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;

public class CartActivity extends Fragment implements FirebaseCallback {

    private static final String TAG = "CartActivityFragment";

    private RecyclerView mRecyclerView;
    private recyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    TextView cartTotal;
    Button buttonCheckout;
    AlertDialog alertIllop;

    boolean safecheck = false;

    View view;
    ViewGroup container;

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
    public void checkIllopCallback(Boolean illopStatus){
        Log.i("console","Illop:" + illopStatus.toString());
        Log.i("console","Safecheck"+ String.valueOf(safecheck));
        Log.i("console", alertIllop + "sth");

        if(illopStatus) {
//            alertIllop = new AlertDialog.Builder(getActivity()).create();
            alertIllop.setIcon(R.drawable.warning);
            alertIllop.setTitle("Warning");
            alertIllop.setMessage("Please return to the cart's previous state");
            //alertIllop.setMessage("Thank you for shopping with us.");
            alertIllop.show();
            alertIllop.setCancelable(false);
            alertIllop.setCanceledOnTouchOutside(false);
            safecheck = true;
            Log.i("console", "callback for cart");

        } else if (alertIllop.isShowing()) {
//        } else if (!illopStatus && safecheck) {
            alertIllop.dismiss();
            safecheck = false;
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup containerGroup, @Nullable Bundle savedInstanceState) {
        container = containerGroup;
        view = inflater.inflate(R.layout.activity_cart, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new recyclerAdapter(cart);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        alertIllop = new AlertDialog.Builder(getContext()).create();

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

                        AlertDialog alertRemove = new AlertDialog.Builder(getActivity()).create();
                        alertRemove.setTitle("Confirm item removal");
                        alertRemove.setMessage("Please remove item from cart");
                        alertRemove.show();

                        mAdapter.notifyDataSetChanged();
                        dbHandler.removeItemFromCart(CartActivity.this, item);

                        cartTotal.setText(mAdapter.getTotalPrice());

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
                alertCheckout.setIcon(R.drawable.greentick);
                alertCheckout.setTitle("Checkout Success");
                alertCheckout.setMessage("Thank you for shopping with us.");
                alertCheckout.show();

                mAdapter.clearCart();
                cartTotal.setText(getCartTotal(cart));
                dbHandler.checkOut();
            }
        });

    }

    public String getCartTotal(ArrayList<Item> cart){
        BigDecimal sum = new BigDecimal(0);
        for (Item i: cart){
            sum = sum.add(i.getPrice());
        }

        return "Cart Total: $" + sum.toString();
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            dbHandler.listenForIllop(this);
            Log.i("console", "start");
        } catch (Exception e) {
            Log.i("console", e.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        dbHandler.getItemsInLocalTrolley(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("console", "detaching on cart");
        dbHandler.detachListener("illop");
    }
}




