package com.suyogbauskar.attenteachers.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.R;
import com.suyogbauskar.attenteachers.utils.ProgressDialog;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AttendanceFragment extends Fragment {

    private FirebaseUser user;
    private TableLayout table;
    private TextView noAttendanceStartedView, totalPresentStudentsView;
    private Button addStudentBtn;
    private boolean isFirstRow;
    private String teacherSubjectCode;
    private int lecturesTakenToday;
    private ProgressDialog progressDialog = new ProgressDialog();

    public AttendanceFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);
        getActivity().setTitle("Live Attendance");

        init(view);
        listenForAttendance();

        return view;
    }

    private void init(View view) {
        user = FirebaseAuth.getInstance().getCurrentUser();

        findAllViews(view);
        setListeners();
    }

    private void findAllViews(View view) {
        table = view.findViewById(R.id.table);
        noAttendanceStartedView = view.findViewById(R.id.noAttendanceStartedView);
        totalPresentStudentsView = view.findViewById(R.id.totalPresentStudentsView);
        addStudentBtn = view.findViewById(R.id.addStudentBtn);
    }

    private void setListeners() {
        addStudentBtn.setOnClickListener(view -> addStudentToAttendance());
    }

    private void addStudentToAttendance() {

    }

    private void listenForAttendance() {
        progressDialog.show(getContext());

        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        teacherSubjectCode = snapshot.child("subject_code").getValue(String.class);
                        lecturesTakenToday = snapshot.child("lectures_taken_today").getValue(Integer.class);

                        FirebaseDatabase.getInstance().getReference("attendance/active_attendance/subject_code")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        progressDialog.hide();
                                        if (teacherSubjectCode.equals(snapshot.getValue(String.class))) {
                                            addStudentBtn.setVisibility(View.VISIBLE);
                                            drawTableHeader();

                                            long currentDate = System.currentTimeMillis();
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy/HH/mm/ss/MMMM", Locale.getDefault());
                                            String dateStr = dateFormat.format(currentDate);
                                            String[] dateArr = dateStr.split("/");
                                            int date = Integer.parseInt(dateArr[0]);
                                            int year = Integer.parseInt(dateArr[2]);
                                            String monthStr = dateArr[6];

                                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("/attendance/" + teacherSubjectCode + "/" +
                                                    year + "/" + monthStr).child(date + "-" + lecturesTakenToday);

                                            Query rollNoQuery = dbRef.orderByChild("rollNo");

                                            rollNoQuery.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (!snapshot.exists()) {
                                                        table.removeViews(1, table.getChildCount() - 1);
                                                    }
                                                    for (DataSnapshot dsp : snapshot.getChildren()) {
                                                        createTableRow(dsp.child("rollNo").getValue(Integer.class), dsp.child("firstname").getValue(String.class) + " " + dsp.child("lastname").getValue(String.class));
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    progressDialog.hide();
                                                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            noAttendanceStartedView.setVisibility(View.VISIBLE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        progressDialog.hide();
                                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.hide();
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void drawTableHeader() {
        TableRow tbRow = new TableRow(getContext());

        TextView tv0 = new TextView(getContext());
        TextView tv1 = new TextView(getContext());
        TextView tv2 = new TextView(getContext());

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
        TableRow tbRow = new TableRow(getContext());

        tbRow.setTag(rollNo);

        TextView tv0 = new TextView(getContext());
        TextView tv1 = new TextView(getContext());
        TextView tv2 = new TextView(getContext());

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
}