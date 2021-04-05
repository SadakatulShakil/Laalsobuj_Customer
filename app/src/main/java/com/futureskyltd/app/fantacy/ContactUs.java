package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;

/**
 * Created by hitasoft on 18/7/17.
 */

public class ContactUs extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = ContactUs.class.getSimpleName();
    ImageView backBtn;
    TextView title, alertText, addressText, emailText, callText, saveBtn;
    EditText fullName, emailEdit, orderNumber, userAccount, message;
    Spinner selectTopic;
    String email = "", address = "", phone = "", selectedTopic = "";
    ArrayList<String> topicsAry = new ArrayList<>();
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    SharedPreferences preferences;
    String accesstoken, customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contactus_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);
        customerId = preferences.getString("customer_id", null);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        saveBtn = (TextView) findViewById(R.id.saveBtn);
        selectTopic = (Spinner) findViewById(R.id.selectTopic);
        alertText = (TextView) findViewById(R.id.alertText);
        addressText = (TextView) findViewById(R.id.addressText);
        emailText = (TextView) findViewById(R.id.emailText);
        callText = (TextView) findViewById(R.id.callText);
        fullName = (EditText) findViewById(R.id.fullName);
        emailEdit = (EditText) findViewById(R.id.emailEdit);
        orderNumber = (EditText) findViewById(R.id.orderNumber);
        userAccount = (EditText) findViewById(R.id.userAccount);
        message = (EditText) findViewById(R.id.message);

        title.setText(getString(R.string.contact_us));
        saveBtn.setText(getString(R.string.send));

        topicsAry = (ArrayList<String>) getIntent().getExtras().get("topicsAry");
        email = getIntent().getExtras().getString("email");
        address = getIntent().getExtras().getString("address");
        phone = getIntent().getExtras().getString("phone");

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        callText.setOnClickListener(this);
        emailText.setOnClickListener(this);

        try {
            addressText.setText(URLDecoder.decode(address, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        emailText.setText(email);
        callText.setText(phone);
        topicsAry.add(0, getString(R.string.select_topic));

        ArrayAdapter queryadapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, R.id.textView, topicsAry);
        queryadapter.setDropDownViewResource(R.layout.spinner_item);
        selectTopic.setAdapter(queryadapter);

        selectTopic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (position == 0) {
                    selectedTopic = "";
                } else {
                    selectedTopic = topicsAry.get(position);
                }

                TextView textView = ((LinearLayout) parent.getChildAt(0)).findViewById(R.id.textView);
                textView.setTextColor(getResources().getColor(R.color.textSecondary));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        /* when user click spinner close a keyboard*/
        selectTopic.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                FantacyApplication.hideSoftKeyboard(ContactUs.this, selectTopic);
                return false;
            }
        });
        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void contactEmail() {

        final ProgressDialog dialog = new ProgressDialog(ContactUs.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CONTACT_ADMIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.v(TAG, "contactEmailRes=" + res);
                try {
                    JSONObject json = new JSONObject(res);
                    String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                    FantacyApplication.showToast(ContactUs.this, message, Toast.LENGTH_SHORT);
                    if (DefensiveClass.optString(json, Constants.TAG_STATUS).equals("true"))
                        finish();
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
                Log.e(TAG, "contactEmailError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("user_name", userAccount.getText().toString().trim());
                map.put("full_name", fullName.getText().toString().trim());
                map.put("email", emailEdit.getText().toString().trim());
                map.put("topic", selectedTopic);
                map.put("order_no", orderNumber.getText().toString().trim());
                map.put("message", message.getText().toString().trim());
                Log.v(TAG, "contactEmailParams=" + map);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.saveBtn:
                String full_name = fullName.getText().toString().trim();
                String emailEd = emailEdit.getText().toString().trim();
                String user_account = userAccount.getText().toString().trim();
                String msg = message.getText().toString().trim();

                if (full_name.length() == 0 || emailEd.length() == 0 || user_account.length() == 0 || msg.length() == 0 || selectedTopic.equals("")) {
                    alertText.setVisibility(View.VISIBLE);
                    alertText.setText(getString(R.string.please_fill_all));
                } else if (full_name.length() < 3 || full_name.length() > 30) {
                    alertText.setVisibility(View.VISIBLE);
                    alertText.setText(getString(R.string.fullname_is_short));
                } else if (!emailEdit.getText().toString().matches(emailPattern)) {
                    alertText.setVisibility(View.VISIBLE);
                    alertText.setText(getString(R.string.email_error));
                } else {
                    alertText.setVisibility(View.GONE);
                    contactEmail();
                }
                break;
            case R.id.callText:
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
                break;
            case R.id.emailText:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", email, null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                break;
        }
    }
}
