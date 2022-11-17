package com.suyogbauskar.attenteachers.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HomeFragment extends Fragment {

    private String firstnameDB, lastnameDB, subjectCodeDB, subjectNameDB, attendanceOf = "", monthStr;
    private static final long START_TIME_IN_MILLIS = 180000;
    private TextView mTextViewCountDown, codeView;
    private Button mButtonStop, generateCodeBtn, deleteBtn;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis, mEndTime;
    private int randomNo, date, year;
    private FirebaseUser user;
    private static final String LECTURE_CO5I_A = "CO5I-A Lecture", LECTURE_CO5I_B = "CO5I-B Lecture", CO5I_1 = "CO5I-1 Practical", CO5I_2 = "CO5I-2 Practical", CO5I_3 = "CO5I-3 Practical", CO5I_4 = "CO5I-4 Practical", CO5I_5 = "CO5I-5 Practical";
    private static final String DB_PATH_LECTURE_COUNT_CO5I_A = "CO5I-A_lectures_taken_today", DB_PATH_LECTURE_COUNT_CO5I_B = "CO5I-B_lectures_taken_today", DB_PATH_PRACTICAL_COUNT_CO5I_1 = "CO5I-1_practicals_taken_today", DB_PATH_PRACTICAL_COUNT_CO5I_2 = "CO5I-2_practicals_taken_today", DB_PATH_PRACTICAL_COUNT_CO5I_3 = "CO5I-3_practicals_taken_today", DB_PATH_PRACTICAL_COUNT_CO5I_4 = "CO5I-4_practicals_taken_today", DB_PATH_PRACTICAL_COUNT_CO5I_5 = "CO5I-5_practicals_taken_today";

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setTitle("Attendance");

        init(view);
        fetchDataFromDatabase();
        refreshDaily();
        setOnClickListeners();
        getCurrentTime();

        return view;
    }

    private void init(View view) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        findAllViews(view);
    }

    private void fetchDataFromDatabase() {
        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid())
                .get().addOnCompleteListener(task -> {
                    DataSnapshot document = task.getResult();
                    firstnameDB = document.child("firstname").getValue(String.class);
                    lastnameDB = document.child("lastname").getValue(String.class);
                    subjectNameDB = document.child("subject_name").getValue(String.class);
                    subjectCodeDB = document.child("subject_code").getValue(String.class);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void findAllViews(View view) {
        mTextViewCountDown = view.findViewById(R.id.text_view_countdown);
        mButtonStop = view.findViewById(R.id.stopBtn);
        generateCodeBtn = view.findViewById(R.id.generateCodeBtn);
        codeView = view.findViewById(R.id.codeView);
        deleteBtn = view.findViewById(R.id.deleteBtn);
    }

    private void setOnClickListeners() {
        generateCodeBtn.setOnClickListener(view -> generateCodeBtn());

        mButtonStop.setOnClickListener(view -> stopAttendanceBtn());

        deleteBtn.setOnClickListener(view -> deleteCurrentAttendanceBtn());
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
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(() -> {
            getActivity().getSupportFragmentManager().beginTransaction().detach(HomeFragment.this).commitNow();
            getActivity().getSupportFragmentManager().beginTransaction().attach(HomeFragment.this).commitNow();
        });
    }

    private void onAttendanceStart() {
        Map<String, Object> data = new HashMap<>();
        data.put("code", randomNo);
        data.put("isAttendanceRunning", true);
        data.put("firstname", firstnameDB);
        data.put("lastname", lastnameDB);
        data.put("subject_code", subjectCodeDB);
        data.put("subject_name", subjectNameDB);
        data.put("uid", user.getUid());

        switch (attendanceOf) {
            case LECTURE_CO5I_A:
                Toast.makeText(getContext(), "Attendance started of CO5I-A", Toast.LENGTH_SHORT).show();
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_LECTURE_COUNT_CO5I_A).setValue(ServerValue.increment(1))
                        .addOnSuccessListener(unused -> FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_LECTURE_COUNT_CO5I_A)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        data.put(DB_PATH_LECTURE_COUNT_CO5I_A, snapshot.getValue(Integer.class));
                                        FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-A").setValue(data);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }));

                break;
            case LECTURE_CO5I_B:
                Toast.makeText(getContext(), "Attendance started of CO5I-B", Toast.LENGTH_SHORT).show();
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_LECTURE_COUNT_CO5I_B).setValue(ServerValue.increment(1))
                        .addOnSuccessListener(unused -> FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_LECTURE_COUNT_CO5I_B)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        data.put(DB_PATH_LECTURE_COUNT_CO5I_B, snapshot.getValue(Integer.class));
                                        FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-B").setValue(data);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }));

                break;
            case CO5I_1:
                Toast.makeText(getContext(), "Attendance started of CO5I-1", Toast.LENGTH_SHORT).show();
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_1).setValue(ServerValue.increment(1))
                        .addOnSuccessListener(unused -> FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_1)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        data.put(DB_PATH_PRACTICAL_COUNT_CO5I_1, snapshot.getValue(Integer.class));
                                        FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-1").setValue(data);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }));

                break;
            case CO5I_2:
                Toast.makeText(getContext(), "Attendance started of CO5I-2", Toast.LENGTH_SHORT).show();
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_2).setValue(ServerValue.increment(1))
                        .addOnSuccessListener(unused -> FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_2)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        data.put(DB_PATH_PRACTICAL_COUNT_CO5I_2, snapshot.getValue(Integer.class));
                                        FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-2").setValue(data);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }));

                break;
            case CO5I_3:
                Toast.makeText(getContext(), "Attendance started of CO5I-3", Toast.LENGTH_SHORT).show();
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_3).setValue(ServerValue.increment(1))
                        .addOnSuccessListener(unused -> FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_3)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        data.put(DB_PATH_PRACTICAL_COUNT_CO5I_3, snapshot.getValue(Integer.class));
                                        FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-3").setValue(data);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }));

                break;
            case CO5I_4:
                Toast.makeText(getContext(), "Attendance started of CO5I-4", Toast.LENGTH_SHORT).show();
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_4).setValue(ServerValue.increment(1))
                        .addOnSuccessListener(unused -> FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_4)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        data.put(DB_PATH_PRACTICAL_COUNT_CO5I_4, snapshot.getValue(Integer.class));
                                        FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-4").setValue(data);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }));

                break;
            case CO5I_5:
                Toast.makeText(getContext(), "Attendance started of CO5I-5", Toast.LENGTH_SHORT).show();
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_5).setValue(ServerValue.increment(1))
                        .addOnSuccessListener(unused -> FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_5)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        data.put(DB_PATH_PRACTICAL_COUNT_CO5I_5, snapshot.getValue(Integer.class));
                                        FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-5").setValue(data);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }));

                break;
        }
    }

    private void onAttendanceStop() {
        Map<String, Object> data = new HashMap<>();
        data.put("code", 0);
        data.put("isAttendanceRunning", false);
        data.put("firstname", "0");
        data.put("lastname", "0");
        data.put("subject_code", "0");
        data.put("subject_name", "0");
        data.put("uid", "0");

        switch (attendanceOf) {
            case LECTURE_CO5I_A:
                data.put(DB_PATH_LECTURE_COUNT_CO5I_A, 0);
                FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-A").setValue(data);
                break;
            case LECTURE_CO5I_B:
                data.put(DB_PATH_LECTURE_COUNT_CO5I_B, 0);
                FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-B").setValue(data);
                break;
            case CO5I_1:
                data.put(DB_PATH_PRACTICAL_COUNT_CO5I_1, 0);
                FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-1").setValue(data);
                break;
            case CO5I_2:
                data.put(DB_PATH_PRACTICAL_COUNT_CO5I_2, 0);
                FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-2").setValue(data);
                break;
            case CO5I_3:
                data.put(DB_PATH_PRACTICAL_COUNT_CO5I_3, 0);
                FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-3").setValue(data);
                break;
            case CO5I_4:
                data.put(DB_PATH_PRACTICAL_COUNT_CO5I_4, 0);
                FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-4").setValue(data);
                break;
            case CO5I_5:
                data.put(DB_PATH_PRACTICAL_COUNT_CO5I_5, 0);
                FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO5I-5").setValue(data);
                break;
        }
    }

    private void refreshDaily() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("dailyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String date = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());

        boolean hasDayChanged = !sharedPreferences.getString("date", "").equals(date);

        editor.putString("date", date);
        editor.apply();

        if (hasDayChanged) {
            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_LECTURE_COUNT_CO5I_A).setValue(0);
            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_LECTURE_COUNT_CO5I_B).setValue(0);
            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_1).setValue(0);
            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_2).setValue(0);
            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_3).setValue(0);
            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_4).setValue(0);
            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_5).setValue(0);
        }
    }

    private void deleteCurrentAttendanceBtn() {
        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Delete Attendance?")
                .setContentText("Currently started attendance will be deleted")
                .setConfirmText("Delete")
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();

                    switch (attendanceOf) {
                        case LECTURE_CO5I_A:
                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_LECTURE_COUNT_CO5I_A)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            FirebaseDatabase.getInstance().getReference("/attendance/CO5I-A/" + subjectCodeDB + "/" +
                                                    year + "/" + monthStr).child(date + "-" + snapshot.getValue(Integer.class)).removeValue();

                                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_LECTURE_COUNT_CO5I_A)
                                                    .setValue(ServerValue.increment(-1));
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            break;
                        case LECTURE_CO5I_B:
                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_LECTURE_COUNT_CO5I_B)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            FirebaseDatabase.getInstance().getReference("/attendance/CO5I-B/" + subjectCodeDB + "/" +
                                                    year + "/" + monthStr).child(date + "-" + snapshot.getValue(Integer.class)).removeValue();

                                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_LECTURE_COUNT_CO5I_B)
                                                    .setValue(ServerValue.increment(-1));
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            break;
                        case CO5I_1:
                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_1)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            FirebaseDatabase.getInstance().getReference("/attendance/CO5I-1/" + subjectCodeDB + "/" +
                                                    year + "/" + monthStr).child(date + "-" + snapshot.getValue(Integer.class)).removeValue();

                                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_1)
                                                    .setValue(ServerValue.increment(-1));
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            break;
                        case CO5I_2:
                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_2)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            FirebaseDatabase.getInstance().getReference("/attendance/CO5I-2/" + subjectCodeDB + "/" +
                                                    year + "/" + monthStr).child(date + "-" + snapshot.getValue(Integer.class)).removeValue();

                                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_2)
                                                    .setValue(ServerValue.increment(-1));
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            break;
                        case CO5I_3:
                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_3)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            FirebaseDatabase.getInstance().getReference("/attendance/CO5I-3/" + subjectCodeDB + "/" +
                                                    year + "/" + monthStr).child(date + "-" + snapshot.getValue(Integer.class)).removeValue();

                                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_3)
                                                    .setValue(ServerValue.increment(-1));
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            break;
                        case CO5I_4:
                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_4)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            FirebaseDatabase.getInstance().getReference("/attendance/CO5I-4/" + subjectCodeDB + "/" +
                                                    year + "/" + monthStr).child(date + "-" + snapshot.getValue(Integer.class)).removeValue();

                                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_4)
                                                    .setValue(ServerValue.increment(-1));
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            break;
                        case CO5I_5:
                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_5)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            FirebaseDatabase.getInstance().getReference("/attendance/CO5I-5/" + subjectCodeDB + "/" +
                                                    year + "/" + monthStr).child(date + "-" + snapshot.getValue(Integer.class)).removeValue();

                                            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/" + DB_PATH_PRACTICAL_COUNT_CO5I_5)
                                                    .setValue(ServerValue.increment(-1));
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            break;
                    }

                    stopTimer();
                    onAttendanceStop();
                })
                .setCancelButton("No", SweetAlertDialog::dismissWithAnimation)
                .show();
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
            deleteBtn.setVisibility(View.VISIBLE);

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

    private void getCurrentTime() {
        long currentDate = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy/HH/mm/ss/MMMM", Locale.getDefault());
        String dateStr = dateFormat.format(currentDate);
        String[] dateArr = dateStr.split("/");
        date = Integer.parseInt(dateArr[0]);
        year = Integer.parseInt(dateArr[2]);
        monthStr = dateArr[6];
    }

    private void generateCodeBtn() {
        randomNo = new Random().nextInt((99999 - 10000) + 1) + 10000;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("You are taking attendance of");
        String[] items = {LECTURE_CO5I_A, LECTURE_CO5I_B, CO5I_1, CO5I_2, CO5I_3, CO5I_4, CO5I_5};
        int checkedItem = 0;
        alertDialog.setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
            switch (which) {
                case 0:
                    attendanceOf = items[0];
                    break;
                case 1:
                    attendanceOf = items[1];
                    break;
                case 2:
                    attendanceOf = items[2];
                    break;
                case 3:
                    attendanceOf = items[3];
                    break;
                case 4:
                    attendanceOf = items[4];
                    break;
                case 5:
                    attendanceOf = items[5];
                    break;
                case 6:
                    attendanceOf = items[6];
                    break;
            }
            onAttendanceStart();
            codeView.setText("Code - " + randomNo);
            generateCodeBtn.setVisibility(View.GONE);
            mButtonStop.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);
            startTimer();
            dialog.dismiss();
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private void stopAttendanceBtn() {
        stopTimer();
        onAttendanceStop();
    }
}