package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

public class DrobniMateriali implements DisplayableItem {
    @SerializedName("ID")
    private int ID;

    @SerializedName("Material")
    private String Material;

    @SerializedName("Vrsta")
    private String Vrsta;

    @SerializedName("Velikost")
    private String Velikost;

    @SerializedName("Kvaliteta")
    private String Kvaliteta;

    @SerializedName("Skladisce")
    private int Skladisce;

    @SerializedName("Regal")
    private String Regal;

    // Konstruktor
    public DrobniMateriali() {}

    // Getter metode
    public int getID() { return ID; }
    public String getMaterial() { return Material; }
    public String getVrsta() { return Vrsta; }
    public String getVelikost() { return Velikost; }
    public String getKvaliteta() { return Kvaliteta; }
    public int getSkladišče() { return Skladisce; }
    public String getRegal() { return Regal; }

    // Setter metode
    public void setID(int ID) { this.ID = ID; }
    public void setMaterial(String material) { this.Material = material; }
    public void setVrsta(String vrsta) { this.Vrsta = vrsta; }
    public void setVelikost(String velikost) { this.Velikost = velikost; }
    public void setKvaliteta(String kvaliteta) { this.Kvaliteta = kvaliteta; }
    public void setSkladišče(int skladisce) { this.Skladisce = skladisce; }
    public void setRegal(String regal) { this.Regal = regal; }

    @Override
    public Map<String, String> getDisplayFields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("ID", String.valueOf(ID));
        fields.put("Material", Material);
        fields.put("Vrsta", Vrsta);
        fields.put("Velikost", Velikost);
        if (Kvaliteta != null && !Kvaliteta.isEmpty()) {
            fields.put("Kvaliteta", Kvaliteta);
        }
        fields.put("Skladišče", String.valueOf(Skladisce));
        fields.put("Regal", Regal);
        return fields;
    }
}