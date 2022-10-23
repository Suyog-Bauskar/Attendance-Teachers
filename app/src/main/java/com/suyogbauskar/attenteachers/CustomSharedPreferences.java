package com.suyogbauskar.attenteachers;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public enum CustomSharedPreferences {
    THEME
    ;

    private static final String SHARED_PREFERENCES = "SHARED_PREFERENCES";

    public static void set(Context context, CustomSharedPreferences key, String value){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key.name(), value);
        editor.apply();
    }

    public static <T> T get(Context context, CustomSharedPreferences key){
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return (T) preferences.getAll().get(key.name());
    }
}
