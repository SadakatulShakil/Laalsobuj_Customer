package com.futureskyltd.app.fantacy;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.Api.ApiInterface;
import com.futureskyltd.app.Api.RetrofitClient;
import com.futureskyltd.app.ApiPojo.CustomerProfile.CustomerProfile;
import com.futureskyltd.app.ApiPojo.GeneralLogIn.GeneralLogIn;
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

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

/**
 * Created by hitasoft on 17/6/17.
 */

public class Profile extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, cartBtn, searchBtn, edit, backgroundImage, userImage;
    TextView userName, location, followBtn;
    String accesstoken, customerId, customerName;
    TabLayout tabLayout;
    ViewPager viewPager;
    String userId = "", username = "", usernameURL = "";
    DatabaseHandler helper;
    private CustomerProfile customerProfile;
    HashMap<String, String> profileMap = new HashMap<String, String>();
    SharedPreferences preferences;
    Map<String, Map<String, String>> getRetMainMap;
    private String localCartCount ="0";

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(Constants.isNotification){
            Intent i = new Intent(this,FragmentMainActivity.class);
            startActivity(i);
        }
        Constants.isNotification=false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.isNotification =false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_main_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
         accesstoken = preferences.getString("TOKEN", null);
         customerId = preferences.getString("customer_id", null);
         customerName = preferences.getString("customer_name",null);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        cartBtn = (ImageView) findViewById(R.id.cartBtn);
        searchBtn = (ImageView) findViewById(R.id.searchBtn);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        edit = (ImageView) findViewById(R.id.edit);
        backgroundImage = (ImageView) findViewById(R.id.backgroundImage);
        userImage = (ImageView) findViewById(R.id.userImage);
        userName = (TextView) findViewById(R.id.userName);
        location = (TextView) findViewById(R.id.location);
        followBtn = (TextView) findViewById(R.id.followBtn);

        backBtn.setVisibility(View.VISIBLE);
        cartBtn.setVisibility(View.VISIBLE);
        searchBtn.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        edit.setOnClickListener(this);
        followBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        cartBtn.setOnClickListener(this);

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
        FragmentMainActivity.setCartBadge(findViewById(R.id.parentLay),Profile.this);

        userId = (String) getIntent().getExtras().get("userId");
        if (userId.equals("")) {
            if (getIntent().getExtras().get("userName") != null) {
                username = (String) getIntent().getExtras().get("userName");
            } else if (getIntent().getExtras().get(Constants.TAG_USERNAME_URL) != null) {
                usernameURL = (String) getIntent().getExtras().get(Constants.TAG_USERNAME_URL);
            }
        } else {
            setupViewPager(viewPager);
            tabLayout.setupWithViewPager(viewPager);
            changeTabsFont();
        }

        helper = DatabaseHandler.getInstance(this);

        if ((accesstoken != null && userId.equals(customerId)) || accesstoken != null && username.equals(customerName)) {
            edit.setVisibility(View.VISIBLE);
            followBtn.setVisibility(View.INVISIBLE);
        } else {
            edit.setVisibility(View.INVISIBLE);
            followBtn.setVisibility(View.VISIBLE);
        }

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

   /* private void getProfile() {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PROFILE, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    Log.d(TAG, "getProfileRes" + res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);
                        if (userId.equals("")) {
                            userId = DefensiveClass.optString(result, Constants.TAG_USER_ID);
                            if ((accesstoken != null && userId.equals(customerId))) {
                                edit.setVisibility(View.VISIBLE);
                                followBtn.setVisibility(View.INVISIBLE);
                            } else {
                                edit.setVisibility(View.INVISIBLE);
                                followBtn.setVisibility(View.VISIBLE);
                            }

                            setupViewPager(viewPager);
                            tabLayout.setupWithViewPager(viewPager);

                            changeTabsFont();
                        }

                        userId = DefensiveClass.optString(result, Constants.TAG_USER_ID);
                        String full_name = DefensiveClass.optString(result, Constants.TAG_FULL_NAME);
                        String user_name = DefensiveClass.optString(result, Constants.TAG_USER_NAME);
                        String haspassword = DefensiveClass.optString(result, Constants.TAG_HAS_PASSWORD);
                        String user_image = DefensiveClass.optString(result, Constants.TAG_USER_IMAGE);
                        String follow_status = DefensiveClass.optString(result, Constants.TAG_FOLLOW_STATUS);
                        String following = DefensiveClass.optString(result, Constants.TAG_FOLLOWING);
                        String followers = DefensiveClass.optString(result, Constants.TAG_FOLLOWERS);
                        String follow_stores = DefensiveClass.optString(result, Constants.TAG_FOLLOW_STORES);
                        String liked_count = DefensiveClass.optString(result, Constants.TAG_LIKED_COUNT);
                        String collection_count = DefensiveClass.optString(result, Constants.TAG_COLLECTION_COUNT);
                        String credits = DefensiveClass.optString(result, Constants.TAG_CREDITS);
                        String email = DefensiveClass.optString(result, Constants.TAG_EMAIL);

                        profileMap.put(Constants.TAG_USER_ID, userId);
                        profileMap.put(Constants.TAG_FULL_NAME, full_name);
                        profileMap.put(Constants.TAG_HAS_PASSWORD, haspassword);
                        profileMap.put(Constants.TAG_USER_NAME, user_name);
                        profileMap.put(Constants.TAG_USER_IMAGE, user_image);
                        profileMap.put(Constants.TAG_FOLLOW_STATUS, follow_status);
                        profileMap.put(Constants.TAG_FOLLOWING, following);
                        profileMap.put(Constants.TAG_FOLLOWERS, followers);
                        profileMap.put(Constants.TAG_FOLLOW_STORES, follow_stores);
                        profileMap.put(Constants.TAG_LIKED_COUNT, liked_count);
                        profileMap.put(Constants.TAG_COLLECTION_COUNT, collection_count);
                        profileMap.put(Constants.TAG_CREDITS, credits);
                        profileMap.put(Constants.TAG_EMAIL, email);

                        if (!user_image.equals("")) {
                            Picasso.get().load(user_image).error(R.mipmap.appicon_round).placeholder(R.mipmap.appicon_round).into(userImage);
                            Picasso.get().load(user_image).error(R.mipmap.appicon_round).placeholder(R.mipmap.appicon_round).into(backgroundImage);
                        }

                        userName.setText(full_name);
                        location.setText(user_name);

                        if (follow_status.equalsIgnoreCase("follow")) {
                            followBtn.setText(getString(R.string.follow));
                        } else {
                            followBtn.setText(getString(R.string.following));
                        }

                        if (userId.equals(customerId)) {
                            FantacyApplication.pref = getApplicationContext().getSharedPreferences("FantacyPref",
                                    MODE_PRIVATE);
                            FantacyApplication.editor = FantacyApplication.pref.edit();

                            FantacyApplication.editor.putString("userImage", user_image);
                            FantacyApplication.editor.putString("fullName", full_name);
                            FantacyApplication.editor.commit();

                            GetSet.setImageUrl(user_image);
                            GetSet.setFullName(full_name);

                            if (FragmentMainActivity.userImage != null && FragmentMainActivity.userName != null) {
                                Picasso.get().load(GetSet.getImageUrl()).placeholder(R.mipmap.appicon_round).error(R.mipmap.appicon_round).into(FragmentMainActivity.userImage);
                                FragmentMainActivity.userName.setText(GetSet.getFullName());
                            }
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           *//* finish();
                            if (PaymentStatus.activity == null) {
                                Intent i = new Intent(Profile.this, PaymentStatus.class);
                                if (userId.equals(GetSet.getUserId())) {
                                    i.putExtra("from", "block");
                                } else {
                                    i.putExtra("from", "other_user_block");
                                }
                                startActivity(i);
                            }*//*
                        } else {
                            *//*Intent i = new Intent(Profile.this, PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);
                            finish();*//*
                        }
                    } else if (status.equalsIgnoreCase("false")) {
                        *//*if (PaymentStatus.activity == null) {
                            Intent i = new Intent(Profile.this, PaymentStatus.class);
                            if (userId.equals(GetSet.getUserId())) {
                                i.putExtra("from", "delete");
                            } else {
                                i.putExtra("from", "other_user_delete");
                            }
                            startActivity(i);
                            finish();
                        }*//*
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
                if (userId.equals("")) {
                    if (!usernameURL.equals("")) {
                        map.put(Constants.TAG_USERNAME_URL, usernameURL);
                    } else {
                        map.put("user_name", username);
                    }
                } else {
                    map.put("other_user_id", userId);
                }
                if (accesstoken !=null) {
                    map.put("logged_user_id", customerId);
                }
                Log.v(TAG, "getProfileParams=" + map);
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
    }*/

    private void followUser(final Boolean follow) {

        String CallingAPI;
        if (follow) // Follow Api
            CallingAPI = Constants.API_FOLLOW_USER;
        else  // Unfollow Api
            CallingAPI = Constants.API_UNFOLLOW_USER;

        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        if (follow) {
                            profileMap.put(Constants.TAG_STATUS, "follow");
                            helper.addUserDetails(profileMap.get(Constants.TAG_USER_ID), "follow");
                            followBtn.setText(getString(R.string.follow));
                        } else {
                            profileMap.put(Constants.TAG_STATUS, "unfollow");
                            helper.addUserDetails(profileMap.get(Constants.TAG_USER_ID), "unfollow");
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
                Log.e(TAG, "followUserError: " + error.getMessage());
                if (follow) {
                    profileMap.put(Constants.TAG_STATUS, "follow");
                    helper.addUserDetails(profileMap.get(Constants.TAG_USER_ID), "follow");
                    followBtn.setText(getString(R.string.follow));
                } else {
                    profileMap.put(Constants.TAG_STATUS, "unfollow");
                    helper.addUserDetails(profileMap.get(Constants.TAG_USER_ID), "unfollow");
                    followBtn.setText(getString(R.string.following));
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("follow_id", userId);
                Log.i(TAG, "followUserParams= " + map);
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
        adapter.addFragment(LikedItems.newInstance("profile", userId), getString(R.string.wishlist));
        adapter.addFragment(Collections.newInstance("profile", userId), getString(R.string.collection));
        adapter.addFragment(Followers.newInstance("profile", userId), getString(R.string.followers));
        adapter.addFragment(Followings.newInstance("profile", userId), getString(R.string.following));
        adapter.addFragment(FollowStore.newInstance("profile", userId), getString(R.string.stores));
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

    private void removeToken() {
        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PUSH_NOTIFICATION_UNREGISTER,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String res) {
                        try {
                            Log.v(TAG, "removeTokenRes=" + res);
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

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
                Log.e(TAG, "removeTokenError: " + error.getMessage());
            }

        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("device_id", deviceId);
                Log.v(TAG, "removeTokenParams=" + map);
                return map;
            }
        };

        FantacyApplication.getInstance().addToRequestQueue(req);
    }


    /**
     * dialog for confirm the user to signout
     **/
    public void signoutDialog() {
        final Dialog dialog = new Dialog(Profile.this);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView yes = (TextView) dialog.findViewById(R.id.yes);
        TextView no = (TextView) dialog.findViewById(R.id.no);

        title.setText(getString(R.string.reallySignOut));
        yes.setText(getString(R.string.yes));
        no.setText(getString(R.string.no));

        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
               /* SharedPreferences pref = getApplicationContext().getSharedPreferences("FantacyPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.commit();
                GetSet.reset();
                HomeFragment.resetAry();
                FragmentMainActivity.messageCount = "0";
                FragmentMainActivity.notifyCount = "0";
                FragmentMainActivity.feedCount = "0";
                FragmentMainActivity.cartCount = "0";
                FragmentMainActivity.creditAmount = "0";
                FragmentMainActivity.currency = "$";
                removeToken();*/
                SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                preferences.edit().putString("TOKEN",null).apply();
                finish();
                Intent p = new Intent(Profile.this, FragmentMainActivity.class);
                p.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(p);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (accesstoken != null/* && userId.equals(customerId)*/) {
            getMenuInflater().inflate(R.menu.profile_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.fragment_main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.address_book) {
            Intent i = new Intent(Profile.this, ShippingAddress.class);
            i.putExtra("from", "profile");
            startActivity(i);
            return true;
        } else if (id == R.id.settings) {
            if (accesstoken != null) {
                Intent i = new Intent(Profile.this, Settings.class);
                startActivity(i);
            } else {
                Intent login = new Intent(Profile.this, SignInActivity.class);
                startActivity(login);
            }
            return true;
        } else if (id == R.id.find_friends) {
            Intent i = new Intent(Profile.this, FindFriends.class);
            i.putExtra("to", "findfriends");
            startActivity(i);
            return true;
        } else if (id == R.id.dispute) {
            Intent i = new Intent(Profile.this, Dispute.class);
            startActivity(i);
            return true;
        } else if (id == R.id.home) {
            Intent i = new Intent(Profile.this, FragmentMainActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.logout) {
            signoutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        getProfile();
    }

    private void getProfile() {
        Retrofit retrofit = RetrofitClient.getRetrofitClient();
        ApiInterface api = retrofit.create(ApiInterface.class);

        Call<CustomerProfile> getProfileCall = api.postByCustomerProfile(customerId, customerId);

        getProfileCall.enqueue(new Callback<CustomerProfile>() {
            @Override
            public void onResponse(Call<CustomerProfile> call, retrofit2.Response<CustomerProfile> response) {
                if(response.code() == 200){
                    customerProfile = response.body();
                    if(customerProfile.getStatus().equals("true")){
                        userName.setText(customerProfile.getResult().getFullName());
                        Picasso.get().load(customerProfile.getResult().getUserImage()).into(userImage);
                        Picasso.get().load(customerProfile.getResult().getUserImage()).into(backgroundImage);
                    }else{
                        Toast.makeText(Profile.this, "Something is error", Toast.LENGTH_SHORT).show();

                    }
                }else {
                    Toast.makeText(Profile.this, response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CustomerProfile> call, Throwable t) {
                Toast.makeText(Profile.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                if(Constants.isNotification){
                    Intent i = new Intent(this,FragmentMainActivity.class);
                    startActivity(i);
                }
                Constants.isNotification=false;
                finish();
                break;
            case R.id.edit:
                Intent i = new Intent(this, EditProfile.class);
                i.putExtra("ProData", customerProfile);
                startActivity(i);
                break;
            case R.id.followBtn:
                if (accesstoken != null) {
                    String status = profileMap.get(Constants.TAG_FOLLOW_STATUS);
                    if (status.equalsIgnoreCase("follow")) {
                        followUser(true);
                        profileMap.put(Constants.TAG_FOLLOW_STATUS, "unfollow");
                        followBtn.setText(getString(R.string.following));
                        helper.addUserDetails(customerId, "unfollow");
                    } else {
                        followUser(false);
                        profileMap.put(Constants.TAG_FOLLOW_STATUS, "follow");
                        followBtn.setText(getString(R.string.follow));
                        helper.addUserDetails(customerId, "follow");
                    }
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
            case R.id.searchBtn:
                Intent s = new Intent(this, RecentSearch.class);
                startActivity(s);
                break;
            case R.id.cartBtn:
                if (accesstoken != null) {
                    Intent c = new Intent(this, CartActivity.class);
                    c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    c.putExtra("shippingId", "0");
                    c.putExtra("itemId", "0");
                    startActivity(c);
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
        }
    }
}
