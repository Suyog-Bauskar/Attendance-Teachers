package com.suyogbauskar.attenteachers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import fcm.androidtoandroid.FirebasePush;
import fcm.androidtoandroid.model.Notification;

public class NotificationActivity extends AppCompatActivity {

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private int selectedSemester;
    private String selectedDivision;
    private Button sendNotificationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        setTitle("Notification");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sendNotificationBtn = findViewById(R.id.sendNotificationBtn);
        sendNotificationBtn.setOnClickListener(v -> showDialogOfSemesterAndDivision());

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
                        String body;
                        long time;
                        for (DataSnapshot dsp: snapshot.getChildren()) {
                            body = dsp.child("body").getValue(String.class);
                            time = dsp.child("timestamp").getValue(Long.class);

                            Toast.makeText(NotificationActivity.this, "Body: " + body + ", Time: " + time, Toast.LENGTH_SHORT).show();
                        }
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
                .setLargeTextField("")
                .setLargeTextFieldHint("write your message here ...")
                .setFirstButtonText("Done")
                .withFirstButtonListner(v -> {
                    if (flatDialog.getLargeTextField().trim().isEmpty()) {
                        Toast.makeText(this, "Enter message", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    flatDialog.dismiss();
                    SharedPreferences sp = getSharedPreferences("teacherDataPref", MODE_PRIVATE);
                    String firstname, lastname;
                    firstname = sp.getString("firstname", "");
                    lastname = sp.getString("lastname", "");
                    FirebasePush firebasePush = new FirebasePush("AAAAtBgfzRs:APA91bFqeVeSH8NFUlNWJA_EuCWmwsCXHqyeawP1UV2sH7XHOcNcjumnoBdCWue0uQIB7B5yeePlYzfDiPrbqEusZFyIJWKrnWecHuIbSqjVvLT-tZoaa7zaMMfzCxVxAzezqFAKjVBd");
                    firebasePush.setNotification(new Notification("Prof. " + firstname + " " + lastname, flatDialog.getLargeTextField()));
                    if (selectedDivision.equals("All")) {
                        firebasePush.sendToTopic("CO" + selectedSemester);
                    } else {
                        firebasePush.sendToTopic("CO" + selectedSemester + "-" + selectedDivision);
                    }

                    String formattedTime = new SimpleDateFormat("hh:mm a").format(new Date());

                    Map<String, Object> data = new HashMap<>();
                    data.put("timestamp", ServerValue.TIMESTAMP);
                    data.put("body", flatDialog.getLargeTextField());
                    data.put("time", formattedTime);

                    Map<String, Object> data2 = new HashMap<>();
                    data2.put("timestamp", ServerValue.TIMESTAMP);
                    data2.put("title", "Prof. " + firstname + " " + lastname);
                    data2.put("body", flatDialog.getLargeTextField());
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
                            .child("CO" + selectedSemester + "-" + selectedDivision)
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
            switch (which) {
                case 0:
                    selectedSemester = 1;
                    break;
                case 1:
                    selectedSemester = 2;
                    break;
                case 2:
                    selectedSemester = 3;
                    break;
                case 3:
                    selectedSemester = 4;
                    break;
                case 4:
                    selectedSemester = 5;
                    break;
                case 5:
                    selectedSemester = 6;
                    break;
            }
            dialog.dismiss();

            AlertDialog.Builder divisionDialog = new AlertDialog.Builder(NotificationActivity.this);
            divisionDialog.setTitle("Division");
            String[] items2 = {"All", "Division A", "Division B", "Practical Batch A1", "Practical Batch A2", "Practical Batch A3", "Practical Batch B1", "Practical Batch B2"};
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
                        selectedDivision = "A1";
                        break;
                    case 4:
                        selectedDivision = "A2";
                        break;
                    case 5:
                        selectedDivision = "A3";
                        break;
                    case 6:
                        selectedDivision = "B1";
                        break;
                    case 7:
                        selectedDivision = "B2";
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