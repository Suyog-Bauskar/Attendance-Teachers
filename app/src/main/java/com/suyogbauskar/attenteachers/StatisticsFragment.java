package com.suyogbauskar.attenteachers;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.pojos.Student;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment implements View.OnClickListener {
    private Button excelBtn;
    private List<Student> students = new ArrayList<>();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public StatisticsFragment() {
    }

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
                for (DataSnapshot dsp : snapshot.getChildren()) {
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
        getAllMonthsAndChildren();
    }

    private void getAllMonthsAndChildren() {
        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //TODO : year is hardcoded, change it to take from teacher
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("attendance/" +
                                snapshot.child("subject_code").getValue(Long.class) + "/2022");

                        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Map<String, Map<String, Map<String, Map<String, Object>>>> allMonthsAndChildren = (Map<String, Map<String, Map<String, Map<String, Object>>>>) snapshot.getValue();

                                for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> entry1 : allMonthsAndChildren.entrySet()) {
                                    Log.d(TAG, "Month: " + entry1.getKey());

                                    for (Map.Entry<String, Map<String, Map<String, Object>>> entry2 : entry1.getValue().entrySet()) {
                                        Log.d(TAG, "Day: " + entry2.getKey());

                                        for (Map.Entry<String, Map<String, Object>> entry3 : entry2.getValue().entrySet()) {
                                            Log.d(TAG, "UID: " + entry3.getKey());

                                            for (Map.Entry<String, Object> entry4 : entry3.getValue().entrySet()) {
                                                Log.d(TAG, entry4.getKey() + " - " + entry4.getValue());
                                            }

                                        }
                                    }
                                    Log.d(TAG, "--------------------------------------");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
        fillStaticData();
    }

    private void fillStaticData() {

    }

    private void autoSizeAllColumns(Workbook workbook) {
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getPhysicalNumberOfRows() > 0) {
                Row row = sheet.getRow(sheet.getFirstRowNum());
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    if (columnIndex == 1) {
                        sheet.setColumnWidth(columnIndex, 5000);
                    } else {
                        sheet.setColumnWidth(columnIndex, 2000);
                    }
                }
            }
        }
    }

    private void getAllPresentStudentsData() {
    }

}