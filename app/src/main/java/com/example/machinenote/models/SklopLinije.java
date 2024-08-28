package com.example.machinenote.models;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SklopLinije {

    private int id;
    private String id_linije;
    private String sklop_linije;
    private String sklop_linije_aktiven;

    // Constructor
    public SklopLinije(int id, String idLinije, String sklopLinije, String sklop_linije_aktiven) {
        this.id = id;
        this.id_linije = idLinije;
        this.sklop_linije = sklopLinije;
        this.sklop_linije_aktiven = sklop_linije_aktiven;
    }

    // Getter for id
    public int getId() {
        return id;
    }

    // Setter for id
    public void setId(int id) {
        this.id = id;
    }

    // Getter for idLinije
    public String getIdLinije() {
        return id_linije;
    }

    // Setter for idLinije
    public void setIdLinije(String idLinije) {
        this.id_linije = idLinije;
    }

    // Getter for sklopLinije
    public String getSklopLinije() {
        return sklop_linije;
    }

    // Setter for sklopLinije
    public void setSklopLinije(String sklopLinije) {
        this.sklop_linije = sklopLinije;
    }

    // Getter for sklopLinijeAktiven
    public String getSklopLinijeAktiven() {
        return sklop_linije_aktiven;
    }

    // Setter for sklopLinijeAktiven
    public void setSklopLinijeAktiven(String sklopLinijeAktiven) {
        this.sklop_linije_aktiven = sklopLinijeAktiven;
    }

    @Override
    public String toString() {
        return "SklopLinije{" +
                "id=" + id +
                ", idLinije='" + id_linije + '\'' +
                ", sklopLinije='" + sklop_linije + '\'' +
                ", sklopLinijeAktiven='" + sklop_linije_aktiven + '\'' +
                '}';
    }

    public static List<SklopLinije> getSklopLinijBasedOnLine(Linija linija, List<SklopLinije> sklopLinij) {
        List<SklopLinije> newSklopLinij = new ArrayList<>();
        for (SklopLinije item : sklopLinij) {
            // Check if the SAP_linije of the current SklopLinije matches the SAP_linije of the provided Linija
            Log.d("mhm", item.getIdLinije() + " : " + linija.getLinija_SAP());
            if (item.getIdLinije().equals(linija.getLinija_SAP())) {

                // Add to the new list if it matches
                newSklopLinij.add(item);
            }
        }

        return newSklopLinij;
    }
}
