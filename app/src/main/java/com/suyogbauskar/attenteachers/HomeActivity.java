package com.suyogbauskar.attenteachers;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.Random;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private BottomNavigationView bottomNav;
    public static int theme;

    private static final long START_TIME_IN_MILLIS = 180000;
    private final int minValue = 10000;
    private final int maxValue = 99999;
    private TextView mTextViewCountDown, codeView;
    private Button mButtonStop, generateCodeBtn;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis, mEndTime;
    private Random rand;
    private int randomNo, nightModeFlags;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        prefs = getSharedPreferences("timerPref", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbarHome);
        toolbar.setTitle("Attendance");
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottomNavigationView);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mButtonStop = findViewById(R.id.button_stop);
        generateCodeBtn = findViewById(R.id.generateCodeBtn);
        codeView = findViewById(R.id.codeView);

        nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            codeView.setTextColor(Color.WHITE);
            mTextViewCountDown.setTextColor(Color.WHITE);
        }
        generateCodeBtn.setOnClickListener(this);

        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    return true;

                case R.id.profile:
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;

                case R.id.settings:
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.home);

        SharedPreferences sharedPreferences = getSharedPreferences("themePref", MODE_PRIVATE);
        theme = sharedPreferences.getInt("theme", 0);

        if (theme == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if (theme == 1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (theme == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        mButtonStop.setOnClickListener(v -> {
            stopTimer();
            db.collection("teachers_data").document(user.getUid()).update("code", 0);
        });

        rand = new Random();
    }

    private void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                stopAttendance();
                db.collection("teachers_data").document(user.getUid()).update("code", 0);
            }
        }.start();

        mTimerRunning = true;
        mButtonStop.setText("Stop");
    }

    private void stopTimer() {
        mCountDownTimer.cancel();
        stopAttendance();
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        mTextViewCountDown.setText(timeLeftFormatted);
    }

    private void stopAttendance() {
        mTimeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
        mTimerRunning = false;
        randomNo = 0;
        HomeActivity.this.recreate();
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);
        editor.putInt("code", randomNo);
        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mTimeLeftInMillis = prefs.getLong("millisLeft", START_TIME_IN_MILLIS);
        mTimerRunning = prefs.getBoolean("timerRunning", false);
        randomNo = prefs.getInt("code", 0);

        updateCountDownText();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();
            codeView.setText("Code - " + randomNo);
            generateCodeBtn.setVisibility(View.GONE);
            mButtonStop.setVisibility(View.VISIBLE);

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
            } else {
                startTimer();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.generateCodeBtn:
                randomNo = rand.nextInt((maxValue - minValue) + 1) + minValue;
                db.collection("teachers_data").document(user.getUid()).update("code", randomNo);
                codeView.setText("Code - " + randomNo);
                generateCodeBtn.setVisibility(View.GONE);
                mButtonStop.setVisibility(View.VISIBLE);

                startTimer();
                break;
        }
    }
}