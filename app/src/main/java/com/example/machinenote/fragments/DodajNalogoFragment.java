package com.example.machinenote.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.DatePicker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentDodajNalogoBinding;
import com.example.machinenote.models.Naloga;
import com.example.machinenote.ApiManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DodajNalogoFragment extends BaseFragment {

    private FragmentDodajNalogoBinding binding;
    private Context context;
    private Calendar selectedDate;
    private List<File> selectedImages;
    private ApiManager apiManager;

    // Activity result launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    public DodajNalogoFragment() {
        // Required empty public constructor
        selectedDate = Calendar.getInstance();
        selectedImages = new ArrayList<>();
    }

    public static DodajNalogoFragment newInstance(Context context) {
        DodajNalogoFragment fragment = new DodajNalogoFragment();
        fragment.context = context;
        fragment.TAG = context.getString(R.string.dodaj_nalogo); // ali "Dodaj nalogo"
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiManager = new ApiManager(context);

        // Initialize activity result launchers
        initializeActivityLaunchers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDodajNalogoBinding.inflate(getLayoutInflater());

        setupClickListeners();
        initializeViews();

        return binding.getRoot();
    }

    private void initializeActivityLaunchers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            File imageFile = saveBitmapToFile(imageBitmap);
                            if (imageFile != null) {
                                selectedImages.add(imageFile);
                                updateImagePreview();
                            }
                        }
                    }
                }
        );

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            File imageFile = createFileFromUri(selectedImageUri);
                            if (imageFile != null) {
                                selectedImages.add(imageFile);
                                updateImagePreview();
                            }
                        }
                    }
                }
        );

        // Permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        showImageSourceDialog();
                    } else {
                        Toast.makeText(context, "Dovoljenje za dostop do kamere je potrebno", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void initializeViews() {
        // Set current date as default
        updateDateDisplay();

        // Hide image preview and remove button initially
        binding.imagePreview.setVisibility(View.GONE);
        binding.odstranislikoBtn.setVisibility(View.GONE);

        // Set the correct tab as checked
        binding.tabVnosNalogeBtn.setChecked(true);
    }

    private void setupClickListeners() {
        // Tab navigation
        binding.tabNalogeBtn.setOnClickListener(v -> {
            // Switch back to NalogeFragment
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(NalogeFragment.newInstance(context));
            }
        });

        binding.tabVnosNalogeBtn.setOnClickListener(v -> {
            // Already on this fragment, do nothing or refresh
        });

        // Action buttons
        binding.prekliciBtn.setOnClickListener(v -> {
            // Cancel - go back to NalogeFragment
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(NalogeFragment.newInstance(context));
            }
        });

        binding.shraniBtn.setOnClickListener(v -> {
            // Save naloga
            saveNaloga();
        });

        binding.rokEditText.setOnClickListener(v -> {
            // Open date picker
            showDatePicker();
        });

        binding.dodajSlikoBtn.setOnClickListener(v -> {
            // Add image functionality
            addImage();
        });

        binding.odstranislikoBtn.setOnClickListener(v -> {
            // Remove image
            removeImage();
        });
    }

    private void saveNaloga() {
        String vzdrzevalec = binding.vzdrzevalecEditText.getText().toString().trim();
        String naslov = binding.naslovEditText.getText().toString().trim();
        String rok = binding.rokEditText.getText().toString().trim();
        String opis = binding.opisEditText.getText().toString().trim();

        if (vzdrzevalec.isEmpty() || naslov.isEmpty() || rok.isEmpty()) {
            Toast.makeText(context, "Prosimo, izpolnite vsa obvezna polja", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Naloga object using the full constructor
        Naloga naloga = new Naloga(
                0, // id (will be set by database)
                vzdrzevalec,
                naslov,
                opis,
                rok,
                "", // izvedeno - empty initially
                "", // komentar - empty initially
                0, // izvedenoBool - 0 means false/not completed
                "", // slike - empty initially (images after completion)
                "" // slikePredIzpolnitvijoNaloge - will be set by server
        );

        // Show loading
        binding.shraniBtn.setEnabled(false);
        binding.shraniBtn.setText("Shranjujem...");

        // Save naloga with images
        apiManager.sendNalogaWithImages(naloga, selectedImages, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.shraniBtn.setEnabled(true);
                        binding.shraniBtn.setText("Shrani");

                        if (response.isSuccessful()) {
                            Toast.makeText(context, "Naloga uspešno shranjena", Toast.LENGTH_SHORT).show();

                            // Go back to NalogeFragment
                            if (getActivity() instanceof MainActivity) {
                                MainActivity mainActivity = (MainActivity) getActivity();
                                mainActivity.loadFragment(NalogeFragment.newInstance(context));
                            }
                        } else {
                            Toast.makeText(context, "Napaka pri shranjevanju naloge", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.shraniBtn.setEnabled(true);
                        binding.shraniBtn.setText("Shrani");
                        Toast.makeText(context, "Napaka: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        binding.rokEditText.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void addImage() {
        // Check camera permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            showImageSourceDialog();
        }
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Izberi vir slike");

        String[] options = {"Kamera", "Galerija"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Camera
                    openCamera();
                    break;
                case 1: // Gallery
                    openGallery();
                    break;
            }
        });

        builder.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(context.getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            Toast.makeText(context, "Kamera ni na voljo", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private File saveBitmapToFile(Bitmap bitmap) {
        try {
            File file = new File(context.getCacheDir(), "image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Napaka pri shranjevanju slike", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private File createFileFromUri(Uri uri) {
        try {
            File file = new File(context.getCacheDir(), "selected_image_" + System.currentTimeMillis() + ".jpg");

            // Copy content from URI to file
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 FileOutputStream fileOutputStream = new FileOutputStream(file)) {

                if (inputStream != null) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                }
            }

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Napaka pri obdelavi slike", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void updateImagePreview() {
        if (!selectedImages.isEmpty()) {
            binding.imagePreview.setVisibility(View.VISIBLE);
            binding.odstranislikoBtn.setVisibility(View.VISIBLE);

            // Update button text to show count
            if (selectedImages.size() == 1) {
                binding.dodajSlikoBtn.setContentDescription("Dodaj še eno sliko (1 slika)");
            } else {
                binding.dodajSlikoBtn.setContentDescription("Dodaj še eno sliko (" + selectedImages.size() + " slik)");
            }

            // Show the most recent image as preview
            File lastImage = selectedImages.get(selectedImages.size() - 1);
            binding.imagePreview.setImageURI(Uri.fromFile(lastImage));
        }
    }

    private void removeImage() {
        selectedImages.clear();
        binding.imagePreview.setVisibility(View.GONE);
        binding.odstranislikoBtn.setVisibility(View.GONE);
        binding.dodajSlikoBtn.setVisibility(View.VISIBLE);
        binding.dodajSlikoBtn.setContentDescription("Dodaj sliko");
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up temporary files
        for (File file : selectedImages) {
            if (file.exists()) {
                file.delete();
            }
        }
    }
}