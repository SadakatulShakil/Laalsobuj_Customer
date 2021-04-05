package com.futureskyltd.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.futureskyltd.app.ApiPojo.InfoApa.Apa;
import com.futureskyltd.app.fantacy.R;

import java.util.ArrayList;

public class InfoApaAdapter extends ArrayAdapter<Apa> {
    private ArrayList<Apa> infoApaArrayList;
    private Context context;
    public InfoApaAdapter(@NonNull Context context, ArrayList<Apa> infoApaArrayList) {
        super(context, 0, infoApaArrayList);
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
        Apa apa = getItem(position);


        if (apa != null) {
            productTypeName.setText(apa.getName());
        }

        return convertView;
    }
}
