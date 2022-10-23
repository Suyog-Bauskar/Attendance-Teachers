package com.suyogbauskar.attenteachers.fragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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
import com.suyogbauskar.attenteachers.R;
import com.suyogbauskar.attenteachers.pojos.Student;

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

public class StatisticsFragment extends Fragment implements View.OnClickListener {
    private Button excelBtn;
    private List<Student> students = new ArrayList<>();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Map<String, Map<String, Map<String, Map<String, Object>>>> allMonthsAndChildren;
    private XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
    private XSSFSheet xssfSheet;
    private XSSFRow xssfRow;
    private XSSFCell xssfCell;
    private final Map<String, Map<String, List<Student>>> requiredPresentData = new HashMap<>();

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

                                

                                int counter = 0;
                                Map<String, List<Student>> tempMap = new HashMap<>();
                                String monthName, firstname = "", lastname = "", dayName = "";
                                int rollNo = 0;
                                List<Student> tempStudentList = new ArrayList<>();

                                allMonthsAndChildren = (Map<String, Map<String, Map<String, Map<String, Object>>>>) snapshot.getValue();

                                for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> entry1 : allMonthsAndChildren.entrySet()) {
                                    Log.d(TAG, "Month: " + entry1.getKey());

                                    monthName = entry1.getKey();

                                    for (Map.Entry<String, Map<String, Map<String, Object>>> entry2 : entry1.getValue().entrySet()) {
                                        Log.d(TAG, "Day: " + entry2.getKey());
                                        dayName = entry2.getKey();

                                        for (Map.Entry<String, Map<String, Object>> entry3 : entry2.getValue().entrySet()) {
                                            Log.d(TAG, "UID: " + entry3.getKey());

                                            for (Map.Entry<String, Object> entry4 : entry3.getValue().entrySet()) {
//                                                Log.d(TAG, entry4.getKey() + " - " + entry4.getValue());
                                                if (entry4.getKey().equals("firstname")) {
                                                    firstname = entry4.getValue().toString();
                                                    counter++;
                                                } else if (entry4.getKey().equals("lastname")) {
                                                    lastname = entry4.getValue().toString();
                                                    counter++;
                                                } else if (entry4.getKey().equals("rollNo")) {
                                                    rollNo = Integer.parseInt(entry4.getValue().toString());
                                                    counter++;
                                                }

                                                if (counter % 3 == 0) {
                                                    tempStudentList.add(new Student(firstname, lastname, rollNo));
                                                }
                                            }

                                        }
                                        tempMap.put(dayName, tempStudentList);
//                                        if (tempStudentList.size() > 0) {
//                                            tempStudentList.clear();
//                                        }
                                        tempStudentList = new ArrayList<>();
                                    }
//                                    Log.d(TAG, "--------------------------------------");
                                    requiredPresentData.put(monthName, tempMap);
//                                    if (tempMap.size() > 0) {
//                                        tempMap.clear();
//                                    }
                                    tempMap = new HashMap<>();
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
        fillAttendance();
        writeExcelDataToFile();
        autoSizeAllColumns(xssfWorkbook);
    }

    private void fillAttendance() {
        int excelRollNo, rollNoFromList, listRollNoIndex = 0, totalRows, columnNo = 0, rowNo;
        String excelDayName;
        List<Student> tempStudentList = new ArrayList<>();

//        for (Map.Entry<String, Map<String, List<Student>>> entry1: requiredPresentData.entrySet()) {
//            Log.d(TAG, "Month Name: " + entry1.getKey());
//
//            for (Map.Entry<String, List<Student>> entry2: entry1.getValue().entrySet()) {
//                Log.d(TAG, "Day Name: " + entry2.getKey());
//
//                for (Student  s: entry2.getValue()) {
//                    Log.d(TAG, "Student: " + s.getFirstname());
//                }
//            }
//            Log.d(TAG, "-----------------------------------------------------");
//        }

        for (Map.Entry<String, Map<String, List<Student>>> entry1: requiredPresentData.entrySet()) {
            //Month names
            Log.d(TAG, "Month: " + entry1.getKey());

            xssfSheet = xssfWorkbook.getSheet(entry1.getKey());

            totalRows = xssfSheet.getLastRowNum();

            for (Map.Entry<String, List<Student>> entry2: entry1.getValue().entrySet()) {
                //Day names
                Log.d(TAG, "Day: " + entry2.getKey());

                listRollNoIndex = 0;

                //Get column index to write based on day name
                xssfRow = xssfSheet.getRow(0);
                for (int i = 0; i < xssfRow.getLastCellNum(); i++) {
                    xssfCell = xssfRow.getCell(i);
                    excelDayName = xssfCell.getStringCellValue();
                    if (excelDayName.equals(entry2.getKey())) {
                        columnNo = i;
                    }
                }

                if (tempStudentList.size() > 0) {
                    tempStudentList.clear();
                }
                tempStudentList.addAll(entry2.getValue());

                for (rowNo = 1; rowNo < totalRows + 1; rowNo++) {
                    try {
                        xssfRow = xssfSheet.getRow(rowNo);
                        xssfCell = xssfRow.getCell(0);
                        excelRollNo = (int) xssfCell.getNumericCellValue();
                        rollNoFromList = tempStudentList.get(listRollNoIndex).getRollNo();

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

    private void writeExcelDataToFile() {
        //TODO : Year is hardcoded, change it to take input from user
        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String filename = snapshot.child("subject_name").getValue(String.class) + " Attendance 2022";
                        File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + filename + ".xlsx");

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
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void fillStaticData() {
        int rowNo, columnNo;

        for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> entry1 : allMonthsAndChildren.entrySet()) {
//            Log.d(TAG, "Month: " + entry1.getKey());

            rowNo = 0;
            columnNo = 0;

            xssfSheet = xssfWorkbook.createSheet(entry1.getKey());

            xssfRow = xssfSheet.createRow(rowNo);

            xssfCell = xssfRow.createCell(columnNo);
            xssfCell.setCellValue("Roll No");

            columnNo++;

            xssfCell = xssfRow.createCell(columnNo);
            xssfCell.setCellValue("Name");

            columnNo++;

            for (Map.Entry<String, Map<String, Map<String, Object>>> entry2 : entry1.getValue().entrySet()) {
//                Log.d(TAG, "Day: " + entry2.getKey());

                xssfCell = xssfRow.createCell(columnNo);
                xssfCell.setCellValue(entry2.getKey());

                columnNo++;

            }

            //Filling All Students Data
            for (Student student : students) {
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