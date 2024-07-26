package com.example.machinenote.models;

public class Sifrant {

    private int sifran_id;
    private String naziv;

    // Constructor
    public Sifrant(int sifran_id, String naziv) {
        this.sifran_id = sifran_id;
        this.naziv = naziv;
    }

    // Getter for sifrantId
    public int getSifrantId() {
        return sifran_id;
    }

    // Setter for sifrantId
    public void setSifrantId(int sifrantId) {
        this.sifran_id = sifrantId;
    }

    // Getter for naziv
    public String getNaziv() {
        return naziv;
    }

    // Setter for naziv
    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    @Override
    public String toString() {
        return "Sifrant{" +
                "sifrantId=" + sifran_id +
                ", naziv='" + naziv + '\'' +
                '}';
    }
}
