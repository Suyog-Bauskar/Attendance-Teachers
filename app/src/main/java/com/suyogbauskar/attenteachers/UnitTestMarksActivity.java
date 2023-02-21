package com.suyogbauskar.attenteachers;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.molihuan.pathselector.PathSelector;
import com.molihuan.pathselector.entity.FileBean;
import com.molihuan.pathselector.fragment.BasePathSelectFragment;
import com.molihuan.pathselector.fragment.impl.PathSelectFragment;
import com.molihuan.pathselector.listener.CommonItemListener;
import com.molihuan.pathselector.utils.MConstants;
import com.suyogbauskar.attenteachers.pojos.StudentData;
import com.suyogbauskar.attenteachers.pojos.UnitTestMarks;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UnitTestMarksActivity extends AppCompatActivity {

    private Button uploadBtn, deleteBtn;
    private FirebaseUser user;
    private TableLayout table;
    private boolean isFirstRow;
    private String subjectCodeTeacher;
    private PathSelectFragment selector;
    private int selectedSemester;
    private ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_test_marks);
        setTitle("Unit Test Marks");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
        selectSemester();

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // Initialize result data
                        Intent data = result.getData();
                        // check condition
                        if (data != null) {
                            Log.d(TAG, "" + data.getData());
                        }
                    }
                });
    }

    private void selectPDF()
    {
        // Initialize intent
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // set type
        intent.setType("application/pdf");
        // Launch intent
        resultLauncher.launch(intent);
    }

    private void init() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        isFirstRow = true;
        findAllViews();
        uploadBtn.setOnClickListener(view -> selectPDF());
        deleteBtn.setOnClickListener(view -> deleteMarks());

        SharedPreferences sh = getSharedPreferences("unitTestMarksPref", MODE_PRIVATE);
        selectedSemester = sh.getInt("semester", 0);
        if (selectedSemester != 0) {
            SharedPreferences sharedPreferences = getSharedPreferences("unitTestMarksPref", MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putInt("semester", 0);
            myEdit.commit();
            allStudentsData();
        }
    }

    private void findAllViews() {
        table = findViewById(R.id.table);
        uploadBtn = findViewById(R.id.uploadBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
    }

    private void selectSemester() {
        AlertDialog.Builder semesterDialog = new AlertDialog.Builder(UnitTestMarksActivity.this);
        semesterDialog.setTitle("Semester");
        String[] items = {"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6"};
        semesterDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
            which++;
            selectedSemester = which;
            allStudentsData();
            dialog.dismiss();
        });
        semesterDialog.create().show();
    }

    private void allStudentsData() {
        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean rightSemester = false;

                        for (DataSnapshot dsp : snapshot.getChildren()) {
                            if (selectedSemester == snapshot.child(dsp.getKey()).child("semester").getValue(Integer.class)) {
                                rightSemester = true;
                                subjectCodeTeacher = dsp.getKey();
                                break;
                            }
                        }

                        if (!rightSemester) {
                            Toast.makeText(UnitTestMarksActivity.this, "You don't teach this semester", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        uploadBtn.setVisibility(View.VISIBLE);
                        deleteBtn.setVisibility(View.VISIBLE);

                        FirebaseDatabase.getInstance().getReference("students_data")
                                .orderByChild("semester")
                                .equalTo(selectedSemester)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Map<Integer, StudentData> studentDataMap = new TreeMap<>();
                                        isFirstRow = true;

                                        table.removeAllViews();
                                        drawTableHeader();

                                        try {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                if (ds.child("isVerified").getValue(Boolean.class)) {
                                                    studentDataMap.put(ds.child("rollNo").getValue(Integer.class),
                                                            new StudentData(ds.child("rollNo").getValue(Integer.class), ds.child("subjects").child(subjectCodeTeacher).child("unitTest1Marks").getValue(Integer.class), ds.child("subjects").child(subjectCodeTeacher).child("unitTest2Marks").getValue(Integer.class), ds.child("firstname").getValue(String.class), ds.child("lastname").getValue(String.class)));
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.d(TAG, e.getMessage());
                                        }
                                        for (Map.Entry<Integer, StudentData> entry1 : studentDataMap.entrySet()) {
                                            int unitOneMarks = entry1.getValue().getUnitTest1Marks();
                                            int unitTwoMarks = entry1.getValue().getUnitTest2Marks();

                                            if (unitOneMarks == -1 && unitTwoMarks == -1) {
                                                createTableRow(entry1.getValue().getRollNo(), entry1.getValue().getFirstname() + " " + entry1.getValue().getLastname(), "-", "-");
                                            } else if (unitOneMarks == -1) {
                                                createTableRow(entry1.getValue().getRollNo(), entry1.getValue().getFirstname() + " " + entry1.getValue().getLastname(), "-", String.valueOf(unitTwoMarks));
                                            } else if (unitTwoMarks == -1) {
                                                createTableRow(entry1.getValue().getRollNo(), entry1.getValue().getFirstname() + " " + entry1.getValue().getLastname(), String.valueOf(unitOneMarks), "-");
                                            } else {
                                                createTableRow(entry1.getValue().getRollNo(), entry1.getValue().getFirstname() + " " + entry1.getValue().getLastname(), String.valueOf(unitOneMarks), String.valueOf(unitTwoMarks));
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(UnitTestMarksActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UnitTestMarksActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteMarks() {
        new SweetAlertDialog(UnitTestMarksActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Both unit test marks will be deleted!")
                .setConfirmText("Delete")
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    FirebaseDatabase.getInstance().getReference("students_data")
                            .orderByChild("semester")
                            .equalTo(selectedSemester)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        ds.child("subjects").child(subjectCodeTeacher).child("unitTest1Marks").getRef().setValue(-1);
                                        ds.child("subjects").child(subjectCodeTeacher).child("unitTest2Marks").getRef().setValue(-1);
                                    }
                                    Toast.makeText(UnitTestMarksActivity.this, "All students marks deleted successfully", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(UnitTestMarksActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .show();
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

    private void selectFileForUpdatingTestMarks() {
        selector = PathSelector.build(this, MConstants.BUILD_ACTIVITY)
                .setRootPath("/storage/emulated/0/Download/")
                .setRequestCode(635)
                .setShowFileTypes("csv")
                .setSelectFileTypes("csv")
                .setMaxCount(1)
                .setShowTitlebarFragment(false)
                .setShowTabbarFragment(false)
                .setAlwaysShowHandleFragment(true)
                .setHandleItemListeners(
                        new CommonItemListener("Cancel") {
                            @Override
                            public boolean onClick(View v, TextView tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                                if (selectedFiles.size() == 1) {
                                    pathSelectFragment.openCloseMultipleMode(false);
                                } else {
                                    restartActivity();
                                }
                                return false;
                            }
                        },
                        new CommonItemListener("OK") {
                            @Override
                            public boolean onClick(View v, TextView tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) {
                                if (selectedFiles.size() == 1) {
                                    SharedPreferences sharedPreferences = getSharedPreferences("unitTestMarksPref", MODE_PRIVATE);
                                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                    myEdit.putInt("semester", selectedSemester);
                                    myEdit.commit();
                                    readCSVFile(selectedFiles.get(0).getPath());
                                    restartActivity();
                                }
                                return false;
                            }
                        }
                )
                .show();
    }

    private void restartActivity() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    private void readCSVFile(String path) {
        try {
            Map<Integer, UnitTestMarks> unitTestMarksList = new HashMap<>();
            Scanner scanner = new Scanner(new File(path));
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splitted = line.split(",");
                unitTestMarksList.put(Integer.parseInt(splitted[0]), new UnitTestMarks(splitted[1], splitted[2]));
            }

            FirebaseDatabase.getInstance().getReference("students_data")
                    .orderByChild("semester")
                    .equalTo(selectedSemester)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ds.child("subjects").child(subjectCodeTeacher).child("unitTest1Marks").getRef().setValue(Integer.parseInt(unitTestMarksList.get(ds.child("rollNo").getValue(Integer.class)).getUnitTest1Marks()));
                                ds.child("subjects").child(subjectCodeTeacher).child("unitTest2Marks").getRef().setValue(Integer.parseInt(unitTestMarksList.get(ds.child("rollNo").getValue(Integer.class)).getUnitTest2Marks()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(UnitTestMarksActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (selector != null && selector.onBackPressed()) {
            return;
        }
        startActivity(new Intent(UnitTestMarksActivity.this, HomeActivity.class));
    }
}