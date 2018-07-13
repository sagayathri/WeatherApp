package com.gayathri.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class dayAdapter extends BaseAdapter {

    ArrayList<SingleDay> arrayList = new ArrayList<SingleDay>();
    Context context;

    dayAdapter(Context context, ArrayList<SingleDay> arrayList1){
        this.context = context;
        arrayList.addAll(arrayList1);
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_single_day, parent, false);
        TextView tv_time = (TextView) view.findViewById(R.id.day_time);
        TextView tv_description = (TextView) view.findViewById(R.id.day_weather_description);
        TextView tv_temp = (TextView) view.findViewById(R.id.day_temp);
        ImageView imageView = (ImageView) view.findViewById(R.id.day_icon);

        final SingleDay singleDay = arrayList.get(position);
        tv_time.setText(singleDay.getDay_time());
        tv_description.setText(singleDay.getDay_weather_description());
        tv_temp.setText(singleDay.getDay_temp());
        imageView.setImageResource(singleDay.getDay_icon_field());
        return view;
    }
}
