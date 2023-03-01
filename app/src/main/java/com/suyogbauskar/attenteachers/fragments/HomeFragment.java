package com.suyogbauskar.attenteachers.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.suyogbauskar.attenteachers.pojos.Subject;
import com.suyogbauskar.attenteachers.pojos.SubjectInformation;
import com.suyogbauskar.attenteachers.utils.ProgressDialog;
import com.uzairiqbal.circulartimerview.CircularTimerListener;
import com.uzairiqbal.circulartimerview.CircularTimerView;
import com.uzairiqbal.circulartimerview.TimeFormatEnum;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HomeFragment extends Fragment {

    private String firstnameDB, lastnameDB, monthStr, selectedSubjectCode, selectedSubjectName, selectedSubjectShortName, selectedAttendanceOf, statusMessage, department;
    private TextView codeView, statusView;
    private Button generateCodeAndStopBtn, deleteBtn;
    private int randomNo, date, year, selectedSemester;
    private FirebaseUser user;
    private CircularTimerView progressBar;
    private SharedPreferences.Editor edit2;
    private final Map<String, SubjectInformation> allSubjects = new TreeMap<>();
    private final ProgressDialog progressDialog = new ProgressDialog();

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setTitle("Attendance");

        init(view);
        getCurrentTime();
        fetchDataFromDatabase();

        return view;
    }

    private void init(View view) {
        user = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("DBPathPref", MODE_PRIVATE);
        selectedAttendanceOf = sharedPreferences.getString("attendanceOf", "");
        selectedSubjectCode = sharedPreferences.getString("subjectCode", "");
        selectedSubjectName = sharedPreferences.getString("subjectName", "");
        selectedSubjectShortName = sharedPreferences.getString("subjectShortName", "");
        selectedSemester = sharedPreferences.getInt("subjectSemester", 0);

        SharedPreferences prefs = getActivity().getSharedPreferences("attendanceStatusPref", MODE_PRIVATE);
        edit2 = prefs.edit();

        findAllViews(view);
        setOnClickListeners();
    }

    private void fetchDataFromDatabase() {
        progressDialog.show(getContext());

        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid())
                .get().addOnCompleteListener(task -> {
                    progressDialog.hide();
                    DataSnapshot document = task.getResult();
                    firstnameDB = document.child("firstname").getValue(String.class);
                    lastnameDB = document.child("lastname").getValue(String.class);
                    department = document.child("department").getValue(String.class);

                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("teacherDataPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isAdmin", document.child("isAdmin").getValue(Boolean.class));
                    editor.putString("firstname", document.child("firstname").getValue(String.class));
                    editor.putString("lastname", document.child("lastname").getValue(String.class));
                    editor.putString("department", document.child("department").getValue(String.class));
                    editor.commit();

                    getSubjectInformation();

                })
                .addOnFailureListener(e -> {
                    progressDialog.hide();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void findAllViews(View view) {
        generateCodeAndStopBtn = view.findViewById(R.id.generateCodeAndStopBtn);
        codeView = view.findViewById(R.id.codeView);
        deleteBtn = view.findViewById(R.id.deleteBtn);
        statusView = view.findViewById(R.id.statusView);
        progressBar = view.findViewById(R.id.timer);
    }

    private void setOnClickListeners() {
        generateCodeAndStopBtn.setOnClickListener(view -> checkButtonName());
        deleteBtn.setOnClickListener(view -> deleteCurrentAttendanceBtn());
    }

    private void checkButtonName() {
        if (generateCodeAndStopBtn.getText().equals("Generate")) {
            generateCodeBtn();
        } else {
            stopAttendanceBtn();
        }
    }

    private void getSubjectInformation() {
        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, SubjectInformation> unsorted = new HashMap<>();

                        for (DataSnapshot dsp : snapshot.getChildren()) {
                            unsorted.put(dsp.getKey(), new SubjectInformation(dsp.getKey(),
                                    snapshot.child(dsp.getKey()).child("subject_name").getValue(String.class),
                                    snapshot.child(dsp.getKey()).child("subject_short_name").getValue(String.class),
                                    snapshot.child(dsp.getKey()).child("semester").getValue(Integer.class)));
                        }
                        allSubjects.putAll(unsorted);
                        refreshDaily();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onAttendanceStart() {
        Map<String, Object> data = new HashMap<>();
        data.put("code", randomNo);
        data.put("isAttendanceRunning", true);
        data.put("firstname", firstnameDB);
        data.put("lastname", lastnameDB);
        data.put("subject_code", selectedSubjectCode);
        data.put("subject_name", selectedSubjectName);
        data.put("subject_short_name", selectedSubjectShortName);
        data.put("uid", user.getUid());

        statusMessage = department + selectedSemester + "-" + selectedAttendanceOf + " " + selectedSubjectShortName + "\nAttendance Started";
        statusView.setText(statusMessage);
        statusView.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + selectedSubjectCode + "/" + selectedAttendanceOf + "_count")
                .setValue(ServerValue.increment(1))
                .addOnSuccessListener(unused -> FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + selectedSubjectCode + "/" + selectedAttendanceOf + "_count")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                data.put("count", snapshot.getValue(Integer.class));
                                FirebaseDatabase.getInstance().getReference("attendance/active_attendance/" + department + selectedSemester + "-" + selectedAttendanceOf).setValue(data);

                                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("DBPathPref",MODE_PRIVATE);
                                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                myEdit.putString("attendanceOf", selectedAttendanceOf);
                                myEdit.putString("subjectCode", selectedSubjectCode);
                                myEdit.putString("subjectName", selectedSubjectName);
                                myEdit.putString("subjectShortName", selectedSubjectShortName);
                                myEdit.putInt("subjectSemester", selectedSemester);
                                myEdit.putInt("count", snapshot.getValue(Integer.class));
                                myEdit.commit();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }));
    }

    private void stopAttendanceBtn() {
        Map<String, Object> data = new HashMap<>();
        data.put("code", 0);
        data.put("isAttendanceRunning", false);
        data.put("firstname", "0");
        data.put("lastname", "0");
        data.put("subject_code", "0");
        data.put("subject_name", "0");
        data.put("subject_short_name", "0");
        data.put("uid", "0");
        data.put("count", 0);

        FirebaseDatabase.getInstance().getReference("attendance/active_attendance/" + department + selectedSemester + "-" + selectedAttendanceOf).setValue(data);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("DBPathPref",MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("attendanceOf", "");
        myEdit.putString("subjectCode", "");
        myEdit.putString("subjectName", "");
        myEdit.putString("subjectShortName", "");
        myEdit.putInt("subjectSemester", 0);
        myEdit.putInt("count", 0);
        myEdit.commit();

        SharedPreferences prefs = getActivity().getSharedPreferences("attendanceStatusPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("wasAttendanceRunning", false);
        editor.putString("statusMessage", "");
        editor.putInt("code", 0);
        editor.apply();

        randomNo = 0;

        new Handler(Looper.getMainLooper()).post(() -> {
            getActivity().getSupportFragmentManager().beginTransaction().detach(HomeFragment.this).commitNow();
            getActivity().getSupportFragmentManager().beginTransaction().attach(HomeFragment.this).commitNow();
        });
    }

    private void refreshDaily() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("dailyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String date = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());

        boolean hasDayChanged = !sharedPreferences.getString("date", "").equals(date);

        editor.putString("date", date);
        editor.apply();

        if (hasDayChanged) {
            for (Map.Entry<String, SubjectInformation> entry1: allSubjects.entrySet()) {
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + entry1.getKey() + "/A_count").setValue(0);
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + entry1.getKey() + "/B_count").setValue(0);
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + entry1.getKey() + "/C_count").setValue(0);
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + entry1.getKey() + "/A1_count").setValue(0);
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + entry1.getKey() + "/A2_count").setValue(0);
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + entry1.getKey() + "/A3_count").setValue(0);
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + entry1.getKey() + "/B1_count").setValue(0);
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + entry1.getKey() + "/B2_count").setValue(0);
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + entry1.getKey() + "/B3_count").setValue(0);
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + entry1.getKey() + "/C1_count").setValue(0);
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + entry1.getKey() + "/C2_count").setValue(0);
                FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + entry1.getKey() + "/C3_count").setValue(0);
            }
            FirebaseDatabase.getInstance().getReference("teachers_data").child(user.getUid()).child("notifications").removeValue();
        }
    }

    private void deleteCurrentAttendanceBtn() {
        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Delete Attendance?")
                .setContentText("Currently started attendance will be deleted")
                .setConfirmText("Delete")
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();

                    FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + selectedSubjectCode + "/" + selectedAttendanceOf + "_count")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    FirebaseDatabase.getInstance().getReference("attendance/" + department + selectedSemester + "-" + selectedAttendanceOf + "/" + selectedSubjectCode + "/" + year + "/" + monthStr)
                                            .child(date + "-" + snapshot.getValue(Integer.class)).removeValue();

                                    FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + selectedSubjectCode + "/" + selectedAttendanceOf + "_count")
                                            .setValue(ServerValue.increment(-1));

                                    stopAttendanceBtn();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setCancelButton("No", SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = getActivity().getSharedPreferences("attendanceStatusPref", MODE_PRIVATE);
        boolean wasAttendanceRunning = prefs.getBoolean("wasAttendanceRunning", false);
        randomNo = prefs.getInt("code", 0);
        statusMessage = prefs.getString("statusMessage", "");

        if (wasAttendanceRunning) {
            long endTime = prefs.getLong("endTime", 0);
            long timerTime = endTime - System.currentTimeMillis();
            if (timerTime < 0) {
                stopAttendanceBtn();
                return;
            }
            timerTime = timerTime / 1000;
            codeView.setText("Code - " + randomNo);
            deleteBtn.setVisibility(View.VISIBLE);
            generateCodeAndStopBtn.setText("Stop");
            statusView.setText(statusMessage);
            statusView.setVisibility(View.VISIBLE);

            progressBar.setCircularTimerListener(new CircularTimerListener() {
                @Override
                public String updateDataOnTick(long remainingTimeInMs) {
                    edit2.putLong("time", remainingTimeInMs);
                    return String.valueOf((int)Math.ceil((remainingTimeInMs / 1000.f)));
                }

                @Override
                public void onTimerFinished() {
                    stopAttendanceBtn();
                }
            }, timerTime, TimeFormatEnum.SECONDS, 10);
            progressBar.startTimer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        edit2.apply();
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
        AtomicBoolean anySubjectFound = new AtomicBoolean(false);
        List<Subject> currentSemesterSubjectList = new ArrayList<>();

        AlertDialog.Builder semesterDialog = new AlertDialog.Builder(getContext());
        semesterDialog.setTitle("Semester");
        String[] items = {"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6"};
        semesterDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
            dialog.dismiss();
            int semester = 0;
            for (Map.Entry<String, SubjectInformation> entry1 : allSubjects.entrySet()) {
                semester = Integer.parseInt(items[which].charAt(items[which].length() - 1) + "");
                if (entry1.getValue().getSubjectSemester() == semester) {
                    anySubjectFound.set(true);
                    currentSemesterSubjectList.add(new Subject(entry1.getValue().getSubjectShortName(), entry1.getValue().getSubjectName(), entry1.getValue().getSubjectCode()));
                }
            }

            if (!anySubjectFound.get()) {
                Toast.makeText(getContext(), "You don't teach this semester", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedSemester = semester;

            AlertDialog.Builder divisionDialog = new AlertDialog.Builder(getContext());
            divisionDialog.setTitle("Division");
            String[] items2 = {"Division A", "Division B", "Division C", "Batch A1", "Batch A2", "Batch A3", "Batch B1", "Batch B2", "Batch B3", "Batch C1", "Batch C2", "Batch C3"};
            divisionDialog.setSingleChoiceItems(items2, -1, (dialog2, which2) -> {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("DBPathPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                switch (which2) {
                    case 0:
                        selectedAttendanceOf = "A";
                        break;
                    case 1:
                        selectedAttendanceOf = "B";
                        break;
                    case 2:
                        selectedAttendanceOf = "C";
                        break;
                    case 3:
                        selectedAttendanceOf = "A1";
                        break;
                    case 4:
                        selectedAttendanceOf = "A2";
                        break;
                    case 5:
                        selectedAttendanceOf = "A3";
                        break;
                    case 6:
                        selectedAttendanceOf = "B1";
                        break;
                    case 7:
                        selectedAttendanceOf = "B2";
                        break;
                    case 8:
                        selectedAttendanceOf = "B3";
                        break;
                    case 9:
                        selectedAttendanceOf = "C1";
                        break;
                    case 10:
                        selectedAttendanceOf = "C2";
                        break;
                    case 11:
                        selectedAttendanceOf = "C3";
                        break;
                }

                editor.putString("attendanceOf", selectedAttendanceOf);
                editor.commit();

                if (currentSemesterSubjectList.size() > 1) {
                    AlertDialog.Builder subjectDialog = new AlertDialog.Builder(getContext());
                    subjectDialog.setTitle("Subjects");
                    String[] items3 = new String[currentSemesterSubjectList.size()];
                    for (int i = 0; i < currentSemesterSubjectList.size(); i++) {
                        items3[i] = currentSemesterSubjectList.get(i).getShortName();
                    }

                    subjectDialog.setSingleChoiceItems(items3, -1, (dialog3, which3) -> {
                        dialog3.dismiss();
                        selectedSubjectCode = currentSemesterSubjectList.get(which3).getCode();
                        selectedSubjectName = currentSemesterSubjectList.get(which3).getName();
                        selectedSubjectShortName = currentSemesterSubjectList.get(which3).getShortName();
                        startAttendanceAfterSelectingAllOptions();
                    });
                    subjectDialog.create().show();
                } else {
                    selectedSubjectCode = currentSemesterSubjectList.get(0).getCode();
                    selectedSubjectName = currentSemesterSubjectList.get(0).getName();
                    selectedSubjectShortName = currentSemesterSubjectList.get(0).getShortName();
                    startAttendanceAfterSelectingAllOptions();
                }
                dialog2.dismiss();
            });
            divisionDialog.create().show();
        });
        semesterDialog.create().show();
    }

    private void startAttendanceAfterSelectingAllOptions() {
        onAttendanceStart();
        codeView.setText("Code - " + randomNo);
        deleteBtn.setVisibility(View.VISIBLE);
        generateCodeAndStopBtn.setText("Stop");

        edit2.putBoolean("wasAttendanceRunning", true);
        edit2.putString("statusMessage", statusMessage);
        edit2.putInt("code", randomNo);
        edit2.putLong("endTime", System.currentTimeMillis() + 180000L);
        edit2.apply();

        progressBar.setCircularTimerListener(new CircularTimerListener() {
            @Override
            public String updateDataOnTick(long remainingTimeInMs) {
                edit2.putLong("time", remainingTimeInMs);
                return String.valueOf((int)Math.ceil((remainingTimeInMs / 1000.f)));
            }

            @Override
            public void onTimerFinished() {
                stopAttendanceBtn();
            }
        }, 180, TimeFormatEnum.SECONDS, 10);
        progressBar.startTimer();
    }
}