package com.suyogbauskar.attenteachers.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.suyogbauskar.attenteachers.R;
import com.suyogbauskar.attenteachers.fragments.HomeFragment;

import java.io.File;

public class SettingsFragment extends Fragment {

    private Button signOutBtn;

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        getActivity().setTitle("Settings");

        findAllViews(view);
        setOnClickListeners();
        UIModeConfiguration(view);

        return view;
    }

    private void findAllViews(View view) {
        signOutBtn = view.findViewById(R.id.signOutBtn);
    }

    private void setOnClickListeners() {
        signOutBtn.setOnClickListener(view -> {
           // do something on sign out
        });
    }

    private void UIModeConfiguration(View view) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(getContext(), R.array.theme_array, R.layout.theme_spinner_item);
        themeAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            themeAdapter = ArrayAdapter.createFromResource(getContext(), R.array.theme_array, R.layout.dark_theme_spinner_item);
            themeAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        }

        Spinner themeSpinner = view.findViewById(R.id.theme_spinner);
        themeSpinner.setAdapter(themeAdapter);

        if (HomeFragment.theme == 0) {
            themeSpinner.setSelection(0);
        } else if (HomeFragment.theme == 1) {
            themeSpinner.setSelection(1);
        } else if (HomeFragment.theme == 2) {
            themeSpinner.setSelection(2);
        }

        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    editor.putInt("theme", 0);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else if (i == 1) {
                    editor.putInt("theme", 1);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else if (i == 2) {
                    editor.putInt("theme", 2);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
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