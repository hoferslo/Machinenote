package com.example.machinenote.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SklopLinije {

    private int id;
    private int linija_id;           // This is the line ID (integer foreign key)
    private String sklop_linije;     // This is the assembly name
    private boolean aktivna;         // This is the active status (boolean)

    // Constructor
    public SklopLinije(int id, int linijaId, String sklopLinije, boolean aktivna) {
        this.id = id;
        this.linija_id = linijaId;
        this.sklop_linije = sklopLinije;
        this.aktivna = aktivna;
    }

    // Getter for id
    public int getId() {
        return id;
    }

    // Setter for id
    public void setId(int id) {
        this.id = id;
    }

    // Getter for line ID (foreign key)
    public int getLinijaId() {
        return linija_id;
    }

    // Setter for line ID
    public void setLinijaId(int linijaId) {
        this.linija_id = linijaId;
    }

    // Getter for assembly name (what gets displayed)
    public String getSklopLinije() {
        return sklop_linije;
    }

    // Setter for assembly name
    public void setSklopLinije(String sklopLinije) {
        this.sklop_linije = sklopLinije;
    }

    // Getter for active status
    public boolean isAktivna() {
        return aktivna;
    }

    // Setter for active status
    public void setAktivna(boolean aktivna) {
        this.aktivna = aktivna;
    }

    // Legacy getter methods (for compatibility if used elsewhere)
    @Deprecated
    public String getIdLinije() {
        return String.valueOf(linija_id);
    }

    @Deprecated
    public String getSklopLinijeAktiven() {
        return String.valueOf(aktivna);
    }

    @Override
    public String toString() {
        return "SklopLinije{" +
                "id=" + id +
                ", linijaId=" + linija_id +
                ", sklopLinije='" + sklop_linije + '\'' +
                ", aktivna=" + aktivna +
                '}';
    }

    public static List<SklopLinije> getSklopLinijBasedOnLine(Linija linija, List<SklopLinije> sklopLinij) {
        List<SklopLinije> newSklopLinij = new ArrayList<>();

        if (linija == null || sklopLinij == null) {
            return newSklopLinij;
        }
        int targetLinija = linija.getLinija_id();
        if (targetLinija == 0 ) {
            return newSklopLinij;
        }

        for (SklopLinije item : sklopLinij) {
            if (item != null && item.getLinijaId() == targetLinija && item.isAktivna()) {
                newSklopLinij.add(item);
            }
        }

        return newSklopLinij;
    }
}