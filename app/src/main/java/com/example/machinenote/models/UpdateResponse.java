package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

public class UpdateResponse {
    @SerializedName("update_available")
    private boolean updateAvailable;

    @SerializedName("latest_version")
    private String latestVersion;

    @SerializedName("download_url")
    private String downloadUrl;

    @SerializedName("is_forced")
    private boolean isForced;

    @SerializedName("release_notes")
    private String releaseNotes;

    @SerializedName("file_size_bytes")
    private long fileSizeBytes;

    // Getters
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public boolean isForced() {
        return isForced;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }
}