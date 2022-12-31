package com.suyogbauskar.attenteachers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.utils.ProgressDialog;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LiveAttendanceActivity extends AppCompatActivity {

    private FirebaseUser user;
    private TableLayout table;
    private TextView noAttendanceStartedView, totalPresentStudentsView;
    private Button addStudentBtn;
    private boolean isFirstRow;
    private String monthStr, subjectCode;
    private int date, year, semester, count;
    private String studentUID, studentFirstname, studentLastname, attendanceOf;
    private Map<String, Object> studentData;
    private final ProgressDialog progressDialog = new ProgressDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_attendance);
        setTitle("Live Attendance");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
        checkForAttendance();
    }

    private void init() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        studentData = new HashMap<>();

        SharedPreferences sharedPreferences = getSharedPreferences("DBPathPref", MODE_PRIVATE);
        attendanceOf = sharedPreferences.getString("attendanceOf", "");
        subjectCode = sharedPreferences.getString("subjectCode", "");
        semester = sharedPreferences.getInt("subjectSemester", 0);
        count = sharedPreferences.getInt("count", 0);

        findAllViews();
        setListeners();
        getDateAndTime();
    }

    private void findAllViews() {
        table = findViewById(R.id.table);
        noAttendanceStartedView = findViewById(R.id.noAttendanceStartedView);
        totalPresentStudentsView = findViewById(R.id.totalPresentStudentsView);
        addStudentBtn = findViewById(R.id.addStudentBtn);
    }

    private void setListeners() {
        addStudentBtn.setOnClickListener(view -> showInputDialogForRollNo());
    }

    private void getDateAndTime() {
        long currentDate = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy/HH/mm/ss/MMMM", Locale.getDefault());
        String dateStr = dateFormat.format(currentDate);
        String[] dateArr = dateStr.split("/");
        date = Integer.parseInt(dateArr[0]);
        year = Integer.parseInt(dateArr[2]);
        monthStr = dateArr[6];
    }

    private void addStudentToAttendance(int rollNo) {
        FirebaseDatabase.getInstance().getReference("students_data")
                .orderByChild("rollNo")
                .equalTo(rollNo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            Toast.makeText(LiveAttendanceActivity.this, "Roll no. " + rollNo + " not found!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            studentUID = ds.getKey();
                            studentFirstname = ds.child("firstname").getValue(String.class);
                            studentLastname = ds.child("lastname").getValue(String.class);
                        }

                        studentData.put("firstname", studentFirstname);
                        studentData.put("lastname", studentLastname);
                        studentData.put("rollNo", rollNo);

                        FirebaseDatabase.getInstance().getReference("attendance/CO" + semester + "-" + attendanceOf + "/" + subjectCode + "/" + year + "/" + monthStr)
                                .child(date + "-" + count)
                                .child(studentUID)
                                .setValue(studentData)
                                .addOnSuccessListener(unused -> Toast.makeText(LiveAttendanceActivity.this, "Roll no. " + rollNo + " added successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(LiveAttendanceActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LiveAttendanceActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showInputDialogForRollNo() {
        final FlatDialog flatDialog = new FlatDialog(LiveAttendanceActivity.this);
        flatDialog.setTitle("Roll No")
                .setSubtitle("Enter roll no to mark attendance")
                .setFirstTextFieldInputType(InputType.TYPE_CLASS_NUMBER)
                .setFirstTextFieldHint("Roll no")
                .setFirstButtonText("OK")
                .setSecondButtonText("CANCEL")
                .withFirstButtonListner(view -> {
                    flatDialog.dismiss();
                    addStudentToAttendance(Integer.parseInt(flatDialog.getFirstTextField()));
                })
                .withSecondButtonListner(view -> flatDialog.dismiss())
                .show();
    }

    private void checkForAttendance() {
        progressDialog.show(LiveAttendanceActivity.this);

        FirebaseDatabase.getInstance().getReference("attendance/active_attendance/CO" + semester + "-" + attendanceOf + "/subject_code")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.hide();
                        if (subjectCode.equals(snapshot.getValue(String.class))) {
                            addStudentBtn.setVisibility(View.VISIBLE);
                            drawTableHeader();

                            FirebaseDatabase.getInstance().getReference("attendance/CO" + semester + "-" + attendanceOf + "/" + subjectCode + "/" + year + "/" + monthStr)
                                    .child(date + "-" + count)
                                    .orderByChild("rollNo")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            table.removeViews(1, table.getChildCount() - 1);
                                            totalPresentStudentsView.setText("Total present students: " + snapshot.getChildrenCount());
                                            for (DataSnapshot dsp : snapshot.getChildren()) {
                                                createTableRow(dsp.child("rollNo").getValue(Integer.class), dsp.child("firstname").getValue(String.class) + " " + dsp.child("lastname").getValue(String.class));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            progressDialog.hide();
                                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            noAttendanceStartedView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.hide();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void drawTableHeader() {
        TableRow tbRow = new TableRow(LiveAttendanceActivity.this);

        TextView tv0 = new TextView(LiveAttendanceActivity.this);
        TextView tv1 = new TextView(LiveAttendanceActivity.this);
        TextView tv2 = new TextView(LiveAttendanceActivity.this);

        tv0.setText("Roll No.");
        tv1.setText("Name");
        tv2.setText("Attendance");

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

    private void createTableRow(int rollNo, String name) {
        TableRow tbRow = new TableRow(LiveAttendanceActivity.this);

        tbRow.setTag(rollNo);

        TextView tv0 = new TextView(LiveAttendanceActivity.this);
        TextView tv1 = new TextView(LiveAttendanceActivity.this);
        TextView tv2 = new TextView(LiveAttendanceActivity.this);

        tv0.setText(String.valueOf(rollNo));
        tv1.setText(name);
        tv2.setText("✅");

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

        tbRow.setOnLongClickListener(view -> {
            new SweetAlertDialog(LiveAttendanceActivity.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Remove Attendance?")
                    .setContentText("Roll no. " + tbRow.getTag().toString() + " attendance will be removed")
                    .setConfirmText("Remove")
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation();

                        long currentDate = System.currentTimeMillis();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy/HH/mm/ss/MMMM", Locale.getDefault());
                        String dateStr = dateFormat.format(currentDate);
                        String[] dateArr = dateStr.split("/");
                        int date = Integer.parseInt(dateArr[0]);
                        int year = Integer.parseInt(dateArr[2]);
                        String monthStr = dateArr[6];

                        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        long currentDate = System.currentTimeMillis();
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy/HH/mm/ss/MMMM", Locale.getDefault());
                                        String dateStr = dateFormat.format(currentDate);
                                        String[] dateArr = dateStr.split("/");
                                        int date = Integer.parseInt(dateArr[0]);
                                        int year = Integer.parseInt(dateArr[2]);
                                        String monthStr = dateArr[6];

                                        FirebaseDatabase.getInstance().getReference("/attendance/" + snapshot.child("subject_code").getValue(String.class) + "/" +
                                                        year + "/" + monthStr).child(date + "-" + snapshot.child("lectures_taken_today").getValue(Integer.class))
                                                .orderByChild("rollNo")
                                                .equalTo(Integer.parseInt(tbRow.getTag().toString()))
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                                            ds.getRef()
                                                                    .removeValue()
                                                                    .addOnSuccessListener(unused -> Toast.makeText(LiveAttendanceActivity.this, "Roll no. " + rollNo + " removed", Toast.LENGTH_SHORT).show())
                                                                    .addOnFailureListener(e -> Toast.makeText(LiveAttendanceActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(LiveAttendanceActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setCancelButton("No", SweetAlertDialog::dismissWithAnimation)
                    .show();
            return true;
        });

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);

        table.addView(tbRow);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(LiveAttendanceActivity.this, HomeActivity.class));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}