package com.example.machinenote.Utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.example.machinenote.ApiManager;
import com.example.machinenote.models.UpdateResponse;
import com.example.machinenote.activities.MainActivity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateManager {
    private static final String TAG = "UpdateManager";
    private Context context;
    private ApiManager apiManager;
    private UpdateCallback callback;

    public interface UpdateCallback {
        void onUpdateCheckComplete();
        void onUpdateCheckFailed(String error);
    }

    public UpdateManager(Context context, ApiManager apiManager) {
        this.context = context;
        this.apiManager = apiManager;
    }

    public void checkForUpdate() {
        checkForUpdate(null);
    }

    public void checkForUpdate(UpdateCallback callback) {
        this.callback = callback;

        String currentVersion = getCurrentAppVersion();
        String packageName = context.getPackageName();

        apiManager.checkForUpdate(currentVersion, packageName, new ApiManager.UpdateVersionCallback() {
            @Override
            public void onUpdateAvailable(UpdateResponse updateResponse) {
                showUpdateDialog(updateResponse);
                if (UpdateManager.this.callback != null) {
                    UpdateManager.this.callback.onUpdateCheckComplete();
                }
            }

            @Override
            public void onNoUpdateNeeded() {
                Log.d(TAG, "App is up to date");
                if (UpdateManager.this.callback != null) {
                    UpdateManager.this.callback.onUpdateCheckComplete();
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Update check failed: " + error);
                if (UpdateManager.this.callback != null) {
                    UpdateManager.this.callback.onUpdateCheckFailed(error);
                }
            }
        });
    }

    private String getCurrentAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting app version", e);
            return "1.0.0";
        }
    }

    private void showUpdateDialog(UpdateResponse updateInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Posodobitev na voljo");

        String message = "Nova različica " + updateInfo.getLatestVersion() + " je na voljo.";
        if (updateInfo.getReleaseNotes() != null && !updateInfo.getReleaseNotes().isEmpty()) {
            message += "\n\nNovosti:\n" + updateInfo.getReleaseNotes();
        }
        if (updateInfo.getFileSizeBytes() > 0) {
            message += "\n\nVelikost: " + formatFileSize(updateInfo.getFileSizeBytes());
        }

        builder.setMessage(message);

        builder.setPositiveButton("Prenesi", (dialog, which) -> {
            downloadAndInstallUpdate(updateInfo.getDownloadUrl());
        });

        if (!updateInfo.isForced()) {
            builder.setNegativeButton("Pozneje", (dialog, which) -> dialog.dismiss());
        } else {
            builder.setCancelable(false);
            builder.setNeutralButton("Zapri aplikacijo", (dialog, which) -> {
                System.exit(0); // Force close app
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void downloadAndInstallUpdate(String downloadUrl) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Prenašam posodobitev...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                File apkFile = downloadApk(downloadUrl, progressDialog);

                // Run on UI thread
                if (context instanceof MainActivity) {
                    ((MainActivity) context).runOnUiThread(() -> {
                        progressDialog.dismiss();
                        installApk(apkFile);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Download failed", e);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).runOnUiThread(() -> {
                        progressDialog.dismiss();
                        showErrorDialog("Prenos ni uspel: " + e.getMessage());
                    });
                }
            }
        }).start();
    }

    private File downloadApk(String downloadUrl, ProgressDialog progressDialog) throws Exception {
        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        int fileLength = connection.getContentLength();

        File apkFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk");

        // Delete existing file if it exists
        if (apkFile.exists()) {
            apkFile.delete();
        }

        FileOutputStream fileOutput = new FileOutputStream(apkFile);
        InputStream inputStream = connection.getInputStream();

        byte[] buffer = new byte[1024];
        int bufferLength;
        int total = 0;

        while ((bufferLength = inputStream.read(buffer)) > 0) {
            total += bufferLength;
            fileOutput.write(buffer, 0, bufferLength);

            if (fileLength > 0) {
                int progress = (int) (total * 100 / fileLength);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).runOnUiThread(() ->
                            progressDialog.setProgress(progress)
                    );
                }
            }
        }

        fileOutput.close();
        inputStream.close();
        connection.disconnect();

        return apkFile;
    }

    private void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri apkUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    apkFile
            );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            apkUri = Uri.fromFile(apkFile);
        }

        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start APK installation", e);
            showErrorDialog("Namestitev ni mogoča. Prosimo omogočite namestitve iz neznanih virov.");
        }
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Napaka");
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    // Static method for easy version checking
    public static String getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0.0";
        }
    }
}