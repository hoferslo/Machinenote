package com.example.machinenote.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentDodajNalogoBinding;
import com.example.machinenote.models.Naloga;
import com.example.machinenote.ApiManager;
import com.example.machinenote.Utility.ImageCaptureHelper;
import com.example.machinenote.Utility.CustomDateTimePicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    // Utility classes
    private ImageCaptureHelper imageCaptureHelper;
    private CustomDateTimePicker customDateTimePicker;

    // Activity result launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

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

        // Initialize utility classes
        initializeUtilities();
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
                    if (imageCaptureHelper != null) {
                        imageCaptureHelper.handleActivityResult(result.getResultCode(), result.getData());
                    }
                }
        );

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (imageCaptureHelper != null) {
                        imageCaptureHelper.handleActivityResult(result.getResultCode(), result.getData());
                    }
                }
        );
    }

    private void initializeUtilities() {
        // Initialize ImageCaptureHelper
        imageCaptureHelper = new ImageCaptureHelper(context, cameraLauncher, galleryLauncher);
        imageCaptureHelper.setImageCaptureCallback(new ImageCaptureHelper.ImageCaptureCallback() {
            @Override
            public void onImageCaptured(Bitmap bitmap) {
                // Save bitmap to file and add to selectedImages list
                File imageFile = saveBitmapToFile(bitmap);
                if (imageFile != null) {
                    selectedImages.add(imageFile);
                    updateImagePreview();
                    Toast.makeText(context, "Slika uspešno dodana", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(context, "Napaka: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize CustomDateTimePicker
        customDateTimePicker = new CustomDateTimePicker(context,
                new CustomDateTimePicker.ICustomDateTimeListener() {
                    @Override
                    public void onSet(android.app.Dialog dialog, Calendar calendarSelected,
                                      java.util.Date dateSelected, int year, String monthFullName,
                                      String monthShortName, int monthNumber, int day,
                                      String weekDayFullName, String weekDayShortName,
                                      int hour24, int hour12, int min, int sec, String AM_PM) {

                        selectedDate = calendarSelected;
                        updateDateDisplay();
                    }

                    @Override
                    public void onCancel() {
                        // Handle cancel if needed
                    }
                });

        // Set to current date
        customDateTimePicker.setDate(selectedDate);
        customDateTimePicker.set24HourFormat(true); // Use 24-hour format for consistency
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
            // Open custom date time picker
            if (customDateTimePicker != null) {
                customDateTimePicker.showDialog();
            }
        });

        binding.dodajSlikoBtn.setOnClickListener(v -> {
            // Add image using ImageCaptureHelper
            if (imageCaptureHelper != null) {
                imageCaptureHelper.captureImage();
            }
        });

        binding.odstranislikoBtn.setOnClickListener(v -> {
            // Remove image
            removeImage();
        });
    }
    private void saveNaloga() {
        String vzdrzevalec = binding.vzdrzevalecEditText.getText().toString().trim();
        String naslov = binding.naslovEditText.getText().toString().trim();
        String opis = binding.opisEditText.getText().toString().trim();

        if (vzdrzevalec.isEmpty() || naslov.isEmpty() || selectedDate == null) {
            Toast.makeText(context, "Prosimo, izpolnite vsa obvezna polja", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format date for SQL (YYYY-MM-DD format)
        SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String rokForDatabase = sqlDateFormat.format(selectedDate.getTime());

        // Create Naloga object using the SQL-formatted date
        Naloga naloga = new Naloga(
                0, // id (will be set by database)
                vzdrzevalec,
                naslov,
                opis,
                rokForDatabase, // Use SQL format instead of display format
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
                            resetForm();
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

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        binding.rokEditText.setText(dateFormat.format(selectedDate.getTime()));
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

    private void resetForm() {
        // Clear all text fields
        binding.vzdrzevalecEditText.setText("");
        binding.naslovEditText.setText("");
        binding.opisEditText.setText("");

        // Reset date to current date
        selectedDate = Calendar.getInstance();
        updateDateDisplay();
        if (customDateTimePicker != null) {
            customDateTimePicker.setDate(selectedDate);
        }

        // Clear images
        selectedImages.clear();
        binding.imagePreview.setVisibility(View.GONE);
        binding.odstranislikoBtn.setVisibility(View.GONE);
        binding.dodajSlikoBtn.setContentDescription("Dodaj sliko");

        // Show success message
        Toast.makeText(context, "Naloga uspešno shranjena.", Toast.LENGTH_LONG).show();
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

        // Clean up utility classes
        if (imageCaptureHelper != null) {
            imageCaptureHelper.deleteAllImages(); // Clean up any remaining temp files
        }

        if (customDateTimePicker != null) {
            customDateTimePicker.dismissDialog(); // Dismiss any open dialogs
        }
    }
}