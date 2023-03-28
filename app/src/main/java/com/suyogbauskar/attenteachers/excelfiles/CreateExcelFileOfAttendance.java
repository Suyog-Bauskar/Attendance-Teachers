package com.suyogbauskar.attenteachers.excelfiles;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.R;
import com.suyogbauskar.attenteachers.pojos.StudentData;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CreateExcelFileOfAttendance extends Service {

    private String year, subjectCode, subjectName, department;
    private int semester;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences sharedPreferences = getSharedPreferences("excelValuesPref", MODE_PRIVATE);
        year = sharedPreferences.getString("year", "");
        subjectCode = sharedPreferences.getString("subjectCode", "");
        subjectName = sharedPreferences.getString("subjectName", "");
        semester = sharedPreferences.getInt("semester", 0);
        SharedPreferences sharedPreferences2 = getSharedPreferences("teacherDataPref", MODE_PRIVATE);
        department = sharedPreferences2.getString("department", "");

        getAllStudentsData();

        return super.onStartCommand(intent, flags, startId);
    }

    private void getAllStudentsData() {
        FirebaseDatabase.getInstance().getReference("/students_data")
                .orderByChild("queryStringSemester").equalTo(department + semester)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<StudentData> studentsListA = new ArrayList<>();
                        List<StudentData> studentsListB = new ArrayList<>();
                        List<StudentData> studentsListC = new ArrayList<>();
                        List<StudentData> studentsListA1 = new ArrayList<>();
                        List<StudentData> studentsListA2 = new ArrayList<>();
                        List<StudentData> studentsListA3 = new ArrayList<>();
                        List<StudentData> studentsListB1 = new ArrayList<>();
                        List<StudentData> studentsListB2 = new ArrayList<>();
                        List<StudentData> studentsListB3 = new ArrayList<>();
                        List<StudentData> studentsListC1 = new ArrayList<>();
                        List<StudentData> studentsListC2 = new ArrayList<>();
                        List<StudentData> studentsListC3 = new ArrayList<>();

                        for (DataSnapshot dsp : snapshot.getChildren()) {
                            String firstname = dsp.child("firstname").getValue(String.class);
                            String lastname = dsp.child("lastname").getValue(String.class);
                            int rollNo = dsp.child("rollNo").getValue(Integer.class);

                            if (dsp.child("division").getValue(String.class).equals("A")) {
                                studentsListA.add(new StudentData(rollNo, firstname, lastname));

                                if (dsp.child("batch").getValue(Integer.class) == 1) {
                                    studentsListA1.add(new StudentData(rollNo, firstname, lastname));
                                } else if (dsp.child("batch").getValue(Integer.class) == 2) {
                                    studentsListA2.add(new StudentData(rollNo, firstname, lastname));
                                } else if (dsp.child("batch").getValue(Integer.class) == 3) {
                                    studentsListA3.add(new StudentData(rollNo, firstname, lastname));
                                }
                            } else if (dsp.child("division").getValue(String.class).equals("B")) {
                                studentsListB.add(new StudentData(rollNo, firstname, lastname));

                                if (dsp.child("batch").getValue(Integer.class) == 1) {
                                    studentsListB1.add(new StudentData(rollNo, firstname, lastname));
                                } else if (dsp.child("batch").getValue(Integer.class) == 2) {
                                    studentsListB2.add(new StudentData(rollNo, firstname, lastname));
                                } else if (dsp.child("batch").getValue(Integer.class) == 3) {
                                    studentsListB3.add(new StudentData(rollNo, firstname, lastname));
                                }
                            } else if (dsp.child("division").getValue(String.class).equals("C")) {
                                studentsListC.add(new StudentData(rollNo, firstname, lastname));

                                if (dsp.child("batch").getValue(Integer.class) == 1) {
                                    studentsListC1.add(new StudentData(rollNo, firstname, lastname));
                                } else if (dsp.child("batch").getValue(Integer.class) == 2) {
                                    studentsListC2.add(new StudentData(rollNo, firstname, lastname));
                                } else if (dsp.child("batch").getValue(Integer.class) == 3) {
                                    studentsListC3.add(new StudentData(rollNo, firstname, lastname));
                                }
                            }
                        }

                        studentsListA.sort(Comparator.comparingInt(StudentData::getRollNo));
                        studentsListB.sort(Comparator.comparingInt(StudentData::getRollNo));
                        studentsListB.sort(Comparator.comparingInt(StudentData::getRollNo));
                        studentsListA1.sort(Comparator.comparingInt(StudentData::getRollNo));
                        studentsListA2.sort(Comparator.comparingInt(StudentData::getRollNo));
                        studentsListA3.sort(Comparator.comparingInt(StudentData::getRollNo));
                        studentsListB1.sort(Comparator.comparingInt(StudentData::getRollNo));
                        studentsListB2.sort(Comparator.comparingInt(StudentData::getRollNo));
                        studentsListB3.sort(Comparator.comparingInt(StudentData::getRollNo));
                        studentsListC1.sort(Comparator.comparingInt(StudentData::getRollNo));
                        studentsListC2.sort(Comparator.comparingInt(StudentData::getRollNo));
                        studentsListC3.sort(Comparator.comparingInt(StudentData::getRollNo));

                        createExcelFile(studentsListA,"A",0, new XSSFWorkbook());
                        createExcelFile(studentsListB,"B",10, new XSSFWorkbook());
                        createExcelFile(studentsListC,"C",20, new XSSFWorkbook());
                        createExcelFile(studentsListA1,"A1",30, new XSSFWorkbook());
                        createExcelFile(studentsListA2,"A2",40, new XSSFWorkbook());
                        createExcelFile(studentsListA3,"A3",50, new XSSFWorkbook());
                        createExcelFile(studentsListB1,"B1",60, new XSSFWorkbook());
                        createExcelFile(studentsListB2,"B2",70, new XSSFWorkbook());
                        createExcelFile(studentsListB3,"B3",80, new XSSFWorkbook());
                        createExcelFile(studentsListC1,"C1",90, new XSSFWorkbook());
                        createExcelFile(studentsListC2,"C2",100, new XSSFWorkbook());
                        createExcelFile(studentsListC3,"C3",110, new XSSFWorkbook());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        sendErrorNotification(error.getMessage(), 7);
                    }
                });
    }

    private void createExcelFile(List<StudentData> studentsList, String className, int errorCode, XSSFWorkbook xssfWorkbook) {
            FirebaseDatabase.getInstance().getReference("attendance/" + department + semester + "-" + className + "/" + subjectCode + "/" + year)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Map<String, Map<String, List<StudentData>>> requiredPresentData = new HashMap<>();

                            if (!snapshot.exists()) {
                                return;
                            }
                            int counter = 0;
                            Map<String, List<StudentData>> tempMap = new HashMap<>();
                            String monthName, firstname = "", lastname = "", dayName = "";
                            int rollNo = 0;
                            List<StudentData> tempStudentList = new ArrayList<>();

                            Map<String, Map<String, Map<String, Map<String, Object>>>> allMonthsAndChildren = (Map<String, Map<String, Map<String, Map<String, Object>>>>) snapshot.getValue();

                            for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> entry1 : allMonthsAndChildren.entrySet()) {
                                //Month
                                monthName = entry1.getKey();

                                for (Map.Entry<String, Map<String, Map<String, Object>>> entry2 : entry1.getValue().entrySet()) {
                                    //Day
                                    dayName = entry2.getKey();

                                    for (Map.Entry<String, Map<String, Object>> entry3 : entry2.getValue().entrySet()) {
                                        //UID

                                        for (Map.Entry<String, Object> entry4 : entry3.getValue().entrySet()) {
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
                                                tempStudentList.add(new StudentData(rollNo, firstname, lastname));
                                            }
                                        }

                                    }
                                    tempMap.put(dayName, tempStudentList);
                                    tempStudentList = new ArrayList<>();
                                }
                                requiredPresentData.put(monthName, tempMap);
                                tempMap = new HashMap<>();
                            }
                            fillStaticData(allMonthsAndChildren, studentsList, xssfWorkbook, errorCode);
                            fillAttendance(requiredPresentData, xssfWorkbook);
                            calculatePercentage(requiredPresentData, xssfWorkbook);
                            autoSizeAllColumns(xssfWorkbook);
                            writeExcelDataToFile(year, xssfWorkbook, errorCode, className);
                            sendNotificationOfExcelFileCreated(errorCode, className);
                            stopService(new Intent(getApplicationContext(), CreateExcelFileOfAttendance.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            sendErrorNotification(error.getMessage(), errorCode + 4);
                        }
                    });
    }

    private void fillAttendance(Map<String, Map<String, List<StudentData>>> requiredPresentData, XSSFWorkbook xssfWorkbook) {
        int excelRollNo, rollNoFromList, listRollNoIndex, totalRows, columnNo = 0, rowNo;
        String excelDayName;
        List<StudentData> tempStudentList = new ArrayList<>();
        XSSFSheet xssfSheet;
        XSSFRow xssfRow;
        XSSFCell xssfCell;

        for (Map.Entry<String, Map<String, List<StudentData>>> entry1 : requiredPresentData.entrySet()) {
            //Month names
            xssfSheet = xssfWorkbook.getSheet(entry1.getKey());

            totalRows = xssfSheet.getLastRowNum();

            for (Map.Entry<String, List<StudentData>> entry2 : entry1.getValue().entrySet()) {
                //Day names
                listRollNoIndex = 0;

                //Get column index to write based on day name
                xssfRow = xssfSheet.getRow(0);
                for (int i = 0; i < xssfRow.getLastCellNum(); i++) {
                    xssfCell = xssfRow.getCell(i);
                    excelDayName = xssfCell.getStringCellValue();
                    if (excelDayName.equals(entry2.getKey())) {
                        columnNo = i;
                        break;
                    }
                }

                if (tempStudentList.size() > 0) {
                    tempStudentList.clear();
                }
                tempStudentList.addAll(entry2.getValue());
                tempStudentList.sort(Comparator.comparingInt(StudentData::getRollNo));

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

    private void calculatePercentage(Map<String, Map<String, List<StudentData>>> requiredPresentData, XSSFWorkbook xssfWorkbook) {
        int totalLectures, studentAttendance, totalRows, totalColumns, rowNo, columnNo;
        float percentage;
        String cellValue;
        XSSFSheet xssfSheet;
        XSSFRow xssfRow;
        XSSFCell xssfCell;

        for (Map.Entry<String, Map<String, List<StudentData>>> entry1 : requiredPresentData.entrySet()) {
            //Month names
            xssfSheet = xssfWorkbook.getSheet(entry1.getKey());

            totalRows = xssfSheet.getLastRowNum() + 1;
            xssfRow = xssfSheet.getRow(0);
            totalColumns = xssfRow.getLastCellNum() - 1;
            totalLectures = xssfRow.getLastCellNum() - 3;

            for (rowNo = 1; rowNo < totalRows; rowNo++) {
                xssfRow = xssfSheet.getRow(rowNo);
                studentAttendance = 0;
                for (columnNo = 2; columnNo < totalColumns; columnNo++) {
                    xssfCell = xssfRow.getCell(columnNo);
                    if (xssfCell != null) {
                        cellValue = xssfCell.getStringCellValue();

                        if (cellValue.equals("P")) {
                            studentAttendance++;
                        }
                    }
                }

                percentage = studentAttendance / (float) totalLectures;
                percentage *= 100;

                xssfCell = xssfRow.createCell(columnNo);
                xssfCell.setCellValue(percentage);
            }
        }
    }

    private void writeExcelDataToFile(String year, XSSFWorkbook xssfWorkbook, int errorCode, String className) {
        String filename = subjectName + " " + year + " " + department + semester + "-" + className;

        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers");
            dir.mkdir();
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers/Attendance");
            dir.mkdir();
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers/Attendance/" + department + semester);
            dir.mkdir();

            File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers/Attendance/" + department + semester + "/" + filename + ".xlsx");

            filePath.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(filePath);
            xssfWorkbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            sendErrorNotification(e.getMessage(), errorCode + 3);
        }
    }

    private void fillStaticData(Map<String, Map<String, Map<String, Map<String, Object>>>> allMonthsAndChildren, List<StudentData> students, XSSFWorkbook xssfWorkbook, int errorCode) {
        int rowNo, columnNo;
        XSSFSheet xssfSheet;
        XSSFRow xssfRow;
        XSSFCell xssfCell;

        for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> entry1 : allMonthsAndChildren.entrySet()) {

            rowNo = 0;
            columnNo = 0;

            try {
                xssfSheet = xssfWorkbook.createSheet(entry1.getKey());
            } catch (IllegalArgumentException e) {
                sendErrorNotification(e.getMessage(), errorCode + 2);
                return;
            }

            xssfRow = xssfSheet.createRow(rowNo);

            xssfCell = xssfRow.createCell(columnNo);
            xssfCell.setCellValue("Roll No");

            columnNo++;

            xssfCell = xssfRow.createCell(columnNo);
            xssfCell.setCellValue("Name");

            columnNo++;

            List<Float> dayNameFloatList = new ArrayList<>();
            List<String> lastCharacters = new ArrayList<>();

            for (Map.Entry<String, Map<String, Map<String, Object>>> entry2 : entry1.getValue().entrySet()) {
                if (entry2.getKey().charAt(entry2.getKey().length() - 1) == '0') {
                    lastCharacters.add("0");
                } else {
                    lastCharacters.add("");
                }
                dayNameFloatList.add(Float.parseFloat(entry2.getKey().replace("-", ".")));
            }

            Collections.sort(dayNameFloatList);

            String dayNameInStr;
            int i = 0;
            for (Float f : dayNameFloatList) {
                dayNameInStr = f.toString().replace(".", "-");
                dayNameInStr += lastCharacters.get(i);
                i++;
                xssfCell = xssfRow.createCell(columnNo);
                xssfCell.setCellValue(dayNameInStr);

                columnNo++;
            }

            xssfCell = xssfRow.createCell(columnNo);
            xssfCell.setCellValue("Percentage");

            //Filling All Students Data
            for (StudentData student : students) {
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
                    if (columnIndex == 0) {
                        sheet.setColumnWidth(columnIndex, 2000);
                    } else if (columnIndex == 1) {
                        sheet.setColumnWidth(columnIndex, 5500);
                    } else {
                        sheet.setColumnWidth(columnIndex, 3000);
                    }
                }
            }
        }
    }

    private void sendNotificationOfExcelFileCreated(int id, String className) {
        Uri selectedUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers/Attendance/" + department + semester);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "File")
                .setSmallIcon(R.drawable.raw_logo)
                .setContentText("Excel file of " + subjectName + " " + year + " " + department + semester + "-" + className + " saved in downloads folder")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Excel file of " + subjectName + " " + year + " " + department + semester + "-" + className + " saved in downloads folder"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify(id, builder.build());
    }

    private void sendErrorNotification(String error, int id) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Error")
                .setSmallIcon(R.drawable.raw_logo)
                .setContentText(error)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(error))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(this).notify(id, builder.build());
    }
}
