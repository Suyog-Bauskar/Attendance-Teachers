package com.suyogbauskar.attenteachers;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.pojos.StudentData;
import com.suyogbauskar.attenteachers.pojos.UnitTestMarks;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UnitTestMarksActivity extends AppCompatActivity {

    private Button uploadBtn, deleteBtn;
    private TableLayout table;
    private boolean isFirstRow, isFirstYear;
    private String subjectCodeTeacher, department, selectedDivision;
    private int selectedSemester;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_test_marks);
        setTitle("Unit Test Marks");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
        whichDataToShow();
    }

    private void init() {
        isFirstRow = true;
        SharedPreferences sharedPreferences2 = getSharedPreferences("teacherDataPref", MODE_PRIVATE);
        department = sharedPreferences2.getString("department", "");
        SharedPreferences sharedPreferences = getSharedPreferences("unitTestSemesterAndDivisionPref",MODE_PRIVATE);
        selectedSemester = sharedPreferences.getInt("semester", 0);
        selectedDivision = sharedPreferences.getString("division", "");
        isFirstYear = sharedPreferences.getBoolean("isFirstYear", false);
        subjectCodeTeacher = sharedPreferences.getString("subjectCodeTeacher", "");
        findAllViews();
        uploadBtn.setOnClickListener(view -> uploadFile());
        deleteBtn.setOnClickListener(view -> deleteMarks());
    }

    private void findAllViews() {
        table = findViewById(R.id.table);
        uploadBtn = findViewById(R.id.uploadBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
    }

    private void whichDataToShow() {
        uploadBtn.setVisibility(View.VISIBLE);
        deleteBtn.setVisibility(View.VISIBLE);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showStudentsData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UnitTestMarksActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (isFirstYear) {
            FirebaseDatabase.getInstance().getReference("students_data")
                    .orderByChild("queryStringDivision")
                    .equalTo(department + selectedSemester + selectedDivision)
                    .addListenerForSingleValueEvent(valueEventListener);
        } else {
            FirebaseDatabase.getInstance().getReference("students_data")
                    .orderByChild("queryStringSemester")
                    .equalTo(department + selectedSemester)
                    .addListenerForSingleValueEvent(valueEventListener);
        }
    }

    private void showStudentsData(DataSnapshot snapshot) {
        List<StudentData> tempList = new ArrayList<>();
        isFirstRow = true;

        table.removeAllViews();
        drawTableHeader();

        try {
            for (DataSnapshot ds : snapshot.getChildren()) {
                if (ds.child("isVerified").getValue(Boolean.class)) {
                    tempList.add(new StudentData(ds.child("rollNo").getValue(Integer.class), ds.child("subjects").child(subjectCodeTeacher).child("unitTest1Marks").getValue(Integer.class), ds.child("subjects").child(subjectCodeTeacher).child("unitTest2Marks").getValue(Integer.class), ds.child("firstname").getValue(String.class), ds.child("lastname").getValue(String.class)));
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        tempList.sort(Comparator.comparing(StudentData::getRollNo));
        for (StudentData student : tempList) {
            int unitOneMarks = student.getUnitTest1Marks();
            int unitTwoMarks = student.getUnitTest2Marks();

            if (unitOneMarks == -1 && unitTwoMarks == -1) {
                createTableRow(student.getRollNo(), student.getFirstname() + " " + student.getLastname(), "-", "-");
            } else if (unitOneMarks == -1) {
                createTableRow(student.getRollNo(), student.getFirstname() + " " + student.getLastname(), "-", String.valueOf(unitTwoMarks));
            } else if (unitTwoMarks == -1) {
                createTableRow(student.getRollNo(), student.getFirstname() + " " + student.getLastname(), String.valueOf(unitOneMarks), "-");
            } else {
                createTableRow(student.getRollNo(), student.getFirstname() + " " + student.getLastname(), String.valueOf(unitOneMarks), String.valueOf(unitTwoMarks));
            }
        }
    }

    private void deleteMarks() {
        new SweetAlertDialog(UnitTestMarksActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Both unit test marks will be deleted!")
                .setConfirmText("Delete")
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    if (isFirstYear) {
                        FirebaseDatabase.getInstance().getReference("students_data")
                                .orderByChild("queryStringDivision")
                                .equalTo(department + selectedSemester + selectedDivision)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        deleteMarksAfterClickingOk(snapshot);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(UnitTestMarksActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        FirebaseDatabase.getInstance().getReference("students_data")
                                .orderByChild("queryStringSemester")
                                .equalTo(department + selectedSemester)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        deleteMarksAfterClickingOk(snapshot);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(UnitTestMarksActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .show();
    }

    private void deleteMarksAfterClickingOk(DataSnapshot snapshot) {
        for (DataSnapshot ds : snapshot.getChildren()) {
            ds.child("subjects").child(subjectCodeTeacher).child("unitTest1Marks").getRef().setValue(-1);
            ds.child("subjects").child(subjectCodeTeacher).child("unitTest2Marks").getRef().setValue(-1);
        }
        Toast.makeText(UnitTestMarksActivity.this, "All students marks deleted successfully", Toast.LENGTH_SHORT).show();
        recreate();
    }

    private void drawTableHeader() {
        TableRow tbRow = new TableRow(UnitTestMarksActivity.this);

        TextView tv0 = new TextView(UnitTestMarksActivity.this);
        TextView tv1 = new TextView(UnitTestMarksActivity.this);
        TextView tv2 = new TextView(UnitTestMarksActivity.this);
        TextView tv3 = new TextView(UnitTestMarksActivity.this);

        tv0.setText("Roll No.");
        tv1.setText("Name");
        tv2.setText("Test 1");
        tv3.setText("Test 2");

        tv0.setTypeface(Typeface.DEFAULT_BOLD);
        tv1.setTypeface(Typeface.DEFAULT_BOLD);
        tv2.setTypeface(Typeface.DEFAULT_BOLD);
        tv3.setTypeface(Typeface.DEFAULT_BOLD);

        tv0.setTextSize(18);
        tv1.setTextSize(18);
        tv2.setTextSize(18);
        tv3.setTextSize(18);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);
        tv3.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);
        tv3.setGravity(Gravity.CENTER);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);
        tv3.setTextColor(Color.BLACK);

        tv0.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv1.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv2.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv3.setBackgroundColor(getResources().getColor(R.color.table_header));

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);
        tbRow.addView(tv3);

        table.addView(tbRow);
    }

    private void createTableRow(int rollNo, String name, String unitTest1, String unitTest2) {
        TableRow tbRow = new TableRow(UnitTestMarksActivity.this);

        TextView tv0 = new TextView(UnitTestMarksActivity.this);
        TextView tv1 = new TextView(UnitTestMarksActivity.this);
        TextView tv2 = new TextView(UnitTestMarksActivity.this);
        TextView tv3 = new TextView(UnitTestMarksActivity.this);

        tv0.setText(String.valueOf(rollNo));
        tv1.setText(name);
        tv2.setText(unitTest1);
        tv3.setText(unitTest2);

        tv0.setTextSize(16);
        tv1.setTextSize(16);
        tv2.setTextSize(16);
        tv3.setTextSize(16);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);
        tv3.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);
        tv3.setGravity(Gravity.CENTER);

        tv0.setBackgroundResource(R.drawable.borders);
        tv1.setBackgroundResource(R.drawable.borders);
        tv2.setBackgroundResource(R.drawable.borders);
        tv3.setBackgroundResource(R.drawable.borders);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);
        tv3.setTextColor(Color.BLACK);

        if (isFirstRow) {
            tv0.setBackgroundColor(getResources().getColor(R.color.white));
            tv1.setBackgroundColor(getResources().getColor(R.color.white));
            tv2.setBackgroundColor(getResources().getColor(R.color.white));
            tv3.setBackgroundColor(getResources().getColor(R.color.white));
            isFirstRow = false;
        } else {
            tv0.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv1.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv2.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv3.setBackgroundColor(getResources().getColor(R.color.light_gray));
            isFirstRow = true;
        }

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);
        tbRow.addView(tv3);

        table.addView(tbRow);
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    readCSVFile(result.getData().getData());
                }
            }
    );

    private void readCSVFile(Uri uri) {
        try {
            Map<Integer, UnitTestMarks> unitTestMarksList = new HashMap<>();
            Scanner scanner = new Scanner(new InputStreamReader(getContentResolver().openInputStream(uri)));
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splitted = line.split(",");
                unitTestMarksList.put(Integer.parseInt(splitted[0]), new UnitTestMarks(splitted[1], splitted[2]));
            }

            if (isFirstYear) {
                FirebaseDatabase.getInstance().getReference("students_data")
                        .orderByChild("queryStringDivision")
                        .equalTo(department + selectedSemester + selectedDivision)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                setUnitTestMarksAfterSelectingFile(snapshot, unitTestMarksList);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(UnitTestMarksActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                FirebaseDatabase.getInstance().getReference("students_data")
                        .orderByChild("queryStringSemester")
                        .equalTo(department + selectedSemester)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                setUnitTestMarksAfterSelectingFile(snapshot, unitTestMarksList);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(UnitTestMarksActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setUnitTestMarksAfterSelectingFile(DataSnapshot snapshot, Map<Integer, UnitTestMarks> unitTestMarksList) {
        try {
            for (DataSnapshot ds : snapshot.getChildren()) {
                ds.child("subjects").child(subjectCodeTeacher).child("unitTest1Marks").getRef().setValue(Integer.parseInt(unitTestMarksList.get(ds.child("rollNo").getValue(Integer.class)).getUnitTest1Marks()));
                ds.child("subjects").child(subjectCodeTeacher).child("unitTest2Marks").getRef().setValue(Integer.parseInt(unitTestMarksList.get(ds.child("rollNo").getValue(Integer.class)).getUnitTest2Marks()));
            }
            recreate();
        } catch (Exception e) {
            Toast.makeText(UnitTestMarksActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile() {
        Intent data = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath());
        data.setDataAndType(uri, "text/csv");
        data = Intent.createChooser(data, "Choose unit test marks");
        activityResultLauncher.launch(data);
    }

    @Override
    protected void onDestroy() {
        if (valueEventListener != null) {
            FirebaseDatabase.getInstance().getReference("students_data").removeEventListener(valueEventListener);
        }
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(UnitTestMarksActivity.this, HomeActivity.class));
    }
}