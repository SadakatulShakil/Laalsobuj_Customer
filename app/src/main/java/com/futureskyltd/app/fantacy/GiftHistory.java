package com.futureskyltd.app.fantacy;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hitasoft on 17/7/17.
 */

public class GiftHistory extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = GiftHistory.class.getSimpleName();
    ImageView backBtn;
    TextView title;
    TabLayout tabLayout;
    ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gift_history_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        title.setText(getString(R.string.gift_history));

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        changeTabsFont();

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
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
        adapter.addFragment(new SentHistory(), getString(R.string.sent));
        adapter.addFragment(new ReceivedHistory(), getString(R.string.received));
        viewPager.setAdapter(adapter);
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
        }
    }

    public static class SentHistory extends Fragment {

        RecyclerView recyclerView;
        LinearLayoutManager linearLayoutManager;
        SentViewAdapter recyclerViewAdapter;
        RelativeLayout progressLay, nullLay;
        ImageView nullImage;
        TextView nullText;
        ArrayList<HashMap<String, String>> giftAry = new ArrayList<HashMap<String, String>>();
        private EndlessRecyclerOnScrollListener mScrollListener = null;
        private SwipeRefreshLayout mSwipeRefreshLayout = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dispute_recyclerview, container, false);

            recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
            mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));
            nullLay = (RelativeLayout) rootView.findViewById(R.id.nullLay);
            nullImage = (ImageView) rootView.findViewById(R.id.nullImage);
            nullText = (TextView) rootView.findViewById(R.id.nullText);
            progressLay = (RelativeLayout) rootView.findViewById(R.id.progress);
            nullImage.setImageResource(R.drawable.no_history);
            nullText.setText(getString(R.string.no_history));
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);

            recyclerViewAdapter = new SentViewAdapter(getActivity(), giftAry);
            recyclerView.setAdapter(recyclerViewAdapter);

            mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int current_page) {
                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        getSentData(current_page * Constants.OVERALL_LIMIT);
                        Log.v(TAG, "onLoadMore offset" + (Constants.OVERALL_LIMIT * current_page));
                    }
                }
            };
            recyclerView.addOnScrollListener(mScrollListener);

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mScrollListener.resetpagecount();
                    giftAry.clear();
                    getSentData(0);
                }
            });
            giftAry.clear();
            getSentData(0);
        }

        private void getSentData(final int offset) {
            SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
            String accesstoken = preferences.getString("TOKEN", null);

            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SENT_GIFTCARD, new Response.Listener<String>() {

                @Override
                public void onResponse(String res) {
                    try {
                        Log.v(TAG, "getSentDataRes=" + res);
                        JSONObject json = new JSONObject(res);
                        progressLay.setVisibility(View.GONE);
                        mSwipeRefreshLayout.setEnabled(true);
                        if (giftAry.size() >= Constants.OVERALL_LIMIT && giftAry.get(giftAry.size() - 1) == null) {
                            giftAry.remove(giftAry.size() - 1);
                            recyclerViewAdapter.notifyItemRemoved(giftAry.size());
                        }
                        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                            giftAry.clear();
                            mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                        }

                        String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("true")) {
                            JSONArray result = json.getJSONArray(Constants.TAG_RESULT);

                            for (int i = 0; i < result.length(); i++) {
                                JSONObject temp = result.getJSONObject(i);

                                String recipient_id = DefensiveClass.optString(temp, Constants.TAG_RECIPIENT_ID);
                                String recipient_name = DefensiveClass.optString(temp, Constants.TAG_RECIPIENT_NAME);
                                String created_date = DefensiveClass.optString(temp, Constants.TAG_CREATED_DATE);
                                String gift_amount = DefensiveClass.optString(temp, Constants.TAG_GIFT_AMOUNT);
                                String currency = DefensiveClass.optString(temp, Constants.TAG_CURRENCY);

                                HashMap<String, String> map = new HashMap<>();

                                map.put(Constants.TAG_RECIPIENT_ID, recipient_id);
                                map.put(Constants.TAG_RECIPIENT_NAME, recipient_name);
                                map.put(Constants.TAG_CREATED_DATE, created_date);
                                map.put(Constants.TAG_GIFT_AMOUNT, gift_amount);
                                map.put(Constants.TAG_CURRENCY, currency);

                                giftAry.add(map);

                            }

                            if (mScrollListener != null && giftAry.size() >= Constants.OVERALL_LIMIT) {
                                mScrollListener.setLoading(false);
                            }
                        }

                        if (giftAry.size() == 0) {
                            nullLay.setVisibility(View.VISIBLE);
                        } else {
                            nullLay.setVisibility(View.GONE);
                        }
                        recyclerViewAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "getSentDataError: " + error.getMessage());
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }
                    if (giftAry.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (giftAry.size() >= Constants.OVERALL_LIMIT) {
                                giftAry.add(null);
                                recyclerViewAdapter.notifyItemInserted(giftAry.size() - 1);
                            } else if (giftAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                                progressLay.setVisibility(View.VISIBLE);
                                mSwipeRefreshLayout.setEnabled(false);
                            }
                            if (mScrollListener != null) {
                                mScrollListener.setLoading(true);
                            }
                        }
                    });

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("user_id", "289");
                    map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                    map.put("offset", Integer.toString(offset));
                    Log.v(TAG, "getSentDataParams=" + map);
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
    }

    public static class SentViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;

        public SentViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public int getItemViewType(int position) {
            return Items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gift_sent_list_item, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                MyViewHolder viewHolder = (MyViewHolder) holder;
                HashMap<String, String> tempMap = Items.get(position);

                if (position == Items.size() - 1)
                    viewHolder.view1.setVisibility(View.VISIBLE);
                else
                    viewHolder.view2.setVisibility(View.VISIBLE);

                viewHolder.userName.setText(tempMap.get(Constants.TAG_RECIPIENT_NAME));
                viewHolder.price.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_GIFT_AMOUNT));

                if (!tempMap.get(Constants.TAG_CREATED_DATE).equals(null) && !tempMap.get(Constants.TAG_CREATED_DATE).equals("")) {
                    long date = Long.parseLong(tempMap.get(Constants.TAG_CREATED_DATE)) * 1000;
                    viewHolder.date.setText(FantacyApplication.getDate(date));
                }
            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
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

            TextView userName, price, date;
            View view2, view1;

            public MyViewHolder(View view) {
                super(view);

                userName = (TextView) view.findViewById(R.id.userName);
                price = (TextView) view.findViewById(R.id.price);
                date = (TextView) view.findViewById(R.id.date);
                view1 = view.findViewById(R.id.view1);
                view2 = view.findViewById(R.id.view2);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                }
            }
        }
    }

    public static class ReceivedHistory extends Fragment {

        RecyclerView recyclerView;
        LinearLayoutManager linearLayoutManager;
        ReceivedViewAdapter recyclerViewAdapter;
        RelativeLayout progressLay, nullLay;
        ImageView nullImage;
        TextView nullText;
        ArrayList<HashMap<String, String>> giftAry = new ArrayList<HashMap<String, String>>();
        private EndlessRecyclerOnScrollListener mScrollListener = null;
        private SwipeRefreshLayout mSwipeRefreshLayout = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dispute_recyclerview, container, false);

            recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
            mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));
            nullLay = (RelativeLayout) rootView.findViewById(R.id.nullLay);
            nullImage = (ImageView) rootView.findViewById(R.id.nullImage);
            nullText = (TextView) rootView.findViewById(R.id.nullText);
            progressLay = (RelativeLayout) rootView.findViewById(R.id.progress);
            nullImage.setImageResource(R.drawable.no_history);
            nullText.setText(getString(R.string.no_history));

            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);

            recyclerViewAdapter = new ReceivedViewAdapter(getActivity(), giftAry);
            recyclerView.setAdapter(recyclerViewAdapter);

            mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int current_page) {
                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        getReceivedData(current_page * Constants.OVERALL_LIMIT);
                        Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                    }
                }
            };
            recyclerView.addOnScrollListener(mScrollListener);

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mScrollListener.resetpagecount();
                    giftAry.clear();
                    getReceivedData(0);
                }
            });
            giftAry.clear();
            getReceivedData(0);
        }

        private void getReceivedData(final int offset) {

            SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
            String accesstoken = preferences.getString("TOKEN", null);

            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_RECEIVED_GIFTCARD, new Response.Listener<String>() {

                @Override
                public void onResponse(String res) {
                    try {
                        Log.v(TAG, "getReceivedDataRes=" + res);
                        JSONObject json = new JSONObject(res);
                        progressLay.setVisibility(View.GONE);
                        mSwipeRefreshLayout.setEnabled(true);
                        if (giftAry.size() >= Constants.OVERALL_LIMIT && giftAry.get(giftAry.size() - 1) == null) {
                            giftAry.remove(giftAry.size() - 1);
                            recyclerViewAdapter.notifyItemRemoved(giftAry.size());
                        }
                        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                            giftAry.clear();
                            mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                        }

                        String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("true")) {
                            JSONArray result = json.getJSONArray(Constants.TAG_RESULT);

                            for (int i = 0; i < result.length(); i++) {
                                JSONObject temp = result.getJSONObject(i);

                                String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);
                                String created_date = DefensiveClass.optString(temp, Constants.TAG_CREATED_DATE);
                                String gift_amount = DefensiveClass.optString(temp, Constants.TAG_GIFT_AMOUNT);
                                String used_amount = DefensiveClass.optString(temp, Constants.TAG_USED_AMOUNT);
                                String voucher_code = DefensiveClass.optString(temp, Constants.TAG_VOUCHER_CODE);
                                String currency = DefensiveClass.optString(temp, Constants.TAG_CURRENCY);

                                HashMap<String, String> map = new HashMap<>();

                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_MESSAGE, message);
                                map.put(Constants.TAG_CREATED_DATE, created_date);
                                map.put(Constants.TAG_GIFT_AMOUNT, gift_amount);
                                map.put(Constants.TAG_USED_AMOUNT, used_amount);
                                map.put(Constants.TAG_VOUCHER_CODE, voucher_code);
                                map.put(Constants.TAG_CURRENCY, currency);

                                giftAry.add(map);
                            }

                            if (mScrollListener != null && giftAry.size() >= Constants.OVERALL_LIMIT) {
                                mScrollListener.setLoading(false);
                            }
                        }

                        if (giftAry.size() == 0) {
                            nullLay.setVisibility(View.VISIBLE);
                        } else {
                            nullLay.setVisibility(View.GONE);
                        }
                        recyclerViewAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "getReceivedDataError: " + error.getMessage());
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }
                    mSwipeRefreshLayout.setEnabled(false);
                    if (giftAry.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (giftAry.size() >= Constants.OVERALL_LIMIT) {
                                giftAry.add(null);
                                recyclerViewAdapter.notifyItemInserted(giftAry.size() - 1);
                            } else if (giftAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                                progressLay.setVisibility(View.VISIBLE);
                                mSwipeRefreshLayout.setEnabled(false);
                            }
                            if (mScrollListener != null) {
                                mScrollListener.setLoading(true);
                            }
                        }
                    });

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("user_id", "289");
                    map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                    map.put("offset", Integer.toString(offset));
                    Log.v(TAG, "getReceivedDataParams=" + map);
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
    }

    public static class ReceivedViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;

        public ReceivedViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public int getItemViewType(int position) {
            return Items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gift_received_list_item, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof MyViewHolder) {
                MyViewHolder holder = (MyViewHolder) viewHolder;
                HashMap<String, String> tempMap = Items.get(position);

                holder.userName.setText(tempMap.get(Constants.TAG_USER_NAME));
                holder.message.setText(tempMap.get(Constants.TAG_MESSAGE));
                holder.total.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_GIFT_AMOUNT));
                holder.used.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_USED_AMOUNT));
                holder.code.setText(tempMap.get(Constants.TAG_VOUCHER_CODE));

                float avail = Float.parseFloat(tempMap.get(Constants.TAG_GIFT_AMOUNT)) - Float.parseFloat(tempMap.get(Constants.TAG_USED_AMOUNT));
                holder.available.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.decimal.format(avail));
                if (!tempMap.get(Constants.TAG_CREATED_DATE).equals(null) && !tempMap.get(Constants.TAG_CREATED_DATE).equals("")) {
                    long date = Long.parseLong(tempMap.get(Constants.TAG_CREATED_DATE)) * 1000;
                    holder.date.setText(FantacyApplication.getDate(date));
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

            TextView userName, date, total, used, available, code, message;

            public MyViewHolder(View view) {
                super(view);
                userName = (TextView) view.findViewById(R.id.userName);
                message = (TextView) view.findViewById(R.id.message);
                date = (TextView) view.findViewById(R.id.date);
                total = (TextView) view.findViewById(R.id.total);
                used = (TextView) view.findViewById(R.id.used);
                available = (TextView) view.findViewById(R.id.available);
                code = (TextView) view.findViewById(R.id.code);

                code.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.code:
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(R.string.app_name + "voucher no", (code.getText().toString()));
                        clipboard.setPrimaryClip(clip);
                        FantacyApplication.showToast(context, context.getString(R.string.vouchernumcopied), Toast.LENGTH_SHORT);
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
