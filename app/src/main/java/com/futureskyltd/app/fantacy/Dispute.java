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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.EndlessRecyclerOnScrollListener;
import com.futureskyltd.app.external.FontCache;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

/**
 * Created by hitasoft on 10/7/17.
 */

public class Dispute extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = Dispute.class.getSimpleName();
    public static boolean refreshBoth = false;
    static ViewPager viewPager;
    ImageView backBtn;
    TextView title;
    TabLayout tabLayout;
    private FragmentRefreshListener activefragmentRefreshListener, closedfragmentRefreshListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dispute_main_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        title.setText(getString(R.string.dispute));

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        changeTabsFont();

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    public FragmentRefreshListener getClosedFragmentRefreshListener() {
        return closedfragmentRefreshListener;
    }

    public void setClosedFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.closedfragmentRefreshListener = fragmentRefreshListener;
    }

    public FragmentRefreshListener getActiveFragmentRefreshListener() {
        return activefragmentRefreshListener;
    }

    public void setActiveFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.activefragmentRefreshListener = fragmentRefreshListener;
    }

    // For set custom font in tab
    private void changeTabsFont() {

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(FontCache.get("font_regular.ttf", this));
                }
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ActiveDispute(), getString(R.string.active));
        adapter.addFragment(new ClosedDispute(), getString(R.string.closed));
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (refreshBoth) {
            refreshBoth = false;
            if (getActiveFragmentRefreshListener() != null) {
                getActiveFragmentRefreshListener().onRefresh();
            }
            if (getClosedFragmentRefreshListener() != null) {
                getClosedFragmentRefreshListener().onRefresh();
            }
        }
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
        }
    }

    public interface FragmentRefreshListener {
        void onRefresh();
    }

    public static class ActiveDispute extends Fragment {

        RecyclerView recyclerView;
        LinearLayoutManager linearLayoutManager;
        RecyclerViewAdapter recyclerViewAdapter;
        ImageView nullImage;
        TextView nullText;
        RelativeLayout nullLay;
        ArrayList<HashMap<String, String>> activedisputeAry = new ArrayList<HashMap<String, String>>();
        private EndlessRecyclerOnScrollListener mScrollListener = null;
        private SwipeRefreshLayout mSwipeRefreshLayout = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dispute_recyclerview, container, false);

            recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
            nullLay = (RelativeLayout) rootView.findViewById(R.id.nullLay);
            nullImage = (ImageView) rootView.findViewById(R.id.nullImage);
            nullText = (TextView) rootView.findViewById(R.id.nullText);
            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
            mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));

            ((Dispute) getActivity()).setActiveFragmentRefreshListener(new Dispute.FragmentRefreshListener() {
                @Override
                public void onRefresh() {
                    // Refresh Your Fragment
                    swipeRefresh();
                    mScrollListener.resetpagecount();
                    getActiveDispute(0);
                }
            });

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);

            DividerItemDecoration itemDivider = new DividerItemDecoration(getActivity(),
                    linearLayoutManager.getOrientation());
            itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
            recyclerView.addItemDecoration(itemDivider);

            recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), activedisputeAry);
            recyclerView.setAdapter(recyclerViewAdapter);
            recyclerView.setAdapter(recyclerViewAdapter);

            mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int current_page) {
                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        getActiveDispute(current_page * Constants.OVERALL_LIMIT);
                        Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                    }
                }
            };
            recyclerView.addOnScrollListener(mScrollListener);

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mScrollListener.resetpagecount();
                    activedisputeAry.clear();
                    getActiveDispute(0);
                }
            });
            activedisputeAry.clear();
            getActiveDispute(0);
        }


        private void getActiveDispute(final int offset) {
            SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
            String accesstoken = preferences.getString("TOKEN", null);
            String customerId = preferences.getString("customer_id", null);
            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ACTIVE_DISPUTE, new Response.Listener<String>() {

                @Override
                public void onResponse(String res) {
                    try {
                        Log.v(TAG, "getActiveDisputeRes=" + res);
                        JSONObject json = new JSONObject(res);
                        if (activedisputeAry.size() >= Constants.OVERALL_LIMIT && activedisputeAry.get(activedisputeAry.size() - 1) == null) {
                            activedisputeAry.remove(activedisputeAry.size() - 1);
                            recyclerViewAdapter.notifyItemRemoved(activedisputeAry.size());
                        }

                        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                        }

                        String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("true")) {
                            JSONArray result = json.getJSONArray(Constants.TAG_RESULT);
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject temp = result.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<>();

                                String dispute_id = DefensiveClass.optString(temp, Constants.TAG_DISPUTE_ID);
                                String dispute_status = DefensiveClass.optString(temp, Constants.TAG_STATUS);
                                String item_title = DefensiveClass.optString(temp, Constants.TAG_ITEM_TITLE);
                                String item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                                String type = DefensiveClass.optString(temp, Constants.TAG_TYPE);
                                String image = DefensiveClass.optString(temp, Constants.TAG_IMAGE);
                                String dispute_date = DefensiveClass.optString(temp, Constants.TAG_DISPUTE_DATE);
                                String shop_id = DefensiveClass.optString(temp, Constants.TAG_SHOP_ID);
                                String shop_name = DefensiveClass.optString(temp, Constants.TAG_SHOP_NAME);
                                String shop_image = DefensiveClass.optString(temp, Constants.TAG_SHOP_IMAGE);
                                String last_replied = DefensiveClass.optString(temp, Constants.TAG_LAST_REPLIED);
                                String last_read = DefensiveClass.optString(temp, Constants.TAG_LAST_READ);

                                map.put(Constants.TAG_DISPUTE_ID, dispute_id);
                                map.put(Constants.TAG_STATUS, dispute_status);
                                map.put(Constants.TAG_ITEM_TITLE, item_title);
                                map.put(Constants.TAG_ITEM_ID, item_id);
                                map.put(Constants.TAG_TYPE, type);
                                map.put(Constants.TAG_IMAGE, image);
                                map.put(Constants.TAG_DISPUTE_DATE, dispute_date);
                                map.put(Constants.TAG_SHOP_ID, shop_id);
                                map.put(Constants.TAG_SHOP_NAME, shop_name);
                                map.put(Constants.TAG_SHOP_IMAGE, shop_image);
                                map.put(Constants.TAG_LAST_REPLIED, last_replied);
                                map.put(Constants.TAG_LAST_READ, last_read);

                                activedisputeAry.add(map);
                            }

                            if (mScrollListener != null) {
                                mScrollListener.setLoading(false);
                            }
                        }

                        if (activedisputeAry.size() == 0) {
                            nullLay.setVisibility(View.VISIBLE);
                            nullImage.setImageResource(R.drawable.no_dispute);
                            nullText.setText(getString(R.string.no_dispute));
                        } else {
                            nullLay.setVisibility(View.GONE);
                        }
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
                    Log.e(TAG, "getActiveDisputeError: " + error.getMessage());
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
                            if (activedisputeAry.size() >= Constants.OVERALL_LIMIT) {
                                activedisputeAry.add(null);
                                recyclerViewAdapter.notifyItemInserted(activedisputeAry.size() - 1);
                            } else if (activedisputeAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                                swipeRefresh();
                            }
                        }
                    });

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("user_id", customerId);
                    map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                    map.put("offset", Integer.toString(offset));
                    Log.v(TAG, "getActiveDisputeParams=" + map);
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

        private void swipeRefresh() {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        private void setErrorLayout() {
            if (activedisputeAry.size() == 0) {
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                }
                nullLay.setVisibility(View.VISIBLE);
            }
        }
    }

    public static class ClosedDispute extends Fragment {

        RecyclerView recyclerView;
        LinearLayoutManager linearLayoutManager;
        RecyclerViewAdapter recyclerViewAdapter;
        ArrayList<HashMap<String, String>> closeddisputeAry = new ArrayList<HashMap<String, String>>();
        ImageView nullImage;
        TextView nullText;
        RelativeLayout nullLay;
        private EndlessRecyclerOnScrollListener mScrollListener = null;
        private SwipeRefreshLayout mSwipeRefreshLayout = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dispute_recyclerview, container, false);

            recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

            nullLay = (RelativeLayout) rootView.findViewById(R.id.nullLay);
            nullImage = (ImageView) rootView.findViewById(R.id.nullImage);
            nullText = (TextView) rootView.findViewById(R.id.nullText);
            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
            mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));
            ((Dispute) getActivity()).setClosedFragmentRefreshListener(new Dispute.FragmentRefreshListener() {
                @Override
                public void onRefresh() {
                    // Refresh Your Fragment
                    swipeRefresh();
                    mScrollListener.resetpagecount();
                    getClosedDispute(0);
                }
            });

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);

            DividerItemDecoration itemDivider = new DividerItemDecoration(getActivity(), linearLayoutManager.getOrientation());
            itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
            recyclerView.addItemDecoration(itemDivider);

            recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), closeddisputeAry);
            recyclerView.setAdapter(recyclerViewAdapter);

            mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int current_page) {
                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        getClosedDispute(current_page * Constants.OVERALL_LIMIT);
                        Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                    }
                }
            };
            recyclerView.addOnScrollListener(mScrollListener);

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mScrollListener.resetpagecount();
                    closeddisputeAry.clear();
                    getClosedDispute(0);
                }
            });

            closeddisputeAry.clear();
            getClosedDispute(0);
        }


        private void getClosedDispute(final int offset) {
            SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
            String accesstoken = preferences.getString("TOKEN", null);
            String customerId = preferences.getString("customer_id", null);
            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CLOSED_DISPUTE, new Response.Listener<String>() {

                @Override
                public void onResponse(String res) {
                    try {
                        Log.v(TAG, "getClosedDisputeRes=" + res);
                        JSONObject json = new JSONObject(res);
                        if (closeddisputeAry.size() >= Constants.OVERALL_LIMIT && closeddisputeAry.get(closeddisputeAry.size() - 1) == null) {
                            closeddisputeAry.remove(closeddisputeAry.size() - 1);
                            recyclerViewAdapter.notifyItemRemoved(closeddisputeAry.size());
                        }

                        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                        }

                        String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("true")) {
                            JSONArray result = json.getJSONArray(Constants.TAG_RESULT);
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject temp = result.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<>();

                                String dispute_id = DefensiveClass.optString(temp, Constants.TAG_DISPUTE_ID);
                                String dispute_status = DefensiveClass.optString(temp, Constants.TAG_STATUS);
                                String item_title = DefensiveClass.optString(temp, Constants.TAG_ITEM_TITLE);
                                String item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                                String image = DefensiveClass.optString(temp, Constants.TAG_IMAGE);
                                String dispute_date = DefensiveClass.optString(temp, Constants.TAG_DISPUTE_DATE);
                                String shop_id = DefensiveClass.optString(temp, Constants.TAG_SHOP_ID);
                                String shop_name = DefensiveClass.optString(temp, Constants.TAG_SHOP_NAME);
                                String shop_image = DefensiveClass.optString(temp, Constants.TAG_SHOP_IMAGE);
                                String last_replied = DefensiveClass.optString(temp, Constants.TAG_LAST_REPLIED);
                                String last_read = DefensiveClass.optString(temp, Constants.TAG_LAST_READ);

                                map.put(Constants.TAG_DISPUTE_ID, dispute_id);
                                map.put(Constants.TAG_STATUS, dispute_status);
                                map.put(Constants.TAG_ITEM_TITLE, item_title);
                                map.put(Constants.TAG_ITEM_ID, item_id);
                                map.put(Constants.TAG_IMAGE, image);
                                map.put(Constants.TAG_DISPUTE_DATE, dispute_date);
                                map.put(Constants.TAG_SHOP_ID, shop_id);
                                map.put(Constants.TAG_SHOP_NAME, shop_name);
                                map.put(Constants.TAG_SHOP_IMAGE, shop_image);
                                map.put(Constants.TAG_LAST_REPLIED, last_replied);
                                map.put(Constants.TAG_LAST_READ, last_read);

                                closeddisputeAry.add(map);
                            }

                            if (mScrollListener != null) {
                                mScrollListener.setLoading(false);
                            }
                        }
                        if (closeddisputeAry.size() == 0) {
                            nullLay.setVisibility(View.VISIBLE);
                            nullImage.setImageResource(R.drawable.no_dispute);
                            nullText.setText(getString(R.string.no_dispute));
                        } else {
                            nullLay.setVisibility(View.GONE);
                        }
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
                    Log.e(TAG, "getClosedDisputeError: " + error.getMessage());
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
                            if (closeddisputeAry.size() >= Constants.OVERALL_LIMIT) {
                                closeddisputeAry.add(null);
                                recyclerViewAdapter.notifyItemInserted(closeddisputeAry.size() - 1);
                            } else if (closeddisputeAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                                swipeRefresh();
                            }
                        }
                    });

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("user_id", customerId);
                    map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                    map.put("offset", Integer.toString(offset));
                    Log.v(TAG, "getClosedDisputeParams=" + map);
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

        public void swipeRefresh() {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        private void setErrorLayout() {
            if (closeddisputeAry.size() == 0) {
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                }
                nullLay.setVisibility(View.VISIBLE);
            }
        }

    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dispute_item, parent, false);
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
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof MyViewHolder) {
                MyViewHolder holder = (MyViewHolder) viewHolder;
                HashMap<String, String> tempMap = Items.get(position);
                holder.status.setText(tempMap.get(Constants.TAG_STATUS));
                holder.id.setText(context.getString(R.string.id) + " : " + tempMap.get(Constants.TAG_DISPUTE_ID));
                if (!tempMap.get(Constants.TAG_DISPUTE_DATE).equals(null) && !tempMap.get(Constants.TAG_DISPUTE_DATE).equals("")) {
                    long date = Long.parseLong(tempMap.get(Constants.TAG_DISPUTE_DATE)) * 1000;
                    holder.date.setText(FantacyApplication.getDate(date));
                }
                String image = tempMap.get(Constants.TAG_IMAGE);
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(holder.image);
                }
            } else if (viewHolder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) viewHolder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
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

            ImageView image;
            TextView id, status, date;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                image = (ImageView) view.findViewById(R.id.image);
                id = (TextView) view.findViewById(R.id.id);
                status = (TextView) view.findViewById(R.id.status);
                date = (TextView) view.findViewById(R.id.date);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);

                mainLay.setOnClickListener(this);
            }


            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mainLay:
                        Intent intent = new Intent(context, DisputeChat.class);
                        intent.putExtra("disputeId", Items.get(getAdapterPosition()).get(Constants.TAG_DISPUTE_ID));
                        if (viewPager.getCurrentItem() == 0)
                            intent.putExtra("from", Items.get(getAdapterPosition()).get(Constants.TAG_TYPE));
                        else
                            intent.putExtra("from", "close");
                        context.startActivity(intent);
                        break;
                }
            }
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}

