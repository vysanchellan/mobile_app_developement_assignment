package com.vysan.smartpantry.ui.main;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vysan.smartpantry.R;
import com.vysan.smartpantry.ui.pantry.PantryFragment;
import com.vysan.smartpantry.ui.recipes.SuggestedFragment;
import com.vysan.smartpantry.ui.settings.SettingsFragment;

/**
 * Hosts the bottom navigation and swaps between the Pantry, Recipes and Settings fragments.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            showFragment(item.getItemId());
            return true;
        });
        // After rotation the FragmentManager restores the current fragment by itself.
        if (savedInstanceState == null) showFragment(R.id.nav_pantry);
    }

    /** Switches tab programmatically, e.g. the "Go to pantry" button on the empty recipes screen. */
    public void selectTab(@IdRes int itemId) {
        bottomNav.setSelectedItemId(itemId);
    }

    private void showFragment(@IdRes int itemId) {
        Fragment fragment;
        if (itemId == R.id.nav_recipes) {
            fragment = new SuggestedFragment();
        } else if (itemId == R.id.nav_settings) {
            fragment = new SettingsFragment();
        } else {
            fragment = new PantryFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
