package com.suyogbauskar.attenteachers;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity implements View.OnClickListener {

    private BottomNavigationView bottomNav;
    private Button excelBtn;
    private FirebaseFirestore db;
    private Map<String, List<String>> monthsAndSubCollectionsName = new HashMap<>();
    private List<Student> allStudents = new ArrayList<>(100);
    private File filePath;
    private CollectionReference yearRef;
    private Query rollNoQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        toolbar.setTitle("Profile");
        setSupportActionBar(toolbar);

        bottomNav = findViewById(R.id.bottomNavigationView);
        excelBtn = findViewById(R.id.excelBtn);

        excelBtn.setOnClickListener(this);

        db = FirebaseFirestore.getInstance();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        bottomNav.setSelectedItemId(R.id.statistics);
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    overridePendingTransition(0, 0);
                    return true;

                case R.id.statistics:
                    return true;

                case R.id.settings:
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        yearRef = db.collection("attendance").document(HomeActivity.subjectCodeDB).collection("2022");

        getAllStudentsData();

        //TODO : Year is hardcoded, change it to take input from user
        yearRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            List<String> tempList = (List<String>) document.get("sub_collections_name");
                            monthsAndSubCollectionsName.put(document.getId(), tempList);
                        }
                    }
                });

//        rollNoQuery = yearRef.orderBy("rollNo");
//
//        rollNoQuery.get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
//                        for (DocumentSnapshot snapshot: snapshotList) {
//                            Log.d(TAG, "onSuccess: " + snapshot.get("rollNo"));
//                        }
//                    }
//                });
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

    private void getAllStudentsData() {
        CollectionReference dataCollRef = db.collection("data");
        Query sortedAllRollNo = dataCollRef.orderBy("rollNo");

        sortedAllRollNo.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot snapshot : snapshotList) {
                        String firstname = snapshot.getString("firstname");
                        String lastname = snapshot.getString("lastname");
                        String enrollNo = snapshot.getString("enrollNo");
                        String uid = snapshot.getString("uid");
                        int rollNo = Integer.parseInt(String.valueOf(snapshot.get("rollNo")));

                        allStudents.add(new Student(firstname, lastname, enrollNo, uid, rollNo));
                    }
                });
    }

    private void createExcelFile() {

        String filename;
        int rowNo, columnNo;
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet;
        XSSFRow xssfRow;
        XSSFCell xssfCell;

        for (Student s: allStudents) {
            Log.d(TAG, "rollNo: " + s.getRollNo());
        }
        //Months
        for (Map.Entry<String, List<String>> entry : monthsAndSubCollectionsName.entrySet()) {

            rowNo = 0;
            columnNo = 0;
            xssfSheet = xssfWorkbook.createSheet(entry.getKey());

            xssfRow = xssfSheet.createRow(rowNo);

            xssfCell = xssfRow.createCell(columnNo);
            xssfCell.setCellValue("Roll No");

            columnNo++;

            xssfCell = xssfRow.createCell(columnNo);
            xssfCell.setCellValue("Name");

            columnNo++;

            for (String daysStr : entry.getValue()) {
                xssfCell = xssfRow.createCell(columnNo);
                xssfCell.setCellValue(daysStr);

                columnNo++;
            }

            //Filling All Students Data
            for (Student student : allStudents) {
                rowNo++;
                columnNo = 0;

                xssfRow = xssfSheet.createRow(rowNo);

                xssfCell = xssfRow.createCell(columnNo);
                xssfCell.setCellValue(student.getRollNo());
                columnNo++;

                xssfCell = xssfRow.createCell(columnNo);
                xssfCell.setCellValue(student.getFirstname() + " " + student.getLastname());
            }

//            for (String s : entry.getValue()) {
//                rollNoQuery = yearRef.document(entry.getKey()).collection(s).orderBy("rollNo");
//
//                rollNoQuery.get()
//                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                            @Override
//                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
//                                for (DocumentSnapshot snapshot : snapshotList) {
//                                    StudentDataAttendance studentDataAttendance = snapshot.toObject(StudentDataAttendance.class);
//                                    Log.d(TAG, "Log: " + entry.getKey() + " = Roll no - " + studentDataAttendance.getRollNo());
//                                }
//                            }
//                        });
//            }
        }

        autoSizeAllColumns(xssfWorkbook);

        //TODO : Year is hardcoded, change it to take input from user
        filename = HomeActivity.subjectNameDB + " Attendance 2022";
        filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + filename + ".xlsx");

        try {

            filePath.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(filePath);
            xssfWorkbook.write(outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.excelBtn:
                createExcelFile();
                break;
        }
    }
}