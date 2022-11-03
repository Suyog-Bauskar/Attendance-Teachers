package com.suyogbauskar.attenteachers;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kal.rackmonthpicker.RackMonthPicker;
import com.suyogbauskar.attenteachers.pojos.Student;
import com.suyogbauskar.attenteachers.utils.ProgressDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceBelow75Activity extends AppCompatActivity {

    private FirebaseUser user;
    private TableLayout table;
    private boolean isFirstRow;
    private int startMonth = 0, startYear = 0, endMonth = 0, endYear = 0;
    private DatabaseReference dbRef;
    private long totalLectures = 0;
    private Map<String, Integer> studentsAttendance;
    private Map<String, Map<String, Map<String, Object>>> allDataOfMonths;
    private String subjectCode;
    private Map<String, Float> studentsBelow75List;
    private Map<String, Student> allStudents;
    private ProgressDialog progressDialog = new ProgressDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_below75);
        setTitle("Statistics");

        init();
    }

    private void init() {

        user = FirebaseAuth.getInstance().getCurrentUser();
        studentsAttendance = new HashMap<>();
        allDataOfMonths = new HashMap<>();
        studentsBelow75List = new HashMap<>();
        allStudents = new HashMap<>();
        getMonthRange();

        table = findViewById(R.id.table);
        isFirstRow = true;

        drawTableHeader();
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
                mainCode();
            }
        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> startActivity(new Intent(AttendanceBelow75Activity.this, HomeActivity.class)));

        alert.show();
    }

    private void mainCode() {
        progressDialog.show(this);
        dbRef = FirebaseDatabase.getInstance().getReference("teachers_data").child(user.getUid()).child("subject_code");
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

            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    dbRef = FirebaseDatabase.getInstance().getReference("attendance").child(snapshot.getValue(String.class)).child(String.valueOf(startYear));

                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            allDataOfMonths = (Map<String, Map<String, Map<String, Object>>>) snapshot.getValue();

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

                            calculatePercentageAndCreateRows();
                            progressDialog.hide();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(AttendanceBelow75Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.hide();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AttendanceBelow75Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
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

            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    subjectCode = snapshot.getValue(String.class);
                    dbRef = FirebaseDatabase.getInstance().getReference("attendance").child(subjectCode).child(String.valueOf(startYear));

                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            allDataOfMonths = (Map<String, Map<String, Map<String, Object>>>) snapshot.getValue();

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

                            dbRef = FirebaseDatabase.getInstance().getReference("attendance").child(subjectCode).child(String.valueOf(endYear));

                            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (allDataOfMonths.size() > 0) {
                                        allDataOfMonths.clear();
                                    }

                                    allDataOfMonths = (Map<String, Map<String, Map<String, Object>>>) snapshot.getValue();

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

                                    calculatePercentageAndCreateRows();
                                    progressDialog.hide();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(AttendanceBelow75Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.hide();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(AttendanceBelow75Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.hide();
                        }
                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AttendanceBelow75Activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                }
            });
        }
    }

    private void calculatePercentageAndCreateRows() {
        for (Map.Entry<String, Integer> entry : studentsAttendance.entrySet()) {
            float percentage = entry.getValue() / (float) totalLectures;
            percentage *= 100;

            if (percentage < 75.00f) {
                studentsBelow75List.put(entry.getKey(), percentage);
            }
        }

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("/students_data");
        Query rollNoQuery = databaseRef.orderByChild("rollNo");

        rollNoQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dsp : snapshot.getChildren()) {
                    String firstname = dsp.child("firstname").getValue(String.class);
                    String lastname = dsp.child("lastname").getValue(String.class);
                    int rollNo = dsp.child("rollNo").getValue(Integer.class);
                    allStudents.put(dsp.getKey(), new Student(firstname, lastname, rollNo));
                }

                for (Map.Entry<String, Float> entry : studentsBelow75List.entrySet()) {
                    String firstname = allStudents.get(entry.getKey()).getFirstname();
                    String lastname = allStudents.get(entry.getKey()).getLastname();
                    int rollNo = allStudents.get(entry.getKey()).getRollNo();

                    createTableRow(rollNo, firstname + " " + lastname, entry.getValue());
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

        tv0.setText("Roll No.");
        tv1.setText("Name");
        tv2.setText("Percentage");

        tv0.setTypeface(Typeface.DEFAULT_BOLD);
        tv1.setTypeface(Typeface.DEFAULT_BOLD);
        tv2.setTypeface(Typeface.DEFAULT_BOLD);

        tv0.setTextSize(18);
        tv1.setTextSize(18);
        tv2.setTextSize(18);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);

        tv0.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv1.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv2.setBackgroundColor(getResources().getColor(R.color.table_header));

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);

        table.addView(tbRow);
    }

    private void createTableRow(int rollNo, String name, float percentage) {
        TableRow tbRow = new TableRow(getApplicationContext());

        tbRow.setTag(rollNo);

        TextView tv0 = new TextView(getApplicationContext());
        TextView tv1 = new TextView(getApplicationContext());
        TextView tv2 = new TextView(getApplicationContext());

        tv0.setText(String.valueOf(rollNo));
        tv1.setText(name);
        tv2.setText(String.valueOf(percentage));

        tv0.setTextSize(16);
        tv1.setTextSize(16);
        tv2.setTextSize(16);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);

        tv0.setBackgroundResource(R.drawable.borders);
        tv1.setBackgroundResource(R.drawable.borders);
        tv2.setBackgroundResource(R.drawable.borders);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);

        if (isFirstRow) {
            tv0.setBackgroundColor(getResources().getColor(R.color.white));
            tv1.setBackgroundColor(getResources().getColor(R.color.white));
            tv2.setBackgroundColor(getResources().getColor(R.color.white));
            isFirstRow = false;
        } else {
            tv0.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv1.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv2.setBackgroundColor(getResources().getColor(R.color.light_gray));
            isFirstRow = true;
        }

        tbRow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                return true;
            }
        });

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);

        table.addView(tbRow);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AttendanceBelow75Activity.this, HomeActivity.class));
    }
}