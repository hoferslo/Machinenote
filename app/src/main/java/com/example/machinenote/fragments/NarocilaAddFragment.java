package com.example.machinenote.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.Utility.SharedPreferencesHelper;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentNarocilaAddBinding;
import com.example.machinenote.models.Lokacija;
import com.example.machinenote.models.Narocila;
import com.example.machinenote.ApiManager;
import com.example.machinenote.Utility.ImageCaptureHelper;
import com.example.machinenote.Utility.CustomDatePicker;
import com.example.machinenote.models.SklopLinije;

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

public class NarocilaAddFragment extends BaseFragment {

    private FragmentNarocilaAddBinding binding;
    private Context context;
    private Calendar selectedDate;
    private List<File> selectedImages;
    private ApiManager apiManager;
    private List<Lokacija> lokacije;
    private List<String> locations = new ArrayList<>();
    private ArrayAdapter<String> locationAdapter;
    private SharedPreferencesHelper sharedPreferencesHelper;

    // Utility classes
    private ImageCaptureHelper imageCaptureHelper;
    private CustomDatePicker customDatePicker;

    // Activity result launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public NarocilaAddFragment() {
        // Required empty public constructor
        selectedDate = Calendar.getInstance();
        selectedImages = new ArrayList<>();
    }

    public static NarocilaAddFragment newInstance(Context context) {
        NarocilaAddFragment fragment = new NarocilaAddFragment();
        fragment.context = context;
        fragment.TAG = context.getString(R.string.tag_dodaj_narocilo); // ali "Dodaj naročilo"
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

        binding = FragmentNarocilaAddBinding.inflate(getLayoutInflater());
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(requireContext());
        // Najprej pokličemo API klic, da dobimo lokacije
        setupApiCalls();
        // Potem nastavimo spinner-je (brez lokacij)
        setupSpinners();
        setupClickListeners();
        initializeViews();
        setupAmountInputValidation();

        return binding.getRoot();
    }

    private void setupSpinners() {
        // Setup Location Spinner - inicializiramo prazen adapter
        locationAdapter = new ArrayAdapter<>(context,
                R.layout.item_spinner_layout, locations);
        locationAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_layout);
        binding.lokacijaSpinner.setAdapter(locationAdapter);

        // Setup Unit Spinner
        String[] units = {"kg", "kom", "kos", "m", "m²", "m³", "l"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(context,
                R.layout.item_spinner_layout, units);
        unitAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_layout);
        binding.enotaSpinner.setAdapter(unitAdapter);

        // Add listener to unit spinner to change input type based on selection
        binding.enotaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedUnit = units[position];
                updateAmountInputType(selectedUnit);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupAmountInputValidation() {
        // Add TextWatcher to handle comma decimal separator
        binding.amountOfArticle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();

                // Replace period with comma for decimal separator
                if (text.contains(".")) {
                    int cursorPosition = binding.amountOfArticle.getSelectionStart();
                    String newText = text.replace(".", ",");
                    binding.amountOfArticle.removeTextChangedListener(this);
                    binding.amountOfArticle.setText(newText);
                    binding.amountOfArticle.setSelection(Math.min(cursorPosition, newText.length()));
                    binding.amountOfArticle.addTextChangedListener(this);
                }
            }
        });
    }

    private void updateAmountInputType(String unit) {
        if ("kom".equals(unit) || "kos".equals(unit)) {
            // For kom and kos, allow only integers
            binding.amountOfArticle.setInputType(InputType.TYPE_CLASS_NUMBER);

            // Clear any decimal values if switching to integer-only unit
            String currentText = binding.amountOfArticle.getText().toString();
            if (currentText.contains(",")) {
                String integerPart = currentText.split(",")[0];
                binding.amountOfArticle.setText(integerPart);
            }
        } else {
            // For other units, allow decimals with comma separator
            binding.amountOfArticle.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
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

        // Initialize CustomDatePicker (instead of CustomDateTimePicker)
        customDatePicker = new CustomDatePicker(getContext(), new CustomDatePicker.ICustomDateListener() {
            @Override
            public void onSet(android.app.Dialog dialog, Calendar calendarSelected,
                              java.util.Date dateSelected, int year, String monthFullName,
                              String monthShortName, int monthNumber, int day,
                              String weekDayFullName, String weekDayShortName) {

                // Update selected date
                selectedDate = calendarSelected;
                updateDateDisplay();
            }

            @Override
            public void onCancel() {
                // Handle cancel if needed
            }
        });

        // Set to current date
        customDatePicker.setDate(selectedDate);
    }

    private void initializeViews() {
        // Set current date as default
        updateDateDisplay();

        // Hide image preview container and remove button initially
        binding.imagePreviewContainer.setVisibility(View.GONE);
        binding.odstranislikoBtn.setVisibility(View.GONE);

        String maintainerName = sharedPreferencesHelper.getUsername();
        if (maintainerName != null && !maintainerName.isEmpty()) {
            binding.nameOfShipper.setText(maintainerName);
        }

    }

    private void setupClickListeners() {
        // Tab navigation
        binding.tabNarocilaBtn.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
            fragmentManager.popBackStack();
            Fragment NarocilaFragment = com.example.machinenote.fragments.NarocilaFragment.newInstance(mainActivity);
            mainActivity.loadFragment(NarocilaFragment);
        });

        // Date picker button
        binding.rokDobave.setOnClickListener(v -> {
            // Open custom date picker
            if (customDatePicker != null) {
                customDatePicker.showDialog();
            }
        });

        // Image action buttons
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

        binding.shraniBtn.setOnClickListener(v -> {
            saveNarocilo();
        });
    }

    private void saveNarocilo() {
        // Get values from spinners and EditTexts with better validation
        String lokacija = "";
        if (binding.lokacijaSpinner.getSelectedItem() != null) {
            lokacija = binding.lokacijaSpinner.getSelectedItem().toString().trim();
        }

        // Debug log to check what's selected
        Log.d("NarocilaAddFragment", "Selected lokacija: '" + lokacija + "'");
        Log.d("NarocilaAddFragment", "Spinner position: " + binding.lokacijaSpinner.getSelectedItemPosition());

        String narocnik = binding.nameOfShipper.getText().toString().trim();
        String naziv = binding.articleName.getText().toString().trim();
        String tehnicniPodatki = binding.technicalInfo.getText().toString().trim();
        String kolicinaInput = binding.amountOfArticle.getText().toString().trim();

        String enotaStr = "";
        if (binding.enotaSpinner.getSelectedItem() != null) {
            enotaStr = binding.enotaSpinner.getSelectedItem().toString().trim();
        }

        // Improved validation
        if (lokacija.isEmpty()) {
            Toast.makeText(context, "Prosimo, izberite lokacijo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (narocnik.isEmpty()) {
            Toast.makeText(context, "Prosimo, vnesite naročnika", Toast.LENGTH_SHORT).show();
            return;
        }

        if (naziv.isEmpty()) {
            Toast.makeText(context, "Prosimo, vnesite naziv artikla", Toast.LENGTH_SHORT).show();
            return;
        }

        if (kolicinaInput.isEmpty()) {
            Toast.makeText(context, "Prosimo, vnesite količino", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate that kom/kos units have integer values
        if (("kom".equals(enotaStr) || "kos".equals(enotaStr)) && kolicinaInput.contains(",")) {
            Toast.makeText(context, "Za enoto " + enotaStr + " vnesite celoštevilčno vrednost", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate == null) {
            Toast.makeText(context, "Prosimo, izberite rok dobave", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert comma to period for database storage (if needed)
        String kolicina = kolicinaInput.replace(",", ".");

        String enota = String.valueOf(binding.enotaSpinner.getSelectedItem());

        // Format date for SQL (YYYY-MM-DD format)
        SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String rokForDatabase = sqlDateFormat.format(selectedDate.getTime());
        String datumVnosa = sqlDateFormat.format(Calendar.getInstance().getTime());

        // Create Narocila object using the SQL-formatted date
        Narocila narocilo = new Narocila(
                0, // id (will be set by database)
                lokacija,
                narocnik,
                naziv,
                tehnicniPodatki,
                kolicina,
                enota,
                "", // slike - will be set by server
                datumVnosa, // datum_vnosa - current date
                rokForDatabase, // rok_za_dobavo - selected date
                "",
                "",
                "", // datum_potrjene_dobave - empty initially
                "novo"// status - default to "novo"
        );

        // Debug log the narocilo object
        Log.d("NarocilaAddFragment", "Narocilo object - Lokacija: '" + narocilo.getLokacija() + "'");

        // Save narocilo with images
        apiManager.sendNarocilaWithImages(narocilo, selectedImages, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            resetForm();
                            Toast.makeText(context, "Naročilo uspešno shranjeno", Toast.LENGTH_SHORT).show();
                            if (getActivity() instanceof MainActivity) {
                                MainActivity mainActivity = (MainActivity) getActivity();
                                mainActivity.loadFragment(NarocilaFragment.newInstance(context));
                            }
                        } else {
                            Toast.makeText(context, "Napaka pri shranjevanju naročila", Toast.LENGTH_SHORT).show();
                            Log.e("NarocilaAddFragment", "Server response error: " + response.code());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(context, "Napaka: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("NarocilaAddFragment", "Network error", t);
                    });
                }
            }
        });
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        binding.rokDobave.setText(dateFormat.format(selectedDate.getTime()));
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
        // Reset spinners to first item
        binding.lokacijaSpinner.setSelection(0);
        binding.enotaSpinner.setSelection(0);

        // Clear all text fields
        binding.nameOfShipper.setText("");
        binding.articleName.setText("");
        binding.technicalInfo.setText("");
        binding.amountOfArticle.setText("");

        // Reset date to current date
        selectedDate = Calendar.getInstance();
        updateDateDisplay();
        if (customDatePicker != null) {
            customDatePicker.setDate(selectedDate);
        }

        // Clear images
        selectedImages.clear();
        binding.imagePreviewContainer.setVisibility(View.GONE);
        binding.odstranislikoBtn.setVisibility(View.GONE);
        binding.noImagePlaceholder.setVisibility(View.VISIBLE);
    }

    private void updateImagePreview() {
        if (!selectedImages.isEmpty()) {
            binding.imagePreviewContainer.setVisibility(View.VISIBLE);
            binding.noImagePlaceholder.setVisibility(View.GONE);
            binding.odstranislikoBtn.setVisibility(View.VISIBLE);

            // Show the most recent image as preview
            File lastImage = selectedImages.get(selectedImages.size() - 1);
            binding.imagePreview.setImageURI(Uri.fromFile(lastImage));

            // Update button text
            if (selectedImages.size() == 1) {
                binding.dodajSlikoBtn.setText("Dodaj še eno sliko");
            } else {
                binding.dodajSlikoBtn.setText("Dodaj sliko (" + selectedImages.size() + ")");
            }
        }
    }

    private void removeImage() {
        selectedImages.clear();
        binding.imagePreviewContainer.setVisibility(View.GONE);
        binding.noImagePlaceholder.setVisibility(View.VISIBLE);
        binding.odstranislikoBtn.setVisibility(View.GONE);
        binding.dodajSlikoBtn.setText(getString(R.string.dodaj_sliko));
    }

    private void setupApiCalls(){
        apiManager.fetchLokacije(new ApiManager.LokacijeCallback() {
            @Override
            public void onSuccess(List<Lokacija> response) {
                lokacije = response;
                locations.clear(); // Počistimo seznam

                for (Lokacija l : lokacije) {
                    locations.add(l.getNaziv());
                }

                // Posodobimo adapter POTEM ko imamo podatke
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        locationAdapter.notifyDataSetChanged();

                        // Dobimo shranjeno lokacijo uporabnika
                        String savedLocationName = sharedPreferencesHelper.getLokacija();

                        // Poiščemo pozicijo te lokacije v seznamu
                        if (!savedLocationName.isEmpty() && !locations.isEmpty()) {
                            int position = locations.indexOf(savedLocationName);
                            if (position >= 0) {
                                // Nastavimo uporabnikovo lokacijo
                                binding.lokacijaSpinner.setSelection(position);
                            } else {
                                // Če shranjene lokacije ni v seznamu, izberemo prvo
                                binding.lokacijaSpinner.setSelection(0);
                            }
                        } else if (!locations.isEmpty()) {
                            // Če ni shranjene lokacije, izberemo prvo
                            binding.lokacijaSpinner.setSelection(0);
                        }
                    });
                }

                Log.e("lokacije", "lokacije: " + lokacije.toString());
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("lokacije", "Error: " + errorMessage);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(context, "Napaka pri nalaganju lokacij: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
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

        if (customDatePicker != null) {
            customDatePicker.dismissDialog(); // Dismiss any open dialogs
        }
    }
}