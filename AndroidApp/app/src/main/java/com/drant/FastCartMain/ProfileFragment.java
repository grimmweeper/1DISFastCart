package com.drant.FastCartMain;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.drant.FastCartMain.ui.purchasehistory.HistorySessionActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ProfileFragment extends Fragment implements IllopCallback {
    Button logout;
    Button purchasehistory;
    private FirebaseAuth firebaseAuth;

    AlertDialog alertIllop;
    private View profileView;

    public ProfileFragment() {
    }

    //private ProfileViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_profile, container, false);
        TextView usernameTest = (TextView) view.findViewById(R.id.user_name);
        usernameTest.setText(User.getInstance().getUserName());
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);



        firebaseAuth = FirebaseAuth.getInstance();
        logout = (Button) view.findViewById(R.id.logout_button);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.getInstance().createNewUser();
                firebaseAuth.signOut();
                getActivity().finish();
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        purchasehistory = (Button) view.findViewById(R.id.purchasehistory);
        purchasehistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HistorySessionActivity.class);
                startActivity(intent);
            }
        });

        alertIllop = new AlertDialog.Builder(getContext()).create();

    }

    @Override
    public void onStart() {
        super.onStart();



        try {
            DatabaseHandler.getInstance().listenForIllop(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        DatabaseHandler.getInstance().detachListener("illop");
    }

    @Override
    public void checkIllopCallback(Boolean illopStatus) {
        if(illopStatus) {
            alertIllop.setIcon(R.drawable.warning);
            alertIllop.setTitle("Warning");
            alertIllop.setMessage("Please return to the cart's previous state");
            alertIllop.show();
            alertIllop.setCancelable(false);
            alertIllop.setCanceledOnTouchOutside(false);

        } else if (alertIllop.isShowing()) {
            alertIllop.dismiss();
        }
    }
}