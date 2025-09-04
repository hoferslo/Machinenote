package com.example.machinenote.Utility;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.machinenote.ApiManager;
import com.example.machinenote.models.UpdateResponse;
import com.example.machinenote.activities.MainActivity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateManager {
    private static final String TAG = "UpdateManager";
    private static final int REQUEST_INSTALL_PACKAGES = 1001;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1002;

    private Context context;
    private ApiManager apiManager;
    private UpdateCallback callback;
    private UpdateResponse pendingUpdate;

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
                pendingUpdate = updateResponse;
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

        // Add version conflict warning
        message += "\n\nOpomba: Po prenosu se bo aplikacija zaprla za uspešno namestitev posodobitve.";

        builder.setMessage(message);

        builder.setPositiveButton("Prenesi", (dialog, which) -> {
            checkPermissionsAndDownload(updateInfo.getDownloadUrl());
        });

        // Add option to uninstall first
        builder.setNeutralButton("Odstrani in prenesi", (dialog, which) -> {
            showUninstallDialog(updateInfo.getDownloadUrl());
        });

        if (!updateInfo.isForced()) {
            builder.setNegativeButton("Pozneje", (dialog, which) -> dialog.dismiss());
        } else {
            builder.setCancelable(false);
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showUninstallDialog(String downloadUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Odstranitev aplikacije");
        builder.setMessage("Odprl se bo meni za odstranitev aplikacije. Po odstranitvi ponovno zaženite APK datoteko za namestitev nove različice.\n\nAli želite nadaljevati?");

        builder.setPositiveButton("Da, odstrani", (dialog, which) -> {
            // First download the APK
            checkPermissionsAndDownload(downloadUrl);

            // Then open uninstall dialog
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to open uninstall dialog", e);
                Toast.makeText(context, "Ne morem odpreti menija za odstranitev", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Prekliči", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void checkPermissionsAndDownload(String downloadUrl) {
        Log.d(TAG, "Checking permissions for download...");

        // Check for install permission (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean canInstall = context.getPackageManager().canRequestPackageInstalls();
            Log.d(TAG, "Can install packages: " + canInstall);
            if (!canInstall) {
                requestInstallPermission();
                return;
            }
        }

        // For Android 10+ (API 29+), we don't need WRITE_EXTERNAL_STORAGE for app-specific directories
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Storage permission not granted");
                requestStoragePermission();
                return;
            }
        }

        Log.d(TAG, "All permissions granted, starting download...");
        downloadAndInstallUpdate(downloadUrl);
    }

    private void requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Potrebno dovoljenje");
            builder.setMessage("Za namestitev posodobitve potrebujemo dovoljenje za namestitev aplikacij iz neznanih virov.");

            builder.setPositiveButton("Dovoli", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                intent.setData(Uri.parse("package:" + context.getPackageName()));

                if (context instanceof MainActivity) {
                    ((MainActivity) context).startActivityForResult(intent, REQUEST_INSTALL_PACKAGES);
                } else {
                    context.startActivity(intent);
                }
            });

            builder.setNegativeButton("Prekliči", (dialog, which) -> {
                Toast.makeText(context, "Brez dovoljenja posodobitev ni mogoča", Toast.LENGTH_LONG).show();
            });

            builder.show();
        }
    }

    private void requestStoragePermission() {
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Potrebno dovoljenje");
            builder.setMessage("Za prenos posodobitve potrebujemo dostop do shranjevanja.");

            builder.setPositiveButton("Dovoli", (dialog, which) -> {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            });

            builder.setNegativeButton("Prekliči", (dialog, which) -> {
                Toast.makeText(context, "Brez dovoljenja posodobitev ni mogoča", Toast.LENGTH_LONG).show();
            });

            builder.show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_INSTALL_PACKAGES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean canInstall = context.getPackageManager().canRequestPackageInstalls();
                Log.d(TAG, "Install permission result: " + canInstall);
                if (canInstall) {
                    checkPermissionsAndDownload(pendingUpdate != null ? pendingUpdate.getDownloadUrl() : "");
                } else {
                    Toast.makeText(context, "Dovoljenje za namestitev ni bilo odobreno", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Storage permission granted");
                checkPermissionsAndDownload(pendingUpdate != null ? pendingUpdate.getDownloadUrl() : "");
            } else {
                Log.d(TAG, "Storage permission denied");
                Toast.makeText(context, "Dovoljenje za shranjevanje ni bilo odobreno", Toast.LENGTH_LONG).show();
            }
        }
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
        Log.d(TAG, "Starting download from: " + downloadUrl);

        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        connection.connect();

        int fileLength = connection.getContentLength();
        Log.d(TAG, "File size: " + fileLength + " bytes");

        // Use app-specific external directory (doesn't require storage permission on Android 10+)
        File downloadDir = context.getExternalFilesDir("updates");
        if (downloadDir == null) {
            // Fallback to internal storage
            downloadDir = new File(context.getFilesDir(), "updates");
        }

        if (!downloadDir.exists()) {
            boolean created = downloadDir.mkdirs();
            Log.d(TAG, "Created download directory: " + created + " at " + downloadDir.getAbsolutePath());
        }

        File apkFile = new File(downloadDir, "update_" + System.currentTimeMillis() + ".apk");
        Log.d(TAG, "Downloading to: " + apkFile.getAbsolutePath());

        if (apkFile.exists()) {
            apkFile.delete();
        }

        FileOutputStream fileOutput = new FileOutputStream(apkFile);
        InputStream inputStream = connection.getInputStream();

        byte[] buffer = new byte[4096];
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

        Log.d(TAG, "APK downloaded successfully. File exists: " + apkFile.exists() + ", Size: " + apkFile.length());
        return apkFile;
    }

    private void installApk(File apkFile) {
        Log.d(TAG, "Starting APK installation...");
        Log.d(TAG, "APK file path: " + apkFile.getAbsolutePath());
        Log.d(TAG, "APK file exists: " + apkFile.exists());
        Log.d(TAG, "APK file size: " + apkFile.length());
        Log.d(TAG, "APK file readable: " + apkFile.canRead());

        if (!apkFile.exists()) {
            showErrorDialog("APK datoteka ni bila najdena na: " + apkFile.getAbsolutePath());
            return;
        }

        // Try multiple installation methods
        boolean installed = false;

        // Method 1: Try with FileProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            installed = tryFileProviderInstall(apkFile);
        }

        // Method 2: Try direct file URI (for older Android versions)
        if (!installed && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            installed = tryDirectFileInstall(apkFile);
        }

        // Method 3: Try copying to different location
        if (!installed) {
            installed = tryCopyAndInstall(apkFile);
        }

        if (!installed) {
            showErrorDialog("Namestitev ni uspela. Preverite nastavitve za namestitve iz neznanih virov.");
        }
    }

    private boolean tryFileProviderInstall(File apkFile) {
        try {
            Log.d(TAG, "Trying FileProvider installation...");

            Uri apkUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    apkFile
            );

            Log.d(TAG, "FileProvider URI: " + apkUri.toString());

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            context.startActivity(intent);
            Log.d(TAG, "FileProvider installation started successfully");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "FileProvider installation failed", e);
            return false;
        }
    }

    private boolean tryDirectFileInstall(File apkFile) {
        try {
            Log.d(TAG, "Trying direct file installation...");

            Uri apkUri = Uri.fromFile(apkFile);
            Log.d(TAG, "Direct file URI: " + apkUri.toString());

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
            Log.d(TAG, "Direct file installation started successfully");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Direct file installation failed", e);
            return false;
        }
    }

    private boolean tryCopyAndInstall(File apkFile) {
        try {
            Log.d(TAG, "Trying copy and install method...");

            // Copy to internal files directory
            File internalDir = new File(context.getFilesDir(), "shared");
            if (!internalDir.exists()) {
                internalDir.mkdirs();
            }

            File copiedApk = new File(internalDir, "update.apk");

            // Copy file
            FileInputStream fis = new FileInputStream(apkFile);
            FileOutputStream fos = new FileOutputStream(copiedApk);

            byte[] buffer = new byte[4096];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            fis.close();
            fos.close();

            Log.d(TAG, "File copied to: " + copiedApk.getAbsolutePath());

            // Try installing copied file
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return tryFileProviderInstall(copiedApk);
            } else {
                return tryDirectFileInstall(copiedApk);
            }

        } catch (Exception e) {
            Log.e(TAG, "Copy and install failed", e);
            return false;
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