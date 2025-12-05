package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Map;

public class OmaraKemikalije implements  DisplayableItem{

    @SerializedName("ID")
    private int id;

    @SerializedName("Omara_ime")
    private String omaraIme;

    @SerializedName("Prostor_ID")
    private Integer prostorId;

    @SerializedName("Program_ID")
    private Integer programId;

    // Constructors
    public OmaraKemikalije() {
    }

    public OmaraKemikalije(int id, String omaraIme, Integer prostorId, Integer programId) {
        this.id = id;
        this.omaraIme = omaraIme;
        this.prostorId = prostorId;
        this.programId = programId;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getOmaraIme() {
        return omaraIme;
    }

    public String getOmara_ime() {
        return omaraIme;
    }

    public Integer getProstorId() {
        return prostorId;
    }

    public Integer getProgramId() {
        return programId;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setOmaraIme(String omaraIme) {
        this.omaraIme = omaraIme;
    }

    public void setProstorId(Integer prostorId) {
        this.prostorId = prostorId;
    }

    public void setProgramId(Integer programId) {
        this.programId = programId;
    }

    @Override
    public String toString() {
        return "OmaraKemikalije{" +
                "id=" + id +
                ", omaraIme='" + omaraIme + '\'' +
                ", prostorId=" + prostorId +
                ", programId=" + programId +
                '}';
    }

    @Override
    public Map<String, String> getDisplayFields() {
        return Collections.emptyMap();
    }
}