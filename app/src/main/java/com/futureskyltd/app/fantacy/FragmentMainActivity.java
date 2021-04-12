package com.futureskyltd.app.fantacy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.helper.FragmentChangeListener;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static android.Manifest.permission.CAMERA;

public class FragmentMainActivity extends BaseActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, FragmentChangeListener,
        NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    Toolbar toolbar;
    ListView listView;
    NavigationView navigationView;
    DrawerLayout drawer;
    Fragment mContent;
    FrameLayout searchLay;
    EditText searchView;
    Snackbar snackbar;
    Display display;
    private SharedPreferences preferences;
    private String accesstoken, customerId="", customerName="";
    ProgressBar creditProgress;
    RelativeLayout messageBadgeLay, notifyBadgeLay, notiBadgeLay, cartBadgeLay, userLay, credit_Lay, feedsBadgeLay;
    TextView title, saveBtn, messageBadgeCount, notifyBadgeCount, notiBadgeCount, cartBadgeCount, credit, feedsCount;
    ImageView navBtn, likedBtn, cartBtn, notifyBtn, homeBtn, appName, searchBtn, barcode, messageBadgeBg, notifyBadgeBg, feedsBadgeBg;
    public static ImageView userImage;
    public static TextView userName;
    public static Context context;
    int exit = 0;
    public static boolean orders = false;
    public static String messageCount = "0", notifyCount = "0", cartCount = "0", creditAmount = "0", currency = "$", feedCount = "0";
    private LinearLayout usrLayout;
    NetworkReceiver networkReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_main);
        context = this;
        getAuthUserInfo();
        networkReceiver = new NetworkReceiver();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        listView = (ListView) findViewById(R.id.nav_menu_listview);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        searchView = (EditText) findViewById(R.id.searchView);
        navBtn = (ImageView) findViewById(R.id.navBtn);
        likedBtn = (ImageView) findViewById(R.id.likedBtn);
        cartBtn = (ImageView) findViewById(R.id.cartBtn);
        notifyBtn = (ImageView) findViewById(R.id.notifyBtn);
        appName = (ImageView) findViewById(R.id.appName);
        searchLay = (FrameLayout) findViewById(R.id.searchLay);
        title = (TextView) findViewById(R.id.title);
        searchBtn = (ImageView) findViewById(R.id.searchBtn);
        saveBtn = (TextView) findViewById(R.id.saveBtn);
        barcode = (ImageView) findViewById(R.id.barcode);
        notiBadgeLay = (RelativeLayout) findViewById(R.id.notiBadgeLay);
        notiBadgeCount = (TextView) findViewById(R.id.notiBadgeCount);
        cartBadgeCount = (TextView) findViewById(R.id.cartBadgeCount);
        cartBadgeLay = (RelativeLayout) findViewById(R.id.cartBadgeLay);

        View header = navigationView.getHeaderView(0);

        homeBtn = (ImageView) header.findViewById(R.id.homeBtn);
        credit_Lay = (RelativeLayout) header.findViewById(R.id.credit_Lay);
        userImage = (ImageView) header.findViewById(R.id.userImage);
        usrLayout = (LinearLayout) header.findViewById(R.id.usrLayout);
        userName = (TextView) header.findViewById(R.id.userName);
        creditProgress = (ProgressBar) header.findViewById(R.id.creditProgress);
        credit = (TextView) header.findViewById(R.id.credit);
        display = this.getWindowManager().getDefaultDisplay();
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);
        customerId = preferences.getString("customer_id", null);
        customerName = preferences.getString("customer_name", null);
        Log.d(TAG, "onResponseAuth: "+customerId+"......"+customerName);


        if (getIntent().getStringExtra("from") != null && getIntent().getStringExtra("from").equals("block")) {
            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(login);
        }

        if (savedInstanceState != null)
            mContent = getSupportFragmentManager().getFragment(
                    savedInstanceState, "mContent");
        if (getIntent().getStringExtra("fromNotification") != null) {
            switch (getIntent().getStringExtra("fromNotification")) {
                case "alert":
                case "group_gift":
                    switchContent(new AlertsFragment());
                    break;
                case "admin":
                    switchContent(new FeedsFragment());
                    break;
                case "seller_message":
                    switchContent(new MessageFragment());
                    break;
            }
        } else if (mContent == null) {
            switchContent(new HomeFragment());
        }

        navigationView.setNavigationItemSelectedListener(this);

        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getBadgeCount();
                //getCreditCurrency();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        drawer.post(new Runnable() {
            @Override
            public void run() {
                toggle.syncState();
            }
        });

        snackbar = Snackbar.make(drawer, getString(R.string.exit_msg), Snackbar.LENGTH_SHORT);
        snackbar.addCallback(new Snackbar.Callback() {

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                exit = 0;
            }

            @Override
            public void onShown(Snackbar snackbar) {
                exit = 1;
            }
        });

        navigationView.post(new Runnable() {

            @Override
            public void run() {
                Resources r = getResources();
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int width = metrics.widthPixels;

                float screenWidth = width / r.getDisplayMetrics().density;
                float navWidth = screenWidth - 56;

                navWidth = Math.min(navWidth, 320);

                int newWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, navWidth, r.getDisplayMetrics());

                DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) navigationView.getLayoutParams();
                params.width = newWidth;
                navigationView.setLayoutParams(params);
            }
        });

        /*if (GetSet.isLogged()) {
            if (LoginActivity.logedfirst.equalsIgnoreCase("yes")) {
                LoginActivity.logedfirst = "no";
                Intent i = new Intent(this, WelcomeScreen.class);
                i.putExtra("type", "first");
                this.startActivity(i);
            }
            userName.setText(GetSet.getFullName());
            if (!GetSet.getImageUrl().equals("")) {
                Picasso.get().load(GetSet.getImageUrl()).into(userImage);
            }
        } else {
            userName.setText(getString(R.string.guest));
        }*/
        if(accesstoken != null){

            userName.setText(customerName);/// get from auth user data////
        }

        navBtn.setOnClickListener(this);
        homeBtn.setOnClickListener(this);
        credit_Lay.setOnClickListener(this);
        notifyBtn.setOnClickListener(this);
        userImage.setOnClickListener(this);
        usrLayout.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        cartBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        barcode.setOnClickListener(this);
        likedBtn.setOnClickListener(this);

        setBadgeCounter();

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getAuthUserInfo() {
        if(accesstoken != null){



        }
    }

    private void setBadgeCounter() {
        messageBadgeLay = (RelativeLayout) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.message));
        notifyBadgeLay = (RelativeLayout) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.notification));
        feedsBadgeLay = (RelativeLayout) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.liveFeeds));
        if (accesstoken != null) {
            // Message badge count
            if (!FragmentMainActivity.messageCount.equals("0") && !FragmentMainActivity.messageCount.equals("")) {
                messageBadgeLay.setVisibility(View.VISIBLE);
                messageBadgeCount = (TextView) messageBadgeLay.findViewById(R.id.badgeCount);
                messageBadgeBg = (ImageView) messageBadgeLay.findViewById(R.id.badgeBg);
                messageBadgeBg.setColorFilter(Color.parseColor("#0BAFFF"));
                messageBadgeCount.setText(messageCount);
            } else {
                messageBadgeLay.setVisibility(View.INVISIBLE);
            }

            // Notification badge count
            if (!FragmentMainActivity.notifyCount.equals("0") && !FragmentMainActivity.notifyCount.equals("")) {
                notifyBadgeLay.setVisibility(View.VISIBLE);
                notifyBadgeCount = (TextView) notifyBadgeLay.findViewById(R.id.badgeCount);
                notifyBadgeBg = (ImageView) notifyBadgeLay.findViewById(R.id.badgeBg);
                notifyBadgeBg.setColorFilter(Color.parseColor("#FF4100"));
                notifyBadgeCount.setText(notifyCount);

                if (notifyBtn.getVisibility() == View.VISIBLE) {
                    notiBadgeLay.setVisibility(View.VISIBLE);
                    notiBadgeCount.setText(notifyCount);
                } else {
                    notiBadgeLay.setVisibility(View.INVISIBLE);
                }
            } else {
                notifyBadgeLay.setVisibility(View.INVISIBLE);
                notiBadgeLay.setVisibility(View.INVISIBLE);
            }

            // LiveFeeds badge count
            if (!FragmentMainActivity.feedCount.equals("0") && !FragmentMainActivity.feedCount.equals("")) {
                feedsBadgeLay.setVisibility(View.VISIBLE);
                feedsCount = (TextView) feedsBadgeLay.findViewById(R.id.badgeCount);
                feedsBadgeBg = (ImageView) feedsBadgeLay.findViewById(R.id.badgeBg);
                feedsBadgeBg.setColorFilter(Color.parseColor("#B3B3B3"));
                feedsCount.setText(feedCount);
            } else {
                feedsBadgeLay.setVisibility(View.INVISIBLE);
            }

            // Cart badge count
            if (!FragmentMainActivity.cartCount.equals("0") && !FragmentMainActivity.cartCount.equals("")) {
                if (cartBtn.getVisibility() == View.VISIBLE) {
                    cartBadgeLay.setVisibility(View.VISIBLE);
                    cartBadgeCount.setText(cartCount);
                } else {
                    cartBadgeLay.setVisibility(View.INVISIBLE);
                }
            } else {
                cartBadgeLay.setVisibility(View.INVISIBLE);
            }

            creditProgress.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            creditProgress.setVisibility(View.GONE);
        } else {
            messageBadgeLay.setVisibility(View.INVISIBLE);
            notifyBadgeLay.setVisibility(View.INVISIBLE);
            notiBadgeLay.setVisibility(View.INVISIBLE);
            feedsBadgeLay.setVisibility(View.INVISIBLE);
            cartBadgeLay.setVisibility(View.INVISIBLE);
            creditProgress.setVisibility(View.GONE);
        }
    }

    public void getBadgeCount() {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_COUNTS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getBadgeCountRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        FragmentMainActivity.messageCount = DefensiveClass.optString(json, Constants.TAG_MESSAGE_COUNT);
                        FragmentMainActivity.notifyCount = DefensiveClass.optString(json, Constants.TAG_NOTIFICATION_COUNT);
                        FragmentMainActivity.feedCount = DefensiveClass.optString(json, Constants.TAG_FEEDS_COUNT);
                        FragmentMainActivity.cartCount = DefensiveClass.optString(json, Constants.TAG_CART_COUNT);
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                            /*Intent i = new Intent(FragmentMainActivity.this, PaymentStatus.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                            /*Intent i = new Intent(FragmentMainActivity.this, PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                        }
                    }
                    setBadgeCounter();
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
                Log.e(TAG, "getBadgeCountError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", FragmentMainActivity.this.customerId);
                Log.i(TAG, "getBadgeCountParams: " + map);
                Log.d(TAG, "getAuthParams: "+ FragmentMainActivity.this.customerId);
                return map;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
/*
                headers.put("Authorization", "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJodHRwczpcL1wvbGFhbHNvYnVqLmNvbSIsInN1YiI6ImxhYWxzb2J1ai5jb20iLCJpYXQiOjE2MTMzODE1NTcsImV4cCI6MTYxMzk4NjM1NywiaWQiOjMwLCJlbWFpbCI6Im1vaGFtbWFkc2hvYm9vekBnbWFpbC5jb20ifQ.AzERLlbaikHdEkvCX_93m7RAwhZgHgPodXgvP4t3tHaNnPg7KRfkJZpXONa72_-DS3KG0uJrHyeOIFQx-JYWWA");
*/
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }

            @Override
            protected void deliverResponse(String response) {
                super.deliverResponse(response);
            }
        };
        FantacyApplication.getInstance().getRequestQueue().cancelAll("count");
        FantacyApplication.getInstance().addToRequestQueue(req, "count");
    }

    public static void setCartBadge(View parent, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        TextView cartBadgeCount = (TextView) parent.findViewById(R.id.cartBadgeCount);
        RelativeLayout cartBadgeLay = (RelativeLayout) parent.findViewById(R.id.cartBadgeLay);
        if (accesstoken != null && !FragmentMainActivity.cartCount.equals("0") && !FragmentMainActivity.cartCount.equals("")) {
            cartBadgeLay.setVisibility(View.VISIBLE);
            cartBadgeCount.setText(cartCount);
        } else {
            cartBadgeLay.setVisibility(View.INVISIBLE);
        }
    }

    private void getCreditCurrency() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        if (accesstoken != null) {
            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_INVITE_HISTORY, new Response.Listener<String>() {

                @Override
                public void onResponse(String res) {
                    try {
                        Log.v(TAG, "getCreditCurrencyRes=" + res);
                        JSONObject json = new JSONObject(res);

                        String status = DefensiveClass.optString(json, Constants.TAG_STATUS);

                        if (status.equalsIgnoreCase("true")) {
                            currency = DefensiveClass.optString(json, Constants.TAG_CURRENCY);
                            creditAmount = DefensiveClass.optString(json, Constants.TAG_CREDITS);
                            // Credit amount
                            if (creditAmount.equals("")) {
                                creditAmount = "0";
                            }
                            if (currency.equals("")) {
                                currency = "$";
                            }
                            credit.setText(currency + " " + creditAmount);
                        } else if (status.equalsIgnoreCase("error")) {
                            String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                            if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                                Intent i = new Intent(FragmentMainActivity.this, PaymentStatus.class);
                                i.putExtra("from", "block");
                                startActivity(i);
                            } else {
                                Intent i = new Intent(FragmentMainActivity.this, PaymentStatus.class);
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
                    Log.e(TAG, "getCreditCurrencyError: " + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("user_id", customerId);
                    Log.i(TAG, "getCreditCurrencyParams: " + map);
                    Log.d(TAG, "getAuthParams2: "+customerId);
                    return map;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
/*
                    headers.put("Authorization", "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJodHRwczpcL1wvbGFhbHNvYnVqLmNvbSIsInN1YiI6ImxhYWxzb2J1ai5jb20iLCJpYXQiOjE2MTMzODE1NTcsImV4cCI6MTYxMzk4NjM1NywiaWQiOjMwLCJlbWFpbCI6Im1vaGFtbWFkc2hvYm9vekBnbWFpbC5jb20ifQ.AzERLlbaikHdEkvCX_93m7RAwhZgHgPodXgvP4t3tHaNnPg7KRfkJZpXONa72_-DS3KG0uJrHyeOIFQx-JYWWA");
*/
                    headers.put("Authorization", "Bearer " + accesstoken);
                    Log.d(TAG, "getHeaders: " + accesstoken);
                    return headers;
                }

            };
            FantacyApplication.getInstance().addToRequestQueue(req);
        } else {
            credit.setText(currency + " " + creditAmount);
        }
    }

    public static boolean onNavOptionSelected(Context context, int id) {
        if (id == R.id.home) {
            ((Activity) context).finish();
            Intent i = new Intent(context, FragmentMainActivity.class);
            context.startActivity(i);
            return true;
        } else if (id == R.id.find_friends) {
            Intent i = new Intent(context, FindFriends.class);
            i.putExtra("to", "findfriends");
            context.startActivity(i);
            return true;
        } else if (id == R.id.settings) {

                Intent i = new Intent(context, Settings.class);
                context.startActivity(i);

        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            if (mContent != null && mContent.isAdded())
                getSupportFragmentManager().putFragment(outState, "mContent", mContent);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    public void switchContent(Fragment fragment) {
        try {
            mContent = fragment;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().addToBackStack("back")
                    .replace(R.id.content_frame, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static void switchFragment(Context context, Fragment fragment) {
        try {
            ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction()
                    .add(fragment, "mContent")
                    .addToBackStack("back")
                    .replace(R.id.content_frame, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setNavSelectionItem(int id, String pageName) {
        // Set navigation view selectable item from id
        navigationView.setCheckedItem(id);

        // Condition for enable/disable toolbar scrolling to allow required pages
        if (id == R.id.liked || id == R.id.collection || id == R.id.groupGift || id == R.id.giftCard
                || id == R.id.invite || id == R.id.myOrders || id == R.id.notification || id == R.id.liveFeeds) {
            AppBarLayout.LayoutParams params =
                    (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
            params.setScrollFlags(0);
        } else {
            AppBarLayout.LayoutParams params =
                    (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        }

        // Set toolbar contents for fragments
        navBtn.setVisibility(View.VISIBLE);
        searchView.setText("");
        searchView.setOnClickListener(null);
        switch (pageName) {
            case "Home":
                cartBtn.setVisibility(View.VISIBLE);
                cartBadgeLay.setVisibility(View.VISIBLE);
                notifyBtn.setVisibility(View.VISIBLE);
                notiBadgeLay.setVisibility(View.VISIBLE);
                searchBtn.setVisibility(View.GONE);
                likedBtn.setVisibility(View.VISIBLE);
                appName.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
                searchLay.setVisibility(View.VISIBLE);
                saveBtn.setVisibility(View.GONE);
                barcode.setVisibility(View.VISIBLE);
                searchView.setFocusable(false);
                searchView.setHint(getString(R.string.search_products));
                searchView.setOnClickListener(this);
                break;
            case "Category":
                cartBtn.setVisibility(View.VISIBLE);
                cartBadgeLay.setVisibility(View.VISIBLE);
                notifyBtn.setVisibility(View.VISIBLE);
                notiBadgeLay.setVisibility(View.VISIBLE);
                searchBtn.setVisibility(View.GONE);
                likedBtn.setVisibility(View.VISIBLE);
                appName.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
                searchLay.setVisibility(View.VISIBLE);
                saveBtn.setVisibility(View.GONE);
                barcode.setVisibility(View.VISIBLE);
                searchView.setFocusable(false);
                searchView.setHint(getString(R.string.search_products));
                searchView.setOnClickListener(this);
                break;
            case "Message":
                cartBtn.setVisibility(View.VISIBLE);
                cartBadgeLay.setVisibility(View.VISIBLE);
                notifyBtn.setVisibility(View.GONE);
                notiBadgeLay.setVisibility(View.INVISIBLE);
                searchBtn.setVisibility(View.VISIBLE);
                likedBtn.setVisibility(View.GONE);
                appName.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(R.string.message);
                searchLay.setVisibility(View.VISIBLE);
                saveBtn.setVisibility(View.GONE);
                barcode.setVisibility(View.GONE);
                searchView.setFocusableInTouchMode(true);
                searchView.setHint(getString(R.string.search_message));
                break;
            case "Liked":
                cartBtn.setVisibility(View.VISIBLE);
                cartBadgeLay.setVisibility(View.VISIBLE);
                notifyBtn.setVisibility(View.GONE);
                notiBadgeLay.setVisibility(View.INVISIBLE);
                searchBtn.setVisibility(View.VISIBLE);
                likedBtn.setVisibility(View.GONE);
                appName.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(R.string.liked);
                searchLay.setVisibility(View.GONE);
                saveBtn.setVisibility(View.GONE);
                break;
            case "Collection":
                cartBtn.setVisibility(View.VISIBLE);
                cartBadgeLay.setVisibility(View.VISIBLE);
                notifyBtn.setVisibility(View.GONE);
                notiBadgeLay.setVisibility(View.INVISIBLE);
                searchBtn.setVisibility(View.VISIBLE);
                likedBtn.setVisibility(View.GONE);
                appName.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(R.string.collection);
                searchLay.setVisibility(View.GONE);
                saveBtn.setVisibility(View.GONE);
                break;
            case "GroupGift":
                cartBtn.setVisibility(View.VISIBLE);
                cartBadgeLay.setVisibility(View.VISIBLE);
                notifyBtn.setVisibility(View.GONE);
                notiBadgeLay.setVisibility(View.INVISIBLE);
                searchBtn.setVisibility(View.VISIBLE);
                likedBtn.setVisibility(View.GONE);
                appName.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(R.string.group_gift);
                searchLay.setVisibility(View.GONE);
                saveBtn.setVisibility(View.GONE);
                break;
            case "GiftCard":
                cartBtn.setVisibility(View.GONE);
                cartBadgeLay.setVisibility(View.GONE);
                notifyBtn.setVisibility(View.GONE);
                notiBadgeLay.setVisibility(View.INVISIBLE);
                searchBtn.setVisibility(View.GONE);
                likedBtn.setVisibility(View.GONE);
                appName.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(R.string.gift_card);
                searchLay.setVisibility(View.GONE);
                saveBtn.setVisibility(View.VISIBLE);
                saveBtn.setText(getString(R.string.history));
                break;
            case "Invite":
                cartBtn.setVisibility(View.GONE);
                cartBadgeLay.setVisibility(View.GONE);
                notifyBtn.setVisibility(View.GONE);
                notiBadgeLay.setVisibility(View.INVISIBLE);
                searchBtn.setVisibility(View.GONE);
                likedBtn.setVisibility(View.GONE);
                appName.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(R.string.invite_earn);
                searchLay.setVisibility(View.GONE);
                saveBtn.setVisibility(View.VISIBLE);
                saveBtn.setText(getString(R.string.history));
                break;
            case "AllStores":
                cartBtn.setVisibility(View.VISIBLE);
                cartBadgeLay.setVisibility(View.VISIBLE);
                notifyBtn.setVisibility(View.GONE);
                notiBadgeLay.setVisibility(View.INVISIBLE);
                searchBtn.setVisibility(View.VISIBLE);
                likedBtn.setVisibility(View.GONE);
                appName.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(R.string.all_stores);
                searchLay.setVisibility(View.VISIBLE);
                saveBtn.setVisibility(View.GONE);
                barcode.setVisibility(View.GONE);
                searchView.setFocusableInTouchMode(true);
                searchView.setHint(getString(R.string.search_stores));
                break;
            case "Help":
                cartBtn.setVisibility(View.GONE);
                cartBadgeLay.setVisibility(View.GONE);
                notifyBtn.setVisibility(View.GONE);
                notiBadgeLay.setVisibility(View.INVISIBLE);
                searchBtn.setVisibility(View.GONE);
                likedBtn.setVisibility(View.GONE);
                appName.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(R.string.help);
                searchLay.setVisibility(View.GONE);
                saveBtn.setVisibility(View.GONE);
                break;
            case "MyOrders":
                cartBtn.setVisibility(View.VISIBLE);
                cartBadgeLay.setVisibility(View.VISIBLE);
                notifyBtn.setVisibility(View.GONE);
                notiBadgeLay.setVisibility(View.INVISIBLE);
                searchBtn.setVisibility(View.VISIBLE);
                likedBtn.setVisibility(View.GONE);
                appName.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(R.string.my_orders);
                searchLay.setVisibility(View.GONE);
                saveBtn.setVisibility(View.GONE);
                break;
            case "Notification":
                cartBtn.setVisibility(View.VISIBLE);
                cartBadgeLay.setVisibility(View.VISIBLE);
                notifyBtn.setVisibility(View.GONE);
                notiBadgeLay.setVisibility(View.INVISIBLE);
                searchBtn.setVisibility(View.VISIBLE);
                likedBtn.setVisibility(View.GONE);
                appName.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(R.string.notification);
                searchLay.setVisibility(View.GONE);
                saveBtn.setVisibility(View.GONE);
                break;
            case "Live Feeds":
                cartBtn.setVisibility(View.VISIBLE);
                cartBadgeLay.setVisibility(View.VISIBLE);
                notifyBtn.setVisibility(View.GONE);
                notiBadgeLay.setVisibility(View.INVISIBLE);
                searchBtn.setVisibility(View.GONE);
                likedBtn.setVisibility(View.GONE);
                appName.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(R.string.live_feeds);
                searchLay.setVisibility(View.GONE);
                saveBtn.setVisibility(View.GONE);
                break;
            case "Alerts":
                cartBtn.setVisibility(View.VISIBLE);
                cartBadgeLay.setVisibility(View.VISIBLE);
                notifyBtn.setVisibility(View.GONE);
                notiBadgeLay.setVisibility(View.INVISIBLE);
                searchBtn.setVisibility(View.GONE);
                likedBtn.setVisibility(View.GONE);
                appName.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                title.setText(R.string.notifications);
                searchLay.setVisibility(View.GONE);
                saveBtn.setVisibility(View.GONE);
                break;
        }

        // Set Badge Count when changing fragments
        setBadgeCounter();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Log.v("onNavigation", "=" + item.getTitle());
        int id = item.getItemId();
        switchFragmentByNavigation(id, item);
        drawer.closeDrawer(Gravity.LEFT);
        return true;
    }


    private void switchFragmentByNavigation(int id, MenuItem item) {
        switch (id) {
            case R.id.home:
                drawer.closeDrawer(Gravity.LEFT);
                switchContent(new HomeFragment());
                break;
            case R.id.allCategories:
                Bundle bundle = new Bundle();
                bundle.putString("from", "all");
                bundle.putInt("position", 0);
                CategoryFragment fragment = new CategoryFragment();
                fragment.setArguments(bundle);
                switchContent(fragment);
                break;
            case R.id.dailyDeals:
                item.setCheckable(false);
                Intent j = new Intent(this, ViewAllActivity.class);
                j.putExtra("from", getString(R.string.daily_deals));
                j.putExtra("key", "deals");
                startActivity(j);
                break;
            case R.id.message:
                if (accesstoken != null) {
                    switchContent(new MessageFragment());
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
            case R.id.liveFeeds:
                if (accesstoken != null) {
                    switchContent(new FeedsFragment());
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
            case R.id.notification:
                if (accesstoken != null) {
                    switchContent(new AlertsFragment());
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
            case R.id.liked:
                if (accesstoken != null) {
                    switchContent(LikedItems.newInstance("menu", customerId));
                    Log.d(TAG, "getAuthLiked: "+customerId);
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
            case R.id.collection:
                if (accesstoken != null) {
                    switchContent(Collections.newInstance("menu", customerId));
                    Log.d(TAG, "getAuthCollection: "+customerId);
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
            case R.id.groupGift:
                if (accesstoken != null) {
                    switchContent(new GroupGiftList());
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
            case R.id.giftCard:
                if (accesstoken != null) {
                    switchContent(new GiftCard());
                    Toast.makeText(this, "Under Construction !", Toast.LENGTH_SHORT).show();
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
            case R.id.invite:
                if (accesstoken != null) {
                    switchContent(new InviteFragment());
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
            case R.id.allStores:
                switchContent(new AllStores());
                break;
            case R.id.help:
                switchContent(new Help());
                break;
            case R.id.rateApp:
               /* item.setCheckable(false);
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }*/

                Toast.makeText(context, "Apps Not Publish Yet! Please Wait for Some Days!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.myOrders:
                if (accesstoken != null) {
                    switchContent(new MyOrders());
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, intentFilter);
        FantacyApplication.showSnack(this, drawer, NetworkReceiver.isConnected());
        if (orders) {
            orders = false;
            switchContent(new MyOrders());
        }

        if (accesstoken != null) {
            getBadgeCount();
            Log.d(TAG, "onResume: " +accesstoken);
        } else {
            setBadgeCounter();
        }
        getCreditCurrency();
    }

    @Override
    protected void onPause() {
        FantacyApplication.showSnack(this, drawer, true);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (networkReceiver != null) {
            // unregister receiver
            unregisterReceiver(networkReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.i(TAG, "onNetworkConnectionChanged: " + isConnected);
        FantacyApplication.showSnack(this, drawer, isConnected);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent b = new Intent(this, BarcodeScanner.class);
                    startActivity(b);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.need_camera_to_access), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 100);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(Gravity.LEFT)) {
            drawer.closeDrawer(Gravity.LEFT);
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            if (exit == 0) {
                snackbar.show();
            } else {
                FragmentMainActivity.this.finishAffinity();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchView:
                Intent i = new Intent(FragmentMainActivity.this, RecentSearch.class);
                startActivity(i);
                break;
            case R.id.navBtn:
                FantacyApplication.hideSoftKeyboard(this, searchView);
                drawer.openDrawer(Gravity.LEFT);
                break;
            case R.id.homeBtn:
                drawer.closeDrawer(Gravity.LEFT);
                switchContent(new HomeFragment());
                break;
            case R.id.credit_Lay:
                if (accesstoken != null) {
                    switchContent(new InviteFragment());
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                drawer.closeDrawer(Gravity.LEFT);
                break;
            case R.id.notifyBtn:
                if (accesstoken != null) {
                    switchContent(new AlertsFragment());
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
            case R.id.userImage:
                if (accesstoken != null) {
                    Intent p = new Intent(this, Profile.class);
                    p.putExtra("userId", customerId);
                    startActivity(p);
                    Log.d(TAG, "getAuthUserImage: "+customerId);
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }

                break;
            case R.id.usrLayout:
                if (accesstoken != null) {
                    Intent p = new Intent(this, Profile.class);
                    p.putExtra("userId", customerId);
                    startActivity(p);
                    Log.d(TAG, "getAuthUsrLayout: "+customerId);
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
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
            case R.id.barcode:
                if (ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{CAMERA}, 100);
                } else {
                    Intent b = new Intent(this, BarcodeScanner.class);
                    startActivity(b);
                }
                break;
            case R.id.likedBtn:
                if (accesstoken != null) {
                    switchContent(LikedItems.newInstance("menu", customerId));
                    Log.d(TAG, "getAuthLikedBtn "+customerId);
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
            case R.id.searchBtn:
                Intent s = new Intent(FragmentMainActivity.this, RecentSearch.class);
                startActivity(s);
                break;
        }
    }
}