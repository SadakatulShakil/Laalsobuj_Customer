package com.futureskyltd.app.fantacy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.CustomAutoCompleteTextView;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by hitasoft on 12/7/17.
 */

public class CreateGroupGift extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener,
        AdapterView.OnItemClickListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, itemImage, recipientArrow, personalizeArrow, contributionArrow, recipientTick, personalizeTick, contributionTick;
    TextView title, itemName, recipient, personalize, contribution, cancelBtn, continueBtn, share, itemPrice,
            shippingPrice, taxPrice, grandTotal, shareOthers;
    EditText fullName, addressLine1, addressLine2, state, city, zipcode, phone, giftTitle, giftMessage, giftNote;
    String itemId = "", itemTitle = "", image = "", size = "", shareUrl = "", giftId = "";
    TextView country;
    CustomAutoCompleteTextView recipientName, editText;
    RecyclerView recyclerView;
    RelativeLayout recipientLay, personalizeLay, contributionLay, progress, nullLay;
    LinearLayout recipientDetailLay, personalizeDetailLay, contributionDetailLay;
    int stepCount, textCount;
    boolean isRecipientSelected = false;
    RecyclerViewAdapter recyclerViewAdapter;
    SuggestAdapter suggestAdapter;
    HashMap<String, String> giftdetails = new HashMap<String, String>();
    ArrayList<HashMap<String, String>> contriAry = new ArrayList<>();
    ArrayList<String> countryIds = new ArrayList<String>();
    ArrayList<String> countryNames = new ArrayList<String>();
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
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
    private int prevPosition;
    String selectedName, selectedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group_gift);

        overridePendingTransition(R.anim.slide_up, R.anim.stay);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        title = (TextView) findViewById(R.id.title);
        itemImage = (ImageView) findViewById(R.id.itemImage);
        itemName = (TextView) findViewById(R.id.itemName);
        recipientArrow = (ImageView) findViewById(R.id.recipientArrow);
        personalizeArrow = (ImageView) findViewById(R.id.personalizeArrow);
        contributionArrow = (ImageView) findViewById(R.id.contributionArrow);
        recipient = (TextView) findViewById(R.id.recipient);
        personalize = (TextView) findViewById(R.id.personalize);
        contribution = (TextView) findViewById(R.id.contribution);
        cancelBtn = (TextView) findViewById(R.id.cancelBtn);
        continueBtn = (TextView) findViewById(R.id.continueBtn);
        recipientLay = (RelativeLayout) findViewById(R.id.recipientLay);
        personalizeLay = (RelativeLayout) findViewById(R.id.personalizeLay);
        contributionLay = (RelativeLayout) findViewById(R.id.contributionLay);
        recipientDetailLay = (LinearLayout) findViewById(R.id.recipientDetailLay);
        recipientTick = (ImageView) findViewById(R.id.recipientTick);
        personalizeTick = (ImageView) findViewById(R.id.personalizeTick);
        contributionTick = (ImageView) findViewById(R.id.contributionTick);
        country = (TextView) findViewById(R.id.country);
        fullName = (EditText) findViewById(R.id.fullName);
        recipientName = (CustomAutoCompleteTextView) findViewById(R.id.recipientName);
        addressLine1 = (EditText) findViewById(R.id.addressLine1);
        addressLine2 = (EditText) findViewById(R.id.addressLine2);
        city = (EditText) findViewById(R.id.city);
        state = (EditText) findViewById(R.id.state);
        zipcode = (EditText) findViewById(R.id.zipcode);
        phone = (EditText) findViewById(R.id.phone);
        personalizeDetailLay = (LinearLayout) findViewById(R.id.personalizeDetailLay);
        contributionDetailLay = (LinearLayout) findViewById(R.id.contributionDetailLay);
        share = (TextView) findViewById(R.id.share);
        itemPrice = (TextView) findViewById(R.id.itemPrice);
        shippingPrice = (TextView) findViewById(R.id.shippingPrice);
        taxPrice = (TextView) findViewById(R.id.taxPrice);
        grandTotal = (TextView) findViewById(R.id.grandTotal);
        giftTitle = (EditText) findViewById(R.id.giftTitle);
        giftMessage = (EditText) findViewById(R.id.giftMessage);
        giftNote = (EditText) findViewById(R.id.giftNote);
        editText = (CustomAutoCompleteTextView) findViewById(R.id.editText);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        progress = (RelativeLayout) findViewById(R.id.progress);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        shareOthers = (TextView) findViewById(R.id.shareOthers);

        title.setText(getString(R.string.group_gift));
        backBtn.setImageResource(R.drawable.close);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        shareOthers.setOnClickListener(this);
        share.setOnClickListener(this);
        country.setOnClickListener(this);
        fullName.addTextChangedListener(new FantacyApplication.avoidMultiSpace(fullName));
        addressLine1.addTextChangedListener(new FantacyApplication.avoidMultiSpace(addressLine1));
        addressLine2.addTextChangedListener(new FantacyApplication.avoidMultiSpace(addressLine2));
        city.addTextChangedListener(new FantacyApplication.avoidMultiSpace(city));
        state.addTextChangedListener(new FantacyApplication.avoidMultiSpace(state));

        fullName.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(30)});
        city.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(30)});
        state.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(30)});
        zipcode.setFilters(new InputFilter[]{filterWithoutSpace, new InputFilter.LengthFilter(20)});

        itemId = (String) getIntent().getExtras().get("itemId");
        itemTitle = (String) getIntent().getExtras().get("itemTitle");
        image = (String) getIntent().getExtras().get("image");
        size = (String) getIntent().getExtras().get("size");

        itemName.setText(itemTitle);
        if (image != null && !image.equals("")) {
            Picasso.get().load(image).into(itemImage);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(this, contriAry);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setHasFixedSize(true);

        progress.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);
        recipientDetailLay.setVisibility(View.GONE);
        continueBtn.setOnClickListener(null);
        cancelBtn.setOnClickListener(null);
        recipientName.addTextChangedListener(new AutoSuggestTextWatcher(recipientName));
        recipientName.setOnItemClickListener(this);
        editText.addTextChangedListener(new AutoSuggestTextWatcher(editText));
        editText.setOnItemClickListener(this);

        getCountry();

        FantacyApplication.setupUI(this, findViewById(R.id.parentLay));

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getCountry() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PRODUCT_BEFORE_ADD, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
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
                        country.setOnClickListener(CreateGroupGift.this);

                        progress.setVisibility(View.GONE);
                        recipientDetailLay.setVisibility(View.VISIBLE);
                        nullLay.setVisibility(View.GONE);
                        continueBtn.setOnClickListener(CreateGroupGift.this);
                        cancelBtn.setOnClickListener(CreateGroupGift.this);
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

    private void getPaymentDetails() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GROUP_GIFT_PAYMENT_DETAIL, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getCountryRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);

                        String currency = DefensiveClass.optString(result, Constants.TAG_CURRENCY);
                        String item_total = DefensiveClass.optString(result, Constants.TAG_ITEM_TOTAL);
                        String shipping_price = DefensiveClass.optString(result, Constants.TAG_SHIPPING_PRICE);
                        String tax = DefensiveClass.optString(result, Constants.TAG_TAX);
                        String grand_total = DefensiveClass.optString(result, Constants.TAG_GRAND_TOTAL);
                        if (shipping_price.equals(""))
                            shipping_price = "0";
                        giftdetails.put(Constants.TAG_ITEM_TOTAL, item_total);
                        giftdetails.put(Constants.TAG_SHIPPING_PRICE, shipping_price);
                        giftdetails.put(Constants.TAG_TAX, tax);
                        giftdetails.put(Constants.TAG_GRAND_TOTAL, grand_total);

                        itemPrice.setText(currency + " " + item_total);
                        grandTotal.setText(currency + " " + grand_total);

                        if (shipping_price.equals("") || shipping_price.equals(null)) {
                            shippingPrice.setText(currency + " 0");
                        } else {
                            shippingPrice.setText(currency + " " + shipping_price);
                        }

                        if (tax.equals("") || tax.equals(null)) {
                            taxPrice.setText(currency + " 0");
                        } else {
                            taxPrice.setText(currency + " " + tax);
                        }

                        stepCount = 1;
                        recipientTick.setVisibility(View.VISIBLE);
                        recipientArrow.setVisibility(View.GONE);
                        personalize.setTextColor(ContextCompat.getColor(CreateGroupGift.this, R.color.white));
                        personalizeArrow.setVisibility(View.VISIBLE);
                        recipientDetailLay.setVisibility(View.GONE);
                        personalizeDetailLay.setVisibility(View.VISIBLE);
                        cancelBtn.setText(getString(R.string.back));
                        continueBtn.setText(getString(R.string.create));
                        continueBtn.setOnClickListener(CreateGroupGift.this);
                        cancelBtn.setOnClickListener(CreateGroupGift.this);
                        progress.setVisibility(View.GONE);
                    } else {
                        recipientDetailLay.setVisibility(View.VISIBLE);
                        progress.setVisibility(View.GONE);
                        continueBtn.setOnClickListener(CreateGroupGift.this);
                        cancelBtn.setOnClickListener(CreateGroupGift.this);
                        FantacyApplication.showToast(CreateGroupGift.this, DefensiveClass.optString(json, Constants.TAG_MESSAGE), Toast.LENGTH_SHORT);
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
                Log.e(TAG, "getPaymentDetailsError: " + error.getMessage());
                setErrorLayout();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("item_id", itemId);
                map.put("size", size);
                map.put("quantity", "1");
                map.put("zipcode", zipcode.getText().toString().trim());
                map.put("country_id", selectedId);
                Log.v(TAG, "getCountryParams=" + map);
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

        recipientDetailLay.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);
        continueBtn.setOnClickListener(null);
        cancelBtn.setOnClickListener(null);

        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void createGroupGift() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);


        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CREATE_GROUP_GIFT, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "createGroupGiftRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);

                        giftId = DefensiveClass.optString(result, Constants.TAG_GIFT_ID);
                        String recipient_name = DefensiveClass.optString(result, Constants.TAG_RECIPIENT_NAME);
                        String city = DefensiveClass.optString(result, Constants.TAG_CITY);
                        shareUrl = DefensiveClass.optString(result, Constants.TAG_SHARE_URL);
                        String title = DefensiveClass.optString(result, Constants.TAG_TITLE);
                        String description = DefensiveClass.optString(result, Constants.TAG_DESCRIPTION);
                        String item_name = DefensiveClass.optString(result, Constants.TAG_ITEM_NAME);
                        String item_image = DefensiveClass.optString(result, Constants.TAG_ITEM_IMAGE);
                        String currency = DefensiveClass.optString(result, Constants.TAG_CURRENCY);
                        String item_total = DefensiveClass.optString(result, Constants.TAG_ITEM_TOTAL);
                        String shipping_price = DefensiveClass.optString(result, Constants.TAG_SHIPPING_PRICE);
                        String tax = DefensiveClass.optString(result, Constants.TAG_TAX);
                        String grand_total = DefensiveClass.optString(result, Constants.TAG_GRAND_TOTAL);

                        stepCount = 2;
                        personalizeTick.setVisibility(View.VISIBLE);
                        personalizeArrow.setVisibility(View.GONE);
                        contribution.setTextColor(ContextCompat.getColor(CreateGroupGift.this, R.color.white));
                        contributionArrow.setVisibility(View.VISIBLE);
                        personalizeDetailLay.setVisibility(View.GONE);
                        contributionDetailLay.setVisibility(View.VISIBLE);
                        cancelBtn.setVisibility(View.GONE);
                        continueBtn.setText(getString(R.string.finish));
                        continueBtn.setOnClickListener(CreateGroupGift.this);
                        progress.setVisibility(View.GONE);

                        FantacyApplication.showToast(CreateGroupGift.this, getString(R.string.groupgift_created_successfully), Toast.LENGTH_SHORT);
                    } else {
                        continueBtn.setOnClickListener(CreateGroupGift.this);
                        cancelBtn.setOnClickListener(CreateGroupGift.this);
                        progress.setVisibility(View.GONE);
                        personalizeDetailLay.setVisibility(View.VISIBLE);
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals("Item not found"))
                            FantacyApplication.showToast(CreateGroupGift.this, getString(R.string.item_not_found), Toast.LENGTH_SHORT);
                        else if (message.equals("Shipping can not be done"))
                            FantacyApplication.showToast(CreateGroupGift.this, getString(R.string.shipping_cannot_done), Toast.LENGTH_SHORT);
                        else
                            FantacyApplication.showToast(CreateGroupGift.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
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
                Log.e(TAG, "createGroupGiftError: " + error.getMessage());
                setErrorLayout();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("item_id", itemId);
                map.put("size", size);
                map.put("quantity", "1");
                map.put("recipient", recipientName.getText().toString().trim());
                map.put("full_name", fullName.getText().toString().trim());
                map.put("address1", addressLine1.getText().toString().trim());

                map.put("item_total", giftdetails.get(Constants.TAG_ITEM_TOTAL));
                map.put("shipping_price", giftdetails.get(Constants.TAG_SHIPPING_PRICE));
                map.put("tax", giftdetails.get(Constants.TAG_TAX));
                map.put("grand_total", giftdetails.get(Constants.TAG_GRAND_TOTAL));


                if (addressLine2.getText().toString().trim().length() == 0) {
                    map.put("address2", "");
                } else {
                    map.put("address2", addressLine2.getText().toString().trim());
                }
                map.put("country_id", selectedId);
                map.put("state", state.getText().toString().trim());
                map.put("city", city.getText().toString().trim());
                map.put("zipcode", zipcode.getText().toString().trim());
                map.put("phone", phone.getText().toString().trim());
                map.put("title", giftTitle.getText().toString().trim());
                map.put("description", giftMessage.getText().toString().trim());
                if (giftNote.getText().toString().trim().length() == 0) {
                    map.put("note", "");
                } else {
                    map.put("note", giftNote.getText().toString().trim());
                }
                Log.v(TAG, "createGroupGiftParams=" + map);
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

        personalizeDetailLay.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);
        continueBtn.setOnClickListener(null);
        cancelBtn.setOnClickListener(null);

        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void setErrorLayout() {
        progress.setVisibility(View.GONE);
        nullLay.setVisibility(View.VISIBLE);
        if (stepCount == 0) {
            recipientDetailLay.setVisibility(View.GONE);
        } else if (stepCount == 1) {
            personalizeDetailLay.setVisibility(View.GONE);
        } else if (stepCount == 2) {
            contributionDetailLay.setVisibility(View.GONE);
        }
    }

    private void getUserData(final String key) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_AT_USER_TAG, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
                Log.v(TAG, "getUserDataRes=" + res);
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray result = json.getJSONArray("result");

                        for (int i = 0; i < result.length(); i++) {
                            HashMap<String, String> data = new HashMap<String, String>();

                            JSONObject temp = result.getJSONObject(i);
                            String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                            String full_name = DefensiveClass.optString(temp, Constants.TAG_FULL_NAME);
                            String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                            String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                            String email = DefensiveClass.optString(temp, Constants.TAG_EMAIL);
                            String country_name = DefensiveClass.optString(temp, Constants.TAG_COUNTRY);
                            String state = DefensiveClass.optString(temp, Constants.TAG_STATE);
                            String address1 = DefensiveClass.optString(temp, Constants.TAG_ADDRESS1);
                            String address2 = DefensiveClass.optString(temp, Constants.TAG_ADDRESS2);
                            String city = DefensiveClass.optString(temp, Constants.TAG_CITY);
                            String zipcode = DefensiveClass.optString(temp, Constants.TAG_ZIPCODE);
                            String phone = DefensiveClass.optString(temp, Constants.TAG_PHONE_NO);

                            data.put(Constants.TAG_USER_ID, user_id);
                            data.put(Constants.TAG_FULL_NAME, full_name);
                            data.put(Constants.TAG_USER_NAME, user_name);
                            data.put(Constants.TAG_USER_IMAGE, user_image);
                            data.put(Constants.TAG_EMAIL, email);
                            data.put(Constants.TAG_COUNTRY, country_name);
                            data.put(Constants.TAG_STATE, state);
                            data.put(Constants.TAG_ADDRESS1, address1);
                            data.put(Constants.TAG_ADDRESS2, address2);
                            data.put(Constants.TAG_CITY, city);
                            data.put(Constants.TAG_ZIPCODE, zipcode);
                            data.put(Constants.TAG_PHONE_NO, phone);

                            if (!user_name.equals(customerId)) {
                                if (!contriAry.contains(data)) {
                                    aList.add(data);
                                }
                            }

                        }


                        if (aList.size() > 0) {
                            suggestAdapter = new SuggestAdapter(CreateGroupGift.this, R.layout.atmention_layout, aList);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (recipientDetailLay.getVisibility() == View.VISIBLE) {
                                        if (recipientName != null) {
                                            recipientName.setAdapter(suggestAdapter);
                                            recipientName.showDropDown();
                                            Log.v("show", "show");
                                        }
                                    } else {
                                        if (editText != null) {
                                            editText.setAdapter(suggestAdapter);
                                            editText.showDropDown();
                                            Log.v("show", "show");
                                        }
                                    }

                                }
                            });
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getUserDataError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("key", key);
                Log.v(TAG, "getUserDataParams=" + map);
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
        FantacyApplication.getInstance().getRequestQueue().cancelAll("@tag");
        FantacyApplication.getInstance().addToRequestQueue(req, "@tag");
    }

    private void shareUsers() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SHARE_GROUPGIFT, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.v(TAG, "shareUsersRes=" + res);
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        FantacyApplication.showToast(CreateGroupGift.this, getString(R.string.shared_successfully), Toast.LENGTH_SHORT);
                        finish();

                    } else {
                        FantacyApplication.showToast(CreateGroupGift.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
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
                Log.e(TAG, "shareUsersError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                ArrayList<String> selectedCont = new ArrayList<String>();
                for (int i = 0; i < contriAry.size(); i++) {
                    selectedCont.add(contriAry.get(i).get(Constants.TAG_USER_ID));
                }
                map.put("user_id", customerId);
                map.put("gift_id", giftId);
                map.put("contributor_id", selectedCont.toString().replaceAll("\\[", "").replaceAll("\\]", ""));
                Log.v(TAG, "shareUsersParams=" + map);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        FantacyApplication.hideSoftKeyboard(CreateGroupGift.this, recipientName);
        HashMap<String, String> map = (HashMap<String, String>) parent.getAdapter().getItem(position);
        isRecipientSelected = true;

        if (recipientDetailLay.getVisibility() == View.VISIBLE) {
            recipientName.setText(map.get(Constants.TAG_EMAIL));
            recipientName.clearFocus();
            fullName.setText(map.get(Constants.TAG_FULL_NAME));
            addressLine1.setText(map.get(Constants.TAG_ADDRESS1));
            addressLine2.setText(map.get(Constants.TAG_ADDRESS2));
            state.setText(map.get(Constants.TAG_STATE));
            city.setText(map.get(Constants.TAG_CITY));
            zipcode.setText(map.get(Constants.TAG_ZIPCODE));
            phone.setText(map.get(Constants.TAG_PHONE_NO));

            if (countryNames.contains(map.get(Constants.TAG_COUNTRY))) {
                int pos = countryNames.indexOf(map.get(Constants.TAG_COUNTRY));
                country.setText(countryNames.get(pos));
                selectedId = countryIds.get(pos);
            }
            recipientName.dismissDropDown();

        } else {
            contriAry.add(0, map);
            recyclerViewAdapter.notifyItemInserted(0);
            recyclerView.smoothScrollToPosition(0);
            editText.dismissDropDown();
        }
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
            case R.id.country:
                showCountryDialog();
                break;
            case R.id.cancelBtn:
                if (stepCount == 0) {
                    finish();
                    overridePendingTransition(R.anim.stay, R.anim.slide_down);
                } else if (stepCount == 1) {
                    stepCount = 0;
                    recipientTick.setVisibility(View.GONE);
                    recipientArrow.setVisibility(View.VISIBLE);
                    personalize.setTextColor(ContextCompat.getColor(this, R.color.opct_white));
                    personalizeArrow.setVisibility(View.GONE);
                    recipientDetailLay.setVisibility(View.VISIBLE);
                    personalizeDetailLay.setVisibility(View.GONE);
                    cancelBtn.setText(getString(R.string.cancel));
                    continueBtn.setText(getString(R.string.continu));
                } else if (stepCount == 2) {
                    stepCount = 1;
                    personalizeTick.setVisibility(View.GONE);
                    personalizeArrow.setVisibility(View.VISIBLE);
                    contribution.setTextColor(ContextCompat.getColor(this, R.color.opct_white));
                    contributionArrow.setVisibility(View.GONE);
                    personalizeDetailLay.setVisibility(View.VISIBLE);
                    contributionDetailLay.setVisibility(View.GONE);
                    cancelBtn.setText(getString(R.string.back));
                    continueBtn.setText(getString(R.string.create));
                }
                break;
            case R.id.continueBtn:
                if (stepCount == 0) {
                    String full_name = fullName.getText().toString().trim();
                    String recipient_name = recipientName.getText().toString().trim();
                    String address_line1 = addressLine1.getText().toString().trim();
                    String address_line2 = addressLine2.getText().toString().trim();
                    String add_city = city.getText().toString().trim();
                    String add_state = state.getText().toString().trim();
                    // String add_country = country.getText().toString().trim();
                    String add_zipcode = zipcode.getText().toString().trim();
                    String add_phone = phone.getText().toString().trim();

                    if (full_name.length() == 0 || recipient_name.length() == 0 || address_line1.length() == 0 || add_city.length() == 0
                            || add_state.length() == 0 || add_phone.length() == 0 || countryIds.size() == 0) {
                        FantacyApplication.showToast(this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT);
                    } else if (full_name.length() < 3) {
                        FantacyApplication.showToast(this, getString(R.string.fullname_is_short), Toast.LENGTH_SHORT);
                    } else if (recipient_name.length() < 3) {
                        FantacyApplication.showToast(this, getString(R.string.nickname_is_short), Toast.LENGTH_SHORT);
                    } else if (!recipientName.getText().toString().matches(emailPattern)) {
                        FantacyApplication.showToast(this, getString(R.string.email_error), Toast.LENGTH_SHORT);
                    } else if (address_line1.length() < 3) {
                        FantacyApplication.showToast(this, getString(R.string.address_is_short), Toast.LENGTH_SHORT);
                    } else if (add_city.length() < 2) {
                        FantacyApplication.showToast(this, getString(R.string.city_is_short), Toast.LENGTH_SHORT);
                    } else if (add_state.length() < 2) {
                        FantacyApplication.showToast(this, getString(R.string.state_is_short), Toast.LENGTH_SHORT);
                    } else {
                        getPaymentDetails();
                    }
                } else if (stepCount == 1) {
                    if (giftTitle.getText().toString().trim().length() == 0 || giftMessage.getText().toString().trim().length() == 0) {
                        FantacyApplication.showToast(this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT);
                    } else {
                        createGroupGift();
                    }
                } else if (stepCount == 2) {
                    /*finish();
                    Intent i = new Intent(this, GroupGiftDetail.class);
                    i.putExtra("giftId", giftId);
                    startActivity(i);
                    overridePendingTransition(R.anim.stay, R.anim.slide_down);*/
                    Toast.makeText(this, "Under COnstruction !", Toast.LENGTH_SHORT).show();

                }
                break;
            case R.id.shareOthers:
                Intent g = new Intent(Intent.ACTION_SEND);
                g.setType("text/plain");
                g.putExtra(Intent.EXTRA_TEXT, shareUrl);
                startActivity(Intent.createChooser(g, "Share"));
                break;
            case R.id.share:
                if (contriAry.size() > 0) {
                    shareUsers();
                }
                break;
        }
    }

    /**
     * Adapter for suggestion list
     **/
    public class SuggestAdapter extends ArrayAdapter<HashMap<String, String>> {
        Context context;
        List<HashMap<String, String>> Items;

        public SuggestAdapter(Context context, int ResourceId,
                              List<HashMap<String, String>> objects) {
            super(context, ResourceId, objects);
            this.context = context;
            this.Items = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.atmention_layout, parent, false);//layout
            } else {
                view = convertView;
                view.forceLayout();
            }

            HashMap<String, String> hm = Items.get(position);

            ImageView userImage = (ImageView) view.findViewById(R.id.userImage);
            TextView userName = (TextView) view.findViewById(R.id.userName);

            userName.setText(hm.get(Constants.TAG_USER_NAME));
            String image = hm.get(Constants.TAG_USER_IMAGE);
            if (image != null && !image.equals("")) {
                Picasso.get().load(image).into(userImage);
            }

            return view;
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.gift_contributors_item, parent, false);

            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.userName.setText(tempMap.get(Constants.TAG_USER_NAME));
            String img = tempMap.get(Constants.TAG_USER_IMAGE);
            if (!img.equals("")) {
                Picasso.get().load(img).into(holder.userImage);
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView userName;
            ImageView userImage, cancel;

            public MyViewHolder(View view) {
                super(view);

                userName = (TextView) view.findViewById(R.id.userName);
                userImage = (ImageView) view.findViewById(R.id.userImage);
                cancel = (ImageView) view.findViewById(R.id.cancel);

                cancel.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.cancel:
                        contriAry.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        break;
                }
            }
        }
    }

    private class AutoSuggestTextWatcher implements TextWatcher {

        private AutoSuggestTextWatcher(EditText editText) {
            super();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            String temp = s.toString();
            if (textCount == 0 || textCount <= temp.length()) {
                textCount = temp.length();
                //if (!Patterns.EMAIL_ADDRESS.matcher(temp).matches())
                if (!isRecipientSelected)
                    getUserData(temp.replace("@", ""));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            textCount = s.toString().length();
            isRecipientSelected = false;
        }

    }

    private void showCountryDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.country_select_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        ListView countryList = (ListView) dialog.findViewById(R.id.countryLists);
        countryList.setAdapter(new ArrayAdapter<String>(CreateGroupGift.this, android.R.layout.simple_list_item_1, countryNames));
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
