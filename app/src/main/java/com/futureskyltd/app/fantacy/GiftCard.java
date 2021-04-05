package com.futureskyltd.app.fantacy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInResult;
import com.futureskyltd.app.external.CustomAutoCompleteTextView;
import com.futureskyltd.app.helper.FragmentChangeListener;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class GiftCard extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = GiftCard.class.getSimpleName();
    private static final int DROP_IN_REQUEST = 100;
    TextView giftText, giftDes, payNow, saveBtn;
    Display display;
    int textCount;
    String selectedValue = "", recipientId = "", currencySymbol, currencyCode;
    boolean isRecipientSelected = false;
    Spinner selectValue;
    EditText message;
    RelativeLayout introLay, progress, nullLay;
    CustomAutoCompleteTextView recipientName;
    SuggestAdapter suggestAdapter;
    ArrayList<String> valueList = new ArrayList<String>();
    ProgressDialog pdialog;
    String tempUserName = "";
    final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gift_card_layout, container, false);

        ((FragmentChangeListener) getActivity()).setNavSelectionItem(R.id.giftCard, "GiftCard");

        giftText = (TextView) view.findViewById(R.id.giftText);
        giftDes = (TextView) view.findViewById(R.id.giftDes);
        payNow = (TextView) view.findViewById(R.id.payNow);
        recipientName = (CustomAutoCompleteTextView) view.findViewById(R.id.recipientName);
        selectValue = (Spinner) view.findViewById(R.id.selectValue);
        message = (EditText) view.findViewById(R.id.message);
        introLay = (RelativeLayout) view.findViewById(R.id.introLay);
        progress = (RelativeLayout) view.findViewById(R.id.progress);
        nullLay = (RelativeLayout) view.findViewById(R.id.nullLay);
        saveBtn = (TextView) getActivity().findViewById(R.id.saveBtn);

        payNow.setOnClickListener(this);
        saveBtn.setOnClickListener(this);

        pdialog = new ProgressDialog(getActivity());
        pdialog.setMessage(getString(R.string.pleasewait));
        pdialog.setCancelable(false);
        pdialog.setCanceledOnTouchOutside(false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        display = getActivity().getWindowManager().getDefaultDisplay();

        recipientName.addTextChangedListener(new AutoSuggestTextWatcher(recipientName));
        recipientName.setOnItemClickListener(this);

        giftText.setText(getString(R.string.app_name) + " " + getString(R.string.gift_card));
        linkTextView(getActivity(), giftDes, getString(R.string.gift_card_des), getString(R.string.learn_more));

        FantacyApplication.setupUI(getActivity(), getView().findViewById(R.id.parentLay));

        valueList.add(getString(R.string.select_value));

        progress.setVisibility(View.VISIBLE);
        introLay.setVisibility(View.GONE);
        nullLay.setVisibility(View.GONE);
        getSettings();

    }

    private void getData() {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADMIN_DATAS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getDataRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);
                        JSONArray gift = result.optJSONArray(Constants.TAG_GIFT_AMOUNT);
                        if (gift != null) {
                            for (int i = 0; i < gift.length(); i++) {
                                JSONObject temp = gift.getJSONObject(i);

                                String value = DefensiveClass.optString(temp, Constants.TAG_VALUE);

                                valueList.add(currencySymbol + " " + value);
                            }
                        }
                    }
                    if (pdialog.isShowing()) {
                        pdialog.dismiss();
                    }

                    ArrayAdapter queryadapter = new ArrayAdapter<String>(getActivity(),
                            R.layout.spinner_item, R.id.textView, valueList);
                    queryadapter.setDropDownViewResource(R.layout.spinner_item);
                    selectValue.setAdapter(queryadapter);

                    selectValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view,
                                                   int position, long id) {
                            selectedValue = valueList.get(position);
                            if (selectedValue.equalsIgnoreCase(getString(R.string.select_value))) {
                                selectedValue = "";
                            }

                            try {
                                TextView textView = ((LinearLayout) parent.getChildAt(0)).findViewById(R.id.textView);
                                textView.setTextColor(getResources().getColor(R.color.textSecondary));
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {

                        }
                    });

                    progress.setVisibility(View.GONE);
                    introLay.setVisibility(View.VISIBLE);
                    nullLay.setVisibility(View.GONE);
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
                setErrorLayout();
                Log.e(TAG, "getDataError: " + error.getMessage());
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
        if (pdialog.isShowing()) {
            pdialog.dismiss();
        }
        progress.setVisibility(View.GONE);
        introLay.setVisibility(View.GONE);
        nullLay.setVisibility(View.VISIBLE);
    }

    private void getUserData(final String key) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

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

                           // String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                            String user_id = "289";
                            String full_name = DefensiveClass.optString(temp, Constants.TAG_FULL_NAME);
                            String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                            String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                            String email = DefensiveClass.optString(temp, Constants.TAG_EMAIL);

                            data.put(Constants.TAG_USER_ID, user_id);
                            data.put(Constants.TAG_FULL_NAME, full_name);
                            data.put(Constants.TAG_USER_NAME, user_name);
                            data.put(Constants.TAG_USER_IMAGE, user_image);
                            data.put(Constants.TAG_EMAIL, email);

                            if (!user_name.equals(GetSet.getUserName()))
                                aList.add(data);
                        }


                        if (aList.size() > 0) {
                            suggestAdapter = new SuggestAdapter(getActivity(), R.layout.atmention_layout, aList);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (recipientName != null) {
                                        recipientName.setAdapter(suggestAdapter);
                                        recipientName.showDropDown();
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
                Log.v(TAG, "getUserDataError" + map);
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

    /*private void generateToken() {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
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
                                        .setCurrencyCode(currencyCode)
                                        .setTotalPrice(selectedValue)
                                        .addLineItem(LineItem.newBuilder()
                                                .setCurrencyCode(currencyCode)
                                                .setDescription("GiftCard")
                                                .setQuantity("1")
                                                .setUnitPrice(selectedValue)
                                                .setTotalPrice(selectedValue)
                                                .build())
                                        .build();

                                GooglePaymentRequest googlePaymentRequest = new GooglePaymentRequest()
                                        .transactionInfo(TransactionInfo.newBuilder()
                                                .setTotalPrice(selectedValue)
                                                .setCurrencyCode("USD")
                                                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                                .build())
                                        .emailRequired(false);


                                DropInRequest dropInRequest = new DropInRequest()
                                        .clientToken(token)
                                        .amount(selectedValue)
                                        .googlePaymentRequest(googlePaymentRequest);

                                startActivityForResult(dropInRequest.getIntent(getActivity()), DROP_IN_REQUEST);
                            } else if (response.equalsIgnoreCase("error")) {
                                String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                                if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                                    Intent i = new Intent(getActivity(), PaymentStatus.class);
                                    i.putExtra("from", "block");
                                    startActivity(i);
                                } else {
                                    Intent i = new Intent(getActivity(), PaymentStatus.class);
                                    i.putExtra("from", "maintenance");
                                    startActivity(i);
                                }
                            } else {
                                FantacyApplication.showToast(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                            }
                        } catch (JSONException e) {
                            FantacyApplication.showToast(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            FantacyApplication.showToast(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        } catch (Exception e) {
                            FantacyApplication.showToast(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "generateTokenError: " + error.getMessage());
                FantacyApplication.showToast(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
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

    private void getSettings() {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_SETTINGS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getSettingsRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        getData();
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);
                        currencyCode = DefensiveClass.optString(result, Constants.TAG_CURRENCY_CODE);
                        currencySymbol = DefensiveClass.optString(result, Constants.TAG_CURRENCY_SYMBOL);
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           /* Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                           /* Intent i = new Intent(getActivity(), PaymentStatus.class);
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

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (pdialog.isShowing()) {
                    pdialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", "289");
                Log.v(TAG, "getSettingsRes=" + map);
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
        pdialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void createGiftCard(final String nonce) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CREATE_GIFTCARD, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    Log.v(TAG, "createGiftCardRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    String msg = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                    if (status.equalsIgnoreCase("true")) {
                        selectValue.setSelection(0);
                        recipientName.setText("");
                        message.setText("");
                        Intent i = new Intent(getActivity(), GiftHistory.class);
                        startActivity(i);

                        FantacyApplication.showToast(getActivity(), getString(R.string.payment_success), Toast.LENGTH_SHORT);
                    } else if (msg.equals("Gift card can not be send to your own")) {
                        FantacyApplication.showToast(getActivity(), getString(R.string.gift_card_for_own), Toast.LENGTH_SHORT);
                    } else {
                        FantacyApplication.showToast(getActivity(), getString(R.string.payment_not_success), Toast.LENGTH_SHORT);
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
                Log.e(TAG, "createGiftCardError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", "289");
                map.put("recipient_id", recipientId);
                map.put("price", selectedValue.replace(currencySymbol + " ", ""));
                map.put("message", message.getText().toString().trim());
                map.put("pay_nonce", nonce);
                Log.v(TAG, "createGiftCardParams=" + map);
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

    private void linkTextView(final Context context, TextView view, final String desc, final String appendTxt) {

        SpannableStringBuilder spanTxt = new SpannableStringBuilder(desc + " ");
        int count = spanTxt.length();
        spanTxt.append(appendTxt);
        spanTxt.setSpan(new MyClickableSpan(context) {
            @Override
            public void onClick(View widget) {
                showHelpDialog();
            }
        }, count, spanTxt.length(), 0);

        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private void showHelpDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.group_gift_help_popup);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView desc = (TextView) dialog.findViewById(R.id.description);
        TextView ok = (TextView) dialog.findViewById(R.id.ok);

        title.setText(getString(R.string.how_to_use_giftcard));
        desc.setText(getString(R.string.giftcard_steps));

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DROP_IN_REQUEST) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                String paymentMethodNonce = result.getPaymentMethodNonce().getNonce();
                Log.v(TAG, "paymentMethodNonce=" + paymentMethodNonce);
                // send paymentMethodNonce to your server
                createGiftCard(paymentMethodNonce);
            } else if (resultCode != RESULT_CANCELED) {
                FantacyApplication.showToast(getActivity(), ((Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR)).getMessage(),
                        Toast.LENGTH_SHORT);
            } else {
                FantacyApplication.showToast(getActivity(), getString(R.string.payment_cancelled),
                        Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FantacyApplication.hideSoftKeyboard(getActivity(), recipientName);
        HashMap<String, String> map = (HashMap<String, String>) parent.getAdapter().getItem(position);
        isRecipientSelected = true;
        recipientName.setText(map.get(Constants.TAG_USER_NAME));
        tempUserName = map.get(Constants.TAG_USER_NAME);
        recipientId = map.get(Constants.TAG_USER_ID);
        recipientName.clearFocus();
        recipientName.dismissDropDown();
        Log.v("dismiss", "dismiss");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.payNow:
                if (selectedValue.equals("") || recipientName.getText().toString().trim().length() == 0
                        || message.getText().toString().trim().length() == 0) {
                    FantacyApplication.showToast(getActivity(), getString(R.string.please_fill_all), Toast.LENGTH_SHORT);
                } else if (recipientName.getText().toString().matches(emailPattern)) {
                    FantacyApplication.showToast(getActivity(), getString(R.string.email_error), Toast.LENGTH_SHORT);
                } else if (!recipientName.getText().toString().equals(tempUserName)) {
                    FantacyApplication.showToast(getActivity(), getString(R.string.please_select_valid_user), Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(getContext(), "Under Construction !", Toast.LENGTH_SHORT).show();
                    //generateToken();
                }
                break;
            case R.id.saveBtn:
                Intent i = new Intent(getActivity(), GiftHistory.class);
                startActivity(i);
                break;
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
            if (temp.length() > 0) {
                textCount = temp.length();
                //if (!Patterns.EMAIL_ADDRESS.matcher(temp).matches())
                if (!isRecipientSelected)
                    getUserData(temp);
            } else {
                tempUserName = "";
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            textCount = s.toString().length();
            isRecipientSelected = false;
        }

    }


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

    class MyClickableSpan extends ClickableSpan {

        Context context;

        public MyClickableSpan(Context context) {
            super();
            this.context = context;
        }

        @Override
        public void onClick(View tv) {
            // FantacyApplication.showToast(context, name + ": " + id, Toast.LENGTH_SHORT);
        }

        @Override
        public void updateDrawState(TextPaint ds) {// override updateDrawState
            ds.setUnderlineText(false); // set to false to remove underline
            ds.setColor(ContextCompat.getColor(context, R.color.light_yellow));
        }
    }
}
