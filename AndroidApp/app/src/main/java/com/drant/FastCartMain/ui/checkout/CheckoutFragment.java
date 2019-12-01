package com.drant.FastCartMain.ui.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.drant.FastCartMain.R;

public class CheckoutFragment extends Fragment {

    public CheckoutFragment(){}


    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_cart,container,false);
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