package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.URLSpanNoUnderline;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WriteReview extends BaseActivity implements View.OnClickListener{

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, itemImage;
    TextView title, itemName, submit,saveBtn, ratingTxt;
    RatingBar ratingBar;
    EditText reviewTitle, reviewDes;
    String image = "", item_name = "", orderid= "", item_id="";
    ProgressDialog pdialog;
    String strReviewTitle, rating="", reviewDescription="", review_id = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_review_layout);

        overridePendingTransition(R.anim.slide_up, R.anim.stay);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        title = (TextView) findViewById(R.id.title);
        ratingTxt = (TextView) findViewById(R.id.ratingtxt);
        saveBtn = (TextView) findViewById(R.id.saveBtn);
        itemImage = (ImageView) findViewById(R.id.itemImage);
        itemName = (TextView) findViewById(R.id.itemName);
        submit = (TextView) findViewById(R.id.submit);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        reviewTitle = (EditText) findViewById(R.id.reviewTitle);
        reviewDes = (EditText) findViewById(R.id.reviewDes);


        pdialog = new ProgressDialog(WriteReview.this);
        pdialog.setMessage(getString(R.string.pleasewait));
        pdialog.setCancelable(false);
        pdialog.setCanceledOnTouchOutside(false);


        image = (String) getIntent().getExtras().get("image");
        item_name = (String) getIntent().getExtras().get("item_name");
        orderid = (String) getIntent().getExtras().get("order_id");
        item_id = (String) getIntent().getExtras().get("item_id");
        //strReviewTitle = (String) getIntent().getExtras().get("review_title");
        if(getIntent().hasExtra("review_id")) {
            review_id = (String) getIntent().getExtras().get("review_id");
            rating = (String) getIntent().getExtras().get("rating");
            reviewDescription = (String) getIntent().getExtras().get("review_desc");
        }
        Log.e("reviewdetail","-"+orderid+" ite "+item_id);
        itemName.setText(item_name);
        title.setText(getString(R.string.review));
        backBtn.setImageResource(R.drawable.close);
        saveBtn.setText(getString(R.string.submit));
        if (image != null && !image.equals("")) {
            Picasso.get().load(image).into(itemImage);
            Picasso.get().load(image).into(itemImage);
        }

        SharedPreferences lanPref = FantacyApplication.getInstance().getApplicationContext().getSharedPreferences("LangPref", MODE_PRIVATE);
        String languageCode = lanPref.getString(Constants.TAG_CODE, Constants.LANGUAGE_CODE);
        if(languageCode.equalsIgnoreCase("ar"))
            reviewDes.setTextDirection(View.TEXT_DIRECTION_RTL);
        else reviewDes.setTextDirection(View.TEXT_DIRECTION_LTR);



        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);


        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Log.e("rating","-"+rating);
                ratingTxt.setText("("+rating+")");
            }
        });


        if(!review_id.equalsIgnoreCase("")){
            float ratingFloat = Float.valueOf(rating);
            ratingBar.setRating(ratingFloat);
//            reviewDes.setText(reviewDescription);
            reviewDes.setText((new HtmlSpanner()).fromHtml(reviewDescription));
            reviewDes.setMovementMethod(LinkMovementMethod.getInstance());
            stripUnderls(reviewDes);

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                reviewDes.setText(Html.fromHtml(reviewDescription.trim(),Html.FROM_HTML_MODE_LEGACY));
//            } else {
//                reviewDes.setText(Html.fromHtml(reviewDescription.trim()));
//            }

        }
    }

    private void stripUnderls(TextView textView) {
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



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backBtn:
                finish();
                overridePendingTransition(R.anim.stay, R.anim.slide_down);
                break;
            case R.id.saveBtn:
                if (getCurrentFocus() != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                if (ratingBar.getRating() == 0.0f) {
                    FantacyApplication.defaultSnack(WriteReview.this.findViewById(R.id.parentLay), getString(R.string.reviewrating_error), "alert");
                    reviewTitle.requestFocus();
                } /*else if (reviewDes.getText().toString().trim().length() < 3) {
                    FantacyApplication.defaultSnack(WriteReview.this.findViewById(R.id.parentLay), getString(R.string.reviewdes_error), "alert");
                    reviewDes.requestFocus();
                } */else {
                    writeReview();
                }
                break;
        }
    }




    private void writeReview() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADD_REVIEW, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    if (pdialog.isShowing()) {
                        pdialog.dismiss();
                    }
                    JSONObject json = new JSONObject(res);
                    Log.i(TAG, "writeReviewRes: " + json);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        Intent i = new Intent(WriteReview.this, DetailActivity.class);
                        setResult(RESULT_OK, i);
                        finish();
                        overridePendingTransition(R.anim.stay, R.anim.slide_down);
                    } else {
                        String error = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        FantacyApplication.defaultSnack(WriteReview.this.findViewById(R.id.parentLay), error, "error");
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
                Log.e(TAG, "writeReviewError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                String msgStr = "";

                SpannableString span = new SpannableString(reviewDes.getText().toString().trim());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
                    msgStr = Html.toHtml(span,Html.FROM_HTML_MODE_LEGACY);
                }else msgStr = Html.toHtml(span);

                msgStr = msgStr.replace(" dir="+'"'+"ltr"+'"', "");

                Log.e("messageEdit","-"+msgStr);

                Map<String, String> map = new HashMap<String, String>();
                map.put("userid", GetSet.getUserId());
                map.put("itemid", item_id);
                map.put("orderid", orderid);
                map.put("rating",ratingBar.getRating()+"");
                map.put("review", msgStr+"");
                map.put("review_id", review_id+"");
                Log.i(TAG, "writeReviewParams: " + map);
                return map;
            }
        };
        pdialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }
}
