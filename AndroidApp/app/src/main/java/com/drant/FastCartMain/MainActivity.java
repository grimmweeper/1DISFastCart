package com.drant.FastCartMain;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//import android.support.annotation.NonNull;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth mAuth;
    static User userObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase + Auth Listeners
        mAuth = FirebaseAuth.getInstance();
//        final Intent authIntent;
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                Intent authIntent;
//                Log.i("console", user.getUid());
//                startActivity(new Intent(MainActivity.this, LoginActivity.class));
//                finish();
                if (user == null) {
//                    Log.i("console", user.getUid());
                    authIntent = new Intent(MainActivity.this, ScannedBarcodeActivity.class);
//                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
                else {
                    Log.i("console", user.getUid());
                    userObject = User.getInstance();
                    userObject.setUserId(user.getUid());
                    authIntent = new Intent(MainActivity.this, LoginActivity.class);
//                    startActivity(new Intent(MainActivity.this, ScannedBarcodeActivity.class));
//                    finish();
                }
                startActivity(authIntent);
                finish();
            }
        };
//        Intent authIntent;
//
//        // go straight to main if a token is stored
//        if (mAuth.getCurrentUser() != null) {
//            authIntent = new Intent(this, ScannedBarcodeActivity.class);
//        } else {
//            authIntent = new Intent(this, LoginActivity.class);
//        }
//        startActivity(authIntent);
//        finish();

    }
}
