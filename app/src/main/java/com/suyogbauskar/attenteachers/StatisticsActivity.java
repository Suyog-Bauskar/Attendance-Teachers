package com.suyogbauskar.attenteachers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity implements View.OnClickListener {

    private BottomNavigationView bottomNav;
    private Button excelBtn;
    private FirebaseFirestore db;
    private List<String> subCollectionsName = new ArrayList<>();
    private Map<String, List<String>> monthsAndSubCollectionsName = new HashMap<>();
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

        //TODO : Year is hardcoded, change it to take input from user
        yearRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> documentData = document.getData();
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

    public void createExcelFile() {

        String filename;
        int rowNo = 0, columnNo = 0;
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet xssfSheet;
        XSSFRow xssfRow;
        XSSFCell xssfCell;

        //TODO : Year is hardcoded, change it to take input from user
        filename = HomeActivity.subjectNameDB + " Attendance 2022";
        filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + filename + ".xlsx");

        for (Map.Entry<String, Map<String, Object>> entry : attendanceCompleteData.entrySet()) {

            xssfSheet = xssfWorkbook.createSheet(entry.getKey());

            if (rowNo == 0) {
                xssfRow = xssfSheet.createRow(rowNo);

                xssfCell = xssfRow.createCell(columnNo);
                xssfCell.setCellValue("Roll No");

                columnNo++;

                xssfCell = xssfRow.createCell(columnNo);
                xssfCell.setCellValue("Name");

                columnNo++;

                for (String s : entry.getValue().keySet()) {
                    xssfCell = xssfRow.createCell(columnNo);
                    xssfCell.setCellValue(s);
                    columnNo++;
                }
            }

            columnNo = 0;
        }

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