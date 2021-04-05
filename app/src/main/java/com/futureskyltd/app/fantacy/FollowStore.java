package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.EndlessRecyclerOnScrollListener;
import com.futureskyltd.app.helper.DatabaseHandler;
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

/**
 * Created by hitasoft on 22/6/17.
 */

public class FollowStore extends Fragment {

    private String TAG = "FollowStore", from = "", userId="";
    ImageView nullImage;
    TextView nullText;
    RelativeLayout nullLay, progressLay;
    RecyclerView recyclerView;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    DatabaseHandler helper;
    ArrayList<HashMap<String,String>> followersAry = new ArrayList<HashMap<String,String>>();

    public static FollowStore newInstance(String from, String userId) {
        FollowStore fragment = new FollowStore();
        Bundle args = new Bundle();
        args.putString("from", from);
        args.putString("userId", userId);
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
        progressLay = (RelativeLayout) view.findViewById(R.id.progress);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));

        if (getArguments() != null) {
            from = getArguments().getString("from");
            userId = getArguments().getString("userId");
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

        helper = DatabaseHandler.getInstance(getActivity());

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(getActivity(),
                linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), followersAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getFollowers(current_page * Constants.OVERALL_LIMIT);
                    Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                getFollowers(0);
                Log.v("Onrefresh:", "On refresh");
            }
        });

        followersAry.clear();
        getFollowers(0);
    }

    private void getFollowers(final int offset) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        String CallingAPI;
        if (from.equals("profile")){
            CallingAPI = Constants.API_FOLLOWING_STORES;
        } else {
            CallingAPI = Constants.API_POPULAR_STORE;
        }
        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v("res", "res=" + res);
                    JSONObject json = new JSONObject(res);
                    if (followersAry.size() >= Constants.OVERALL_LIMIT && followersAry.get(followersAry.size() - 1) == null) {
                        followersAry.remove(followersAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(followersAry.size());
                    }
                    progressLay.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setEnabled(true);
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                        followersAry.clear();
                    }

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray result = json.getJSONArray(Constants.TAG_RESULT);
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject temp = result.getJSONObject(i);

                            HashMap<String, String> map = new HashMap<String, String>();
                            String store_id = DefensiveClass.optString(temp, Constants.TAG_STORE_ID);
                            String store_name = DefensiveClass.optString(temp, Constants.TAG_STORE_NAME);
                            String wifi = DefensiveClass.optString(temp, Constants.TAG_WIFI);
                            String merchant_name = DefensiveClass.optString(temp, Constants.TAG_MERCHANT_NAME);
                            String follow_status = DefensiveClass.optString(temp, Constants.TAG_STATUS);
                            String image = DefensiveClass.optString(temp, Constants.TAG_IMAGE);

                            map.put(Constants.TAG_STORE_ID, store_id);
                            map.put(Constants.TAG_STORE_NAME, store_name);
                            map.put(Constants.TAG_WIFI, wifi);
                            map.put(Constants.TAG_MERCHANT_NAME, merchant_name);
                            map.put(Constants.TAG_STATUS, follow_status);
                            map.put(Constants.TAG_IMAGE, image);

                            followersAry.add(map);

                            helper.addStoreDetails(store_id, follow_status);
                        }

                        if (mScrollListener != null && followersAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    }

                    if (followersAry.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                        nullImage.setImageResource(R.drawable.no_stores);
                        nullText.setText(getString(R.string.no_stores));
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
                Log.e("Fantacy Error", "Error: " + error.getMessage());
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
                        if (followersAry.size() >= Constants.OVERALL_LIMIT) {
                            followersAry.add(null);
                            recyclerViewAdapter.notifyItemInserted(followersAry.size() - 1);
                        } else if (followersAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                            if (from.equals("profile")){
                                swipeRefresh();
                            } else {
                                progressLay.setVisibility(View.VISIBLE);
                                mSwipeRefreshLayout.setEnabled(false);
                            }
                        }
                        if (mScrollListener != null){
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", userId);
                if (from.equals("profile") && accesstoken != null){
                    map.put("logged_user_id", "289");
                }
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                Log.v("map", "map=" + map);
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

    private void swipeRefresh(){
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void setErrorLayout(){
        if (followersAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
        }
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setEnabled(true);
    }

    private void followStore(final Boolean follow, final int position) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        String CallingAPI;
        if (follow) // Follow Api
            CallingAPI = Constants.API_FOLLOW_STORE;
        else  // Unfollow Api
            CallingAPI = Constants.API_UNFOLLOW_STORE;

        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        if (follow) {
                            followersAry.get(position).put(Constants.TAG_STATUS, "follow");
                            helper.updateStoreDetails(followersAry.get(position).get(Constants.TAG_STORE_ID), "follow");
                        } else {
                            followersAry.get(position).put(Constants.TAG_STATUS, "unfollow");
                            helper.updateStoreDetails(followersAry.get(position).get(Constants.TAG_STORE_ID), "unfollow");
                        }
                        recyclerViewAdapter.notifyDataSetChanged();
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
                Log.e("Fantacy Error", "Error: " + error.getMessage());
                if (follow) {
                    followersAry.get(position).put(Constants.TAG_STATUS, "follow");
                    helper.updateStoreDetails(followersAry.get(position).get(Constants.TAG_STORE_ID), "follow");
                } else {
                    followersAry.get(position).put(Constants.TAG_STATUS, "unfollow");
                    helper.updateStoreDetails(followersAry.get(position).get(Constants.TAG_STORE_ID), "unfollow");
                }
                recyclerViewAdapter.notifyDataSetChanged();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", "289");
                map.put("store_id", followersAry.get(position).get(Constants.TAG_STORE_ID));
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

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;
        DatabaseHandler helper;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
            this.helper = DatabaseHandler.getInstance(context);
        }

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView userImage, followBtn;
            TextView fullName, userName;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                userImage = (ImageView) view.findViewById(R.id.userImage);
                fullName = (TextView) view.findViewById(R.id.fullName);
                userName = (TextView) view.findViewById(R.id.userName);
                followBtn = (ImageView) view.findViewById(R.id.followBtn);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);

                userImage.setOnClickListener(this);
                followBtn.setOnClickListener(this);
                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.userImage:
                    case R.id.mainLay:
                        Intent i = new Intent(getActivity(), StoreProfile.class);
                        i.putExtra("storeId", Items.get(getAdapterPosition()).get(Constants.TAG_STORE_ID));
                        startActivity(i);
                        break;
                    case R.id.followBtn:
                        if (GetSet.isLogged()){
                            String status = helper.getStoreDetails(Items.get(getAdapterPosition()).get(Constants.TAG_STORE_ID));
                            if (status.equalsIgnoreCase("follow")) {
                                followStore(true, getAdapterPosition());
                                Items.get(getAdapterPosition()).put(Constants.TAG_STATUS, "unfollow");
                                helper.updateStoreDetails(Items.get(getAdapterPosition()).get(Constants.TAG_STORE_ID), "unfollow");
                            } else {
                                followStore(false, getAdapterPosition());
                                Items.get(getAdapterPosition()).put(Constants.TAG_STATUS, "follow");
                                helper.updateStoreDetails(Items.get(getAdapterPosition()).get(Constants.TAG_STORE_ID), "follow");
                            }
                            notifyDataSetChanged();
                        } else {
                            Intent login = new Intent(getActivity(), LoginActivity.class);
                            startActivity(login);
                        }
                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return Items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.followers_list_items, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                HashMap<String, String> tempMap = Items.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;
                viewHolder.fullName.setText(tempMap.get(Constants.TAG_STORE_NAME));
                viewHolder.userName.setText("@" + tempMap.get(Constants.TAG_MERCHANT_NAME));

                String image = tempMap.get(Constants.TAG_IMAGE);
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(viewHolder.userImage);
                }
                String status = helper.getStoreDetails(tempMap.get(Constants.TAG_STORE_ID));
                if (status.equalsIgnoreCase("follow")) {
                    viewHolder.followBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.primary_color_sharp_corner));
                    viewHolder.followBtn.setImageResource(R.drawable.store_unfollow);
                } else {
                    viewHolder.followBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    viewHolder.followBtn.setImageResource(R.drawable.store_follow);
                }
            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerViewAdapter != null){
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }
}
