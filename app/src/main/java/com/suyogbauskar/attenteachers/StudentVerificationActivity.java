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
                        table.removeAllViews();
                        if (snapshot.getChildrenCount() == 0) {
                            allStudentsVerifiedView.setVisibility(View.VISIBLE);
                            return;
                        }
                        drawTableHeader();
                        allStudentsVerifiedView.setVisibility(View.GONE);
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            createTableRow(ds.child("rollNo").getValue(Integer.class), ds.child("firstname").getValue(String.class) + " " + ds.child("lastname").getValue(String.class), ds.child("enrollNo").getValue(Long.class));
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

        tv0.setText("Roll No.");
        tv1.setText("Name");
        tv2.setText("Enroll No.");
        tv3.setText("Verified");

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

    private void createTableRow(int rollNo, String name, long enrollNo) {
        TableRow tbRow = new TableRow(StudentVerificationActivity.this);

        tbRow.setTag(rollNo);

        TextView tv0 = new TextView(StudentVerificationActivity.this);
        TextView tv1 = new TextView(StudentVerificationActivity.this);
        TextView tv2 = new TextView(StudentVerificationActivity.this);
        TextView tv3 = new TextView(StudentVerificationActivity.this);

        tv0.setText(String.valueOf(rollNo));
        tv1.setText(name);
        tv2.setText(String.valueOf(enrollNo));
        tv3.setText("❌");

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

        tbRow.setOnClickListener(view -> new SweetAlertDialog(StudentVerificationActivity.this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Verify Student?")
                .setContentText("Roll no. " + tbRow.getTag().toString() + " will be verified")
                .setConfirmText("Verify")
                .setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();
                    int rollNoOfTag = Integer.parseInt(tbRow.getTag().toString());

                    FirebaseDatabase.getInstance().getReference("students_data")
                            .orderByChild("rollNo")
                            .equalTo(rollNoOfTag)
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
            int rollNoFromTag = Integer.parseInt(tbRow.getTag().toString());

            new SweetAlertDialog(StudentVerificationActivity.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Delete student?")
                    .setContentText("Roll no. " + rollNoFromTag + " will be deleted")
                    .setConfirmText("Delete")
                    .setConfirmClickListener(sweetAlertDialog -> FirebaseDatabase.getInstance().getReference("students_data")
                            .orderByChild("rollNo")
                            .equalTo(rollNoFromTag)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds: snapshot.getChildren()) {
                                        //TODO : Delete student from authentication using cloud function
                                        ds.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(StudentVerificationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }))
                    .setCancelText("No")
                    .setCancelClickListener(Dialog::dismiss).show();

            return true;
        });

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);
        tbRow.addView(tv3);

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