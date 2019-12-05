package com.drant.FastCartMain;

import android.os.Bundle;
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
import java.util.Arrays;

public class CartActivity extends Fragment {

    private static final String TAG = "CartActivityFragment";

    private RecyclerView mRecyclerView;
    private recyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    TextView cartTotal;
    Button buttonAdd;
    Button buttonCheckout;

    //hardcoded array to get items from (button accesses these items)
    Item item1 = new Item("Apple", "1.50", R.drawable.ic_android);
    Item item2 = new Item("Orange", "0.90", R.drawable.ic_android);
    Item item3 = new Item("Grapes", "4.75", R.drawable.ic_android);
    ArrayList<Item> items = new ArrayList<>(Arrays.asList(item1, item2, item3));

    //start w empty cart to fill
    ArrayList<Item> cart = new ArrayList<Item>();


    int clickCounter=0;


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
                        mAdapter.removeItem(position);
                        cartTotal.setText(getCartTotal(cart));

                        Toast.makeText(getActivity(), "Item removed from cart", Toast.LENGTH_SHORT).show();
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        cartTotal = view.findViewById(R.id.cartTotal);
        buttonAdd =(Button) view.findViewById(R.id.addBtn);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (clickCounter < 3) {
                    mAdapter.addItem(items.get(clickCounter));
                    clickCounter += 1;
                } else {
                    clickCounter = 0;
                    mAdapter.addItem(items.get(clickCounter));
                    clickCounter += 1;
                }

                // setting cart total to footer
                cartTotal.setText(getCartTotal(cart));
            }
        });

        buttonCheckout = (Button) view.findViewById(R.id.checkoutbtn);
        buttonCheckout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                AlertDialog alertCheckout = new AlertDialog.Builder(getActivity()).create();
                alertCheckout.setTitle("Checkout Success");
                alertCheckout.setMessage("Thank you for shopping with us.");
                alertCheckout.show();


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
}




