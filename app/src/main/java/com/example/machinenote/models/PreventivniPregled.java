package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.Map;

public class PreventivniPregled implements DisplayableItem {

    @SerializedName("id")
    private int id;

    @SerializedName("opis")
    private String opis;

    @SerializedName("linija_sap")
    private String linijaSap;

    @SerializedName("trajanje_std_min")
    private int trajanjeStdMin;

    @SerializedName("frekvenca")
    private int frekvenca;

    @SerializedName("datum")
    private String datum;

    @SerializedName("lastnost")
    private String lastnost;

    @SerializedName("opombe")
    private String opombe;

    @SerializedName("std_vrednost")
    private String stdVrednost;

    @SerializedName("naziv_linije")
    private String nazivLinije;

    @SerializedName("prostor_naziv")
    private String prostorNaziv;

    @SerializedName("lokacija_naziv")
    private String lokacijaNaziv;

    @SerializedName("sklop_linije")
    private String sklopLinije;

    @SerializedName("naslednji_pregled")
    private String naslenjniPregled;

    @SerializedName("dni_zamude")
    private int dniZamude;

    // Constructors
    public PreventivniPregled() {}

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public String getLinijaSap() {
        return linijaSap;
    }

    public void setLinijaSap(String linijaSap) {
        this.linijaSap = linijaSap;
    }

    public int getTrajanjeStdMin() {
        return trajanjeStdMin;
    }

    public void setTrajanjeStdMin(int trajanjeStdMin) {
        this.trajanjeStdMin = trajanjeStdMin;
    }

    public int getFrekvenca() {
        return frekvenca;
    }

    public void setFrekvenca(int frekvenca) {
        this.frekvenca = frekvenca;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getLastnost() {
        return lastnost;
    }

    public void setLastnost(String lastnost) {
        this.lastnost = lastnost;
    }

    public String getOpombe() {
        return opombe;
    }

    public void setOpombe(String opombe) {
        this.opombe = opombe;
    }

    public String getStdVrednost() {
        return stdVrednost;
    }

    public void setStdVrednost(String stdVrednost) {
        this.stdVrednost = stdVrednost;
    }

    public String getNazivLinije() {
        return nazivLinije;
    }

    public void setNazivLinije(String nazivLinije) {
        this.nazivLinije = nazivLinije;
    }

    public String getProstorNaziv() {
        return prostorNaziv;
    }

    public void setProstorNaziv(String prostorNaziv) {
        this.prostorNaziv = prostorNaziv;
    }

    public String getLokacijaNaziv() {
        return lokacijaNaziv;
    }

    public void setLokacijaNaziv(String lokacijaNaziv) {
        this.lokacijaNaziv = lokacijaNaziv;
    }

    public String getSklopLinije() {
        return sklopLinije;
    }

    public void setSklopLinije(String sklopLinije) {
        this.sklopLinije = sklopLinije;
    }

    public String getNaslenjniPregled() {
        return naslenjniPregled;
    }

    public void setNaslenjniPregled(String naslenjniPregled) {
        this.naslenjniPregled = naslenjniPregled;
    }

    public int getDniZamude() {
        return dniZamude;
    }

    public void setDniZamude(int dniZamude) {
        this.dniZamude = dniZamude;
    }

    @Override
    public String toString() {
        return "PreventivniPregled{" +
                "id=" + id +
                ", opis='" + opis + '\'' +
                ", linijaSap='" + linijaSap + '\'' +
                ", datum='" + datum + '\'' +
                ", nazivLinije='" + nazivLinije + '\'' +
                ", dniZamude=" + dniZamude +
                '}';
    }

    @Override
    public Map<String, String> getDisplayFields() {
        return Collections.emptyMap();
    }

    // Helper metode
    public String getStatusText() {
        if (dniZamude > 0) {
            return "Zamuda: " + dniZamude + " dni";
        } else if (dniZamude == 0) {
            return "Danes";
        } else {
            return "Prihajajoč";
        }
    }

    public String getFullLocation() {
        StringBuilder location = new StringBuilder();
        if (lokacijaNaziv != null && !lokacijaNaziv.isEmpty()) {
            location.append(lokacijaNaziv);
        }
        if (prostorNaziv != null && !prostorNaziv.isEmpty()) {
            if (location.length() > 0) location.append(" - ");
            location.append(prostorNaziv);
        }
        return location.toString();
    }
}
