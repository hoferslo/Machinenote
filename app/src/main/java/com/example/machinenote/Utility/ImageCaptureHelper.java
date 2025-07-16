package com.example.machinenote.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.FileProvider;

import com.example.machinenote.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageCaptureHelper {

    private static final String TAG = "ImageCaptureHelper";
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    private final Context context;
    private ImageCaptureCallback callback;
    private String currentPhotoPath;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private boolean isFromCamera = false; // Track the source of the image

    public interface ImageCaptureCallback {
        void onImageCaptured(Bitmap bitmap);
        void onError(String error);
    }

    public ImageCaptureHelper(Context context, ActivityResultLauncher<Intent> cameraLauncher) {
        this.context = context;
        this.cameraLauncher = cameraLauncher;
    }

    public ImageCaptureHelper(Context context, ActivityResultLauncher<Intent> cameraLauncher, ActivityResultLauncher<Intent> galleryLauncher) {
        this.context = context;
        this.cameraLauncher = cameraLauncher;
        this.galleryLauncher = galleryLauncher;
    }

    public ImageCaptureHelper(Context context) {
        this.context = context;
    }

    public void setImageCaptureCallback(ImageCaptureCallback callback) {
        this.callback = callback;
    }

    public void captureImage() {
        showImageSourceDialog();
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.select_image_source));

        String[] options = {
                context.getString(R.string.camera),
                context.getString(R.string.gallery)
        };

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    captureImageFromCamera();
                    break;
                case 1:
                    captureImageFromGallery();
                    break;
            }
        });

        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.show();
    }

    public void captureImageFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file", ex);
                if (callback != null) callback.onError("Error creating image file");
                return;
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                currentPhotoPath = photoFile.getAbsolutePath();
                isFromCamera = true; // Set flag for camera
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            if (callback != null)
                callback.onError("No camera activity found to handle the intent.");
        }
    }

    // Renamed from captureImageAnswer for consistency
    public void captureImageAnswer() {
        captureImageFromCamera();
    }

    public void captureImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            isFromCamera = false; // Set flag for gallery
            currentPhotoPath = null; // Clear any previous camera path

            if (galleryLauncher != null) {
                galleryLauncher.launch(intent);
            } else {
                cameraLauncher.launch(intent);
            }
        } else {
            if (callback != null)
                callback.onError("No gallery activity found to handle the intent.");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getFilesDir();
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void handleActivityResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = null;

            // First check if data has URI (gallery selection)
            if (data != null && data.getData() != null) {
                // Handle gallery selection
                Uri selectedImageUri = data.getData();
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(selectedImageUri);
                    if (inputStream != null) {
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error loading image from gallery", e);
                    if (callback != null) callback.onError("Failed to load image from gallery");
                    resetState();
                    return;
                }
            } else if (currentPhotoPath != null && new File(currentPhotoPath).exists()) {
                // Handle camera capture - only if file exists
                bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            }

            if (bitmap != null) {
                if (callback != null) callback.onImageCaptured(bitmap);
            } else {
                if (callback != null) callback.onError("Failed to load image");
            }

            resetState();
        } else {
            if (callback != null) callback.onError("Image capture failed or canceled");
            resetState();
        }
    }

    private void resetState() {
        isFromCamera = false;
        currentPhotoPath = null;
    }

    // Overloaded method for backward compatibility
    public void handleActivityResult(int resultCode) {
        handleActivityResult(resultCode, null);
    }

    public void deleteAllImages() {
        File storageDir = context.getFilesDir();
        if (storageDir.isDirectory()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jpg")) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            Log.e(TAG, "Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        } else {
            Log.e(TAG, "The specified directory is not a directory or does not exist.");
        }
    }
}