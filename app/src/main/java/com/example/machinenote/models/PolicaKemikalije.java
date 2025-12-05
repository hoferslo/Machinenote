package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Map;
public class PolicaKemikalije implements  DisplayableItem {

    @SerializedName("ID")
    private int id;

    @SerializedName("Polica")
    private String polica;

    @SerializedName("Omara_ID")
    private Integer omaraId;

    // Constructors
    public PolicaKemikalije() {
    }

    public PolicaKemikalije(int id, String polica, Integer omaraId) {
        this.id = id;
        this.polica = polica;
        this.omaraId = omaraId;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getPolica() {
        return polica;
    }

    public Integer getOmaraId() {
        return omaraId;
    }

    public Integer getOmara_ID() {
        return omaraId;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setPolica(String polica) {
        this.polica = polica;
    }

    public void setOmaraId(Integer omaraId) {
        this.omaraId = omaraId;
    }

    public void setOmara_ID(Integer omaraId) {
        this.omaraId = omaraId;
    }

    @Override
    public String toString() {
        return "PolicaKemikalije{" +
                "id=" + id +
                ", polica='" + polica + '\'' +
                ", omaraId=" + omaraId +
                '}';
    }

    @Override
    public Map<String, String> getDisplayFields() {
        return Collections.emptyMap();
    }
}