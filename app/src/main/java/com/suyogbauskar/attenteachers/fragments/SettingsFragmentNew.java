package com.suyogbauskar.attenteachers.fragments;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.suyogbauskar.attenteachers.CustomSharedPreferences;
import com.suyogbauskar.attenteachers.R;
import com.suyogbauskar.attenteachers.SettingManager;

public class SettingsFragmentNew extends PreferenceFragmentCompat {
    private final SettingManager settingManager = new SettingManager();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        ListPreference themePreference = findPreference("theme");
        themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            CustomSharedPreferences.set(getContext(), CustomSharedPreferences.THEME, newValue.toString());
            settingManager.init(getContext());
            return true;
        });
    }
}