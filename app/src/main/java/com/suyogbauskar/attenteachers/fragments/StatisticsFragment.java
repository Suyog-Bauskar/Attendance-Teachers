package com.suyogbauskar.attenteachers.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.suyogbauskar.attenteachers.AttendanceBelow75Activity;
import com.suyogbauskar.attenteachers.CreateExcelFileService;
import com.suyogbauskar.attenteachers.R;

public class StatisticsFragment extends Fragment {
    private Button excelBtn, attendanceBelow75Btn;

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
        attendanceBelow75Btn = view.findViewById(R.id.attendanceBelow75Btn);
    }

    private void setOnClickListeners() {
        excelBtn.setOnClickListener(view -> {
            showYearPicker();
        });

        attendanceBelow75Btn.setOnClickListener(view -> startActivity(new Intent(getActivity(), AttendanceBelow75Activity.class)));
    }

    private void showYearPicker() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("Year");
        String[] items = {"2022", "2023", "2024", "2025", "2026"};
        int checkedItem = 0;
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
                dialog.dismiss();
                editor.commit();
                requireActivity().startService(new Intent(getContext(), CreateExcelFileService.class));
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }
}