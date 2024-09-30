package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

public class Naloga {

    @SerializedName("id")
    private int id;

    @SerializedName("vzdrzevalec")
    private String vzdrzevalec;

    @SerializedName("naloga")
    private String naloga;

    @SerializedName("opis")
    private String opis;

    @SerializedName("rok_za_izvedbo")
    private String rokZaIzvedbo;

    @SerializedName("izvedeno")
    private String izvedeno;

    @SerializedName("komentar")
    private String komentar;

    @SerializedName("izvedeno_bool")
    private int izvedenoBool;

    @SerializedName("slike")
    private String slike;

    @SerializedName("slike_pred_izpolnitvijo_naloge")
    private String slikePredIzpolnitvijoNaloge;

    // Constructor
    public Naloga(int id, String vzdrzevalec, String naloga, String opis, String rokZaIzvedbo,
                  String izvedeno, String komentar, int izvedenoBool, String slike, String slikePredIzpolnitvijoNaloge) {
        this.id = id;
        this.vzdrzevalec = vzdrzevalec;
        this.naloga = naloga;
        this.opis = opis;
        this.rokZaIzvedbo = rokZaIzvedbo;
        this.izvedeno = izvedeno;
        this.komentar = komentar;
        this.izvedenoBool = izvedenoBool;
        this.slike = slike;
        this.slikePredIzpolnitvijoNaloge = slikePredIzpolnitvijoNaloge;
    }

    // Getter and Setter for id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and Setter for vzdrzevalec
    public String getVzdrzevalec() {
        return vzdrzevalec;
    }

    public void setVzdrzevalec(String vzdrzevalec) {
        this.vzdrzevalec = vzdrzevalec;
    }

    // Getter and Setter for naloga
    public String getNaloga() {
        return naloga;
    }

    public void setNaloga(String naloga) {
        this.naloga = naloga;
    }

    // Getter and Setter for opis
    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    // Getter and Setter for rokZaIzvedbo
    public String getRokZaIzvedbo() {
        return rokZaIzvedbo;
    }

    public void setRokZaIzvedbo(String rokZaIzvedbo) {
        this.rokZaIzvedbo = rokZaIzvedbo;
    }

    // Getter and Setter for izvedeno
    public String getIzvedeno() {
        return izvedeno;
    }

    public void setIzvedeno(String izvedeno) {
        this.izvedeno = izvedeno;
    }

    // Getter and Setter for komentar
    public String getKomentar() {
        return komentar;
    }

    public void setKomentar(String komentar) {
        this.komentar = komentar;
    }

    // Getter and Setter for izvedenoBool
    public int getIzvedenoBool() {
        return izvedenoBool;
    }

    public void setIzvedenoBool(int izvedenoBool) {
        this.izvedenoBool = izvedenoBool;
    }

    // Getter and Setter for slike
    public String getSlike() {
        return slike;
    }

    public void setSlike(String slike) {
        this.slike = slike;
    }

    // Getter and Setter for slikePredIzpolnitvijoNaloge
    public String[] getSlikePredIzpolnitvijoNaloge() {
        if (slikePredIzpolnitvijoNaloge != null && !slikePredIzpolnitvijoNaloge.isEmpty()) {
            return slikePredIzpolnitvijoNaloge.split(",");
        }
        return new String[]{};
    }

    public void setSlikePredIzpolnitvijoNaloge(String slikePredIzpolnitvijoNaloge) {
        this.slikePredIzpolnitvijoNaloge = slikePredIzpolnitvijoNaloge;
    }

    @Override
    public String toString() {
        return "Naloga{" +
                "id=" + id +
                ", vzdrzevalec='" + vzdrzevalec + '\'' +
                ", naloga='" + naloga + '\'' +
                ", opis='" + opis + '\'' +
                ", rokZaIzvedbo='" + rokZaIzvedbo + '\'' +
                ", izvedeno='" + izvedeno + '\'' +
                ", komentar='" + komentar + '\'' +
                ", izvedenoBool=" + izvedenoBool +
                ", slike='" + slike + '\'' +
                ", slikePredIzpolnitvijoNaloge='" + slikePredIzpolnitvijoNaloge + '\'' +
                '}';
    }
}
