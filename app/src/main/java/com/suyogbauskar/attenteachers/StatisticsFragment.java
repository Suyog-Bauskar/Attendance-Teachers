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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.pojos.Attendance;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class StatisticsFragment extends Fragment implements View.OnClickListener {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final CsvMapper CSV_MAPPER = new CsvMapper();
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
                    String firstname = String.valueOf(dsp.child("firstname").getValue());
                    String lastname = String.valueOf(dsp.child("lastname").getValue());
                    int rollNo = Integer.parseInt(String.valueOf(dsp.child("rollNo").getValue()));
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
        getAllPresentStudentsData("22517", 2022);

        XSSFWorkbook xssfWorkbook;
        XSSFSheet xssfSheet;
        XSSFRow xssfRow;
        XSSFCell xssfCell;

        int rowNo, columnNo;
    }

    private void getAllPresentStudentsData(String subjectCode, int year) {
        Map<String, List<AttendanceStudent>> presentStudentsData = new HashMap<>();
        fetchData(subjectCode, year, this::dataConsumer);
    }

    private void dataConsumer(Object data){
        Attendance attendance = MAPPER.convertValue(data, Attendance.class);
        Map<String, List<Map<String, Object>>> monthlyAttendance = attendance.getAll();
        Map<String, Set<String>> monthlyColumns = attendance.getColumns();

        Map<String, String> monthlyCsv= new HashMap<>();
        monthlyAttendance.forEach( (month, entries) -> monthlyCsv.put(month,getCSV(entries,monthlyColumns.get(month))));
        System.out.println(monthlyCsv);
    }

    private String getCSV(List<Map<String, Object>> entries, Set<String> columns) {
        CsvSchema.Builder schema = CsvSchema.builder().setUseHeader(true);
        columns.stream().sorted(Comparator.reverseOrder()).forEach(schema::addColumn);

        try (StringWriter strW = new StringWriter()) {
            SequenceWriter seqW = CSV_MAPPER.writer().withSchema(schema.build()).writeValues(strW);
            for (Map<String, Object> entry : entries) seqW.write(entry);
            seqW.close();
            return strW.toString();
        }catch (IOException e){
            throw new RuntimeException("Failed while writing to csv", e);
        }
    }

    private void fetchData(String subjectCode, int year, Consumer<Object> dataConsumer) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference(String.format("/attendance/%s/%d", subjectCode, year));
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataConsumer.accept(snapshot.getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw new RuntimeException("Event got canceled while waiting", error.toException());
            }
        });
    }
}