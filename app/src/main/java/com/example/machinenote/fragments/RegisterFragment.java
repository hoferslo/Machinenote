package com.example.machinenote.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.AnimationHelper;
import com.example.machinenote.Utility.GuideManager;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentRegisterBinding;
import com.example.machinenote.RegistrationRequest;
import com.example.machinenote.models.Role;
import com.example.machinenote.models.User;
import com.google.android.material.chip.Chip;

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
    private List<User> users;
    private String selectedRole = "";
    private List<Role> availableRoles = new ArrayList<>();
    private Map<String, Chip> permissionChips = new HashMap<>();

    // Guide
    private GuideManager guideManager;
    private boolean shouldShowGuide = false;

    // Mode tracking
    private boolean isUserMode = true;

    public RegisterFragment() {
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

        initializePermissionChips();
        setupChipAnimations();
        setupSpinnerAnimations();
        setupModeToggleButtons();
        setupCancelButton();
        loadRolesFromApi();
        setupRegisterButton();
        loadUsersFromApi();
        setupGuide();
        setupHelpButton();

        setUserMode();
        animateHelpButtonIn();

        return view;
    }

    private void setupSpinnerAnimations() {
        binding.userSpinner.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                AnimationHelper.bounceClick(v);
            }
            return false; // Let the click event continue
        });

        binding.roleSpinner.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                AnimationHelper.bounceClick(v);
            }
            return false; // Let the click event continue
        });
    }

    private void setupGuide() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean hasSeenGuide = prefs.getBoolean("register_guide_seen", false);

        if (!hasSeenGuide) {
            shouldShowGuide = true;
        }
    }

    private void animateHelpButtonIn() {
        AnimationHelper.popInAndWiggle(binding.helpButton, 200, R.color.action_primary);
    }

    private void setupHelpButton() {
        binding.helpButton.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);

            if (isUserMode) {
                showUserModeGuide();
            } else {
                showRoleModeGuide();
            }
        });
    }

    private void setupModeToggleButtons() {
        binding.btnUserMode.setOnClickListener(v -> {setUserMode();
        AnimationHelper.bounceClick(v);
        });
        binding.btnRoleMode.setOnClickListener(v -> { setRoleMode();
                AnimationHelper.bounceClick(v);
        });
    }

    private void setupCancelButton() {
        binding.cancelBtn.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setUserMode() {
        isUserMode = true;

        binding.toggleGroup.check(binding.btnUserMode.getId());

        binding.userSection.setVisibility(View.VISIBLE);
        binding.roleSection.setVisibility(View.GONE);

        binding.registerButton.setText("Dodeli vlogo");

        restoreSelectedRolePermissions();
        disableAllChips();
        animateHelpButtonIn();

        updateToolbarTitle("Dodeli vlogo uporabniku");
    }

    private void setRoleMode() {
        isUserMode = false;

        binding.toggleGroup.check(binding.btnRoleMode.getId());

        binding.userSection.setVisibility(View.GONE);
        binding.roleSection.setVisibility(View.VISIBLE);

        binding.registerButton.setText("Ustvari novo vlogo");

        clearAllChips();
        enableAllChips();
        animateHelpButtonIn();

        updateToolbarTitle("Ustvari novo vlogo");
    }

    private void restoreSelectedRolePermissions() {
        if (!TextUtils.isEmpty(selectedRole)) {
            Role selectedRoleObj = findRoleByName(selectedRole);
            if (selectedRoleObj != null) {
                setPermissionsForRole(selectedRoleObj);
            }
        } else {
            clearAllChips();
        }
    }

    private void updateToolbarTitle(String title) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).binding.toolbarTitle.setText(title);
        }
    }

    private void initializePermissionChips() {
        permissionChips.put("Knjiženje", binding.chipKnjizenje);
        permissionChips.put("Zastoji", binding.chipZastoji);
        permissionChips.put("Rezervni deli", binding.chipRezervniDeli);
        permissionChips.put("Preventivni pregledi", binding.chipPreventivniPregledi);
        permissionChips.put("Imenik", binding.chipImenik);
        permissionChips.put("Naloge", binding.chipNaloge);
        permissionChips.put("Dodajanje nalog", binding.chipDodajanjeNalog);
        permissionChips.put("Remonti", binding.chipRemonti);
        permissionChips.put("Orodja", binding.chipOrodja);
        permissionChips.put("Register", binding.chipRegister);
        permissionChips.put("Naročila", binding.chipNarocila);
        permissionChips.put("Dodajanje Naročil", binding.chipDodajanjeNarocil);
        permissionChips.put("Upravljanje Naročil", binding.chipUpravljanjeNarocil);
        permissionChips.put("Kemikalije", binding.chipKemikalije);
    }

    private void loadRolesFromApi() {
        apiManager.getRoles(new Callback<List<Role>>() {
            @Override
            public void onResponse(Call<List<Role>> call, Response<List<Role>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    availableRoles = response.body();
                    setupRoleSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<Role>> call, Throwable t) {
                setupSpinnerWithDefaults();
                Toast.makeText(context, "Napaka pri nalaganju vlog: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadUsersFromApi() {
        apiManager.getUsers(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    users = response.body();
                    setupUserSpinner();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e("Error", "Failed to get users: " + t.getMessage());
            }
        });
    }

    private void setupUserSpinner() {
        List<String> userNames = new ArrayList<>();
        userNames.add("Izberi uporabnika...");
        for (User user : users) {
            userNames.add(user.getUsername());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_spinner_layout, userNames);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown_layout);
        binding.userSpinner.setAdapter(adapter);


        binding.userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                }

                if (view != null) {
                    AnimationHelper.bounceClick(view);
                }

                String selectedUsername = parent.getItemAtPosition(position).toString();
                loadCurrentUserRole(selectedUsername);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupRoleSpinner() {
        List<String> roleNames = new ArrayList<>();
        roleNames.add("Izberi vlogo...");

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

                if (view != null) {
                    AnimationHelper.bounceClick(view);
                }
                if (position == 0) {
                    selectedRole = "";
                    clearAllChips();
                    if (isUserMode) {
                        disableAllChips();
                    }
                    return;
                }

                String selectedItem = parent.getItemAtPosition(position).toString();
                selectedRole = selectedItem;

                Role selectedRoleObj = findRoleByName(selectedItem);

                if (selectedRoleObj != null) {
                    setPermissionsForRole(selectedRoleObj);
                } else {
                    setDefaultPermissionsForRole(selectedItem);
                }

                if (isUserMode) {
                    disableAllChips();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRole = "";
                clearAllChips();
                if (isUserMode) {
                    disableAllChips();
                }
            }
        });
    }

    private void loadCurrentUserRole(String username) {
        User selectedUser = null;
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                selectedUser = user;
                break;
            }
        }

        if (selectedUser != null && selectedUser.getRoleId() != 0) {
            for (int i = 0; i < availableRoles.size(); i++) {
                if (availableRoles.get(i).getRoleId() == selectedUser.getRoleId()) {
                    String currentRoleName = availableRoles.get(i).getRole();
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.roleSpinner.getAdapter();
                    int rolePosition = adapter.getPosition(currentRoleName);
                    if (rolePosition >= 0) {
                        binding.roleSpinner.setSelection(rolePosition);
                    }
                    break;
                }
            }
        }
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

                if (view != null) {
                    AnimationHelper.bounceClick(view);
                }
                if (position == 0) {
                    selectedRole = "";
                    clearAllChips();
                    if (isUserMode) {
                        disableAllChips();
                    }
                    return;
                }

                String selectedItem = parent.getItemAtPosition(position).toString();
                selectedRole = selectedItem;

                setDefaultPermissionsForRole(selectedItem);

                if (isUserMode) {
                    disableAllChips();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRole = "";
                clearAllChips();
                if (isUserMode) {
                    disableAllChips();
                }
            }
        });
    }

    private void setupChipAnimations() {
        for (Chip chip : permissionChips.values()) {
            chip.setOnClickListener(v -> {
                AnimationHelper.bounceClick(v);
                // The chip's checked state changes automatically
            });
        }
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
        clearAllChips();

        List<Chip> chipsToAnimate = new ArrayList<>();

        if (role.isKnjizenje()) binding.chipKnjizenje.setChecked(true);
        if (role.isZastoji()) binding.chipZastoji.setChecked(true);
        if (role.isRezervniDeli()) binding.chipRezervniDeli.setChecked(true);
        if (role.isPreventivniPregledi()) binding.chipPreventivniPregledi.setChecked(true);
        if (role.isDodajanjeNalog()) binding.chipDodajanjeNalog.setChecked(true);
        if (role.isImenik()) binding.chipImenik.setChecked(true);
        if (role.isNaloge()) binding.chipNaloge.setChecked(true);
        if (role.isRemonti()) binding.chipRemonti.setChecked(true);
        if (role.isOrodja()) binding.chipOrodja.setChecked(true);
        if (role.isRegister()) binding.chipRegister.setChecked(true);
        if (role.isNarocila()) binding.chipNarocila.setChecked(true);
        if (role.isDodajanjeNarocil()) binding.chipDodajanjeNarocil.setChecked(true);
        if (role.isUpravljanjeNarocil()) binding.chipUpravljanjeNarocil.setChecked(true);
        if (role.isKemikalije()) binding.chipKemikalije.setChecked(true);
        for (int i = 0; i < chipsToAnimate.size(); i++) {
            final Chip chip = chipsToAnimate.get(i);
            chip.postDelayed(() -> AnimationHelper.popIn(chip, 200), i * 50);
        }
    }

    private void setDefaultPermissionsForRole(String roleName) {
        clearAllChips();

        switch (roleName) {
            case "Admin":
                for (Chip chip : permissionChips.values()) {
                    chip.setChecked(true);
                }
                break;
            case "Vzdrževanje":
                binding.chipKnjizenje.setChecked(true);
                binding.chipZastoji.setChecked(true);
                binding.chipRezervniDeli.setChecked(true);
                break;
            case "Gost":
                binding.chipImenik.setChecked(true);
                break;
        }
    }

    private void enableAllChips() {
        for (Chip chip : permissionChips.values()) {
            chip.setEnabled(true);
            chip.setClickable(true);
        }
    }

    private void disableAllChips() {
        for (Chip chip : permissionChips.values()) {
            chip.setEnabled(false);
            chip.setClickable(false);
        }
    }

    private void clearAllChips() {
        for (Chip chip : permissionChips.values()) {
            chip.setChecked(false);
        }
    }

    private void setupRegisterButton() {
        binding.registerButton.setOnClickListener(v -> {
            AnimationHelper.bounceClick(v);
            if (isUserMode) {
                attemptUserUpdate();
            } else {
                attemptRoleCreation();
            }
        });
    }

    public void attemptUserUpdate() {
        if (binding.userSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(context, "Izberi uporabnika", Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.roleSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(context, "Izberi vlogo", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedUsername = binding.userSpinner.getSelectedItem().toString();
        String roleName = binding.roleSpinner.getSelectedItem().toString();

        Role selectedRole = findRoleByName(roleName);
        if (selectedRole == null) {
            Toast.makeText(context, "Napaka: vloga ni najdena", Toast.LENGTH_SHORT).show();
            return;
        }

        int roleId = selectedRole.getRoleId();

        User updatedUser = new User(selectedUsername, null, roleId, null, 3);

        apiManager.updateUser(selectedUsername, updatedUser, new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Uporabnik uspešno posodobljen", Toast.LENGTH_SHORT).show();
                    clearUserForm();
                } else {
                    Toast.makeText(context, "Napaka pri posodabljanju uporabnika", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("Error", "Failed to update user: " + t.getMessage());
                Toast.makeText(context, "Napaka pri povezavi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptRoleCreation() {
        binding.addARole.setError(null);

        String roleName = binding.addARole.getText().toString().trim();

        if (TextUtils.isEmpty(roleName)) {
            binding.addARole.setError("Ime vloge je obvezno");
            binding.addARole.requestFocus();
            return;
        }

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

    private List<String> collectPermissions() {
        List<String> permissions = new ArrayList<>();

        for (Map.Entry<String, Chip> entry : permissionChips.entrySet()) {
            if (entry.getValue().isChecked()) {
                permissions.add(entry.getKey());
            }
        }

        return permissions;
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
                loadRolesFromApi();
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

    private void clearRoleForm() {
        binding.addARole.setText("");
        clearAllChips();
    }

    private void clearUserForm() {
        binding.userSpinner.setSelection(0);
        binding.roleSpinner.setSelection(0);
        selectedRole = "";
        clearAllChips();
        if (isUserMode) {
            disableAllChips();
        }
    }

    private void resetButton() {
        binding.registerButton.setEnabled(true);
        if (isUserMode) {
            binding.registerButton.setText("Dodeli vlogo");
        } else {
            binding.registerButton.setText("Ustvari novo vlogo");
        }
    }

    // ============ GUIDE METHODS ============

    private void showUserModeGuide() {
        if (!isUserMode) setUserMode();

        binding.getRoot().postDelayed(() -> {
            guideManager = new GuideManager(requireActivity());
            guideManager
                    .addStep(binding.toggleGroup, "Izbira načina",
                            "S tema gumboma preklapljate med dvema načinoma:\n\n" +
                                    "• UPORABNIK - Za dodelitev vloge obstoječemu uporabniku\n" +
                                    "• VLOGA - Za ustvarjanje nove vloge z dovoljenji")
                    .addStep(binding.userSpinner, "Izbira uporabnika",
                            "Najprej izberite uporabnika kateremu želite spremeniti ali dodeliti vlogo.\n\n" +
                                    "Seznam prikazuje vse registrirane uporabnike v sistemu.")
                    .addStep(binding.roleSpinner, "Izbira vloge",
                            "Izberite vlogo ki jo želite dodeliti uporabniku.\n\n" +
                                    "Če uporabnik že ima vlogo, se bo ta prikazala kot trenutno izbrana.\n\n" +
                                    "Dovoljenja izbrane vloge se bodo prikazala spodaj (samo za ogled).")
                    .addStep(binding.chipGroupPermissions, "Dovoljenja vloge",
                            "Tukaj so prikazana dovoljenja izbrane vloge.\n\n" +
                                    "V načinu UPORABNIK so dovoljenja samo za pregled - ne morete jih spreminjati.\n\n" +
                                    "Vsaka vloga ima svoj nabor dovoljenj ki določajo kaj lahko uporabnik dela v aplikaciji.")
                    .addStep(binding.registerButton, "Dodeli vlogo",
                            "Ko ste izbrali uporabnika in vlogo, kliknite 'Dodeli vlogo' za shranitev.\n\n" +
                                    "Uporabnik bo takoj dobil nova dovoljenja.")

                    .start();
        }, 200);
    }

    private void showRoleModeGuide() {
        if (isUserMode) setRoleMode();

        binding.getRoot().postDelayed(() -> {
            guideManager = new GuideManager(requireActivity());
            guideManager
                    .addStep(binding.toggleGroup, "Način VLOGA",
                            "V načinu VLOGA lahko ustvarite nove vloge z različnimi dovoljenji.\n\n" +
                                    "Uporabite gumb UPORABNIK za dodelitev vlog obstoječim uporabnikom.")
                    .addStep(binding.addARole, "Ime nove vloge",
                            "Vnesite ime za novo vlogo.\n\n" +
                                    "Primeri: 'Skladiščnik', 'Vodja izmene', 'Tehnični direktor'...")
                    .addStep(binding.chipGroupPermissions, "Izbira dovoljenj",
                            "Izberite dovoljenja za novo vlogo s klikom na posamezne čipe.\n\n" +
                                    "V načinu VLOGA lahko prosto izbirate in spreminjate dovoljenja.\n\n" +
                                    "Izbrana dovoljenja (modra) bodo na voljo uporabnikom s to vlogo.")
                    .addStep(binding.chipKnjizenje, "Primeri dovoljenj",
                            "Dovoljenja kot so:\n" +
                                    "• Knjiženje - vnos delovnih ur\n" +
                                    "• Zastoji - beleženje zastojev\n" +
                                    "• Naročila - ogled naročil\n" +
                                    "• Upravljanje naročil - urejanje naročil\n\n" +
                                    "Kombinacija dovoljenj določa kaj lahko vloga dela.")
                    .addStep(binding.registerButton, "Ustvari novo vlogo",
                            "Ko ste izbrali ime in dovoljenja, kliknite 'Ustvari novo vlogo'.\n\n" +
                                    "Nova vloga bo takoj na voljo za dodelitev uporabnikom.")
                    .addStep(binding.cancelBtn, "Prekliči",
                            "Če želite zapreti ta pogled brez shranjevanja, uporabite gumb Prekliči.")

                    .start();
        }, 200);
    }

    private void startAutoGuide() {
        binding.getRoot().postDelayed(() -> {
            showUserModeGuide();
        }, 300);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isUserMode) {
            updateToolbarTitle("Dodeli vlogo uporabniku");
        } else {
            updateToolbarTitle("Ustvari novo vlogo");
        }

        if (shouldShowGuide && binding != null) {
            startAutoGuide();
            shouldShowGuide = false;

            SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("register_guide_seen", true).apply();
        }
    }
}