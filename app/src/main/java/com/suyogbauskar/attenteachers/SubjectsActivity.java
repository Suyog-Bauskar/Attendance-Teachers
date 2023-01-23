package com.suyogbauskar.attenteachers;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.pojos.Subject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectsActivity extends AppCompatActivity {

    private FirebaseUser user;
    private TableLayout table;
    private boolean isFirstRow;
    private final List<Integer> semesterList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);
        setTitle("Subjects");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user = FirebaseAuth.getInstance().getCurrentUser();
        table = findViewById(R.id.table);
        showSubjects();
    }

    private void showSubjects() {
        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Subject> subjectMap = new HashMap<>();
                        Subject tempSubject;
                        isFirstRow = true;
                        table.removeAllViews();
                        drawTableHeader();
                        for (DataSnapshot dsp : snapshot.getChildren()) {
                            tempSubject = new Subject(dsp.child("subject_short_name").getValue(String.class), dsp.child("subject_name").getValue(String.class), dsp.getKey(), dsp.child("semester").getValue(Integer.class));
                            subjectMap.put(dsp.getKey(), tempSubject);
                            semesterList.add(dsp.child("semester").getValue(Integer.class));
                            createTableRow(dsp.child("subject_short_name").getValue(String.class), dsp.child("subject_name").getValue(String.class), dsp.getKey(), dsp.child("semester").getValue(Integer.class), tempSubject);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SubjectsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void editSubjectDetails(String shortName, String name, String code, int semester) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) (15 * getResources().getDisplayMetrics().density + 0.5f), (int) (15 * getResources().getDisplayMetrics().density + 0.5f), (int) (15 * getResources().getDisplayMetrics().density + 0.5f), 0);

        AlertDialog.Builder alert = new AlertDialog.Builder(SubjectsActivity.this);
        alert.setTitle("Update Details");

        LinearLayout layout = new LinearLayout(SubjectsActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText shortNameEditText = new EditText(SubjectsActivity.this);
        shortNameEditText.setHint("Short Name");
        shortNameEditText.setText(shortName);
        shortNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        shortNameEditText.setLayoutParams(params);
        layout.addView(shortNameEditText);

        final EditText nameEditText = new EditText(SubjectsActivity.this);
        nameEditText.setHint("Name");
        nameEditText.setText(name);
        nameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        nameEditText.setLayoutParams(params);
        layout.addView(nameEditText);

        final EditText codeEditText = new EditText(SubjectsActivity.this);
        codeEditText.setHint("Code");
        codeEditText.setText(code);
        codeEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        codeEditText.setLayoutParams(params);
        layout.addView(codeEditText);

        final EditText semesterEditText = new EditText(SubjectsActivity.this);
        semesterEditText.setHint("Enroll no.");
        semesterEditText.setText(String.valueOf(semester));
        semesterEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        semesterEditText.setLayoutParams(params);
        layout.addView(semesterEditText);

        alert.setView(layout);

        alert.setPositiveButton("Save", (dialog, whichButton) -> {
            String shortNameStr = shortNameEditText.getText().toString().trim().toUpperCase();
            String nameStr = capitalizeWord(nameEditText.getText().toString().trim().toLowerCase());
            String codeStr = codeEditText.getText().toString().trim();
            int semesterInt = Integer.parseInt(semesterEditText.getText().toString().trim());

            if (shortNameStr.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter Short Name", Toast.LENGTH_LONG).show();
            } else if (nameStr.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter Name", Toast.LENGTH_LONG).show();
            } else if ((codeStr.length() != 5) || (Long.parseLong(codeStr) == 0)) {
                Toast.makeText(getApplicationContext(), "Invalid Code", Toast.LENGTH_LONG).show();
            } else if (semesterInt <= 0 || semesterInt > 6) {
                Toast.makeText(getApplicationContext(), "Invalid Semester", Toast.LENGTH_LONG).show();
            } else if (semesterList.contains(semesterInt) && (semester != semesterInt)) {
                Toast.makeText(getApplicationContext(), "You already teach this semester", Toast.LENGTH_LONG).show();
            } else {
                if ((shortName.equals(shortNameStr)) && (name.equals(nameStr)) && (code.equals(codeStr)) && (semester == semesterInt)) {
                    return;
                }

                if (!code.equals(codeStr)) {

                } else {
                    FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects/" + code)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    snapshot.child("subject_short_name").getRef().setValue(shortNameStr);
                                    snapshot.child("subject_name").getRef().setValue(nameStr);
                                    snapshot.child("semester").getRef().setValue(semesterInt);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(SubjectsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        alert.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss());
        alert.show();
    }

    private void removeSubject() {

    }

    private String capitalizeWord(String str) {
        String words[] = str.split("\\s");
        String capitalizeWord = "";
        for (String w : words) {
            String first = w.substring(0, 1);
            String afterFirst = w.substring(1);
            capitalizeWord += first.toUpperCase() + afterFirst + " ";
        }
        return capitalizeWord.trim();
    }

    private void drawTableHeader() {
        TableRow tbRow = new TableRow(SubjectsActivity.this);

        TextView tv0 = new TextView(SubjectsActivity.this);
        TextView tv1 = new TextView(SubjectsActivity.this);
        TextView tv2 = new TextView(SubjectsActivity.this);
        TextView tv3 = new TextView(SubjectsActivity.this);

        tv0.setText("Short Name");
        tv1.setText("Name");
        tv2.setText("Code");
        tv3.setText("Semester");

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

    private void createTableRow(String shortName, String name, String code, int semester, Subject subject) {
        TableRow tbRow = new TableRow(SubjectsActivity.this);
        tbRow.setTag(subject);

        TextView tv0 = new TextView(SubjectsActivity.this);
        TextView tv1 = new TextView(SubjectsActivity.this);
        TextView tv2 = new TextView(SubjectsActivity.this);
        TextView tv3 = new TextView(SubjectsActivity.this);

        tv0.setText(shortName);
        tv1.setText(name);
        tv2.setText(code);
        tv3.setText(String.valueOf(semester));

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

        tbRow.setOnClickListener(view -> editSubjectDetails(((Subject) tbRow.getTag()).getShortName(), ((Subject) tbRow.getTag()).getName(), ((Subject) tbRow.getTag()).getCode(), ((Subject) tbRow.getTag()).getSemester()));

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
        startActivity(new Intent(SubjectsActivity.this, HomeActivity.class));
    }
}