package com.suyogbauskar.attenteachers.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.suyogbauskar.attenteachers.MainActivity;
import com.suyogbauskar.attenteachers.R;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SettingsFragment extends PreferenceFragmentCompat {

    private FirebaseUser user;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        getActivity().setTitle("Settings");

        user = FirebaseAuth.getInstance().getCurrentUser();

        Preference signOutPreference = findPreference("sign_out");
        signOutPreference.setOnPreferenceClickListener(preference -> {
            FirebaseAuth.getInstance().signOut();
            deleteCache(getContext());
            startActivity(new Intent(getContext(), MainActivity.class));
            return true;
        });

        Preference changePasswordPreference = findPreference("change_password");
        changePasswordPreference.setOnPreferenceClickListener(preference -> {
            new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Change Password?")
                    .setContentText("Link to change your password will be sent on " + user.getEmail())
                    .setConfirmText("Change")
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation();
                        FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                                .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Link sent successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setCancelButton("Cancel", SweetAlertDialog::dismissWithAnimation)
                    .show();
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