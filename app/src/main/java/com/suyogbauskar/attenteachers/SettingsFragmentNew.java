package com.suyogbauskar.attenteachers;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragmentNew extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        ListPreference themePreference = findPreference("theme");
        themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue.equals("system")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            } else if (newValue.equals("light")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (newValue.equals("dark")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            return true;
        });
    }
}