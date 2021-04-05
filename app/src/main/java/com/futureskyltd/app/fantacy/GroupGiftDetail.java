package com.futureskyltd.app.fantacy;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInResult;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GroupGiftDetail extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = GroupGiftDetail.class.getSimpleName();
    private static final int DROP_IN_REQUEST = 100;
    ImageView backBtn, cartBtn, itemImage, creatorImage, recipientImage;
    TextView title, giftExpired, giftContributionInfo, giftTitle, giftDes, itemName, createdDate, address, creatorName,
            recipientName, totalAmount, totalContributions, pending, contributeBtn, noContributors;
    RecyclerView recyclerView;
    Display display;
    String giftId = "", contributionAmt = "";
    Float pendingAmt;
    LinearLayout mainLay;
    RelativeLayout progressLay, nullLay;
    RecyclerViewAdapter recyclerViewAdapter;
    HashMap<String, String> dataMap = new HashMap<String, String>();
    ArrayList<HashMap<String, String>> contributorAry = new ArrayList<HashMap<String, String>>();
    SharedPreferences preferences;
    String accesstoken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_gift_detail);
        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        title = (TextView) findViewById(R.id.title);
        cartBtn = (ImageView) findViewById(R.id.cartBtn);
        itemImage = (ImageView) findViewById(R.id.itemImage);
        creatorImage = (ImageView) findViewById(R.id.creatorImage);
        recipientImage = (ImageView) findViewById(R.id.recipientImage);
        giftExpired = (TextView) findViewById(R.id.giftExpired);
        giftContributionInfo = (TextView) findViewById(R.id.giftContributionInfo);
        giftTitle = (TextView) findViewById(R.id.giftTitle);
        giftDes = (TextView) findViewById(R.id.giftDes);
        itemName = (TextView) findViewById(R.id.itemName);
        createdDate = (TextView) findViewById(R.id.createdDate);
        address = (TextView) findViewById(R.id.address);
        creatorName = (TextView) findViewById(R.id.creatorName);
        recipientName = (TextView) findViewById(R.id.recipientName);
        totalAmount = (TextView) findViewById(R.id.totalAmount);
        totalContributions = (TextView) findViewById(R.id.totalContributions);
        pending = (TextView) findViewById(R.id.pending);
        contributeBtn = (TextView) findViewById(R.id.contributeBtn);
        noContributors = (TextView) findViewById(R.id.noContributors);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        progressLay = (RelativeLayout) findViewById(R.id.progress);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        mainLay = (LinearLayout) findViewById(R.id.mainLay);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        cartBtn.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.group_gift));
        cartBtn.setImageResource(R.drawable.share_white);
        display = this.getWindowManager().getDefaultDisplay();

        backBtn.setOnClickListener(this);
        contributeBtn.setOnClickListener(this);
        cartBtn.setOnClickListener(this);
        creatorImage.setOnClickListener(this);

        giftId = getIntent().getExtras().getString("giftId");

        progressLay.setVisibility(View.VISIBLE);
        mainLay.setVisibility(View.GONE);
        contributeBtn.setVisibility(View.GONE);

        if (FantacyApplication.isRTL(GroupGiftDetail.this)) {
            itemName.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            address.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else {
            title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            address.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }

        getData();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(this, contributorAry);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setHasFixedSize(true);

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getData() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GROUP_GIFT_DETAIL, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    Log.v(TAG, "getDataRes=" + res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);

                        String gift_id = DefensiveClass.optString(result, Constants.TAG_GIFT_ID);
                        String recipient_name = DefensiveClass.optString(result, Constants.TAG_RECIPIENT_NAME);
                        String recipient_image = DefensiveClass.optString(result, Constants.TAG_RECIPIENT_IMAGE);
                        String start_date = DefensiveClass.optString(result, Constants.TAG_START_DATE);
                        String end_date = DefensiveClass.optString(result, Constants.TAG_END_DATE);
                        String gift_status = DefensiveClass.optString(result, Constants.TAG_STATUS);
                        String currency = DefensiveClass.optString(result, Constants.TAG_CURRENCY);
                        String currency_code = DefensiveClass.optString(result, Constants.TAG_CURRENCY_CODE);
                        String minimum_contribution = DefensiveClass.optString(result, Constants.TAG_MINIMUM_CONTRIBUTION);
                        String total_contribution = DefensiveClass.optString(result, Constants.TAG_TOTAL_CONTRIBUTION);
                        String paid_contribution = DefensiveClass.optString(result, Constants.TAG_PAID_CONTRIBUTION);
                        String total_contributors = DefensiveClass.optString(result, Constants.TAG_TOTAL_CONTRIBUTORS);
                        String share_url = DefensiveClass.optString(result, Constants.TAG_SHARE_URL);
                        String title = DefensiveClass.optString(result, Constants.TAG_TITLE);
                        String description = DefensiveClass.optString(result, Constants.TAG_DESCRIPTION);
                        String creator_id = DefensiveClass.optString(result, Constants.TAG_CREATOR_ID);
                        String creator_name = DefensiveClass.optString(result, Constants.TAG_CREATOR_NAME);
                        String creator_image = DefensiveClass.optString(result, Constants.TAG_CREATOR_IMAGE);
                        String merchant_id = DefensiveClass.optString(result, Constants.TAG_MERCHANT_ID);

                        JSONObject address = result.getJSONObject(Constants.TAG_ADDRESS);
                        String address1 = DefensiveClass.optString(address, Constants.TAG_ADDRESS1);
                        String address2 = DefensiveClass.optString(address, Constants.TAG_ADDRESS2);
                        String city = DefensiveClass.optString(address, Constants.TAG_CITY);
                        String state = DefensiveClass.optString(address, Constants.TAG_STATE);
                        String country = DefensiveClass.optString(address, Constants.TAG_COUNTRY);

                        JSONObject product = result.getJSONObject(Constants.TAG_PRODUCT);
                        String item_id = DefensiveClass.optString(product, Constants.TAG_ITEM_ID);
                        String item_name = DefensiveClass.optString(product, Constants.TAG_ITEM_NAME);
                        String item_image = DefensiveClass.optString(product, Constants.TAG_ITEM_IMAGE);

                        JSONArray contributors = result.optJSONArray(Constants.TAG_CONTRIBUTORS);
                        if (contributors != null) {
                            for (int i = 0; i < contributors.length(); i++) {
                                JSONObject jobj = contributors.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<String, String>();

                                String user_id = DefensiveClass.optString(jobj, Constants.TAG_USER_ID);
                                String user_name = DefensiveClass.optString(jobj, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(jobj, Constants.TAG_USER_IMAGE);
                                String amount_contributed = DefensiveClass.optString(jobj, Constants.TAG_AMOUNT_CONTRIBUTED);

                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_AMOUNT_CONTRIBUTED, amount_contributed);

                                contributorAry.add(map);
                            }
                        }

                        dataMap.put(Constants.TAG_GIFT_ID, gift_id);
                        dataMap.put(Constants.TAG_RECIPIENT_NAME, recipient_name);
                        dataMap.put(Constants.TAG_RECIPIENT_IMAGE, recipient_image);
                        dataMap.put(Constants.TAG_START_DATE, start_date);
                        dataMap.put(Constants.TAG_END_DATE, end_date);
                        dataMap.put(Constants.TAG_STATUS, gift_status);
                        dataMap.put(Constants.TAG_CURRENCY, currency);
                        dataMap.put(Constants.TAG_CURRENCY_CODE, currency_code);
                        dataMap.put(Constants.TAG_MINIMUM_CONTRIBUTION, minimum_contribution);
                        dataMap.put(Constants.TAG_TOTAL_CONTRIBUTION, total_contribution);
                        dataMap.put(Constants.TAG_PAID_CONTRIBUTION, paid_contribution);
                        dataMap.put(Constants.TAG_TOTAL_CONTRIBUTORS, total_contributors);
                        dataMap.put(Constants.TAG_SHARE_URL, share_url);
                        dataMap.put(Constants.TAG_TITLE, title);
                        dataMap.put(Constants.TAG_DESCRIPTION, description);
                        dataMap.put(Constants.TAG_CREATOR_ID, creator_id);
                        dataMap.put(Constants.TAG_CREATOR_NAME, creator_name);
                        dataMap.put(Constants.TAG_CREATOR_IMAGE, creator_image);
                        dataMap.put(Constants.TAG_MERCHANT_ID, merchant_id);
                        dataMap.put(Constants.TAG_ADDRESS1, address1);
                        dataMap.put(Constants.TAG_ADDRESS2, address2);
                        dataMap.put(Constants.TAG_CITY, city);
                        dataMap.put(Constants.TAG_STATE, state);
                        dataMap.put(Constants.TAG_COUNTRY, country);
                        dataMap.put(Constants.TAG_ITEM_ID, item_id);
                        dataMap.put(Constants.TAG_ITEM_NAME, item_name);
                        dataMap.put(Constants.TAG_ITEM_IMAGE, item_image);

                        setData();
                        mainLay.setVisibility(View.VISIBLE);
                        progressLay.setVisibility(View.GONE);
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
                Log.e(TAG, "getDataError: " + error.getMessage());
                setErrorLayout();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", "289");
                map.put("gift_id", giftId);
                Log.e(TAG, "getDataParams=" + map);
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

    private void setData() {

        giftTitle.setText(dataMap.get(Constants.TAG_TITLE));
        giftDes.setText(dataMap.get(Constants.TAG_DESCRIPTION));
        itemName.setText(dataMap.get(Constants.TAG_ITEM_NAME));
        address.setText(dataMap.get(Constants.TAG_ADDRESS1) + ", " + dataMap.get(Constants.TAG_ADDRESS2) + ", " +
                dataMap.get(Constants.TAG_CITY) + ", " + dataMap.get(Constants.TAG_STATE) + ", " + dataMap.get(Constants.TAG_COUNTRY));
        creatorName.setText(dataMap.get(Constants.TAG_CREATOR_NAME));
        recipientName.setText(dataMap.get(Constants.TAG_RECIPIENT_NAME));
        totalAmount.setText(dataMap.get(Constants.TAG_CURRENCY) + " " + dataMap.get(Constants.TAG_TOTAL_CONTRIBUTION));
        totalContributions.setText(dataMap.get(Constants.TAG_CURRENCY) + " " + dataMap.get(Constants.TAG_PAID_CONTRIBUTION));

        pendingAmt = Float.parseFloat(dataMap.get(Constants.TAG_TOTAL_CONTRIBUTION)) - Float.parseFloat(dataMap.get(Constants.TAG_PAID_CONTRIBUTION));
        pendingAmt = Float.valueOf(FantacyApplication.decimal.format(pendingAmt));
        pending.setText(dataMap.get(Constants.TAG_CURRENCY) + " " + pendingAmt);

        long enddate = 0, startdate = 0;
        if (!dataMap.get(Constants.TAG_START_DATE).equals(null) && !dataMap.get(Constants.TAG_START_DATE).equals("")) {
            startdate = Long.parseLong(dataMap.get(Constants.TAG_START_DATE)) * 1000;
        }
        createdDate.setText(getString(R.string.created) + " : " + FantacyApplication.getDate(startdate));

        if (!dataMap.get(Constants.TAG_END_DATE).equals(null) && !dataMap.get(Constants.TAG_END_DATE).equals("")) {
            enddate = Long.parseLong(dataMap.get(Constants.TAG_END_DATE)) * 1000;
        }
        @SuppressLint({"StringFormatInvalid", "LocalSuppress"})
        String info = getString(R.string.groupgift_contribution_info, FantacyApplication.getDate(enddate));
        giftContributionInfo.setText(Html.fromHtml(info + " <font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>" + getString(R.string.groupgift_contribution_info_continue) + "</font>"));

        if (dataMap.get(Constants.TAG_STATUS).equalsIgnoreCase("Expired")) {
            giftExpired.setVisibility(View.VISIBLE);
            contributeBtn.setVisibility(View.GONE);
        } else if (dataMap.get(Constants.TAG_TOTAL_CONTRIBUTION).equalsIgnoreCase(dataMap.get(Constants.TAG_PAID_CONTRIBUTION))) {
            giftExpired.setVisibility(View.GONE);
            contributeBtn.setVisibility(View.GONE);
        } else {
            giftExpired.setVisibility(View.GONE);
            contributeBtn.setVisibility(View.VISIBLE);
        }

        String item_image = dataMap.get(Constants.TAG_ITEM_IMAGE);
        if (!item_image.equals("")) {
            Picasso.get().load(item_image).into(itemImage);
        }

        String creator_image = dataMap.get(Constants.TAG_CREATOR_IMAGE);
        if (!creator_image.equals("")) {
            Picasso.get().load(creator_image).into(creatorImage);
        }

        String recipient_image = dataMap.get(Constants.TAG_RECIPIENT_IMAGE);
        if (!recipient_image.equals("")) {
            Picasso.get().load(recipient_image).into(recipientImage);
        }

        if (contributorAry.size() == 0) {
            noContributors.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void setErrorLayout() {
        progressLay.setVisibility(View.GONE);
        mainLay.setVisibility(View.GONE);
        contributeBtn.setVisibility(View.GONE);
        nullLay.setVisibility(View.VISIBLE);
    }

   /* private void generateToken() {
        final ProgressDialog dialog = new ProgressDialog(GroupGiftDetail.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_BRAINTREE_CLIENT_TOKEN,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String res) {
                        try {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Log.v(TAG, "generateTokenRes=" + res);
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            String token = DefensiveClass.optString(json, Constants.TAG_TOKEN);
                            if (response.equalsIgnoreCase("true")) {
                                Cart cart = Cart.newBuilder()
                                        .setCurrencyCode("USD")
                                        .setTotalPrice(contributionAmt)
                                        .addLineItem(LineItem.newBuilder()
                                                .setCurrencyCode("USD")
                                                .setDescription("Description")
                                                .setQuantity("1")
                                                .setUnitPrice(contributionAmt)
                                                .setTotalPrice(contributionAmt)
                                                .build())
                                        .build();

                                GooglePaymentRequest googlePaymentRequest = new GooglePaymentRequest()
                                        .transactionInfo(TransactionInfo.newBuilder()
                                                .setTotalPrice(contributionAmt)
                                                .setCurrencyCode("USD")
                                                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                                .build())
                                        .emailRequired(false);

                                DropInRequest dropInRequest = new DropInRequest()
                                        .clientToken(token)
                                        .amount(contributionAmt)
                                        .googlePaymentRequest(googlePaymentRequest);
                                startActivityForResult(dropInRequest.getIntent(GroupGiftDetail.this), DROP_IN_REQUEST);
                            } else {
                                FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                            }
                        } catch (JSONException e) {
                            FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        } catch (Exception e) {
                            FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "generateTokenError: " + error.getMessage());
                FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }

        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                return map;
            }
        };

        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req, "Token");
    }*/

    private void paynowDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.pay_contribution_dialog);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        ImageView cancel = (ImageView) dialog.findViewById(R.id.cancel);
        TextView minContribution = (TextView) dialog.findViewById(R.id.minContribution);
        final EditText editText = (EditText) dialog.findViewById(R.id.editText);
        TextView payNow = (TextView) dialog.findViewById(R.id.payNow);

        String minAmount = getString(R.string.enter_your_contribute_amount_minimum_transaction) + " (" + dataMap.get(Constants.TAG_MINIMUM_CONTRIBUTION) + ")";
        minContribution.setText(minAmount);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        contributionAmt = "";
        payNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FantacyApplication.hideSoftKeyboard(GroupGiftDetail.this, editText);
                String payAmount = editText.getText().toString();
                if (payAmount.length() == 0) {
                    FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.please_enter_amount), Toast.LENGTH_SHORT);
                } else {
                    Float balanceAmount = pendingAmt - Float.parseFloat(payAmount);
                    if (Float.parseFloat(payAmount) < Float.parseFloat(dataMap.get(Constants.TAG_MINIMUM_CONTRIBUTION))) {
                        FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.minimum_contribute_amount) + " " + dataMap.get(Constants.TAG_MINIMUM_CONTRIBUTION), Toast.LENGTH_SHORT);
                    } else if ((Float.parseFloat(payAmount)) > pendingAmt) {
                        FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.contribution_more_than_pending), Toast.LENGTH_SHORT);
                    } else if (balanceAmount != 0 && balanceAmount < Float.parseFloat(dataMap.get(Constants.TAG_MINIMUM_CONTRIBUTION))) {
                        float lessAmt = pendingAmt - Float.parseFloat(dataMap.get(Constants.TAG_MINIMUM_CONTRIBUTION));
                        FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.contribution_equal_pending) + " " + lessAmt + getString(R.string.or_pay) + " " + pendingAmt, Toast.LENGTH_SHORT);
                    } else {
                        dialog.dismiss();
                        contributionAmt = editText.getText().toString();
                        Toast.makeText(GroupGiftDetail.this, "Under Construction !", Toast.LENGTH_SHORT).show();
                        //generateToken();
                    }
                }
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void payBraintree(final String nonce) {
        final ProgressDialog dialog = new ProgressDialog(GroupGiftDetail.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PAY_GROUPGIFT,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String res) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        try {
                            Log.v(TAG, "payBraintreeRes=" + res);
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {
                                FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.payment_success), Toast.LENGTH_SHORT);
                                finish();
                                Intent i = new Intent(GroupGiftDetail.this, GroupGiftDetail.class);
                                i.putExtra("giftId", getIntent().getExtras().getString("giftId"));
                                startActivity(i);
                            } else {
                                FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                        } catch (Exception e) {
                            e.printStackTrace();
                            FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }

        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("item_id", dataMap.get(Constants.TAG_ITEM_ID));
                map.put("gift_id", dataMap.get(Constants.TAG_GIFT_ID));
                map.put("amount", contributionAmt);
                map.put("currency_code", dataMap.get(Constants.TAG_CURRENCY_CODE));
                map.put("pay_nonce", nonce);
                Log.v(TAG, "payBraintreeParams=" + map);
                return map;
            }
        };
        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req, "Pay");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DROP_IN_REQUEST) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                String paymentMethodNonce = result.getPaymentMethodNonce().getNonce();
                Log.v(TAG, "paymentMethodNonce=" + paymentMethodNonce);
                // send paymentMethodNonce to your server
                payBraintree(paymentMethodNonce);
            } else if (resultCode != RESULT_CANCELED) {
                FantacyApplication.showToast(GroupGiftDetail.this, ((Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR)).getMessage(),
                        Toast.LENGTH_SHORT);
            } else {
                FantacyApplication.showToast(GroupGiftDetail.this, getString(R.string.payment_cancelled),
                        Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fragment_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return FragmentMainActivity.onNavOptionSelected(this, id);
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
            case R.id.cartBtn:
                Intent g = new Intent(Intent.ACTION_SEND);
                g.setType("text/plain");
                g.putExtra(Intent.EXTRA_TEXT, dataMap.get(Constants.TAG_SHARE_URL));
                startActivity(Intent.createChooser(g, "Share"));
                break;
            case R.id.contributeBtn:
                paynowDialog();
                break;
            case R.id.creatorImage:
                Intent c = new Intent(this, Profile.class);
                c.putExtra("userId", dataMap.get(Constants.TAG_CREATOR_ID));
                startActivity(c);
                break;
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
                    .inflate(R.layout.gift_contributors_list_item, parent, false);

            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.userName.setText(tempMap.get(Constants.TAG_USER_NAME));
            holder.price.setText(dataMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_AMOUNT_CONTRIBUTED));
            String img = tempMap.get(Constants.TAG_USER_IMAGE);
            if (!img.equals("")) {
                Picasso.get().load(img).into(holder.userImage);
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView userName, price;
            ImageView userImage;

            public MyViewHolder(View view) {
                super(view);

                userName = (TextView) view.findViewById(R.id.userName);
                userImage = (ImageView) view.findViewById(R.id.userImage);
                price = (TextView) view.findViewById(R.id.price);
            }
        }
    }
}
