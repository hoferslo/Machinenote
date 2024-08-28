package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentQRCodeScannerBinding;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;

public class QRCodeScannerFragment extends Fragment {

    private FragmentQRCodeScannerBinding binding;
    private QRCodeScannerListener listener;
    private Context context;
    private boolean scanned = false;
    public String TAG = "QR branje";

    private QRCodeScanCallback qrCodeScanCallback;

    public static QRCodeScannerFragment newInstance(Context context, QRCodeScanCallback callback) {
        QRCodeScannerFragment fragment = new QRCodeScannerFragment();
        fragment.context = context;
        fragment.setQRCodeScanCallback(callback);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
    }

    public void setQRCodeScanCallback(QRCodeScanCallback callback) {
        this.qrCodeScanCallback = callback;
    }

    public interface QRCodeScannerListener { //todo maybe throw this out, its not being used (only in mainActivity)
        void onQRCodeScanned(String result);
        void onScanCancelled();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof QRCodeScannerListener) {
            listener = (QRCodeScannerListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement QRCodeScannerListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentQRCodeScannerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.cancelButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onScanCancelled();
                qrCodeScanCallback.onScanCancelled();
                ((MainActivity) context).clearLastFragmentFromBackStack();
            }
        });

        startCamera();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .setTargetResolution(new Size(1080, 1920))
                .build();

        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1080, 1920))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), imageProxy -> {
            scanQRCode(imageProxy);
        });

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private void scanQRCode(ImageProxy imageProxy) {
        if (!scanned) {
            @NonNull ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
            if (planes.length > 0) {
                InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

                BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build();

                BarcodeScanning.getClient(options)
                        .process(image)
                        .addOnSuccessListener(barcodes -> {
                            for (Barcode barcode : barcodes) {
                                if (barcode.getRawValue() != null && qrCodeScanCallback != null) {
                                    listener.onQRCodeScanned(barcode.getRawValue());
                                    qrCodeScanCallback.onQRCodeScanned(barcode.getRawValue());
                                    ((MainActivity) context).clearLastFragmentFromBackStack();
                                    scanned = true;
                                    break;
                                }
                            }
                        })
                        .addOnFailureListener(Throwable::printStackTrace)
                        .addOnCompleteListener(task -> imageProxy.close());
            } else {
                imageProxy.close();
            }
        }
    }

    public interface QRCodeScanCallback {
        void onQRCodeScanned(String qrCode);
        void onScanCancelled();
    }

}
