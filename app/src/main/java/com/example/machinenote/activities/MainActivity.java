package com.example.machinenote.activities;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.machinenote.R;
import com.example.machinenote.Utility.ConnectionChecker;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.databinding.ActivityMainBinding;
import com.example.machinenote.fragments.LoginFragment;
import com.example.machinenote.fragments.QRCodeScannerFragment;

import java.time.LocalDateTime;


public class MainActivity extends AppCompatActivity implements QRCodeScannerFragment.QRCodeScannerListener {

    private AppBarConfiguration appBarConfiguration;
    public ActivityMainBinding binding;
    ActionBarDrawerToggle toggle;
    public boolean navigationForDrawerShown = true;
    private Drawable originalNavigationIcon;
    SharedPreferencesHelper sharedPreferencesHelper;
    private ConnectionChecker connectionChecker;
    public boolean serverConnection = true;


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

        //backwards press logic
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 33+
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    () -> {
                        if (binding.loadingLl.getVisibility() != View.VISIBLE) {
                            if (binding.drawerLayout.getDrawerLockMode(GravityCompat.START) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
                                if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
                                    binding.drawerLayout.closeDrawer(binding.navView);
                                } else {
                                    if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                                        clearLastFragmentFromBackStack();
                                    } else {
                                        binding.drawerLayout.openDrawer(binding.navView);
                                    }
                                }
                            } else {
                                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                                    clearLastFragmentFromBackStack();
                                } else {
                                    finish();
                                }
                            }
                        }
                    }
            );
        } else {
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (binding.loadingLl.getVisibility() != View.VISIBLE) {
                        if (binding.drawerLayout.getDrawerLockMode(GravityCompat.START) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
                            if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
                                binding.drawerLayout.closeDrawer(binding.navView);
                            } else {
                                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                                    clearLastFragmentFromBackStack();
                                } else {
                                    binding.drawerLayout.openDrawer(binding.navView);
                                }
                            }
                        } else {
                            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                                clearLastFragmentFromBackStack();
                            } else {
                                finish();
                            }
                        }
                    }
                }
            });
        }


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

        binding.noWifiBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle(R.string.no_connection);
            builder.setMessage(getString(R.string.no_connection_explanation));

            // Optionally add an OK button to dismiss the dialog
            builder.setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
            });

            // Create and show the dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });
        // Save a string value
        disableDrawer();
        loadFragment(LoginFragment.newInstance(this));

        connectionChecker = new ConnectionChecker(this, new ConnectionChecker.ConnectionCallback() {
            @Override
            public void onSuccess() {
                // Handle successful connection
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d("ConnectionChecker", "Server is reachable: " + LocalDateTime.now());
                }
                //showLoadingBar(false, "");
                showNoWifiBtn(false);
                // Create a Handler to post the delayed task
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> new Thread(connectionChecker).start(), 1500);
                if (!serverConnection) {
                    serverConnection = true;
                    triggerOnResumeOnLastFragment();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle connection failure
                Log.e("ConnectionChecker", "Server is not reachable: " + errorMessage);
                //showLoadingBar(true, "Ni povezave do strežnika, preveri wifi");
                showNoWifiBtn(true);
                new Thread(connectionChecker).start();
                if (serverConnection) {
                    serverConnection = false;
                    triggerOnResumeOnLastFragment();
                }
            }
        });

        // Start checking the connection
        new Thread(connectionChecker).start();

    }

    public void showLoadingBar(boolean b, String text) {
        binding.loadingLl.setVisibility(b ? View.VISIBLE : View.GONE);

    }

    public void showNoWifiBtn(boolean b) {
        binding.noWifiBtn.setVisibility(b ? View.VISIBLE : View.GONE);
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
            if (!fragmentManager.getFragments().isEmpty()) {
                fragmentManager.getFragments().get(fragmentManager.getFragments().size() - 1).onPause();
            }
            fragmentTransaction.add(binding.fragmentContainer.getId(), fragment, fragment.getClass().getName());
            fragmentTransaction.addToBackStack(null); // Optional: Add to back stack
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop checking when the activity is destroyed
        //connectionChecker.stopChecking();
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
        triggerOnResumeOnLastFragment();
    }

    public void triggerOnResumeOnLastFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (!fragmentManager.getFragments().isEmpty()) {
            fragmentManager.getFragments().get(fragmentManager.getFragments().size() - 1).onResume();
        }
    }

    public void clearAllFragmentFromBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        while (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
        }
    }

    @Override
    public void onQRCodeScanned(String result) {

    }

    @Override
    public void onScanCancelled() {

    }
}