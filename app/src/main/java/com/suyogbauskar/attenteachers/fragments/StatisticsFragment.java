package com.suyogbauskar.attenteachers.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.suyogbauskar.attenteachers.AttendanceBelow75Activity;
import com.suyogbauskar.attenteachers.CreateExcelFileService;
import com.suyogbauskar.attenteachers.R;

public class StatisticsFragment extends Fragment{
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
        excelBtn.setOnClickListener(view -> requireActivity().startService(new Intent(getContext(), CreateExcelFileService.class)));
        attendanceBelow75Btn.setOnClickListener(view -> startActivity(new Intent(getActivity(), AttendanceBelow75Activity.class)));
    }

}