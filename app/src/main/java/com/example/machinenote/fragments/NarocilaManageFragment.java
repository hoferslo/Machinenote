package com.example.machinenote.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
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

import com.example.machinenote.BaseFragment;
import com.example.machinenote.R;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentNarocilaManageBinding;
import com.example.machinenote.models.Lokacija;
import com.example.machinenote.models.Narocila;
import com.example.machinenote.ApiManager;
import com.example.machinenote.Utility.ImageCaptureHelper;
import com.example.machinenote.Utility.CustomDatePicker;

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

public class NarocilaManageFragment extends BaseFragment {

    private FragmentNarocilaManageBinding binding;
    private Context context;
    private Calendar selectedPredvidenaDate;
    private Calendar originalDeliveryDate;
    private List<File> selectedImages;
    private ApiManager apiManager;
    private Narocila currentNarocilo;
    private List<Lokacija> lokacije;
    private List<String> locations = new ArrayList<>();
    private ArrayAdapter<String> locationAdapter;

    // Edit modes
    private boolean isEditingBasicInfo = false;
    private boolean isEditingArticle = false;
    private boolean isEditingImage = false;

    // Utility classes
    private ImageCaptureHelper imageCaptureHelper;
    private CustomDatePicker customDatePicker;

    // Activity result launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public NarocilaManageFragment() {
        // Required empty public constructor
        selectedPredvidenaDate = Calendar.getInstance();
        originalDeliveryDate = Calendar.getInstance();
        selectedImages = new ArrayList<>();
    }

    public static NarocilaManageFragment newInstance(Context context, Narocila narocilo) {
        NarocilaManageFragment fragment = new NarocilaManageFragment();
        fragment.context = context;
        fragment.currentNarocilo = narocilo;
        fragment.TAG = context.getString(R.string.tag_uredi_narocilo); // ali "Uredi naročilo"
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

        binding = FragmentNarocilaManageBinding.inflate(getLayoutInflater());
        setupSpinners();
        setupClickListeners();
        initializeViews();
        setupAmountInputValidation();

        // Najprej naložimo lokacije, potem pa naročilo podatke
        setupApiCalls();

        return binding.getRoot();
    }

    private void setupSpinners() {
        // Setup Status Spinner
        String[] statuses = {"novo", "v_obdelavi", "naroceno", "dostavljeno", "preklicano"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(context,
                R.layout.item_spinner_layout, statuses);
        statusAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown_layout);
        binding.statusSpinner.setAdapter(statusAdapter);

        // Setup Location Spinner
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

                // Update selected predvidena date
                selectedPredvidenaDate = calendarSelected;
                updatePredvidenaDateDisplay();
            }

            @Override
            public void onCancel() {
                // Handle cancel if needed
            }
        });

        // Set to current date
        customDatePicker.setDate(selectedPredvidenaDate);
    }

    private void initializeViews() {
        // Set initial edit mode states (all disabled by default)
        setEditMode(false, false, false);

        // Hide image action buttons initially
        binding.imageButtonsContainer.setVisibility(View.GONE);
        binding.odstranislikoBtn.setVisibility(View.GONE);
    }

    private void loadNarociloData() {
        if (currentNarocilo == null) return;

        try {
            // Load basic info
            setSpinnerSelection(binding.lokacijaSpinner, currentNarocilo.getLokacija());
            binding.nameOfShipper.setText(currentNarocilo.getNarocnik());

            // Load article info
            binding.articleName.setText(currentNarocilo.getNaziv());

            // Format amount with comma for display
            String kolicina = currentNarocilo.getKolicina();
            if (kolicina != null && kolicina.contains(".")) {
                kolicina = kolicina.replace(".", ",");
            }
            binding.amountOfArticle.setText(kolicina);

            binding.technicalInfo.setText(currentNarocilo.getTehnicniPodatki());

            // Set unit spinner - POPRAVLJENA VERZIJA
            String enotaValue = currentNarocilo.getEnota();
            if (enotaValue != null && !enotaValue.isEmpty()) {
                try {
                    // Če je enota shranjena kot številka (indeks)
                    int unitPosition = Integer.parseInt(enotaValue);
                    if (unitPosition >= 0 && unitPosition < binding.enotaSpinner.getAdapter().getCount()) {
                        binding.enotaSpinner.setSelection(unitPosition);
                    }
                } catch (NumberFormatException e) {
                    // Če enota ni številka, poskusimo najti po vrednosti
                    setSpinnerSelection(binding.enotaSpinner, enotaValue);
                }

                // Update input type based on loaded unit
                String selectedUnit = binding.enotaSpinner.getSelectedItem().toString();
                updateAmountInputType(selectedUnit);
            }

            // Load status
            setSpinnerSelection(binding.statusSpinner, currentNarocilo.getStatus());

            // Load dates
            loadOriginalDeliveryDate();
            loadPredvidenaDate();

            // Load timestamps
            binding.createdTimestamp.setText(formatDateForDisplay(currentNarocilo.getDatumVnosa()));
            binding.orderIdText.setText("#NAR-" + currentNarocilo.getId());

            // Load images if available
            loadImages();

        } catch (Exception e) {
            Log.e(TAG, "Error loading narocilo data", e);
            Toast.makeText(context, "Napaka pri nalaganju podatkov", Toast.LENGTH_SHORT).show();
        }
    }

    private void setSpinnerSelection(android.widget.Spinner spinner, String value) {
        if (spinner == null || value == null) return;

        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null) {
            // Poskusi najti točno ujemanje
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
                return;
            }

            // Če točno ujemanje ni najdeno, poskusi case-insensitive iskanje
            for (int i = 0; i < adapter.getCount(); i++) {
                String item = adapter.getItem(i).toString();
                if (item.equalsIgnoreCase(value)) {
                    spinner.setSelection(i);
                    return;
                }
            }

            Log.w(TAG, "Vrednost '" + value + "' ni bila najdena v spinnerju");
        }
    }

    private void loadOriginalDeliveryDate() {
        try {
            String rok = currentNarocilo.getRokZaDobavo();
            if (rok != null && !rok.isEmpty()) {
                SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                java.util.Date parsedDate = sqlFormat.parse(rok);

                // Preverimo, da rezultat parsiranja ni null
                if (parsedDate != null) {
                    originalDeliveryDate.setTime(parsedDate);
                } else {
                    originalDeliveryDate = Calendar.getInstance();
                }
            } else {
                originalDeliveryDate = Calendar.getInstance();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing original delivery date", e);
            originalDeliveryDate = Calendar.getInstance();
        }
        updateOriginalDateDisplay();
    }

    private void loadPredvidenaDate() {
        String predvidenaString = currentNarocilo.getDatumPredvideneDobave();

        // POPRAVEK: Preverimo null, prazen niz IN specifično "0000-00-00"
        if (predvidenaString != null && !predvidenaString.isEmpty() && !predvidenaString.equals("0000-00-00")) {
            try {
                SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                java.util.Date parsedDate = sqlFormat.parse(predvidenaString);

                if (parsedDate != null) {
                    selectedPredvidenaDate.setTime(parsedDate);

                    // VARNOSTNI PAS: Če je parsirano leto zaradi napak v bazi manjše od 2000, vsili današnji dan
                    if (selectedPredvidenaDate.get(Calendar.YEAR) < 2000) {
                        selectedPredvidenaDate = Calendar.getInstance();
                    }
                } else {
                    selectedPredvidenaDate = Calendar.getInstance(); // Če parsiranje vrne null, odpri današnji dan
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing predvidena date", e);
                selectedPredvidenaDate = Calendar.getInstance(); // Ob napaki skoči na današnji dan
            }
        } else {
            // Če je v bazi dejanski NULL, prazno ali "0000-00-00", postavi koledar na DANAŠNJI DAN
            selectedPredvidenaDate = Calendar.getInstance();
        }

        updatePredvidenaDateDisplay();

        if (customDatePicker != null) {
            customDatePicker.setDate(selectedPredvidenaDate);
        }
    }

    private void loadImages() {
        // Load images from currentNarocilo.getSlike() if available
        String slikeString = currentNarocilo.getSlike();
        if (slikeString != null && !slikeString.isEmpty()) {
            // Parse image URLs and load them
            // This depends on how images are stored in your system
            binding.imagePreviewContainer.setVisibility(View.VISIBLE);
            binding.noImagePlaceholder.setVisibility(View.GONE);
        } else {
            binding.imagePreviewContainer.setVisibility(View.GONE);
            binding.noImagePlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        // Edit buttons
        binding.editBasicInfoBtn.setOnClickListener(v -> {
            toggleBasicInfoEdit();
        });

        binding.editArticleBtn.setOnClickListener(v -> {
            toggleArticleEdit();
        });

        binding.editImageBtn.setOnClickListener(v -> {
            toggleImageEdit();
        });

        // Date picker button
        binding.predvidenaDostavaBtn.setOnClickListener(v -> {
            if (customDatePicker != null) {
                customDatePicker.showDialog();
            }
        });

        // Image action buttons
        binding.dodajSlikoBtn.setOnClickListener(v -> {
            if (imageCaptureHelper != null) {
                imageCaptureHelper.captureImage();
            }
        });

        binding.odstranislikoBtn.setOnClickListener(v -> {
            removeImage();
        });

        // Bottom action buttons
        binding.nazajBtn.setOnClickListener(v -> {
            // Go back to NarocilaFragment
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.loadFragment(NarocilaFragment.newInstance(context));
            }
        });

        binding.shraniSpremembeBtn.setOnClickListener(v -> {
            saveChanges();
        });
    }

    private void toggleBasicInfoEdit() {
        isEditingBasicInfo = !isEditingBasicInfo;
        binding.lokacijaSpinner.setEnabled(isEditingBasicInfo);
        binding.nameOfShipper.setEnabled(isEditingBasicInfo);
        binding.rokDobave.setEnabled(isEditingBasicInfo);

        // Update button appearance or icon if needed
        updateEditButtonState(binding.editBasicInfoBtn, isEditingBasicInfo);
    }

    private void toggleArticleEdit() {
        isEditingArticle = !isEditingArticle;
        binding.articleName.setEnabled(isEditingArticle);
        binding.amountOfArticle.setEnabled(isEditingArticle);
        binding.enotaSpinner.setEnabled(isEditingArticle);
        binding.technicalInfo.setEnabled(isEditingArticle);

        // Update button appearance or icon if needed
        updateEditButtonState(binding.editArticleBtn, isEditingArticle);
    }

    private void toggleImageEdit() {
        isEditingImage = !isEditingImage;
        binding.imageButtonsContainer.setVisibility(isEditingImage ? View.VISIBLE : View.GONE);

        // Update button appearance or icon if needed
        updateEditButtonState(binding.editImageBtn, isEditingImage);
    }

    private void updateEditButtonState(android.widget.Button button, boolean isEditing) {
        // You can change button appearance here based on edit state
        // For example, change background color or icon
        if (isEditing) {
            button.setAlpha(1.0f);
        } else {
            button.setAlpha(0.6f);
        }
    }

    private void setEditMode(boolean basicInfo, boolean article, boolean image) {
        isEditingBasicInfo = basicInfo;
        isEditingArticle = article;
        isEditingImage = image;

        // Basic info controls
        binding.lokacijaSpinner.setEnabled(basicInfo);
        binding.nameOfShipper.setEnabled(basicInfo);
        binding.rokDobave.setEnabled(basicInfo);

        // Article controls
        binding.articleName.setEnabled(article);
        binding.amountOfArticle.setEnabled(article);
        binding.enotaSpinner.setEnabled(article);
        binding.technicalInfo.setEnabled(article);

        // Image controls
        binding.imageButtonsContainer.setVisibility(image ? View.VISIBLE : View.GONE);

        // Update button states
        updateEditButtonState(binding.editBasicInfoBtn, basicInfo);
        updateEditButtonState(binding.editArticleBtn, article);
        updateEditButtonState(binding.editImageBtn, image);
    }

    private void saveChanges() {
        if (currentNarocilo == null) return;

        // Get updated values
        String lokacija = binding.lokacijaSpinner.getSelectedItem() != null ?
                binding.lokacijaSpinner.getSelectedItem().toString() : currentNarocilo.getLokacija();
        String narocnik = binding.nameOfShipper.getText().toString().trim();
        String naziv = binding.articleName.getText().toString().trim();
        String tehnicniPodatki = binding.technicalInfo.getText().toString().trim();
        String kolicinaInput = binding.amountOfArticle.getText().toString().trim();
        String status = binding.statusSpinner.getSelectedItem() != null ?
                binding.statusSpinner.getSelectedItem().toString() : currentNarocilo.getStatus();
        String adminOpombe = binding.adminOpombe.getText().toString().trim();

        String enotaStr = "";
        if (binding.enotaSpinner.getSelectedItem() != null) {
            enotaStr = binding.enotaSpinner.getSelectedItem().toString().trim();
        }

        // Validate required fields
        if (narocnik.isEmpty() || naziv.isEmpty() || kolicinaInput.isEmpty()) {
            Toast.makeText(context, "Prosimo, izpolnite vsa obvezna polja", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate that kom/kos units have integer values
        if (("kom".equals(enotaStr) || "kos".equals(enotaStr)) && kolicinaInput.contains(",")) {
            Toast.makeText(context, "Za enoto " + enotaStr + " vnesite celoštevilčno vrednost", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert comma to period for database storage
        String kolicina = kolicinaInput.replace(",", ".");

        // Get unit - shranimo indeks spinnerja kot string
        String enota = String.valueOf(binding.enotaSpinner.getSelectedItemPosition());

        // Format dates
        SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String rokForDatabase = sqlDateFormat.format(originalDeliveryDate.getTime());
        String predvidenaForDatabase = sqlDateFormat.format(selectedPredvidenaDate.getTime());

        // Update narocilo object
        currentNarocilo.setLokacija(lokacija);
        currentNarocilo.setNarocnik(narocnik);
        currentNarocilo.setNaziv(naziv);
        currentNarocilo.setTehnicniPodatki(tehnicniPodatki);
        currentNarocilo.setKolicina(kolicina);
        currentNarocilo.setEnota(enota);
        currentNarocilo.setStatus(status);
        currentNarocilo.setRokZaDobavo(rokForDatabase);
        currentNarocilo.setDatumPredvideneDobave(predvidenaForDatabase);

        // Show loading state
        binding.shraniSpremembeBtn.setEnabled(false);
        binding.shraniSpremembeBtn.setText("Shranjujem...");

        // Save changes with images
        apiManager.updateNarocilaWithImages(currentNarocilo.getId(), currentNarocilo, selectedImages,
                new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                binding.shraniSpremembeBtn.setEnabled(true);
                                binding.shraniSpremembeBtn.setText(getString(R.string.shrani_spremembe));

                                if (response.isSuccessful()) {
                                    Toast.makeText(context, "Spremembe uspešno shranjene", Toast.LENGTH_SHORT).show();
                                    // Disable all edit modes
                                    setEditMode(false, false, false);
                                    // Update timestamps
                                    updateTimestamps();
                                } else {
                                    Toast.makeText(context, "Napaka pri shranjevanju sprememb", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                binding.shraniSpremembeBtn.setEnabled(true);
                                binding.shraniSpremembeBtn.setText(getString(R.string.shrani_spremembe));
                                Toast.makeText(context, "Napaka: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
    }

    private void updateOriginalDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        binding.rokDobave.setText(dateFormat.format(originalDeliveryDate.getTime()));
    }

    private void updatePredvidenaDateDisplay() {
        // Če ima koledar nastavljeno leto 1900 ali je blizu trenutka kreiranja za stare zapise,
        // lahko na gumb izpišeš opozorilo ali pa današnji datum
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        String predvidenaString = currentNarocilo.getDatumPredvideneDobave();
        if (predvidenaString == null || predvidenaString.isEmpty() || predvidenaString.equals("0000-00-00")) {
            binding.predvidenaDostavaBtn.setText("Izberi datum"); // Namesto 30.11.0002 ali 1900 raje izpiši tekst
        } else {
            binding.predvidenaDostavaBtn.setText(dateFormat.format(selectedPredvidenaDate.getTime()));
        }
    }

    private void updateTimestamps() {
        // Update last modified timestamp to current time
        String currentTime = formatDateForDisplay(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(Calendar.getInstance().getTime()));
        binding.updatedTimestamp.setText(currentTime);
    }

    private String formatDateForDisplay(String sqlDate) {
        try {
            SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return displayFormat.format(sqlFormat.parse(sqlDate));
        } catch (Exception e) {
            return sqlDate; // Return original if parsing fails
        }
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

                        // POMEMBNO: Šele ko so lokacije naložene, naložimo podatke naročila
                        loadNarociloData();
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
                        // Tudi ob napaki naložimo podatke naročila (ostali podatki bodo še vedno prikazani)
                        loadNarociloData();
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.showBackArrow();
        if (currentNarocilo != null) {
            mainActivity.binding.toolbarTitle.setText(TAG + " #" + currentNarocilo.getId());
        } else {
            mainActivity.binding.toolbarTitle.setText(TAG);
        }
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
            imageCaptureHelper.deleteAllImages();
        }

        if (customDatePicker != null) {
            customDatePicker.dismissDialog();
        }
    }
}