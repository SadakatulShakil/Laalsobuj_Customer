package com.futureskyltd.app.fantacy;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.futureskyltd.app.helper.NetworkReceiver;

/**
 * Created by hitasoft on 18/7/17.
 */

public class HelpContent extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener{

    ImageView backBtn;
    TextView title;
    String pageName="";
    WebView webView;
    public static String mainContent="", subContent="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_content_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        webView = (WebView) findViewById(R.id.webView);

        pageName = getIntent().getExtras().getString("pageName");

        title.setText(pageName);
        //main.setText(FantacyApplication.stripHtml(mainContent.trim()));
        //sub.setText(FantacyApplication.stripHtml(subContent.trim()));

        String content = mainContent + "\n" + subContent;

        String start = "<html><head><style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/font/font_regular.ttf\")}body {font-family: MyFont;font-size: medium;text-align: justify;}</style></head><body>";
        String end = "</body></html>";
        String myHtmlString = start + content + end;

        webView.loadData(content, "text/html", "UTF-8");

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NetworkReceiver.isConnected()){
            FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
        } else {
            FantacyApplication.showSnack(this, findViewById(R.id.parentLay), false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
        }
    }
}
