package com.drant.FastCartMain;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        Intent authIntent;

        // go straight to main if a token is stored
        if (mAuth.getCurrentUser() != null) {
            authIntent = new Intent(this, ScannedBarcodeActivity.class);
        } else {
            authIntent = new Intent(this, LoginActivity.class);
        }
        startActivity(authIntent);
        finish();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAuth.signOut();
    }
}
