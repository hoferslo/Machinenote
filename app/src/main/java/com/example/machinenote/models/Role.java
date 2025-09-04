package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

public class Role {
    @SerializedName("roleId")
    private int role_Id;

    @SerializedName("role")
    private String role;

    @SerializedName("knjizenje")
    private boolean knjizenje;

    @SerializedName("rezervniDeli")
    private boolean rezervniDeli;

    @SerializedName("preventivniPregledi")
    private boolean preventivniPregledi;

    @SerializedName("dodajanjeNalog")
    private boolean dodajanjeNalog;

    @SerializedName("imenik")
    private boolean imenik;

    @SerializedName("zastoji")
    private boolean zastoji;

    @SerializedName("naloge")
    private boolean naloge;

    @SerializedName("remonti")
    private boolean remonti;

    @SerializedName("orodja")
    private boolean orodja;

    @SerializedName("register")
    private boolean register;

    @SerializedName("narocila")
    private boolean narocila;

    @SerializedName("dodajanje_narocil")
    private boolean dodajanje_narocil;

    @SerializedName("upravljanje_narocil")
    private boolean upravljanje_narocil;

    // Updated constructor to include dodajanje_nalog
    public Role(int role_Id, String role, boolean knjizenje, boolean rezervniDeli, boolean imenik, boolean preventivniPregledi, boolean zastoji, boolean naloge, boolean dodajanjeNalog, boolean remonti, boolean orodja, boolean register, boolean narocila, boolean dodajanjeNarocil, boolean upravljanjeNarocil) {
        this.role_Id = role_Id;
        this.role = role;
        this.knjizenje = knjizenje;
        this.rezervniDeli = rezervniDeli;
        this.imenik = imenik;
        this.preventivniPregledi = preventivniPregledi;
        this.zastoji = zastoji;
        this.naloge = naloge;
        this.dodajanjeNalog = dodajanjeNalog;
        this.remonti = remonti;
        this.orodja = orodja;
        this.register = register;
        this.narocila = narocila;
        this.dodajanje_narocil = dodajanjeNarocil;
        this.upravljanje_narocil = upravljanjeNarocil;

    }

    // Getters and Setters
    public int getRoleId() {
        return role_Id;
    }

    public void setRoleId(int role_Id) {
        this.role_Id = role_Id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isKnjizenje() {
        return knjizenje;
    }

    public void setKnjizenje(boolean knjizenje) {
        this.knjizenje = knjizenje;
    }

    public boolean isRezervniDeli() {
        return rezervniDeli;
    }

    public void setRezervniDeli(boolean rezervniDeli) {
        this.rezervniDeli = rezervniDeli;
    }

    public boolean isImenik() {
        return imenik;
    }

    public void setImenik(boolean imenik) {
        this.imenik = imenik;
    }

    public boolean isPreventivniPregledi() {
        return preventivniPregledi;
    }

    public void setPreventivniPregledi(boolean preventivniPregledi) {
        this.preventivniPregledi = preventivniPregledi;
    }

    public boolean isZastoji() {
        return zastoji;
    }

    public void setZastoji(boolean zastoji) {
        this.zastoji = zastoji;
    }

    public boolean isNaloge() {
        return naloge;
    }

    public void setNaloge(boolean naloge) {
        this.naloge = naloge;
    }

    public boolean isDodajanjeNalog() {
        return dodajanjeNalog;
    }

    public void setDodajanjeNalog(boolean dodajanjeNalog) {
        this.dodajanjeNalog = dodajanjeNalog;
    }

    public boolean isRemonti() {
        return remonti;
    }

    public void setRemonti(boolean remonti) {
        this.remonti = remonti;
    }

    public boolean isOrodja() {
        return orodja;
    }

    public void setOrodja(boolean orodja) {
        this.orodja = orodja;
    }

    public boolean isRegister() {
        return register;
    }

    public void setRegister(boolean register) {
        this.register = register;
    }

    public boolean isDodajanjeNarocil() {
        return dodajanje_narocil;
    }

    public void setDodajanjeNarocil(boolean dodajanjeNarocil) {
        this.dodajanje_narocil = dodajanjeNarocil;
    }

    public boolean isUpravljanjeNarocil() {
        return upravljanje_narocil;
    }

    public void setUpravljanjeNarocil(boolean upravljanjeNarocil) {
        this.upravljanje_narocil = upravljanjeNarocil;
    }

    public boolean isNarocila() {
        return narocila;
    }

    public void setNarocila(boolean narocila) {
        this.narocila = narocila;
    }

    @Override
    public String toString() {
        return "Role{" +
                "role_Id=" + role_Id +
                ", role='" + role + '\'' +
                ", knjizenje=" + knjizenje +
                ", rezervniDeli=" + rezervniDeli +
                ", imenik=" + imenik +
                ", preventivniPregledi=" + preventivniPregledi +
                ", zastoji=" + zastoji +
                ", naloge=" + naloge +
                ", dodajanjeNalog=" + dodajanjeNalog +
                ", remonti=" + remonti +
                ", orodja=" + orodja +
                ", register=" + register +
                ", narocila=" + narocila +
                ", dodajanje_narocil=" + dodajanje_narocil +
                ", upravljanje_narocil=" + upravljanje_narocil +
                '}';
    }
}
