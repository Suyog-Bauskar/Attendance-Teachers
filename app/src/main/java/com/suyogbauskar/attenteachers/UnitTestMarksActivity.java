package com.suyogbauskar.attenteachers;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.pojos.StudentData;
import com.suyogbauskar.attenteachers.pojos.UnitTestMarks;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class UnitTestMarksActivity extends AppCompatActivity {

    private Button selectSemesterBtn, uploadBtn, deleteBtn;
    private int selectedSemester;
    private FirebaseUser user;
    private TableLayout table;
    private boolean isFirstRow;
    private String subjectCodeTeacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_test_marks);
        setTitle("Unit Test Marks");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
    }

    private void init() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        isFirstRow = true;
        findAllViews();
        selectSemesterBtn.setOnClickListener(view -> showSemesterAndUnitTestPickerDialog());
        uploadBtn.setOnClickListener(view -> uploadFile());
        deleteBtn.setOnClickListener(view -> deleteMarks());
    }

    private void findAllViews() {
        selectSemesterBtn = findViewById(R.id.selectSemesterBtn);
        table = findViewById(R.id.table);
        uploadBtn = findViewById(R.id.uploadBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
    }

    private void showSemesterAndUnitTestPickerDialog() {
        PopupMenu semesterMenu = new PopupMenu(UnitTestMarksActivity.this, selectSemesterBtn);
        semesterMenu.getMenu().add(Menu.NONE, 1, 1, "Semester 1");
        semesterMenu.getMenu().add(Menu.NONE, 2, 2, "Semester 2");
        semesterMenu.getMenu().add(Menu.NONE, 3, 3, "Semester 3");
        semesterMenu.getMenu().add(Menu.NONE, 4, 4, "Semester 4");
        semesterMenu.getMenu().add(Menu.NONE, 5, 5, "Semester 5");
        semesterMenu.getMenu().add(Menu.NONE, 6, 6, "Semester 6");
        semesterMenu.show();

        semesterMenu.setOnMenuItemClickListener(item -> {
            FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean rightSemester = false;

                            for (DataSnapshot dsp : snapshot.getChildren()) {
                                if (item.getItemId() == snapshot.child(dsp.getKey()).child("semester").getValue(Integer.class)) {
                                    rightSemester = true;
                                    subjectCodeTeacher = dsp.getKey();
                                    break;
                                }
                            }

                            if (!rightSemester) {
                                Toast.makeText(UnitTestMarksActivity.this, "You don't teach this semester", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            selectedSemester = item.getItemId();
                            selectSemesterBtn.setVisibility(View.GONE);
                            uploadBtn.setVisibility(View.VISIBLE);
                            deleteBtn.setVisibility(View.VISIBLE);

                            FirebaseDatabase.getInstance().getReference("students_data")
                                    .orderByChild("semester")
                                    .equalTo(selectedSemester)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Map<Integer, StudentData> tempMap = new TreeMap<>();
                                            isFirstRow = true;

                                            table.removeAllViews();
                                            drawTableHeader();

                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                if (ds.child("isVerified").getValue(Boolean.class)) {
                                                    tempMap.put(ds.child("rollNo").getValue(Integer.class),
                                                            new StudentData(ds.child("rollNo").getValue(Integer.class), ds.child("subjects").child(subjectCodeTeacher).child("unitTest1Marks").getValue(Integer.class), ds.child("subjects").child(subjectCodeTeacher).child("unitTest2Marks").getValue(Integer.class), ds.child("firstname").getValue(String.class), ds.child("lastname").getValue(String.class)));
                                                }
                                            }
                                            for (Map.Entry<Integer, StudentData> entry1 : tempMap.entrySet()) {
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
            return true;
        });
    }

    private void uploadFile() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UnitTestMarksActivity.this);
        alertDialog.setTitle("Choose Unit Test");
        final String[] listItems = new String[]{"Unit Test 1", "Unit Test 2"};
        alertDialog.setSingleChoiceItems(listItems, -1, (dialog, which) -> {
            int selectedUnitTest;

            if (which == 0) {
                selectedUnitTest = 1;
            } else {
                selectedUnitTest = 2;
            }

            openFilePickerDialog(selectedUnitTest);

            dialog.dismiss();
        });
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> {});
        alertDialog.create().show();
    }

    private void deleteMarks() {

    }

    private void drawTableHeader() {
        TableRow tbRow = new TableRow(UnitTestMarksActivity.this);

        TextView tv0 = new TextView(UnitTestMarksActivity.this);
        TextView tv1 = new TextView(UnitTestMarksActivity.this);
        TextView tv2 = new TextView(UnitTestMarksActivity.this);
        TextView tv3 = new TextView(UnitTestMarksActivity.this);

        tv0.setText("Roll No.");
        tv1.setText("Name");
        tv2.setText("Unit Test 1");
        tv3.setText("Unit Test 2");

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
            List<UnitTestMarks> unitTestMarksList = new ArrayList<>();
            InputStream inputStream = getContentResolver().openInputStream(uri);
            InputStreamReader isr = new InputStreamReader(inputStream);

            Scanner scanner = new Scanner(isr);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splitted = line.split(",");
                unitTestMarksList.add(new UnitTestMarks(splitted[0], splitted[1], splitted[2]));
//                for (String s : splitted) {
//                    Log.d(TAG, s);
//                }
            }
            for (UnitTestMarks mark: unitTestMarksList) {
                Log.d(TAG, "Roll No: " + mark.getRollNo() + ", First: " + mark.getUnitTest1Marks() + ", Second: " + mark.getUnitTest2Marks());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }
    }

    private void openFilePickerDialog(int whichUnitTest) {
        Intent data = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath());
        data.setDataAndType(uri, "text/csv");
        data = Intent.createChooser(data, "Choose unit test " + whichUnitTest + " marks");
        activityResultLauncher.launch(data);
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