package com.suyogbauskar.attenteachers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class NotificationAdapter extends BaseAdapter {
    String[] subTitles;
    String[] times;
    LayoutInflater inflater;

    public NotificationAdapter(Context applicationContext, String[] subTitles, String[] times) {
        this.subTitles = subTitles;
        this.times = times;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return subTitles.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.row_view, null);
        TextView tv1 = (TextView) view.findViewById(R.id.body);
        TextView tv2 = (TextView) view.findViewById(R.id.time);
        tv1.setText(subTitles[i]);
        tv2.setText(times[i]);
        return view;
    }
}
