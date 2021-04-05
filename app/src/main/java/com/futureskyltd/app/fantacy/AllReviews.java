package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.EndlessRecyclerOnScrollListener;
import com.futureskyltd.app.external.URLSpanNoUnderline;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.squareup.picasso.Picasso;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by hitasoft on 26/5/17.
 */

public class AllReviews extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, itemImage;
    TextView title, itemName, totalrating,  averagerating, totalreview, viewallrating, rat5count, rat4count, rat3count, rat2count, rat1count;
    String itemId = "", itemTitle = "",  image = "", shopid, selectedQuery="";
    RecyclerView questionView;
    RelativeLayout progressLay, nullLay, bottomlay;
    EditText messageEdit, subject;
    ImageView nullImage;
    TextView nullText;
    LinearLayout contentLay, allreviewlay;
    Spinner querySpin;
    ProgressDialog loaderdialog;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    View subjectView;
    ArrayList<HashMap<String, String>> reviewlist = new ArrayList<HashMap<String, String>>();
    ReviewAdapter reviewAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_main_layout);
        overridePendingTransition(R.anim.slide_up, R.anim.stay);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        questionView = (RecyclerView)findViewById(R.id.commentList);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        itemImage = (ImageView) findViewById(R.id.itemImage);
        title = (TextView) findViewById(R.id.title);
        itemName = (TextView) findViewById(R.id.itemName);
        progressLay = (RelativeLayout) findViewById(R.id.progress);
        bottomlay = (RelativeLayout) findViewById(R.id.bottomlay);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        nullImage = (ImageView) findViewById(R.id.nullImage);
        nullText = (TextView) findViewById(R.id.nullText);
        contentLay = (LinearLayout) findViewById(R.id.contentLay);
        allreviewlay = (LinearLayout) findViewById(R.id.allreviewlay);
        averagerating = (TextView) findViewById(R.id.averagerating);
        totalrating = (TextView) findViewById(R.id.totalrating);
        totalreview = (TextView) findViewById(R.id.totalreview);
        viewallrating = (TextView) findViewById(R.id.viewallrating);
        rat5count = (TextView) findViewById(R.id.rat5_count);
        rat4count = (TextView) findViewById(R.id.rat4_count);
        rat3count = (TextView) findViewById(R.id.rat3_count);
        rat2count = (TextView) findViewById(R.id.rat2_count);
        rat1count = (TextView) findViewById(R.id.rat1_count);
        ProgressBar rat1progress = (ProgressBar)findViewById(R.id.rat1progress);
        ProgressBar rat2progress = (ProgressBar)findViewById(R.id.rat2progress);
        ProgressBar rat3progress = (ProgressBar)findViewById(R.id.rat3progress);
        ProgressBar rat4progress = (ProgressBar)findViewById(R.id.rat4progress);
        ProgressBar rat5progress = (ProgressBar)findViewById(R.id.rat5progress);

        backBtn.setImageResource(R.drawable.close);

        contentLay.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        bottomlay.setVisibility(View.GONE);
        allreviewlay.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        bottomlay.setOnClickListener(this);

        itemId = (String) getIntent().getExtras().get("itemId");
        itemTitle = (String) getIntent().getExtras().get("itemTitle");
        image = (String) getIntent().getExtras().get("image");
        String data = (String)getIntent().getExtras().get("data");

        title.setText(getResources().getString(R.string.allreviews));
        itemName.setText(itemTitle);

        if (image != null && !image.equals("")) {
            Picasso.get().load(image).into(itemImage);
        }

        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has("review_count")) {
                averagerating.setText(jsonObject.getString("rating"));
                totalrating.setText(jsonObject.getString("rating_count") + " Ratings" + " & ");
                totalreview.setText( jsonObject.getString("review_count") + " Reviews");

                rat5count.setText("" + jsonObject.getString("five"));
                rat4count.setText("" + jsonObject.getString("four"));
                rat3count.setText("" + jsonObject.getString("three"));
                rat2count.setText("" + jsonObject.getString("two"));
                rat1count.setText("" + jsonObject.getString("one"));
                if (jsonObject.getString("five").equalsIgnoreCase("")) rat5count.setText("0");
                if (jsonObject.getString("four").equalsIgnoreCase("")) rat4count.setText("0");
                if (jsonObject.getString("three").equalsIgnoreCase("")) rat3count.setText("0");
                if (jsonObject.getString("two").equalsIgnoreCase("")) rat2count.setText("0");
                if (jsonObject.getString("one").equalsIgnoreCase("")) rat1count.setText("0");


                if(!jsonObject.getString("rating_count").equalsIgnoreCase("")){
                    int total_ratcnt = Integer.parseInt(jsonObject.getString("rating_count"));
                    float rat5cnt = 0, rat4cnt =0, rat3cnt = 0, rat2cnt =0, rat1cnt =0;
                    if(!jsonObject.getString("five").equalsIgnoreCase("")){
                        rat5cnt = Float.parseFloat(jsonObject.getString("five"));
                        float five_per = (rat5cnt / total_ratcnt)*100;
                        Log.e("ratingcon","-"+five_per);
                        rat5progress.setProgress(Math.round(five_per));}else rat5progress.setProgress(0);

                    if(!jsonObject.getString("four").equalsIgnoreCase("")){
                        rat4cnt = Float.parseFloat(jsonObject.getString("four"));
                        float four_per = (rat4cnt / total_ratcnt)*100;
                        rat4progress.setProgress(Math.round(four_per));}else rat4progress.setProgress(0);

                    if(!jsonObject.getString("three").equalsIgnoreCase("")){
                        rat3cnt = Float.parseFloat(jsonObject.getString("three"));
                        float three_per = (rat3cnt / total_ratcnt)*100;
                        rat3progress.setProgress(Math.round(three_per));}else rat3progress.setProgress(0);

                    if(!jsonObject.getString("two").equalsIgnoreCase("")){
                        rat2cnt = Float.parseFloat(jsonObject.getString("two"));
                        float two_per = (rat2cnt / total_ratcnt)*100;
                        rat2progress.setProgress(Math.round(two_per));}else rat2progress.setProgress(0);

                    if(!jsonObject.getString("one").equalsIgnoreCase("")){
                        rat1cnt = Float.parseFloat(jsonObject.getString("one"));
                        float one_per = (rat1cnt / total_ratcnt)*100;
                        rat1progress.setProgress(Math.round(one_per));}else rat1progress.setProgress(0);

                }

            }
        }catch(Exception e){

        }

        loaderdialog = new ProgressDialog(AllReviews.this);
        loaderdialog.setMessage(getString(R.string.pleasewait));
        loaderdialog.setCancelable(false);
        loaderdialog.setCanceledOnTouchOutside(false);
        loaderdialog.show();

        getReviews(0);


        LinearLayoutManager commentsManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        questionView.setLayoutManager(commentsManager);

        DividerItemDecoration commentDivider = new DividerItemDecoration(this, commentsManager.getOrientation());
        commentDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));

        questionView.addItemDecoration(commentDivider);
        reviewAdapter = new ReviewAdapter(this, reviewlist);
        questionView.setAdapter(reviewAdapter);

        mScrollListener = new EndlessRecyclerOnScrollListener(commentsManager) {
            @Override
            public void onLoadMore(int current_page) {
                getReviews(current_page * Constants.OVERALL_LIMIT);
            }
        };
        questionView.addOnScrollListener(mScrollListener);


        FantacyApplication.setupUI(this, findViewById(R.id.parentLay));

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getReviews(final int offset) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GETREVIEWS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    if (reviewlist.size() >= Constants.OVERALL_LIMIT && reviewlist.get(reviewlist.size() - 1) == null) {
                        reviewlist.remove(reviewlist.size() - 1);
                        reviewAdapter.notifyItemRemoved(reviewlist.size());
                    }

                    if (loaderdialog.isShowing()) {
                        loaderdialog.dismiss();
                    }
                    Log.v(TAG ,"getreviews="+res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);

                    if (status.equalsIgnoreCase("true")) {
                        JSONArray reviewArray = json.getJSONArray(Constants.TAG_RESULT);
                        for (int p = 0; p < reviewArray.length(); p++) {
                            JSONObject pobj = reviewArray.getJSONObject(p);

                            HashMap<String, String> pmap = new HashMap<>();

                            String review_id = DefensiveClass.optString(pobj, Constants.TAG_ID);
                            String user_id = DefensiveClass.optString(pobj, Constants.TAG_USER_ID);
                            String user_name = DefensiveClass.optString(pobj, Constants.TAG_USER_NAME);
                            String user_image = DefensiveClass.optString(pobj, Constants.TAG_USER_IMAGE);
                            String review = DefensiveClass.optString(pobj, Constants.TAG_REVIEW);
                            String rating = DefensiveClass.optString(pobj, Constants.TAG_RATING);

                            pmap.put(Constants.TAG_ID, review_id);
                            pmap.put(Constants.TAG_USER_ID, user_id);
                            pmap.put(Constants.TAG_USER_NAME, user_name);
                            pmap.put(Constants.TAG_USER_IMAGE, user_image);
                            pmap.put(Constants.TAG_RATING, rating);
                            pmap.put(Constants.TAG_REVIEW,review);
                            reviewlist.add(pmap);
                        }


                        if (mScrollListener != null && reviewlist.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }

                    } else {
                        FantacyApplication.showToast(AllReviews.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                    }

                    progressLay.setVisibility(View.GONE);
                    if (reviewlist.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_message);
                        nullText.setText(getString(R.string.no_data));
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                    reviewAdapter.notifyDataSetChanged();


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
                if (loaderdialog.isShowing()) {
                    loaderdialog.dismiss();
                }
                setErrorLayout();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (reviewlist.size() >= Constants.OVERALL_LIMIT) {
                            reviewlist.add(null);
                            reviewAdapter.notifyItemInserted(reviewlist.size() - 1);
                        } else if (reviewlist.size() == 0) {
                            progressLay.setVisibility(View.VISIBLE);
                        }

                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("item_id", itemId);
                map.put("offset", ""+Integer.toString(offset));
                map.put("limit", ""+Integer.toString(Constants.OVERALL_LIMIT));
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
        FantacyApplication.getInstance().addToRequestQueue(req);
    }


    private void setErrorLayout() {
        if (reviewlist.size() == 0) {
            progressLay.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
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
            case R.id.bottomlay:
                Intent login = new Intent(AllReviews.this, PostQuestion.class);
                login.putExtra("itemId",itemId);
                login.putExtra("itemTitle",itemTitle);
                login.putExtra("question",itemTitle);
                login.putExtra("question_id",itemId);
                login.putExtra("image","");
                startActivity(login);
                break;
        }
    }



    public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;
        String itemId, itemImage;

        public ReviewAdapter(Context context, ArrayList<HashMap<String, String>> items) {
            this.Items = items;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.review_item, parent, false);
                return new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
            final HashMap<String, String> map = Items.get(position);
            if (viewHolder instanceof MyViewHolder) {

                MyViewHolder holder = (MyViewHolder) viewHolder;

                holder.user_name.setText("" + map.get(Constants.TAG_USER_NAME));
                /*String reviewremovespace = map.get(Constants.TAG_REVIEW).replace("\n", "").replaceAll("\\<[^>]*>","").replace("\r", "");
                String reviewFinal = html2text(reviewremovespace);
*/
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    holder.reviewtxt.setText(""+Html.fromHtml(map.get(Constants.TAG_REVIEW),Html.FROM_HTML_MODE_LEGACY));
//                } else {
//                    holder.reviewtxt.setText(""+Html.fromHtml(map.get(Constants.TAG_REVIEW)));
//                }
//                holder.reviewtxt.setMovementMethod(LinkMovementMethod.getInstance());
//                stripUnderlines(holder.reviewtxt);
                holder.reviewtxt.setText((new HtmlSpanner()).fromHtml(map.get(Constants.TAG_REVIEW)));
                holder.reviewtxt.setMovementMethod(LinkMovementMethod.getInstance());
                stripUnderlines(holder.reviewtxt);


                if(map.get(Constants.TAG_REVIEW).equalsIgnoreCase(""))
                    holder.reviewtxt.setVisibility(View.GONE);
                else holder.reviewtxt.setVisibility(View.VISIBLE);

                holder.ratingBar.setRating(Float.parseFloat(map.get(Constants.TAG_RATING)));
                LayerDrawable stars = (LayerDrawable) holder.ratingBar.getProgressDrawable().getCurrent();
                stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.yellow), PorterDuff.Mode.SRC_ATOP);
                stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.divider), PorterDuff.Mode.SRC_ATOP);
                stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.yellow), PorterDuff.Mode.SRC_ATOP);

                String imageUrl = map.get(Constants.TAG_USER_IMAGE);
                if (!imageUrl.equals("")) {
                    Picasso.get().load(imageUrl).into(holder.image);
                }

                holder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent p = new Intent(context, Profile.class);
                        p.putExtra("userId", map.get(Constants.TAG_USER_ID));
                        startActivity(p);
                    }
                });

            }else if (viewHolder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) viewHolder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return Items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView user_name, reviewtxt;
            RatingBar ratingBar;
            ImageView image;
            RelativeLayout parentLay;

            public MyViewHolder(View view) {
                super(view);

                user_name = (TextView) view.findViewById(R.id.user_name);
                reviewtxt = (TextView) view.findViewById(R.id.reviewtxt);
                ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
                image = (ImageView) view.findViewById(R.id.image);
                parentLay = (RelativeLayout) view.findViewById(R.id.parentlay);
//                parentLay.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                }

            }
        }
    }

    public static String html2text(String html) {
        return Jsoup.parse(html).text();
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





}
