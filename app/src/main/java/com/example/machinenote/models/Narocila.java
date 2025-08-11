package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class Narocila implements DisplayableItem, Serializable {

    // Dodajte serialVersionUID za boljšo kompatibilnost
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private int id;

    @SerializedName("lokacija")
    private String lokacija;

    @SerializedName("narocnik")
    private String narocnik;

    @SerializedName("naziv")
    private String naziv;

    @SerializedName("tehnicni_podatki")
    private String tehnicniPodatki;

    @SerializedName("kolicina")
    private String kolicina;

    @SerializedName("enota")
    private String enota;

    @SerializedName("slike")
    private String slike;

    @SerializedName("datum_vnosa")
    private String datumVnosa;

    @SerializedName("rok_za_dobavo")
    private String rokZaDobavo;

    @SerializedName("datum_potrjene_dobave")
    private String datumPotrjeneDobave;

    @SerializedName("status")
    private String status;;;


    public Narocila(int id, String lokacija, String narocnik, String naziv, String tehnicniPodatki,
                    String kolicina, String enota, String slike, String datumVnosa, String rokZaDobavo,
                    String datumPotrjeneDobave, String status) {
        this.id = id;
        this.lokacija = lokacija;
        this.narocnik = narocnik;
        this.naziv = naziv;
        this.tehnicniPodatki = tehnicniPodatki;
        this.kolicina = kolicina;
        this.enota = enota;
        this.slike = slike;
        this.datumVnosa = datumVnosa;
        this.rokZaDobavo = rokZaDobavo;
        this.datumPotrjeneDobave = datumPotrjeneDobave;
        this.status = status;
    }

    // Getter and Setter for id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and Setter for lokacija
    public String getLokacija() {
        return lokacija;
    }

    public void setLokacija(String lokacija) {
        this.lokacija = lokacija;
    }

    // Getter and Setter for narocnik
    public String getNarocnik() {
        return narocnik;
    }

    public void setNarocnik(String narocnik) {
        this.narocnik = narocnik;
    }

    // Getter and Setter for naziv
    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    // Getter and Setter for tehnicniPodatki
    public String getTehnicniPodatki() {
        return tehnicniPodatki;
    }

    public void setTehnicniPodatki(String tehnicniPodatki) {
        this.tehnicniPodatki = tehnicniPodatki;
    }

    // Getter and Setter for kolicina
    public String getKolicina() {
        return kolicina;
    }

    public void setKolicina(String kolicina) {
        this.kolicina = kolicina;
    }

    // Getter and Setter for enota
    public String getEnota() {
        return enota;
    }

    public void setEnota(String enota) {
        this.enota = enota;
    }

    // Getter and Setter for slike
    public String getSlike() {
        return slike;
    }

    public void setSlike(String slike) {
        this.slike = slike;
    }

    // Getter and Setter for datumVnosa
    public String getDatumVnosa() {
        return datumVnosa;
    }

    public void setDatumVnosa(String datumVnosa) {
        this.datumVnosa = datumVnosa;
    }

    // Getter and Setter for rokZaDobavo
    public String getRokZaDobavo() {
        return rokZaDobavo;
    }

    public void setRokZaDobavo(String rokZaDobavo) {
        this.rokZaDobavo = rokZaDobavo;
    }

    // Getter and Setter for datumPotrjeneDobave
    public String getDatumPotrjeneDobave() {
        return datumPotrjeneDobave;
    }

    public void setDatumPotrjeneDobave(String datumPotrjeneDobave) {
        this.datumPotrjeneDobave = datumPotrjeneDobave;
    }

    // Getter and Setter for status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getter for slike - splits by comma if multiple images
    public String[] getSlikeArray() {
        if (slike != null && !slike.isEmpty()) {
            return slike.split(",");
        }
        return new String[]{};
    }

    @Override
    public String toString() {
        return "Narocila{" +
                "id=" + id +
                ", lokacija='" + lokacija + '\'' +
                ", narocnik='" + narocnik + '\'' +
                ", naziv='" + naziv + '\'' +
                ", tehnicniPodatki='" + tehnicniPodatki + '\'' +
                ", kolicina='" + kolicina + '\'' +
                ", enota=" + enota +
                ", slike='" + slike + '\'' +
                ", datumVnosa='" + datumVnosa + '\'' +
                ", rokZaDobavo='" + rokZaDobavo + '\'' +
                ", datumPotrjeneDobave='" + datumPotrjeneDobave + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public Map<String, String> getDisplayFields() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Naziv", getNaziv());
        map.put("Naročnik", getNarocnik());
        map.put("Lokacija", getLokacija());
        map.put("Količina", getKolicina());
        map.put("Rok za dobavo", getRokZaDobavo());
        map.put("Status", getStatus());
        return map;
    }
}