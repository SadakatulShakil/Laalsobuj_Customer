package com.futureskyltd.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.futureskyltd.app.ApiPojo.Upazila.Upazila;
import com.futureskyltd.app.fantacy.R;

import java.util.ArrayList;

public class UpazilaAdapter extends ArrayAdapter<Upazila> {
    private ArrayList<Upazila> upazilaArrayList;
    private Context context;
    public UpazilaAdapter(@NonNull Context context, ArrayList<Upazila> upazilaArrayList) {
        super(context, 0, upazilaArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }


    @Override
    public boolean isEnabled(int position) {
        return position != -1;
    }

    private View initView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_view, parent, false);

        }

        TextView productTypeName = convertView.findViewById(R.id.spinnerView);
        Upazila upazila = getItem(position);


        if (upazila != null) {
            productTypeName.setText(upazila.getUpazila());
        }

        return convertView;
    }
}
