package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

public class Imenik {

    @SerializedName("id")
    private int id;

    @SerializedName("naziv_podjetja")
    private String nazivPodjetja;

    @SerializedName("kontaktna_oseba")
    private String kontaktnaOseba;

    @SerializedName("telefon")
    private String telefon;

    @SerializedName("gsm")
    private String gsm;

    @SerializedName("mail")
    private String mail;

    // Default constructor
    public Imenik() {
    }

    // Parameterized constructor
    public Imenik(int id, String nazivPodjetja, String kontaktnaOseba, String telefon, String gsm, String mail) {
        this.id = id;
        this.nazivPodjetja = nazivPodjetja;
        this.kontaktnaOseba = kontaktnaOseba;
        this.telefon = telefon;
        this.gsm = gsm;
        this.mail = mail;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNazivPodjetja() {
        return nazivPodjetja;
    }

    public void setNazivPodjetja(String nazivPodjetja) {
        this.nazivPodjetja = nazivPodjetja;
    }

    public String getKontaktnaOseba() {
        return kontaktnaOseba;
    }

    public void setKontaktnaOseba(String kontaktnaOseba) {
        this.kontaktnaOseba = kontaktnaOseba;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getGsm() {
        return gsm;
    }

    public void setGsm(String gsm) {
        this.gsm = gsm;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @Override
    public String toString() {
        return "Imenik{" +
                "id=" + id +
                ", nazivPodjetja='" + nazivPodjetja + '\'' +
                ", kontaktnaOseba='" + kontaktnaOseba + '\'' +
                ", telefon='" + telefon + '\'' +
                ", gsm='" + gsm + '\'' +
                ", mail='" + mail + '\'' +
                '}';
    }
}
