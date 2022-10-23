package com.suyogbauskar.attenteachers;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SettingManager {
    enum Theme{
        SYSTEM,
        LIGHT,
        DARK;

        public static Theme parse(String theme){
            List<Theme> themes =  Arrays.stream(Theme.values()).filter(t -> t.name().equals(theme)).collect(Collectors.toList());
            return themes.size()>0 ? themes.get(0): SYSTEM;
        }
    }

    public void init(Context context){
        initTheme(context);
    }

    private void initTheme(Context context) {
        Theme theme = Theme.parse(CustomSharedPreferences.get(context,CustomSharedPreferences.THEME));
        switch (theme){
            case SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                throw new RuntimeException("Unexpected Theme Entry found");
        }
    }
}
