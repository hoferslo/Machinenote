package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Kemikalija {
    @SerializedName("ID")
    private int id;

    @SerializedName("ime_SLO")
    private String ime_SLO;

    @SerializedName("ime_ENG")
    private String ime_ENG;

    @SerializedName("Formula")
    private String formula;

    @SerializedName("CAS_stevilo")
    private String cas_stevilo;

    @SerializedName("Firma")
    private String firma;

    @SerializedName("Rok_uporabe")
    private Date rok_uporabe;

    @SerializedName("Teza")
    private String teza;

    @SerializedName("Agregatno_stanje")
    private String agregatno_stanje;

    @SerializedName("Ident_Unichem")
    private int ident_Unichem;

    @SerializedName("Opombe")
    private String opombe;

    @SerializedName("Polica_ID")
    private int polica_ID;

    @SerializedName("Polica")
    private String polica;

    @SerializedName("Omara_ime")
    private String omara;

    @SerializedName("Ime_programa")
    private String program;


    public Kemikalija() {
    }

    public Kemikalija(int id, String ime_SLO, String ime_ENG, String formula, String cas_stevilo,
                      String firma, Date rok_uporabe, String teza, String agregatno_stanje,
                      int ident_Unichem, String opombe, int polica_ID, String polica, String omara,
                      String program) {
        this.id = id;
        this.ime_SLO = ime_SLO;
        this.ime_ENG = ime_ENG;
        this.formula = formula;
        this.cas_stevilo = cas_stevilo;
        this.firma = firma;
        this.rok_uporabe = rok_uporabe;
        this.teza = teza;
        this.agregatno_stanje = agregatno_stanje;
        this.ident_Unichem = ident_Unichem;
        this.opombe = opombe;
        this.polica_ID = polica_ID;
        this.polica = polica;
        this.omara = omara;
        this.program = program;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIme_SLO() {
        return ime_SLO;
    }

    public void setIme_SLO(String ime_SLO) {
        this.ime_SLO = ime_SLO;
    }

    public String getIme_ENG() {
        return ime_ENG;
    }

    public void setIme_ENG(String ime_ENG) {
        this.ime_ENG = ime_ENG;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getCas_stevilo() {
        return cas_stevilo;
    }

    public void setCas_stevilo(String cas_stevilo) {
        this.cas_stevilo = cas_stevilo;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }

    public Date getRok_uporabe() {
        return rok_uporabe;
    }

    public void setRok_uporabe(Date rok_uporabe) {
        this.rok_uporabe = rok_uporabe;
    }

    public String getTeza() {
        return teza;
    }

    public void setTeza(String teza) {
        this.teza = teza;
    }

    public String getAgregatno_stanje() {
        return agregatno_stanje;
    }

    public void setAgregatno_stanje(String agregatno_stanje) {
        this.agregatno_stanje = agregatno_stanje;
    }

    public int getIdent_Unichem() {
        return ident_Unichem;
    }

    public void setIdent_Unichem(int ident_Unichem) {
        this.ident_Unichem = ident_Unichem;
    }

    public String getOpombe() {
        return opombe;
    }

    public void setOpombe(String opombe) {
        this.opombe = opombe;
    }

    public int getPolica_ID() {
        return polica_ID;
    }

    public void setPolica_ID(int polica_ID) {
        this.polica_ID = polica_ID;
    }

    public String getPolica() {
        return polica;
    }

    public void setPolica(String polica) {
        this.polica = polica;
    }

    public String getOmara() {
        return omara;
    }

    public void setOmara(String omara) {
        this.omara = omara;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }
}