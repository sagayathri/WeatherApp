package com.gayathri.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class myAdapter extends BaseAdapter {

    ArrayList<SingleRow> arrayList = new ArrayList<SingleRow>();
    Context context;

    myAdapter(Context context, ArrayList<SingleRow> arrayList1){
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
        View view = inflater.inflate(R.layout.list_row, parent, false);
        TextView tv_date = (TextView) view.findViewById(R.id.day_Date);
        TextView tv_description = (TextView) view.findViewById(R.id.day_description);
        TextView tv_minmax = (TextView) view.findViewById(R.id.day_minmax);
        ImageView imageView = (ImageView) view.findViewById(R.id.day_weathericon);

        final SingleRow singleRows = arrayList.get(position);
        tv_date.setText(singleRows.getDate_feild());
        tv_description.setText(singleRows.getDescription());
        tv_minmax.setText(singleRows.getMinmax_field());
        imageView.setImageResource(singleRows.getIcon_field());
        return view;
    }
}




