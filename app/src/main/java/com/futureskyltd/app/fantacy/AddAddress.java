package com.futureskyltd.app.fantacy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by hitasoft on 8/7/17.
 */

public class AddAddress extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = AddAddress.class.getSimpleName();
    ImageView backBtn;
    TextView title, saveBtn;
    EditText fullName, nickName, addressLine1, addressLine2, city, state, zipcode, phone;
    TextView country;
    LinearLayout mainLay;
    String from = "", to = "";
    RelativeLayout progress, nullLay;
    ArrayList<String> countryIds = new ArrayList<String>();
    ArrayList<String> countryNames = new ArrayList<String>();
    HashMap<String, String> data = new HashMap<String, String>();
    String selectedId, selectedName;
    private int index;
    private static int prevPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_address_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        saveBtn = (TextView) findViewById(R.id.saveBtn);
        fullName = (EditText) findViewById(R.id.fullName);
        nickName = (EditText) findViewById(R.id.nickName);
        addressLine1 = (EditText) findViewById(R.id.addressLine1);
        addressLine2 = (EditText) findViewById(R.id.addressLine2);
        city = (EditText) findViewById(R.id.city);
        state = (EditText) findViewById(R.id.state);
        zipcode = (EditText) findViewById(R.id.zipcode);
        phone = (EditText) findViewById(R.id.phone);
        mainLay = (LinearLayout) findViewById(R.id.mainLay);
        progress = (RelativeLayout) findViewById(R.id.progress);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        country = (TextView) findViewById(R.id.country);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.VISIBLE);

        from = getIntent().getExtras().getString("from");
        to = getIntent().getExtras().getString("to");

        if (to.equals("add")) {
            title.setText(getString(R.string.add_address));
        } else {
            title.setText(getString(R.string.edit_address));
            data = (HashMap<String, String>) getIntent().getExtras().get("data");
        }

        FantacyApplication.setupUI(AddAddress.this, findViewById(R.id.parentLay));

        fullName.addTextChangedListener(new FantacyApplication.avoidMultiSpace(fullName));
        nickName.addTextChangedListener(new FantacyApplication.avoidMultiSpace(nickName));
        addressLine1.addTextChangedListener(new FantacyApplication.avoidMultiSpace(addressLine1));
        addressLine2.addTextChangedListener(new FantacyApplication.avoidMultiSpace(addressLine2));
        city.addTextChangedListener(new FantacyApplication.avoidMultiSpace(city));
        state.addTextChangedListener(new FantacyApplication.avoidMultiSpace(state));

        fullName.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(30)});
        nickName.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(20)});
        city.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(30)});
        state.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(30)});
        zipcode.setFilters(new InputFilter[]{filterWithoutSpace, new InputFilter.LengthFilter(20)});
        addressLine1.setFilters(new InputFilter[]{FantacyApplication.EMOJI_FILTER, new InputFilter.LengthFilter(30)});
        addressLine2.setFilters(new InputFilter[]{FantacyApplication.EMOJI_FILTER, new InputFilter.LengthFilter(30)});
        backBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);

        progress.setVisibility(View.VISIBLE);
        mainLay.setVisibility(View.GONE);
        nullLay.setVisibility(View.GONE);

        getCountry();

        if (FantacyApplication.isRTL(AddAddress.this)) {
            fullName.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            nickName.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            addressLine1.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            addressLine2.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            city.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            state.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            zipcode.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            phone.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else {
            fullName.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            nickName.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            addressLine1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            addressLine2.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            city.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            state.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            zipcode.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            phone.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void setAddress() {
        Log.i(TAG, "setAddressData: " + data);
        nickName.setText(data.get(Constants.TAG_NICK_NAME));
        fullName.setText(data.get(Constants.TAG_FULL_NAME));
        addressLine1.setText(data.get(Constants.TAG_ADDRESS1));
        addressLine2.setText(data.get(Constants.TAG_ADDRESS2));
        city.setText(data.get(Constants.TAG_CITY));
        state.setText(data.get(Constants.TAG_STATE));
        zipcode.setText(data.get(Constants.TAG_ZIPCODE));
        phone.setText(data.get(Constants.TAG_PHONE_NO));
        selectedId = data.get(Constants.TAG_COUNTRY_ID);
    }

    private void getCountry() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerID = preferences.getString("customer_id", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PRODUCT_BEFORE_ADD, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    Log.i(TAG, "getCountryRes: " + res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {

                        JSONArray result = json.getJSONArray(Constants.TAG_COUNTRY);

                        for (int i = 0; i < result.length(); i++) {
                            JSONObject temp = result.getJSONObject(i);

                            String id = DefensiveClass.optInt(temp, Constants.TAG_ID);
                            String name = DefensiveClass.optString(temp, Constants.TAG_NAME);

                            countryIds.add(id);
                            countryNames.add(name);
                        }
                        country.setClickable(true);
                        country.setOnClickListener(AddAddress.this);

                        if (to.equals("edit")) {
                            setAddress();
                            index = countryIds.indexOf(data.get(Constants.TAG_COUNTRY_ID));
                            country.setText(countryNames.get(index));
                        }

                        progress.setVisibility(View.GONE);
                        mainLay.setVisibility(View.VISIBLE);
                        nullLay.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    setErrorLayout();
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    setErrorLayout();
                    e.printStackTrace();
                } catch (Exception e) {
                    setErrorLayout();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getCountryError: " + error.getMessage());
                setErrorLayout();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
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

        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void setErrorLayout() {
        progress.setVisibility(View.GONE);
        mainLay.setVisibility(View.GONE);
        nullLay.setVisibility(View.VISIBLE);
    }

    private void addAddress() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        final ProgressDialog dialog = new ProgressDialog(AddAddress.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADD_ADDRESS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    Log.v(TAG, "addAddressRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        if (from.equals("profile")) {
                            finish();
                            Intent i = new Intent(AddAddress.this, ShippingAddress.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.putExtra("from", from);
                            startActivity(i);
                        } else if (from.equals("cartEdit")) {
                            finish();
                            Intent c = new Intent(AddAddress.this, CartActivity.class);
                            c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            c.putExtra("shippingId", (String) getIntent().getExtras().get("shippingId"));
                            c.putExtra("itemId", (String) getIntent().getExtras().get("itemId"));
                            if(getIntent().hasExtra("size"))
                                c.putExtra("size", (String) getIntent().getExtras().get("size"));
                            c.putExtra("quantity", (String) getIntent().getExtras().get("quantity"));
                            startActivity(c);
                        } else if (from.equals("cartAdd")) {
                            JSONObject result = json.getJSONObject(Constants.TAG_RESULT);
                            String shippingId = DefensiveClass.optString(result, Constants.TAG_SHIPPING_ID);
                            finish();
                            Intent c = new Intent(AddAddress.this, CartActivity.class);
                            c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            c.putExtra("shippingId", shippingId);
                            c.putExtra("itemId", (String) getIntent().getExtras().get("itemId"));
                            if(getIntent().hasExtra("size"))
                                c.putExtra("size", (String) getIntent().getExtras().get("size"));
                            c.putExtra("quantity", (String) getIntent().getExtras().get("quantity"));
                            startActivity(c);
                        } else {
                            finish();
                            Intent i = new Intent(AddAddress.this, ShippingAddress.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.putExtra("from", from);
                            i.putExtra("shippingId", (String) getIntent().getExtras().get("shippingId"));
                            i.putExtra("itemId", (String) getIntent().getExtras().get("itemId"));
                            if(getIntent().hasExtra("size"))
                                i.putExtra("size", (String) getIntent().getExtras().get("size"));
                            i.putExtra("quantity", (String) getIntent().getExtras().get("quantity"));
                            startActivity(i);
                        }
                    } else {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals("Already a Shipping Address with this Nick Name Exist")) {
                            FantacyApplication.showToast(AddAddress.this, getString(R.string.shipping_nick_name_error), LENGTH_SHORT);
                        }
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
                Log.e(TAG, "addAddressError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("nick_name", data.get(Constants.TAG_NICK_NAME));
                map.put("full_name", data.get(Constants.TAG_FULL_NAME));
                map.put("address1", data.get(Constants.TAG_ADDRESS1));
                map.put("address2", data.get(Constants.TAG_ADDRESS2));
                map.put("city", data.get(Constants.TAG_CITY));
                map.put("state", data.get(Constants.TAG_STATE));
                map.put("zipcode", data.get(Constants.TAG_ZIPCODE));
                map.put("country_name", data.get(Constants.TAG_COUNTRY_NAME));
                map.put("phone_no", data.get(Constants.TAG_PHONE_NO));
                map.put("country_id", data.get(Constants.TAG_COUNTRY_ID));
                if (to.equals("add")) {
                    map.put("shipping_id", "0");
                } else {
                    map.put("shipping_id", data.get(Constants.TAG_SHIPPING_ID));
                }
                Log.v(TAG, "addAddressParams=" + map);
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

    InputFilter filterWithoutSpace = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    InputFilter filterWithSpace = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

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
        prevPosition = 0;
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.v("isConnected", "isConnected=" + isConnected);
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.country:
                showCountryDialog();
                break;
            case R.id.saveBtn:
                String full_name = fullName.getText().toString().trim();
                String nick_name = nickName.getText().toString().trim();
                String address_line1 = addressLine1.getText().toString().trim();
                String address_line2 = addressLine2.getText().toString().trim();
                String add_city = city.getText().toString().trim();
                String add_state = state.getText().toString().trim();
                String add_zipcode = zipcode.getText().toString().trim();
                String add_phone = phone.getText().toString().trim();
                String countryId = selectedId;
                String countryName = country.getText().toString();
                if (full_name.length() == 0 || nick_name.length() == 0 || address_line1.length() == 0 || countryName.length() == 0 || add_city.length() == 0
                        || add_state.length() == 0 || add_zipcode.length() == 0 || add_phone.length() == 0) {
                    FantacyApplication.showToast(AddAddress.this, getString(R.string.please_fill_all), LENGTH_SHORT);
                } else if (full_name.length() < 3 || full_name.length() > 30) {
                    FantacyApplication.showToast(AddAddress.this, getString(R.string.fullname_is_short), LENGTH_SHORT);
                } else if (nick_name.length() < 3 || nick_name.length() > 20) {
                    FantacyApplication.showToast(AddAddress.this, getString(R.string.nickname_is_short), LENGTH_SHORT);
                } else if (address_line1.length() < 3) {
                    FantacyApplication.showToast(AddAddress.this, getString(R.string.address_is_short), LENGTH_SHORT);
                } else if (add_city.length() < 2) {
                    FantacyApplication.showToast(AddAddress.this, getString(R.string.city_is_short), LENGTH_SHORT);
                } else if (add_state.length() < 2) {
                    FantacyApplication.showToast(AddAddress.this, getString(R.string.state_is_short), LENGTH_SHORT);
                } else {
                    data.put(Constants.TAG_FULL_NAME, full_name);
                    data.put(Constants.TAG_NICK_NAME, nick_name);
                    data.put(Constants.TAG_ADDRESS1, address_line1);
                    data.put(Constants.TAG_ADDRESS2, address_line2);
                    data.put(Constants.TAG_COUNTRY_ID, countryId);
                    data.put(Constants.TAG_COUNTRY_NAME, countryName);
                    data.put(Constants.TAG_CITY, add_city);
                    data.put(Constants.TAG_STATE, add_state);
                    data.put(Constants.TAG_ZIPCODE, add_zipcode);
                    data.put(Constants.TAG_PHONE_NO, add_phone);
                    addAddress();
                }
                break;
        }
    }

    private void showCountryDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.country_select_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        ListView countryList = (ListView) dialog.findViewById(R.id.countryLists);
        countryList.setAdapter(new ArrayAdapter<String>(AddAddress.this, android.R.layout.simple_list_item_1, countryNames));
        if (!dialog.isShowing()) {
            dialog.show();
        }
        countryList.setSelection(prevPosition);

        countryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedId = countryIds.get(position);
                selectedName = countryNames.get(position);
                /*Pass Country Id using setTag() and Country Name using setText()*/
                country.setTag(selectedId);
                country.setText(selectedName);
                prevPosition = position;
                dialog.dismiss();
            }
        });
    }
}
