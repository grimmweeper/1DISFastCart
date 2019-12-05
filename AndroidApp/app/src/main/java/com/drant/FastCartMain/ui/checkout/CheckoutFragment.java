package com.drant.FastCartMain.ui.checkout;

import android.app.Activity;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drant.FastCartMain.CartActivity;
import com.drant.FastCartMain.FirebaseCallback;
import com.drant.FastCartMain.Item;
import com.drant.FastCartMain.LoginActivity;
import com.drant.FastCartMain.R;
import com.drant.FastCartMain.recyclerAdapter;

import java.math.BigDecimal;
import java.util.ArrayList;

public class CheckoutFragment extends Fragment {

    public CheckoutFragment(){}


    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_cart,container,false);
        Log.i("sum", "oncreateview");
        return view;

        /** notificationsViewModel =
         ViewModelProviders.of(this).get(CheckoutViewModel.class);
         View root = inflater.inflate(R.layout.activity_cart, container, false);
         /**final TextView textView = root.findViewById(R.id.cart);
         notificationsViewModel.getText().observe(this, new Observer<String>() {
        @Override
        public void onChanged(@Nullable String s) {
        textView.setText(s);
        }
        });
         return root;*/
    }
}