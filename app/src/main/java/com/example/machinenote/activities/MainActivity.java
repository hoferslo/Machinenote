package com.example.machinenote.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.machinenote.R;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.databinding.ActivityMainBinding;
import com.example.machinenote.fragments.LoginFragment;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    public ActivityMainBinding binding;
    ActionBarDrawerToggle toggle;
    public boolean navigationForDrawerShown = true;
    private Drawable originalNavigationIcon;
    SharedPreferencesHelper sharedPreferencesHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(this);

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

        binding.logout.setOnClickListener(view -> {
            clearAllFragmentFromBackStack();
            sharedPreferencesHelper.clear();
            disableDrawer();
            loadFragment(LoginFragment.newInstance(this));
        });
        // Save a string value
        disableDrawer();
        loadFragment(LoginFragment.newInstance(this));

    }

    private boolean isUserLoggedIn() {
        return !sharedPreferencesHelper.getString(SharedPreferencesHelper.Username, null).isEmpty();
    }

    private void disableDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    // Method to enable the drawer
    private void enableDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void initDrawerInfo() {
        enableDrawer();
        binding.drawerUserNameTv.setText(sharedPreferencesHelper.getString("Username", "Ni povezave"));
        binding.drawerUserRoleTv.setText(sharedPreferencesHelper.getRole().getRole());
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existingFragment = fragmentManager.findFragmentByTag(fragment.getClass().getName());

        if (existingFragment == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(
                    R.anim.fragment_slide_in_from_right,  // Enter animation
                    R.anim.fragment_slide_out_to_left,    // Exit animation
                    R.anim.fragment_slide_in_from_left,   // Pop enter animation
                    R.anim.fragment_slide_out_to_right    // Pop exit animation
            );

            fragmentTransaction.replace(binding.fragmentContainer.getId(), fragment, fragment.getClass().getName());
            fragmentTransaction.addToBackStack(null); // Optional: Add to back stack
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.getDrawerLockMode(GravityCompat.START) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
            if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
                binding.drawerLayout.closeDrawer(binding.navView);
            } else {
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    super.onBackPressed();
                } else {
                    binding.drawerLayout.openDrawer(binding.navView);
                }
            }
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                super.onBackPressed();
            } else {
                finish();
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

    public Fragment getCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return fragmentManager.findFragmentById(binding.fragmentContainer.getId());
    }

    public void clearLastFragmentFromBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();
    }

    public void clearAllFragmentFromBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        while (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
        }
    }

}