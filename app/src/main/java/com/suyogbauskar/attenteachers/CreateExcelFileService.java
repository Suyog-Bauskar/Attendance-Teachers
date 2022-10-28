package com.suyogbauskar.attenteachers;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CreateExcelFileService extends Service {

    private List<Student> students;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Map<String, Map<String, Map<String, Map<String, Object>>>> allMonthsAndChildren;
    private XSSFWorkbook xssfWorkbook;
    private XSSFSheet xssfSheet;
    private XSSFRow xssfRow;
    private XSSFCell xssfCell;
    private final Map<String, Map<String, List<Student>>> requiredPresentData = new HashMap<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "Creating Excel File...", Toast.LENGTH_SHORT).show();
        students = new ArrayList<>();
        xssfWorkbook = new XSSFWorkbook();

        createNotificationChannelForFile();
        createNotificationChannelForError();
        getAllStudentsData();

        return super.onStartCommand(intent, flags, startId);
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
                getAllMonthsAndChildren();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                sendErrorNotification(error.getMessage());
            }
        });
    }

    private void getAllMonthsAndChildren() {
        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //TODO : year is hardcoded, change it to take from teacher
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("attendance/" +
                                snapshot.child("subject_code").getValue(String.class) + "/2022");

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
//                                    Log.d(TAG, "Month: " + entry1.getKey());

                                    monthName = entry1.getKey();

                                    for (Map.Entry<String, Map<String, Map<String, Object>>> entry2 : entry1.getValue().entrySet()) {
//                                        Log.d(TAG, "Day: " + entry2.getKey());
                                        dayName = entry2.getKey();

                                        for (Map.Entry<String, Map<String, Object>> entry3 : entry2.getValue().entrySet()) {
//                                            Log.d(TAG, "UID: " + entry3.getKey());

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
                                        tempStudentList = new ArrayList<>();
                                    }
                                    requiredPresentData.put(monthName, tempMap);
                                    tempMap = new HashMap<>();
                                }
                                fillStaticData();
                                fillAttendance();
                                autoSizeAllColumns(xssfWorkbook);
                                writeExcelDataToFile();
                                sendNotificationOfExcelFileCreated();
                                stopService(new Intent(getApplicationContext(), CreateExcelFileService.class));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                sendErrorNotification(error.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        sendErrorNotification(error.getMessage());
                    }
                });
    }

    private void fillAttendance() {
        int excelRollNo, rollNoFromList, listRollNoIndex = 0, totalRows, columnNo = 0, rowNo;
        String excelDayName;
        List<Student> tempStudentList = new ArrayList<>();

        for (Map.Entry<String, Map<String, List<Student>>> entry1: requiredPresentData.entrySet()) {
            //Month names
//            Log.d(TAG, "Month: " + entry1.getKey());

            xssfSheet = xssfWorkbook.getSheet(entry1.getKey());

            totalRows = xssfSheet.getLastRowNum();

            for (Map.Entry<String, List<Student>> entry2: entry1.getValue().entrySet()) {
                //Day names
//                Log.d(TAG, "Day: " + entry2.getKey());

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

                        try {
                            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers");
                            dir.mkdir();
                        } catch (Exception e) {
                            sendErrorNotification(e.getMessage());
                        }
                        File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers/" + filename + ".xlsx");

                        try {

                            filePath.createNewFile();

                            FileOutputStream outputStream = new FileOutputStream(filePath);
                            xssfWorkbook.write(outputStream);
                            outputStream.flush();
                            outputStream.close();

                        } catch (Exception e) {
                            sendErrorNotification(e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        sendErrorNotification(error.getMessage());
                    }
                });
    }

    private void fillStaticData() {
        int rowNo, columnNo;

        for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> entry1 : allMonthsAndChildren.entrySet()) {

            rowNo = 0;
            columnNo = 0;

            try {
                xssfSheet = xssfWorkbook.createSheet(entry1.getKey());
            } catch (IllegalArgumentException e) {
                sendErrorNotification(e.getMessage());
                return;
            }

            xssfRow = xssfSheet.createRow(rowNo);

            xssfCell = xssfRow.createCell(columnNo);
            xssfCell.setCellValue("Roll No");

            columnNo++;

            xssfCell = xssfRow.createCell(columnNo);
            xssfCell.setCellValue("Name");

            columnNo++;

            float dayNameInFloat;
            List<Float> dayNameFloatList = new ArrayList<>();

            for (Map.Entry<String, Map<String, Map<String, Object>>> entry2 : entry1.getValue().entrySet()) {
                dayNameInFloat = Float.parseFloat(entry2.getKey().replace("-", "."));
                dayNameFloatList.add(dayNameInFloat);
            }

            Collections.sort(dayNameFloatList);

            String dayNameInStr;
            for (Float f: dayNameFloatList) {
                dayNameInStr = f.toString().replace(".", "-");
                Log.d(TAG, "Value: " + dayNameInStr);
                xssfCell = xssfRow.createCell(columnNo);
                xssfCell.setCellValue(dayNameInStr);

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

    private void createNotificationChannelForFile() {
        String name = "File";
        String description = "File Notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("File", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void createNotificationChannelForError() {
        String name = "Error";
        String description = "Error Notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("Error", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void sendNotificationOfExcelFileCreated() {
        Uri selectedUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "File")
                .setSmallIcon(R.drawable.raw_logo)
                .setContentText("Excel file saved in downloads folder")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify(0, builder.build());
    }

    private void sendErrorNotification(String error) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Error")
                .setSmallIcon(R.drawable.raw_logo)
                .setContentText(error)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(this).notify(0, builder.build());
    }
}
