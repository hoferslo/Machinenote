package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

public class ImageCandidate {

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("original")
    private String original;

    @SerializedName("title")
    private String title;

    @SerializedName("source")
    private String source;

    public String getThumbnail() { return thumbnail; }
    public String getOriginal() { return original; }
    public String getTitle() { return title; }
    public String getSource() { return source; }
}