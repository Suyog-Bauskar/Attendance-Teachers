package com.suyogbauskar.attenteachers;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment implements View.OnClickListener {
    private Button excelBtn;
    private List<Student> students = new ArrayList<>();

    public StatisticsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        getActivity().setTitle("Statistics");

        findAllViews(view);
        setOnClickListeners();

        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        return view;
    }

    private void findAllViews(View view) {
        excelBtn = view.findViewById(R.id.excelBtn);
    }

    private void setOnClickListeners() {
        excelBtn.setOnClickListener(this);
    }

    private void getAllStudentsData() {

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("/students_data");
        Query rollNoQuery = databaseRef.orderByChild("rollNo");

        rollNoQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dsp: snapshot.getChildren()) {
                    String firstname = dsp.child("firstname").getValue(String.class);
                    String lastname = dsp.child("lastname").getValue(String.class);
                    int rollNo = dsp.child("rollNo").getValue(Integer.class);
                    students.add(new Student(firstname, lastname, rollNo));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getAllStudentsData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.excelBtn:
                createExcelFile();
                break;
        }
    }

    private void createExcelFile() {
        //TODO : year is hardcoded, change it to take from teacher

    }

    private void getAllPresentStudentsData() {
    }

}