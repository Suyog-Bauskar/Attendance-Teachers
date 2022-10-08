package com.suyogbauskar.attenteachers;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HomeFragment extends Fragment implements View.OnClickListener {

    public static int theme;

    private String firstnameDB, lastnameDB, subjectCodeDB, subjectNameDB;
    private static final long START_TIME_IN_MILLIS = 180000;
    private TextView mTextViewCountDown, codeView;
    private Button mButtonStop, generateCodeBtn, deleteBtn;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis, mEndTime;
    private int randomNo;
    private FirebaseUser user;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setTitle("Home");

        fetchDataFromDatabase();
        findAllViews(view);
        changeUIForNight();
        setOnClickListeners();
        setUserDefinedTheme();

        return view;
    }

    private void fetchDataFromDatabase() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid());

        databaseRef.get().addOnCompleteListener(task -> {
            DataSnapshot document = task.getResult();
            firstnameDB = document.child("firstname").getValue().toString();
            lastnameDB = document.child("lastname").getValue().toString();
            subjectNameDB = document.child("subject_name").getValue().toString();
            subjectCodeDB = document.child("subject_code").getValue().toString();
        });
    }

    private void findAllViews(View view) {
        mTextViewCountDown = view.findViewById(R.id.text_view_countdown);
        mButtonStop = view.findViewById(R.id.stopBtn);
        generateCodeBtn = view.findViewById(R.id.generateCodeBtn);
        codeView = view.findViewById(R.id.codeView);
        deleteBtn = view.findViewById(R.id.deleteBtn);
    }

    private void changeUIForNight() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            codeView.setTextColor(Color.WHITE);
            mTextViewCountDown.setTextColor(Color.WHITE);
        }
    }

    private void setOnClickListeners() {
        generateCodeBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        mButtonStop.setOnClickListener(this);
    }

    private void setUserDefinedTheme() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        theme = sharedPreferences.getInt("theme", 0);

        if (theme == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if (theme == 1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (theme == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
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
        getActivity().getSupportFragmentManager().beginTransaction().detach(this).commitNow();
        getActivity().getSupportFragmentManager().beginTransaction().attach(this).commitNow();
    }

    private void onAttendanceStart() {

        DatabaseReference activeAttendanceRef = FirebaseDatabase.getInstance().getReference("attendance/active_attendance");

        Map<String, Object> data = new HashMap<>();
        data.put("code", randomNo);
        data.put("isAttendanceRunning", true);
        data.put("firstname", firstnameDB);
        data.put("lastname", lastnameDB);
        data.put("subject_code", subjectCodeDB);
        data.put("subject_name", subjectNameDB);

        activeAttendanceRef.setValue(data);

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

//        DocumentReference todayAttendance = db.collection("attendance").document(subjectCodeDB).collection(String.valueOf(year)).document(monthStr);
//        todayAttendance.update("sub_collections_name", FieldValue.arrayUnion(dayAndTime));
    }

    private void onAttendanceStop() {
        DatabaseReference activeAttendanceRef = FirebaseDatabase.getInstance().getReference("attendance/active_attendance");

        Map<String, Object> data = new HashMap<>();
        data.put("code", 0);
        data.put("isAttendanceRunning", false);
        data.put("firstname", "0");
        data.put("lastname", "0");
        data.put("subject_code", "0");
        data.put("subject_name", "0");

        activeAttendanceRef.setValue(data);
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = getActivity().getSharedPreferences("timerPref", MODE_PRIVATE);
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
    public void onStop() {
        super.onStop();

        SharedPreferences prefs = getActivity().getSharedPreferences("timerPref", MODE_PRIVATE);
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
    public void onClick(View v) {
        int minValue = 10000;
        int maxValue = 99999;
        switch (v.getId()) {
            case R.id.generateCodeBtn:
                randomNo = new Random().nextInt((maxValue - minValue) + 1) + minValue;
                onAttendanceStart();
                codeView.setText("Code - " + randomNo);
                generateCodeBtn.setVisibility(View.GONE);
                mButtonStop.setVisibility(View.VISIBLE);
                deleteBtn.setVisibility(View.VISIBLE);

                startTimer();
                break;

            case R.id.stopBtn:
                stopTimer();
                onAttendanceStop();
                break;

            //TODO : Test delete functionality
            case R.id.deleteBtn:
                new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Delete Attendance?")
                        .setContentText("Currently started attendance will be deleted")
                        .setConfirmText("Delete")
                        .setConfirmClickListener(sDialog -> {
                            sDialog.dismissWithAnimation();
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

                            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("/attendance/" + subjectCodeDB + "/" +
                                    year + "/" + monthStr + "/" +);

//                            db.collection("attendance")
//                                    .document(subjectCodeDB)
//                                    .collection(String.valueOf(year))
//                                    .document(monthStr)
//                                    .collection(dayAndTime)
//                                    .get()
//                                    .addOnCompleteListener(task -> {
//                                        if (task.isSuccessful()) {
//                                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                                document.getReference().delete();
//                                            }
//                                            Toast.makeText(getContext(), "Attendance deleted", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });

                            stopTimer();
                            onAttendanceStop();
                        })
                        .setCancelButton("No", SweetAlertDialog::dismissWithAnimation)
                        .show();
                break;
        }
    }
}