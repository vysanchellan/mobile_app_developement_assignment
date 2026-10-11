package com.vysan.smartpantry.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.vysan.smartpantry.R;

/**
 * Entry point of the app. Later hosts the bottom navigation and its three fragments.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
