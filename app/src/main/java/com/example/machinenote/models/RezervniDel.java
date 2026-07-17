package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedHashMap;
import java.util.Map;

public class RezervniDel implements DisplayableItem {
    @SerializedName("ID")
    private int id;
    private int skladišče;
    private String regal;
    private String artikel;
    private int znaki;
    private String artikel_dolgi_text;
    private String proizvajalec;
    private String dobavitelj;
    private double znesek;
    private double minimalna_zaloga;
    private double dobava;
    private double poraba;
    private int inventura;
    private int enota_id;               // ← nested objekt
    private String enota_naziv;
    private int enota_tip;
    private String slikaUrl;


    // Default constructor
    public RezervniDel() {
    }

    // Parameterized constructor
    public RezervniDel(int ID, int skladišče, String regal, String artikel, int znaki, String artikelDolgiText,
                       String proizvajalec, String dobavitelj, double znesek, double minimalnaZaloga,
                       double dobava, double poraba, int inventura, int enota_id, String slikaUrl) {
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
        this.enota_id = enota_id;
        this.slikaUrl = slikaUrl;
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

    public double getMinimalna_zaloga() {
        return minimalna_zaloga;
    }

    public void setMinimalna_zaloga(double minimalna_zaloga) {
        this.minimalna_zaloga = minimalna_zaloga;
    }

    public double getDobava() {
        return dobava;
    }

    public void setDobava(double dobava) {
        this.dobava = dobava;
    }

    public double getPoraba() {
        return poraba;
    }

    public void setPoraba(double poraba) {
        this.poraba = poraba;
    }

    public double getRealZalogo() {
        return dobava - poraba;
    }

    public int getInventura() {
        return inventura;
    }

    public void setInventura(int inventura) {
        this.inventura = inventura;
    }

    public int getEnota() {
        return enota_id;
    }

    public void setEnota(int enota_id) {
        this.enota_id = enota_id;
    }

    public String getEnota_naziv() { return enota_naziv != null ? enota_naziv : ""; }
    public void setEnota_naziv(String enota_naziv) { this.enota_naziv = enota_naziv; }

    public int getEnota_tip() { return enota_tip; }
    public void setEnota_tip(int enota_tip) { this.enota_tip = enota_tip; }

    public boolean isEnotaDecimal() { return enota_tip == 1; }

    public String getEnotaNaziv() { return getEnota_naziv(); }

    public String getSlikaUrl() { return slikaUrl; }
    public void setSlikaUrl(String slikaUrl) { this.slikaUrl = slikaUrl; }

    @Override
    public Map<String, String> getDisplayFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", String.valueOf(id));
        fields.put("Skladišče", String.valueOf(skladišče));
        fields.put("Regal", regal);
        fields.put("Artikel", artikel);
        fields.put("Znaki", String.valueOf(znaki));
        fields.put("Dolgi Opis", artikel_dolgi_text);
        fields.put("Proizvajalec", proizvajalec);
        fields.put("Dobavitelj", dobavitelj);
        fields.put("Znesek", String.format("%.2f", znesek));
        fields.put("Minimalna Zaloga", String.format("%.3f", minimalna_zaloga) + " " + getEnotaNaziv());
        fields.put("Dobava", String.format("%.3f", dobava) + " " + getEnotaNaziv());
        fields.put("Poraba", String.format("%.3f", poraba) + " " + getEnotaNaziv());
        fields.put("Realna Zaloga", String.format("%.3f", getRealZalogo()) + " " + getEnotaNaziv());
        fields.put("Inventura", String.valueOf(inventura));
        fields.put("Enota", getEnotaNaziv());
        fields.put("Slika", slikaUrl);
        return fields;
    }
}