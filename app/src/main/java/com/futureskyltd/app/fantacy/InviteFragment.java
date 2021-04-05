package com.futureskyltd.app.fantacy;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.helper.FragmentChangeListener;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 17/7/17.
 */

public class InviteFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = InviteFragment.class.getSimpleName();
    TextView creditBalance, referralUrl, inviteDes, refer, saveBtn;
    Display display;
    String credit_per_invite = "", referral_url = "";
    RelativeLayout progress, nullLay, introLay;
    ArrayList<HashMap<String, String>> inviteAry = new ArrayList<HashMap<String, String>>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.invite_main_layout, container, false);

        ((FragmentChangeListener) getActivity()).setNavSelectionItem(R.id.giftCard, "Invite");

        creditBalance = (TextView) view.findViewById(R.id.creditBalance);
        referralUrl = (TextView) view.findViewById(R.id.referralUrl);
        inviteDes = (TextView) view.findViewById(R.id.inviteDes);
        refer = (TextView) view.findViewById(R.id.refer);
        saveBtn = (TextView) getActivity().findViewById(R.id.saveBtn);
        progress = (RelativeLayout) view.findViewById(R.id.progress);
        nullLay = (RelativeLayout) view.findViewById(R.id.nullLay);
        introLay = (RelativeLayout) view.findViewById(R.id.introLay);

        referralUrl.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        refer.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        display = getActivity().getWindowManager().getDefaultDisplay();

        introLay.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);
        inviteHistory();
    }

    private void inviteHistory() {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_INVITE_HISTORY, new Response.Listener<String>() {

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "inviteHistoryRes=" + res);
                    progress.setVisibility(View.GONE);
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);

                    if (status.equalsIgnoreCase("true")) {
                        FragmentMainActivity.currency = DefensiveClass.optString(json, Constants.TAG_CURRENCY);
                        FragmentMainActivity.creditAmount = DefensiveClass.optString(json, Constants.TAG_CREDITS);
                        credit_per_invite = DefensiveClass.optString(json, Constants.TAG_CREDIT_PER_INVITE);
                        referral_url = DefensiveClass.optString(json, Constants.TAG_REFERRAL_URL);

                        introLay.setVisibility(View.VISIBLE);
                        nullLay.setVisibility(View.GONE);

                        if (FragmentMainActivity.creditAmount.equals("")) {
                            FragmentMainActivity.creditAmount = "0";
                        }

                        if (FragmentMainActivity.currency.equals("")) {
                            FragmentMainActivity.currency = "$";
                        }
                        creditBalance.setText(FragmentMainActivity.currency + " " + FragmentMainActivity.creditAmount);
                        referralUrl.setText(referral_url);
                        linkTextView(getActivity(), inviteDes, getString(R.string.invite_des, credit_per_invite), getString(R.string.learn_more));

                        JSONArray result = json.optJSONArray(Constants.TAG_RESULT);

                        if (result != null) {
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject temp = result.getJSONObject(i);

                                String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String created_date = DefensiveClass.optString(temp, Constants.TAG_CREATED_DATE);
                                String credit_amount = DefensiveClass.optString(temp, Constants.TAG_CREDITS);

                                HashMap<String, String> map = new HashMap<>();

                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_CREATED_DATE, created_date);
                                map.put(Constants.TAG_CREDITS, credit_amount);

                                inviteAry.add(map);
                            }
                        }

                    } else if (status.equalsIgnoreCase("error")) {
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
                        setErrorLayout();
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
                Log.e(TAG, "inviteHistoryError: " + error.getMessage());
                setErrorLayout();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", "289");
                Log.i(TAG, "inviteHistoryParams: " + map);
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
        introLay.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        nullLay.setVisibility(View.VISIBLE);
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
            ds.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
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

        title.setText(getString(R.string.how_to_use_referral_code));
        desc.setText(getString(R.string.invite_steps));

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refer:
                Intent g = new Intent(Intent.ACTION_SEND);
                g.setType("text/plain");
                g.putExtra(Intent.EXTRA_TEXT, referralUrl.getText().toString());
                startActivity(Intent.createChooser(g, "Share"));
                break;

            case R.id.saveBtn:
                Intent i = new Intent(getActivity(), InviteHistory.class);
                i.putExtra("data", inviteAry);
                startActivity(i);
                break;
            case R.id.referralUrl:
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(R.string.app_name + "referral code", (referralUrl.getText().toString()));
                clipboard.setPrimaryClip(clip);
                FantacyApplication.defaultSnack(getActivity().findViewById(R.id.parentLay), getString(R.string.refercodecopied), "alert");
                break;
        }
    }
}
