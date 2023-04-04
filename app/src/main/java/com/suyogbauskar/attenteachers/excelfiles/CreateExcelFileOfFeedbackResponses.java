package com.suyogbauskar.attenteachers.excelfiles;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Rating;
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CreateExcelFileOfFeedbackResponses extends Service {

    private XSSFWorkbook xssfWorkbook;
    private String department;
    private int semester;
    private List<String> questionsList;
    private Set<String> subjectCodes;
    private List<Float> oneRating, twoRating, threeRating, fourRating, fiveRating, sixRating, sevenRating, eightRating, nineRating;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences sp = getSharedPreferences("teacherDataPref", MODE_PRIVATE);
        department = sp.getString("department", "");

        SharedPreferences sharedPreferences = getSharedPreferences("FeedbackPref", MODE_PRIVATE);
        subjectCodes = sharedPreferences.getStringSet("subjectCodes", new HashSet<>());
        semester = sharedPreferences.getInt("semester", 0);

        xssfWorkbook = new XSSFWorkbook();

        questionsList = new ArrayList<>();
        questionsList.add("Has the teacher covered entire syllabus as prescribed by board?");
        questionsList.add("Has the teacher covered relevant topics beyond syllabus?");
        questionsList.add("Effectiveness of teacher in terms of course content, Communication skills and use of teaching aids.");
        questionsList.add("Pace on which contents were covered.");
        questionsList.add("Motivation and inspiration for students to learn.");
        questionsList.add("Support for the development of students skill, Practical demonstration, Hands on training.");
        questionsList.add("Clarity of exceptions of students.");
        questionsList.add("Feedback provided on student's progress.");
        questionsList.add("Willingness to offer help and advice to students.");

        createExcelFile();

        return super.onStartCommand(intent, flags, startId);
    }

    private void createExcelFile() {
        for (String subjectCode : subjectCodes) {
            FirebaseDatabase.getInstance().getReference("students_data")
                    .orderByChild("queryStringSemester")
                    .equalTo(department + semester)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int rowNo = 0, columnNo = 0;
                            String subjectShortName = "";
                            XSSFSheet xssfSheet;
                            XSSFRow xssfRow;
                            XSSFCell xssfCell;

                            oneRating = new ArrayList<>();
                            twoRating = new ArrayList<>();
                            threeRating = new ArrayList<>();
                            fourRating = new ArrayList<>();
                            fiveRating = new ArrayList<>();
                            sixRating = new ArrayList<>();
                            sevenRating = new ArrayList<>();
                            eightRating = new ArrayList<>();
                            nineRating = new ArrayList<>();

                            for (DataSnapshot dsp : snapshot.getChildren()) {
                                if (dsp.child("subjects").child(subjectCode).child("feedbackSubmitted").getValue(Boolean.class)) {
                                    subjectShortName = dsp.child("subjects").child(subjectCode).child("subjectShortName").getValue(String.class);
                                    oneRating.add(dsp.child("subjects").child(subjectCode).child("oneRating").getValue(Float.class));
                                    twoRating.add(dsp.child("subjects").child(subjectCode).child("twoRating").getValue(Float.class));
                                    threeRating.add(dsp.child("subjects").child(subjectCode).child("threeRating").getValue(Float.class));
                                    fourRating.add(dsp.child("subjects").child(subjectCode).child("fourRating").getValue(Float.class));
                                    fiveRating.add(dsp.child("subjects").child(subjectCode).child("fiveRating").getValue(Float.class));
                                    sixRating.add(dsp.child("subjects").child(subjectCode).child("sixRating").getValue(Float.class));
                                    sevenRating.add(dsp.child("subjects").child(subjectCode).child("sevenRating").getValue(Float.class));
                                    eightRating.add(dsp.child("subjects").child(subjectCode).child("eightRating").getValue(Float.class));
                                    nineRating.add(dsp.child("subjects").child(subjectCode).child("nineRating").getValue(Float.class));
                                }
                            }

                            if (oneRating.size() == 0) {
                                sendErrorNotification("Currently there are no responses submitted by students", Integer.parseInt(subjectCode + "1"));
                                return;
                            }

                            try {
                                xssfSheet = xssfWorkbook.createSheet(subjectShortName);
                            } catch (IllegalArgumentException e) {
                                sendErrorNotification(e.getMessage(), Integer.parseInt(subjectCode + "2"));
                                return;
                            }

                            xssfRow = xssfSheet.createRow(0);
                            xssfCell = xssfRow.createCell(0);
                            xssfCell.setCellValue("Questions / Count");
                            columnNo = 1;
                            for (int i = 1; i <= oneRating.size(); i++) {
                                xssfCell = xssfRow.createCell(columnNo);
                                xssfCell.setCellValue(i);
                                columnNo++;
                            }
                            xssfCell = xssfRow.createCell(columnNo);
                            xssfCell.setCellValue("Average");

                            for (int i = 0; i < 9; i++) {
                                rowNo++;
                                columnNo = 0;

                                xssfRow = xssfSheet.createRow(rowNo);
                                xssfCell = xssfRow.createCell(columnNo);
                                xssfCell.setCellValue(questionsList.get(i));
                                columnNo++;

                                for (int j = 0; j < oneRating.size(); j++) {
                                    switch (i) {
                                        case 0:
                                            xssfCell = xssfRow.createCell(columnNo);
                                            xssfCell.setCellValue(oneRating.get(j));
                                            break;
                                        case 1:
                                            xssfCell = xssfRow.createCell(columnNo);
                                            xssfCell.setCellValue(twoRating.get(j));
                                            break;
                                        case 2:
                                            xssfCell = xssfRow.createCell(columnNo);
                                            xssfCell.setCellValue(threeRating.get(j));
                                            break;
                                        case 3:
                                            xssfCell = xssfRow.createCell(columnNo);
                                            xssfCell.setCellValue(fourRating.get(j));
                                            break;
                                        case 4:
                                            xssfCell = xssfRow.createCell(columnNo);
                                            xssfCell.setCellValue(fiveRating.get(j));
                                            break;
                                        case 5:
                                            xssfCell = xssfRow.createCell(columnNo);
                                            xssfCell.setCellValue(sixRating.get(j));
                                            break;
                                        case 6:
                                            xssfCell = xssfRow.createCell(columnNo);
                                            xssfCell.setCellValue(sevenRating.get(j));
                                            break;
                                        case 7:
                                            xssfCell = xssfRow.createCell(columnNo);
                                            xssfCell.setCellValue(eightRating.get(j));
                                            break;
                                        case 8:
                                            xssfCell = xssfRow.createCell(columnNo);
                                            xssfCell.setCellValue(nineRating.get(j));
                                            break;
                                    }
                                    columnNo++;
                                }

                                if (columnNo == oneRating.size() + 1) {
                                    xssfCell = xssfRow.createCell(columnNo);
                                    switch (i) {
                                        case 0:
                                            xssfCell.setCellValue(getAverageOfList(oneRating));
                                            break;
                                        case 1:
                                            xssfCell.setCellValue(getAverageOfList(twoRating));
                                            break;
                                        case 2:
                                            xssfCell.setCellValue(getAverageOfList(threeRating));
                                            break;
                                        case 3:
                                            xssfCell.setCellValue(getAverageOfList(fourRating));
                                            break;
                                        case 4:
                                            xssfCell.setCellValue(getAverageOfList(fiveRating));
                                            break;
                                        case 5:
                                            xssfCell.setCellValue(getAverageOfList(sixRating));
                                            break;
                                        case 6:
                                            xssfCell.setCellValue(getAverageOfList(sevenRating));
                                            break;
                                        case 7:
                                            xssfCell.setCellValue(getAverageOfList(eightRating));
                                            break;
                                        case 8:
                                            xssfCell.setCellValue(getAverageOfList(nineRating));
                                            break;
                                    }
                                }
                            }

                            autoSizeAllColumns();
                            writeExcelDataToFile(subjectCode);
                            sendNotificationOfExcelFileCreated(Integer.parseInt(subjectCode), subjectShortName);
                            stopService(new Intent(getApplicationContext(), CreateExcelFileOfFeedbackResponses.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            sendErrorNotification(error.getMessage(), Integer.parseInt(subjectCode + "3"));
                        }
                    });
        }
    }

    private float getAverageOfList(List<Float> list) {
        float sum = 0f;

        for (int i = 0; i < list.size(); i++) {
            sum = sum + list.get(i);
        }
        return (sum / list.size());
    }

    private void writeExcelDataToFile(String subjectCode) {
        String filename = department + semester;

        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers");
            dir.mkdir();
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers/Feedback Responses");
            dir.mkdir();

            File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers/Feedback Responses/" + filename + ".xlsx");

            filePath.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(filePath);
            xssfWorkbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            sendErrorNotification(e.getMessage(), Integer.parseInt(subjectCode + "4"));
        }
    }

    private void autoSizeAllColumns() {
        int numberOfSheets = xssfWorkbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = xssfWorkbook.getSheetAt(i);
            if (sheet.getPhysicalNumberOfRows() > 0) {
                Row row = sheet.getRow(sheet.getFirstRowNum());
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    if (columnIndex == 0) {
                        sheet.setColumnWidth(columnIndex, 15000);
                    } else {
                        sheet.setColumnWidth(columnIndex, 2000);
                    }
                }
            }
        }
    }

    private void sendNotificationOfExcelFileCreated(int id, String subjectShortName) {
        Uri selectedUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten Teachers/Feedback Responses");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "File")
                .setSmallIcon(R.drawable.raw_logo)
                .setContentText(subjectShortName + " feedback responses saved in downloads folder")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(subjectShortName + " feedback responses saved in downloads folder"))
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
