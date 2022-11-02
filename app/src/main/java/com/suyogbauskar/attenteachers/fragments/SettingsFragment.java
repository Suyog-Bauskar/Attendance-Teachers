package com.suyogbauskar.attenteachers.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.suyogbauskar.attenteachers.MainActivity;
import com.suyogbauskar.attenteachers.R;
import com.suyogbauskar.attenteachers.utils.CustomSharedPreferences;
import com.suyogbauskar.attenteachers.utils.SettingManager;

import java.io.File;

public class SettingsFragment extends PreferenceFragmentCompat {
    private final SettingManager settingManager = new SettingManager();
    private FirebaseUser user;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        getActivity().setTitle("Settings");

        user = FirebaseAuth.getInstance().getCurrentUser();

        ListPreference themePreference = findPreference("theme");
        themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            CustomSharedPreferences.set(getContext(), CustomSharedPreferences.THEME, newValue.toString());
            settingManager.init(getContext());
            return true;
        });

        Preference signOutPreference = findPreference("sign_out");
        signOutPreference.setOnPreferenceClickListener(preference -> {
            deleteCache(getContext());
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), MainActivity.class));
            return true;
        });

        Preference deleteAccountPreference = findPreference("delete_account");
        deleteAccountPreference.setOnPreferenceClickListener(preference -> {
            deleteCache(getContext());
            FirebaseAuth.getInstance().signOut();
            FirebaseAuth.getInstance().getCurrentUser().delete();
            startActivity(new Intent(getActivity(), MainActivity.class));
            return true;
        });
    }

    private void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}