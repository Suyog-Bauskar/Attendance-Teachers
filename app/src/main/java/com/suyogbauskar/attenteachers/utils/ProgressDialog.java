package com.suyogbauskar.attenteachers.utils;

import android.content.Context;
import android.graphics.Color;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProgressDialog {
    private SweetAlertDialog pDialog;

    public void show(Context context) {
        pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    public void hide() {
        if(pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }
}
