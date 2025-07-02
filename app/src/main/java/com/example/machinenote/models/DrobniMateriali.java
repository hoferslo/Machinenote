package com.example.machinenote.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class DrobniMateriali implements DisplayableItem {
    private int id;
    private String material;
    private String vrsta;
    private String velikost;
    private String kvaliteta;
    private String skladišče;
    private String regal;

    // Default constructor
    public DrobniMateriali() {
    }

    // Parameterized constructor
    public DrobniMateriali(int id, String material, String vrsta, String velikost,
                           String kvaliteta, String skladišče, String regal) {
        this.id = id;
        this.material = material;
        this.vrsta = vrsta;
        this.velikost = velikost;
        this.kvaliteta = kvaliteta;
        this.skladišče = skladišče;
        this.regal = regal;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getVrsta() {
        return vrsta;
    }

    public void setVrsta(String vrsta) {
        this.vrsta = vrsta;
    }

    public String getVelikost() {
        return velikost;
    }

    public void setVelikost(String velikost) {
        this.velikost = velikost;
    }

    public String getKvaliteta() {
        return kvaliteta;
    }

    public void setKvaliteta(String kvaliteta) {
        this.kvaliteta = kvaliteta;
    }

    public String getSkladišče() {
        return skladišče;
    }

    public void setSkladišče(String skladišče) {
        this.skladišče = skladišče;
    }

    public String getRegal() {
        return regal;
    }

    public void setRegal(String regal) {
        this.regal = regal;
    }

    @Override
    public Map<String, String> getDisplayFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", String.valueOf(id));
        fields.put("Material", material);
        fields.put("Vrsta", vrsta);
        fields.put("Velikost", velikost);
        fields.put("Kvaliteta", kvaliteta != null ? kvaliteta : "");
        fields.put("Skladišče", skladišče);
        fields.put("Regal", regal);
        return fields;
    }
}
