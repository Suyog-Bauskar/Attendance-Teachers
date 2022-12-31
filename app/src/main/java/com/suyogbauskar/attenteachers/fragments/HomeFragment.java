package com.suyogbauskar.attenteachers.fragments;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.R;
import com.suyogbauskar.attenteachers.pojos.SubjectInformation;
import com.suyogbauskar.attenteachers.utils.ProgressDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HomeFragment extends Fragment {

    private String firstnameDB, lastnameDB, monthStr, selectedSubjectCode, selectedSubjectName, selectedSubjectShortName, selectedAttendanceOf, statusMessage;
    private TextView codeView, statusView;
    private Button generateCodeAndStopBtn, deleteBtn;
    private int randomNo, date, year, selectedSemester, count;
    private FirebaseUser user;
    private boolean wasAttendanceRunning;
    private final Map<String, SubjectInformation> allSubjects = new TreeMap<>();
    private final ProgressDialog progressDialog = new ProgressDialog();

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setTitle("Attendance");

        init(view);
        setOnClickListeners();
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
        count = sharedPreferences.getInt("count", 0);
        Log.d(TAG, "Attendance: " + selectedAttendanceOf);
        findAllViews(view);
    }

    private void fetchDataFromDatabase() {
        progressDialog.show(getContext());

        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid())
                .get().addOnCompleteListener(task -> {
                    progressDialog.hide();
                    DataSnapshot document = task.getResult();
                    firstnameDB = document.child("firstname").getValue(String.class);
                    lastnameDB = document.child("lastname").getValue(String.class);
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

                            refreshDaily(dsp);
                        }
                        allSubjects.putAll(unsorted);
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

        statusMessage = "CO" + selectedSemester + "-" + selectedAttendanceOf + " " + selectedSubjectShortName + "\nAttendance Started";
        statusView.setText(statusMessage);
        statusView.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + selectedSubjectCode + "/" + selectedAttendanceOf + "_count")
                .setValue(ServerValue.increment(1))
                .addOnSuccessListener(unused -> FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + selectedSubjectCode + "/" + selectedAttendanceOf + "_count")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                data.put("count", snapshot.getValue(Integer.class));
                                FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO" + selectedSemester + "-" + selectedAttendanceOf).setValue(data);

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

        FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO" + selectedSemester + "-" + selectedAttendanceOf).setValue(data);

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

        statusView.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);
        codeView.setText("Generate\nCode");
        generateCodeAndStopBtn.setText("Generate");
    }

    private void refreshDaily(DataSnapshot snapshot) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("dailyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String date = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());

        boolean hasDayChanged = !sharedPreferences.getString("date", "").equals(date);

        editor.putString("date", date);
        editor.apply();

        if (hasDayChanged) {
            snapshot.child("A_count").getRef().setValue(0);
            snapshot.child("B_count").getRef().setValue(0);
            snapshot.child("A1_count").getRef().setValue(0);
            snapshot.child("A2_count").getRef().setValue(0);
            snapshot.child("A3_count").getRef().setValue(0);
            snapshot.child("B1_count").getRef().setValue(0);
            snapshot.child("B2_count").getRef().setValue(0);
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
                                    FirebaseDatabase.getInstance().getReference("attendance/CO" + selectedSemester + "-" + selectedAttendanceOf + "/" + selectedSubjectCode + "/" + year + "/" + monthStr)
                                            .child(date + "-" + snapshot.getValue(Integer.class)).removeValue();

                                    FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + selectedSubjectCode + "/" + selectedAttendanceOf + "_count")
                                            .setValue(ServerValue.increment(-1));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                    stopAttendanceBtn();
                })
                .setCancelButton("No", SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = getActivity().getSharedPreferences("attendanceStatusPref", MODE_PRIVATE);
        wasAttendanceRunning = prefs.getBoolean("wasAttendanceRunning", false);
        randomNo = prefs.getInt("code", 0);
        statusMessage = prefs.getString("statusMessage", "");

        if (wasAttendanceRunning) {
            codeView.setText("Code - " + randomNo);
            deleteBtn.setVisibility(View.VISIBLE);
            generateCodeAndStopBtn.setText("Stop");
            statusView.setText(statusMessage);
            statusView.setVisibility(View.VISIBLE);
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
        AtomicBoolean anySubjectFound = new AtomicBoolean(false);

        PopupMenu semesterMenu = new PopupMenu(getContext(), codeView);
        semesterMenu.getMenu().add(Menu.NONE, 1, 1, "Semester 1");
        semesterMenu.getMenu().add(Menu.NONE, 2, 2, "Semester 2");
        semesterMenu.getMenu().add(Menu.NONE, 3, 3, "Semester 3");
        semesterMenu.getMenu().add(Menu.NONE, 4, 4, "Semester 4");
        semesterMenu.getMenu().add(Menu.NONE, 5, 5, "Semester 5");
        semesterMenu.getMenu().add(Menu.NONE, 6, 6, "Semester 6");
        semesterMenu.show();

        semesterMenu.setOnMenuItemClickListener(item -> {
            for (Map.Entry<String, SubjectInformation> entry1 : allSubjects.entrySet()) {
                if (entry1.getValue().getSubjectSemester() == item.getItemId()) {
                    anySubjectFound.set(true);
                    selectedSubjectCode = entry1.getValue().getSubjectCode();
                    selectedSubjectName = entry1.getValue().getSubjectName();
                    selectedSubjectShortName = entry1.getValue().getSubjectShortName();
                }
            }

            if (!anySubjectFound.get()) {
                Toast.makeText(getContext(), "You don't teach this semester", Toast.LENGTH_SHORT).show();
                return false;
            }

            selectedSemester = item.getItemId();

            PopupMenu attendanceOfMenu = new PopupMenu(getContext(), codeView);
            attendanceOfMenu.getMenu().add(Menu.NONE, 1, 1, "Division A");
            attendanceOfMenu.getMenu().add(Menu.NONE, 2, 2, "Division B");
            attendanceOfMenu.getMenu().add(Menu.NONE, 3, 3, "Batch A1");
            attendanceOfMenu.getMenu().add(Menu.NONE, 4, 4, "Batch A2");
            attendanceOfMenu.getMenu().add(Menu.NONE, 5, 5, "Batch A3");
            attendanceOfMenu.getMenu().add(Menu.NONE, 6, 6, "Batch B1");
            attendanceOfMenu.getMenu().add(Menu.NONE, 7, 7, "Batch B2");
            attendanceOfMenu.show();
            attendanceOfMenu.setOnMenuItemClickListener(item2 -> {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("DBPathPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                switch (item2.getItemId()) {
                    case 1:
                        selectedAttendanceOf = "A";
                        editor.putString("attendanceOf", "A");
                        break;

                    case 2:
                        selectedAttendanceOf = "B";
                        editor.putString("attendanceOf", "B");
                        break;

                    case 3:
                        selectedAttendanceOf = "A1";
                        editor.putString("attendanceOf", "A1");
                        break;

                    case 4:
                        selectedAttendanceOf = "A2";
                        editor.putString("attendanceOf", "A2");
                        break;

                    case 5:
                        selectedAttendanceOf = "A3";
                        editor.putString("attendanceOf", "A3");
                        break;

                    case 6:
                        selectedAttendanceOf = "B1";
                        editor.putString("attendanceOf", "B1");
                        break;

                    case 7:
                        selectedAttendanceOf = "B2";
                        editor.putString("attendanceOf", "B2");
                        break;
                }

                editor.commit();
                onAttendanceStart();
                codeView.setText("Code - " + randomNo);
                deleteBtn.setVisibility(View.VISIBLE);
                generateCodeAndStopBtn.setText("Stop");

                SharedPreferences prefs = getActivity().getSharedPreferences("attendanceStatusPref", MODE_PRIVATE);
                SharedPreferences.Editor edit2 = prefs.edit();
                edit2.putBoolean("wasAttendanceRunning", true);
                edit2.putString("statusMessage", statusMessage);
                edit2.putInt("code", randomNo);
                edit2.apply();
                return true;
            });
            return true;
        });
    }

}