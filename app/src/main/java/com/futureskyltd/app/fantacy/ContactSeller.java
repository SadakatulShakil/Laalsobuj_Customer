package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;

/**
 * Created by hitasoft on 26/5/17.
 */

public class ContactSeller extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, itemImage;
    TextView title, writeComment, itemName;
    String itemId = "", itemTitle = "", image = "", shopid, selectedQuery="";
    EditText messageEdit, subject;
    Spinner querySpin;
    View subjectView;
    ArrayList<String> queryList = new ArrayList<String>();
    SharedPreferences preferences;
    String accesstoken, customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_seller);
        overridePendingTransition(R.anim.slide_up, R.anim.stay);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);
        customerId = preferences.getString("customer_id", null);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        title = (TextView) findViewById(R.id.title);
        writeComment = (TextView) findViewById(R.id.writeComment);
        itemImage = (ImageView) findViewById(R.id.itemImage);
        itemName = (TextView) findViewById(R.id.itemName);
        messageEdit = (EditText) findViewById(R.id.messageEdit);
        querySpin = (Spinner) findViewById(R.id.querySpin);
        subject = (EditText) findViewById(R.id.subject);
        subjectView = (View) findViewById(R.id.subjectView);

        title.setText(getString(R.string.contact_seller));
        backBtn.setImageResource(R.drawable.close);
        writeComment.setText(getString(R.string.send));

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        writeComment.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        writeComment.setOnClickListener(this);

        itemId = (String) getIntent().getExtras().get("itemId");
        shopid = (String) getIntent().getExtras().get("shopId");
        itemTitle = (String) getIntent().getExtras().get("itemTitle");
        image = (String) getIntent().getExtras().get("image");


        itemName.setText(itemTitle);
        if (image != null && !image.equals("")) {
            Picasso.get().load(image).into(itemImage);
        }

        queryList.add(getString(R.string.select_query));
        queryList.addAll((Collection<? extends String>) getIntent().getExtras().get("data"));

        ArrayAdapter queryAdapter = new ArrayAdapter<String>(ContactSeller.this,
                R.layout.spinner_item, R.id.textView, queryList);
        queryAdapter.setDropDownViewResource(R.layout.spinner_item);
        querySpin.setAdapter(queryAdapter);
        querySpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedQuery = queryList.get(position);
                if (selectedQuery.equalsIgnoreCase("Others")) {
                    subject.setVisibility(View.VISIBLE);
                    subjectView.setVisibility(View.VISIBLE);
                } else {
                    subject.setVisibility(View.GONE);
                    subjectView.setVisibility(View.GONE);
                }
                TextView textView = ((LinearLayout) parent.getChildAt(0)).findViewById(R.id.textView);
                textView.setTextColor(getResources().getColor(R.color.textSecondary));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        FantacyApplication.setupUI(this, findViewById(R.id.parentLay));

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void sendChat() {

        final ProgressDialog dialog = new ProgressDialog(ContactSeller.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SEND_SELLER_MESSAGE, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Log.v(TAG ,"sendChatRes="+res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        finish();
                        FantacyApplication.showToast(ContactSeller.this, getString(R.string.message_send_successfully), Toast.LENGTH_SHORT);
                    } else {
                        FantacyApplication.showToast(ContactSeller.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
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
                Log.e(TAG, "sendChatError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("item_id", itemId);
                map.put("shop_id", shopid);
                if (selectedQuery.equalsIgnoreCase("Others")) {
                    map.put("subject", subject.getText().toString());
                } else {
                    map.put("subject", selectedQuery);
                }
                map.put("message", messageEdit.getText().toString());
                Log.v(TAG, "sendChatParams="+map);
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
                overridePendingTransition(R.anim.stay, R.anim.slide_down);
                break;
            case R.id.writeComment:
                FantacyApplication.hideSoftKeyboard(ContactSeller.this, messageEdit);
                if (selectedQuery.equalsIgnoreCase("Others") && subject.getText().toString().trim().length() == 0) {
                    FantacyApplication.showToast(ContactSeller.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT);
                } else if (selectedQuery.equals(getString(R.string.select_query))){
                    FantacyApplication.showToast(ContactSeller.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT);
                } else if (messageEdit.getText().toString().trim().length() == 0){
                    FantacyApplication.showToast(ContactSeller.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT);
                } else {
                    sendChat();
                }
                break;
        }
    }
}
