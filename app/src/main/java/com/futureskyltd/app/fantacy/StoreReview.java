package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.EndlessRecyclerOnScrollListener;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.futureskyltd.app.utils.ItemsParsing;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 23/6/17.
 */

public class StoreReview extends Fragment {

    private static final String TAG = StoreReview.class.getSimpleName();
    private String from = "", storeId = "";
    ImageView nullImage;
    TextView nullText;
    RelativeLayout nullLay;
    RecyclerView recyclerView;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<HashMap<String, String>> reviewAry = new ArrayList<HashMap<String, String>>();

    public static StoreReview newInstance(String from, String storeId) {
        StoreReview fragment = new StoreReview();
        Bundle args = new Bundle();
        args.putString("from", from);
        args.putString("storeId", storeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.liked_main_layout, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        nullLay = (RelativeLayout) view.findViewById(R.id.nullLay);
        nullImage = (ImageView) view.findViewById(R.id.nullImage);
        nullText = (TextView) view.findViewById(R.id.nullText);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));

        if (getArguments() != null) {
            from = getArguments().getString("from");
            storeId = getArguments().getString("storeId");
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) nullLay.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.topMargin = FantacyApplication.dpToPx(getActivity(), 80);
        nullLay.setLayoutParams(params);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), reviewAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getReviews(current_page * Constants.OVERALL_LIMIT);
                    Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                reviewAry.clear();
                getReviews(0);
            }
        });

        reviewAry.clear();
        getReviews(0);
    }

    private void getReviews(final int offset) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_STORE_REVIEWS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getReviewsRes=" + res);
                    JSONObject json = new JSONObject(res);
                    if (reviewAry.size() >= Constants.OVERALL_LIMIT && reviewAry.get(reviewAry.size() - 1) == null) {
                        reviewAry.remove(reviewAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(reviewAry.size());
                    }

                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                        reviewAry.clear();
                    }

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray result = json.getJSONArray(Constants.TAG_RESULT);
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject temp = result.getJSONObject(i);

                            HashMap<String, String> map = new HashMap<String, String>();
                            String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                            String full_name = DefensiveClass.optString(temp, Constants.TAG_FULL_NAME);
                            String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                            String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                            String item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                            String item_title = DefensiveClass.optString(temp, Constants.TAG_ITEM_TITLE);
                            String item_image = DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE);
                            String title = DefensiveClass.optString(temp, Constants.TAG_TITLE);
                            String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);
                            String rating = DefensiveClass.optString(temp, Constants.TAG_RATING);
                            String visible = DefensiveClass.optString(temp, Constants.TAG_VISIBLE);

                            JSONObject productObject = temp.has(Constants.TAG_PRODUCT)?temp.getJSONObject(Constants.TAG_PRODUCT):new JSONObject();
                            JSONArray productArray = new JSONArray();
                            productArray.put(0, productObject);;
                            map.put(Constants.TAG_PRODUCT,productArray.toString());

                            map.put(Constants.TAG_USER_ID, user_id);
                            map.put(Constants.TAG_FULL_NAME, full_name);
                            map.put(Constants.TAG_USER_NAME, user_name);
                            map.put(Constants.TAG_USER_IMAGE, user_image);
                            map.put(Constants.TAG_ITEM_ID, item_id);
                            map.put(Constants.TAG_ITEM_TITLE, item_title);
                            map.put(Constants.TAG_ITEM_IMAGE, item_image);
                            map.put(Constants.TAG_TITLE, title);
                            map.put(Constants.TAG_MESSAGE, message);
                            map.put(Constants.TAG_RATING, rating);
                            map.put(Constants.TAG_VISIBLE,visible);

                            reviewAry.add(map);
                        }

                        if (mScrollListener != null && reviewAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    }

                    if (reviewAry.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                        nullImage.setImageResource(R.drawable.no_review);
                        nullText.setText(getString(R.string.no_review));
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                    recyclerView.stopScroll();
                    recyclerViewAdapter.notifyDataSetChanged();

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
                Log.e(TAG, "getReviewsError: " + error.getMessage());
                setErrorLayout();
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (reviewAry.size() >= Constants.OVERALL_LIMIT) {
                            reviewAry.add(null);
                            recyclerViewAdapter.notifyItemInserted(reviewAry.size() - 1);
                        } else if (reviewAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                            swipeRefresh();
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();
                map.put("store_id", storeId);
                if (GetSet.isLogged()) {
                    map.put("user_id", GetSet.getUserId());
                }
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                Log.v(TAG, "getReviewsParams=" + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void swipeRefresh() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void setErrorLayout() {
        if (reviewAry.size() == 0) {
            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
            }
            nullLay.setVisibility(View.VISIBLE);
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView userImage;
            TextView fullName, title, message;
            RatingBar ratingBar;

            public MyViewHolder(View view) {
                super(view);

                userImage = (ImageView) view.findViewById(R.id.userImage);
                fullName = (TextView) view.findViewById(R.id.fullName);
                title = (TextView) view.findViewById(R.id.title);
                message = (TextView) view.findViewById(R.id.message);
                ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);

                userImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.userImage:
                        break;
                }
            }
        }

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reviews_list_item, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            return Items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final HashMap<String, String> tempMap = Items.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;
                viewHolder.fullName.setText(tempMap.get(Constants.TAG_FULL_NAME));
                viewHolder.title.setText(tempMap.get(Constants.TAG_ITEM_TITLE));
                viewHolder.message.setText(Html.fromHtml(tempMap.get(Constants.TAG_MESSAGE)));

                String image = tempMap.get(Constants.TAG_USER_IMAGE);
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(viewHolder.userImage);
                }

           /*     if (FantacyApplication.isRTL(context)) {
                    viewHolder.title.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                    viewHolder.message.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                } else {
                    viewHolder.title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                    viewHolder.message.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                }
*/
                viewHolder.ratingBar.setRating(Float.parseFloat(tempMap.get(Constants.TAG_RATING)));

                LayerDrawable stars = (LayerDrawable) viewHolder.ratingBar.getProgressDrawable().getCurrent();
                stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
                stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.textSecondary), PorterDuff.Mode.SRC_ATOP);
                stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);

                ((MyViewHolder) holder).userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent p = new Intent(context, Profile.class);
                        p.putExtra("userId", tempMap.get(Constants.TAG_USER_ID));
                        startActivity(p);
                    }
                });

                ((MyViewHolder) holder).fullName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent p = new Intent(context, Profile.class);
                        p.putExtra("userId", tempMap.get(Constants.TAG_USER_ID));
                        startActivity(p);
                    }
                });

                ((MyViewHolder) holder).title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{
                        ItemsParsing similarItems = new ItemsParsing(context);
                        JSONArray similarAry = new JSONArray(tempMap.get(Constants.TAG_PRODUCT));
                         if(tempMap.get(Constants.TAG_VISIBLE).equalsIgnoreCase("true")) {
                             Intent i = new Intent(getActivity(), DetailActivity.class);
                             i.putExtra("items", similarItems.getItems(similarAry));
                             i.putExtra("position", 0);
                             startActivity(i);
                         }else Toast.makeText(context,getResources().getString(R.string.item_not_found),Toast.LENGTH_SHORT).show();
                        }catch(Exception e){}

                    }
                });

            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }
    }
}
