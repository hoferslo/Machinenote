package com.example.machinenote.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentQRCodeScannerBinding;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;
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

    public interface QRCodeScannerListener {
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

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), this::scanQRCode);

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void scanQRCode(ImageProxy imageProxy) {
        if (!scanned && imageProxy.getImage() != null) {
            try {
                // Pridobi YUV byte array iz ImageProxy
                byte[] data = imageProxyToByteArray(imageProxy);
                int width = imageProxy.getWidth();
                int height = imageProxy.getHeight();

                // Uporabi ZXing za dekodiranje (pravilno handla encoding!)
                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                        data, width, height, 0, 0, width, height, false);

                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                MultiFormatReader reader = new MultiFormatReader();

                try {
                    Result result = reader.decode(bitmap);
                    String qrValue = result.getText();

                    android.util.Log.d(TAG, "ZXing decoded: " + qrValue);

                    if (qrValue != null && qrCodeScanCallback != null) {
                        requireActivity().runOnUiThread(() -> {
                            listener.onQRCodeScanned(qrValue);
                            qrCodeScanCallback.onQRCodeScanned(qrValue);
                            ((MainActivity) context).clearLastFragmentFromBackStack();
                        });
                        scanned = true;
                    }
                } catch (Exception e) {
                    // QR koda ni najdena v tem frame-u, nadaljuj skeniranje
                }
            } catch (Exception e) {
                android.util.Log.e(TAG, "Scan error: " + e.getMessage());
            } finally {
                imageProxy.close();
            }
        } else {
            imageProxy.close();
        }
    }

    // Helper metoda za konverzijo ImageProxy v byte array
    private byte[] imageProxyToByteArray(ImageProxy imageProxy) {
        ImageProxy.PlaneProxy yPlane = imageProxy.getPlanes()[0];
        ByteBuffer yBuffer = yPlane.getBuffer();
        byte[] data = new byte[yBuffer.remaining()];
        yBuffer.get(data);
        return data;
    }

    public interface QRCodeScanCallback {
        void onQRCodeScanned(String qrCode);
        void onScanCancelled();
    }
}