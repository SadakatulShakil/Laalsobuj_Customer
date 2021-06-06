package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.FontCache;
import com.futureskyltd.app.helper.DatabaseHandler;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hitasoft on 23/6/17.
 */

public class StoreProfile extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = StoreProfile.class.getSimpleName();
    ImageView backBtn, cartBtn, searchBtn, backgroundImage, userImage;
    TextView userName, location, followBtn, rating, ratingTxt;
    TabLayout tabLayout;
    ViewPager viewPager;
    String storeId = "";
    DatabaseHandler helper;
    HashMap<String, String> profileMap = new HashMap<String, String>();
    SharedPreferences preferences;
    Map<String, Map<String, String>> getRetMainMap;
    private String localCartCount ="0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_main_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        cartBtn = (ImageView) findViewById(R.id.cartBtn);
        searchBtn = (ImageView) findViewById(R.id.searchBtn);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        backgroundImage = (ImageView) findViewById(R.id.backgroundImage);
        userImage = (ImageView) findViewById(R.id.userImage);
        userName = (TextView) findViewById(R.id.userName);
        location = (TextView) findViewById(R.id.location);
        followBtn = (TextView) findViewById(R.id.followBtn);
        rating = (TextView) findViewById(R.id.rating);
        ratingTxt = (TextView) findViewById(R.id.ratingTxt);

        backBtn.setVisibility(View.VISIBLE);
        cartBtn.setVisibility(View.VISIBLE);
        searchBtn.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        followBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        cartBtn.setOnClickListener(this);

        storeId = getIntent().getExtras().getString("storeId");

        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String getLocalCart = preferences.getString("localCart", null);
        if(getLocalCart != null){

            getRetMainMap = new Gson().fromJson(
                    getLocalCart, new TypeToken<HashMap<String, Map<String, String>>>() {}.getType()
            );
            //FragmentMainActivity.cartCount = String.valueOf(getRetMainMap.values().size());
            Log.d(TAG, "addLocalCartItem3: "+ getRetMainMap+"...."+FragmentMainActivity.cartCount);

            /*localItemCount();*/
        }
        //localCartCount = String.valueOf(getRetMainMap.size());
        FragmentMainActivity.setCartBadge(findViewById(R.id.parentLay), StoreProfile.this);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        helper = DatabaseHandler.getInstance(this);

        changeTabsFont();

        getProfile();

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getProfile() {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_STORE_PROFILE, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    Log.v(TAG, "getProfileRes=" + res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);

                        String store_id = DefensiveClass.optString(result, Constants.TAG_STORE_ID);
                        String store_name = DefensiveClass.optString(result, Constants.TAG_STORE_NAME);
                        String merchant_name = DefensiveClass.optString(result, Constants.TAG_MERCHANT_NAME);
                        String store_image = DefensiveClass.optString(result, Constants.TAG_STORE_IMAGE);
                        String banner_image = DefensiveClass.optString(result, Constants.TAG_BANNER_IMAGE);
                        String store_address = DefensiveClass.optString(result, Constants.TAG_STORE_ADDRESS);
                        String lat = DefensiveClass.optString(result, Constants.TAG_LAT);
                        String lon = DefensiveClass.optString(result, Constants.TAG_LON);
                        String store_followers = DefensiveClass.optString(result, Constants.TAG_STORE_FOLLOWERS);
                        String product_count = DefensiveClass.optString(result, Constants.TAG_PRODUCT_COUNT);
                        String review_count = DefensiveClass.optString(result, Constants.TAG_REVIEW_COUNT);
                        String average_rating = DefensiveClass.optString(result, Constants.TAG_AVERAGE_RATING);
                        String follow_status = DefensiveClass.optString(result, Constants.TAG_FOLLOW_STATUS);

                        profileMap.put(Constants.TAG_STORE_ID, store_id);
                        profileMap.put(Constants.TAG_STORE_NAME, store_name);
                        profileMap.put(Constants.TAG_MERCHANT_NAME, merchant_name);
                        profileMap.put(Constants.TAG_STORE_IMAGE, store_image);
                        profileMap.put(Constants.TAG_BANNER_IMAGE, banner_image);
                        profileMap.put(Constants.TAG_STORE_ADDRESS, store_address);
                        profileMap.put(Constants.TAG_LAT, lat);
                        profileMap.put(Constants.TAG_LON, lon);
                        profileMap.put(Constants.TAG_STORE_FOLLOWERS, store_followers);
                        profileMap.put(Constants.TAG_PRODUCT_COUNT, product_count);
                        profileMap.put(Constants.TAG_REVIEW_COUNT, review_count);
                        profileMap.put(Constants.TAG_AVERAGE_RATING, average_rating);
                        profileMap.put(Constants.TAG_FOLLOW_STATUS, follow_status);

                        if (!store_image.equals("")) {
                            Picasso.get().load(store_image).error(R.drawable.temp).placeholder(R.drawable.temp).into(userImage);
                        }

                        if (!banner_image.equals("")) {
                            Picasso.get().load(banner_image).error(R.drawable.temp).placeholder(R.drawable.temp).into(backgroundImage);
                        }

                        userName.setText(store_name);
                        location.setText(store_address);

                        if (average_rating.equals("") || average_rating.equals("0")) {
                            rating.setVisibility(View.INVISIBLE);
                            ratingTxt.setVisibility(View.INVISIBLE);
                        } else {
                            rating.setText(average_rating);
                            rating.setVisibility(View.VISIBLE);
                            ratingTxt.setVisibility(View.VISIBLE);
                        }

                        if (follow_status.equalsIgnoreCase("follow")) {
                            followBtn.setText(getString(R.string.follow));
                        } else {
                            followBtn.setText(getString(R.string.following));
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                            Intent i = new Intent(StoreProfile.this, PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);
                        } else {
                            Intent i = new Intent(StoreProfile.this, PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);
                        }
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
                Log.e(TAG, "getProfileError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("store_id", storeId);
                if (GetSet.isLogged()) {
                    map.put("user_id", GetSet.getUserId());
                }
                Log.v(TAG, "getProfileParams=" + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void followStore(final Boolean follow) {

        String CallingAPI;
        if (follow) // Follow Api
            CallingAPI = Constants.API_FOLLOW_STORE;
        else  // Unfollow Api
            CallingAPI = Constants.API_UNFOLLOW_STORE;

        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "followStoreRes=" + res);
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        if (follow) {
                            profileMap.put(Constants.TAG_STATUS, "follow");
                            helper.addStoreDetails(profileMap.get(Constants.TAG_STORE_ID), "follow");
                            followBtn.setText(getString(R.string.follow));
                        } else {
                            profileMap.put(Constants.TAG_STATUS, "unfollow");
                            helper.addStoreDetails(profileMap.get(Constants.TAG_STORE_ID), "unfollow");
                            followBtn.setText(getString(R.string.following));
                        }
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
                Log.e(TAG, "followStoreError: " + error.getMessage());
                if (follow) {
                    profileMap.put(Constants.TAG_STATUS, "follow");
                    helper.addStoreDetails(profileMap.get(Constants.TAG_STORE_ID), "follow");
                    followBtn.setText(getString(R.string.follow));
                } else {
                    profileMap.put(Constants.TAG_STATUS, "unfollow");
                    helper.addStoreDetails(profileMap.get(Constants.TAG_STORE_ID), "unfollow");
                    followBtn.setText(getString(R.string.following));
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("store_id", storeId);
                Log.i(TAG, "followStoreParams: " + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
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
        adapter.addFragment(StoreProducts.newInstance("store", storeId), getString(R.string.products));
        adapter.addFragment(StoreReview.newInstance("store", storeId), getString(R.string.review));
        adapter.addFragment(StoreNews.newInstance("store", storeId), getString(R.string.news));
        adapter.addFragment(Followers.newInstance("store", storeId), getString(R.string.followers));
        viewPager.setAdapter(adapter);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fragment_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return FragmentMainActivity.onNavOptionSelected(this, id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
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
            case R.id.followBtn:
                if (GetSet.isLogged()) {
                    String status = profileMap.get(Constants.TAG_FOLLOW_STATUS);
                    if (status.equalsIgnoreCase("follow")) {
                        followStore(true);
                        profileMap.put(Constants.TAG_FOLLOW_STATUS, "unfollow");
                        followBtn.setText(getString(R.string.following));
                        helper.addStoreDetails(profileMap.get(Constants.TAG_STORE_ID), "unfollow");
                    } else {
                        followStore(false);
                        profileMap.put(Constants.TAG_FOLLOW_STATUS, "follow");
                        followBtn.setText(getString(R.string.follow));
                        helper.addStoreDetails(profileMap.get(Constants.TAG_STORE_ID), "follow");
                    }
                } else {
                    Intent login = new Intent(StoreProfile.this, LoginActivity.class);
                    startActivity(login);
                }
                break;
            case R.id.searchBtn:
                Intent s = new Intent(this, RecentSearch.class);
                startActivity(s);
                break;
            case R.id.cartBtn:
                if (GetSet.isLogged()) {
                    Intent c = new Intent(this, CartActivity.class);
                    c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    c.putExtra("shippingId", "0");
                    c.putExtra("itemId", "0");
                    startActivity(c);
                } else {
                    Intent login = new Intent(this, LoginActivity.class);
                    startActivity(login);
                }
                break;
        }
    }
}
