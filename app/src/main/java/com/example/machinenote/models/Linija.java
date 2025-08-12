package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

public class Linija {
    // Use @SerializedName to match PHP response field names
    @SerializedName("id")
    private int linija_id;

    @SerializedName("linija_sap")
    private String linija_SAP;

    @SerializedName("naziv_linije")
    private String naziv_linije;

    @SerializedName("aktivna")
    private String linija_aktivna;

    @SerializedName("prostor_id")
    private int prostor_id;

    @SerializedName("lokacija")  // This maps to "dodatna_lokacija" in PHP
    private String lokacija;

    // Additional fields that come from your PHP response
    @SerializedName("prostor_naziv")
    private String prostor_naziv;

    @SerializedName("lokacija_naziv")
    private String lokacija_naziv;

    @SerializedName("stevilo_sklopov")
    private int stevilo_sklopov;

    // Default constructor (required for Gson)
    public Linija() {
    }

    // Constructor with all fields
    public Linija(int linija_id, String linija_SAP, String naziv_linije, String linija_aktivna,
                  int prostor_id, String lokacija) {
        this.linija_id = linija_id;
        this.linija_SAP = linija_SAP;
        this.naziv_linije = naziv_linije;
        this.linija_aktivna = linija_aktivna;
        this.prostor_id = prostor_id;
        this.lokacija = lokacija;
    }

    // Getters and Setters
    public int getLinija_id() {
        return linija_id;
    }

    public void setLinija_id(int linija_id) {
        this.linija_id = linija_id;
    }

    public String getLinija_SAP() {
        return linija_SAP;
    }

    public void setLinija_SAP(String linija_SAP) {
        this.linija_SAP = linija_SAP;
    }

    public String getNaziv_linije() {
        return naziv_linije;
    }

    public void setNaziv_linije(String naziv_linije) {
        this.naziv_linije = naziv_linije;
    }

    public String getLinija_aktivna() {
        return linija_aktivna;
    }

    public void setLinija_aktivna(String linija_aktivna) {
        this.linija_aktivna = linija_aktivna;
    }

    public int getProstor_id() {
        return prostor_id;
    }

    public void setProstor_id(int prostor_id) {
        this.prostor_id = prostor_id;
    }

    public String getLokacija() {
        return lokacija;
    }

    public void setLokacija(String lokacija) {
        this.lokacija = lokacija;
    }

    // Additional getters/setters for new fields
    public String getProstor_naziv() {
        return prostor_naziv;
    }

    public void setProstor_naziv(String prostor_naziv) {
        this.prostor_naziv = prostor_naziv;
    }

    public String getLokacija_naziv() {
        return lokacija_naziv;
    }

    public void setLokacija_naziv(String lokacija_naziv) {
        this.lokacija_naziv = lokacija_naziv;
    }

    public int getStevilo_sklopov() {
        return stevilo_sklopov;
    }

    public void setStevilo_sklopov(int stevilo_sklopov) {
        this.stevilo_sklopov = stevilo_sklopov;
    }

    // Helper method for display
    public String getLinijeSapAndNames() {
        return linija_SAP + " : " + naziv_linije;
    }

    // Helper method to get full location info
    public String getFullLocationInfo() {
        if (lokacija_naziv != null && prostor_naziv != null) {
            return lokacija_naziv + " - " + prostor_naziv;
        } else if (prostor_naziv != null) {
            return prostor_naziv;
        } else if (lokacija_naziv != null) {
            return lokacija_naziv;
        }
        return "";
    }

    @Override
    public String toString() {
        return "Linija{" +
                "linija_id=" + linija_id +
                ", linija_SAP='" + linija_SAP + '\'' +
                ", naziv_linije='" + naziv_linije + '\'' +
                ", linija_aktivna='" + linija_aktivna + '\'' +
                ", prostor_id=" + prostor_id +
                ", lokacija='" + lokacija + '\'' +
                ", prostor_naziv='" + prostor_naziv + '\'' +
                ", lokacija_naziv='" + lokacija_naziv + '\'' +
                '}';
    }
}