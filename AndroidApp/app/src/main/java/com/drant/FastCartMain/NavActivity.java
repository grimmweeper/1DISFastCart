package com.drant.FastCartMain;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.drant.FastCartMain.ui.checkout.CheckoutFragment;
import com.drant.FastCartMain.ui.profile.ProfileFragment;
import com.drant.FastCartMain.ui.scanitem.ScanItemFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class NavActivity extends AppCompatActivity {

    NavigationView navigation;

    final Fragment fragment1 = new ProfileFragment();
    final Fragment fragment2 = new ScanItemFragment();
    final Fragment fragment3 = new CheckoutFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = fragment1;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fm.beginTransaction().add(R.id.main_container, fragment3, "3").hide(fragment3).commit();
        fm.beginTransaction().add(R.id.main_container, fragment2, "2").hide(fragment2).commit();
        fm.beginTransaction().add(R.id.main_container,fragment1, "1").commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_profile:
                    fm.beginTransaction().hide(active).show(fragment1).commit();
                    active = fragment1;
                    return true;

                case R.id.navigation_scanitem:
                    fm.beginTransaction().hide(active).show(fragment2).commit();
                    active = fragment2;
                    return true;

                case R.id.navigation_checkout:
                    fm.beginTransaction().hide(active).show(fragment3).commit();
                    active = fragment3;
                    return true;
            }
            return false;
        }
    };



    /**@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch(id){
                    case R.id.navigation_scanitem:
                        Intent i = new Intent(NavActivity.this, ScannedBarcodeActivity.class);
                        startActivity(i);
                        break;
                    case R.id.navigation_checkout:
                        Intent j = new Intent(NavActivity.this, CartActivity.class);
                        startActivity(j);
                        break;
//                        ActivityNavigator activityNavigator = new ActivityNavigator(NavActivity.this);
//                        activityNavigator.navigate(activityNavigator.createDestination().
//                        setIntent(new Intent(NavActivity.this, CartActivity.class)), null, null, null);

                }
                return false;
            }
        });


    }*/
    private void initInstances(){

        navigation = (NavigationView) findViewById(R.id.navigation);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch(id){
                    case R.id.navigation_scanitem:
                        Intent i = new Intent(NavActivity.this, ScannedBarcodeActivity.class);
                        startActivity(i);
                        break;
                    case R.id.navigation_checkout:
                        Intent j = new Intent(NavActivity.this, CartActivity.class);
                        startActivity(j);
                        break;
                }
                return false;
            }
        });

    }

}
