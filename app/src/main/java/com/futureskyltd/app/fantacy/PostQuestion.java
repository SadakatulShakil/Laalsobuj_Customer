package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.URLSpanNoUnderline;
import com.futureskyltd.app.helper.MaxLinesInputFilter;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.squareup.picasso.Picasso;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 26/5/17.
 */

public class PostQuestion extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, itemImage;
    TextView title, writeComment, itemName, question;
    String itemId = "", itemTitle = "", questionStr="", question_id = "", image = "", shopid, selectedQuery="", buyertype = "";
    EditText messageEdit, subject;
    LinearLayout contentLay;
    Spinner querySpin;
    View subjectView;
    private SharedPreferences preferences;
    private String accesstoken;
    ArrayList<String> queryList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_question);
        overridePendingTransition(R.anim.slide_up, R.anim.stay);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        itemImage = (ImageView) findViewById(R.id.itemImage);
        title = (TextView) findViewById(R.id.title);
        itemName = (TextView) findViewById(R.id.itemName);
        writeComment = (TextView) findViewById(R.id.writeComment);
        question = (TextView) findViewById(R.id.question);
        messageEdit = (EditText) findViewById(R.id.messageEdit);
        contentLay = (LinearLayout)findViewById(R.id.contentLay);

        backBtn.setImageResource(R.drawable.close);


        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        writeComment.setVisibility(View.VISIBLE);


        backBtn.setOnClickListener(this);
        writeComment.setOnClickListener(this);

        itemId = (String) getIntent().getExtras().get("itemId");
        itemTitle = (String) getIntent().getExtras().get("itemTitle");
        questionStr = (String) getIntent().getExtras().get("question");
        question_id = (String) getIntent().getExtras().get("question_id");
        image = (String) getIntent().getExtras().get("image");

        if(questionStr.equalsIgnoreCase("")) {
            writeComment.setText(getString(R.string.post));
            question.setVisibility(View.GONE);
            contentLay.setVisibility(View.VISIBLE);
            title.setText(getResources().getString(R.string.writequestion));
            itemName.setText(itemTitle);
        }
        else {
            contentLay.setVisibility(View.GONE);
            question.setVisibility(View.VISIBLE);
            writeComment.setText(getResources().getString(R.string.submit));
            title.setText(getResources().getString(R.string.write_answer));
        }

        SharedPreferences
                lanPref = FantacyApplication.getInstance().getApplicationContext().getSharedPreferences("LangPref", MODE_PRIVATE);
        String languageCode = lanPref.getString(Constants.TAG_CODE, Constants.LANGUAGE_CODE);
        if(languageCode.equalsIgnoreCase("ar"))
            messageEdit.setTextDirection(View.TEXT_DIRECTION_RTL);
        else messageEdit.setTextDirection(View.TEXT_DIRECTION_LTR);

        messageEdit.setFilters(new InputFilter[]{new MaxLinesInputFilter(5)});

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            question.setText("Q: "+Html.fromHtml(questionStr,Html.FROM_HTML_MODE_LEGACY));
        } else {
            question.setText("Q: "+Html.fromHtml(questionStr));
        }*/
        question.setText("Q: "+(new HtmlSpanner()).fromHtml(questionStr));
        question.setMovementMethod(LinkMovementMethod.getInstance());
        stripUnderlines(question);

        if (image != null && !image.equals("")) {
            Picasso.get().load(image).into(itemImage);
        }

        FantacyApplication.setupUI(this, findViewById(R.id.parentLay));

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);


    }

    private void stripUnderlines(TextView textView) {
        Spannable s = (Spannable) (textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s.toString().trim());
    }

    private void sendChat() {

        final ProgressDialog dialog = new ProgressDialog(PostQuestion.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADDPRODUCTFAQ, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Log.v(TAG ,"addproductfaq="+res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        Intent i = new Intent(PostQuestion.this, DetailActivity.class);
                        setResult(RESULT_OK, i);
                        finish();

                        FantacyApplication.showToast(PostQuestion.this, getString(R.string.sent_successfully), Toast.LENGTH_SHORT);
                    } else {
                         writeComment.setEnabled(true);
                        FantacyApplication.showToast(PostQuestion.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
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
                writeComment.setEnabled(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                String msgStr = "";

                SpannableString span = new SpannableString(messageEdit.getText().toString().trim());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
                    msgStr = Html.toHtml(span,Html.FROM_HTML_MODE_LEGACY);
                }else
                    msgStr = Html.toHtml(span);

                msgStr = msgStr.replace(" dir="+'"'+"ltr"+'"', "");

                Log.e("messageEdit","-"+msgStr);

                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", "289");
                map.put("item_id", itemId);
                map.put("content", msgStr);
                map.put("type", "question");
                map.put("parent_id", "");
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

    private void writeAnswer(final String itemId, final String parentId, final String content) {
        final ProgressDialog dialog = new ProgressDialog(PostQuestion.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADDPRODUCTFAQ, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Log.v(TAG ,"addproductfaq="+res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        Intent i = new Intent(PostQuestion.this, OtherAnswers.class);
                        setResult(RESULT_OK, i);
                        finish();
                        FantacyApplication.showToast(PostQuestion.this, getString(R.string.message_send_successfully), Toast.LENGTH_SHORT);
                    } else {
                        writeComment.setEnabled(true);
                        FantacyApplication.showToast(PostQuestion.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
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
                writeComment.setEnabled(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", "289");
                map.put("item_id", itemId);
                map.put("content", content);
                map.put("type", "answer");
                map.put("parent_id", parentId);
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
                FantacyApplication.hideSoftKeyboard(PostQuestion.this, messageEdit);
                if (selectedQuery.equalsIgnoreCase("Others") && subject.getText().toString().trim().length() == 0) {
                    FantacyApplication.showToast(PostQuestion.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT);
                } else if (selectedQuery.equals(getString(R.string.select_query))){
                    FantacyApplication.showToast(PostQuestion.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT);
                } else if (messageEdit.getText().toString().trim().length() == 0){
                    FantacyApplication.showToast(PostQuestion.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT);
                } else {
                    writeComment.setEnabled(false);

                    String msgStr = "";

                    SpannableString span = new SpannableString(messageEdit.getText().toString().trim());
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
                        msgStr = Html.toHtml(span,Html.FROM_HTML_MODE_LEGACY);
                    }else
                        msgStr = Html.toHtml(span);


                    msgStr = msgStr.replace(" dir="+'"'+"ltr"+'"', "");
                    Log.e("messageEdit","-"+msgStr);

                    if(questionStr.equalsIgnoreCase("")) {
                        sendChat();
                    }
                    else{
                        writeAnswer(itemId,question_id, msgStr.trim());
                    }
                }
                break;
        }
    }





}
