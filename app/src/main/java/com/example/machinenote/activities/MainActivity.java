package com.example.machinenote.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.machinenote.R;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.databinding.ActivityMainBinding;
import com.example.machinenote.databinding.FragmentDashboardBinding;
import com.example.machinenote.fragments.DashboardFragment;
import com.example.machinenote.fragments.KnjizenjeFragment;
import com.example.machinenote.fragments.LoginFragment;
import com.example.machinenote.fragments.ZastojiFragment;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    public ActivityMainBinding binding;
    ActionBarDrawerToggle toggle;
    public boolean navigationForDrawerShown = true;
    private Drawable originalNavigationIcon;
    public boolean onDashboard = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        setSupportActionBar(binding.toolbar);

        originalNavigationIcon = binding.toolbar.getNavigationIcon();

        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            // Handle navigation view item clicks here.
            // You can use FragmentManager to switch between fragments
            binding.drawerLayout.closeDrawer(binding.navView);
            return true;
        });


        loadFragment(DashboardFragment.newInstance(this));
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(this);

        // Save a string value



    }


/*    public void loadFragment(Fragment fragment, boolean replace) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (replace) {
            fragmentTransaction.replace(binding.fragmentContainer.getId(), fragment);
        } else {
            fragmentTransaction.add(binding.fragmentContainer.getId(), fragment);
        }

        fragmentTransaction.addToBackStack(null); // Optional: Add to back stack
        fragmentTransaction.commit();
    }*/

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(
                R.anim.fragment_slide_in_from_right,  // Enter animation
                R.anim.fragment_slide_out_to_left,    // Exit animation
                R.anim.fragment_slide_in_from_left,   // Pop enter animation
                R.anim.fragment_slide_out_to_right    // Pop exit animation
        );
        fragmentTransaction.replace(binding.fragmentContainer.getId(), fragment);
        fragmentTransaction.addToBackStack(null); // Optional: Add to back stack
        fragmentTransaction.commit();

    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
            binding.drawerLayout.closeDrawer(binding.navView);
        } else {
            if (!onDashboard) {
                super.onBackPressed();
            } else {
                binding.drawerLayout.openDrawer(binding.navView);
            }
        }
    }



    public void showDrawerIcon() {
        binding.toolbar.setNavigationIcon(originalNavigationIcon);
        toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        binding.toolbar.setTitle("");
    }

    public void showBackArrow() {
        binding.toolbar.setNavigationIcon(R.mipmap.arrow_back);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed()); // Handle back button click
    }

}