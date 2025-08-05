package com.example.machinenote.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentCompletedNalogaBinding;
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

public class CompletedNalogaFragment extends BaseFragment {

    ;
    private static final String ARG_NALOGA = "naloga";

    private FragmentCompletedNalogaBinding binding;
    private Context context;
    private Naloga naloga;
    private Calendar selectedCompletionDate;
    private List<File> selectedCompletionImages;
    private ApiManager apiManager;

    // Utility classes
    private ImageCaptureHelper imageCaptureHelper;
    private CustomDateTimePicker customDateTimePicker;

    // Activity result launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public CompletedNalogaFragment() {
        // Required empty public constructor
        selectedCompletionDate = Calendar.getInstance();
        selectedCompletionImages = new ArrayList<>();
    }

    public static CompletedNalogaFragment newInstance(Context context, Naloga naloga) {
        CompletedNalogaFragment fragment = new CompletedNalogaFragment();
        fragment.context = context;
        fragment.TAG = context.getString(R.string.posodobi_nalogo);

        // Shrani nalogo v Bundle
        Bundle args = new Bundle();
        args.putSerializable(ARG_NALOGA, naloga);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            naloga = (Naloga) getArguments().getSerializable(ARG_NALOGA);
        }
        Log.d(TAG, "onCreate: " + naloga);
        apiManager = new ApiManager(context);

        // Initialize activity result launchers
        initializeActivityLaunchers();

        // Initialize utility classes
        initializeUtilities();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCompletedNalogaBinding.inflate(getLayoutInflater());

        setupClickListeners();
        initializeViews();
        populateNalogaData();

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
                // Save bitmap to file and add to selectedCompletionImages list
                File imageFile = saveBitmapToFile(bitmap);
                if (imageFile != null) {
                    selectedCompletionImages.add(imageFile);
                    updateCompletionImagePreview();
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

                        selectedCompletionDate = calendarSelected;
                        updateCompletionDateDisplay();
                    }

                    @Override
                    public void onCancel() {
                        // Handle cancel if needed
                    }
                });

        // Set to current date
        customDateTimePicker.setDate(selectedCompletionDate);
        customDateTimePicker.set24HourFormat(true);
    }

    private void initializeViews() {
        // Set current date as default for completion
        updateCompletionDateDisplay();

        // Hide completion image preview and remove button initially
        binding.slikaPoIzvedbiPreview.setVisibility(View.GONE);
        binding.odstraniSlikoPoIzvedbiBtn.setVisibility(View.GONE);

        // Set the correct tab as checked
        binding.tabNalogeBtn.setChecked(true);
    }

    private void populateNalogaData() {
        if (naloga == null) return;

        // Populate naloga data
        binding.kdoMoraTextView.setText(naloga.getVzdrzevalec());
        binding.rokNalogeTextView.setText(naloga.getRokZaIzvedbo());
        binding.imeNalogeTextView.setText(naloga.getNaloga());
        binding.podrobenOpisTextView.setText(naloga.getOpis());

        // Show original image if exists
        String[] originalImages = naloga.getSlikePredIzpolnitvijoNaloge();
        if (originalImages.length > 0 && !originalImages[0].isEmpty()) {
            binding.originalSlikaSection.setVisibility(View.VISIBLE);
            // Load the first original image
            // You might want to use an image loading library like Glide or Picasso here
            // For example: Glide.with(context).load(originalImages[0]).into(binding.originalnaSlika);
            binding.originalnaSlika.setImageURI(Uri.parse(originalImages[0]));
        } else {
            binding.originalSlikaSection.setVisibility(View.GONE);
        }
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
            // Switch to DodajNalogoFragment
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(DodajNalogoFragment.newInstance(context));
            }
        });

        // Action buttons
        binding.prekliciBtn.setOnClickListener(v -> {
            // Cancel - go back to NalogeFragment
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(NalogeFragment.newInstance(context));
            }
        });

        binding.potrdiIzvedboBtn.setOnClickListener(v -> {
            // Complete naloga
            completeNaloga();
        });

        binding.datumIzvedbeBtn.setOnClickListener(v -> {
            // Open custom date time picker
            if (customDateTimePicker != null) {
                customDateTimePicker.showDialog();
            }
        });

        binding.dodajSlikoPoIzvedbiBtn.setOnClickListener(v -> {
            // Add completion image using ImageCaptureHelper
            if (imageCaptureHelper != null) {
                imageCaptureHelper.captureImage();
            }
        });

        binding.odstraniSlikoPoIzvedbiBtn.setOnClickListener(v -> {
            // Remove completion image
            removeCompletionImage();
        });
    }

    private void completeNaloga() {
        String komentar = binding.komentarIzvedbeEditText.getText().toString().trim();

        if (komentar.isEmpty()) {
            Toast.makeText(context, "Prosimo, vnesite komentar o izvedbi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format date for SQL (YYYY-MM-DD format)
        SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String completionDateForDatabase = sqlDateFormat.format(selectedCompletionDate.getTime());

        // Update naloga object with completion data
        naloga.setIzvedeno(completionDateForDatabase);
        naloga.setKomentar(komentar);
        naloga.setIzvedenoBool(1); // Mark as completed

        // Show loading
        binding.potrdiIzvedboBtn.setEnabled(false);
        binding.potrdiIzvedboBtn.setText("Shranjujem...");

        // Update naloga with completion images
        apiManager.updateNalogaWithImages(naloga.getId(), naloga, selectedCompletionImages, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.potrdiIzvedboBtn.setEnabled(true);
                        binding.potrdiIzvedboBtn.setText("Potrdi izvedbo");

                        if (response.isSuccessful()) {
                            Toast.makeText(context, "Naloga uspešno označena kot opravljena", Toast.LENGTH_LONG).show();

                            // Clean up temporary files
                            cleanupTempFiles();

                            // Go back to NalogeFragment
                            if (getActivity() instanceof MainActivity) {
                                MainActivity mainActivity = (MainActivity) getActivity();
                                mainActivity.loadFragment(NalogeFragment.newInstance(context));
                            }
                        } else {
                            Toast.makeText(context, "Napaka pri shranjevanju izvedbe naloge", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.potrdiIzvedboBtn.setEnabled(true);
                        binding.potrdiIzvedboBtn.setText("Potrdi izvedbo");
                        Toast.makeText(context, "Napaka: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void updateCompletionDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        binding.datumIzvedbeBtn.setText(dateFormat.format(selectedCompletionDate.getTime()));
    }

    private File saveBitmapToFile(Bitmap bitmap) {
        try {
            File file = new File(context.getCacheDir(), "completion_image_" + System.currentTimeMillis() + ".jpg");
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

    private void updateCompletionImagePreview() {
        if (!selectedCompletionImages.isEmpty()) {
            binding.slikaPoIzvedbiPreview.setVisibility(View.VISIBLE);
            binding.odstraniSlikoPoIzvedbiBtn.setVisibility(View.VISIBLE);

            // Show the most recent image as preview
            File lastImage = selectedCompletionImages.get(selectedCompletionImages.size() - 1);
            binding.slikaPoIzvedbiPreview.setImageURI(Uri.fromFile(lastImage));

            // Update content description
            if (selectedCompletionImages.size() == 1) {
                binding.dodajSlikoPoIzvedbiBtn.setContentDescription("Dodaj še eno sliko (1 slika)");
            } else {
                binding.dodajSlikoPoIzvedbiBtn.setContentDescription("Dodaj še eno sliko (" + selectedCompletionImages.size() + " slik)");
            }
        }
    }

    private void removeCompletionImage() {
        selectedCompletionImages.clear();
        binding.slikaPoIzvedbiPreview.setVisibility(View.GONE);
        binding.odstraniSlikoPoIzvedbiBtn.setVisibility(View.GONE);
        binding.dodajSlikoPoIzvedbiBtn.setContentDescription("Dodaj sliko po izvedbi");
    }

    private void cleanupTempFiles() {
        // Clean up temporary files
        for (File file : selectedCompletionImages) {
            if (file.exists()) {
                file.delete();
            }
        }
        selectedCompletionImages.clear();
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

        cleanupTempFiles();

        // Clean up utility classes
        if (imageCaptureHelper != null) {
            imageCaptureHelper.deleteAllImages();
        }

        if (customDateTimePicker != null) {
            customDateTimePicker.dismissDialog();
        }
    }
}