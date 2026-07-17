package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

public class SaveImageRequest {

    @SerializedName("id")
    private int id;

    // SPREMENJENO: Uskajeno s PHP-jem
    @SerializedName("slika_url")
    private String imageUrl;

    public SaveImageRequest(int id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}