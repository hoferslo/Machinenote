package com.example.machinenote.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("role_id")
    private int roleId;

    @SerializedName("apiKey")
    private String apiKey;

    // Role object for joined data (optional)
    @SerializedName("role")
    private Role role;

    @SerializedName("lokacija_id")
    private int lokacijaId;

    @SerializedName("lokacija")
    private String lokacija;

    // Constructors
    public User() {}

    public User(String username, String password, int roleId, String apiKey, int lokacijaId) {
        this.username = username;
        this.password = password;
        this.roleId = roleId;
        this.apiKey = apiKey;
        this.lokacijaId = lokacijaId;
    }

    public User(String username, String password, int roleId, String apiKey, Role role, int lokacijaId, String lokacija) {
        this.username = username;
        this.password = password;
        this.roleId = roleId;
        this.apiKey = apiKey;
        this.role = role;
        this.lokacijaId = lokacijaId;
        this.lokacija = lokacija;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setLokacijaId(int lokacijaId) { this.lokacijaId = lokacijaId; }

    public int getLokacijaId() { return lokacijaId; }

    public String getLokacija(){return lokacija;}

    public void setLokacija(String lokacija){this.lokacija = lokacija;}

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", roleId=" + roleId +
                ", apiKey='" + apiKey + '\'' +
                ", role=" + role +
                ", lokacijaId=" + lokacijaId +
                ", lokacija='" + lokacija + '\'' +
                '}';
    }
}