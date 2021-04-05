package com.futureskyltd.app.fantacy;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.futureskyltd.app.utils.GetSet;

/**
 * Created by hitasoft on 18/9/17.
 */

public class ViewAllStores extends BaseActivity implements View.OnClickListener{

    private static final String TAG = ViewAllStores.class.getSimpleName();
    ImageView backBtn, cartBtn, searchBtn;
    TextView title;
    String from="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_all_stores);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        cartBtn = (ImageView) findViewById(R.id.cartBtn);
        searchBtn = (ImageView) findViewById(R.id.searchBtn);

        title.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);
        cartBtn.setVisibility(View.VISIBLE);
        searchBtn.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        cartBtn.setOnClickListener(this);

        from = (String) getIntent().getExtras().get("from");
        if (from.equals(getString(R.string.popular_store))) {
            title.setText(getString(R.string.popular_store));
        }

        if (GetSet.isLogged()){
            switchContent(FollowStore.newInstance("viewAll", GetSet.getUserId()));
        } else {
            switchContent(FollowStore.newInstance("viewAll", ""));
        }

    }

    public void switchContent(Fragment fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().addToBackStack("back")
                    .replace(R.id.content_frame, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        this.finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.searchBtn:
                Intent s = new Intent(this, RecentSearch.class);
                startActivity(s);
                break;
            case R.id.cartBtn:
                if (GetSet.isLogged()) {
                    Intent c = new Intent(this, CartActivity.class);
                    c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    c.putExtra("shippingId", "0");
                    c.putExtra("itemId", "0");
                    startActivity(c);
                } else {
                    Intent login = new Intent(this, LoginActivity.class);
                    startActivity(login);
                }
                break;
        }
    }
}
