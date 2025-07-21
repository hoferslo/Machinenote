package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

public class PregledOpravilo implements DisplayableItem {

    @SerializedName("OpraviloID")
    private int id;

    @SerializedName("OpisOpravila")
    private String opisOpravila;

    @SerializedName("Sklop_Linije")
    private String sklopLinije;

    @SerializedName("Trajanje_STD_Min")
    private int trajanjeStdMin;

    @SerializedName("Lastnost")
    private String lastnost;

    @SerializedName("STD_Vrednost_Lastnosti")
    private String stdVrednostLastnosti;

    @SerializedName("Status")
    private String status;

    @SerializedName("Datum_Izvedbe")
    private String datumIzvedbe;

    @SerializedName("Dejansko_Trajanje_Min")
    private Integer dejanskoTrajanjeMin;

    @SerializedName("ActVredParameter")
    private String actVredParameter;

    @SerializedName("Opombe")
    private String opombe;

    @SerializedName("Vzdrzevalec")
    private String vzdrzevalec;

    // Constructors
    public PregledOpravilo() {}

    public PregledOpravilo(int id, String opisOpravila, String sklopLinije, String status) {
        this.id = id;
        this.opisOpravila = opisOpravila;
        this.sklopLinije = sklopLinije;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOpisOpravila() {
        return opisOpravila;
    }

    public void setOpisOpravila(String opisOpravila) {
        this.opisOpravila = opisOpravila;
    }

    public String getSklopLinije() {
        return sklopLinije;
    }

    public void setSklopLinije(String sklopLinije) {
        this.sklopLinije = sklopLinije;
    }

    public int getTrajanjeStdMin() {
        return trajanjeStdMin;
    }

    public void setTrajanjeStdMin(int trajanjeStdMin) {
        this.trajanjeStdMin = trajanjeStdMin;
    }

    public String getLastnost() {
        return lastnost;
    }

    public void setLastnost(String lastnost) {
        this.lastnost = lastnost;
    }

    public String getStdVrednostLastnosti() {
        return stdVrednostLastnosti;
    }

    public void setStdVrednostLastnosti(String stdVrednostLastnosti) {
        this.stdVrednostLastnosti = stdVrednostLastnosti;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDatumIzvedbe() {
        return datumIzvedbe;
    }

    public void setDatumIzvedbe(String datumIzvedbe) {
        this.datumIzvedbe = datumIzvedbe;
    }

    public Integer getDejanskoTrajanjeMin() {
        return dejanskoTrajanjeMin;
    }

    public void setDejanskoTrajanjeMin(Integer dejanskoTrajanjeMin) {
        this.dejanskoTrajanjeMin = dejanskoTrajanjeMin;
    }

    public String getActVredParameter() {
        return actVredParameter;
    }

    public void setActVredParameter(String actVredParameter) {
        this.actVredParameter = actVredParameter;
    }

    public String getOpombe() {
        return opombe;
    }

    public void setOpombe(String opombe) {
        this.opombe = opombe;
    }

    public String getVzdrzevalec() {
        return vzdrzevalec;
    }

    public void setVzdrzevalec(String vzdrzevalec) {
        this.vzdrzevalec = vzdrzevalec;
    }

    @Override
    public Map<String, String> getDisplayFields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("OpisOpravila", opisOpravila != null ? opisOpravila : "");
        fields.put("Status", getStatusWithIcon());
        fields.put("Sklop_Linije", sklopLinije != null ? sklopLinije : "");
        fields.put("Trajanje_STD_Min", trajanjeStdMin + " min");
        fields.put("ActVredParameter", actVredParameter != null ? actVredParameter : stdVrednostLastnosti);
        return fields;
    }

    private String getStatusWithIcon() {
        switch (status) {
            case "Ni zaceto":
                return "⏳ " + status;
            case "V teku":
                return "🔄 " + status;
            case "Zakljuceno":
                return "✅ " + status;
            case "Preskoceno":
                return "⏭️ " + status;
            default:
                return status;
        }
    }

    public boolean isCompleted() {
        return "Zakljuceno".equals(status);
    }

    public boolean isInProgress() {
        return "V teku".equals(status);
    }

    public boolean isNotStarted() {
        return "Ni zaceto".equals(status);
    }

    public boolean isSkipped() {
        return "Preskoceno".equals(status);
    }

    @Override
    public String toString() {
        return "PregledOpravilo{" +
                "id=" + id +
                ", opisOpravila='" + opisOpravila + '\'' +
                ", sklopLinije='" + sklopLinije + '\'' +
                ", status='" + status + '\'' +
                ", actVredParameter='" + actVredParameter + '\'' +
                '}';
    }
}