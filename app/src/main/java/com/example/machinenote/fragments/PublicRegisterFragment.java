package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.machinenote.Utility.GuideManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.RegistrationRequest;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentPublicRegisterBinding;

import java.util.ArrayList;

/**
 * Fragment za javno registracijo uporabnikov
 * Uporabniki se registrirajo z osnovnimi podatki in dobijo "Gost" vlogo
 */
public class PublicRegisterFragment extends BaseFragment {

    public String TAG = "Registracija";
    private FragmentPublicRegisterBinding binding;
    private Context context;
    private ApiManager apiManager;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    public PublicRegisterFragment() {
        // Required empty public constructor
    }

    public static PublicRegisterFragment newInstance(Context context) {
        PublicRegisterFragment fragment = new PublicRegisterFragment();
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
        binding = FragmentPublicRegisterBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        context = getContext();

        setupPasswordVisibilityToggles();
        setupButtons();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(mainActivity);
            boolean hasSeenGuide = helper.getBoolean("has_seen_register_guide", false);

            if (!hasSeenGuide) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isAdded() && getActivity() != null) {
                        showRegistrationGuide();
                    }
                }, 600);
            }
        }
    }

    private void setupPasswordVisibilityToggles() {
        // Password visibility toggle
        binding.togglePasswordVisibility.setOnClickListener(v -> {
            if (isPasswordVisible) {
                binding.editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                binding.togglePasswordVisibility.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.visibility_off, 0);
            } else {
                binding.editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                binding.togglePasswordVisibility.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.visibility, 0);
            }
            isPasswordVisible = !isPasswordVisible;
            binding.editTextPassword.setSelection(binding.editTextPassword.length());
        });

        // Confirm password visibility toggle
        binding.toggleConfirmPasswordVisibility.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                binding.editTextConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                binding.toggleConfirmPasswordVisibility.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.visibility_off, 0);
            } else {
                binding.editTextConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                binding.toggleConfirmPasswordVisibility.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.visibility, 0);
            }
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            binding.editTextConfirmPassword.setSelection(binding.editTextConfirmPassword.length());
        });
    }

    private void setupButtons() {
        binding.registerButton.setOnClickListener(v -> attemptRegistration());

        binding.backToLoginButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.getSupportFragmentManager().popBackStack();
            }
        });

        binding.helpButton.setOnClickListener(v -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded() && getActivity() != null) {
                    showRegistrationGuide();
                }
            }, 600);
        });
    }

    private void attemptRegistration() {
        // Reset errors
        binding.editTextUsername.setError(null);
        binding.editTextPassword.setError(null);
        binding.editTextConfirmPassword.setError(null);

        String username = binding.editTextUsername.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();
        String confirmPassword = binding.editTextConfirmPassword.getText().toString().trim();

        View focusView = null;
        boolean cancel = false;

        // Validate username
        if (TextUtils.isEmpty(username)) {
            binding.editTextUsername.setError("Uporabniško ime je obvezno");
            focusView = binding.editTextUsername;
            cancel = true;
        } else if (username.length() < 3) {
            binding.editTextUsername.setError("Uporabniško ime mora imeti vsaj 3 znake");
            focusView = binding.editTextUsername;
            cancel = true;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            binding.editTextPassword.setError("Geslo je obvezno");
            if (focusView == null) focusView = binding.editTextPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            binding.editTextPassword.setError("Geslo mora imeti vsaj 8 znakov, vsebovati mora vsaj eno veliko črko, eno malo črko in eno številko");
            if (focusView == null) focusView = binding.editTextPassword;
            cancel = true;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.editTextConfirmPassword.setError("Potrditev gesla je obvezna");
            if (focusView == null) focusView = binding.editTextConfirmPassword;
            cancel = true;
        } else if (!password.equals(confirmPassword)) {
            binding.editTextConfirmPassword.setError("Gesli se ne ujemata");
            if (focusView == null) focusView = binding.editTextConfirmPassword;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) focusView.requestFocus();
            return;
        }

        performRegistration(username, password);
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

    private void performRegistration(String username, String password) {
        binding.registerButton.setEnabled(false);
        binding.registerButton.setText("Registriram...");

        // Create registration request with "Gost" role (guest role with no permissions)
        RegistrationRequest registrationRequest = new RegistrationRequest(username, password, "Gost", new ArrayList<>());

        apiManager.createUser(registrationRequest, new ApiManager.RegistrationCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                Toast.makeText(context, "Registracija uspešna! Zdaj se lahko prijavite.", Toast.LENGTH_LONG).show();

                // Return to login screen
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.getSupportFragmentManager().popBackStack();
                }
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
        } else if (lowerError.contains("registration") || lowerError.contains("user")) {
            return "Napaka pri registraciji uporabnika";
        } else if (lowerError.contains("validation")) {
            return "Napačni podatki";
        } else {
            return "Registracija neuspešna: " + errorMessage;
        }
    }

    private void resetButton() {
        binding.registerButton.setEnabled(true);
        binding.registerButton.setText("Registriraj se");
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.binding.toolbarTitle.setText(TAG);

            // Hide navigation icon for registration screen
            mainActivity.binding.toolbar.setNavigationIcon(null);
        }
    }

    private void showRegistrationGuide() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;

        GuideManager guideManager = new GuideManager(mainActivity);

        guideManager
                // Step 1: Username field
                .addStep(
                        binding.editTextUsername,
                        "Uporabniško ime",
                        "Izberite uporabniško ime (vsaj 3 znake). To ime boste uporabljali za prijavo."
                )

                // Step 2: Password field
                .addStep(
                        binding.editTextPassword,
                        "Geslo",
                        "Ustvarite varno geslo: vsaj 8 znakov, ena velika črka, ena mala črka in ena številka."
                )

                // Step 3: Password visibility toggle
                .addStep(
                        binding.togglePasswordVisibility,
                        "Prikaz gesla",
                        "Kliknite ikono očesa za prikaz ali skritje gesla."
                )

                // Step 4: Confirm password
                .addStep(
                        binding.editTextConfirmPassword,
                        "Potrditev gesla",
                        "Ponovno vnesite geslo za potrditev. Gesli se morata ujemati."
                )

                // Step 5: Register button
                .addStep(
                        binding.registerButton,
                        "Registracija",
                        "Ko ste izpolnili vse podatke, kliknite tukaj za registracijo. Po uspešni registraciji se lahko prijavite."
                )

                // Step 6: Back to login
                .addStep(
                        binding.backToLoginButton,
                        "Nazaj na prijavo",
                        "Če že imate račun, se vrnite na prijavno stran."
                )

                .setOnCompleteListener(() -> {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(activity);
                        helper.putBoolean("has_seen_register_guide", true);
                        Toast.makeText(context, "Dobrodošli! Zdaj lahko izpolnite obrazec.", Toast.LENGTH_SHORT).show();
                    }
                })
                .start();
    }
}