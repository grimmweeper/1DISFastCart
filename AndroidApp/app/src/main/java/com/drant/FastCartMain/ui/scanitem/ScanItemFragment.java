package com.drant.FastCartMain.ui.scanitem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.drant.FastCartMain.R;

public class ScanItemFragment extends Fragment {

    private ScanItemViewModel dashboardViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(ScanItemViewModel.class);
        View root = inflater.inflate(R.layout.activity_scan_barcode, container, false);
        /**final TextView textView = root.findViewById(R.id.text_scanitem);
        dashboardViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        return root;
    }
}