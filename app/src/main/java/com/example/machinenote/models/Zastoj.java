package com.example.machinenote.models;

public class Zastoj {
    private int zastoji_id;
    private String vzdrzevalec;
    private String linija;
    private String sifrant;
    private String opomba;
    private String razlog;
    private String zacetek;
    private String konec;
    private String delavec;
    private String SAP_linije;
    private String slike;
    private String dezurstvo;
    private int trajanje_zastoja_v_minutah;
    private int leto;
    private int mesec;
    private String QR_koda_vnos;

    public Zastoj(String vzdrzevalec, String linija, String sifrant, String opomba,
                  String razlog, String zacetek, String konec, String delavec, String SAP_linije,
                  String slike, String dezurstvo, int trajanje_zastoja_v_minutah, int leto,
                  int mesec, String QR_koda_vnos) {
        this.vzdrzevalec = vzdrzevalec;
        this.linija = linija;
        this.sifrant = sifrant;
        this.opomba = opomba;
        this.razlog = razlog;
        this.zacetek = zacetek;
        this.konec = konec;
        this.delavec = delavec;
        this.SAP_linije = SAP_linije;
        this.slike = slike;
        this.dezurstvo = dezurstvo;
        this.trajanje_zastoja_v_minutah = trajanje_zastoja_v_minutah;
        this.leto = leto;
        this.mesec = mesec;
        this.QR_koda_vnos = QR_koda_vnos;
    }

    // Getters and Setters
    public int getZastoji_id() {
        return zastoji_id;
    }

    public void setZastoji_id(int zastoji_id) {
        this.zastoji_id = zastoji_id;
    }

    public String getVzdrzevalec() {
        return vzdrzevalec;
    }

    public void setVzdrzevalec(String vzdrzevalec) {
        this.vzdrzevalec = vzdrzevalec;
    }

    public String getLinija() {
        return linija;
    }

    public void setLinija(String linija) {
        this.linija = linija;
    }

    public String getSifrant() {
        return sifrant;
    }

    public void setSifrant(String sifrant) {
        this.sifrant = sifrant;
    }

    public String getOpomba() {
        return opomba;
    }

    public void setOpomba(String opomba) {
        this.opomba = opomba;
    }

    public String getRazlog() {
        return razlog;
    }

    public void setRazlog(String razlog) {
        this.razlog = razlog;
    }

    public String getZacetek() {
        return zacetek;
    }

    public void setZacetek(String zacetek) {
        this.zacetek = zacetek;
    }

    public String getKonec() {
        return konec;
    }

    public void setKonec(String konec) {
        this.konec = konec;
    }

    public String getDelavec() {
        return delavec;
    }

    public void setDelavec(String delavec) {
        this.delavec = delavec;
    }

    public String getSAP_linije() {
        return SAP_linije;
    }

    public void setSAP_linije(String SAP_linije) {
        this.SAP_linije = SAP_linije;
    }

    public String getSlike() {
        return slike;
    }

    public void setSlike(String slike) {
        this.slike = slike;
    }

    public String getDezurstvo() {
        return dezurstvo;
    }

    public void setDezurstvo(String dezurstvo) {
        this.dezurstvo = dezurstvo;
    }

    public int getTrajanje_zastoja_v_minutah() {
        return trajanje_zastoja_v_minutah;
    }

    public void setTrajanje_zastoja_v_minutah(int trajanje_zastoja_v_minutah) {
        this.trajanje_zastoja_v_minutah = trajanje_zastoja_v_minutah;
    }

    public int getLeto() {
        return leto;
    }

    public void setLeto(int leto) {
        this.leto = leto;
    }

    public int getMesec() {
        return mesec;
    }

    public void setMesec(int mesec) {
        this.mesec = mesec;
    }

    public String getQR_koda_vnos() {
        return QR_koda_vnos;
    }

    public void setQR_koda_vnos(String QR_koda_vnos) {
        this.QR_koda_vnos = QR_koda_vnos;
    }
}
