package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentRegisterBinding;
import com.example.machinenote.RegistrationRequest;
import com.example.machinenote.models.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends BaseFragment {

    public String TAG = "Upravljanje uporabnikov";
    private FragmentRegisterBinding binding;
    private Context context;
    private ApiManager apiManager;
    private String selectedRole = "";
    private List<Role> availableRoles = new ArrayList<>();
    private Map<String, CheckBox> permissionCheckBoxes = new HashMap<>();

    // Mode tracking
    private boolean isUserMode = true; // true = user registration, false = role creation

    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance(Context context) {
        RegisterFragment fragment = new RegisterFragment();
        fragment.context = context;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiManager = new ApiManager(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        context = getContext();

        initializePermissionCheckBoxes();
        setupModeToggleButtons();
        setupCancelButton();
        loadRolesFromApi();
        setupRegisterButton();

        // Set initial mode
        setUserMode();

        return view;
    }

    private void setupModeToggleButtons() {
        binding.btnUserMode.setOnClickListener(v -> setUserMode());
        binding.btnRoleMode.setOnClickListener(v -> setRoleMode());
    }

    private void setupCancelButton() {
        binding.cancelBtn.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setUserMode() {
        isUserMode = true;

        // Check the user mode button in toggle group
        binding.toggleGroup.check(binding.btnUserMode.getId());

        // Show/hide sections
        binding.userSection.setVisibility(View.VISIBLE);
        binding.roleSection.setVisibility(View.GONE);

        // Update register button text
        binding.registerButton.setText("Dodeli vlogo");

        // Restore permissions for the currently selected role
        restoreSelectedRolePermissions();
        disableAllCheckboxes();

        // Update toolbar title
        updateToolbarTitle("Dodeli vlogo uporabniku");
    }

    private void setRoleMode() {
        isUserMode = false;

        // Check the role mode button in toggle group
        binding.toggleGroup.check(binding.btnRoleMode.getId());

        // Show/hide sections
        binding.userSection.setVisibility(View.GONE);
        binding.roleSection.setVisibility(View.VISIBLE);

        // Update register button text
        binding.registerButton.setText("Ustvari novo vlogo");

        // Clear all checkboxes and enable them for role creation
        clearAllCheckboxes();
        enableAllCheckboxes();

        // Update toolbar title
        updateToolbarTitle("Ustvari novo vlogo");
    }

    private void restoreSelectedRolePermissions() {
        // Only restore if we have a selected role
        if (!TextUtils.isEmpty(selectedRole)) {
            Role selectedRoleObj = findRoleByName(selectedRole);
            if (selectedRoleObj != null) {
                setPermissionsForRole(selectedRoleObj);
            } else {
                setDefaultPermissionsForRole(selectedRole);
            }
        } else {
            // No role selected, clear all checkboxes
            clearAllCheckboxes();
        }
    }

    private void updateToolbarTitle(String title) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).binding.toolbarTitle.setText(title);
        }
    }

    private void initializePermissionCheckBoxes() {
        // Create a map for easier management of checkboxes
        permissionCheckBoxes.put("Knjiženje", binding.checkBoxKnjizenje);
        permissionCheckBoxes.put("Zastoji", binding.checkBoxZastoji);
        permissionCheckBoxes.put("Rezervni deli", binding.checkBoxRezervniDelo);
        permissionCheckBoxes.put("Preventivni pregledi", binding.checkBoxPreventivniPregledi);
        permissionCheckBoxes.put("Imenik", binding.checkBoxImenik);
        permissionCheckBoxes.put("Naloge", binding.checkBoxNaloge);
        permissionCheckBoxes.put("Dodajanje nalog", binding.checkBoxDodajanjeNalog);
        permissionCheckBoxes.put("Remonti", binding.checkBoxRemonti);
        permissionCheckBoxes.put("Orodja", binding.checkBoxOrodja);
        permissionCheckBoxes.put("Register", binding.checkBoxRegister);
        permissionCheckBoxes.put("Naročila", binding.checkBoxNarocila);
        permissionCheckBoxes.put("Dodajanje Naročil", binding.checkBoxDodajanjeNarocil);
        permissionCheckBoxes.put("Upravljanje Naročil", binding.checkBoxUpravljanjeNarocil);
    }

    private void loadRolesFromApi() {
        apiManager.getRoles(new Callback<List<Role>>() {
            @Override
            public void onResponse(Call<List<Role>> call, Response<List<Role>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    availableRoles = response.body();
                    setupSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<Role>> call, Throwable t) {
                // Fallback to default roles if API fails
                setupSpinnerWithDefaults();
                Toast.makeText(context, "Napaka pri nalaganju vlog: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinner() {
        List<String> roleNames = new ArrayList<>();
        roleNames.add("Izberi vlogo..."); // Default option

        for (Role role : availableRoles) {
            roleNames.add(role.getRole());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_spinner_layout, roleNames);

        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown_layout);
        binding.roleSpinner.setAdapter(adapter);

        binding.roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Default "Izberi vlogo..." selected
                    selectedRole = "";
                    clearAllCheckboxes();
                    if (isUserMode) {
                        disableAllCheckboxes();
                    }
                    return;
                }

                String selectedItem = parent.getItemAtPosition(position).toString();
                selectedRole = selectedItem;

                // Find the selected role and set permissions accordingly
                Role selectedRoleObj = findRoleByName(selectedItem);

                if (selectedRoleObj != null) {
                    setPermissionsForRole(selectedRoleObj);
                } else {
                    setDefaultPermissionsForRole(selectedItem);
                }

                // Only disable checkboxes in user mode
                if (isUserMode) {
                    disableAllCheckboxes();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRole = "";
                clearAllCheckboxes();
                if (isUserMode) {
                    disableAllCheckboxes();
                }
            }
        });
    }

    private void setupSpinnerWithDefaults() {
        String[] items = {"Izberi vlogo...", "Admin", "Vzdrževanje", "Gost"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.roleSpinner.setAdapter(adapter);

        binding.roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedRole = "";
                    clearAllCheckboxes();
                    if (isUserMode) {
                        disableAllCheckboxes();
                    }
                    return;
                }

                String selectedItem = parent.getItemAtPosition(position).toString();
                selectedRole = selectedItem;

                setDefaultPermissionsForRole(selectedItem);

                if (isUserMode) {
                    disableAllCheckboxes();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRole = "";
                clearAllCheckboxes();
                if (isUserMode) {
                    disableAllCheckboxes();
                }
            }
        });
    }

    private Role findRoleByName(String roleName) {
        for (Role role : availableRoles) {
            if (role.getRole().equals(roleName)) {
                return role;
            }
        }
        return null;
    }

    private void setPermissionsForRole(Role role) {
        // Clear all checkboxes first
        clearAllCheckboxes();

        // Set permissions based on role
        if (role.isKnjizenje()) binding.checkBoxKnjizenje.setChecked(true);
        if (role.isZastoji()) binding.checkBoxZastoji.setChecked(true);
        if (role.isRezervniDeli()) binding.checkBoxRezervniDelo.setChecked(true);
        if (role.isPreventivniPregledi()) binding.checkBoxPreventivniPregledi.setChecked(true);
        if (role.isDodajanjeNalog()) binding.checkBoxDodajanjeNalog.setChecked(true);
        if (role.isImenik()) binding.checkBoxImenik.setChecked(true);
        if (role.isNaloge()) binding.checkBoxNaloge.setChecked(true);
        if (role.isRemonti()) binding.checkBoxRemonti.setChecked(true);
        if (role.isOrodja()) binding.checkBoxOrodja.setChecked(true);
        if (role.isRegister()) binding.checkBoxRegister.setChecked(true);
        if (role.isNarocila()) binding.checkBoxNarocila.setChecked(true);
        if (role.isDodajanjeNarocil()) binding.checkBoxDodajanjeNarocil.setChecked(true);
        if (role.isUpravljanjeNarocil()) binding.checkBoxUpravljanjeNarocil.setChecked(true);
    }

    private void setDefaultPermissionsForRole(String roleName) {
        // Clear all checkboxes first
        clearAllCheckboxes();

        // Set default permissions for built-in roles
        switch (roleName) {
            case "Admin":
                // Admin has all permissions
                for (CheckBox checkBox : permissionCheckBoxes.values()) {
                    checkBox.setChecked(true);
                }
                break;
            case "Vzdrževanje":
                // Maintenance role has specific permissions
                binding.checkBoxZastoji.setChecked(true);
                binding.checkBoxRezervniDelo.setChecked(true);
                binding.checkBoxPreventivniPregledi.setChecked(true);
                binding.checkBoxNaloge.setChecked(true);
                binding.checkBoxRemonti.setChecked(true);
                binding.checkBoxOrodja.setChecked(true);
                break;
            case "Gost":
                // Guest has no permissions - already cleared
                break;
        }
    }

    private void enableAllCheckboxes() {
        for (CheckBox checkBox : permissionCheckBoxes.values()) {
            checkBox.setEnabled(true);
        }
    }

    private void disableAllCheckboxes() {
        for (CheckBox checkBox : permissionCheckBoxes.values()) {
            checkBox.setEnabled(false);
        }
    }

    private void clearAllCheckboxes() {
        for (CheckBox checkBox : permissionCheckBoxes.values()) {
            checkBox.setChecked(false);
        }
    }

    private void setupRegisterButton() {
        binding.registerButton.setOnClickListener(v -> {
            if (isUserMode) {
                attemptUserRegistration();
            } else {
                attemptRoleCreation();
            }
        });
    }

    private void attemptUserRegistration() {
        // Reset errors
        binding.editTextUsername.setError(null);
        binding.editTextPassword.setError(null);

        String username = binding.editTextUsername.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        View focusView = null;
        boolean cancel = false;

        if (TextUtils.isEmpty(username)) {
            binding.editTextUsername.setError("Uporabniško ime je obvezno");
            focusView = binding.editTextUsername;
            cancel = true;
        } else if (username.length() < 3) {
            binding.editTextUsername.setError("Uporabniško ime mora imeti vsaj 3 znake");
            focusView = binding.editTextUsername;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            binding.editTextPassword.setError("Geslo je obvezno");
            if (focusView == null) focusView = binding.editTextPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            binding.editTextPassword.setError("Geslo mora imeti vsaj 8 znakov, vsebovati mora vsaj eno veliko črko, eno malo črko in eno številko");
            if (focusView == null) focusView = binding.editTextPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(selectedRole)) {
            Toast.makeText(context, "Izberi vlogo za uporabnika", Toast.LENGTH_SHORT).show();
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) focusView.requestFocus();
            return;
        }

        performUserRegistration(username, password, selectedRole);
    }

    private void attemptRoleCreation() {
        // Reset errors
        binding.addARole.setError(null);

        String roleName = binding.addARole.getText().toString().trim();

        if (TextUtils.isEmpty(roleName)) {
            binding.addARole.setError("Ime vloge je obvezno");
            binding.addARole.requestFocus();
            return;
        }

        // Check if role already exists
        if (findRoleByName(roleName) != null) {
            binding.addARole.setError("Vloga s tem imenom že obstaja");
            binding.addARole.requestFocus();
            return;
        }

        List<String> permissions = collectPermissions();
        if (permissions.isEmpty()) {
            Toast.makeText(context, "Izberi vsaj eno dovoljenje za vlogo", Toast.LENGTH_SHORT).show();
            return;
        }

        performRoleCreation(roleName, permissions);
    }

    private boolean isPasswordValid(String password) {
        // Password requirements: at least 8 characters, one uppercase, one lowercase, one digit
        if (password.length() < 8) return false;

        boolean hasUpper = false, hasLower = false, hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        return hasUpper && hasLower && hasDigit;
    }

    private List<String> collectPermissions() {
        List<String> permissions = new ArrayList<>();

        for (Map.Entry<String, CheckBox> entry : permissionCheckBoxes.entrySet()) {
            if (entry.getValue().isChecked()) {
                permissions.add(entry.getKey());
            }
        }

        return permissions;
    }

    private void performUserRegistration(String username, String password, String role) {
        binding.registerButton.setEnabled(false);
        binding.registerButton.setText("Registriram...");

        // Check if the role with these permissions already exists
        Role existingRole = findRoleByName(role);
        List<String> currentPermissions = collectPermissions();

        if (existingRole != null && permissionsMatch(existingRole, currentPermissions)) {
            // Use existing role
            RegistrationRequest registrationRequest = new RegistrationRequest(username, password, role, new ArrayList<>());

            apiManager.registerUser(registrationRequest, new ApiManager.RegistrationCallback() {
                @Override
                public void onSuccess() {
                    if (!isAdded()) return;
                    Toast.makeText(context, "Uporabnik uspešno registriran z obstoječo vlogo: " + role, Toast.LENGTH_SHORT).show();
                    clearUserForm();
                    resetButton();
                }

                @Override
                public void onFailure(String errorMessage) {
                    if (!isAdded()) return;
                    String displayMessage = getDisplayErrorMessage(errorMessage);
                    Toast.makeText(context, displayMessage, Toast.LENGTH_LONG).show();
                    resetButton();
                }
            });
        } else {
            // For user registration, we don't need to pass permissions as they're determined by the role
            RegistrationRequest registrationRequest = new RegistrationRequest(username, password, role, new ArrayList<>());

            apiManager.registerUser(registrationRequest, new ApiManager.RegistrationCallback() {
                @Override
                public void onSuccess() {
                    if (!isAdded()) return;
                    Toast.makeText(context, "Uporabnik uspešno registriran", Toast.LENGTH_SHORT).show();
                    clearUserForm();
                    resetButton();
                }

                @Override
                public void onFailure(String errorMessage) {
                    if (!isAdded()) return;
                    String displayMessage = getDisplayErrorMessage(errorMessage);
                    Toast.makeText(context, displayMessage, Toast.LENGTH_LONG).show();
                    resetButton();
                }
            });
        }
    }

    private boolean permissionsMatch(Role role, List<String> permissions) {
        // Convert role permissions to list for comparison
        List<String> rolePermissions = new ArrayList<>();
        if (role.isKnjizenje()) rolePermissions.add("Knjiženje");
        if (role.isZastoji()) rolePermissions.add("Zastoji");
        if (role.isRezervniDeli()) rolePermissions.add("Rezervni deli");
        if (role.isPreventivniPregledi()) rolePermissions.add("Preventivni pregledi");
        if (role.isImenik()) rolePermissions.add("Imenik");
        if (role.isNaloge()) rolePermissions.add("Naloge");
        if (role.isDodajanjeNalog()) rolePermissions.add("Dodajanje nalog");
        if (role.isRemonti()) rolePermissions.add("Remonti");
        if (role.isOrodja()) rolePermissions.add("Orodja");
        if (role.isRegister()) rolePermissions.add("Register");
        if (role.isNarocila()) rolePermissions.add("Naročila");
        if (role.isDodajanjeNarocil()) rolePermissions.add("Dodajanje Naročil");
        if (role.isUpravljanjeNarocil()) rolePermissions.add("Upravljanje Naročil");

        return rolePermissions.containsAll(permissions) && permissions.containsAll(rolePermissions);
    }

    private void performRoleCreation(String roleName, List<String> permissions) {
        binding.registerButton.setEnabled(false);
        binding.registerButton.setText("Ustvarjam...");

        apiManager.createRole(roleName, permissions, new ApiManager.RoleCreationCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Toast.makeText(context, "Vloga '" + roleName + "' uspešno ustvarjena", Toast.LENGTH_SHORT).show();
                clearRoleForm();
                resetButton();
                loadRolesFromApi(); // Refresh the roles list
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isAdded()) return;
                String displayMessage = getDisplayErrorMessage(errorMessage);
                Toast.makeText(context, displayMessage, Toast.LENGTH_LONG).show();
                resetButton();
            }
        });
    }

    private String getDisplayErrorMessage(String errorMessage) {
        // Convert API error messages to user-friendly Slovenian messages
        if (errorMessage == null) return "Neznana napaka";

        String lowerError = errorMessage.toLowerCase();

        if (lowerError.contains("username") && lowerError.contains("exists")) {
            return "Uporabniško ime že obstaja";
        } else if (lowerError.contains("connection") || lowerError.contains("network")) {
            return "Napaka pri povezavi s strežnikom";
        } else if (lowerError.contains("role") && lowerError.contains("creation")) {
            return "Napaka pri ustvarjanju vloge";
        } else if (lowerError.contains("role") && lowerError.contains("exists")) {
            return "Vloga s tem imenom že obstaja";
        } else if (lowerError.contains("user") && lowerError.contains("creation")) {
            return "Napaka pri ustvarjanju uporabnika";
        } else if (lowerError.contains("permission")) {
            return "Napaka pri nastavitvi dovoljenj";
        } else if (lowerError.contains("validation")) {
            return "Napačni podatki";
        } else {
            return "Operacija neuspešna: " + errorMessage;
        }
    }

    private void clearUserForm() {
        binding.editTextUsername.setText("");
        binding.editTextPassword.setText("");
        binding.roleSpinner.setSelection(0); // Reset to "Izberi vlogo..."
    }

    private void clearRoleForm() {
        binding.addARole.setText("");
        clearAllCheckboxes();
    }

    private void resetButton() {
        binding.registerButton.setEnabled(true);
        if (isUserMode) {
            binding.registerButton.setText("Dodeli vlogo");
        } else {
            binding.registerButton.setText("Ustvari novo vlogo");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isUserMode) {
            updateToolbarTitle("Dodeli vlogo uporabniku");
        } else {
            updateToolbarTitle("Ustvari novo vlogo");
        }
    }
}