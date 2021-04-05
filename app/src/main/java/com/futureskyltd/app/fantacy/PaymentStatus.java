package com.futureskyltd.app.fantacy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.CircleProgress;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PaymentStatus extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    public String TAG = this.getClass().getSimpleName();
    CircleProgress circleProgress;
    ImageView backBtn, nullImage;
    TextView title, subTitle, continueBtn;
    String from;
    public static PaymentStatus activity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_status_layout);

        activity = this;
        circleProgress = (CircleProgress) findViewById(R.id.circleProgress);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        nullImage = (ImageView) findViewById(R.id.nullImage);
        title = (TextView) findViewById(R.id.title);
        subTitle = (TextView) findViewById(R.id.subTitle);
        continueBtn = (TextView) findViewById(R.id.continueBtn);
        from = (String) getIntent().getExtras().get("from");

        backBtn.setOnClickListener(this);
        continueBtn.setOnClickListener(this);

        Log.e("fromPayment", "-" + from);
        switch (from) {
            case "block":
                nullImage.setVisibility(View.VISIBLE);
                circleProgress.setVisibility(View.GONE);
                title.setText(getString(R.string.admin_block));
                subTitle.setText(getString(R.string.admin_block_text));
                continueBtn.setText(getString(R.string.to_login));
                clearData();
                break;
            case "delete":
                nullImage.setVisibility(View.VISIBLE);
                circleProgress.setVisibility(View.GONE);
                title.setText(getString(R.string.admin_delete_error));
                subTitle.setVisibility(View.GONE);
                continueBtn.setText(getString(R.string.to_login));
                clearData();
                break;
            case "other_user_block":
                nullImage.setVisibility(View.VISIBLE);
                circleProgress.setVisibility(View.GONE);
                title.setText(getString(R.string.other_user_block));
                subTitle.setVisibility(View.GONE);
                continueBtn.setText(getString(R.string.go_back));
                break;
            case "other_user_delete":
                nullImage.setVisibility(View.VISIBLE);
                circleProgress.setVisibility(View.GONE);
                title.setText(getString(R.string.other_user_delete));
                subTitle.setVisibility(View.GONE);
                continueBtn.setText(getString(R.string.go_back));
                break;
            case "maintenance":
                nullImage.setVisibility(View.VISIBLE);
                circleProgress.setVisibility(View.GONE);
                title.setText(getString(R.string.site_maintain));
                subTitle.setText(getString(R.string.site_maintain_text));
                continueBtn.setText(getString(R.string.refresh));
                break;

        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NetworkReceiver.isConnected()) {
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

    private void removeToken() {
        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PUSH_NOTIFICATION_UNREGISTER,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "removeTokenRes=" + res);
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "removeTokenError: " + error.getMessage());
            }

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("device_id", deviceId);
                Log.v(TAG, "removeTokenParams=" + map);
                return map;
            }
        };

        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void getData() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADMIN_DATAS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getDataRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        nullImage.setVisibility(View.GONE);
                        circleProgress.setVisibility(View.VISIBLE);
                        finish();
                    } else {
                        nullImage.setVisibility(View.VISIBLE);
                        circleProgress.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getDataError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.continueBtn:
                switch (from) {
                    case "block":
                    case "delete":
                        finish();
                        Intent i = new Intent(this, FragmentMainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.putExtra("from", "block");
                        startActivity(i);
                        break;
                    case "maintenance":
                        nullImage.setVisibility(View.GONE);
                        circleProgress.setVisibility(View.VISIBLE);
                        getData();

                        break;
                    case "other_user_block":
                    case "other_user_delete":
                        finish();
                        activity = null;
                        break;
                    default:
                        finish();
                        FragmentMainActivity.orders = true;
                        Intent main = new Intent(this, FragmentMainActivity.class);
                        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(main);
                        break;
                }
                break;
        }
    }

    void clearData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("FantacyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
        GetSet.reset();
        FragmentMainActivity.userName.setText(R.string.guest);
        Picasso.get().load(R.drawable.temp).placeholder(R.drawable.temp).error(R.drawable.temp).into(FragmentMainActivity.userImage);
        HomeFragment.resetAry();
        FragmentMainActivity.messageCount = "0";
        FragmentMainActivity.notifyCount = "0";
        FragmentMainActivity.feedCount = "0";
        FragmentMainActivity.cartCount = "0";
        FragmentMainActivity.creditAmount = "0";
        FragmentMainActivity.currency = "$";
        removeToken();
    }

    @Override
    protected void onDestroy() {
        activity = null;
        super.onDestroy();
    }
}
