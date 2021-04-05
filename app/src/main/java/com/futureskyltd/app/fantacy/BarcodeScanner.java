package com.futureskyltd.app.fantacy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.Result;
import com.futureskyltd.app.external.ObjectSerializer;
import com.futureskyltd.app.helper.NetworkReceiver;

import java.io.IOException;
import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by hitasoft on 16/5/17.
 */

public class BarcodeScanner extends BaseActivity implements View.OnClickListener, ZXingScannerView.ResultHandler,
        NetworkReceiver.ConnectivityReceiverListener{

    Toolbar toolbar;
    ImageView backBtn;
    TextView title;
    ZXingScannerView mScannerView;
    SharedPreferences pref;
    SharedPreferences.Editor edit;
    ArrayList<String> searchAry = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanner);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        title = (TextView) findViewById(R.id.title);

        setSupportActionBar(toolbar);

        title.setText(getString(R.string.scan_barcode));
        title.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);

        pref = getApplicationContext().getSharedPreferences("SearchHistory", MODE_PRIVATE);
        edit = pref.edit();

        backBtn.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        if (NetworkReceiver.isConnected()){
            FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
        } else {
            FantacyApplication.showSnack(this, findViewById(R.id.parentLay), false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScannerView.stopCamera();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void handleResult(Result rawResult) {
        try {
            ArrayList<String> tempAry = (ArrayList<String>) ObjectSerializer.deserialize(pref.getString("history", ObjectSerializer.serialize(new ArrayList<String>())));
            searchAry.addAll(tempAry);
            searchAry.add(0, "barcode-"+rawResult.getText());
            edit.putString("history", ObjectSerializer.serialize(searchAry));
            edit.commit();
            finish();
            Intent i = new Intent(this, CategoryListings.class);
            i.putExtra("from", "barcode");
            i.putExtra("searchKey", rawResult.getText());
            startActivity(i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backBtn:
                finish();
                break;
        }
    }
}
