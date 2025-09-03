package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.Utility.UpdateManager;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentSettingsBinding;
import com.example.machinenote.models.Role;


public class SettingsFragment extends BaseFragment {

    public String TAG = "Nastavitve";
    FragmentSettingsBinding binding;
    Context context;
    private SharedPreferencesHelper prefsHelper;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(Context context) {
        SettingsFragment fragment = new SettingsFragment();
        fragment.context = context;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefsHelper = SharedPreferencesHelper.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(getLayoutInflater());

        setupViews();
        setupClickListeners();
        loadUserInfo();
        setupThemeToggle();

        return binding.getRoot();
    }

    private void setupViews() {
        // Skrijemo spremembo gesla in uporabniškega imena (kot si zahteval)
        binding.changeUsernameSection.setVisibility(View.GONE);
        binding.changePasswordSection.setVisibility(View.GONE);
        binding.saveChangesBtn.setVisibility(View.GONE);

        // Spremenimo layout za cancel gumb da zavzame celotno širino
        binding.cancelBtn.setText("Nazaj");
        binding.cancelBtn.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Display current app version
        String currentVersion = UpdateManager.getAppVersion(requireContext());
        binding.textCurrentVersion.setText("Različica: " + currentVersion);
    }

    private void setupClickListeners() {
        binding.cancelBtn.setOnClickListener(v -> {
            // Nazaj na prejšnji fragment
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        // Obvestila switch (zaenkrat samo toast)
        binding.switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Shranimo nastavitev
                prefsHelper.putBoolean("notifications_enabled", isChecked);
                Toast.makeText(getContext(),
                        isChecked ? "Obvestila vključena" : "Obvestila izključena",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Check for updates button click listener
        binding.checkUpdateBtn.setOnClickListener(v -> {
            checkForUpdates();
        });
    }

    private void checkForUpdates() {
        MainActivity mainActivity = (MainActivity) requireActivity();

        // Check if we have server connection
        if (!mainActivity.serverConnection) {
            Toast.makeText(getContext(), "Ni povezave do strežnika", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        mainActivity.showLoadingBar(true, "Preverjam posodobitve...");
        binding.checkUpdateBtn.setEnabled(false);
        binding.checkUpdateBtn.setText("Preverjam...");

        // Perform update check with callback
        mainActivity.updateManager.checkForUpdate(new UpdateManager.UpdateCallback() {
            @Override
            public void onUpdateCheckComplete() {
                // Hide loading and reset button
                mainActivity.showLoadingBar(false, "");
                binding.checkUpdateBtn.setEnabled(true);
                binding.checkUpdateBtn.setText("Preveri posodobitve");

                // Show success message (if no update was available, user will see this)
                Toast.makeText(getContext(), "Aplikacija je posodobljena", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUpdateCheckFailed(String error) {
                // Hide loading and reset button
                mainActivity.showLoadingBar(false, "");
                binding.checkUpdateBtn.setEnabled(true);
                binding.checkUpdateBtn.setText("Preveri posodobitve");

                // Show error message
                Toast.makeText(getContext(), "Napaka pri preverjanju: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadUserInfo() {
        // Prikazuj trenutno uporabniško ime
        String username = prefsHelper.getUsername();
        if (!username.isEmpty()) {
            binding.textCurrentUsername.setText(username);
        } else {
            binding.textCurrentUsername.setText("Ni nastavljeno");
        }

        // Prikazuj trenutno vlogo
        Role userRole = prefsHelper.getRole();
        if (userRole != null && userRole.getRole() != null) {
            binding.textCurrentRole.setText(userRole.getRole());
        } else {
            binding.textCurrentRole.setText("Ni nastavljeno");
        }

        // Naloži nastavitve obvestil
        boolean notificationsEnabled = prefsHelper.getBoolean("notifications_enabled", true);
        binding.switchNotifications.setChecked(notificationsEnabled);
    }

    private void setupThemeToggle() {
        // Preberi trenutno nastavitev teme iz SharedPreferences
        boolean isDarkTheme = SharedPreferencesHelper.getInstance(requireContext())
                .getInt("theme_simple", 0) == 1;

        // Nastavi switch brez triggeranja listener-ja
        binding.switchDarkTheme.setOnCheckedChangeListener(null);
        binding.switchDarkTheme.setChecked(isDarkTheme);

        // Listener za toggle teme
        binding.switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save theme
            SharedPreferencesHelper.getInstance(requireContext())
                    .putInt("theme_simple", isChecked ? 1 : 0);

            // Set a fragment argument to remember the back arrow
            Bundle args = new Bundle();
            args.putBoolean("show_back_arrow", true);
            setArguments(args);

            // Apply the theme (forces activity recreation)
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO
            );
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        Bundle args = getArguments();
        if (args != null && args.getBoolean("show_back_arrow", false)) {
            mainActivity.showBackArrow();
        }
        mainActivity.binding.toolbarTitle.setText(TAG);

        // Omogoči nazaj gumb v toolbar
        if (mainActivity.getSupportActionBar() != null) {
            mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mainActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Nastavi click listener za nazaj gumb v toolbar
        mainActivity.binding.toolbar.setNavigationOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        // Onemogoči nazaj gumb ko zapustimo fragment
        MainActivity mainActivity = (MainActivity) requireActivity();
        if (mainActivity.getSupportActionBar() != null) {
            mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mainActivity.getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
        mainActivity.binding.toolbar.setNavigationOnClickListener(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}