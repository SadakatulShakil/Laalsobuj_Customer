package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
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
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 20/7/17.
 */

public class WriteReview2 extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, itemImage;
    TextView title, itemName, submit;
    RatingBar ratingBar;
    EditText reviewTitle, reviewDes;
    String image = "", item_name = "", orderid, storeid;
    ProgressDialog pdialog;
    String strReviewTitle, rating, reviewDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_review_layout);

        overridePendingTransition(R.anim.slide_up, R.anim.stay);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        title = (TextView) findViewById(R.id.title);
        itemImage = (ImageView) findViewById(R.id.itemImage);
        itemName = (TextView) findViewById(R.id.itemName);
        submit = (TextView) findViewById(R.id.submit);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        reviewTitle = (EditText) findViewById(R.id.reviewTitle);
        reviewDes = (EditText) findViewById(R.id.reviewDes);

        pdialog = new ProgressDialog(WriteReview2.this);
        pdialog.setMessage(getString(R.string.pleasewait));
        pdialog.setCancelable(false);
        pdialog.setCanceledOnTouchOutside(false);

        title.setText(getString(R.string.review));
        backBtn.setImageResource(R.drawable.close);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        submit.setOnClickListener(this);

        image = (String) getIntent().getExtras().get("image");
        item_name = (String) getIntent().getExtras().get("item_name");
        orderid = (String) getIntent().getExtras().get("orderid");
        storeid = (String) getIntent().getExtras().get("storeid");
        strReviewTitle = (String) getIntent().getExtras().get("review_title");
        rating = (String) getIntent().getExtras().get("rating");
        reviewDescription = (String) getIntent().getExtras().get("review_des");
        itemName.setText(item_name);
        if (image != null && !image.equals("")) {
            Picasso.get().load(image).into(itemImage);
        }
        if ((strReviewTitle.length() != 0) && (reviewDescription.length() != 0)) {
            reviewTitle.setText(strReviewTitle);
            reviewDes.setText(reviewDescription);
            float ratingFloat = Float.valueOf(rating);
            reviewTitle.setEnabled(false);
            reviewDes.setEnabled(false);
            ratingBar.setRating(ratingFloat);
            ratingBar.setIsIndicator(true);
            submit.setTextColor(getResources().getColor(R.color.textLight));
            submit.setVisibility(View.GONE);
        } else {
            reviewTitle.setEnabled(true);
            reviewDes.setEnabled(true);
            ratingBar.setRating(0);
            ratingBar.setIsIndicator(false);
            submit.setTextColor(getResources().getColor(R.color.colorPrimary));
            submit.setVisibility(View.VISIBLE);
        }
        // register ctInstance().setConnectivityListener(this);
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
            case R.id.submit:
                if (getCurrentFocus() != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                if (ratingBar.getRating() == 0.0f) {
                    FantacyApplication.defaultSnack(WriteReview2.this.findViewById(R.id.parentLay), getString(R.string.reviewrating_error), "alert");
                    reviewTitle.requestFocus();
                } else if (reviewTitle.getText().toString().trim().length() < 3) {
                    FantacyApplication.defaultSnack(WriteReview2.this.findViewById(R.id.parentLay), getString(R.string.reviewtitle_error), "alert");
                    reviewTitle.requestFocus();
                } else if (reviewDes.getText().toString().trim().length() < 3) {
                    FantacyApplication.defaultSnack(WriteReview2.this.findViewById(R.id.parentLay), getString(R.string.reviewdes_error), "alert");
                    reviewDes.requestFocus();
                } else {
                    writeReview();
                }
                break;
        }
    }

    private void writeReview() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_WRITE_REVIEW, new Response.Listener<String>() {
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
                        finish();
                        overridePendingTransition(R.anim.stay, R.anim.slide_down);
                    } else {
                        String error = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        FantacyApplication.defaultSnack(WriteReview2.this.findViewById(R.id.parentLay), error, "error");
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
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("store_id", storeid);
                map.put("order_id", orderid);
                map.put("title", reviewTitle.getText().toString());
                map.put("message", reviewDes.getText().toString());
                map.put("rating", String.valueOf(ratingBar.getRating()));
                Log.i(TAG, "writeReviewParams: " + map);
                return map;
            }
        };
        pdialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }
}
