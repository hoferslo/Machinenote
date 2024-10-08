package com.example.machinenote.Utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.machinenote.models.Role;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SharedPreferencesHelper {

    private static final String PREFERENCES_FILE = "com.example.machinenote.PREFERENCES";

    // Preference keys

    public static final String Username = "Username";
    public static final String Password = "Password";
    public static final String Token = "Token";
    public static final String Role = "Role";

    private static SharedPreferencesHelper instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

    private SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new GsonBuilder().create();
    }

    public static synchronized SharedPreferencesHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesHelper(context);
        }
        return instance;
    }

    // Method to save a string value
    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    // Method to get a string value
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // Method to save an integer value
    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    // Method to get an integer value
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    // Method to save a boolean value
    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    // Method to get a boolean value
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    // Method to save a Role object
    public void putRole(Role role) {
        String roleJson = gson.toJson(role);
        editor.putString(Role, roleJson);
        editor.apply();
    }

    // Method to get a Role object
    public Role getRole() {
        String roleJson = sharedPreferences.getString(Role, null);
        return gson.fromJson(roleJson, Role.class);
    }

    // Method to remove a specific key
    public void remove(String key) {
        editor.remove(key);
        editor.apply();
    }

    // Method to clear all preferences
    public void clear() {
        editor.clear();
        editor.apply();
    }
}
