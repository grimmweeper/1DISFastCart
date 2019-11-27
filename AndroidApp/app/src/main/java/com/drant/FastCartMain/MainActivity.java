package com.drant.FastCartMain;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.drant.FastCartMain.ui.checkout.CheckoutFragment;
import com.drant.FastCartMain.ui.profile.ProfileFragment;
import com.drant.FastCartMain.ui.scanitem.ScanItemFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//import android.support.annotation.NonNull;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

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

    private BottomNavigationView.OnNavigationItemSelectedListener navListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fm = null;
            switch (item.getItemId()) {
                case R.id.navigation_profile:
                    fm = new ProfileFragment();
                    break;
//                    fm.beginTransaction().hide(active).show(fragment1).commit();
//                    active = fragment1;
//                    return true;

                case R.id.navigation_scanitem:
                    fm = new ScanItemFragment();
                    break;
//                    fm.beginTransaction().hide(active).show(fragment2).commit();
//                    active = fragment2;
//                    return true;

                case R.id.navigation_checkout:
                    fm = new CheckoutFragment();
                    break;
//                    fm.beginTransaction().hide(active).show(fragment3).commit();
//                    active = fragment3;
//                    return true;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fm).commit();
            return true;
        }
    };

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
