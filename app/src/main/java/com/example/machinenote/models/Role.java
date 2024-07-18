package com.example.machinenote.models;

public class Role {
    private int roleId;
    private String role;
    private boolean knjizenje;
    private boolean rezervniDeli;
    private boolean imenik;
    private boolean preventivniPregledi;
    private boolean zastoji;
    private boolean naloge;
    private boolean remonti;
    private boolean orodja;
    private boolean register;

    public Role(int roleId, String role, boolean knjizenje, boolean rezervniDeli, boolean imenik, boolean preventivniPregledi, boolean zastoji, boolean naloge, boolean remonti, boolean orodja, boolean register) {
        this.roleId = roleId;
        this.role = role;
        this.knjizenje = knjizenje;
        this.rezervniDeli = rezervniDeli;
        this.imenik = imenik;
        this.preventivniPregledi = preventivniPregledi;
        this.zastoji = zastoji;
        this.naloge = naloge;
        this.remonti = remonti;
        this.orodja = orodja;
        this.register = register;
    }

    // Getters and Setters
    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isKnjizenje() {
        return knjizenje;
    }

    public void setKnjizenje(boolean knjizenje) {
        this.knjizenje = knjizenje;
    }

    public boolean isRezervniDeli() {
        return rezervniDeli;
    }

    public void setRezervniDeli(boolean rezervniDeli) {
        this.rezervniDeli = rezervniDeli;
    }

    public boolean isImenik() {
        return imenik;
    }

    public void setImenik(boolean imenik) {
        this.imenik = imenik;
    }

    public boolean isPreventivniPregledi() {
        return preventivniPregledi;
    }

    public void setPreventivniPregledi(boolean preventivniPregledi) {
        this.preventivniPregledi = preventivniPregledi;
    }

    public boolean isZastoji() {
        return zastoji;
    }

    public void setZastoji(boolean zastoji) {
        this.zastoji = zastoji;
    }

    public boolean isNaloge() {
        return naloge;
    }

    public void setNaloge(boolean naloge) {
        this.naloge = naloge;
    }

    public boolean isRemonti() {
        return remonti;
    }

    public void setRemonti(boolean remonti) {
        this.remonti = remonti;
    }

    public boolean isOrodja() {
        return orodja;
    }

    public void setOrodja(boolean orodja) {
        this.orodja = orodja;
    }
    public boolean isRegister() {
        return register;
    }

    public void setRegister(boolean register) {
        this.register = register;
    }
}
