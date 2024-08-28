package com.example.machinenote.Utility;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.machinenote.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageHelper {
    public static void handleImage(Context context, Bitmap bitmap, LinearLayout Ll) {
        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        float aspectRatio = (float) imageHeight / imageWidth;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        int maxWidth = context.getResources().getDisplayMetrics().widthPixels; // Width of the screen or parent
        params.height = (int) (maxWidth * aspectRatio); // Height adjusted by aspect ratio
        imageView.setLayoutParams(params);
        imageView.setPadding(0, 0, 0, 0);

        imageView.setOnClickListener(v -> showDeleteConfirmationDialog(context, imageView, bitmap, Ll));

        Ll.addView(imageView);
    }

    public static void showDeleteConfirmationDialog(Context context, ImageView imageView, Bitmap bitmap, LinearLayout Ll) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete_image))
                .setMessage(context.getString(R.string.confirm_delete_image))
                .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> {
                    // Remove the image from the layout
                    Ll.removeView(imageView);
                    // Optionally, you can also recycle the bitmap if it's no longer needed
                    bitmap.recycle();
                })
                .setNegativeButton(context.getString(R.string.no), null)
                .show();
    }

    public static List<File> getImagesFromLayout(Context context, LinearLayout Ll) {
        List<File> imageFiles = new ArrayList<>();
        int childCount = Ll.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View view = Ll.getChildAt(i);
            if (view instanceof ImageView imageView) {
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                File file = bitmapToFile(context, bitmap, "image_" + i + ".jpg");
                imageFiles.add(file);
            }
        }

        return imageFiles;
    }

    public static File bitmapToFile(Context context, Bitmap bitmap, String fileName) {
        File file = new File(context.getCacheDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos); //todo maybe put settings for this
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
