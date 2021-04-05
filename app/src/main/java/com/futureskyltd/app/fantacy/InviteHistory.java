package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
 * Created by hitasoft on 18/7/17.
 */

public class InviteHistory extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = InviteHistory.class.getSimpleName();
    static ViewPager viewPager;
    static ArrayList<HashMap<String, String>> inviteAry = new ArrayList<HashMap<String, String>>();
    ImageView backBtn, nullImage;
    TextView title, nullText;
    //  RecyclerView recyclerView;
    RelativeLayout nullLay;
    TabLayout tabLayout;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_history_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = findViewById(R.id.title);
        backBtn = findViewById(R.id.backBtn);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        title.setText(getString(R.string.invite_history));
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        backBtn.setOnClickListener(this);
        inviteAry.clear();
        inviteAry = (ArrayList<HashMap<String, String>>) getIntent().getExtras().get("data");

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        changeTabsFont();

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Invite(), getString(R.string.invite));
        adapter.addFragment(new Refund(), getString(R.string.refund));
        viewPager.setAdapter(adapter);
    }

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

    @Override
    protected void onPause() {
        super.onPause();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
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

    public static class Invite extends Fragment {

        RecyclerView recyclerView;
        LinearLayoutManager linearLayoutManager;
        RecyclerViewAdapter recyclerViewAdapter;
        ImageView nullImage;
        TextView nullText;
        RelativeLayout nullLay;
        private EndlessRecyclerOnScrollListener mScrollListener = null;
        private SwipeRefreshLayout mSwipeRefreshLayout = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dispute_recyclerview, container, false);

            recyclerView = rootView.findViewById(R.id.recyclerView);
            nullLay = rootView.findViewById(R.id.nullLay);
            nullImage = rootView.findViewById(R.id.nullImage);
            nullText = rootView.findViewById(R.id.nullText);
            mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
            mSwipeRefreshLayout.setEnabled(false);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);

            nullText.setText(getString(R.string.no_history));
            nullImage.setImageResource(R.drawable.no_history);
            if (inviteAry.size() == 0) {
                nullLay.setVisibility(View.VISIBLE);
            }
            recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), inviteAry);
            recyclerView.setAdapter(recyclerViewAdapter);
        }
    }

    public static class Refund extends Fragment {

        static ArrayList<HashMap<String, String>> refundAry = new ArrayList<HashMap<String, String>>();
        RecyclerView recyclerView;
        LinearLayoutManager linearLayoutManager;
        RefundRecyclerViewAdapter recyclerViewAdapter;
        ImageView nullImage;
        TextView nullText;
        RelativeLayout nullLay;
        private EndlessRecyclerOnScrollListener mScrollListener = null;
        private SwipeRefreshLayout mSwipeRefreshLayout = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dispute_recyclerview, container, false);

            recyclerView = rootView.findViewById(R.id.recyclerView);
            nullLay = rootView.findViewById(R.id.nullLay);
            nullImage = rootView.findViewById(R.id.nullImage);
            nullText = rootView.findViewById(R.id.nullText);
            mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
            mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));
            refundAry.clear();
            RefundHistory();
            return rootView;
        }


        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            nullText.setText(getString(R.string.no_history));
            nullImage.setImageResource(R.drawable.no_history);

            recyclerViewAdapter = new RefundRecyclerViewAdapter(getActivity(), refundAry);
            recyclerView.setAdapter(recyclerViewAdapter);

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refundAry.clear();
                    RefundHistory();
                }
            });
        }

        private void RefundHistory() {
            SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
            String accesstoken = preferences.getString("TOKEN", null);

            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_REFUND_HISTORY, new Response.Listener<String>() {

                @Override
                public void onResponse(String res) {
                    try {
                        Log.v(TAG, "RefundHistoryRes=" + res);
                        JSONObject json = new JSONObject(res);

                        String status = DefensiveClass.optString(json, Constants.TAG_STATUS);

                        if (status.equalsIgnoreCase("true")) {

                            JSONArray result = json.optJSONArray(Constants.TAG_RESULT);
                            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                                mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                            }
                            if (result != null) {
                                for (int i = 0; i < result.length(); i++) {
                                    JSONObject temp = result.getJSONObject(i);

                                    String user_id = DefensiveClass.optString(temp, Constants.TAG_ORDER_ID);
                                    String created_date = DefensiveClass.optString(temp, Constants.TAG_REFUND_DATE);
                                    String credit_amount = DefensiveClass.optString(temp, Constants.TAG_CREDITS);

                                    HashMap<String, String> map = new HashMap<>();

                                    map.put(Constants.TAG_ORDER_ID, user_id);
                                    map.put(Constants.TAG_REFUND_DATE, created_date);
                                    map.put(Constants.TAG_CREDITS, credit_amount);

                                    refundAry.add(map);
                                }
                                recyclerViewAdapter.notifyDataSetChanged();
                            }

                        } else if (status.equalsIgnoreCase("error")) {
                            String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                            if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                                /*Intent i = new Intent(getActivity(), PaymentStatus.class);
                                i.putExtra("from", "block");
                                startActivity(i);*/
                            } else {
                                /*Intent i = new Intent(getActivity(), PaymentStatus.class);
                                i.putExtra("from", "maintenance");
                                startActivity(i);*/
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
                    Log.e(TAG, "RefundHistoryError: " + error.getMessage());
                    setErrorLayout();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("user_id", "289");
                    Log.i(TAG, "RefundHistoryParams: " + map);
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
            if (refundAry.size() == 0) {
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                }
                nullLay.setVisibility(View.VISIBLE);
            }
        }
    }


    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.invite_history_list_item, parent, false);

            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.userName.setText(tempMap.get(Constants.TAG_USER_NAME));
            holder.price.setText("$ " + tempMap.get(Constants.TAG_CREDITS));

            holder.bottomView.setVisibility(View.VISIBLE);
            if (position == Items.size() - 1) {
                holder.bottomView.setVisibility(View.GONE);
            }

            holder.topView.setVisibility(View.VISIBLE);
            if (position == 0) {
                holder.topView.setVisibility(View.GONE);
            }

            if (!tempMap.get(Constants.TAG_CREATED_DATE).equals(null) && !tempMap.get(Constants.TAG_CREATED_DATE).equals("")) {
                long date = Long.parseLong(tempMap.get(Constants.TAG_CREATED_DATE)) * 1000;
                holder.date.setText(FantacyApplication.getDate(date));
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView userName, price, date;
            View topView, bottomView;

            public MyViewHolder(View view) {
                super(view);

                userName = view.findViewById(R.id.userName);
                price = view.findViewById(R.id.price);
                date = view.findViewById(R.id.date);
                topView = view.findViewById(R.id.topView);
                bottomView = view.findViewById(R.id.bottomView);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                }
            }
        }
    }

    public static class RefundRecyclerViewAdapter extends RecyclerView.Adapter<RefundRecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public RefundRecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RefundRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.invite_history_list_item, parent, false);

            return new RefundRecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RefundRecyclerViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.userName.setText("REFUND ORDER ID: " + tempMap.get(Constants.TAG_ORDER_ID));
            holder.price.setText(FragmentMainActivity.currency + " " + tempMap.get(Constants.TAG_CREDITS));


            holder.bottomView.setVisibility(View.VISIBLE);
            if (position == Items.size() - 1) {
                holder.bottomView.setVisibility(View.GONE);
            }

            holder.topView.setVisibility(View.VISIBLE);
            if (position == 0) {
                holder.topView.setVisibility(View.GONE);
            }

            if (tempMap.get(Constants.TAG_REFUND_DATE) != null && !tempMap.get(Constants.TAG_REFUND_DATE).equals("")) {
                long date = Long.parseLong(tempMap.get(Constants.TAG_REFUND_DATE)) * 1000;
                holder.date.setText(FantacyApplication.getDate(date));
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView userName, price, date;
            View topView, bottomView;

            public MyViewHolder(View view) {
                super(view);

                userName = view.findViewById(R.id.userName);
                price = view.findViewById(R.id.price);
                date = view.findViewById(R.id.date);
                topView = view.findViewById(R.id.topView);
                bottomView = view.findViewById(R.id.bottomView);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {

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
