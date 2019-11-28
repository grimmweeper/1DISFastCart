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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase + Auth Listeners
        mAuth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
//                Log.i("console", user.getUid());
//                startActivity(new Intent(MainActivity.this, LoginActivity.class));
//                finish();
                if (user == null) {
//                    Log.i("console", user.getUid());
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
                else {
                    Log.i("console", user.getUid());
                    User userObject = User.getInstance();
                    userObject.setUserId(user.getUid());
                    startActivity(new Intent(MainActivity.this, ScannedBarcodeActivity.class));
                    finish();
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
    }
}
