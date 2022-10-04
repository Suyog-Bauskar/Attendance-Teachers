package com.suyogbauskar.attenteachers;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    private List<Student> allStudents = new ArrayList<>(70);
    private List<MonthsSubCollPresentStudents> monthsSubCollPresentStudentsList = new ArrayList<>(12);
    private List<StudentDataAttendance> presentStudentsList = new ArrayList<>();
    private File filePath;
    private CollectionReference yearRef;
    private Query rollNoQuery;
    private XSSFWorkbook xssfWorkbook;
    private XSSFSheet xssfSheet;
    private XSSFRow xssfRow;
    private XSSFCell xssfCell;
    private int rowNo, columnNo, listIndex;
    private String filename;

    //New
    private Map<String, List<StudentDataAttendance>> tempMap;
    private List<Map<String, List<StudentDataAttendance>>> tempListForMap;
    private MonthsSubCollPresentStudents presentStudents;

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

        AfterStarting afterStarting = new AfterStarting();
        afterStarting.execute();

//        yearRef = db.collection("attendance").document(HomeActivity.subjectCodeDB).collection("2022");
//
//        getAllStudentsData();
//
//        //TODO : Year is hardcoded, change it to take input from user
//        yearRef.get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            List<String> tempList = (List<String>) document.get("sub_collections_name");
//                            monthsAndSubCollectionsName.put(document.getId(), tempList);
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

    private void fillExcelStaticData() {
        //Months
            for (Map.Entry<String, List<String>> entry : monthsAndSubCollectionsName.entrySet()) {

                //Headers
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
            }
    }

//    private void createExcelFile() {
//
//        xssfWorkbook = new XSSFWorkbook();
//
//        //Months
//        for (Map.Entry<String, List<String>> entry : monthsAndSubCollectionsName.entrySet()) {
//
//            //Headers
//            rowNo = 0;
//            columnNo = 0;
//            xssfSheet = xssfWorkbook.createSheet(entry.getKey());
//
//            xssfRow = xssfSheet.createRow(rowNo);
//
//            xssfCell = xssfRow.createCell(columnNo);
//            xssfCell.setCellValue("Roll No");
//
//            columnNo++;
//
//            xssfCell = xssfRow.createCell(columnNo);
//            xssfCell.setCellValue("Name");
//
//            columnNo++;
//
//            for (String daysStr : entry.getValue()) {
//                xssfCell = xssfRow.createCell(columnNo);
//                xssfCell.setCellValue(daysStr);
//
//                columnNo++;
//            }
//
//            //Filling All Students Data
//            for (Student student : allStudents) {
//                rowNo++;
//                columnNo = 0;
//
//                xssfRow = xssfSheet.createRow(rowNo);
//
//                xssfCell = xssfRow.createCell(columnNo);
//                xssfCell.setCellValue(student.getRollNo());
//                columnNo++;
//
//                xssfCell = xssfRow.createCell(columnNo);
//                xssfCell.setCellValue(student.getFirstname() + " " + student.getLastname());
//            }
//
//            columnNo = 2;
//
//            for (String daysStr : entry.getValue()) {
//                rollNoQuery = yearRef.document(entry.getKey()).collection(daysStr).orderBy("rollNo");
//
//                rollNoQuery.get()
//                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                            @Override
//                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
//                                for (DocumentSnapshot snapshot : snapshotList) {
//                                    StudentDataAttendance studentDataAttendance = snapshot.toObject(StudentDataAttendance.class);
//
//                                    particularDayPresentStudents.add(studentDataAttendance);
//                                }
//
//                                listIndex = 0;
//
//                                for (int rowNo = 1; rowNo < xssfSheet.getLastRowNum() + 1; rowNo++) {
//                                    xssfRow = xssfSheet.getRow(rowNo);
//                                    xssfCell = xssfRow.getCell(0);
//
//                                    int checkingRollNo = (int) xssfCell.getNumericCellValue();
//
//                                    if ((listIndex < particularDayPresentStudents.size()) && (checkingRollNo == particularDayPresentStudents.get(listIndex).getRollNo())) {
//                                        xssfCell = xssfRow.createCell(columnNo);
//                                        xssfCell.setCellValue("True");
//                                    }
//                                    listIndex++;
//                                }
//                            }
//                        });
//                columnNo++;
//            }
//
//        }
//
//        autoSizeAllColumns(xssfWorkbook);
//
//        //TODO : Year is hardcoded, change it to take input from user
//        filename = HomeActivity.subjectNameDB + " Attendance 2022";
//        filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + filename + ".xlsx");
//
//        try {
//
//            filePath.createNewFile();
//
//            FileOutputStream outputStream = new FileOutputStream(filePath);
//            xssfWorkbook.write(outputStream);
//            outputStream.flush();
//            outputStream.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.excelBtn:
                CreateExcelFile createExcelFile = new CreateExcelFile();
                createExcelFile.execute();
//                createExcelFile();
                break;
        }
    }

    private class AfterStarting extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            yearRef = db.collection("attendance").document(HomeActivity.subjectCodeDB).collection("2022");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            getAllStudentsData();

            //TODO : Year is hardcoded, change it to take input from user
            yearRef.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                List<String> tempList = (List<String>) document.get("sub_collections_name");
                                monthsAndSubCollectionsName.put(document.getId(), tempList);
                            }

                            for (Map.Entry<String, List<String>> entry : monthsAndSubCollectionsName.entrySet()) {
                                String monthName = entry.getKey();

                                for (String daysName : entry.getValue()) {
                                    rollNoQuery = yearRef.document(entry.getKey()).collection(daysName).orderBy("rollNo");

                                    rollNoQuery.get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                                                    for (DocumentSnapshot snapshot : snapshotList) {
                                                        StudentDataAttendance studentDataAttendance = snapshot.toObject(StudentDataAttendance.class);

                                                        presentStudentsList.add(studentDataAttendance);
                                                    }

                                                    tempMap = new HashMap<>();

                                                    List<StudentDataAttendance> tempStudentList = new ArrayList<>();

                                                    for (StudentDataAttendance s: presentStudentsList) {
                                                        tempStudentList.add(new StudentDataAttendance(s.getRollNo(), s.getFirstname(), s.getLastname()));
                                                    }

                                                    tempMap.put(daysName, tempStudentList);

                                                    if (presentStudentsList != null) {
                                                        presentStudentsList.clear();
                                                    }

                                                    tempListForMap = new ArrayList<>();
                                                    tempListForMap.add(tempMap);


                                                    presentStudents = new MonthsSubCollPresentStudents(monthName, tempListForMap);

                                                    //TODO : Add day to this month
                                                    for (int i = 0; i < monthsSubCollPresentStudentsList.size(); i++) {
                                                        if (monthsSubCollPresentStudentsList.get(i).getMonthName() == monthName) {
                                                            monthsSubCollPresentStudentsList.remove(i);
                                                        }
                                                    }
                                                    monthsSubCollPresentStudentsList.add(presentStudents);
                                                }
                                            });
                                }
                                //
                            }
                        }
                    });

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }
    }

    private class CreateExcelFile extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            xssfWorkbook = new XSSFWorkbook();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            fillExcelStaticData();

            int excelRollNo, rollNoFromList, listRollNoIndex, totalRows;
            List<StudentDataAttendance> tempListForStudentAttendance;

            Log.d(TAG, "doInBackground: " + monthsSubCollPresentStudentsList);

            for (MonthsSubCollPresentStudents students: monthsSubCollPresentStudentsList) {
//                Log.d(TAG, "Month: " + students.getMonthName());

                xssfSheet = xssfWorkbook.getSheet(students.getMonthName());

                columnNo = 2;
                totalRows = xssfSheet.getLastRowNum();

                for (Map<String, List<StudentDataAttendance>> list: students.getDayNameAndPresentStudents()) {
                    for (Map.Entry<String, List<StudentDataAttendance>> map: list.entrySet()) {
//                        Log.d(TAG, "Day Name: " + map.getKey());

                        tempListForStudentAttendance = map.getValue();
                        rollNoFromList = 0;
                        listRollNoIndex = 0;

                        for (rowNo = 1; rowNo < totalRows + 1; rowNo++) {
                            try {
                                xssfRow = xssfSheet.getRow(rowNo);
                                xssfCell = xssfRow.getCell(0);
                                excelRollNo = (int) xssfCell.getNumericCellValue();
                                rollNoFromList = tempListForStudentAttendance.get(listRollNoIndex).getRollNo();

//                                Log.d(TAG, "excelRollNo: " + excelRollNo);
//                                Log.d(TAG, "rollNoFromList: " + rollNoFromList);
                                if (excelRollNo == rollNoFromList) {
                                    xssfCell = xssfRow.createCell(columnNo);
                                    xssfCell.setCellValue("P");
                                    listRollNoIndex++;
                                } else if (excelRollNo > rollNoFromList) {
                                    rowNo--;
                                    listRollNoIndex++;
                                }

                            } catch (IndexOutOfBoundsException e) {
                                break;
                            }
                        }
                        columnNo++;
                    }
                }
            }

            autoSizeAllColumns(xssfWorkbook);

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

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
    }
}