package com.suyogbauskar.attenteachers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.pojos.StudentData;

import java.util.Map;
import java.util.TreeMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class StudentDataActivity extends AppCompatActivity {

    private TableLayout table;
    private boolean isFirstRow;
    private TextView noStudentsFoundView;
    private Button selectSemesterBtn;
    private String firstnameStr, lastnameStr;
    private long studentEnrollNo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_data);
        setTitle("Students Data");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findAllViews();
    }

    private void selectSemester() {
        PopupMenu semesterMenu = new PopupMenu(StudentDataActivity.this, selectSemesterBtn);
        semesterMenu.getMenu().add(Menu.NONE, 1, 1, "Semester 1");
        semesterMenu.getMenu().add(Menu.NONE, 2, 2, "Semester 2");
        semesterMenu.getMenu().add(Menu.NONE, 3, 3, "Semester 3");
        semesterMenu.getMenu().add(Menu.NONE, 4, 4, "Semester 4");
        semesterMenu.getMenu().add(Menu.NONE, 5, 5, "Semester 5");
        semesterMenu.getMenu().add(Menu.NONE, 6, 6, "Semester 6");
        semesterMenu.show();

        semesterMenu.setOnMenuItemClickListener(item -> {
            showAllStudentsData(item.getItemId());
            return true;
        });
    }

    private void showAllStudentsData(int semester) {
        FirebaseDatabase.getInstance().getReference("students_data")
                .orderByChild("semester")
                .equalTo(semester)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<Integer, StudentData> tempMap = new TreeMap<>();

                        selectSemesterBtn.setVisibility(View.GONE);
                        table.removeAllViews();
                        isFirstRow = true;
                        if (snapshot.getChildrenCount() == 0) {
                            noStudentsFoundView.setVisibility(View.VISIBLE);
                            return;
                        }
                        drawTableHeader();
                        noStudentsFoundView.setVisibility(View.GONE);
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.child("isVerified").getValue(Boolean.class)) {
                                tempMap.put(ds.child("rollNo").getValue(Integer.class),
                                        new StudentData(ds.child("rollNo").getValue(Integer.class),ds.child("batch").getValue(Integer.class),ds.child("semester").getValue(Integer.class), ds.child("enrollNo").getValue(Long.class), ds.child("firstname").getValue(String.class), ds.child("lastname").getValue(String.class), ds.child("division").getValue(String.class)));
                            }
                        }
                        for (Map.Entry<Integer, StudentData> entry1: tempMap.entrySet()) {
                            createTableRow(entry1.getValue().getRollNo(), entry1.getValue().getFirstname() + " " + entry1.getValue().getLastname(), entry1.getValue().getEnrollNo(), entry1.getValue().getDivision(), entry1.getValue().getBatch());
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
        selectSemesterBtn = findViewById(R.id.selectSemesterBtn);
        selectSemesterBtn.setOnClickListener(view -> selectSemester());
    }

    private void drawTableHeader() {
        TableRow tbRow = new TableRow(StudentDataActivity.this);

        TextView tv0 = new TextView(StudentDataActivity.this);
        TextView tv1 = new TextView(StudentDataActivity.this);
        TextView tv2 = new TextView(StudentDataActivity.this);
        TextView tv3 = new TextView(StudentDataActivity.this);
        TextView tv4 = new TextView(StudentDataActivity.this);

        tv0.setText("Roll No.");
        tv1.setText("Name");
        tv2.setText("Enroll No.");
        tv3.setText("Division");
        tv4.setText("Batch");

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

    private void createTableRow(int rollNo, String name, long enrollNo, String division, int batch) {
        TableRow tbRow = new TableRow(StudentDataActivity.this);

        tbRow.setTag(enrollNo);

        TextView tv0 = new TextView(StudentDataActivity.this);
        TextView tv1 = new TextView(StudentDataActivity.this);
        TextView tv2 = new TextView(StudentDataActivity.this);
        TextView tv3 = new TextView(StudentDataActivity.this);
        TextView tv4 = new TextView(StudentDataActivity.this);

        tv0.setText(String.valueOf(rollNo));
        tv1.setText(name);
        tv2.setText(String.valueOf(enrollNo));
        tv3.setText(division);
        tv4.setText(String.valueOf(batch));

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

        tbRow.setOnClickListener(view -> {
            long enrollNoFromTag = Long.parseLong(tbRow.getTag().toString());

            FirebaseDatabase.getInstance().getReference("students_data")
                    .orderByChild("enrollNo")
                    .equalTo(enrollNoFromTag)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String studentFirstname = "", studentLastname = "", studentDivision = "";
                            int studentRollNo = 0, studentBatch = 0;

                            for (DataSnapshot ds : snapshot.getChildren()) {
                                studentFirstname = ds.child("firstname").getValue(String.class);
                                studentLastname = ds.child("lastname").getValue(String.class);
                                studentDivision = ds.child("division").getValue(String.class);
                                studentRollNo = ds.child("rollNo").getValue(Integer.class);
                                studentBatch = ds.child("batch").getValue(Integer.class);
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
                            rollNoEditText.setText(String.valueOf(studentRollNo));
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

                            final EditText divisionEditText = new EditText(StudentDataActivity.this);
                            divisionEditText.setHint("Division");
                            divisionEditText.setText(studentDivision);
                            divisionEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                            divisionEditText.setLayoutParams(params);
                            layout.addView(divisionEditText);

                            final EditText batchEditText = new EditText(StudentDataActivity.this);
                            batchEditText.setHint("Batch");
                            batchEditText.setText(String.valueOf(studentBatch));
                            batchEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            batchEditText.setLayoutParams(params);
                            layout.addView(batchEditText);

                            alert.setView(layout);

                            alert.setPositiveButton("Save", (dialog, whichButton) -> {
                                firstnameStr = firstnameEditText.getText().toString().trim().toLowerCase();
                                lastnameStr = lastnameEditText.getText().toString().trim().toLowerCase();
                                String rollNoStr = rollNoEditText.getText().toString().trim();
                                String enrollNoStr = enrollNoEditText.getText().toString().trim();
                                long enrollNoLong = Long.parseLong(enrollNoEditText.getText().toString().trim());
                                int rollNoInt = Integer.parseInt(rollNoEditText.getText().toString().trim());
                                String divisionStr = divisionEditText.getText().toString().trim();
                                int batchInt = Integer.parseInt(batchEditText.getText().toString().trim());

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
                                } else if (!(divisionStr.equals("A") || divisionStr.equals("B"))) {
                                    Toast.makeText(getApplicationContext(), "Invalid Division", Toast.LENGTH_LONG).show();
                                } else if (batchInt <= 0 || batchInt >= 6) {
                                    Toast.makeText(getApplicationContext(), "Invalid Batch", Toast.LENGTH_LONG).show();
                                } else {
                                    firstnameStr = firstnameStr.substring(0, 1).toUpperCase() + firstnameStr.substring(1);
                                    lastnameStr = lastnameStr.substring(0, 1).toUpperCase() + lastnameStr.substring(1);
                                    FirebaseDatabase.getInstance().getReference("students_data")
                                            .orderByChild("enrollNo")
                                            .equalTo(enrollNoFromTag)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                                    for (DataSnapshot ds: snapshot1.getChildren()) {
                                                        ds.getRef().child("rollNo").setValue(rollNoInt);
                                                        ds.getRef().child("firstname").setValue(firstnameStr);
                                                        ds.getRef().child("lastname").setValue(lastnameStr);
                                                        ds.getRef().child("enrollNo").setValue(enrollNoLong);
                                                        ds.getRef().child("division").setValue(divisionStr);
                                                        ds.getRef().child("batch").setValue(batchInt);
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
            long enrollNoFromTag = Long.parseLong(tbRow.getTag().toString());

            new SweetAlertDialog(StudentDataActivity.this, SweetAlertDialog.WARNING_TYPE)
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
                                        Toast.makeText(StudentDataActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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