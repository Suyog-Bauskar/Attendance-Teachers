package com.suyogbauskar.attenteachers.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.AttendanceBelow75Activity;
import com.suyogbauskar.attenteachers.R;
import com.suyogbauskar.attenteachers.SubjectsActivity;
import com.suyogbauskar.attenteachers.excelfiles.CreateExcelFileOfAttendance;

import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsFragment extends Fragment {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Button excelBtn, attendanceBelow75Btn, subjectsBtn;
    private boolean subjectFound;

    public StatisticsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        getActivity().setTitle("Statistics");

        findAllViews(view);
        setOnClickListeners();

        return view;
    }

    private void findAllViews(View view) {
        excelBtn = view.findViewById(R.id.excelBtn);
        attendanceBelow75Btn = view.findViewById(R.id.attendanceBelow75Btn);
        subjectsBtn = view.findViewById(R.id.subjectsBtn);
    }

    private void setOnClickListeners() {
        excelBtn.setOnClickListener(view -> showDialogForCreatingExcelFile());
        attendanceBelow75Btn.setOnClickListener(view -> showDialogForFindingStudentsBelow75());
        subjectsBtn.setOnClickListener(view -> startActivity(new Intent(getActivity(), SubjectsActivity.class)));
    }

    private void showDialogForCreatingExcelFile() {
        AlertDialog.Builder semesterDialog = new AlertDialog.Builder(getContext());
        semesterDialog.setTitle("Semester");
        String[] items = {"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6"};

        AtomicInteger selectedSemester = new AtomicInteger();
        semesterDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("excelValuesPref",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            switch (which) {
                case 0:
                    editor.putInt("semester", 1);
                    selectedSemester.set(1);
                    break;
                case 1:
                    editor.putInt("semester", 2);
                    selectedSemester.set(2);
                    break;
                case 2:
                    editor.putInt("semester", 3);
                    selectedSemester.set(3);
                    break;
                case 3:
                    editor.putInt("semester", 4);
                    selectedSemester.set(4);
                    break;
                case 4:
                    editor.putInt("semester", 5);
                    selectedSemester.set(5);
                    break;
                case 5:
                    editor.putInt("semester", 6);
                    selectedSemester.set(6);
                    break;
            }
            editor.commit();
            dialog.dismiss();

            subjectFound = false;
            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dsp : snapshot.getChildren()) {
                                if (snapshot.child(dsp.getKey()).child("semester").getValue(Integer.class) == selectedSemester.get()) {
                                    editor.putString("subjectCode", dsp.getKey());
                                    editor.putString("subjectName", snapshot.child(dsp.getKey()).child("subject_name").getValue(String.class));
                                    editor.commit();
                                    subjectFound = true;
                                    break;
                                }
                            }

                            if (!subjectFound) {
                                Toast.makeText(getContext(), "You don't teach this semester", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            AlertDialog.Builder yearDialog = new AlertDialog.Builder(getContext());
                            yearDialog.setTitle("Year");
                            String[] items2 = {"2023", "2024", "2025", "2026", "2027"};

                            yearDialog.setSingleChoiceItems(items2, -1, (dialog2, which2) -> {
                                SharedPreferences sharedPreferences2 = getActivity().getSharedPreferences("excelValuesPref",MODE_PRIVATE);
                                SharedPreferences.Editor editor2 = sharedPreferences2.edit();
                                switch (which2) {
                                    case 0:
                                        editor2.putString("year", items2[0]);
                                        break;
                                    case 1:
                                        editor2.putString("year", items2[1]);
                                        break;
                                    case 2:
                                        editor2.putString("year", items2[2]);
                                        break;
                                    case 3:
                                        editor2.putString("year", items2[3]);
                                        break;
                                    case 4:
                                        editor2.putString("year", items2[4]);
                                        break;
                                }
                                Toast.makeText(getContext(), "Creating Excel File...", Toast.LENGTH_SHORT).show();
                                dialog2.dismiss();
                                editor2.commit();
                                requireActivity().startService(new Intent(getContext(), CreateExcelFileOfAttendance.class));
                            });
                            yearDialog.create().show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        semesterDialog.create().show();
    }

    private void showDialogForFindingStudentsBelow75() {
        AlertDialog.Builder semesterDialog = new AlertDialog.Builder(getContext());
        semesterDialog.setTitle("Semester");
        String[] items = {"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6"};

        AtomicInteger selectedSemester = new AtomicInteger();
        semesterDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("attendanceBelow75Pref",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            switch (which) {
                case 0:
                    editor.putInt("semester", 1);
                    selectedSemester.set(1);
                    break;
                case 1:
                    editor.putInt("semester", 2);
                    selectedSemester.set(2);
                    break;
                case 2:
                    editor.putInt("semester", 3);
                    selectedSemester.set(3);
                    break;
                case 3:
                    editor.putInt("semester", 4);
                    selectedSemester.set(4);
                    break;
                case 4:
                    editor.putInt("semester", 5);
                    selectedSemester.set(5);
                    break;
                case 5:
                    editor.putInt("semester", 6);
                    selectedSemester.set(6);
                    break;
            }
            editor.commit();
            dialog.dismiss();

            subjectFound = false;
            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dsp : snapshot.getChildren()) {
                                if (snapshot.child(dsp.getKey()).child("semester").getValue(Integer.class) == selectedSemester.get()) {
                                    editor.putString("subjectCode", dsp.getKey());
                                    editor.commit();
                                    subjectFound = true;
                                    break;
                                }
                            }

                            if (!subjectFound) {
                                Toast.makeText(getContext(), "You don't teach this semester", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            AlertDialog.Builder classDialog = new AlertDialog.Builder(getContext());
                            classDialog.setTitle("Class");
                            String[] items2 = {"All", "A Division", "B Division", "A1 Practical Batch", "A2 Practical Batch", "A3 Practical Batch", "B1 Practical Batch", "B2 Practical Batch"};
                            int checkedItem2 = 0;
                            classDialog.setSingleChoiceItems(items2, checkedItem2, (dialog2, which2) -> {
                                SharedPreferences sharedPreferences2 = getActivity().getSharedPreferences("attendanceBelow75Pref",MODE_PRIVATE);
                                SharedPreferences.Editor editor2 = sharedPreferences2.edit();
                                switch (which2) {
                                    case 0:
                                        editor2.putString("class", "All");
                                        break;
                                    case 1:
                                        editor2.putString("class", "A");
                                        break;
                                    case 2:
                                        editor2.putString("class", "B");
                                        break;
                                    case 3:
                                        editor2.putString("class", "A1");
                                        break;
                                    case 4:
                                        editor2.putString("class", "A2");
                                        break;
                                    case 5:
                                        editor2.putString("class", "A3");
                                        break;
                                    case 6:
                                        editor2.putString("class", "B1");
                                        break;
                                    case 7:
                                        editor2.putString("class", "B2");
                                        break;
                                }
                                dialog2.dismiss();
                                editor2.commit();
                                startActivity(new Intent(getActivity(), AttendanceBelow75Activity.class));
                            });
                            classDialog.create().show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        semesterDialog.create().show();
    }
}