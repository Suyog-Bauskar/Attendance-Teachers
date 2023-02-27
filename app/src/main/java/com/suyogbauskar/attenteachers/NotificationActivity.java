package com.suyogbauskar.attenteachers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private int selectedSemester;
    private String selectedDivision, department;
    private Button sendNotificationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        setTitle("Notification");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sendNotificationBtn = findViewById(R.id.sendNotificationBtn);
        sendNotificationBtn.setOnClickListener(v -> showDialogOfSemesterAndDivision());
        SharedPreferences sharedPreferences2 = getSharedPreferences("teacherDataPref", MODE_PRIVATE);
        department = sharedPreferences2.getString("department", "");

        showNotifications();
    }

    private void showNotifications() {
        FirebaseDatabase.getInstance().getReference("teachers_data")
                .child(user.getUid())
                .child("notifications")
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> titles = new ArrayList<>();
                        List<String> body = new ArrayList<>();
                        List<String> times = new ArrayList<>();

                        for (DataSnapshot dsp : snapshot.getChildren()) {
                            titles.add(dsp.child("title").getValue(String.class));
                            body.add(dsp.child("body").getValue(String.class));
                            times.add(dsp.child("time").getValue(String.class));
                        }

                        String[] titleArr = new String[titles.size()];
                        String[] bodyArr = new String[body.size()];
                        String[] timesArr = new String[times.size()];
                        int reverseCounter = body.size() - 1;

                        for (int i = 0; i < bodyArr.length; i++) {
                            titleArr[i] = titles.get(reverseCounter);
                            bodyArr[i] = body.get(reverseCounter);
                            timesArr[i] = times.get(reverseCounter);
                            reverseCounter--;
                        }

                        ListView notificationList = (ListView) findViewById(R.id.notificationListView);
                        NotificationAdapter notificationAdapter = new NotificationAdapter(getApplicationContext(), titleArr, bodyArr, timesArr);
                        notificationList.setAdapter(notificationAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(NotificationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendNotification() {
        FlatDialog flatDialog = new FlatDialog(NotificationActivity.this);
        flatDialog.setTitle("Send a message")
                .setFirstTextField("")
                .setFirstTextFieldHint("write your message here ...")
                .setFirstButtonText("Done")
                .withFirstButtonListner(v -> {
                    if (flatDialog.getFirstTextField().trim().isEmpty()) {
                        Toast.makeText(this, "Enter message", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    flatDialog.dismiss();
                    SharedPreferences sp = getSharedPreferences("teacherDataPref", MODE_PRIVATE);
                    String firstname, lastname;
                    firstname = sp.getString("firstname", "");
                    lastname = sp.getString("lastname", "");

                    String formattedTime = new SimpleDateFormat("hh:mm a").format(new Date());

                    Map<String, Object> data = new HashMap<>();
                    data.put("timestamp", ServerValue.TIMESTAMP);
                    data.put("firstname", firstname);
                    data.put("lastname", lastname);
                    data.put("title", department + selectedSemester + "-" + selectedDivision);
                    data.put("body", flatDialog.getFirstTextField());
                    data.put("time", formattedTime);

                    Map<String, Object> data2 = new HashMap<>();
                    data2.put("timestamp", ServerValue.TIMESTAMP);
                    data2.put("title", "Prof. " + firstname + " " + lastname);
                    data2.put("body", flatDialog.getFirstTextField());
                    data2.put("time", formattedTime);

                    long currentDate = System.currentTimeMillis();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy/HH/mm/ss/MMMM", Locale.getDefault());
                    String dateStr = dateFormat.format(currentDate);
                    String[] dateArr = dateStr.split("/");
                    int date = Integer.parseInt(dateArr[0]);
                    int year = Integer.parseInt(dateArr[2]);
                    String monthStr = dateArr[6];

                    String randomKey = FirebaseDatabase.getInstance().getReference("teachers_data").child(user.getUid()).push().getKey();
                    FirebaseDatabase.getInstance().getReference("teachers_data")
                            .child(user.getUid())
                            .child("notifications")
                            .child(randomKey)
                            .setValue(data);

                    String randomKey2 = FirebaseDatabase.getInstance().getReference("notifications").push().getKey();
                    FirebaseDatabase.getInstance().getReference("notifications")
                            .child(String.valueOf(year))
                            .child(monthStr)
                            .child(String.valueOf(date))
                            .child(department + selectedSemester + "-" + selectedDivision)
                            .child(randomKey2)
                            .setValue(data2);

                })
                .setSecondButtonText("Cancel")
                .withSecondButtonListner(v -> flatDialog.dismiss())
                .show();
    }

    private void showDialogOfSemesterAndDivision() {
        AlertDialog.Builder semesterDialog = new AlertDialog.Builder(NotificationActivity.this);
        semesterDialog.setTitle("Semester");
        String[] items = {"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6"};
        semesterDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
            which++;
            selectedSemester = which;
            dialog.dismiss();

            AlertDialog.Builder divisionDialog = new AlertDialog.Builder(NotificationActivity.this);
            divisionDialog.setTitle("Division");
            String[] items2 = {"All", "Division A", "Division B", "Division C", "Batch A1", "Batch A2", "Batch A3", "Batch B1", "Batch B2", "Batch B3", "Batch C1", "Batch C2", "Batch C3"};
            divisionDialog.setSingleChoiceItems(items2, -1, (dialog2, which2) -> {
                switch (which2) {
                    case 0:
                        selectedDivision = "All";
                        break;
                    case 1:
                        selectedDivision = "A";
                        break;
                    case 2:
                        selectedDivision = "B";
                        break;
                    case 3:
                        selectedDivision = "C";
                        break;
                    case 4:
                        selectedDivision = "A1";
                        break;
                    case 5:
                        selectedDivision = "A2";
                        break;
                    case 6:
                        selectedDivision = "A3";
                        break;
                    case 7:
                        selectedDivision = "B1";
                        break;
                    case 8:
                        selectedDivision = "B2";
                        break;
                    case 9:
                        selectedDivision = "B3";
                        break;
                    case 10:
                        selectedDivision = "C1";
                        break;
                    case 11:
                        selectedDivision = "C2";
                        break;
                    case 12:
                        selectedDivision = "C3";
                        break;
                }
                dialog2.dismiss();
                sendNotification();
            });
            divisionDialog.create().show();
        });
        semesterDialog.create().show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(NotificationActivity.this, HomeActivity.class));
    }
}