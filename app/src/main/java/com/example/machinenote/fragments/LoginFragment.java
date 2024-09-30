package com.example.machinenote.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.ImageCaptureHelper;
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
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private boolean areCameraPermissionsGranted = false;

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

        ImageCaptureHelper imageCaptureHelper = new ImageCaptureHelper(context);
        imageCaptureHelper.deleteAllImages();

        binding.loginBtn.setOnClickListener(v -> loginUsingTextviewUsernameAndPassword());

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

    private void loginUsingTextviewUsernameAndPassword() {
        if (areCameraPermissionsGranted) {
            String username = binding.username.getText().toString().trim();
            String password = binding.password.getText().toString().trim();
            login(username, password);
        } else {
            requestCameraPermission();
        }
    }

    private void loginUsingSharedPrefsUsernameAndPassword() {
        SharedPreferencesHelper sharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
        String username = sharedPreferencesHelper.getString("Username", "");
        String password = sharedPreferencesHelper.getString("Password", "");
        binding.username.setText(username);
        binding.password.setText(password);
        if (areCameraPermissionsGranted) {
            if (!username.isEmpty() && !password.isEmpty()) {
                loginUsingTextviewUsernameAndPassword();
            }
        } else {
            requestCameraPermission();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
        if (binding.username.getText().toString().isEmpty()) {
            loginUsingSharedPrefsUsernameAndPassword();
        } else {
            loginUsingTextviewUsernameAndPassword();
        }
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

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            areCameraPermissionsGranted = true;
            loginUsingSharedPrefsUsernameAndPassword();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            // If request is cancelled, the result arrays are empty
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                areCameraPermissionsGranted = true;
                loginUsingTextviewUsernameAndPassword();
            } else {
                // Permission denied, show a message to the user explaining why the permission is necessary
                Toast.makeText(context, "Za zajem slik je potrebno dovoljenje uporabe kamere.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}