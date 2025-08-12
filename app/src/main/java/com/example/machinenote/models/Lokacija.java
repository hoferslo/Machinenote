package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class Lokacija implements DisplayableItem, Serializable {

    // Dodajte serialVersionUID za boljšo kompatibilnost
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private int id;

    @SerializedName("naziv")
    private String naziv;

    @SerializedName("aktivna")
    private String aktivna;

    // Constructor
    public Lokacija(int id, String naziv, String aktivna) {
        this.id = id;
        this.naziv = naziv;
        this.aktivna = aktivna;
    }

    // Default constructor
    public Lokacija() {
    }

    // Getter and Setter for id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and Setter for naziv
    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    // Getter and Setter for aktivna
    public String getAktivna() {
        return aktivna;
    }

    public void setAktivna(String aktivna) {
        this.aktivna = aktivna;
    }

    // Helper method to check if location is active
    public boolean isAktivna() {
        return "TRUE".equalsIgnoreCase(aktivna);
    }

    @Override
    public String toString() {
        return "Lokacija{" +
                "id=" + id +
                ", naziv='" + naziv + '\'' +
                ", aktivna='" + aktivna + '\'' +
                '}';
    }

    @Override
    public Map<String, String> getDisplayFields() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("ID", String.valueOf(getId()));
        map.put("Naziv", getNaziv());
        map.put("Aktivna", getAktivna());
        return map;
    }
}