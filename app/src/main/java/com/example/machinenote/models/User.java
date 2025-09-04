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

    // Constructors
    public User() {}

    public User(String username, String password, int roleId, String apiKey) {
        this.username = username;
        this.password = password;
        this.roleId = roleId;
        this.apiKey = apiKey;
    }

    public User(String username, String password, int roleId, String apiKey, Role role) {
        this.username = username;
        this.password = password;
        this.roleId = roleId;
        this.apiKey = apiKey;
        this.role = role;
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

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", roleId=" + roleId +
                ", apiKey='" + apiKey + '\'' +
                ", role=" + role +
                '}';
    }
}