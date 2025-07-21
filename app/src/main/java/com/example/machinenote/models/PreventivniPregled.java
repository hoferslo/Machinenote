package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class PreventivniPregled implements DisplayableItem {

    @SerializedName("ID")
    private int id;

    @SerializedName("Naziv")
    private String naziv;

    @SerializedName("Datum")
    private String datum;

    @SerializedName("Status")
    private String status;

    @SerializedName("Linija_SAP")
    private String linijaSAP;

    @SerializedName("NazivLinije")
    private String nazivLinije;

    @SerializedName("Lokacija")
    private String lokacija;

    @SerializedName("Prostor")
    private String prostor;

    @SerializedName("TotalOpravila")
    private int totalOpravila;

    @SerializedName("CompletedOpravila")
    private int completedOpravila;

    @SerializedName("Progress")
    private double progress;

    @SerializedName("Vzdrzevalec")
    private String vzdrzevalec;

    @SerializedName("Tip_Pregleda")
    private String tipPregleda;

    @SerializedName("Datum_Ustvarjen")
    private String datumUstvarjen;

    @SerializedName("Datum_Zakljucen")
    private String datumZakljucen;

    // Constructors
    public PreventivniPregled() {}

    public PreventivniPregled(int id, String naziv, String datum, String status) {
        this.id = id;
        this.naziv = naziv;
        this.datum = datum;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLinijaSAP() {
        return linijaSAP;
    }

    public void setLinijaSAP(String linijaSAP) {
        this.linijaSAP = linijaSAP;
    }

    public String getNazivLinije() {
        return nazivLinije;
    }

    public void setNazivLinije(String nazivLinije) {
        this.nazivLinije = nazivLinije;
    }

    public String getLokacija() {
        return lokacija;
    }

    public void setLokacija(String lokacija) {
        this.lokacija = lokacija;
    }

    public String getProstor() {
        return prostor;
    }

    public void setProstor(String prostor) {
        this.prostor = prostor;
    }

    public int getTotalOpravila() {
        return totalOpravila;
    }

    public void setTotalOpravila(int totalOpravila) {
        this.totalOpravila = totalOpravila;
    }

    public int getCompletedOpravila() {
        return completedOpravila;
    }

    public void setCompletedOpravila(int completedOpravila) {
        this.completedOpravila = completedOpravila;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public String getVzdrzevalec() {
        return vzdrzevalec;
    }

    public void setVzdrzevalec(String vzdrzevalec) {
        this.vzdrzevalec = vzdrzevalec;
    }

    public String getTipPregleda() {
        return tipPregleda;
    }

    public void setTipPregleda(String tipPregleda) {
        this.tipPregleda = tipPregleda;
    }

    public String getDatumUstvarjen() {
        return datumUstvarjen;
    }

    public void setDatumUstvarjen(String datumUstvarjen) {
        this.datumUstvarjen = datumUstvarjen;
    }

    public String getDatumZakljucen() {
        return datumZakljucen;
    }

    public void setDatumZakljucen(String datumZakljucen) {
        this.datumZakljucen = datumZakljucen;
    }

    @Override
    public String toString() {
        return "PreventivniPregled{" +
                "id=" + id +
                ", naziv='" + naziv + '\'' +
                ", datum='" + datum + '\'' +
                ", status='" + status + '\'' +
                ", linijaSAP='" + linijaSAP + '\'' +
                ", nazivLinije='" + nazivLinije + '\'' +
                ", lokacija='" + lokacija + '\'' +
                ", prostor='" + prostor + '\'' +
                ", progress=" + progress +
                '}';
    }

    @Override
    public Map<String, String> getDisplayFields() {
        return Collections.emptyMap();
    }
}