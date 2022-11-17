package com.suyogbauskar.attenteachers.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.suyogbauskar.attenteachers.AttendanceBelow75Activity;
import com.suyogbauskar.attenteachers.R;
import com.suyogbauskar.attenteachers.excelfiles.CreateExcelFileCO5I_1;
import com.suyogbauskar.attenteachers.excelfiles.CreateExcelFileCO5I_2;
import com.suyogbauskar.attenteachers.excelfiles.CreateExcelFileCO5I_3;
import com.suyogbauskar.attenteachers.excelfiles.CreateExcelFileCO5I_4;
import com.suyogbauskar.attenteachers.excelfiles.CreateExcelFileCO5I_5;
import com.suyogbauskar.attenteachers.excelfiles.CreateExcelFileCO5I_A;
import com.suyogbauskar.attenteachers.excelfiles.CreateExcelFileCO5I_B;

public class StatisticsFragment extends Fragment {
    private Button excelBtn, attendanceBelow75Btn;

    public StatisticsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        getActivity().setTitle("Statistics");

        createNotificationChannelForFile();
        createNotificationChannelForError();
        findAllViews(view);
        setOnClickListeners();

        return view;
    }

    private void findAllViews(View view) {
        excelBtn = view.findViewById(R.id.excelBtn);
        attendanceBelow75Btn = view.findViewById(R.id.attendanceBelow75Btn);
    }

    private void setOnClickListeners() {
        excelBtn.setOnClickListener(view -> showYearPicker());

        attendanceBelow75Btn.setOnClickListener(view -> startActivity(new Intent(getActivity(), AttendanceBelow75Activity.class)));
    }

    private void showYearPicker() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("Year");
        String[] items = {"2022", "2023", "2024", "2025", "2026"};
        int checkedItem = 0;
        alertDialog.setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("yearPref",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            switch (which) {
                case 0:
                    editor.putString("year", items[0]);
                    break;
                case 1:
                    editor.putString("year", items[1]);
                    break;
                case 2:
                    editor.putString("year", items[2]);
                    break;
                case 3:
                    editor.putString("year", items[3]);
                    break;
                case 4:
                    editor.putString("year", items[4]);
                    break;
            }
            Toast.makeText(getContext(), "Creating Excel File...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            editor.commit();
            requireActivity().startService(new Intent(getContext(), CreateExcelFileCO5I_A.class));
            requireActivity().startService(new Intent(getContext(), CreateExcelFileCO5I_B.class));
            requireActivity().startService(new Intent(getContext(), CreateExcelFileCO5I_1.class));
            requireActivity().startService(new Intent(getContext(), CreateExcelFileCO5I_2.class));
            requireActivity().startService(new Intent(getContext(), CreateExcelFileCO5I_3.class));
            requireActivity().startService(new Intent(getContext(), CreateExcelFileCO5I_4.class));
            requireActivity().startService(new Intent(getContext(), CreateExcelFileCO5I_5.class));
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private void createNotificationChannelForFile() {
        String name = "File";
        String description = "File Notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("File", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void createNotificationChannelForError() {
        String name = "Error";
        String description = "Error Notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("Error", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}