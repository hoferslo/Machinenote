package com.example.machinenote.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {
    private static final String THEME_PREFS = "theme_prefs";
    private static final String THEME_KEY = "selected_theme";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);
    }

    public static void saveTheme(Context context, int theme) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(THEME_KEY, theme);
        editor.apply();
    }

    public static int getSavedTheme(Context context) {
        return getPreferences(context).getInt(THEME_KEY, THEME_LIGHT); // TODO: Changed from THEME_LIGHT to THEME_SYSTEM
    }

    public static void applyTheme(int theme) {
        switch (theme) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static String getThemeName(Context context, int theme) {
        switch (theme) {
            case THEME_LIGHT:
                return "Svetla tema";
            case THEME_DARK:
                return "Temna tema";
            case THEME_SYSTEM:
            default:
                return "Sistemska tema";
        }
    }
}