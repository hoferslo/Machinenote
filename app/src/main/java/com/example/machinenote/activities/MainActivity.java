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
import androidx.appcompat.app.AppCompatDelegate;
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
        if (updateManager != null) {
            updateManager.onActivityResult(requestCode, resultCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (updateManager != null) {
            updateManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        scheduleUpdateCheck();

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

        // setSupportActionBar MUST come before toggle setup
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        // Back press logic
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_OVERLAY,
                    () -> {
                        if (binding.loadingLl.getVisibility() != View.VISIBLE) {
                            handleBackPress();
                        }
                    }
            );
        } else {
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (binding.loadingLl.getVisibility() != View.VISIBLE) {
                        handleBackPress();
                    }
                }
            });
        }

        binding.navView.setNavigationItemSelectedListener(item -> {
            binding.drawerLayout.closeDrawer(binding.navView);
            return true;
        });

        binding.logout.setOnClickListener(view -> {
            clearAllFragmentFromBackStack();
            boolean hasSeenGuide = sharedPreferencesHelper.getBoolean("has_seen_register_guide", false);
            sharedPreferencesHelper.clear();
            sharedPreferencesHelper.putBoolean("has_seen_register_guide", hasSeenGuide);
            disableDrawer();
            showDrawerIcon();
            loadFragment(LoginFragment.newInstance(this));
        });

        binding.settingsBtn.setOnClickListener(view -> {
            if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
                binding.drawerLayout.closeDrawer(binding.navView);
            }
            loadFragment(new SettingsFragment());
        });

        binding.noWifiBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle(R.string.no_connection);
            builder.setMessage(getString(R.string.no_connection_explanation));
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        disableDrawer();
        if (savedInstanceState == null) {
            loadFragment(LoginFragment.newInstance(this));
        }

        connectionChecker = new ConnectionChecker(this, new ConnectionChecker.ConnectionCallback() {
            @Override
            public void onSuccess() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d("ConnectionChecker", "Server is reachable: " + LocalDateTime.now());
                }
                showNoWifiBtn(false);
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
                Log.e("ConnectionChecker", "Server is not reachable: " + errorMessage);
                showNoWifiBtn(true);
                new Thread(connectionChecker).start();
                if (serverConnection) {
                    serverConnection = false;
                    triggerOnResumeOnLastFragment();
                }
            }
        });

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

    private void enableDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void initDrawerInfo() {
        binding.drawerUserNameTv.setText(sharedPreferencesHelper.getString("Username", "Ni povezava"));
        binding.drawerUserRoleTv.setText(sharedPreferencesHelper.getRole().getRole());
    }

    private void scheduleUpdateCheck() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (serverConnection) {
                updateManager.checkForUpdate();
            }
        }, 2000);
    }

    private void handleBackPress() {
        if (isKeyboardVisible()) {
            View view = getCurrentFocus();
            if (view != null) {
                view.clearFocus();
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            return;
        }

        if (GuideManager.isAnyGuideActive()) {
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

    private boolean isKeyboardVisible() {
        View rootView = binding.getRoot();
        android.graphics.Rect r = new android.graphics.Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        int screenHeight = rootView.getRootView().getHeight();
        int keypadHeight = screenHeight - r.bottom;
        return keypadHeight > screenHeight * 0.15;
    }

    public void loadFragment(Fragment fragment) {
        if (isFinishing() || isDestroyed()) {
            Log.w("MainActivity", "Activity is finishing/destroyed, cannot load fragment");
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.isStateSaved()) {
            Log.w("MainActivity", "Fragment manager state is saved, cannot load fragment");
            return;
        }

        Fragment existingFragment = fragmentManager.findFragmentByTag(fragment.getClass().getName());

        if (existingFragment == null) {
            try {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(
                        R.anim.fragment_slide_in_from_right,
                        R.anim.fragment_slide_out_to_left,
                        R.anim.fragment_slide_in_from_left,
                        R.anim.fragment_slide_out_to_right
                );

                if (!fragmentManager.getFragments().isEmpty()) {
                    Fragment currentFragment = fragmentManager.getFragments().get(fragmentManager.getFragments().size() - 1);
                    if (currentFragment != null && currentFragment.isAdded() && !currentFragment.isDetached()) {
                        currentFragment.onPause();
                    }
                }

                fragmentTransaction.replace(binding.fragmentContainer.getId(), fragment, fragment.getClass().getName());

                if (!(fragment instanceof DashboardFragment) && !(fragment instanceof LoginFragment)) {
                    fragmentTransaction.addToBackStack(null);
                }

                fragmentTransaction.commitAllowingStateLoss();




            } catch (Exception e) {
                Log.e("MainActivity", "Error loading fragment: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void showBackArrow() {
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setToolbarNavigationClickListener(v -> onBackPressed());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.mipmap.arrow_back);
        }
        android.animation.ValueAnimator anim = android.animation.ValueAnimator.ofFloat(0f, 1f);
        anim.addUpdateListener(valueAnimator ->
                toggle.onDrawerSlide(binding.drawerLayout, (Float) valueAnimator.getAnimatedValue())
        );
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator());
        anim.setDuration(300);
        anim.start();
    }

    public void showDrawerIcon() {
        enableDrawer();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        toggle.setDrawerIndicatorEnabled(true);
        toggle.setToolbarNavigationClickListener(null);
        android.animation.ValueAnimator anim = android.animation.ValueAnimator.ofFloat(1f, 0f);
        anim.addUpdateListener(valueAnimator ->
                toggle.onDrawerSlide(binding.drawerLayout, (Float) valueAnimator.getAnimatedValue())
        );
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                toggle.syncState();
            }
        });
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator());
        anim.setDuration(300);
        anim.start();
    }

    public void hideNavigationIcon() {
        disableDrawer();
        binding.toolbar.setNavigationIcon(null);
    }

    public Fragment getCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return fragmentManager.findFragmentById(binding.fragmentContainer.getId());
    }

    public void clearLastFragmentFromBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();
        updateToolbarForCurrentFragment();
        triggerOnResumeOnLastFragment();
    }

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
        if (!fragmentManager.getFragments().isEmpty()) {
            Fragment lastFragment = fragmentManager.getFragments().get(fragmentManager.getFragments().size() - 1);
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
        builder.setNegativeButton("Ne", (dialog, which) -> dialog.dismiss());
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