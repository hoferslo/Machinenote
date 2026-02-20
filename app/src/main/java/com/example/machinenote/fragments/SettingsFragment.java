package com.example.machinenote.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.Utility.UpdateManager;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentSettingsBinding;
import com.example.machinenote.models.Lokacija;
import com.example.machinenote.models.Role;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SettingsFragment extends BaseFragment {

    public String TAG = "Nastavitve";
    FragmentSettingsBinding binding;
    Context context;
    private SharedPreferencesHelper prefsHelper;
    private ApiManager apiManager;
    private List<Lokacija> lokacije;
    private List<String> locations = new ArrayList<>();
    private ArrayAdapter<String> locationAdapter;

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
        apiManager = new ApiManager(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(getLayoutInflater());
        setupApiCalls();
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

        binding.changeLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLocation = locations.get(position);
                prefsHelper.putString(SharedPreferencesHelper.Location, selectedLocation);
                updateUserLocation(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        locationAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, locations);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.changeLocationSpinner.setAdapter(locationAdapter);

    }

    private void updateUserLocation(int position) {
        String username = prefsHelper.getUsername();
        Lokacija selectedLokacija = lokacije.get(position);
        int lokacijaId = selectedLokacija.getId();

        apiManager.updateUserLocation(lokacijaId, username,
                new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.d("updateUserLocation", "Response: " + response.code());
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("updateUserLocation", "Error: " + t.getMessage());
                    }
                });
    }

    private void setupApiCalls(){
        apiManager.fetchLokacije(new ApiManager.LokacijeCallback() {
            @Override
            public void onSuccess(List<Lokacija> response) {
                lokacije = response;
                locations.clear();

                for (Lokacija l : lokacije) {
                    locations.add(l.getNaziv());
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        locationAdapter.notifyDataSetChanged();

                        // Get the saved location name from SharedPreferences
                        String savedLocationName = SharedPreferencesHelper.getInstance(context).getLokacija();

                        // Find the position of this location in the list
                        if (!savedLocationName.isEmpty() && !locations.isEmpty()) {
                            int position = locations.indexOf(savedLocationName);
                            if (position >= 0) {
                                binding.changeLocationSpinner.setSelection(position);
                            } else {
                                // If saved location not found, select first item
                                binding.changeLocationSpinner.setSelection(0);
                            }
                        } else if (!locations.isEmpty()) {
                            // No saved location, select first item
                            binding.changeLocationSpinner.setSelection(0);
                        }
                    });
                }

                Log.e("lokacije", "lokacije: " + lokacije.toString());
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("lokacije", "Error: " + errorMessage);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(context, "Napaka pri nalaganju lokacij: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            }
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
            // Save theme preference
            SharedPreferencesHelper.getInstance(requireContext())
                    .putInt("theme_simple", isChecked ? 1 : 0);

            // Restart the app
            MainActivity mainActivity = (MainActivity) requireActivity();
            Intent intent = mainActivity.getIntent();
            mainActivity.finish();
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.showBackArrow();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}