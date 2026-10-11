package com.vysan.smartpantry;

import android.app.Application;

import com.vysan.smartpantry.ui.settings.Prefs;

/**
 * Applies the saved theme before any screen is drawn, so the app never flashes the wrong theme.
 */
public class SmartPantryApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Prefs.applyTheme(new Prefs(this).getThemeMode());
    }
}
