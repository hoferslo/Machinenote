package com.example.machinenote.models;

public class Remont {

    private int id;
    private String vzdrzevalec;
    private String linija;
    private String sklop_linije;
    private String opomba;
    private String zamenjani_deli;
    private String zacetek;
    private String konec;
    private String SAP_linije;
    private String slike;

    // Constructor
    public Remont(int id, String vzdrzevalec, String linija, String sklop_linije, String opomba,
                  String zamenjani_deli, String zacetek, String konec,
                  String SAP_linije, String slike) {
        this.id = id;
        this.vzdrzevalec = vzdrzevalec;
        this.linija = linija;
        this.sklop_linije = sklop_linije;
        this.opomba = opomba;
        this.zamenjani_deli = zamenjani_deli;
        this.zacetek = zacetek;
        this.konec = konec;
        this.SAP_linije = SAP_linije;
        this.slike = slike;
    }

    // Getter for id
    public int getId() {
        return id;
    }

    // Setter for id
    public void setId(int id) {
        this.id = id;
    }

    // Getter for vzdrzevalec
    public String getVzdrzevalec() {
        return vzdrzevalec;
    }

    // Setter for vzdrzevalec
    public void setVzdrzevalec(String vzdrzevalec) {
        this.vzdrzevalec = vzdrzevalec;
    }

    // Getter for linija
    public String getLinija() {
        return linija;
    }

    // Setter for linija
    public void setLinija(String linija) {
        this.linija = linija;
    }

    // Getter for sklopLinije
    public String getSklopLinije() {
        return sklop_linije;
    }

    // Setter for sklopLinije
    public void setSklopLinije(String sklopLinije) {
        this.sklop_linije = sklopLinije;
    }

    // Getter for opomba
    public String getOpomba() {
        return opomba;
    }

    // Setter for opomba
    public void setOpomba(String opomba) {
        this.opomba = opomba;
    }

    // Getter for zamenjaniDeli
    public String getZamenjaniDeli() {
        return zamenjani_deli;
    }

    // Setter for zamenjaniDeli
    public void setZamenjaniDeli(String zamenjaniDeli) {
        this.zamenjani_deli = zamenjaniDeli;
    }

    // Getter for zacetek
    public String getZacetek() {
        return zacetek;
    }

    // Setter for zacetek
    public void setZacetek(String zacetek) {
        this.zacetek = zacetek;
    }

    // Getter for konec
    public String getKonec() {
        return konec;
    }

    // Setter for konec
    public void setKonec(String konec) {
        this.konec = konec;
    }

    // Getter for sapLinije
    public String getSapLinije() {
        return SAP_linije;
    }

    // Setter for sapLinije
    public void setSapLinije(String SAP_linije) {
        this.SAP_linije = SAP_linije;
    }

    // Getter for slike
    public String getSlike() {
        return slike;
    }

    // Setter for slike
    public void setSlike(String slike) {
        this.slike = slike;
    }

    @Override
    public String toString() {
        return "Remont{" +
                "id=" + id +
                ", vzdrzevalec='" + vzdrzevalec + '\'' +
                ", linija='" + linija + '\'' +
                ", sklopLinije='" + sklop_linije + '\'' +
                ", opomba='" + opomba + '\'' +
                ", zamenjaniDeli='" + zamenjani_deli + '\'' +
                ", zacetek=" + zacetek +
                ", konec=" + konec +
                ", sapLinije='" + SAP_linije + '\'' +
                ", slike='" + slike + '\'' +
                '}';
    }
}
