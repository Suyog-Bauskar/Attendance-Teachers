package com.suyogbauskar.attenteachers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.suyogbauskar.attenteachers.excelfiles.CreateExcelFileOfAttendance;
import com.suyogbauskar.attenteachers.pojos.StudentData;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UtilityActivity extends AppCompatActivity {

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Button excelBtn, attendanceBelow75Btn, subjectsBtn, uploadTimetableBtn, removeLastSemesterStudentsBtn, updateAllStudentsDetailsBtn;
    private boolean subjectFound, isAdmin;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility);
        setTitle("Utility");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
    }

    private void init() {
        findAllViews();
        setOnClickListeners();

        SharedPreferences sp = getSharedPreferences("teacherDataPref", MODE_PRIVATE);
        isAdmin = sp.getBoolean("isAdmin", false);
        if (isAdmin) {
            layout.setVisibility(View.VISIBLE);
        }
    }

    private void findAllViews() {
        excelBtn = findViewById(R.id.excelBtn);
        attendanceBelow75Btn = findViewById(R.id.attendanceBelow75Btn);
        subjectsBtn = findViewById(R.id.subjectsBtn);
        uploadTimetableBtn = findViewById(R.id.uploadTimetableBtn);
        removeLastSemesterStudentsBtn = findViewById(R.id.removeLastSemesterStudentsBtn);
        updateAllStudentsDetailsBtn = findViewById(R.id.updateAllStudentsDetailsBtn);
        layout = findViewById(R.id.layout);
    }

    private void setOnClickListeners() {
        excelBtn.setOnClickListener(view -> showDialogForCreatingExcelFile());
        attendanceBelow75Btn.setOnClickListener(view -> showDialogForFindingStudentsBelow75());
        subjectsBtn.setOnClickListener(view -> startActivity(new Intent(UtilityActivity.this, SubjectsActivity.class)));
        uploadTimetableBtn.setOnClickListener(view -> showDialogOfSemester());
        removeLastSemesterStudentsBtn.setOnClickListener(view -> removeLastSemesterStudents());
        updateAllStudentsDetailsBtn.setOnClickListener(view -> readCSVFile());
    }

    private void removeLastSemesterStudents() {
        new SweetAlertDialog(UtilityActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("6th semester students will be removed")
                .setConfirmText("Remove")
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    FirebaseDatabase.getInstance().getReference("students_data")
                            .orderByChild("semester")
                            .equalTo(6)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dsp : snapshot.getChildren()) {
                                        dsp.child("semester").getRef().setValue(LocalDate.now().getYear());
                                    }
                                    Toast.makeText(UtilityActivity.this, "Last semester students removed successfully", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(UtilityActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setCancelButton("Cancel", SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    private void readCSVFile() {
        try {
            Map<Long, StudentData> studentsDetailsList = new HashMap<>();
            int rollNo, batch, semester;
            long enrollNo;
            String division;
            Scanner scanner = new Scanner(new File(getApplicationContext().getExternalFilesDir(null), "Students_Details.csv"));
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splitted = line.split(",");

                enrollNo = Long.parseLong(splitted[0]);
                rollNo = Integer.parseInt(splitted[1]);
                semester = Integer.parseInt(splitted[2]);
                division = splitted[3].toUpperCase();
                batch = Integer.parseInt(splitted[4]);

                if (String.valueOf(enrollNo).length() != 10) {
                    Toast.makeText(this, "Invalid enrollment no.", Toast.LENGTH_SHORT).show();
                    break;
                } else if (String.valueOf(rollNo).length() > 3) {
                    Toast.makeText(this, "Invalid roll no.", Toast.LENGTH_SHORT).show();
                    break;
                } else if (semester <= 0 || semester > 6) {
                    Toast.makeText(this, "Invalid semester", Toast.LENGTH_SHORT).show();
                    break;
                } else if (!(division.equals("A") || division.equals("B"))) {
                    Toast.makeText(this, "Invalid division", Toast.LENGTH_SHORT).show();
                    break;
                } else if (batch <= 0 || batch > 3) {
                    Toast.makeText(this, "Invalid batch", Toast.LENGTH_SHORT).show();
                    break;
                } else {
                    studentsDetailsList.put(enrollNo, new StudentData(rollNo, batch, semester, enrollNo, division));
                }
            }

            FirebaseDatabase.getInstance().getReference("students_data")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ds.child("rollNo").getRef().setValue(studentsDetailsList.get(ds.child("enrollNo").getValue(Long.class)).getRollNo());
                                ds.child("batch").getRef().setValue(studentsDetailsList.get(ds.child("enrollNo").getValue(Long.class)).getBatch());
                                ds.child("semester").getRef().setValue(studentsDetailsList.get(ds.child("enrollNo").getValue(Long.class)).getSemester());
                                ds.child("division").getRef().setValue(studentsDetailsList.get(ds.child("enrollNo").getValue(Long.class)).getDivision());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(UtilityActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Number Expected", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadTimetable(int selectedSemester) {
        try {
            String name = "CO" + selectedSemester + "_Timetable.csv";
            File file = new File(getApplicationContext().getExternalFilesDir(null), name);
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("text/csv")
                    .build();
            FirebaseStorage.getInstance().getReference().child("CO").child("Students_Timetables").child(name)
                    .putStream(new FileInputStream(file), metadata)
                    .addOnSuccessListener(taskSnapshot -> Toast.makeText(UtilityActivity.this, "Timetable uploaded successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(UtilityActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            Toast.makeText(UtilityActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDialogOfSemester() {
        AlertDialog.Builder semesterDialog = new AlertDialog.Builder(UtilityActivity.this);
        semesterDialog.setTitle("Semester");
        String[] items = {"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6"};
        semesterDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
            switch (which) {
                case 0:
                    uploadTimetable(1);
                    break;
                case 1:
                    uploadTimetable(2);
                    break;
                case 2:
                    uploadTimetable(3);
                    break;
                case 3:
                    uploadTimetable(4);
                    break;
                case 4:
                    uploadTimetable(5);
                    break;
                case 5:
                    uploadTimetable(6);
                    break;
            }
            dialog.dismiss();
        });
        semesterDialog.create().show();
    }

    private void showDialogForCreatingExcelFile() {
        AlertDialog.Builder semesterDialog = new AlertDialog.Builder(UtilityActivity.this);
        semesterDialog.setTitle("Semester");
        String[] items = {"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6"};

        AtomicInteger selectedSemester = new AtomicInteger();
        semesterDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
            SharedPreferences sharedPreferences = getSharedPreferences("excelValuesPref", MODE_PRIVATE);
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
                                Toast.makeText(UtilityActivity.this, "You don't teach this semester", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            AlertDialog.Builder yearDialog = new AlertDialog.Builder(UtilityActivity.this);
                            yearDialog.setTitle("Year");
                            String[] items2 = {"2023", "2024", "2025", "2026", "2027"};

                            yearDialog.setSingleChoiceItems(items2, -1, (dialog2, which2) -> {
                                SharedPreferences sharedPreferences2 = getSharedPreferences("excelValuesPref", MODE_PRIVATE);
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
                                Toast.makeText(UtilityActivity.this, "Creating Excel File...", Toast.LENGTH_SHORT).show();
                                dialog2.dismiss();
                                editor2.commit();
                                startService(new Intent(UtilityActivity.this, CreateExcelFileOfAttendance.class));
                            });
                            yearDialog.create().show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(UtilityActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        semesterDialog.create().show();
    }

    private void showDialogForFindingStudentsBelow75() {
        AlertDialog.Builder semesterDialog = new AlertDialog.Builder(UtilityActivity.this);
        semesterDialog.setTitle("Semester");
        String[] items = {"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6"};

        AtomicInteger selectedSemester = new AtomicInteger();
        semesterDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
            SharedPreferences sharedPreferences = getSharedPreferences("attendanceBelow75Pref", MODE_PRIVATE);
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
                                Toast.makeText(UtilityActivity.this, "You don't teach this semester", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            AlertDialog.Builder classDialog = new AlertDialog.Builder(UtilityActivity.this);
                            classDialog.setTitle("Class");
                            String[] items2 = {"All", "A Division", "B Division", "A1 Practical Batch", "A2 Practical Batch", "A3 Practical Batch", "B1 Practical Batch", "B2 Practical Batch"};
                            int checkedItem2 = 0;
                            classDialog.setSingleChoiceItems(items2, checkedItem2, (dialog2, which2) -> {
                                SharedPreferences sharedPreferences2 = getSharedPreferences("attendanceBelow75Pref", MODE_PRIVATE);
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
                                startActivity(new Intent(UtilityActivity.this, AttendanceBelow75Activity.class));
                            });
                            classDialog.create().show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(UtilityActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        semesterDialog.create().show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(UtilityActivity.this, HomeActivity.class));
    }
}