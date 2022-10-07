package com.suyogbauskar.attenteachers;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

public class StatisticsFragment extends Fragment implements View.OnClickListener {

    private Button excelBtn;
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

    @Override
    public void onClick(View view) {

    }
}