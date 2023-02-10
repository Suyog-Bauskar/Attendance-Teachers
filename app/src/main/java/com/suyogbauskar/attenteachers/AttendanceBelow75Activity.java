package com.suyogbauskar.attenteachers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kal.rackmonthpicker.RackMonthPicker;
import com.suyogbauskar.attenteachers.pojos.StudentData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceBelow75Activity extends AppCompatActivity {

    private TableLayout table;
    private boolean isFirstRow;
    private int startMonth, startYear, endMonth, endYear;
    private String subjectCode, whichClass;
    private Map<String, Float> studentsBelow75List;
    private Map<String, StudentData> allStudentsA;
    private Map<String, StudentData> allStudentsB;
    private Map<String, StudentData> allStudentsA1;
    private Map<String, StudentData> allStudentsA2;
    private Map<String, StudentData> allStudentsA3;
    private Map<String, StudentData> allStudentsB1;
    private Map<String, StudentData> allStudentsB2;
    private int semester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_below75);
        setTitle("Statistics");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
    }

    private void init() {
        studentsBelow75List = new HashMap<>();
        allStudentsA = new HashMap<>();
        allStudentsA1 = new HashMap<>();
        allStudentsA2 = new HashMap<>();
        allStudentsA3 = new HashMap<>();
        allStudentsB = new HashMap<>();
        allStudentsB1 = new HashMap<>();
        allStudentsB2 = new HashMap<>();
        SharedPreferences sharedPreferences = getSharedPreferences("attendanceBelow75Pref", MODE_PRIVATE);
        whichClass = sharedPreferences.getString("class", "");
        semester = sharedPreferences.getInt("semester", 0);
        subjectCode = sharedPreferences.getString("subjectCode", "");
        startMonth = sharedPreferences.getInt("startMonth", 0);
        startYear = sharedPreferences.getInt("startYear", 0);
        endMonth = sharedPreferences.getInt("endMonth", 0);
        endYear = sharedPreferences.getInt("endYear", 0);

        table = findViewById(R.id.table);
        isFirstRow = true;
        drawTableHeader();
        getAllStudentsBasedOnDivision();
    }

    private void getMonthRange() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Select Range");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView startDateView = new TextView(this);
        startDateView.setGravity(Gravity.CENTER_HORIZONTAL);
        startDateView.setTextColor(Color.BLACK);
        startDateView.setTextSize(20);

        TextView endDateView = new TextView(this);
        endDateView.setGravity(Gravity.CENTER_HORIZONTAL);
        endDateView.setTextColor(Color.BLACK);
        endDateView.setTextSize(20);

        Button startingMonthBtn = new Button(this);
        startingMonthBtn.setText("Starting Month");
        startingMonthBtn.setTextSize(20);
        startingMonthBtn.setAllCaps(false);
        startingMonthBtn.setHeight(50);
        startingMonthBtn.setOnClickListener(view -> new RackMonthPicker(AttendanceBelow75Activity.this)
                .setLocale(Locale.ENGLISH)
                .setPositiveButton((month, startDate, endDate, year, monthLabel) -> {
                    startMonth = month;
                    startYear = year;
                    startDateView.setText(monthLabel);
                })
                .setNegativeButton(dialog -> startActivity(new Intent(AttendanceBelow75Activity.this, HomeActivity.class))).show());
        layout.addView(startingMonthBtn);
        layout.addView(startDateView);

        Button endingMonthBtn = new Button(this);
        endingMonthBtn.setText("Ending Month");
        endingMonthBtn.setTextSize(20);
        endingMonthBtn.setAllCaps(false);
        startingMonthBtn.setHeight(50);
        endingMonthBtn.setOnClickListener(view -> new RackMonthPicker(AttendanceBelow75Activity.this)
                .setLocale(Locale.ENGLISH)
                .setPositiveButton((month, startDate, endDate, year, monthLabel) -> {
                    endMonth = month;
                    endYear = year;
                    endDateView.setText(monthLabel);
                })
                .setNegativeButton(dialog -> startActivity(new Intent(AttendanceBelow75Activity.this, HomeActivity.class))).show());
        layout.addView(endingMonthBtn);
        layout.addView(endDateView);

        alert.setView(layout);

        alert.setPositiveButton("Ok", (dialogInterface, i) -> {
            if (startMonth == 0 || startYear == 0) {
                Toast.makeText(this, "Select starting month", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AttendanceBelow75Activity.this, HomeActivity.class));
            } else if (endMonth == 0 || endYear == 0) {
                Toast.makeText(this, "Select ending month", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AttendanceBelow75Activity.this, HomeActivity.class));
            } else if (endYear - startYear > 1) {
                Toast.makeText(this, "Range too long", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AttendanceBelow75Activity.this, HomeActivity.class));
            } else {
                String startingPath = "CO" + semester + "-";
                mainCode(startingPath + "A", allStudentsA, "A", "-");
                mainCode(startingPath + "B", allStudentsB, "B", "-");
                mainCode(startingPath + "A1", allStudentsA1, "A", "1");
                mainCode(startingPath + "A2", allStudentsA2, "A", "2");
                mainCode(startingPath + "A3", allStudentsA3, "A", "3");
                mainCode(startingPath + "B1", allStudentsB1, "B", "1");
                mainCode(startingPath + "B2", allStudentsB2, "B", "2");
            }
        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> startActivity(new Intent(AttendanceBelow75Activity.this, HomeActivity.class)));

        alert.show();
    }

    private void getAllStudentsBasedOnDivision() {
        FirebaseDatabase.getInstance().getReference("students_data")
                .orderByChild("semester")
                .equalTo(semester)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        getMonthRange();
                        for (DataSnapshot dsp : snapshot.getChildren()) {
                            String firstname = dsp.child("firstname").getValue(String.class);
                            String lastname = dsp.child("lastname").getValue(String.class);
                            String division = dsp.child("division").getValue(String.class);
                            int batch = dsp.child("batch").getValue(Integer.class);
                            int rollNo = dsp.child("rollNo").getValue(Integer.class);

                            if (division.equals("A")) {
                                allStudentsA.put(dsp.getKey(), new StudentData(rollNo, firstname, lastname));
                                if (batch == 1) {
                                    allStudentsA1.put(dsp.getKey(), new StudentData(rollNo, firstname, lastname));
                                } else if (batch == 2) {
                                    allStudentsA2.put(dsp.getKey(), new StudentData(rollNo, firstname, lastname));
                                } else if (batch == 3) {
                                    allStudentsA3.put(dsp.getKey(), new StudentData(rollNo, firstname, lastname));
                                }
                            } else if (division.equals("B")) {
                                allStudentsB.put(dsp.getKey(), new StudentData(rollNo, firstname, lastname));
                                if (batch == 1) {
                                    allStudentsB1.put(dsp.getKey(), new StudentData(rollNo, firstname, lastname));
                                } else if (batch == 2) {
                                    allStudentsB2.put(dsp.getKey(), new StudentData(rollNo, firstname, lastname));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AttendanceBelow75Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mainCode(String attendanceOf, Map<String, StudentData> allStudents, String whichDivision, String batch) {
        List<String> monthsName = new ArrayList<>();

        if (startYear == endYear) {
            for (int i = startMonth; i <= endMonth; i++) {
                switch (i) {
                    case 1:
                        monthsName.add("January");
                        break;

                    case 2:
                        monthsName.add("February");
                        break;

                    case 3:
                        monthsName.add("March");
                        break;

                    case 4:
                        monthsName.add("April");
                        break;

                    case 5:
                        monthsName.add("May");
                        break;

                    case 6:
                        monthsName.add("June");
                        break;

                    case 7:
                        monthsName.add("July");
                        break;

                    case 8:
                        monthsName.add("August");
                        break;

                    case 9:
                        monthsName.add("September");
                        break;

                    case 10:
                        monthsName.add("October");
                        break;

                    case 11:
                        monthsName.add("November");
                        break;

                    case 12:
                        monthsName.add("December");
                        break;

                    default:
                        monthsName.add("");
                }
            }

            FirebaseDatabase.getInstance().getReference("attendance/" + attendanceOf + "/")
                    .child(subjectCode).child(String.valueOf(startYear))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                return;
                            }
                            Map<String, Map<String, Map<String, Object>>> allDataOfMonths = (Map<String, Map<String, Map<String, Object>>>) snapshot.getValue();
                            Map<String, Integer> studentsAttendance = new HashMap<>();
                            long totalLectures = 0;

                            for (Map.Entry<String, Map<String, Map<String, Object>>> entry1 : allDataOfMonths.entrySet()) {
                                //Month
                                if (monthsName.contains(entry1.getKey())) {
                                    for (Map.Entry<String, Map<String, Object>> entry2 : entry1.getValue().entrySet()) {
                                        //Day name
                                        totalLectures += 1;
                                        for (Map.Entry<String, Object> entry3 : entry2.getValue().entrySet()) {
                                            //UID
                                            if (studentsAttendance.containsKey(entry3.getKey())) {
                                                studentsAttendance.put(entry3.getKey(), studentsAttendance.get(entry3.getKey()) + 1);
                                            } else {
                                                studentsAttendance.put(entry3.getKey(), 1);
                                            }
                                        }
                                    }
                                }
                            }

                            calculatePercentageAndCreateRows(studentsAttendance, totalLectures, allStudents, whichDivision, batch);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(AttendanceBelow75Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            for (int i = startMonth; i <= 12; i++) {
                switch (i) {
                    case 1:
                        monthsName.add("January");
                        break;

                    case 2:
                        monthsName.add("February");
                        break;

                    case 3:
                        monthsName.add("March");
                        break;

                    case 4:
                        monthsName.add("April");
                        break;

                    case 5:
                        monthsName.add("May");
                        break;

                    case 6:
                        monthsName.add("June");
                        break;

                    case 7:
                        monthsName.add("July");
                        break;

                    case 8:
                        monthsName.add("August");
                        break;

                    case 9:
                        monthsName.add("September");
                        break;

                    case 10:
                        monthsName.add("October");
                        break;

                    case 11:
                        monthsName.add("November");
                        break;

                    case 12:
                        monthsName.add("December");
                        break;

                    default:
                        monthsName.add("");
                }
            }
            final long[] totalLectures = {0};
            FirebaseDatabase.getInstance().getReference("attendance/" + attendanceOf + "/")
                    .child(subjectCode).child(String.valueOf(startYear))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                return;
                            }
                            Map<String, Map<String, Map<String, Object>>> allDataOfMonths = (Map<String, Map<String, Map<String, Object>>>) snapshot.getValue();
                            Map<String, Integer> studentsAttendance = new HashMap<>();

                            for (Map.Entry<String, Map<String, Map<String, Object>>> entry1 : allDataOfMonths.entrySet()) {
                                //Month
                                if (monthsName.contains(entry1.getKey())) {
                                    for (Map.Entry<String, Map<String, Object>> entry2 : entry1.getValue().entrySet()) {
                                        //Day name
                                        totalLectures[0] += 1;
                                        for (Map.Entry<String, Object> entry3 : entry2.getValue().entrySet()) {
                                            //UID
                                            if (studentsAttendance.containsKey(entry3.getKey())) {
                                                studentsAttendance.put(entry3.getKey(), studentsAttendance.get(entry3.getKey()) + 1);
                                            } else {
                                                studentsAttendance.put(entry3.getKey(), 1);
                                            }
                                        }
                                    }
                                }
                            }

                            if (monthsName.size() > 0) {
                                monthsName.clear();
                            }

                            for (int i = 1; i <= endMonth; i++) {
                                switch (i) {
                                    case 1:
                                        monthsName.add("January");
                                        break;

                                    case 2:
                                        monthsName.add("February");
                                        break;

                                    case 3:
                                        monthsName.add("March");
                                        break;

                                    case 4:
                                        monthsName.add("April");
                                        break;

                                    case 5:
                                        monthsName.add("May");
                                        break;

                                    case 6:
                                        monthsName.add("June");
                                        break;

                                    case 7:
                                        monthsName.add("July");
                                        break;

                                    case 8:
                                        monthsName.add("August");
                                        break;

                                    case 9:
                                        monthsName.add("September");
                                        break;

                                    case 10:
                                        monthsName.add("October");
                                        break;

                                    case 11:
                                        monthsName.add("November");
                                        break;

                                    case 12:
                                        monthsName.add("December");
                                        break;

                                    default:
                                        monthsName.add("");
                                }
                            }

                            FirebaseDatabase.getInstance().getReference("attendance/" + attendanceOf + "/")
                                    .child(subjectCode).child(String.valueOf(endYear))
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (!snapshot.exists()) {
                                                return;
                                            }
                                            Map<String, Map<String, Map<String, Object>>> allDataOfMonths = (Map<String, Map<String, Map<String, Object>>>) snapshot.getValue();

                                            for (Map.Entry<String, Map<String, Map<String, Object>>> entry1 : allDataOfMonths.entrySet()) {
                                                //Month
                                                if (monthsName.contains(entry1.getKey())) {
                                                    for (Map.Entry<String, Map<String, Object>> entry2 : entry1.getValue().entrySet()) {
                                                        //Day name
                                                        totalLectures[0] += 1;
                                                        for (Map.Entry<String, Object> entry3 : entry2.getValue().entrySet()) {
                                                            //UID
                                                            if (studentsAttendance.containsKey(entry3.getKey())) {
                                                                studentsAttendance.put(entry3.getKey(), studentsAttendance.get(entry3.getKey()) + 1);
                                                            } else {
                                                                studentsAttendance.put(entry3.getKey(), 1);
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            calculatePercentageAndCreateRows(studentsAttendance, totalLectures[0], allStudents, whichDivision, batch);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(AttendanceBelow75Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(AttendanceBelow75Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void calculatePercentageAndCreateRows(Map<String, Integer> studentsAttendance, long totalLectures, Map<String, StudentData> allStudents, String whichDivision, String batch) {
        for (Map.Entry<String, Integer> entry : studentsAttendance.entrySet()) {

            float percentage = entry.getValue() / (float) totalLectures;
            percentage *= 100;

            if (percentage < 75.00f) {
                studentsBelow75List.put(entry.getKey(), percentage);
            }
        }

        FirebaseDatabase.getInstance().getReference("students_data")
                .orderByChild("semester")
                .equalTo(semester)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (Map.Entry<String, Float> entry : studentsBelow75List.entrySet()) {
                            String firstname = allStudents.get(entry.getKey()).getFirstname();
                            String lastname = allStudents.get(entry.getKey()).getLastname();
                            int rollNo = allStudents.get(entry.getKey()).getRollNo();

                            createTableRow(whichDivision, batch, rollNo, firstname + " " + lastname, entry.getValue());
                        }

                        List<String> tempAllStudent = new ArrayList<>(allStudents.keySet());
                        List<String> tempStudentsAttendance = new ArrayList<>(studentsAttendance.keySet());
                        tempAllStudent.removeAll(tempStudentsAttendance);

                        for (String uid: tempAllStudent) {
                            String firstname = allStudents.get(uid).getFirstname();
                            String lastname = allStudents.get(uid).getLastname();
                            int rollNo = allStudents.get(uid).getRollNo();

                            createTableRow(whichDivision, batch, rollNo, firstname + " " + lastname, 0);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AttendanceBelow75Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void drawTableHeader() {
        TableRow tbRow = new TableRow(getApplicationContext());

        TextView tv0 = new TextView(getApplicationContext());
        TextView tv1 = new TextView(getApplicationContext());
        TextView tv2 = new TextView(getApplicationContext());
        TextView tv3 = new TextView(getApplicationContext());
        TextView tv4 = new TextView(getApplicationContext());

        tv0.setText("Division");
        tv1.setText("Batch");
        tv2.setText("Roll No.");
        tv3.setText("Name");
        tv4.setText("Percentage");

        tv0.setTypeface(Typeface.DEFAULT_BOLD);
        tv1.setTypeface(Typeface.DEFAULT_BOLD);
        tv2.setTypeface(Typeface.DEFAULT_BOLD);
        tv3.setTypeface(Typeface.DEFAULT_BOLD);
        tv4.setTypeface(Typeface.DEFAULT_BOLD);

        tv0.setTextSize(18);
        tv1.setTextSize(18);
        tv2.setTextSize(18);
        tv3.setTextSize(18);
        tv4.setTextSize(18);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);
        tv3.setPadding(30, 30, 15, 30);
        tv4.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);
        tv3.setGravity(Gravity.CENTER);
        tv4.setGravity(Gravity.CENTER);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);
        tv3.setTextColor(Color.BLACK);
        tv4.setTextColor(Color.BLACK);

        tv0.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv1.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv2.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv3.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv4.setBackgroundColor(getResources().getColor(R.color.table_header));

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);
        tbRow.addView(tv3);
        tbRow.addView(tv4);

        table.addView(tbRow);
    }

    private void createTableRow(String division, String batch, int rollNo, String name, float percentage) {
        TableRow tbRow = new TableRow(getApplicationContext());

        tbRow.setTag(rollNo);

        TextView tv0 = new TextView(getApplicationContext());
        TextView tv1 = new TextView(getApplicationContext());
        TextView tv2 = new TextView(getApplicationContext());
        TextView tv3 = new TextView(getApplicationContext());
        TextView tv4 = new TextView(getApplicationContext());

        tv0.setText(division);
        tv1.setText(batch);
        tv2.setText(String.valueOf(rollNo));
        tv3.setText(name);
        tv4.setText(String.valueOf(percentage));

        tv0.setTextSize(16);
        tv1.setTextSize(16);
        tv2.setTextSize(16);
        tv3.setTextSize(16);
        tv4.setTextSize(16);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);
        tv3.setPadding(30, 30, 15, 30);
        tv4.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);
        tv3.setGravity(Gravity.CENTER);
        tv4.setGravity(Gravity.CENTER);

        tv0.setBackgroundResource(R.drawable.borders);
        tv1.setBackgroundResource(R.drawable.borders);
        tv2.setBackgroundResource(R.drawable.borders);
        tv3.setBackgroundResource(R.drawable.borders);
        tv4.setBackgroundResource(R.drawable.borders);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);
        tv3.setTextColor(Color.BLACK);
        tv4.setTextColor(Color.BLACK);

        if (isFirstRow) {
            tv0.setBackgroundColor(getResources().getColor(R.color.white));
            tv1.setBackgroundColor(getResources().getColor(R.color.white));
            tv2.setBackgroundColor(getResources().getColor(R.color.white));
            tv3.setBackgroundColor(getResources().getColor(R.color.white));
            tv4.setBackgroundColor(getResources().getColor(R.color.white));
            isFirstRow = false;
        } else {
            tv0.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv1.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv2.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv3.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv4.setBackgroundColor(getResources().getColor(R.color.light_gray));
            isFirstRow = true;
        }

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);
        tbRow.addView(tv3);
        tbRow.addView(tv4);

        table.addView(tbRow);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AttendanceBelow75Activity.this, UtilityActivity.class));
    }
}