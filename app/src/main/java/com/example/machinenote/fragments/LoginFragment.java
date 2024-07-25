package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentLoginBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends BaseFragment {

    public String TAG = "Login";
    FragmentLoginBinding binding;
    Context context;
    private ApiManager apiManager;
    private boolean isPasswordVisible = false;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance(Context context) {
        LoginFragment fragment = new LoginFragment();
        fragment.context = context;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(getLayoutInflater());

        apiManager = new ApiManager(context);

        binding.loginBtn.setOnClickListener(v -> {
            String username = binding.username.getText().toString().trim();
            String password = binding.password.getText().toString().trim();
            login(username, password);
        });

        binding.togglePasswordVisibility.setOnClickListener(v -> {
            if (isPasswordVisible) {
                binding.password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                binding.togglePasswordVisibility.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.visibility_off, 0);
            } else {
                binding.password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                binding.togglePasswordVisibility.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.visibility, 0);
            }
            isPasswordVisible = !isPasswordVisible;
            binding.password.setSelection(binding.password.length()); // Move cursor to the end
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);

        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
        String username = sharedPreferencesHelper.getString("Username", "");
        String password = sharedPreferencesHelper.getString("Password", "");
        login(username, password);
    }

    public void login(String username, String password) {
        apiManager.login(username, password, new ApiManager.LoginCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show();
                    MainActivity mainActivity = (MainActivity) requireActivity();
                    mainActivity.clearAllFragmentFromBackStack();
                    mainActivity.loadFragment(DashboardFragment.newInstance(context));
                    mainActivity.initDrawerInfo();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(context, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("error", errorMessage);
            }
        });
    }
}