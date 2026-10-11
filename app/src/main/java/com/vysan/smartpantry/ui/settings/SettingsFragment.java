package com.vysan.smartpantry.ui.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.vysan.smartpantry.BuildConfig;
import com.vysan.smartpantry.R;
import com.vysan.smartpantry.data.repo.PantryRepository;

/**
 * Settings tab: expiring-soon alerts and window, theme, pantry reset and an About card.
 * Choices are saved in SharedPreferences through {@link Prefs} as soon as they change.
 */
public class SettingsFragment extends Fragment {

    private static final int WINDOW_SHORT = 2;
    private static final int WINDOW_MEDIUM = 3;
    private static final int WINDOW_LONG = 5;

    private Prefs prefs;

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = new Prefs(requireContext());
        setupAlerts(view);
        setupTheme(view);
        view.findViewById(R.id.reset_button).setOnClickListener(v -> confirmReset());
        ((TextView) view.findViewById(R.id.about_version))
                .setText(getString(R.string.settings_version, BuildConfig.VERSION_NAME));
    }

    private void setupAlerts(View view) {
        MaterialSwitch alerts = view.findViewById(R.id.switch_alerts);
        MaterialButtonToggleGroup window = view.findViewById(R.id.window_group);
        alerts.setChecked(prefs.isExpiryAlertsEnabled());
        window.check(windowButton(prefs.getSoonWindowDays()));
        setWindowEnabled(window, alerts.isChecked());

        // Listeners are added after the initial state so restoring it does not count as a change.
        alerts.setOnCheckedChangeListener((button, checked) -> {
            prefs.setExpiryAlertsEnabled(checked);
            setWindowEnabled(window, checked);
        });
        window.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) prefs.setSoonWindowDays(windowDays(checkedId));
        });
    }

    private void setupTheme(View view) {
        MaterialButtonToggleGroup theme = view.findViewById(R.id.theme_group);
        theme.check(themeButton(prefs.getThemeMode()));
        theme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            int mode = checkedId == R.id.theme_light ? Prefs.THEME_LIGHT
                    : checkedId == R.id.theme_dark ? Prefs.THEME_DARK : Prefs.THEME_SYSTEM;
            if (mode == prefs.getThemeMode()) return;
            prefs.setThemeMode(mode);
            Prefs.applyTheme(mode);   // recreates the activity; this tab is restored afterwards
        });
    }

    private void confirmReset() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_reset_title)
                .setMessage(R.string.dialog_reset_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.settings_reset_action, (dialog, which) -> {
                    PantryRepository.getInstance(requireContext()).deleteAll();
                    Snackbar.make(requireView(), R.string.snackbar_reset, Snackbar.LENGTH_SHORT).show();
                })
                .show();
    }

    private static void setWindowEnabled(MaterialButtonToggleGroup group, boolean enabled) {
        for (int i = 0; i < group.getChildCount(); i++) group.getChildAt(i).setEnabled(enabled);
    }

    @IdRes
    private static int windowButton(int days) {
        if (days == WINDOW_SHORT) return R.id.window_2;
        if (days == WINDOW_LONG) return R.id.window_5;
        return R.id.window_3;
    }

    private static int windowDays(@IdRes int buttonId) {
        if (buttonId == R.id.window_2) return WINDOW_SHORT;
        if (buttonId == R.id.window_5) return WINDOW_LONG;
        return WINDOW_MEDIUM;
    }

    @IdRes
    private static int themeButton(int mode) {
        if (mode == Prefs.THEME_LIGHT) return R.id.theme_light;
        if (mode == Prefs.THEME_DARK) return R.id.theme_dark;
        return R.id.theme_system;
    }
}
