package com.example.machinenote.fragments;

import static java.lang.Integer.parseInt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.machinenote.ApiManager;
import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
// Animation was here AnimationHelper.bounceClick(view);
import com.example.machinenote.Utility.ImageCaptureHelper;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.Utility.ConfettiHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentLoginBinding;
import com.example.machinenote.models.Lokacija;

import java.lang.reflect.Field;
import java.util.List;

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

        binding.loginBtn.setOnClickListener(v -> {
                loginUsingTextviewUsernameAndPassword();
                // Animation was here AnimationHelper.bounceClick(v);
         });

        binding.togglePasswordVisibility.setOnClickListener(v -> {
            // Animation was here AnimationHelper.bounceClick(v);
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
            // Animation was here AnimationHelper.bounceClick(v);
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(PublicRegisterFragment.newInstance(context));
            }
        });

        binding.forgotPassword.setOnClickListener(
                v -> { displayUserInput();
                // Animation was here AnimationHelper.bounceClick(v);
        });

        return binding.getRoot();
    }

    private static void setCursorDrawable(EditText editText, int drawableRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ use the official method
            editText.setTextCursorDrawable(drawableRes);
        } else {
            // API 28 and below use reflection
            try {
                Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
                field.setAccessible(true);
                field.set(editText, drawableRes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void displayUserInput() {
        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_password_reset, null);

        // Get references to views
        EditText editTextUsername = dialogView.findViewById(R.id.editTextUsername);
        EditText editTextPassword = dialogView.findViewById(R.id.editTextPassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        // Set cursor drawable
        setCursorDrawable(editTextUsername, 0);
        setCursorDrawable(editTextPassword, 0);

        // Create dialog without default buttons
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Make dialog background transparent so custom layout background shows
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Handle cancel button
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            // Animation was here AnimationHelper.bounceClick(v);
        });

        // Handle submit button
        btnSubmit.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            // Animation was here AnimationHelper.bounceClick(v);

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

        // Show dialog
        dialog.show();

        // Request focus and show keyboard for username field
        editTextUsername.requestFocus();
        editTextUsername.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editTextUsername, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);
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

                    apiManager.fetchLokacije(new ApiManager.LokacijeCallback() {
                        @Override
                        public void onSuccess(List<Lokacija> response) {
                            List<Lokacija> lokacije = response;

                            // Get the stored location ID value from SharedPreferences
                            String locationIdString = SharedPreferencesHelper.getInstance(context)
                                    .getString(SharedPreferencesHelper.LocationID, "");
                            Log.d("LokacijaLogin", "Stored location ID string: " + locationIdString);

                            // Check if it's not empty before parsing
                            if (!locationIdString.isEmpty()) {
                                int storedLocationId = parseInt(locationIdString);
                            Log.d("LokacijaLogin", "Stored location ID: " + storedLocationId);

                                for (Lokacija l : lokacije) {
                                    if (l.getId() == storedLocationId) {
                                        SharedPreferencesHelper.getInstance(context)
                                                .putString(SharedPreferencesHelper.Location, l.getNaziv());
                                        Log.d("LokacijaLogin", "Stored location Naziv: " + l.getNaziv());
                                        break; // Exit loop once found
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            // Handle error
                            Log.e("Lokacije", "Failed to fetch locations: " + errorMessage);
                        }
                    });


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