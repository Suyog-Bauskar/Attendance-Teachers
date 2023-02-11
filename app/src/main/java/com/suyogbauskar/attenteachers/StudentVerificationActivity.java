package com.suyogbauskar.attenteachers;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.pojos.StudentData;

import java.util.Map;
import java.util.TreeMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class StudentVerificationActivity extends AppCompatActivity {

    private TableLayout table;
    private boolean isFirstRow;
    private TextView allStudentsVerifiedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_verification);
        setTitle("Student Verification");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findAllViews();
        findNotVerifiedStudents();
    }

    private void findNotVerifiedStudents() {
        FirebaseDatabase.getInstance().getReference("students_data")
                .orderByChild("isVerified")
                .equalTo(false)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<Long, StudentData> tempMap = new TreeMap<>();
                        table.removeAllViews();
                        if (snapshot.getChildrenCount() == 0) {
                            allStudentsVerifiedView.setVisibility(View.VISIBLE);
                            return;
                        }
                        drawTableHeader();
                        allStudentsVerifiedView.setVisibility(View.GONE);
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            tempMap.put(ds.child("enrollNo").getValue(Long.class),
                                    new StudentData(ds.child("rollNo").getValue(Integer.class), ds.child("batch").getValue(Integer.class),ds.child("semester").getValue(Integer.class), ds.child("enrollNo").getValue(Long.class), ds.child("firstname").getValue(String.class), ds.child("lastname").getValue(String.class), ds.child("division").getValue(String.class)));
                        }
                        for (Map.Entry<Long, StudentData> entry1: tempMap.entrySet()) {
                            createTableRow(entry1.getValue().getRollNo(), entry1.getValue().getFirstname() + " " + entry1.getValue().getLastname(),entry1.getValue().getSemester(), entry1.getValue().getEnrollNo(), entry1.getValue().getDivision(), entry1.getValue().getBatch());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StudentVerificationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void findAllViews() {
        table = findViewById(R.id.table);
        allStudentsVerifiedView = findViewById(R.id.allStudentsVerifiedView);
    }

    private void drawTableHeader() {
        TableRow tbRow = new TableRow(StudentVerificationActivity.this);

        TextView tv0 = new TextView(StudentVerificationActivity.this);
        TextView tv1 = new TextView(StudentVerificationActivity.this);
        TextView tv2 = new TextView(StudentVerificationActivity.this);
        TextView tv3 = new TextView(StudentVerificationActivity.this);
        TextView tv4 = new TextView(StudentVerificationActivity.this);
        TextView tv5 = new TextView(StudentVerificationActivity.this);
        TextView tv6 = new TextView(StudentVerificationActivity.this);

        tv0.setText("Roll No.");
        tv1.setText("Name");
        tv2.setText("Semester");
        tv3.setText("Enroll No.");
        tv4.setText("Division");
        tv5.setText("Batch");
        tv6.setText("Verified");

        tv0.setTypeface(Typeface.DEFAULT_BOLD);
        tv1.setTypeface(Typeface.DEFAULT_BOLD);
        tv2.setTypeface(Typeface.DEFAULT_BOLD);
        tv3.setTypeface(Typeface.DEFAULT_BOLD);
        tv4.setTypeface(Typeface.DEFAULT_BOLD);
        tv5.setTypeface(Typeface.DEFAULT_BOLD);
        tv6.setTypeface(Typeface.DEFAULT_BOLD);

        tv0.setTextSize(18);
        tv1.setTextSize(18);
        tv2.setTextSize(18);
        tv3.setTextSize(18);
        tv4.setTextSize(18);
        tv5.setTextSize(18);
        tv6.setTextSize(18);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);
        tv3.setPadding(30, 30, 15, 30);
        tv4.setPadding(30, 30, 15, 30);
        tv5.setPadding(30, 30, 15, 30);
        tv6.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);
        tv3.setGravity(Gravity.CENTER);
        tv4.setGravity(Gravity.CENTER);
        tv5.setGravity(Gravity.CENTER);
        tv6.setGravity(Gravity.CENTER);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);
        tv3.setTextColor(Color.BLACK);
        tv4.setTextColor(Color.BLACK);
        tv5.setTextColor(Color.BLACK);
        tv6.setTextColor(Color.BLACK);

        tv0.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv1.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv2.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv3.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv4.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv5.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv6.setBackgroundColor(getResources().getColor(R.color.table_header));

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);
        tbRow.addView(tv3);
        tbRow.addView(tv4);
        tbRow.addView(tv5);
        tbRow.addView(tv6);

        table.addView(tbRow);
    }

    private void createTableRow(int rollNo, String name,int semester, long enrollNo, String division, int batch) {
        TableRow tbRow = new TableRow(StudentVerificationActivity.this);

        tbRow.setTag(enrollNo);

        TextView tv0 = new TextView(StudentVerificationActivity.this);
        TextView tv1 = new TextView(StudentVerificationActivity.this);
        TextView tv2 = new TextView(StudentVerificationActivity.this);
        TextView tv3 = new TextView(StudentVerificationActivity.this);
        TextView tv4 = new TextView(StudentVerificationActivity.this);
        TextView tv5 = new TextView(StudentVerificationActivity.this);
        TextView tv6 = new TextView(StudentVerificationActivity.this);

        tv0.setText(String.valueOf(rollNo));
        tv1.setText(name);
        tv2.setText(String.valueOf(semester));
        tv3.setText(String.valueOf(enrollNo));
        tv4.setText(division);
        tv5.setText(String.valueOf(batch));
        tv6.setText("❌");

        tv0.setTextSize(16);
        tv1.setTextSize(16);
        tv2.setTextSize(16);
        tv3.setTextSize(16);
        tv4.setTextSize(16);
        tv5.setTextSize(16);
        tv6.setTextSize(16);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);
        tv3.setPadding(30, 30, 15, 30);
        tv4.setPadding(30, 30, 15, 30);
        tv5.setPadding(30, 30, 15, 30);
        tv6.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);
        tv3.setGravity(Gravity.CENTER);
        tv4.setGravity(Gravity.CENTER);
        tv5.setGravity(Gravity.CENTER);
        tv6.setGravity(Gravity.CENTER);

        tv0.setBackgroundResource(R.drawable.borders);
        tv1.setBackgroundResource(R.drawable.borders);
        tv2.setBackgroundResource(R.drawable.borders);
        tv3.setBackgroundResource(R.drawable.borders);
        tv4.setBackgroundResource(R.drawable.borders);
        tv5.setBackgroundResource(R.drawable.borders);
        tv6.setBackgroundResource(R.drawable.borders);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);
        tv3.setTextColor(Color.BLACK);
        tv4.setTextColor(Color.BLACK);
        tv5.setTextColor(Color.BLACK);
        tv6.setTextColor(Color.BLACK);

        if (isFirstRow) {
            tv0.setBackgroundColor(getResources().getColor(R.color.white));
            tv1.setBackgroundColor(getResources().getColor(R.color.white));
            tv2.setBackgroundColor(getResources().getColor(R.color.white));
            tv3.setBackgroundColor(getResources().getColor(R.color.white));
            tv4.setBackgroundColor(getResources().getColor(R.color.white));
            tv5.setBackgroundColor(getResources().getColor(R.color.white));
            tv6.setBackgroundColor(getResources().getColor(R.color.white));
            isFirstRow = false;
        } else {
            tv0.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv1.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv2.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv3.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv4.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv5.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv6.setBackgroundColor(getResources().getColor(R.color.light_gray));
            isFirstRow = true;
        }

        tbRow.setOnClickListener(view -> new SweetAlertDialog(StudentVerificationActivity.this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Verify Student?")
                .setContentText("Enroll no. " + tbRow.getTag().toString() + " will be verified")
                .setConfirmText("Verify")
                .setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();
                    long enrollNoOfTag = Long.parseLong(tbRow.getTag().toString());

                    FirebaseDatabase.getInstance().getReference("students_data")
                            .orderByChild("enrollNo")
                            .equalTo(enrollNoOfTag)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds: snapshot.getChildren()) {
                                        ds.getRef().child("isVerified").setValue(true);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(StudentVerificationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setCancelButton("No", SweetAlertDialog::dismissWithAnimation)
                .show());

        tbRow.setOnLongClickListener(view -> {
            long enrollNoFromTag = Long.parseLong(tbRow.getTag().toString());

            new SweetAlertDialog(StudentVerificationActivity.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Delete student?")
                    .setContentText("Enroll no. " + enrollNoFromTag + " will be deleted")
                    .setConfirmText("Delete")
                    .setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        FirebaseDatabase.getInstance().getReference("students_data")
                                .orderByChild("enrollNo")
                                .equalTo(enrollNoFromTag)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds: snapshot.getChildren()) {
                                            ds.getRef().removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(StudentVerificationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setCancelText("No")
                    .setCancelClickListener(Dialog::dismiss).show();

            return true;
        });

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);
        tbRow.addView(tv3);
        tbRow.addView(tv4);
        tbRow.addView(tv5);
        tbRow.addView(tv6);

        table.addView(tbRow);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(StudentVerificationActivity.this, HomeActivity.class));
    }
}