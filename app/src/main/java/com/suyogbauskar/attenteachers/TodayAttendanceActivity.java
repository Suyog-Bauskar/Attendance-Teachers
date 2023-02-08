package com.suyogbauskar.attenteachers;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TodayAttendanceActivity extends AppCompatActivity {

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private TableLayout table;
    private Button selectSemesterBtn;
    private boolean isFirstRow;
    private String subjectCodeTeacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_attendance);
        setTitle("Today's Attendance");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
    }

    private void init() {
        table = findViewById(R.id.table);
        isFirstRow = true;

        selectSemesterBtn = findViewById(R.id.selectSemesterBtn);
        selectSemesterBtn.setOnClickListener(view -> selectSemester());
    }

    private void selectSemester() {
        PopupMenu semesterMenu = new PopupMenu(TodayAttendanceActivity.this, selectSemesterBtn);
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
                                Toast.makeText(TodayAttendanceActivity.this, "You don't teach this semester", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            PopupMenu attendanceOfMenu = new PopupMenu(TodayAttendanceActivity.this, selectSemesterBtn);
                            attendanceOfMenu.getMenu().add(Menu.NONE, 1, 1, "Division A");
                            attendanceOfMenu.getMenu().add(Menu.NONE, 2, 2, "Division B");
                            attendanceOfMenu.getMenu().add(Menu.NONE, 3, 3, "Batch A1");
                            attendanceOfMenu.getMenu().add(Menu.NONE, 4, 4, "Batch A2");
                            attendanceOfMenu.getMenu().add(Menu.NONE, 5, 5, "Batch A3");
                            attendanceOfMenu.getMenu().add(Menu.NONE, 6, 6, "Batch B1");
                            attendanceOfMenu.getMenu().add(Menu.NONE, 7, 7, "Batch B2");
                            attendanceOfMenu.show();
                            attendanceOfMenu.setOnMenuItemClickListener(item2 -> {
                                switch (item2.getItemId()) {
                                    case 1:
                                        showTodayAttendance(item.getItemId(), "A");
                                        break;

                                    case 2:
                                        showTodayAttendance(item.getItemId(), "B");
                                        break;

                                    case 3:
                                        showTodayAttendance(item.getItemId(), "A1");
                                        break;

                                    case 4:
                                        showTodayAttendance(item.getItemId(), "A2");
                                        break;

                                    case 5:
                                        showTodayAttendance(item.getItemId(), "A3");
                                        break;

                                    case 6:
                                        showTodayAttendance(item.getItemId(), "B1");
                                        break;

                                    case 7:
                                        showTodayAttendance(item.getItemId(), "B2");
                                        break;
                                }
                                return true;
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(TodayAttendanceActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            return true;
        });
    }

    private void showTodayAttendance(int semester, String attendanceOf) {
        long currentDate = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy/HH/mm/ss/MMMM", Locale.getDefault());
        String dateStr = dateFormat.format(currentDate);
        String[] dateArr = dateStr.split("/");
        int date = Integer.parseInt(dateArr[0]);
        int year = Integer.parseInt(dateArr[2]);
        String monthStr = dateArr[6];

        FirebaseDatabase.getInstance().getReference("attendance")
                .child("CO" + semester + "-" + attendanceOf)
                .child(subjectCodeTeacher)
                .child(String.valueOf(year))
                .child(monthStr)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectSemesterBtn.setVisibility(View.GONE);
                        String dateStr;
                        final String[] lectureCount = new String[1];
                        drawTableHeader();
                        for (DataSnapshot dsp: snapshot.getChildren()) {
                            dateStr = dsp.getKey().split("-")[0];

                            if (dateStr.equals(String.valueOf(date))) {
                                dsp.getRef().orderByChild("rollNo").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                        lectureCount[0] = dsp.getKey().split("-")[1];
                                        for (DataSnapshot dsp2: snapshot2.getChildren()) {
                                            createTableRow(lectureCount[0], dsp2.child("rollNo").getValue(Integer.class), dsp2.child("firstname").getValue(String.class) + " " + dsp2.child("lastname").getValue(String.class));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(TodayAttendanceActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TodayAttendanceActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void drawTableHeader() {
        TableRow tbRow = new TableRow(TodayAttendanceActivity.this);

        TextView tv0 = new TextView(TodayAttendanceActivity.this);
        TextView tv1 = new TextView(TodayAttendanceActivity.this);
        TextView tv2 = new TextView(TodayAttendanceActivity.this);
        TextView tv3 = new TextView(TodayAttendanceActivity.this);

        tv0.setText("Lecture");
        tv1.setText("Roll No.");
        tv2.setText("Name");
        tv3.setText("Attendance");

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

    private void createTableRow(String lectureCount, int rollNo, String name) {
        TableRow tbRow = new TableRow(TodayAttendanceActivity.this);

        TextView tv0 = new TextView(TodayAttendanceActivity.this);
        TextView tv1 = new TextView(TodayAttendanceActivity.this);
        TextView tv2 = new TextView(TodayAttendanceActivity.this);
        TextView tv3 = new TextView(TodayAttendanceActivity.this);

        tv0.setText(lectureCount);
        tv1.setText(String.valueOf(rollNo));
        tv2.setText(name);
        tv3.setText("✅");

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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(TodayAttendanceActivity.this, HomeActivity.class));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}