package com.example.machinenote.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageCaptureHelper {

    private static final String TAG = "ImageCaptureHelper";

    private final Context context;
    private ImageCaptureCallback callback;
    private String currentPhotoPath;
    private ActivityResultLauncher<Intent> cameraLauncher;

    public interface ImageCaptureCallback {
        void onImageCaptured(Bitmap bitmap);

        void onError(String error);
    }

    public ImageCaptureHelper(Context context, ActivityResultLauncher<Intent> cameraLauncher) {
        this.context = context;
        this.cameraLauncher = cameraLauncher;
    }

    public ImageCaptureHelper(Context context) {
        this.context = context;

    }

    public void setImageCaptureCallback(ImageCaptureCallback callback) {
        this.callback = callback;
    }

    public void captureImage() {

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
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            if (callback != null)
                callback.onError("No camera activity found to handle the intent.");
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

    public void handleActivityResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            if (bitmap != null) {
                if (callback != null) callback.onImageCaptured(bitmap);
            } else {
                if (callback != null) callback.onError("Failed to load image");
            }
        } else {
            if (callback != null) callback.onError("Image capture failed or canceled");
        }
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
