package com.example.machinenote.customFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.machinenote.R;
import com.example.machinenote.databinding.FragmentLinijeBottomSheetBinding;
import com.example.machinenote.models.Linija;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LinijeBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = "LinijeBottomSheet";
    private static final String ARG_LINIJA = "arg_linija";

    private FragmentLinijeBottomSheetBinding binding;
    private Linija linija;
    private String currentImageUrl = "";
    private ExecutorService executor;
    private Handler mainHandler;

    // Keep both methods for backward compatibility
    public static LinijeBottomSheetFragment newInstance(Context context, Linija linija) {
        return newInstance(linija);
    }

    public static LinijeBottomSheetFragment newInstance(Linija linija) {
        LinijeBottomSheetFragment fragment = new LinijeBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LINIJA, linija);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize executor and handler
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Get arguments
        if (getArguments() != null) {
            linija = (Linija) getArguments().getSerializable(ARG_LINIJA);
        }

        if (linija == null) {
            Log.e(TAG, "Linija object is null, dismissing dialog");
            dismiss();
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentLinijeBottomSheetBinding.inflate(inflater, container, false);

        setupViews();
        loadLinijaImage();
        setupClickListeners();

        return binding.getRoot();
    }

    private void setupViews() {
        if (linija == null || binding == null) return;

        // Populate header with ID
        binding.linijaIdHeader.setText(getString(R.string.id_linije) + ": " + linija.getLinija_id());

        // Populate basic information
        setSafeText(binding.sapCode, linija.getLinija_SAP(), getString(R.string.ni_sap_kode));
        setSafeText(binding.lineName, linija.getNaziv_linije(), getString(R.string.ni_naziva_linije));

        // Set active status with color coding
        setupActiveStatus();

        // Populate location information
        setSafeText(binding.spaceName, linija.getProstor_naziv(), getString(R.string.ni_prostora));
        setSafeText(binding.locationName, linija.getLokacija_naziv(), getString(R.string.ni_lokacije));
        setSafeText(binding.fullLocation, linija.getFullLocationInfo(), getString(R.string.ni_polne_lokacije));

        // Populate additional information
        binding.prostorId.setText(String.valueOf(linija.getProstor_id()));

        // Set assembly count with color coding for emphasis
        setupAssemblyCount();
    }

    private void setSafeText(android.widget.TextView textView, String value, String fallback) {
        if (value != null && !value.trim().isEmpty()) {
            textView.setText(value);
        } else {
            textView.setText(fallback);
        }
    }

    private void setupActiveStatus() {
        String activeStatus = linija.getLinija_aktivna();
        if (activeStatus != null && !activeStatus.trim().isEmpty()) {
            binding.activeStatus.setText(activeStatus);
            // Color code based on active status
            if (isActiveStatus(activeStatus)) {
                binding.activeStatus.setTextColor(getResources().getColor(R.color.success_primary, null));
            } else {
                binding.activeStatus.setTextColor(getResources().getColor(R.color.error_primary, null));
            }
        } else {
            binding.activeStatus.setText(getString(R.string.ni_statusa));
            binding.activeStatus.setTextColor(getResources().getColor(R.color.content_secondary, null));
        }
    }

    private boolean isActiveStatus(String status) {
        return "Da".equalsIgnoreCase(status) ||
                "Yes".equalsIgnoreCase(status) ||
                "1".equals(status) ||
                "true".equalsIgnoreCase(status);
    }

    private void setupAssemblyCount() {
        int assemblyCount = linija.getStevilo_sklopov();
        binding.assemblyCount.setText(String.valueOf(assemblyCount));
        if (assemblyCount > 0) {
            binding.assemblyCount.setTextColor(getResources().getColor(R.color.action_primary, null));
        } else {
            binding.assemblyCount.setTextColor(getResources().getColor(R.color.content_secondary, null));
        }
    }

    private void loadLinijaImage() {
        if (binding == null || linija == null) return;

        String pictureUrl = buildImageUrl();

        if (pictureUrl.isEmpty()) {
            showNoImageAvailable();
            return;
        }

        // Show loading status
        binding.imageStatus.setVisibility(View.VISIBLE);
        binding.imageStatus.setText(getString(R.string.nalagam_sliko));

        // Check if image exists on server (background thread)
        executor.execute(() -> {
            boolean imageExists = checkImageExists(pictureUrl);

            // Update UI on main thread
            mainHandler.post(() -> {
                if (binding != null && isAdded()) {
                    loadImageResult(pictureUrl, imageExists);
                }
            });
        });
    }

    private String buildImageUrl() {
        String sapCode = linija.getLinija_SAP();

        if (sapCode != null && sapCode.length() >= 6) {
            return "http://192.168.12.192/uploads_images/Orodja_linije/" + sapCode.substring(0, 6) + ".jpg";
        } else if (linija.getLinija_id() > 0) {
            return "http://192.168.12.192/uploads_images/Orodja_linije/linija_" + linija.getLinija_id() + ".jpg";
        }

        return "";
    }

    private boolean checkImageExists(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD"); // Use HEAD instead of GET for efficiency
            connection.setConnectTimeout(3000); // Reduced timeout
            connection.setReadTimeout(3000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            Log.w(TAG, "Failed to check image existence: " + e.getMessage());
            return false;
        }
    }

    private void loadImageResult(String pictureUrl, boolean imageExists) {
        if (!isAdded() || binding == null) return;

        if (imageExists && !pictureUrl.isEmpty()) {
            try {
                // Load image from URL in background
                loadImageFromUrl(pictureUrl);
                currentImageUrl = pictureUrl;

            } catch (Exception e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                showImageError();
            }
        } else {
            showNoImageAvailable();
        }
    }

    private void loadImageFromUrl(String imageUrl) {
        // Show loading status while downloading
        binding.imageStatus.setVisibility(View.VISIBLE);
        binding.imageStatus.setText(getString(R.string.nalagam_sliko));

        executor.execute(() -> {
            try {
                // Download image
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoInput(true);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    connection.disconnect();

                    // Update UI on main thread
                    mainHandler.post(() -> {
                        if (binding != null && isAdded() && bitmap != null) {
                            binding.linijaImage.setImageBitmap(bitmap);
                            binding.imageStatus.setVisibility(View.GONE);
                        } else {
                            showImageError();
                        }
                    });
                } else {
                    connection.disconnect();
                    mainHandler.post(() -> {
                        if (isAdded()) {
                            showImageError();
                        }
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "Failed to download image: " + e.getMessage());
                mainHandler.post(() -> {
                    if (isAdded()) {
                        showImageError();
                    }
                });
            }
        });
    }

    private void showImageError() {
        if (binding != null) {
            binding.linijaImage.setImageResource(R.drawable.bg_image);
            binding.imageStatus.setVisibility(View.VISIBLE);
            binding.imageStatus.setText(getString(R.string.napaka_pri_nalaganju_slike));
            currentImageUrl = "";
        }
    }

    private void showNoImageAvailable() {
        if (binding != null) {
            binding.linijaImage.setImageResource(R.drawable.bg_image);
            binding.imageStatus.setVisibility(View.VISIBLE);
            binding.imageStatus.setText(getString(R.string.slika_ni_na_voljo));
            currentImageUrl = "";
        }
    }

    private void setupClickListeners() {
        if (binding == null || linija == null) return;

        // Image click listener - show image in a custom dialog instead of intent
        binding.linijaImage.setOnClickListener(v -> showImageDialog());

        // SAP code click
        binding.sapCode.setOnClickListener(v -> {
            String sapCode = linija.getLinija_SAP();
            if (sapCode != null && !sapCode.trim().isEmpty()) {
                showToast(getString(R.string.sap_koda) + ": " + sapCode);
            }
        });

        // Line name click
        binding.lineName.setOnClickListener(v -> {
            String lineName = linija.getNaziv_linije();
            if (lineName != null && !lineName.trim().isEmpty()) {
                showToast(getString(R.string.naziv_linije) + ": " + lineName);
            }
        });

        // Active status click
        binding.activeStatus.setOnClickListener(v -> showActiveStatusInfo());

        // Space name click
        binding.spaceName.setOnClickListener(v -> {
            String spaceName = linija.getProstor_naziv();
            if (spaceName != null && !spaceName.trim().isEmpty()) {
                showToast(getString(R.string.prostor) + ": " + spaceName);
            }
        });

        // Location name click
        binding.locationName.setOnClickListener(v -> {
            String locationName = linija.getLokacija_naziv();
            if (locationName != null && !locationName.trim().isEmpty()) {
                showToast(getString(R.string.lokacija) + ": " + locationName);
            }
        });

        // Full location click
        binding.fullLocation.setOnClickListener(v -> {
            String fullLocationInfo = linija.getFullLocationInfo();
            if (fullLocationInfo != null && !fullLocationInfo.trim().isEmpty()) {
                showToast(getString(R.string.polna_lokacija) + ": " + fullLocationInfo);
            }
        });

        // Prostor ID click
        binding.prostorId.setOnClickListener(v ->
                showToast(getString(R.string.prostor_id) + ": " + linija.getProstor_id()));

        // Assembly count click
        binding.assemblyCount.setOnClickListener(v -> showAssemblyCountInfo());

        // Long click on line name for extended information
        binding.lineName.setOnLongClickListener(v -> {
            showExtendedLineInfo();
            return true;
        });
    }

    private void showImageDialog() {
    }

    private void showActiveStatusInfo() {
        String statusMessage = getString(R.string.status_linije) + ": ";
        String activeStatus = linija.getLinija_aktivna();

        if (isActiveStatus(activeStatus)) {
            statusMessage += getString(R.string.linija_je_aktivna);
        } else {
            statusMessage += getString(R.string.linija_ni_aktivna);
        }

        showToast(statusMessage);
    }

    private void showAssemblyCountInfo() {
        int count = linija.getStevilo_sklopov();
        String message = getString(R.string.stevilo_sklopov) + ": " + count;

        if (count > 0) {
            message += "\n" + getString(R.string.klikni_za_prikaz_sklopov);
        } else {
            message += "\n" + getString(R.string.ni_sklopov_na_liniji);
        }

        showToast(message);
    }

    private void showExtendedLineInfo() {
        StringBuilder extendedInfo = new StringBuilder();
        extendedInfo.append(getString(R.string.podrobnosti_linije)).append(":\n");
        extendedInfo.append(getString(R.string.id_linije)).append(": ").append(linija.getLinija_id()).append("\n");
        extendedInfo.append(getString(R.string.sap_koda)).append(": ")
                .append(linija.getLinija_SAP() != null ? linija.getLinija_SAP() : "-").append("\n");
        extendedInfo.append(getString(R.string.polna_lokacija)).append(": ")
                .append(linija.getFullLocationInfo() != null ? linija.getFullLocationInfo() : "-");

        showToast(extendedInfo.toString());
    }

    private void showToast(String message) {
        if (getContext() != null && isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Shutdown executor to prevent memory leaks
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }

        // Clean up binding when the view is destroyed to prevent memory leaks
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Ensure executor is properly cleaned up
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}