package com.example.machinenote.models;


public class RezervniDel {
    private int id;
    private int skladišče;
    private String regal;
    private String artikel;
    private int znaki;
    private String artikel_dolgi_text;
    private String proizvajalec;
    private String dobavitelj;
    private double znesek;
    private int minimalna_zaloga;
    private int dobava;
    private int poraba;
    private int inventura;

    // Default constructor
    public RezervniDel() {
    }

    // Parameterized constructor
    public RezervniDel(int ID, int skladišče, String regal, String artikel, int znaki, String artikelDolgiText,
                       String proizvajalec, String dobavitelj, double znesek, int minimalnaZaloga,
                       int dobava, int poraba, int inventura) {
        this.id = ID;
        this.skladišče = skladišče;
        this.regal = regal;
        this.artikel = artikel;
        this.znaki = znaki;
        this.artikel_dolgi_text = artikelDolgiText;
        this.proizvajalec = proizvajalec;
        this.dobavitelj = dobavitelj;
        this.znesek = znesek;
        this.minimalna_zaloga = minimalnaZaloga;
        this.dobava = dobava;
        this.poraba = poraba;
        this.inventura = inventura;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSkladišče() {
        return skladišče;
    }

    public void setSkladišče(int skladišče) {
        this.skladišče = skladišče;
    }

    public String getRegal() {
        return regal;
    }

    public void setRegal(String regal) {
        this.regal = regal;
    }

    public String getArtikel() {
        return artikel;
    }

    public void setArtikel(String artikel) {
        this.artikel = artikel;
    }

    public int getZnaki() {
        return znaki;
    }

    public void setZnaki(int znaki) {
        this.znaki = znaki;
    }

    public String getArtikel_dolgi_text() {
        return artikel_dolgi_text;
    }

    public void setArtikel_dolgi_text(String artikel_dolgi_text) {
        this.artikel_dolgi_text = artikel_dolgi_text;
    }

    public String getProizvajalec() {
        return proizvajalec;
    }

    public void setProizvajalec(String proizvajalec) {
        this.proizvajalec = proizvajalec;
    }

    public String getDobavitelj() {
        return dobavitelj;
    }

    public void setDobavitelj(String dobavitelj) {
        this.dobavitelj = dobavitelj;
    }

    public double getZnesek() {
        return znesek;
    }

    public void setZnesek(double znesek) {
        this.znesek = znesek;
    }

    public int getMinimalna_zaloga() {
        return minimalna_zaloga;
    }

    public void setMinimalna_zaloga(int minimalna_zaloga) {
        this.minimalna_zaloga = minimalna_zaloga;
    }

    public int getDobava() {
        return dobava;
    }

    public void setDobava(int dobava) {
        this.dobava = dobava;
    }

    public int getPoraba() {
        return poraba;
    }

    public void setPoraba(int poraba) {
        this.poraba = poraba;
    }

    public int getRealZalogo() {
        return dobava - poraba;
    }

    public int getInventura() {
        return inventura;
    }

    public void setInventura(int inventura) {
        this.inventura = inventura;
    }
}
