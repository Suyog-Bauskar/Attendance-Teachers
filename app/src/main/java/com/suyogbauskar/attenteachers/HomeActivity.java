package com.suyogbauskar.attenteachers;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.attenteachers.fragments.HomeFragment;
import com.suyogbauskar.attenteachers.fragments.SettingsFragment;
import com.suyogbauskar.attenteachers.pojos.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private TextView nameView, emailView;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
    }

    private void init() {
        user = FirebaseAuth.getInstance().getCurrentUser();

        createNotificationChannelForError();
        createNotificationChannelForFile();
        requestStoragePermission();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view);

        setLeftNavigationDrawer();
        setBottomNav();
    }

    private void setBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav_view);
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomeFragment()).commit();
                    break;

                case R.id.attendance:
                    startActivity(new Intent(HomeActivity.this, LiveAttendanceActivity.class));
                    break;

                case R.id.today:
                    startActivity(new Intent(HomeActivity.this, TodayAttendanceActivity.class));
                    break;
            }
            return true;
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomeFragment()).commit();
    }

    private void setLeftNavigationDrawer() {
        View header = navigationView.getHeaderView(0);
        nameView = header.findViewById(R.id.nameView);
        emailView = header.findViewById(R.id.emailView);

        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        nameView.setText(snapshot.child("firstname").getValue(String.class) + " " + snapshot.child("lastname").getValue(String.class));
                        emailView.setText(user.getEmail());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.start, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT < 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
        }
    }

    private void selectSemesterAndDivision() {
        AlertDialog.Builder semesterDialog = new AlertDialog.Builder(HomeActivity.this);
        semesterDialog.setTitle("Semester");
        String[] items = {"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6"};
        semesterDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
            SharedPreferences sharedPreferences = getSharedPreferences("unitTestSemesterAndDivisionPref",MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();

            which++;
            int selectedSemester = which;
            AtomicReference<String> selectedDivision = new AtomicReference<>("");
            myEdit.putInt("semester", selectedSemester);
            if (selectedSemester == 1 || selectedSemester == 2) {
                AlertDialog.Builder divisionDialog = new AlertDialog.Builder(HomeActivity.this);
                divisionDialog.setTitle("Division");
                String[] items2 = {"Division A", "Division B", "Division C"};
                divisionDialog.setSingleChoiceItems(items2, -1, (dialog2, which2) -> {
                    switch (which2) {
                        case 0:
                            selectedDivision.set("A");
                            break;
                        case 1:
                            selectedDivision.set("B");
                            break;
                        case 2:
                            selectedDivision.set("C");
                            break;
                    }
                    myEdit.putString("division", selectedDivision.get());
                    myEdit.putBoolean("isFirstYear", true);
                    getAllSubjectsOfThatSemester(selectedSemester, myEdit);
                    dialog2.dismiss();
                });
                divisionDialog.create().show();
            } else {
                myEdit.putBoolean("isFirstYear", false);
                getAllSubjectsOfThatSemester(selectedSemester, myEdit);
            }
            dialog.dismiss();
        });
        semesterDialog.create().show();
    }

    private void getAllSubjectsOfThatSemester(int selectedSemester, SharedPreferences.Editor myEdit) {
        FirebaseDatabase.getInstance().getReference("teachers_data/" + user.getUid() + "/subjects")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Subject> currentSemesterSubjectList = new ArrayList<>();
                        boolean rightSemester = false;

                        for (DataSnapshot dsp : snapshot.getChildren()) {
                            if (selectedSemester == dsp.child("semester").getValue(Integer.class)) {
                                rightSemester = true;
                                currentSemesterSubjectList.add(new Subject(dsp.getKey(), dsp.child("subject_short_name").getValue(String.class)));
                            }
                        }

                        if (!rightSemester) {
                            Toast.makeText(HomeActivity.this, "You don't teach this semester", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (currentSemesterSubjectList.size() > 1) {
                            AlertDialog.Builder subjectDialog = new AlertDialog.Builder(HomeActivity.this);
                            subjectDialog.setTitle("Subjects");
                            String[] items3 = new String[currentSemesterSubjectList.size()];
                            for (int i = 0; i < currentSemesterSubjectList.size(); i++) {
                                items3[i] = currentSemesterSubjectList.get(i).getShortName();
                            }

                            subjectDialog.setSingleChoiceItems(items3, -1, (dialog3, which3) -> {
                                dialog3.dismiss();
                                myEdit.putString("subjectCodeTeacher", currentSemesterSubjectList.get(which3).getCode());
                                myEdit.commit();
                                startActivity(new Intent(HomeActivity.this, UnitTestMarksActivity.class));
                            });
                            subjectDialog.create().show();
                        } else {
                            myEdit.putString("subjectCodeTeacher", currentSemesterSubjectList.get(0).getCode());
                            myEdit.commit();
                            startActivity(new Intent(HomeActivity.this, UnitTestMarksActivity.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 || requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                sendErrorNotification("Storage permission is required to save excel file");
                finishAffinity();
            }
        }
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

    private void createNotificationChannelForFile() {
        String name = "File";
        String description = "File Notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("File", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void sendErrorNotification(String error) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Error")
                .setSmallIcon(R.drawable.raw_logo)
                .setContentText(error)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(error))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(this).notify(0, builder.build());
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            finishAffinity();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.notification) {
            startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
        }
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.students:
                startActivity(new Intent(HomeActivity.this, StudentDataActivity.class));
                break;

            case R.id.verification:
                startActivity(new Intent(HomeActivity.this, StudentVerificationActivity.class));
                break;

            case R.id.unitTestMarks:
                selectSemesterAndDivision();
                break;

            case R.id.submission:
                startActivity(new Intent(HomeActivity.this, SubmissionActivity.class));
                break;

            case R.id.utility:
                startActivity(new Intent(HomeActivity.this, UtilityActivity.class));
                break;

            case R.id.settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SettingsFragment()).commit();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }
}