package com.suyogbauskar.attenteachers;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.suyogbauskar.attenteachers.fragments.HomeFragment;
import com.suyogbauskar.attenteachers.fragments.SettingsFragmentNew;
import com.suyogbauskar.attenteachers.fragments.StatisticsFragment;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav_view);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.home:
                    selectedFragment = new HomeFragment();
                    break;

                case R.id.statistics:
                    selectedFragment = new StatisticsFragment();
                    break;

                case R.id.settings:
                    selectedFragment = new SettingsFragmentNew();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, selectedFragment).commit();
            return true;
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomeFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}