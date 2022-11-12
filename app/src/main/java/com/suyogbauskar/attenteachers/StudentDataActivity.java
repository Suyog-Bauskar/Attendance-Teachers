package com.suyogbauskar.attenteachers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class StudentDataActivity extends AppCompatActivity {

    private TableLayout table;
    private boolean isFirstRow;
    private TextView noStudentsFoundView;
    private String firstnameStr, lastnameStr;
    private long studentEnrollNo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_data);
        setTitle("Students Data");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findAllViews();
        showAllStudentsData();
    }

    private void showAllStudentsData() {
        FirebaseDatabase.getInstance().getReference("students_data")
                .orderByChild("rollNo")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        table.removeAllViews();
                        if (snapshot.getChildrenCount() == 0) {
                            noStudentsFoundView.setVisibility(View.VISIBLE);
                            return;
                        }
                        drawTableHeader();
                        noStudentsFoundView.setVisibility(View.GONE);
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            createTableRow(ds.child("rollNo").getValue(Integer.class), ds.child("firstname").getValue(String.class) + " " + ds.child("lastname").getValue(String.class), ds.child("enrollNo").getValue(Long.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StudentDataActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void findAllViews() {
        table = findViewById(R.id.table);
        noStudentsFoundView = findViewById(R.id.noStudentsFoundView);
    }

    private void drawTableHeader() {
        TableRow tbRow = new TableRow(StudentDataActivity.this);

        TextView tv0 = new TextView(StudentDataActivity.this);
        TextView tv1 = new TextView(StudentDataActivity.this);
        TextView tv2 = new TextView(StudentDataActivity.this);

        tv0.setText("Roll No.");
        tv1.setText("Name");
        tv2.setText("Enroll No.");

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

    private void createTableRow(int rollNo, String name, long enrollNo) {
        TableRow tbRow = new TableRow(StudentDataActivity.this);

        tbRow.setTag(rollNo);

        TextView tv0 = new TextView(StudentDataActivity.this);
        TextView tv1 = new TextView(StudentDataActivity.this);
        TextView tv2 = new TextView(StudentDataActivity.this);

        tv0.setText(String.valueOf(rollNo));
        tv1.setText(name);
        tv2.setText(String.valueOf(enrollNo));

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

        tbRow.setOnClickListener(view -> {
            int rollNoFromTag = Integer.parseInt(tbRow.getTag().toString());

            FirebaseDatabase.getInstance().getReference("students_data")
                    .orderByChild("rollNo")
                    .equalTo(rollNoFromTag)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String studentFirstname = "", studentLastname = "";

                            for (DataSnapshot ds : snapshot.getChildren()) {
                                studentFirstname = ds.child("firstname").getValue(String.class);
                                studentLastname = ds.child("lastname").getValue(String.class);
                                studentEnrollNo = ds.child("enrollNo").getValue(Long.class);
                            }

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins((int) (15 * getResources().getDisplayMetrics().density + 0.5f), (int) (15 * getResources().getDisplayMetrics().density + 0.5f), (int) (15 * getResources().getDisplayMetrics().density + 0.5f), 0);

                            AlertDialog.Builder alert = new AlertDialog.Builder(StudentDataActivity.this);
                            alert.setTitle("Update Details");

                            LinearLayout layout = new LinearLayout(StudentDataActivity.this);
                            layout.setOrientation(LinearLayout.VERTICAL);

                            final EditText rollNoEditText = new EditText(StudentDataActivity.this);
                            rollNoEditText.setHint("Roll no.");
                            rollNoEditText.setText(String.valueOf(rollNoFromTag));
                            rollNoEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            rollNoEditText.setLayoutParams(params);
                            layout.addView(rollNoEditText);

                            final EditText firstnameEditText = new EditText(StudentDataActivity.this);
                            firstnameEditText.setHint("Firstname");
                            firstnameEditText.setText(studentFirstname);
                            firstnameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                            firstnameEditText.setLayoutParams(params);
                            layout.addView(firstnameEditText);

                            final EditText lastnameEditText = new EditText(StudentDataActivity.this);
                            lastnameEditText.setHint("Lastname");
                            lastnameEditText.setText(studentLastname);
                            lastnameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                            lastnameEditText.setLayoutParams(params);
                            layout.addView(lastnameEditText);

                            final EditText enrollNoEditText = new EditText(StudentDataActivity.this);
                            enrollNoEditText.setHint("Enroll no.");
                            enrollNoEditText.setText(String.valueOf(studentEnrollNo));
                            enrollNoEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            enrollNoEditText.setLayoutParams(params);
                            layout.addView(enrollNoEditText);

                            alert.setView(layout);

                            alert.setPositiveButton("Save", (dialog, whichButton) -> {
                                firstnameStr = firstnameEditText.getText().toString().trim().toLowerCase();
                                lastnameStr = lastnameEditText.getText().toString().trim().toLowerCase();
                                String rollNoStr = rollNoEditText.getText().toString().trim();
                                String enrollNoStr = enrollNoEditText.getText().toString().trim();
                                long enrollNoLong = Long.parseLong(enrollNoEditText.getText().toString().trim());
                                int rollNoInt = Integer.parseInt(rollNoEditText.getText().toString().trim());

                                if (firstnameStr.isEmpty()) {
                                    Toast.makeText(getApplicationContext(), "Enter Firstname", Toast.LENGTH_LONG).show();
                                } else if (lastnameStr.isEmpty()) {
                                    Toast.makeText(getApplicationContext(), "Enter Lastname", Toast.LENGTH_LONG).show();
                                } else if ((enrollNoStr.length() != 10) || (enrollNoLong == 0)) {
                                    Toast.makeText(getApplicationContext(), "Invalid Enrollment No", Toast.LENGTH_LONG).show();
                                } else if (rollNoStr.isEmpty()) {
                                    Toast.makeText(getApplicationContext(), "Invalid Roll No", Toast.LENGTH_LONG).show();
                                } else if (rollNoStr.length() >= 4 || (rollNoInt == 0)) {
                                    Toast.makeText(getApplicationContext(), "Invalid Roll No", Toast.LENGTH_LONG).show();
                                } else {
                                    firstnameStr = firstnameStr.substring(0, 1).toUpperCase() + firstnameStr.substring(1);
                                    lastnameStr = lastnameStr.substring(0, 1).toUpperCase() + lastnameStr.substring(1);
                                    FirebaseDatabase.getInstance().getReference("students_data")
                                            .orderByChild("rollNo")
                                            .equalTo(rollNoFromTag)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                                    for (DataSnapshot ds: snapshot1.getChildren()) {
                                                        ds.getRef().child("rollNo").setValue(rollNoInt);
                                                        ds.getRef().child("firstname").setValue(firstnameStr);
                                                        ds.getRef().child("lastname").setValue(lastnameStr);
                                                        ds.getRef().child("enrollNo").setValue(enrollNoLong);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Toast.makeText(StudentDataActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });

                            alert.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss());

                            alert.show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(StudentDataActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tbRow.setOnLongClickListener(view -> {
            int rollNoFromTag = Integer.parseInt(tbRow.getTag().toString());

            new SweetAlertDialog(StudentDataActivity.this, SweetAlertDialog.WARNING_TYPE)
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
                                    Toast.makeText(StudentDataActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }))
                    .setCancelText("No")
                    .setCancelClickListener(Dialog::dismiss).show();

            return true;
        });

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);

        table.addView(tbRow);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(StudentDataActivity.this, HomeActivity.class));
    }
}