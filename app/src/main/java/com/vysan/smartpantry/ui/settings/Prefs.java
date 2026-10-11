package com.vysan.smartpantry.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Typed access to the user's settings, stored in SharedPreferences (small key-value data does not
 * belong in the SQLite database).
 */
public final class Prefs {

    private static final String FILE_NAME = "smart_pantry_prefs";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_SOON_WINDOW = "soon_window_days";
    private static final String KEY_THEME = "theme_mode";

    /** Days ahead that count as "expiring soon" until the user picks otherwise. */
    public static final int DEFAULT_SOON_WINDOW = 3;
    /** Passed to ExpiryHelper when alerts are off, so nothing is ever classed as "soon". */
    private static final int ALERTS_OFF_WINDOW = -1;

    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    private final SharedPreferences prefs;

    public Prefs(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public boolean isExpiryAlertsEnabled() {
        return prefs.getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public void setExpiryAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    public int getSoonWindowDays() {
        return prefs.getInt(KEY_SOON_WINDOW, DEFAULT_SOON_WINDOW);
    }

    public void setSoonWindowDays(int days) {
        prefs.edit().putInt(KEY_SOON_WINDOW, days).apply();
    }

    /** The window the pantry chips should use: the chosen days, or none when alerts are off. */
    public int getEffectiveSoonWindow() {
        return isExpiryAlertsEnabled() ? getSoonWindowDays() : ALERTS_OFF_WINDOW;
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME, THEME_SYSTEM);
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME, mode).apply();
    }

    /** Applies a theme choice app-wide; AppCompat recreates visible activities as needed. */
    public static void applyTheme(int mode) {
        if (mode == THEME_LIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (mode == THEME_DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
