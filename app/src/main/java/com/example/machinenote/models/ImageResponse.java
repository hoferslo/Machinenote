package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ImageResponse {

    @SerializedName("query")
    private String query;

    @SerializedName("kandidati")
    private List<ImageCandidate> kandidati;

    @SerializedName("rezerva")
    private List<ImageCandidate> rezerva;

    public String getQuery() {
        return query;
    }

    public List<ImageCandidate> getKandidati() {
        return kandidati;
    }

    public List<ImageCandidate> getRezerva() {
        return rezerva;
    }
}