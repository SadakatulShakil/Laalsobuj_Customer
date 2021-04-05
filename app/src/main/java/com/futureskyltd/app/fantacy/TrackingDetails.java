package com.futureskyltd.app.fantacy;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 20/7/17.
 */

public class TrackingDetails extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = TrackingDetails.class.getSimpleName();
    ImageView backBtn;
    TextView title, shippingDate, shippingName, shippingService, trackingId, additionalMessage, continuebtn;
    String sdate, from, shipping_date = "", shipping_name = "", shipping_service = "", tracking_id = "", additional_msg = "", orderid, image = "", item_name = "";
    EditText shippingDateEdit, shippingNameEdit, shippingServiceEdit, trackingIdEdit, additionalMessageEdit;
    long shippingTimeStamp;
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    final String inputFormat = "dd-MM-yyyy";
    SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat);
    int noOfTimesCalled = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        from = (String) getIntent().getExtras().get("from");
        if (from != null && from.equals("status")) {
            setContentView(R.layout.tracking_details);
            overridePendingTransition(R.anim.slide_up, R.anim.stay);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            backBtn = (ImageView) findViewById(R.id.backBtn);
            title = (TextView) findViewById(R.id.title);
            shippingDate = (TextView) findViewById(R.id.shippingDate);
            shippingName = (TextView) findViewById(R.id.shippingName);
            shippingService = (TextView) findViewById(R.id.shippingService);
            trackingId = (TextView) findViewById(R.id.trackingId);
            additionalMessage = (TextView) findViewById(R.id.additionalMessage);

            title.setText(getString(R.string.tracking_details));
            backBtn.setImageResource(R.drawable.close);

            backBtn.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);

            backBtn.setOnClickListener(this);

            shipping_date = (String) getIntent().getExtras().get("shipping_date");
            shipping_name = (String) getIntent().getExtras().get("shipping_name");
            shipping_service = (String) getIntent().getExtras().get("shipping_service");
            tracking_id = (String) getIntent().getExtras().get("tracking_id");
            additional_msg = (String) getIntent().getExtras().get("additional_msg");
            image = (String) getIntent().getExtras().get("image");
            item_name = (String) getIntent().getExtras().get("item_name");
            shippingName.setText(shipping_name);
            shippingService.setText(shipping_service);
            trackingId.setText(tracking_id);
            additionalMessage.setText(additional_msg);

            if (!shipping_date.equals(null) && !shipping_date.equals("")) {
                long date = Long.parseLong(shipping_date) * 1000;
                shippingDate.setText(FantacyApplication.getDate(date));
            }
        } else {
            setContentView(R.layout.return_details);
            overridePendingTransition(R.anim.slide_up, R.anim.stay);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            FantacyApplication.setupUI(TrackingDetails.this, findViewById(R.id.parentLay));

            backBtn = (ImageView) findViewById(R.id.backBtn);
            title = (TextView) findViewById(R.id.title);

            shippingDateEdit = (EditText) findViewById(R.id.shippingDate);
            shippingNameEdit = (EditText) findViewById(R.id.shippingName);
            shippingServiceEdit = (EditText) findViewById(R.id.shippingService);
            trackingIdEdit = (EditText) findViewById(R.id.trackingId);
            additionalMessageEdit = (EditText) findViewById(R.id.additionalText);

            continuebtn = (TextView) findViewById(R.id.continueBtn);
            title.setText(getString(R.string.return_details));
            image = (String) getIntent().getExtras().get("image");
            orderid = (String) getIntent().getExtras().get("orderid");
            item_name = (String) getIntent().getExtras().get("item_name");

            backBtn.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);

            shippingDateEdit.setOnClickListener(this);
            backBtn.setOnClickListener(this);
            continuebtn.setOnClickListener(this);
        }

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.slide_down);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    private void changeStatus() {
        final ProgressDialog pd;
        pd = new ProgressDialog(TrackingDetails.this);
        pd.setMessage(getString(R.string.pleasewait));
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CHANGE_STATUS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                    Log.i(TAG, "changeStatusRes= " + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        String error = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        FantacyApplication.defaultSnack(TrackingDetails.this.findViewById(R.id.parentLay), error, "error");
                    } else {
                        MyOrders.refreshorder = true;
                        finish();
                        overridePendingTransition(R.anim.stay, R.anim.slide_down);
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
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                Log.e(TAG, "changeStatusError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("order_id", orderid);
                map.put("chstatus", "Returned");

                map.put("id", "0");
                map.put("courier_name", shippingNameEdit.getText().toString().trim());
                map.put("courier_service", shippingServiceEdit.getText().toString().trim());
                map.put("track_id", trackingIdEdit.getText().toString().trim());
                map.put("notes", additionalMessageEdit.getText().toString().trim());
                map.put("shipping_date", String.valueOf(shippingTimeStamp));
                map.put("reason", additionalMessageEdit.getText().toString().trim());
                Log.i(TAG, "changeStatusParams= " + map);
                return map;
            }
        };
        pd.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    public static String getDate(long timeStamp, String format) {

        try {
            DateFormat sdf = new SimpleDateFormat(format);
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    private boolean compareDates(String compareStringOne) {
        Date date;
        Date dateCompareOne;
        Calendar now = Calendar.getInstance();

        int day = now.get(Calendar.DATE);
        int month = now.get(Calendar.MONTH) + 1;
        int year = now.get(Calendar.YEAR);

        date = parseDate(day + "-" + month + "-" + year);
        Log.e(TAG, "CurentDate=" + date.toString());
        dateCompareOne = parseDate(compareStringOne);

        Log.e(TAG, "dateCompareOne=" + dateCompareOne.toString());

        if (dateCompareOne.before(date)) {
            return false;
        } else return true;
    }

    private Date parseDate(String date) {

        try {
            return inputParser.parse(date);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shippingDate:

                int mYear, mMonth, mDay;
                Date minDate = new Date("Jan-01-01");
                Calendar mcurrentDate = Calendar.getInstance();
                mYear = mcurrentDate.get(Calendar.YEAR);
                mMonth = mcurrentDate.get(Calendar.MONTH);
                mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
                noOfTimesCalled = 0;
                minDate.setYear(Calendar.getInstance().get(Calendar.YEAR) - 100);

                DatePickerDialog mDatePicker = new DatePickerDialog(TrackingDetails.this, new DatePickerDialog.OnDateSetListener() {


                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {

                        Calendar newDate = Calendar.getInstance();
                        newDate.set(selectedyear, selectedmonth, selectedday);
                        if (!compareDates(dateFormatter.format(newDate.getTime()))) {

                            if (noOfTimesCalled % 2 == 0) {
                                noOfTimesCalled = 1;
                                // JoysaleApplication.dialog(ShippingDetail.this, getString(R.string.alert), "Date Error");
                                shippingDateEdit.setText("");
                            }

                        } else {
                            shippingTimeStamp = newDate.getTimeInMillis() / 1000;
                            sdate = getDate(newDate.getTimeInMillis(), "MMM-dd-yyyy");
                            // shippingdate.setText(dateFormatter.format(newDate.getTime()));
                            shippingDateEdit.setText(sdate);

                        }


                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select Shipping Date");
                mDatePicker.show();
                break;
            case R.id.backBtn:
                finish();
                overridePendingTransition(R.anim.stay, R.anim.slide_down);
                break;
            case R.id.continueBtn:
                if (shippingDateEdit.getText().toString().trim().length() == 0) {
                    FantacyApplication.showToast(TrackingDetails.this, getString(R.string.please_select_shipping_date), Toast.LENGTH_SHORT);
                } else if (shippingNameEdit.getText().toString().trim().length() == 0) {
                    FantacyApplication.showToast(TrackingDetails.this, getString(R.string.please_enter_shipping_name), Toast.LENGTH_SHORT);
                } else if (shippingServiceEdit.getText().toString().trim().length() == 0) {
                    FantacyApplication.showToast(TrackingDetails.this, getString(R.string.please_enter_shipping_service), Toast.LENGTH_SHORT);
                } else if (trackingIdEdit.getText().toString().trim().length() == 0) {
                    FantacyApplication.showToast(TrackingDetails.this, getString(R.string.please_enter_tracking_id), Toast.LENGTH_SHORT);
                } else {
                    changeStatus();
                }

                break;
        }
    }

}
