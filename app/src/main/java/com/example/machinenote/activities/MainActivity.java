package com.example.machinenote.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseActivity;
import com.example.machinenote.R;
import com.example.machinenote.Utility.ConnectionChecker;
import com.example.machinenote.Utility.GuideManager;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.Utility.UpdateManager;
import com.example.machinenote.databinding.ActivityMainBinding;
import com.example.machinenote.fragments.DashboardFragment;
import com.example.machinenote.fragments.LoginFragment;
import com.example.machinenote.fragments.QRCodeScannerFragment;
import com.example.machinenote.fragments.SettingsFragment;

import java.time.LocalDateTime;


public class MainActivity extends BaseActivity implements QRCodeScannerFragment.QRCodeScannerListener {

    private AppBarConfiguration appBarConfiguration;
    public ActivityMainBinding binding;
    ActionBarDrawerToggle toggle;
    public boolean navigationForDrawerShown = true;
    private Drawable originalNavigationIcon;
    SharedPreferencesHelper sharedPreferencesHelper;
    private ConnectionChecker connectionChecker;
    public boolean serverConnection = true;
    private boolean doubleBackToExitPressedOnce = false;
    private Handler exitHandler = new Handler(Looper.getMainLooper());
    public UpdateManager updateManager;
    private ApiManager apiManager;

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (binding.loadingLl.getVisibility() != View.VISIBLE) {
            handleBackPress();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("toolbar_title", binding.toolbarTitle.getText().toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Forward to UpdateManager
        if (updateManager != null) {
            updateManager.onActivityResult(requestCode, resultCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward to UpdateManager
        if (updateManager != null) {
            updateManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        scheduleUpdateCheck();
        // ENOSTAVEN PRISTOP     za temo
        SharedPreferencesHelper prefsHelper = SharedPreferencesHelper.getInstance(this);
        boolean isDarkTheme = prefsHelper.getInt("theme_simple", 0) == 1;

        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            binding.toolbarTitle.setText(savedInstanceState.getString("toolbar_title"));
        }

        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(this);

        toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        setSupportActionBar(binding.toolbar);

        //backwards press logic
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_OVERLAY, // <-- Spremenjena prioriteta!
                    () -> {
                        if (binding.loadingLl.getVisibility() != View.VISIBLE) {
                            handleBackPress();
                        }
                    }
            );
        } else {
            // Za API 33 in nižje moraš uporabiti drugačen pristop
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (binding.loadingLl.getVisibility() != View.VISIBLE) {
                        handleBackPress();
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
            boolean hasSeenGuide = sharedPreferencesHelper.getBoolean(
                    "has_seen_register_guide", false
            );

            sharedPreferencesHelper.clear();

            sharedPreferencesHelper.putBoolean("has_seen_register_guide", hasSeenGuide);
            disableDrawer();
            showDrawerIcon();
            loadFragment(LoginFragment.newInstance(this));
        });


        binding.settingsBtn.setOnClickListener(view -> {
            // Close drawer if it's open
            if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
                binding.drawerLayout.closeDrawer(binding.navView);
            }

            // Load SettingsFragment
            loadFragment(new SettingsFragment());
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
        if (savedInstanceState == null) {
            loadFragment(LoginFragment.newInstance(this));
        }

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
                    checkForAppUpdate();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle connection failure
                Log.e("ConnectionChecker", "Server is not reachable: " + errorMessage);
                //showLoadingBar(true, "Ni povezava do strežnika, preveri wifi");
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
        apiManager = new ApiManager(this);
        updateManager = new UpdateManager(this, apiManager);
    }



    private void checkForAppUpdate() {
        if (serverConnection) {
            updateManager.checkForUpdate();
        }
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
        binding.drawerUserNameTv.setText(sharedPreferencesHelper.getString("Username", "Ni povezava"));
        binding.drawerUserRoleTv.setText(sharedPreferencesHelper.getRole().getRole());
    }

    private void scheduleUpdateCheck() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (serverConnection) {
                updateManager.checkForUpdate();
            }
        }, 2000); // Check after 2 seconds
    }

    private void handleBackPress() {
        // First check if keyboard is open
        if (isKeyboardVisible()) {
            // Let the system handle closing the keyboard
            View view = getCurrentFocus();
            if (view != null) {
                view.clearFocus();
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            return; // Stop here, don't process fragment navigation
        }

        if(GuideManager.isAnyGuideActive()){
            GuideManager.closeActive();
            Log.d("MainActivityGuide", "Guide closed");
        } else if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
            binding.drawerLayout.closeDrawer(binding.navView);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            clearLastFragmentFromBackStack();
        } else {
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment instanceof DashboardFragment) {
                showExitConfirmation();
            } else {
                finish();
            }
        }
    }

    // Add this helper method to check if keyboard is visible
    private boolean isKeyboardVisible() {
        View rootView = binding.getRoot();
        android.graphics.Rect r = new android.graphics.Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        int screenHeight = rootView.getRootView().getHeight();
        int keypadHeight = screenHeight - r.bottom;

        // If keyboard takes up more than 15% of the screen, it's visible
        return keypadHeight > screenHeight * 0.15;
    }

    // Replace your existing loadFragment method with this safer version:

    public void loadFragment(Fragment fragment) {
        // Check if activity is still valid
        if (isFinishing() || isDestroyed()) {
            Log.w("MainActivity", "Activity is finishing/destroyed, cannot load fragment");
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        // Check if fragment manager is available
        if (fragmentManager.isStateSaved()) {
            Log.w("MainActivity", "Fragment manager state is saved, cannot load fragment");
            return;
        }

        Fragment existingFragment = fragmentManager.findFragmentByTag(fragment.getClass().getName());

        if (existingFragment == null) {
            try {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(
                        R.anim.fragment_slide_in_from_right,  // Enter animation
                        R.anim.fragment_slide_out_to_left,    // Exit animation
                        R.anim.fragment_slide_in_from_left,   // Pop enter animation
                        R.anim.fragment_slide_out_to_right    // Pop exit animation
                );

                // Safely pause the current fragment
                if (!fragmentManager.getFragments().isEmpty()) {
                    Fragment currentFragment = fragmentManager.getFragments().get(fragmentManager.getFragments().size() - 1);
                    if (currentFragment != null && currentFragment.isAdded() && !currentFragment.isDetached()) {
                        currentFragment.onPause();
                    }
                }

                fragmentTransaction.replace(binding.fragmentContainer.getId(), fragment, fragment.getClass().getName());

                // Ne dodaj DashboardFragment in LoginFragment v backstack
                if (!(fragment instanceof DashboardFragment) && !(fragment instanceof LoginFragment)) {
                    fragmentTransaction.addToBackStack(null);
                }

                fragmentTransaction.commitAllowingStateLoss(); // Use commitAllowingStateLoss for better safety

                // Automatically show back arrow or drawer icon based on fragment type
                if (fragment instanceof DashboardFragment || fragment instanceof LoginFragment) {
                    showDrawerIcon();
                } else {
                    showBackArrow();
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error loading fragment: " + e.getMessage());
            }
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
        // After popping, update the toolbar icon for the fragment that is now visible
        updateToolbarForCurrentFragment();
        triggerOnResumeOnLastFragment();
    }

    /**
     * After popping a fragment off the back stack, checks what fragment is now
     * on top and updates the toolbar icon accordingly (back arrow vs drawer icon).
     */
    private void updateToolbarForCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (!fragmentManager.getFragments().isEmpty()) {
            Fragment topFragment = fragmentManager.getFragments().get(fragmentManager.getFragments().size() - 1);
            if (topFragment instanceof DashboardFragment || topFragment instanceof LoginFragment) {
                showDrawerIcon();
            } else {
                showBackArrow();
            }
        }
    }

    public void triggerOnResumeOnLastFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Check if there are any fragments currently loaded
        if (!fragmentManager.getFragments().isEmpty()) {
            // Get the last fragment (current visible fragment)
            Fragment lastFragment = fragmentManager.getFragments().get(fragmentManager.getFragments().size() - 1);

            // CRITICAL: Check if fragment is properly attached before calling onResume()
            if (lastFragment != null && lastFragment.isAdded() && !lastFragment.isDetached() && lastFragment.getActivity() != null) {
                try {
                    lastFragment.onResume();
                } catch (IllegalStateException e) {
                    Log.e("MainActivity", "Fragment not attached when calling onResume: " + e.getMessage());
                }
            } else {
                Log.w("MainActivity", "Cannot trigger onResume - fragment not properly attached");
            }
        } else {
            Log.w("MainActivity", "No fragments available to trigger onResume");
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

    private void showExitConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Zapri aplikacijo");
        builder.setMessage("Ali res želite zapreti aplikacijo?");

        builder.setPositiveButton("Da", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });

        builder.setNegativeButton("Ne", (dialog, which) -> {
            dialog.dismiss();
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void handleDoubleBackPress() {
        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Pritisnite znova za zapiranje aplikacije", Toast.LENGTH_SHORT).show();

        exitHandler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}