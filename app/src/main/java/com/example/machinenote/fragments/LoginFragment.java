package com.example.machinenote.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.ImageCaptureHelper;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.Utility.ConfettiHelper;
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
            binding.password.setSelection(binding.password.length());
        });

        // Add register button click listener
        binding.registerButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(PublicRegisterFragment.newInstance(context));
            }
        });

        binding.forgotPassword.setOnClickListener(
                v -> displayUserInput()
        );

        return binding.getRoot();
    }

    private void displayUserInput(){
        // Create a LinearLayout to hold both EditTexts
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Create EditText for username
        final EditText editTextUsername = new EditText(context);
        editTextUsername.setHint("Uporabniško ime");
        editTextUsername.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        editTextUsername.setTextCursorDrawable(null);

        // Create EditText for password - VISIBLE TEXT (not hidden)
        final EditText editTextPassword = new EditText(context);
        editTextPassword.setHint("Novo geslo");
        editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        editTextPassword.setTextCursorDrawable(null);

        // Add both EditTexts to the layout
        layout.addView(editTextUsername);
        layout.addView(editTextPassword);

        // Create and show the AlertDialog
        AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Pozabljeno geslo")
                .setMessage("Vnesite svoje podatke za ponastavitev gesla:")
                .setView(layout)
                .setPositiveButton("Pošlji", null)
                .setNegativeButton("Prekliči", null)
                .show();

        // Request focus and show keyboard for username field
        editTextUsername.requestFocus();
        editTextUsername.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editTextUsername, InputMethodManager.SHOW_IMPLICIT);
        }, 200);

        // Override the positive button to prevent dialog from closing on validation error
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Clear previous errors
            editTextUsername.setError(null);
            editTextPassword.setError(null);

            // Validate username
            if (username.isEmpty()) {
                editTextUsername.setError("Prosim vnesite uporabniško ime");
                editTextUsername.requestFocus();
                return;
            }

            // Validate password
            if (!isValidPassword(password)) {
                editTextPassword.setError("Geslo mora imeti vsaj 8 znakov, vsebovati mora vsaj eno veliko črko, eno malo črko in eno številko");
                editTextPassword.requestFocus();
                return;
            }

            // If both are valid, proceed and close dialog
            sendPasswordResetEmail(username, password);
            dialog.dismiss();
        });
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        return hasUpperCase && hasLowerCase && hasDigit;
    }

    private void sendPasswordResetEmail(String username, String password) {
        String recipient = "matej.kandare@unichem.si";
        String subject = "Pozabljeno geslo";
        String encodedUsername = Uri.encode(username);
        String encodedPassword = Uri.encode(password);

        String message = "Pozdravljeni!\n\n" +
                "Prosim za ponastavitev gesla\n\n" +
                "Uporabnik: " + username + "\n" +
                "Geslo: " + password + "\n" +
                "Povezava za resetiranje: http://192.168.12.192/apiv2/resetPassword.php?username=" +
                encodedUsername + "&password=" + encodedPassword + "\n\n" +
                "Lep pozdrav";
        // Use ACTION_SENDTO with mailto: URI to open email apps directly
        android.content.Intent emailIntent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
        emailIntent.setData(android.net.Uri.parse("mailto:")); // Only email apps handle this
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{recipient});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);

        try {
            startActivity(emailIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "Ni nameščenega e-poštnega odjemalca", Toast.LENGTH_SHORT).show();
        }
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
        // CRITICAL FIX: Check if fragment is properly attached before calling requireActivity()
        if (!isAdded() || isDetached() || getActivity() == null) {
            Log.w(TAG, "Fragment not attached to activity, skipping onResume");
            return;
        }

        super.onResume();

        // Use getActivity() instead of requireActivity() for additional safety
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.binding.toolbarTitle.setText(TAG);

            // Skrij drawer ikono ali back arrow za login
            mainActivity.binding.toolbar.setNavigationIcon(null);

            if (binding.username.getText().toString().isEmpty()) {
                loginUsingSharedPrefsUsernameAndPassword();
            } else {
                loginUsingTextviewUsernameAndPassword();
            }
        }
    }

    public void login(String username, String password) {
        apiManager.login(username, password, new ApiManager.LoginCallback() {
            @Override
            public void onSuccess() {
                // CRITICAL FIX: Check if fragment is still attached before UI operations
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show();

                    // Check for easter egg AFTER successful login
                    if ("UNICHEM".equals(username) && "UNICHEM".equals(password)) {
                        Log.d(TAG, "Easter egg activated after successful login!");
                        Activity activity = getActivity();
                        if (activity != null) {
                            ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
                            ConfettiHelper.showConfetti(context, rootView);

                            // Add a slight delay to ensure the confetti shows
                            Toast.makeText(context, "🎉 UNICHEM! 🎉", Toast.LENGTH_LONG).show();
                        }
                    }

                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.clearAllFragmentFromBackStack();
                    mainActivity.loadFragment(DashboardFragment.newInstance(context));
                    mainActivity.initDrawerInfo();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                // CRITICAL FIX: Check if fragment is still attached before showing toast
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(context, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
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
                if (isAdded()) {
                    Toast.makeText(context, "Za zajem slik je potrebno dovoljenje uporabe kamere.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}