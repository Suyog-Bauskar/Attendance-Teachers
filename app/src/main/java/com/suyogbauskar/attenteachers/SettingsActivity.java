package com.suyogbauskar.attenteachers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private BottomNavigationView bottomNav;
    private Button signOutBtn, changeDetailsBtn;
    private Spinner themeSpinner;
    private FirebaseUser user;
    private LinearLayout.LayoutParams params;
    private LinearLayout layout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottomNavigationView);
        signOutBtn = findViewById(R.id.signOutBtn);
        changeDetailsBtn = findViewById(R.id.changeDetailsBtn);

        bottomNav.setSelectedItemId(R.id.settings);
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    overridePendingTransition(0, 0);
                    return true;

                case R.id.profile:
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;

                case R.id.settings:
                    return true;
            }
            return false;
        });


        signOutBtn.setOnClickListener(this);
        changeDetailsBtn.setOnClickListener(this);

        SharedPreferences sharedPreferences = getSharedPreferences("themePref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        themeSpinner = findViewById(R.id.theme_spinner);
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.theme_array, R.layout.theme_spinner_item);
        themeAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            themeAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.theme_array, R.layout.dark_theme_spinner_item);
            themeAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        }

        themeSpinner.setAdapter(themeAdapter);

        if (HomeActivity.theme == 0) {
            themeSpinner.setSelection(0);
        } else if (HomeActivity.theme == 1) {
            themeSpinner.setSelection(1);
        } else if (HomeActivity.theme == 2) {
            themeSpinner.setSelection(2);
        }

        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    editor.putInt("theme", 0);
                    editor.apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else if (i == 1) {
                    editor.putInt("theme", 1);
                    editor.apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else if (i == 2) {
                    editor.putInt("theme", 2);
                    editor.apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) (15 * getResources().getDisplayMetrics().density + 0.5f), (int) (15 * getResources().getDisplayMetrics().density + 0.5f), (int) (15 * getResources().getDisplayMetrics().density + 0.5f), 0);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signOutBtn:
                signOut();
                break;

            case R.id.changeDetailsBtn:
                startActivity(new Intent(getApplicationContext(), ChangeDetailsActivity.class));
                break;
        }
    }
}