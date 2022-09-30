package com.suyogbauskar.attenteachers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private String firstnameDB, lastnameDB, subjectCodeDB, subjectNameDB;
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
    private DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("teachers_data").document(user.getUid());

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    firstnameDB = document.getString("firstname");
                    lastnameDB = document.getString("lastname");
                    subjectCodeDB = document.getString("subject_code");
                    subjectNameDB = document.getString("subject_name");
                }
            }
        });

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
            onAttendanceStop();
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
                onAttendanceStop();
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
                stopAttendance();
                onAttendanceStop();
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
                onAttendanceStart();
                codeView.setText("Code - " + randomNo);
                generateCodeBtn.setVisibility(View.GONE);
                mButtonStop.setVisibility(View.VISIBLE);

                startTimer();
                break;
        }
    }

    private void onAttendanceStart() {
        db.collection("attendance").document("active_attendance").update("code", randomNo);
        db.collection("attendance").document("active_attendance").update("isAttendanceRunning", "true");
        db.collection("attendance").document("active_attendance").update("firstname", firstnameDB);
        db.collection("attendance").document("active_attendance").update("lastname", lastnameDB);
        db.collection("attendance").document("active_attendance").update("subject_code", subjectCodeDB);
        db.collection("attendance").document("active_attendance").update("subject_name", subjectNameDB);
        db.collection("attendance").document("active_attendance").update("uid", user.getUid());

        long currentDate = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy/HH/mm/ss/MMMM", Locale.getDefault());
        String dateStr = dateFormat.format(currentDate);
        String[] dateArr = dateStr.split("/");
        int date = Integer.parseInt(dateArr[0]);
        int month = Integer.parseInt(dateArr[1]);
        int year = Integer.parseInt(dateArr[2]);
        int hour = Integer.parseInt(dateArr[3]);
        int minute = Integer.parseInt(dateArr[4]);
        int second = Integer.parseInt(dateArr[5]);
        String monthStr = dateArr[6];
        String dayAndTime = date + "-" + hour;

        Map<String, Object> attendance = new HashMap<>();
        attendance.put(dayAndTime, Collections.emptyList());

        DocumentReference todayAttendance = db.collection("attendance").document(subjectCodeDB).collection(String.valueOf(year)).document(monthStr);
        todayAttendance.set(attendance, SetOptions.merge());
    }

    private void onAttendanceStop() {
        db.collection("attendance").document("active_attendance").update("code", 0);
        db.collection("attendance").document("active_attendance").update("isAttendanceRunning", "false");
        db.collection("attendance").document("active_attendance").update("firstname", "0");
        db.collection("attendance").document("active_attendance").update("lastname", "0");
        db.collection("attendance").document("active_attendance").update("subject_code", "0");
        db.collection("attendance").document("active_attendance").update("subject_name", "0");
        db.collection("attendance").document("active_attendance").update("uid", "0");
    }
}