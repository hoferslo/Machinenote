package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentRegisterBinding;
import com.example.machinenote.RegistrationRequest;

import java.util.ArrayList;
import java.util.List;

public class RegisterFragment extends BaseFragment {

    public String TAG = "Register";
    private FragmentRegisterBinding binding;
    private Context context;
    private ApiManager apiManager;
    private String selectedRole = "";

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        context = getContext();

        setupSpinner();
        setupRegisterButton();

        return view;
    }

    private void setupSpinner() {
        String[] items = {"Nova rola", "Admin", "Vzdrževanje"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.roleSpinner.setAdapter(adapter);

        binding.roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                selectedRole = selectedItem;

                if (selectedItem.equals("Nova rola")) {
                    binding.addARole.setVisibility(View.VISIBLE);
                } else {
                    binding.addARole.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRole = "";
                binding.addARole.setVisibility(View.GONE);
            }
        });
    }

    private void setupRegisterButton() {
        binding.registerButton.setOnClickListener(v -> attemptRegistration());
    }

    private void attemptRegistration() {
        // Reset errors
        binding.editTextUsername.setError(null);
        binding.editTextPassword.setError(null);
        binding.addARole.setError(null);

        String username = binding.editTextUsername.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();
        String customRole = binding.addARole.getText().toString().trim();

        View focusView = null;
        boolean cancel = false;

        if (TextUtils.isEmpty(username)) {
            binding.editTextUsername.setError("Uporabniško ime je obvezno");
            focusView = binding.editTextUsername;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            binding.editTextPassword.setError("Geslo je obvezno");
            if (focusView == null) focusView = binding.editTextPassword;
            cancel = true;
        } else if (password.length() < 6) {
            binding.editTextPassword.setError("Geslo mora imeti vsaj 6 znakov");
            if (focusView == null) focusView = binding.editTextPassword;
            cancel = true;
        }

        if (selectedRole.equals("Nova rola") && TextUtils.isEmpty(customRole)) {
            binding.addARole.setError("Ime nove role je obvezno");
            if (focusView == null) focusView = binding.addARole;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) focusView.requestFocus();
            return;
        }

        String roleForRegistration = selectedRole.equals("Nova rola") ? customRole : selectedRole;
        performRegistration(username, password, roleForRegistration);
    }

    private List<String> collectPermissions() {
        List<String> permissions = new ArrayList<>();
        if (binding.checkBoxKnjizenje.isChecked()) permissions.add("Knjiženje");
        if (binding.checkBoxZastoji.isChecked()) permissions.add("Zastoji");
        if (binding.checkBoxRezervniDelo.isChecked()) permissions.add("Rezervni deli");
        if (binding.checkBoxPreventivniPregledi.isChecked()) permissions.add("Preventivni pregledi");
        if (binding.checkBoxImenik.isChecked()) permissions.add("Imenik");
        if (binding.checkBoxNaloge.isChecked()) permissions.add("Naloge");
        if (binding.checkBoxRemonti.isChecked()) permissions.add("Remonti");
        if (binding.checkBoxOrodja.isChecked()) permissions.add("Orodja");
        if (binding.checkBoxRegister.isChecked()) permissions.add("Register");
        return permissions;
    }

    private void performRegistration(String username, String password, String role) {
        binding.registerButton.setEnabled(false);
        binding.registerButton.setText("Registering...");

        List<String> permissions = collectPermissions();

        // Assuming RegistrationRequest is package-private in com.example.machinenote package
        RegistrationRequest registrationRequest = new RegistrationRequest(username, password, role, permissions);

        apiManager.registerUser(registrationRequest, new ApiManager.RegistrationCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show();
                clearForm();
                resetButton();
                // TODO: Optionally navigate to LoginFragment or DashboardFragment
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isAdded()) return;
                Toast.makeText(context, "Registration failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                resetButton();
            }
        });
    }

    private void clearForm() {
        binding.editTextUsername.setText("");
        binding.editTextPassword.setText("");
        binding.addARole.setText("");
        binding.roleSpinner.setSelection(0);
        binding.checkBoxKnjizenje.setChecked(false);
        binding.checkBoxZastoji.setChecked(false);
        binding.checkBoxRezervniDelo.setChecked(false);
        binding.checkBoxPreventivniPregledi.setChecked(false);
        binding.checkBoxImenik.setChecked(false);
        binding.checkBoxNaloge.setChecked(false);
        binding.checkBoxRemonti.setChecked(false);
        binding.checkBoxOrodja.setChecked(false);
        binding.checkBoxRegister.setChecked(false);
    }

    private void resetButton() {
        binding.registerButton.setEnabled(true);
        binding.registerButton.setText("Registriraj");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).binding.toolbarTitle.setText(TAG);
        }
    }
}
