package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;



public class PaymentActivity extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    private static final int DROP_IN_REQUEST = 100;
    ImageView backBtn, codIcon, instantIcon, creditTick, couponTick, giftTick;
    TextView title, cpnApply, couponApply, couponCancel, appliedCoupon, couponEdit, couponDelete, addGift,
            giftCancel, giftApply, appliedGift, giftRemove, useCredit, creditCancel, creditApply, appliedCredit,
            creditEdit, creditRemove, checkout, discounttext, itemPrice, shippingPrice, taxPrice, discountPrice,
            grandTotal, couponapplied, availablecredit, maxcredit, txtPrice;
    EditText couponEnter, giftEnter, creditEnter;
    String grand_total;
    RelativeLayout couponEnterLay, couponAppliedLay, giftEnterLay, giftAppliedLay, creditEnterLay, creditAppliedLay, nullLay;
    LinearLayout codLay, discountLay, instantLay, mainLay, paymentLay;
    HashMap<String, String> paymentData = new HashMap<>();
    ProgressDialog dialog;
    String shippingId = "0", itemId = "0", couponCode = "", giftCard = "", creditApplied = "", paymentType = "", size = "", quantity = "";
    int totalItems = 0;
    private SharedPreferences preferences;
    private String accesstoken, customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_main_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);
        customerId = preferences.getString("customer_id", null);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        couponEnter = (EditText) findViewById(R.id.couponEnter);
        cpnApply = (TextView) findViewById(R.id.cpnApply);
        couponApply = (TextView) findViewById(R.id.couponApply);
        couponCancel = (TextView) findViewById(R.id.couponCancel);
        couponEnterLay = (RelativeLayout) findViewById(R.id.couponEnterLay);
        couponAppliedLay = (RelativeLayout) findViewById(R.id.couponAppliedLay);
        appliedCoupon = (TextView) findViewById(R.id.appliedCoupon);
        couponEdit = (TextView) findViewById(R.id.couponEdit);
        couponDelete = (TextView) findViewById(R.id.couponDelete);
        addGift = (TextView) findViewById(R.id.addGift);
        couponTick = (ImageView) findViewById(R.id.couponTick);
        giftTick = (ImageView) findViewById(R.id.giftTick);
        creditTick = (ImageView) findViewById(R.id.creditTick);
        giftCancel = (TextView) findViewById(R.id.giftCancel);
        giftApply = (TextView) findViewById(R.id.giftApply);
        couponapplied = (TextView) findViewById(R.id.couponapplied);
        appliedGift = (TextView) findViewById(R.id.appliedGift);
        giftRemove = (TextView) findViewById(R.id.giftRemove);
        giftEnter = (EditText) findViewById(R.id.giftEnter);
        giftEnterLay = (RelativeLayout) findViewById(R.id.giftEnterLay);
        giftAppliedLay = (RelativeLayout) findViewById(R.id.giftAppliedLay);
        useCredit = (TextView) findViewById(R.id.useCredit);
        creditCancel = (TextView) findViewById(R.id.creditCancel);
        creditApply = (TextView) findViewById(R.id.creditApply);
        appliedCredit = (TextView) findViewById(R.id.appliedCredit);
        creditEdit = (TextView) findViewById(R.id.creditEdit);
        creditRemove = (TextView) findViewById(R.id.creditRemove);
        creditEnter = (EditText) findViewById(R.id.creditEnter);
        creditEnterLay = (RelativeLayout) findViewById(R.id.creditEnterLay);
        creditAppliedLay = (RelativeLayout) findViewById(R.id.creditAppliedLay);
        codIcon = (ImageView) findViewById(R.id.codIcon);
        instantIcon = (ImageView) findViewById(R.id.instantIcon);
        codLay = (LinearLayout) findViewById(R.id.codLay);
        discountLay = (LinearLayout) findViewById(R.id.discountLay);
        instantLay = (LinearLayout) findViewById(R.id.instantLay);
        checkout = (TextView) findViewById(R.id.checkout);
        discounttext = (TextView) findViewById(R.id.discounttext);
        itemPrice = (TextView) findViewById(R.id.itemPrice);
        shippingPrice = (TextView) findViewById(R.id.shippingPrice);
        taxPrice = (TextView) findViewById(R.id.taxPrice);
        discountPrice = (TextView) findViewById(R.id.discountPrice);
        grandTotal = (TextView) findViewById(R.id.grandTotal);
        availablecredit = (TextView) findViewById(R.id.availablecredit);
        maxcredit = (TextView) findViewById(R.id.maxcredit);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        mainLay = (LinearLayout) findViewById(R.id.mainLay);
        paymentLay = (LinearLayout) findViewById(R.id.paymentLay);
        txtPrice = (TextView) findViewById(R.id.txtPrice);

        title.setText(getString(R.string.payment));

        dialog = new ProgressDialog(PaymentActivity.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        cpnApply.setOnClickListener(this);
        couponCancel.setOnClickListener(this);
        couponApply.setOnClickListener(this);
        couponEdit.setOnClickListener(this);
        couponDelete.setOnClickListener(this);
        addGift.setOnClickListener(this);
        giftCancel.setOnClickListener(this);
        giftApply.setOnClickListener(this);
        giftRemove.setOnClickListener(this);
        useCredit.setOnClickListener(this);
        creditCancel.setOnClickListener(this);
        creditApply.setOnClickListener(this);
        creditEdit.setOnClickListener(this);
        creditRemove.setOnClickListener(this);
        codLay.setOnClickListener(this);
        instantLay.setOnClickListener(this);
        checkout.setOnClickListener(this);

        getPayment("");

        itemId = (String) getIntent().getExtras().get("itemId");
        shippingId = (String) getIntent().getExtras().get("shippingId");
        totalItems = (int) getIntent().getExtras().get("totalItems");
        if (!itemId.equals("0")) {
            size = (String) getIntent().getExtras().get("size");
            quantity = (String) getIntent().getExtras().get("quantity");
        }


        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }


    private void getPayment(final String type) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PAYMENT_PROCESS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getPaymentRes=" + res);
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);

                        String item_count = DefensiveClass.optString(result, Constants.TAG_ITEM_COUNT);
                        String total_credit = DefensiveClass.optString(result, Constants.TAG_TOTAL_CREDIT);
                        String max_credit_usable = DefensiveClass.optString(result, Constants.TAG_MAX_CREDIT_USABLE);
                        String credit_used = DefensiveClass.optString(result, Constants.TAG_CREDIT_USED);
                        String gift_amount = DefensiveClass.optString(result, Constants.TAG_GIFT_AMOUNT);
                        String currency = DefensiveClass.optString(result, Constants.TAG_CURRENCY);
                        String currency_code = DefensiveClass.optString(result, Constants.TAG_CURRENCY_CODE);
                        String coupon_discount = DefensiveClass.optString(result, Constants.TAG_COUPON_DISCOUNT);
                        String item_total = DefensiveClass.optString(result, Constants.TAG_ITEM_TOTAL);
                        String shipping_price = DefensiveClass.optString(result, Constants.TAG_SHIPPING_PRICE);
                        String tax = DefensiveClass.optString(result, Constants.TAG_TAX);
                        grand_total = DefensiveClass.optString(result, Constants.TAG_GRAND_TOTAL);
                        String cod = DefensiveClass.optString(result, Constants.TAG_COD);
                        String shippingbyitem = DefensiveClass.optString(result, Constants.TAG_SHIPPING_BY_ITEM);
                        String shippingbyseller = DefensiveClass.optString(result, Constants.TAG_SHIPPING_BY_SELLER);
                        String taxbyitem = DefensiveClass.optString(result, Constants.TAG_TAX_BY_ITEM);

                        paymentData.put(Constants.TAG_ITEM_COUNT, item_count);
                        paymentData.put(Constants.TAG_TOTAL_CREDIT, total_credit);
                        paymentData.put(Constants.TAG_MAX_CREDIT_USABLE, max_credit_usable);
                        paymentData.put(Constants.TAG_CREDIT_USED, credit_used);
                        paymentData.put(Constants.TAG_GIFT_AMOUNT, gift_amount);
                        paymentData.put(Constants.TAG_CURRENCY, currency);
                        paymentData.put(Constants.TAG_CURRENCY_CODE, currency_code);
                        paymentData.put(Constants.TAG_COUPON_DISCOUNT, coupon_discount);
                        paymentData.put(Constants.TAG_COD, cod);
                        paymentData.put(Constants.TAG_SHIPPING_BY_ITEM, shippingbyitem);
                        paymentData.put(Constants.TAG_SHIPPING_BY_SELLER, shippingbyseller);
                        paymentData.put(Constants.TAG_TAX_BY_ITEM, taxbyitem);

                        if (coupon_discount.equals(""))
                            paymentData.put(Constants.TAG_COUPON_DISCOUNT, "0");

                        if (credit_used.equals(""))
                            paymentData.put(Constants.TAG_CREDIT_USED, "0");

                        if (gift_amount.equals(""))
                            paymentData.put(Constants.TAG_GIFT_AMOUNT, "0");

                        paymentData.put(Constants.TAG_ITEM_TOTAL, item_total);
                        paymentData.put(Constants.TAG_SHIPPING_PRICE, shipping_price);
                        paymentData.put(Constants.TAG_TAX, tax);
                        paymentData.put(Constants.TAG_GRAND_TOTAL, grand_total);

                    } else {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (!couponCode.equals("")) {
                            couponCode = "";
                            couponapplied.setText(message);
                            couponTick.setImageDrawable(ContextCompat.getDrawable(PaymentActivity.this, R.drawable.cod_no));
                            couponapplied.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.red));
                        } else if (!giftCard.equals("")) {
                            giftCard = "";
                            giftAppliedLay.setVisibility(View.VISIBLE);
                            appliedGift.setText(message);
                            giftTick.setImageDrawable(ContextCompat.getDrawable(PaymentActivity.this, R.drawable.cod_no));
                            appliedGift.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.red));
                        } else if (!creditApplied.equals("")) {
                            creditApplied = "";
                            appliedCredit.setText(message);
                            creditTick.setImageDrawable(ContextCompat.getDrawable(PaymentActivity.this, R.drawable.cod_no));
                            appliedCredit.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.red));
                        }
                    }
                    setPaymentData(type);

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
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getPaymentError: " + error.getMessage());
                setErrorLayout();
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
                map.put("shipping_id", shippingId);
                if (!couponCode.equals(""))
                    map.put("coupon_code", couponCode);

                if (!giftCard.equals(""))
                    map.put("gift_no", giftCard);

                if (!creditApplied.equals(""))
                    map.put("credit_amount", creditApplied);

                map.put("size", size);
                map.put("quantity", quantity);

                Log.v(TAG, "getPaymentParams=" + map);
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

    private void setErrorLayout() {
        mainLay.setVisibility(View.GONE);
        checkout.setVisibility(View.GONE);
        nullLay.setVisibility(View.VISIBLE);

    }

    private void setPaymentData(String c_type) {
        if (totalItems > 1) {
            txtPrice.setText(getString(R.string.price) + " (" + totalItems + " " + getString(R.string.items) + ")");
        } else if (totalItems > 0) {
            txtPrice.setText(getString(R.string.price) + " (" + totalItems + " " + getString(R.string.item) + ")");
        }
        itemPrice.setText(paymentData.get(Constants.TAG_CURRENCY) + " " + paymentData.get(Constants.TAG_ITEM_TOTAL));
        if (paymentData.get(Constants.TAG_SHIPPING_PRICE).equals("0") || paymentData.get(Constants.TAG_SHIPPING_PRICE).equals(""))
            shippingPrice.setText(getString(R.string.free_text));
        else
            shippingPrice.setText(paymentData.get(Constants.TAG_CURRENCY) + " " + paymentData.get(Constants.TAG_SHIPPING_PRICE));

        taxPrice.setText(paymentData.get(Constants.TAG_CURRENCY) + " " + paymentData.get(Constants.TAG_TAX));
        grandTotal.setText(paymentData.get(Constants.TAG_CURRENCY) + " " + paymentData.get(Constants.TAG_GRAND_TOTAL));
        paymentLay.setVisibility(View.VISIBLE);

        boolean codAllowed = true;

        if (!paymentData.get(Constants.TAG_GIFT_AMOUNT).equals("0")) {
            discountLay.setVisibility(View.VISIBLE);
            double giftAmount = Double.parseDouble(paymentData.get(Constants.TAG_GIFT_AMOUNT));
            double shippingAmount;
            if (paymentData.get(Constants.TAG_SHIPPING_PRICE).equals("0") || paymentData.get(Constants.TAG_SHIPPING_PRICE).equals(""))
                shippingAmount = 0;
            else
                shippingAmount = Double.parseDouble(paymentData.get(Constants.TAG_SHIPPING_PRICE));
            double itemTotal = Double.parseDouble(paymentData.get(Constants.TAG_ITEM_TOTAL)) + shippingAmount;
            itemTotal = itemTotal + Double.parseDouble(paymentData.get(Constants.TAG_TAX));
            if (giftAmount > itemTotal) {
                discountPrice.setText(paymentData.get(Constants.TAG_CURRENCY) + " " + itemTotal);
            } else {
                discountPrice.setText(paymentData.get(Constants.TAG_CURRENCY) + " " + giftAmount);
            }

            if (Float.parseFloat(paymentData.get(Constants.TAG_GRAND_TOTAL)) == 0) {
                paymentType = "giftcard";
                paymentLay.setVisibility(View.GONE);
            } else {
                codAllowed = false;
                codLay.setVisibility(View.GONE);
            }
            giftTick.setImageDrawable(ContextCompat.getDrawable(PaymentActivity.this, R.drawable.tick_color));
            appliedGift.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.colorPrimary));
            appliedGift.setText(giftEnter.getText().toString() + " " + getString(R.string.voucher_applied));
            giftAppliedLay.setVisibility(View.VISIBLE);
            cpnApply.setEnabled(false);
            useCredit.setEnabled(false);
            cpnApply.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.textLight));
            useCredit.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.textLight));


            if (creditEnterLay.getVisibility() == View.VISIBLE) {
                creditEnterLay.setVisibility(View.GONE);
                creditEnter.setText("");
            }
            useCredit.setVisibility(View.VISIBLE);

            if (couponEnterLay.getVisibility() == View.VISIBLE) {
                couponEnterLay.setVisibility(View.GONE);
                couponEnter.setText("");
            }
            cpnApply.setVisibility(View.VISIBLE);

        } else if (!paymentData.get(Constants.TAG_CREDIT_USED).equals("0")) {
            discountLay.setVisibility(View.VISIBLE);
            discounttext.setText(getString(R.string.credit));
            discountPrice.setText("- " + paymentData.get(Constants.TAG_CURRENCY) + " " + paymentData.get(Constants.TAG_CREDIT_USED));

            if (Float.parseFloat(paymentData.get(Constants.TAG_GRAND_TOTAL)) == 0) {
                paymentType = "credit";
                paymentLay.setVisibility(View.GONE);
            } else {
                codAllowed = false;
                codLay.setVisibility(View.GONE);
            }
            creditTick.setImageDrawable(ContextCompat.getDrawable(PaymentActivity.this, R.drawable.tick_color));
            appliedCredit.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.colorPrimary));
            appliedCredit.setText(paymentData.get(Constants.TAG_CURRENCY) + " " + creditEnter.getText().toString() + " " + getString(R.string.used_from_credit));
            cpnApply.setEnabled(false);
            addGift.setEnabled(false);
            cpnApply.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.textLight));
            addGift.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.textLight));

            if (couponEnterLay.getVisibility() == View.VISIBLE) {
                couponEnterLay.setVisibility(View.GONE);
                couponEnter.setText("");
            }
            cpnApply.setVisibility(View.VISIBLE);

            if (giftEnterLay.getVisibility() == View.VISIBLE) {
                giftEnterLay.setVisibility(View.GONE);
                giftEnter.setText("");
            }
            addGift.setVisibility(View.VISIBLE);
        } else if (!paymentData.get(Constants.TAG_COUPON_DISCOUNT).equals("0")) {
            codAllowed = false;
            discountLay.setVisibility(View.VISIBLE);
            discounttext.setText(getString(R.string.discount));
            discountPrice.setText("- " + paymentData.get(Constants.TAG_CURRENCY) + " " + paymentData.get(Constants.TAG_COUPON_DISCOUNT));


            addGift.setEnabled(false);
            useCredit.setEnabled(false);
            couponTick.setImageDrawable(ContextCompat.getDrawable(PaymentActivity.this, R.drawable.tick_color));
            couponapplied.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.colorPrimary));
            couponapplied.setText(getString(R.string.coupon_applied));
            addGift.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.textLight));
            useCredit.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.textLight));

            if (creditEnterLay.getVisibility() == View.VISIBLE) {
                creditEnterLay.setVisibility(View.GONE);
                creditEnter.setText("");
            }
            useCredit.setVisibility(View.VISIBLE);

            if (giftEnterLay.getVisibility() == View.VISIBLE) {
                giftEnterLay.setVisibility(View.GONE);
                giftEnter.setText("");
            }
            addGift.setVisibility(View.VISIBLE);
        } else {
            cpnApply.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.colorPrimary));
            addGift.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.colorPrimary));
            useCredit.setTextColor(ContextCompat.getColor(PaymentActivity.this, R.color.colorPrimary));
            cpnApply.setEnabled(true);
            addGift.setEnabled(true);
            useCredit.setEnabled(true);
            discountLay.setVisibility(View.GONE);
        }

        availablecredit.setText(getString(R.string.available) + " : " + paymentData.get(Constants.TAG_CURRENCY) + " " + paymentData.get(Constants.TAG_TOTAL_CREDIT));

        if (paymentData.get(Constants.TAG_COD).equalsIgnoreCase("enable") && codAllowed) {
            codLay.setVisibility(View.VISIBLE);
        } else {
            codLay.setVisibility(View.GONE);
        }
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (paymentData.get(Constants.TAG_COD).equalsIgnoreCase("enable") && c_type.equalsIgnoreCase("coupon"))
            codLay.setVisibility(View.VISIBLE);
    }

  /*  private void generateToken() {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_BRAINTREE_CLIENT_TOKEN,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String res) {
                        try {

                            Log.v(TAG, "generateTokenRes=" + res);
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            final String token = DefensiveClass.optString(json, Constants.TAG_TOKEN);
                            if (response.equalsIgnoreCase("true")) {
                                DropInResult.fetchDropInResult(PaymentActivity.this, token, new DropInResult.DropInResultListener() {
                                    @Override
                                    public void onError(Exception exception) {
                                        // an error occurred
                                        Log.e(TAG, "generateTokenError: " + exception.getMessage());
                                        if (dialog != null && dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onResult(DropInResult result) {
                                        if (dialog != null && dialog.isShowing()) {
                                            dialog.dismiss();
                                        }
                                        Cart cart = Cart.newBuilder()
                                                .setCurrencyCode(paymentData.get(Constants.TAG_CURRENCY_CODE))
                                                .setTotalPrice(paymentData.get(Constants.TAG_GRAND_TOTAL))
                                                .addLineItem(LineItem.newBuilder()
                                                        .setCurrencyCode(paymentData.get(Constants.TAG_CURRENCY_CODE))
                                                        .setDescription("Description")
                                                        .setQuantity("1")
                                                        .setUnitPrice(paymentData.get(Constants.TAG_GRAND_TOTAL))
                                                        .setTotalPrice(paymentData.get(Constants.TAG_GRAND_TOTAL))
                                                        .build())
                                                .build();

                                        GooglePaymentRequest googlePaymentRequest = new GooglePaymentRequest()
                                                .transactionInfo(TransactionInfo.newBuilder()
                                                        .setTotalPrice(paymentData.get(Constants.TAG_GRAND_TOTAL))
                                                        .setCurrencyCode("USD")
                                                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                                        .build())
                                                .emailRequired(false);

                                        Log.e("grand_totalpay","-"+paymentData.get(Constants.TAG_GRAND_TOTAL));

                                        DropInRequest dropInRequest = new DropInRequest()
                                                .tokenizationKey(token)
                                                .amount(paymentData.get(Constants.TAG_GRAND_TOTAL))
                                                .googlePaymentRequest(googlePaymentRequest);

                                        startActivityForResult(dropInRequest.getIntent(PaymentActivity.this), DROP_IN_REQUEST);
                                    }
                                });
                            } else if (response.equalsIgnoreCase("error")) {
                                String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                                if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                                    Intent i = new Intent(PaymentActivity.this, PaymentStatus.class);
                                    i.putExtra("from", "block");
                                    startActivity(i);
                                } else {
                                    Intent i = new Intent(PaymentActivity.this, PaymentStatus.class);
                                    i.putExtra("from", "maintenance");
                                    startActivity(i);
                                }
                            } else {
                                FantacyApplication.showToast(PaymentActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                            }
                        } catch (JSONException e) {
                            FantacyApplication.showToast(PaymentActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            FantacyApplication.showToast(PaymentActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        } catch (Exception e) {
                            FantacyApplication.showToast(PaymentActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "generateTokenError: " + error.getMessage());
                FantacyApplication.showToast(PaymentActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }

        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                Log.i(TAG, "generateTokenParams: " + map);
                return map;
            }
        };

        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req, "Token");
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DROP_IN_REQUEST) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                String paymentMethodNonce = result.getPaymentMethodNonce().getNonce();
                Log.v(TAG, "paymentMethodNonce=" + paymentMethodNonce);
                // send paymentMethodNonce to your server
                createOrder(paymentMethodNonce);
            } else if (resultCode != RESULT_CANCELED) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                FantacyApplication.showToast(PaymentActivity.this, ((Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR)).getMessage(),
                        Toast.LENGTH_SHORT);
            } else {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                FantacyApplication.showToast(PaymentActivity.this, getString(R.string.payment_cancelled),
                        Toast.LENGTH_SHORT);
            }
        }
    }

    private void createOrder(final String nonce) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CREATE_ORDER, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    Log.v(TAG, "createOrderRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        /*Intent i = new Intent(PaymentActivity.this, PaymentStatus.class);
                        i.putExtra("from", "payment");
                        startActivity(i);
                        finish();*/
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
                Log.e(TAG, "createOrderError: " + error.getMessage());
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
                map.put("shipping_id", shippingId);
                map.put("payment_type", paymentType);
                map.put("grand_total", paymentData.get(Constants.TAG_GRAND_TOTAL));
                map.put("shipping_price", paymentData.get(Constants.TAG_SHIPPING_PRICE));
                map.put("tax", paymentData.get(Constants.TAG_TAX));
                map.put("item_total", paymentData.get(Constants.TAG_ITEM_TOTAL));

                map.put("shippingbyitem", paymentData.get(Constants.TAG_SHIPPING_BY_ITEM));
                map.put("shippingbyseller", paymentData.get(Constants.TAG_SHIPPING_BY_SELLER));
                map.put("taxbyitem", paymentData.get(Constants.TAG_TAX_BY_ITEM));

                map.put("coupon_discount", paymentData.get(Constants.TAG_COUPON_DISCOUNT));
                if (!couponCode.equals(""))
                    map.put("coupon_code", couponCode);

                if (!giftCard.equals(""))
                    map.put("gift_id", giftCard);

                if (!creditApplied.equals(""))
                    map.put("credit_used", paymentData.get(Constants.TAG_CREDIT_USED));
                if (!giftCard.equals(""))
                    map.put("gift_amount", paymentData.get(Constants.TAG_GIFT_AMOUNT));

                if (!itemId.equals("0")) {
                    map.put("size", size);
                    map.put("quantity", quantity);
                }
                map.put("pay_nonce", nonce);

                Log.d(TAG, "getParams: "+ map.toString());
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            case R.id.cpnApply:
                cpnApply.setVisibility(View.INVISIBLE);
                couponEnterLay.setVisibility(View.VISIBLE);
                break;
            case R.id.couponCancel:
                cpnApply.setVisibility(View.VISIBLE);
                couponEnterLay.setVisibility(View.GONE);
                break;
            case R.id.couponApply:
                if (couponEnter.getText().toString().isEmpty()) {
                    couponEnter.setError(getString(R.string.coupon_should_not_empty));
                } else {
                    couponCode = couponEnter.getText().toString();
                    giftCard = "";
                    creditApplied = "";
                    getPayment("coupon");
                    couponEnterLay.setVisibility(View.GONE);
                    couponAppliedLay.setVisibility(View.VISIBLE);
                    appliedCoupon.setText(couponEnter.getText().toString());
                }
                break;
            case R.id.couponEdit:
                couponAppliedLay.setVisibility(View.GONE);
                couponEnterLay.setVisibility(View.VISIBLE);
                break;
            case R.id.couponDelete:
                couponCode = "";
                couponEnter.setText("");
                appliedCoupon.setText("");
                getPayment("coupon");
                couponAppliedLay.setVisibility(View.GONE);
                couponEnterLay.setVisibility(View.GONE);
                cpnApply.setVisibility(View.VISIBLE);
                break;
            case R.id.addGift:
                addGift.setVisibility(View.INVISIBLE);
                giftEnterLay.setVisibility(View.VISIBLE);
                break;
            case R.id.giftCancel:
                addGift.setVisibility(View.VISIBLE);
                giftEnterLay.setVisibility(View.GONE);
                break;
            case R.id.giftApply:
                if (giftEnter.getText().toString().isEmpty()) {
                    giftEnter.setError(getString(R.string.voucher_should_not_empty));
                } else {
                    couponCode = "";
                    giftCard = giftEnter.getText().toString();
                    creditApplied = "";
                    giftEnterLay.setVisibility(View.GONE);
                    getPayment("");
                }
                break;
            case R.id.giftRemove:
                giftCard = "";
                giftEnter.setText("");
                getPayment("");
                addGift.setVisibility(View.VISIBLE);
                giftAppliedLay.setVisibility(View.GONE);
                break;
            case R.id.useCredit:
                useCredit.setVisibility(View.INVISIBLE);
                creditEnterLay.setVisibility(View.VISIBLE);
                break;
            case R.id.creditCancel:

                creditEnterLay.setVisibility(View.GONE);
                creditEnter.setText("");
                if (appliedCredit.getText().toString().contains(getString(R.string.used_from_credit))) {
                    useCredit.setVisibility(View.GONE);
                    creditAppliedLay.setVisibility(View.VISIBLE);
                } else {
                    useCredit.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.creditApply:
                FantacyApplication.hideSoftKeyboard(PaymentActivity.this, creditApply);
                float grandCredit = Float.parseFloat(paymentData.get(Constants.TAG_GRAND_TOTAL));
                if (!paymentData.get(Constants.TAG_CREDIT_USED).equals("0")) {
                    grandCredit = grandCredit + Float.parseFloat(paymentData.get(Constants.TAG_CREDIT_USED));
                }
                if (creditEnter.getText().toString().isEmpty()) {
                    creditEnter.setError(getString(R.string.amount_should_not_empty));
                } else if (paymentData.get(Constants.TAG_TOTAL_CREDIT).equals("0")) {
                    FantacyApplication.defaultSnack(PaymentActivity.this.findViewById(R.id.parentLay), getString(R.string.credit_zero), "alert");
                } else if (Float.parseFloat(creditEnter.getText().toString()) > Float.parseFloat(paymentData.get(Constants.TAG_TOTAL_CREDIT))) {
                    FantacyApplication.defaultSnack(PaymentActivity.this.findViewById(R.id.parentLay), getString(R.string.credit_low), "alert");
                } else if (Float.parseFloat(creditEnter.getText().toString()) > grandCredit) {
                    FantacyApplication.defaultSnack(PaymentActivity.this.findViewById(R.id.parentLay), getString(R.string.credit_equal_less), "alert");
                } else {
                    giftCard = "";
                    couponCode = "";
                    creditApplied = creditEnter.getText().toString();
                    getPayment("");
                    creditEnterLay.setVisibility(View.GONE);
                    creditAppliedLay.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.creditEdit:
                creditAppliedLay.setVisibility(View.GONE);
                creditEnterLay.setVisibility(View.VISIBLE);
                break;
            case R.id.creditRemove:
                creditApplied = "";
                creditEnter.setText("");
                appliedCredit.setText("");
                getPayment("");
                creditAppliedLay.setVisibility(View.GONE);
                creditEnterLay.setVisibility(View.GONE);
                useCredit.setVisibility(View.VISIBLE);
                break;
            case R.id.codLay:
                paymentType = "cod";
                codIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.radio_select));
                instantIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.radio_unselect));
                break;
            case R.id.instantLay:
                paymentType = "braintree";
                codIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.radio_unselect));
                instantIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.radio_select));
                break;
            case R.id.checkout:
                switch (paymentType) {
                    case "":
                        FantacyApplication.defaultSnack(PaymentActivity.this.findViewById(R.id.parentLay), getString(R.string.select_payment), "alert");
                        break;
                    case "braintree":
                        checkOrder("braintree");
                        break;
                    default:
                        checkOrder("");
                        break;
                }
                break;
        }
    }

    private void checkOrder(final String paymentType) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PAYMENT_PROCESS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "checkOrderRes=" + res);
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        if (paymentType.equals("braintree")) {

                            Log.d(TAG, "onResponse: "+ grand_total);
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            finish();
                            Intent intent = new Intent(PaymentActivity.this, OrderPaymentActivity.class);
                            intent.putExtra("amount", grand_total);
                            startActivity(intent);
                            //generateToken();
//                            createOrder("");
                        } else {
                            createOrder("");
                        }
                    } else {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals("Requested Quantity Not Available")) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Intent intent = new Intent(getApplicationContext(), CartActivity.class);
                            intent.putExtra("itemId", "0");
                            intent.putExtra("shippingId", shippingId);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
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
                Log.e(TAG, "checkOrderError: " + error.getMessage());
                setErrorLayout();
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
                map.put("shipping_id", shippingId);
                if (!couponCode.equals(""))
                    map.put("coupon_code", couponCode);

                if (!giftCard.equals(""))
                    map.put("gift_no", giftCard);

                if (!creditApplied.equals(""))
                    map.put("credit_amount", creditApplied);

                map.put("size", size);
                map.put("quantity", quantity);

                Log.v(TAG, "checkOrderParams=" + map.toString());
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
}
