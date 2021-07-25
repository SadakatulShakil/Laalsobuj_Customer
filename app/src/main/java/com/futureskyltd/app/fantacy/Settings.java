package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 8/7/17.
 */

public class Settings extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn;
    TextView title, currency, language;
    RelativeLayout languageLay, currencyLay;
    public static SharedPreferences pref;
    HashMap<String, String> settingsData = new HashMap<>();
    ArrayList<HashMap<String, String>> allCurrencyAry = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> langAry = new ArrayList<HashMap<String, String>>();
    SwitchCompat emailSwitch, notificationSwitch1, notificationSwitch2, notificationSwitch3, notificationSwitch4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        languageLay = (RelativeLayout) findViewById(R.id.languageLay);
        currencyLay = (RelativeLayout) findViewById(R.id.currencyLay);
        emailSwitch = (SwitchCompat) findViewById(R.id.emailSwitch);
        notificationSwitch1 = (SwitchCompat) findViewById(R.id.notificationSwitch1);
        notificationSwitch2 = (SwitchCompat) findViewById(R.id.notificationSwitch2);
        notificationSwitch3 = (SwitchCompat) findViewById(R.id.notificationSwitch3);
        notificationSwitch4 = (SwitchCompat) findViewById(R.id.notificationSwitch4);
        currency = (TextView) findViewById(R.id.currency);
        language = (TextView) findViewById(R.id.language);
        title.setText(getString(R.string.settings));

        pref = getApplicationContext().getSharedPreferences("LangPref", MODE_PRIVATE);
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        getSettings();

        backBtn.setOnClickListener(this);
        languageLay.setOnClickListener(this);
        currencyLay.setOnClickListener(this);
        emailSwitch.setOnClickListener(this);
        notificationSwitch1.setOnClickListener(this);
        notificationSwitch2.setOnClickListener(this);
        notificationSwitch3.setOnClickListener(this);
        notificationSwitch4.setOnClickListener(this);

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void setData() {
        currency.setText(LocationNCurrency.selectedCurrency);
        language.setText(LocationNCurrency.selectedLang);
        if (settingsData.get(Constants.TAG_FOLLOW_EMAIL).equals("true"))
            emailSwitch.setChecked(true);

        if (settingsData.get(Constants.TAG_FOLLOW_NOTIFY).equals("true"))
            notificationSwitch1.setChecked(true);

        if (settingsData.get(Constants.TAG_MENTION_NOTIFY).equals("true"))
            notificationSwitch2.setChecked(true);

        if (settingsData.get(Constants.TAG_PRODUCT_ADDED).equals("true"))
            notificationSwitch3.setChecked(true);

        if (settingsData.get(Constants.TAG_RECEIVE_NEWS_ADMIN).equals("true"))
            notificationSwitch4.setChecked(true);

    }

    private void getSettings() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        final ProgressDialog dialog = new ProgressDialog(Settings.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_SETTINGS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {

                    Log.v(TAG, "getSettingsRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);
                        String currency_id = DefensiveClass.optString(result, Constants.TAG_CURRENCY_ID);
                        String currency_code = DefensiveClass.optString(result, Constants.TAG_CURRENCY_CODE);
                        String has_password = DefensiveClass.optString(result, Constants.TAG_HAS_PASSWORD);
                        String follow_email = DefensiveClass.optString(result, Constants.TAG_FOLLOW_EMAIL);
                        String follow_notify = DefensiveClass.optString(result, Constants.TAG_FOLLOW_NOTIFY);
                        String mention_notify = DefensiveClass.optString(result, Constants.TAG_MENTION_NOTIFY);
                        String product_added = DefensiveClass.optString(result, Constants.TAG_PRODUCT_ADDED);
                        String receive_news = DefensiveClass.optString(result, Constants.TAG_RECEIVE_NEWS_ADMIN);
                        String language = DefensiveClass.optString(result, Constants.TAG_LANGUAGE);
                        LocationNCurrency.selectedCurrency = currency_code;
                        LocationNCurrency.selectedID = currency_id;
                        LocationNCurrency.selectedLangCode = language;
                        settingsData.put(Constants.TAG_CURRENCY_ID, currency_id);
                        settingsData.put(Constants.TAG_CURRENCY_CODE, currency_code);
                        settingsData.put(Constants.TAG_HAS_PASSWORD, has_password);
                        settingsData.put(Constants.TAG_FOLLOW_EMAIL, follow_email);
                        settingsData.put(Constants.TAG_FOLLOW_NOTIFY, follow_notify);
                        settingsData.put(Constants.TAG_MENTION_NOTIFY, mention_notify);
                        settingsData.put(Constants.TAG_PRODUCT_ADDED, product_added);
                        settingsData.put(Constants.TAG_RECEIVE_NEWS_ADMIN, receive_news);

                        JSONArray currency = result.getJSONArray(Constants.TAG_CURRENCY);
                        allCurrencyAry.clear();
                        for (int i = 0; i < currency.length(); i++) {
                            JSONObject temp = currency.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();
                            String all_currency_id = DefensiveClass.optString(temp, Constants.TAG_CURRENCY_ID);
                            String all_currency_code = DefensiveClass.optString(temp, Constants.TAG_CURRENCY_CODE);

                            map.put(Constants.TAG_CURRENCY_ID, all_currency_id);
                            map.put(Constants.TAG_CURRENCY_CODE, all_currency_code);
                            allCurrencyAry.add(map);
                        }

                        JSONArray languages = result.getJSONArray(Constants.TAG_LANGUAGES);
                        for (int i = 0; i < languages.length(); i++) {
                            JSONObject temp = languages.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();
                            String code = DefensiveClass.optString(temp, Constants.TAG_CODE);
                            String name = DefensiveClass.optString(temp, Constants.TAG_NAME);

                            if (LocationNCurrency.selectedLangCode.equals(code)) {
                                LocationNCurrency.selectedLang = name;
                            }

                            map.put(Constants.TAG_CODE, code);
                            map.put(Constants.TAG_NAME, name);
                            langAry.add(map);
                        }
                        setData();
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           /* Intent i = new Intent(Settings.this, PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                            /*Intent i = new Intent(Settings.this, PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                Log.v(TAG, "getSettingsParams=" + map);
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }
        };
        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void setSettings() {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SET_SETTINGS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "setSettingsRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
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
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("someone_follow_email", String.valueOf(emailSwitch.isChecked()));
                map.put("someone_follow_notify", String.valueOf(notificationSwitch1.isChecked()));
                map.put("someone_mention_notify", String.valueOf(notificationSwitch2.isChecked()));
                map.put("store_product_added", String.valueOf(notificationSwitch3.isChecked()));
                map.put("receive_news_admin", String.valueOf(notificationSwitch4.isChecked()));
                Log.v(TAG, "setSettingsParams=" + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    protected void onResume() {
        super.onResume();
        language.setText(pref.getString("language", Constants.LANGUAGE));
        currency.setText(LocationNCurrency.selectedCurrency);
        if (LocationNCurrency.changedCurrency) {
            LocationNCurrency.changedCurrency = false;
            setSettings();
        }
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
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
                setSettings();
                finish();
                break;
            case R.id.languageLay:
                Intent i = new Intent(this, LocationNCurrency.class);
                i.putExtra("from", "lang");
                i.putExtra("data", langAry);
                startActivity(i);
                break;
            case R.id.currencyLay:
                Intent j = new Intent(this, LocationNCurrency.class);
                j.putExtra("from", "currency");
                j.putExtra("data", allCurrencyAry);
                j.putExtra("currencyid", settingsData.get(Constants.TAG_CURRENCY_ID));
                startActivity(j);
                break;
            case R.id.emailSwitch:
            case R.id.notificationSwitch1:
            case R.id.notificationSwitch2:
            case R.id.notificationSwitch3:
            case R.id.notificationSwitch4:
                setSettings();
                break;
        }
    }
}
