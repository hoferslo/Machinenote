package com.example.machinenote.models;

public class Linija {
    private int linija_id;
    private String linija_SAP;
    private String naziv_linije;
    private String linija_aktivna;
    private int prostor_id;
    private String lokacija;

    // Constructor
    public Linija(int linija_id, String linija_SAP, String naziv_linije, String linija_aktivna, int prostor_id, String lokacija) {
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

    public String getLinijeSapAndNames() {
        return linija_SAP + " : " + naziv_linije;
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
}
