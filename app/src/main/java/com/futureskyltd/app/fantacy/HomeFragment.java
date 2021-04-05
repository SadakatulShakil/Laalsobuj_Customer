package com.futureskyltd.app.fantacy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.AutoScrollViewPager;
import com.futureskyltd.app.external.CornerImageView;
import com.futureskyltd.app.external.RecyclerItemClickListener;
import com.futureskyltd.app.helper.EqualSpacingItemDecoration;
import com.futureskyltd.app.helper.FragmentChangeListener;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.ItemsParsing;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


public class HomeFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = HomeFragment.class.getSimpleName();
    RecyclerView categoryList, dealList, popularList, recentList, featuredList, storeList, discountList, suggestList, categoryProductView, toprateview;
    LinearLayoutManager categoryManager, dealManager, popularManager, recentManager, featuredManager, storeManager;
    CategoryViewAdapter categoryAdapter;
    AutoScrollViewPager bannerPager;
    BannerPagerAdapter bannerAdapter;
    RecyclerViewAdapter dealAdapter, popularAdapter, recentAdapter, featuredAdapter, discountAdapter, suggestAdapter, categoryProductAdapter, topratedAdapter;
    RecyclerViewAdapter storeAdapter;
    NestedScrollView mScrollView;
    View dealView;
    Display display;
    RelativeLayout dealLay, progressLay, nullLay;
    LinearLayout mainLay, popularLay, recentLay, featuredLay, storeLay, discountLay, suggestlay, categoryproduct_lay, rated_lay;
    TextView title, popularAll, dealAll, recentAll, featuredAll, storeAll, dealTimer, discountall, suggestall, categoryproall, toprateall;
    TextView popular_title, recent_title, feature_title, store_title, discount_title, suggest_title, cateproduct_title, toprate_title;
    int rightPadding, leftPadding, itemWidth, listHeight, dealWidth, storeImagewidth;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    Handler mHandler;
    public static String validTill = "", validTime = "";
    public static ArrayList<HashMap<String, String>> categoryAry = new ArrayList<HashMap<String, String>>();
    public ArrayList<HashMap<String, String>> bannerAry = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String, String>> dealAry = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String, String>> popularAry = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String, String>> recentAry = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String, String>> featuredAry = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String, String>> storeAry = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String, String>> discountAry = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String, String>> categoryProductAry = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String, String>> suggestAry = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String, String>> topratedAry = new ArrayList<HashMap<String, String>>();
    int refreshcount = 0;
    private SharedPreferences preferences;
    private String accesstoken, customerId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_main_layout, container, false);

        categoryList = (RecyclerView) view.findViewById(R.id.categoryList);
        bannerPager = (AutoScrollViewPager) view.findViewById(R.id.bannerPager);
        dealList = (RecyclerView) view.findViewById(R.id.dealList);
        popularList = (RecyclerView) view.findViewById(R.id.popularList);
        dealLay = (RelativeLayout) view.findViewById(R.id.dealLay);
        recentList = (RecyclerView) view.findViewById(R.id.recentList);
        featuredList = (RecyclerView) view.findViewById(R.id.featuredList);
        categoryProductView = (RecyclerView) view.findViewById(R.id.categoryproductview);
        mScrollView = (NestedScrollView) view.findViewById(R.id.mScrollView);
        storeList = (RecyclerView) view.findViewById(R.id.storeList);
        discountList = (RecyclerView) view.findViewById(R.id.discountList);
        suggestList = (RecyclerView) view.findViewById(R.id.suggest_view);
        toprateview = (RecyclerView) view.findViewById(R.id.toprateview);
        popularAll = (TextView) view.findViewById(R.id.popularAll);
        dealAll = (TextView) view.findViewById(R.id.dealAll);
        recentAll = (TextView) view.findViewById(R.id.recentAll);
        featuredAll = (TextView) view.findViewById(R.id.featuredAll);
        storeAll = (TextView) view.findViewById(R.id.storeAll);
        discountall = (TextView) view.findViewById(R.id.discountall);
        suggestall = (TextView) view.findViewById(R.id.suggestall);
        categoryproall = (TextView) view.findViewById(R.id.categoryproall);
        toprateall = (TextView) view.findViewById(R.id.toprateall);
        popular_title = (TextView) view.findViewById(R.id.popular_title);
        recent_title = (TextView) view.findViewById(R.id.recent_title);
        feature_title = (TextView) view.findViewById(R.id.feature_title);
        store_title = (TextView) view.findViewById(R.id.store_title);
        discount_title = (TextView) view.findViewById(R.id.discount_title);
        suggest_title = (TextView) view.findViewById(R.id.suggest_title);
        cateproduct_title = (TextView) view.findViewById(R.id.cateproduct_title);
        toprate_title = (TextView) view.findViewById(R.id.toprate_title);
        popularLay = (LinearLayout) view.findViewById(R.id.popularLay);
        recentLay = (LinearLayout) view.findViewById(R.id.recentLay);
        featuredLay = (LinearLayout) view.findViewById(R.id.featuredLay);
        storeLay = (LinearLayout) view.findViewById(R.id.storeLay);
        discountLay = (LinearLayout) view.findViewById(R.id.discountlay);
        suggestlay = (LinearLayout) view.findViewById(R.id.suggestlay);
        categoryproduct_lay = (LinearLayout) view.findViewById(R.id.categoryproductlay);
        rated_lay = (LinearLayout) view.findViewById(R.id.ratedproductlay);
        mainLay = (LinearLayout) view.findViewById(R.id.mainLay);
        nullLay = (RelativeLayout) view.findViewById(R.id.nullLay);
        progressLay = (RelativeLayout) view.findViewById(R.id.progress);
        dealTimer = (TextView) view.findViewById(R.id.dealTimer);
        dealView = (View) view.findViewById(R.id.dealView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));
        preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);
        customerId = preferences.getString("customer_id", null);

        bannerAry = new ArrayList<>();

        popularAll.setOnClickListener(this);
        dealAll.setOnClickListener(this);
        recentAll.setOnClickListener(this);
        featuredAll.setOnClickListener(this);
        storeAll.setOnClickListener(this);
        dealView.setOnClickListener(this);
        discountall.setOnClickListener(this);
        suggestall.setOnClickListener(this);
        categoryproall.setOnClickListener(this);
        toprateall.setOnClickListener(this);

        popularLay.setVisibility(View.GONE);
        recentLay.setVisibility(View.GONE);
        featuredLay.setVisibility(View.GONE);

        storeLay.setVisibility(View.GONE);
        discountLay.setVisibility(View.GONE);
        suggestlay.setVisibility(View.GONE);
        categoryproduct_lay.setVisibility(View.GONE);
        rated_lay.setVisibility(View.GONE);

        ((FragmentChangeListener) getActivity()).setNavSelectionItem(R.id.home, "Home");

        return view;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        display = getActivity().getWindowManager().getDefaultDisplay();
        ///////////////////for category recyclerview////////////////////
        categoryManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        categoryList.setLayoutManager(categoryManager);
        categoryAdapter = new CategoryViewAdapter(getActivity(), categoryAry);
        categoryList.setAdapter(categoryAdapter);
        categoryList.setHasFixedSize(true);

        /////////
        rightPadding = FantacyApplication.dpToPx(getActivity(), 5);
        leftPadding = FantacyApplication.dpToPx(getActivity(), 10);
        int padleftright = FantacyApplication.dpToPx(getActivity(), 50);

        float scale = (float) display.getWidth() / Constants.HOME_BANNER_WIDTH;
        int newHeight = (int) Math.round(Constants.HOME_BANNER_HEIGHT * scale);
        bannerPager.getLayoutParams().height = newHeight;

        refreshcount = 0;
        storeImagewidth = Math.round(display.getWidth() * 0.85f);
        itemWidth = display.getWidth() * 37 / 100;
        dealWidth = display.getWidth() * 43 / 100;
        listHeight = itemWidth + FantacyApplication.dpToPx(getActivity(), 80);
        dealLay.getLayoutParams().height = dealWidth + FantacyApplication.dpToPx(getActivity(), 80);

        ///////////Banner Image sliding ///////////
        bannerAdapter = new BannerPagerAdapter(getActivity(), bannerAry);
        bannerPager.setAdapter(bannerAdapter);
        bannerPager.setPageMargin(rightPadding);
        bannerAdapter.notifyDataSetChanged();
        if (bannerAry.size() > 0)
            bannerPager.startAutoScroll();

        ///////Deal Of the day Recyclier View//////////////

        dealManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        dealList.setLayoutManager(dealManager);
        dealAdapter = new RecyclerViewAdapter(getActivity(), dealAry, "deals", "");
        dealList.setAdapter(dealAdapter);
        dealList.setHasFixedSize(true);

        if (refreshcount == 0)
            dealList.addItemDecoration(new EqualSpacingItemDecoration(18));

        /*popularManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        popularList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        //  popularList.setLayoutManager(popularManager);
        popularAdapter = new RecyclerViewAdapter(getActivity(), popularAry, "popular");
        popularList.setAdapter(popularAdapter);
        popularList.setHasFixedSize(true);
*/
        ViewCompat.setNestedScrollingEnabled(bannerPager, false);

        categoryList.addOnItemTouchListener(categoryItemClick(getActivity(), categoryList));
        dealList.addOnItemTouchListener(recyclerItemClick(getActivity(), dealList, dealAry, ""));
        categoryList.setNestedScrollingEnabled(false);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshcount++;
                bannerAry.clear();
                getHomeData();
                dealList.scrollToPosition(0);
            }
        });

        //  storeList.setPadding(FantacyApplication.dpToPx(getActivity(), 15), 0, 0, 0);
        bannerPager.setPadding(FantacyApplication.dpToPx(getActivity(), 10), 0, 0, 0);
        if (FantacyApplication.isRTL(getActivity())) {
            dealList.setPadding(0, 0, dealWidth, 0);
        } else {
            dealList.setPadding(dealWidth, 0, 0, 0);
        }

        if (categoryAry.size() == 0 && bannerAry.size() == 0 && dealAry.size() == 0 && popularAry.size() == 0 &&
                recentAry.size() == 0 && featuredAry.size() == 0 && storeAry.size() == 0) {
            mainLay.setVisibility(View.GONE);
            progressLay.setVisibility(View.VISIBLE);
            nullLay.setVisibility(View.GONE);
        } else {
            hideHomeData();
        }
        //
        getHomeData();

    }

    private void getHomeData() {

        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_HOME, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                JSONObject ascendOrderObject = null, designObject = null;
                try {
                    Log.d(TAG, "getHomeDataRes=" + res);
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }


                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {

                        Toast.makeText(getContext(), "Home data showing !", Toast.LENGTH_SHORT).show();

                        JSONObject productObject = json.getJSONObject("item_lists");

                        ascendOrderObject = json.getJSONObject("ascending_order");

                        designObject = json.getJSONObject("layout_design");

                        resetAry();

                        JSONArray banner = json.getJSONArray(Constants.TAG_BANNER);
                        for (int i = 0; i < banner.length(); i++) {
                            JSONObject temp = banner.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();


                            String url = DefensiveClass.optString(temp, Constants.TAG_SLIDER_URL);
                            String image = DefensiveClass.optString(temp, Constants.TAG_IMAGE);
                            String type = DefensiveClass.optString(temp, Constants.TAG_TYPE);
                            String itemstatus = temp.has(Constants.TAG_ITEMSTATUS) ? DefensiveClass.optString(temp, Constants.TAG_ITEMSTATUS) : "";
                            String id = DefensiveClass.optString(temp, Constants.TAG_CATEGORY_ID);
                            String category = DefensiveClass.optString(temp, "category_name");
                            String sub_category = DefensiveClass.optString(temp, "subcategory_name");
                            String sub_categoryid = DefensiveClass.optString(temp, "subcategory_id");
                            String super_category = DefensiveClass.optString(temp, "supercategory_name");
                            String super_categoryid = DefensiveClass.optString(temp, "supercategory_id");

                            map.put(Constants.TAG_SLIDER_URL, url);
                            map.put(Constants.TAG_IMAGE, image);
                            map.put(Constants.TAG_TYPE, type);
                            map.put(Constants.TAG_ITEMSTATUS, itemstatus);
                            map.put(Constants.TAG_ID, id);
                            map.put(Constants.TAG_CATEGORY, category);
                            map.put(Constants.TAG_SUB_CATEGORY, sub_category);
                            map.put(Constants.TAG_SUB_CATEGORYID, sub_categoryid);
                            map.put(Constants.TAG_SUPER_CATEGORY, super_category);
                            map.put(Constants.TAG_SUPER_CATEGORYID, super_categoryid);

                            if (temp.has(Constants.TAG_PRODUCT)) {
                                JSONArray productArray = temp.getJSONArray(Constants.TAG_PRODUCT);
                                map.put(Constants.TAG_PRODUCT, productArray.toString());
                            }
                            bannerAry.add(map);
                        }


                        bannerAdapter = new BannerPagerAdapter(getActivity(), bannerAry);
                        bannerPager.setAdapter(bannerAdapter);
                        bannerPager.setPageMargin(rightPadding);
                        bannerAdapter.notifyDataSetChanged();
                        if (bannerAry.size() > 0)
                            bannerPager.startAutoScroll();

                        JSONArray category = json.getJSONArray(Constants.TAG_CATEGORY);
                        for (int i = 0; i < category.length(); i++) {
                            JSONObject temp = category.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();

                            String id = DefensiveClass.optString(temp, Constants.TAG_ID);
                            String name = DefensiveClass.optString(temp, Constants.TAG_NAME);
                            String icon = DefensiveClass.optString(temp, Constants.TAG_ICON);

                            map.put(Constants.TAG_ID, id);
                            map.put(Constants.TAG_NAME, name);
                            map.put(Constants.TAG_ICON, icon);

                            categoryAry.add(map);
                        }

                        JSONObject deals = json.getJSONObject(Constants.TAG_DAILY_DEALS);
                        validTill = DefensiveClass.optString(deals, Constants.TAG_VALID_TILL);
                        validTime = DefensiveClass.optString(deals, Constants.TAG_VALID_TIME);
                        JSONArray items = deals.getJSONArray(Constants.TAG_ITEMS);
                        ItemsParsing dealItems = new ItemsParsing(getActivity());
                        dealAry.addAll(dealItems.getItems(items));

                        JSONArray popular = productObject.has(Constants.TAG_POPULAR_PRODUCTS) ? productObject.getJSONArray(Constants.TAG_POPULAR_PRODUCTS) : new JSONArray();
                        ItemsParsing popularItems = new ItemsParsing(getActivity());
                        popularAry.addAll(popularItems.getItems(popular));


                        JSONArray recent = productObject.has(Constants.TAG_RECENT_PRODUCTS) ? productObject.getJSONArray(Constants.TAG_RECENT_PRODUCTS) : new JSONArray();
                        ItemsParsing recentItems = new ItemsParsing(getActivity());
                        recentAry.addAll(recentItems.getItems(recent));

                        JSONArray featured = productObject.has(Constants.TAG_FEATURED_PRODUCTS) ? productObject.getJSONArray(Constants.TAG_FEATURED_PRODUCTS) : new JSONArray();
                        ItemsParsing featuredItems = new ItemsParsing(getActivity());
                        featuredAry.addAll(featuredItems.getItems(featured));


                        JSONArray store = productObject.has(Constants.TAG_POPULAR_STORE) ? productObject.getJSONArray(Constants.TAG_POPULAR_STORE) : new JSONArray();
                        for (int i = 0; i < store.length(); i++) {
                            JSONObject temp = store.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();

                            String store_id = DefensiveClass.optString(temp, Constants.TAG_STORE_ID);
                            String store_name = DefensiveClass.optString(temp, Constants.TAG_STORE_NAME);
                            String wifi = DefensiveClass.optString(temp, Constants.TAG_WIFI);
                            String merchant_name = DefensiveClass.optString(temp, Constants.TAG_MERCHANT_NAME);
                            String stat = DefensiveClass.optString(temp, Constants.TAG_STATUS);
                            String image = DefensiveClass.optString(temp, Constants.TAG_IMAGE);

                            map.put(Constants.TAG_STORE_ID, store_id);
                            map.put(Constants.TAG_STORE_NAME, store_name);
                            map.put(Constants.TAG_WIFI, wifi);
                            map.put(Constants.TAG_MERCHANT_NAME, merchant_name);
                            map.put(Constants.TAG_STATUS, stat);
                            map.put(Constants.TAG_IMAGE, image);

                            storeAry.add(map);
                        }


                        JSONArray suggested = productObject.has(Constants.TAG_SUGGESTEDITEMS) ? productObject.getJSONArray(Constants.TAG_SUGGESTEDITEMS) : new JSONArray();
                        ItemsParsing suggestedItems = new ItemsParsing(getActivity());
                        if (suggested.length() > 0)
                            suggestAry.addAll(suggestedItems.getSuggestedItems(suggested));

                        JSONArray discounts = productObject.has(Constants.TAG_DISCOUNTS) ? productObject.getJSONArray(Constants.TAG_DISCOUNTS) : new JSONArray();
                        ItemsParsing discountItems = new ItemsParsing(getActivity());
                        discountAry.addAll(discountItems.getItems(discounts));

                        JSONArray categoryproducts = productObject.has(Constants.TAG_CATEGORYPRODUCT) ? productObject.getJSONArray(Constants.TAG_CATEGORYPRODUCT) : new JSONArray();
                        ItemsParsing categoryproductItems = new ItemsParsing(getActivity());
                        categoryProductAry.addAll(categoryproductItems.getItems(categoryproducts));

                        JSONArray toprated = productObject.has(Constants.TAG_TOPRATED) ? productObject.getJSONArray(Constants.TAG_TOPRATED) : new JSONArray();
                        ItemsParsing topratedItems = new ItemsParsing(getActivity());
                        topratedAry.addAll(topratedItems.getItems(toprated));

                        getHomeData2();

                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                            /*Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                            //Log.d(TAG, "onResponse: " + res);
                            Toast.makeText(getContext(), "You are Blocked By Admin Or Deleted !", Toast.LENGTH_LONG).show();
                        } else {
                           /* Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                            Toast.makeText(getContext(), "We Are Maintain Our Website!", Toast.LENGTH_LONG).show();
                        }
                    }

                    mainLay.setVisibility(View.VISIBLE);
                    progressLay.setVisibility(View.GONE);
                    nullLay.setVisibility(View.GONE);

                    mHandler = new Handler();
                    mHandler.post(mRunnable);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                categoryAdapter.notifyDataSetChanged();
                bannerAdapter.notifyDataSetChanged();
                // popularAdapter.notifyDataSetChanged();
                //   recentAdapter.notifyDataSetChanged();
                // featuredAdapter.notifyDataSetChanged();
                // storeAdapter.notifyDataSetChanged();
                dealAdapter.notifyDataSetChanged();

                if (categoryAry.size() == 0 && bannerAry.size() == 0 && dealAry.size() == 0 && popularAry.size() == 0 &&
                        recentAry.size() == 0 && featuredAry.size() == 0 && storeAry.size() == 0) {
                    mainLay.setVisibility(View.GONE);
                    progressLay.setVisibility(View.GONE);
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    setData(ascendOrderObject, designObject, 0);
                    hideHomeData();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getHomeDataError: " + error.getMessage());
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                if (categoryAry.size() == 0 && bannerAry.size() == 0 && dealAry.size() == 0 && popularAry.size() == 0 &&
                        recentAry.size() == 0 && featuredAry.size() == 0 && storeAry.size() == 0) {
                    mainLay.setVisibility(View.GONE);
                    progressLay.setVisibility(View.GONE);
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    hideHomeData();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                if (customerId!= null) {
                    map.put("user_id", customerId);
                }
                Log.d(TAG, "getHomeDataParams: " + map);

                return map;
            }

           /* @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJodHRwczpcL1wvbGFhbHNvYnVqLmNvbSIsInN1YiI6ImxhYWxzb2J1ai5jb20iLCJpYXQiOjE2MTMzODE1NTcsImV4cCI6MTYxMzk4NjM1NywiaWQiOjMwLCJlbWFpbCI6Im1vaGFtbWFkc2hvYm9vekBnbWFpbC5jb20ifQ.AzERLlbaikHdEkvCX_93m7RAwhZgHgPodXgvP4t3tHaNnPg7KRfkJZpXONa72_-DS3KG0uJrHyeOIFQx-JYWWA");
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }*/
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    public static void resetAry() {
        categoryAry.clear();
        dealAry.clear();
        popularAry.clear();
        recentAry.clear();
        featuredAry.clear();
        storeAry.clear();

        discountAry.clear();
        categoryProductAry.clear();
        suggestAry.clear();
        topratedAry.clear();
    }


    public void setData(JSONObject ascendObject, JSONObject designObject, int pos) {

        int padpxl = FantacyApplication.dpToPx(getActivity(), 10);


        String one = "", two = "", three = "", four = "", five = "", six = "", seven = "", eight = "";
        try {
            if (pos == 0) {
                one = ascendObject.has("one") ? ascendObject.getString("one") : "";
                two = ascendObject.has("two") ? ascendObject.getString("two") : "";
                three = ascendObject.has("three") ? ascendObject.getString("three") : "";
            } else if (pos == 1) {
                four = ascendObject.has("one") ? ascendObject.getString("one") : "";
                five = ascendObject.has("two") ? ascendObject.getString("two") : "";
                six = ascendObject.has("three") ? ascendObject.getString("three") : "";
            } else if (pos == 2) {
                seven = ascendObject.has("one") ? ascendObject.getString("one") : "";
                eight = ascendObject.has("two") ? ascendObject.getString("two") : "";
            }


            storeManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

            DividerItemDecoration itemDivider = new DividerItemDecoration(getContext(),
                    dealManager.getOrientation());
            itemDivider.setDrawable(getResources().getDrawable(R.drawable.item_divider));

            DividerItemDecoration test = new DividerItemDecoration(getContext(),
                    dealManager.getOrientation());
            test.setDrawable(getResources().getDrawable(R.drawable.recycler_divider));


            DividerItemDecoration storeDivider = new DividerItemDecoration(getContext(),
                    storeManager.getOrientation());
            storeDivider.setDrawable(getResources().getDrawable(R.drawable.emptyspace_15dp));

            if (pos == 0) {
                popularManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

                if (designObject.getString(one).equalsIgnoreCase("slider3") && !one.equalsIgnoreCase("suggested_items") && !one.equalsIgnoreCase("popular_stores"))
                    popularList.setLayoutManager(new GridLayoutManager(getContext(), 2));
                else popularList.setLayoutManager(popularManager);

                popularAdapter = new RecyclerViewAdapter(getActivity(), getArry(one), designObject.has(one) ? designObject.getString(one) : "", one);
                popularList.setAdapter(popularAdapter);
                popularList.setHasFixedSize(true);

                recentManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

                if (designObject.getString(two).equalsIgnoreCase("slider3") && !two.equalsIgnoreCase("suggested_items") && !two.equalsIgnoreCase("popular_stores"))
                    recentList.setLayoutManager(new GridLayoutManager(getContext(), 2));
                else recentList.setLayoutManager(recentManager);

                recentAdapter = new RecyclerViewAdapter(getActivity(), getArry(two), designObject.has(two) ? designObject.getString(two) : "", two);
                recentList.setAdapter(recentAdapter);
                recentList.setHasFixedSize(true);

                featuredManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                if (designObject.getString(three).equalsIgnoreCase("slider3") && !three.equalsIgnoreCase("suggested_items") && !three.equalsIgnoreCase("popular_stores"))
                    featuredList.setLayoutManager(new GridLayoutManager(getContext(), 2));
                else featuredList.setLayoutManager(featuredManager);

                featuredAdapter = new RecyclerViewAdapter(getActivity(), getArry(three), designObject.has(three) ? designObject.getString(three) : "", three);
                featuredList.setAdapter(featuredAdapter);
                featuredList.setHasFixedSize(true);

                popular_title.setText(getTitle(one));
                recent_title.setText(getTitle(two));
                feature_title.setText(getTitle(three));

                popularAll.setTag(one);
                recentAll.setTag(two);
                featuredAll.setTag(three);

                if (getArry(one).size() == 0) {
                    popularLay.setVisibility(View.GONE);
                } else {
                    popularLay.setVisibility(View.VISIBLE);
                }

                if (getArry(two).size() == 0) {
                    recentLay.setVisibility(View.GONE);
                } else {
                    recentLay.setVisibility(View.VISIBLE);
                }

                if (getArry(three).size() == 0) {
                    featuredLay.setVisibility(View.GONE);
                } else {
                    featuredLay.setVisibility(View.VISIBLE);
                }

                // popularList.addOnItemTouchListener(recyclerItemClick(getActivity(), popularList, getArry(one),one));
                //   recentList.addOnItemTouchListener(recyclerItemClick(getActivity(), recentList, getArry(two),two));
                //   featuredList.addOnItemTouchListener(recyclerItemClick(getActivity(), featuredList, getArry(three),three));

                if (refreshcount == 0) {
                    //dealList.addItemDecoration(itemDivider);
                    if (!designObject.getString(one).equalsIgnoreCase("slider2"))
                        popularList.addItemDecoration(new EqualSpacingItemDecoration(padpxl));
                    if (!designObject.getString(two).equalsIgnoreCase("slider2"))
                        recentList.addItemDecoration(new EqualSpacingItemDecoration(padpxl));
                    if (!designObject.getString(three).equalsIgnoreCase("slider2"))
                        featuredList.addItemDecoration(new EqualSpacingItemDecoration(padpxl));
                }

                dealList.setNestedScrollingEnabled(false);
                popularList.setNestedScrollingEnabled(false);
                recentList.setNestedScrollingEnabled(false);
                featuredList.setNestedScrollingEnabled(false);

            } else if (pos == 1) {

                storeManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                if (designObject.getString(four).equalsIgnoreCase("slider3") && !four.equalsIgnoreCase("suggested_items") && !four.equalsIgnoreCase("popular_stores"))
                    storeList.setLayoutManager(new GridLayoutManager(getContext(), 2));
                else storeList.setLayoutManager(storeManager);
                storeAdapter = new RecyclerViewAdapter(getActivity(), getArry(four), designObject.has(four) ? designObject.getString(four) : "", four);
                storeList.setAdapter(storeAdapter);
                storeList.setHasFixedSize(true);

                storeManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                if (designObject.getString(five).equalsIgnoreCase("slider3") && !five.equalsIgnoreCase("suggested_items") && !five.equalsIgnoreCase("popular_stores"))
                    discountList.setLayoutManager(new GridLayoutManager(getContext(), 2));
                else discountList.setLayoutManager(storeManager);

                discountAdapter = new RecyclerViewAdapter(getActivity(), getArry(five), designObject.has(five) ? designObject.getString(five) : "", five);
                discountList.setAdapter(discountAdapter);
                discountList.setHasFixedSize(true);

                storeManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                if (designObject.getString(six).equalsIgnoreCase("slider3") && !six.equalsIgnoreCase("suggested_items") && !six.equalsIgnoreCase("popular_stores"))
                    suggestList.setLayoutManager(new GridLayoutManager(getContext(), 2));
                else suggestList.setLayoutManager(storeManager);

                suggestAdapter = new RecyclerViewAdapter(getActivity(), getArry(six), designObject.has(six) ? designObject.getString(six) : "", six);
                suggestList.setAdapter(suggestAdapter);
                suggestList.setHasFixedSize(true);

                // storeList.addOnItemTouchListener(recyclerItemClick(getActivity(), storeList, getArry(four), four));
                // discountList.addOnItemTouchListener(recyclerItemClick(getActivity(), discountList, getArry(five), five));
                //   suggestList.addOnItemTouchListener(recyclerItemClick(getActivity(), suggestList, getArry(six), six));

                if (refreshcount == 0) {
                    if (!designObject.getString(four).equalsIgnoreCase("slider2"))
                        storeList.addItemDecoration(new EqualSpacingItemDecoration(padpxl));
                    if (!designObject.getString(five).equalsIgnoreCase("slider2"))
                        discountList.addItemDecoration(new EqualSpacingItemDecoration(padpxl));
                    if (!designObject.getString(six).equalsIgnoreCase("slider2"))
                        suggestList.addItemDecoration(new EqualSpacingItemDecoration(padpxl));
                }
                storeList.setNestedScrollingEnabled(false);
                discountList.setNestedScrollingEnabled(false);
                suggestList.setNestedScrollingEnabled(false);

                store_title.setText(getTitle(four));
                discount_title.setText(getTitle(five));
                suggest_title.setText(getTitle(six));

                storeAll.setTag(four);
                discountall.setTag(five);
                suggestall.setTag(six);

                if (getArry(four).size() == 0) {
                    storeLay.setVisibility(View.GONE);
                } else {
                    storeLay.setVisibility(View.VISIBLE);
                }

                if (getArry(five).size() == 0) {
                    discountLay.setVisibility(View.GONE);
                } else {
                    discountLay.setVisibility(View.VISIBLE);
                }

                if (getArry(six).size() == 0) {
                    suggestlay.setVisibility(View.GONE);
                } else {
                    suggestlay.setVisibility(View.VISIBLE);
                }

            } else if (pos == 2) {

                storeManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                if (designObject.getString(seven).equalsIgnoreCase("slider3") && !seven.equalsIgnoreCase("suggested_items")
                        && !seven.equalsIgnoreCase("popular_stores"))
                    categoryProductView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                else categoryProductView.setLayoutManager(storeManager);
                categoryProductAdapter = new RecyclerViewAdapter(getActivity(), getArry(seven), designObject.has(seven) ? designObject.getString(seven) : "", seven);
                categoryProductView.setAdapter(categoryProductAdapter);
                categoryProductView.setHasFixedSize(true);

                storeManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                if (designObject.getString(eight).equalsIgnoreCase("slider3") && !eight.equalsIgnoreCase("suggested_items")
                        && !eight.equalsIgnoreCase("popular_stores"))
                    toprateview.setLayoutManager(new GridLayoutManager(getContext(), 2));
                else toprateview.setLayoutManager(storeManager);
                topratedAdapter = new RecyclerViewAdapter(getActivity(), getArry(eight), designObject.has(eight) ? designObject.getString(eight) : "", eight);
                toprateview.setAdapter(topratedAdapter);
                toprateview.setHasFixedSize(true);


                //categoryProductView.addOnItemTouchListener(recyclerItemClick(getActivity(),categoryProductView, getArry(seven), seven));
                //toprateview.addOnItemTouchListener(recyclerItemClick(getActivity(),toprateview, getArry(eight), eight));
                if (refreshcount == 0) {
                    if (!designObject.getString(seven).equalsIgnoreCase("slider2"))
                        categoryProductView.addItemDecoration(new EqualSpacingItemDecoration(padpxl));
                    if (!designObject.getString(eight).equalsIgnoreCase("slider2"))
                        toprateview.addItemDecoration(new EqualSpacingItemDecoration(padpxl));
                }

                categoryProductView.setNestedScrollingEnabled(false);
                toprateview.setNestedScrollingEnabled(false);

                cateproduct_title.setText(getTitle(seven));
                toprate_title.setText(getTitle(eight));

                categoryproall.setTag(seven);
                toprateall.setTag(eight);

                if (getArry(seven).size() == 0) {
                    categoryproduct_lay.setVisibility(View.GONE);
                } else {
                    categoryproduct_lay.setVisibility(View.VISIBLE);
                }

                if (getArry(eight).size() == 0) {
                    rated_lay.setVisibility(View.GONE);
                } else {
                    rated_lay.setVisibility(View.VISIBLE);
                }
            }

        } catch (Exception e) {
            Log.e("Exceptionhome", "-" + e.toString());
        }


    }

    public ArrayList<HashMap<String, String>> getArry(String data) {
        switch (data) {
            case "categories":
                return categoryProductAry;
            case "recently_added":
                return recentAry;
            case "discounts":
                return discountAry;
            case "featured_items":
                return featuredAry;
            case "suggested_items":
                return suggestAry;
            case "top_rated":
                return topratedAry;
            case "popular_stores":
                return storeAry;
            case "most_popular":
                return popularAry;
            default:
                return new ArrayList<HashMap<String, String>>();
        }
    }


    public String getTitle(String data) {
        switch (data) {
            case "categories":
                return getResources().getString(R.string.category_products);
            case "recently_added":
                return getResources().getString(R.string.new_arrivals);
            case "discounts":
                return getResources().getString(R.string.discount);
            case "featured_items":
                return getResources().getString(R.string.featured);
            case "suggested_items":
                return getResources().getString(R.string.suggest_items);
            case "top_rated":
                return getResources().getString(R.string.toprated);
            case "popular_stores":
                return getResources().getString(R.string.popular_store);
            case "most_popular":
                return getResources().getString(R.string.popular_products);
            default:
                return getResources().getString(R.string.popular_products);
        }

    }

    private void hideHomeData() {

        if (bannerAry.size() == 0) {
            bannerPager.setVisibility(View.GONE);
        } else {
            bannerPager.setVisibility(View.VISIBLE);
            if (FantacyApplication.isRTL(getActivity())) {
                bannerPager.setCurrentItem(bannerAry.size() - 1);
            }
        }

        if (categoryAry.size() == 0) {
            categoryList.setVisibility(View.GONE);
        } else {
            categoryList.setVisibility(View.VISIBLE);
        }

        /*if (dealAry.size() == 0) {
            dealLay.setVisibility(View.GONE);
        } else {
            mHandler = new Handler();
            checkDailyDeal();
        }*/

       /* if (popularAry.size() == 0) {
            popularLay.setVisibility(View.GONE);
        } else {
            popularLay.setVisibility(View.VISIBLE);
        }

        if (recentAry.size() == 0) {
            recentLay.setVisibility(View.GONE);
        } else {
            recentLay.setVisibility(View.VISIBLE);
        }

        if (featuredAry.size() == 0) {
            featuredLay.setVisibility(View.GONE);
        } else {
            featuredLay.setVisibility(View.VISIBLE);
        }

        if (storeAry.size() == 0) {
            storeLay.setVisibility(View.GONE);
        } else {
            storeLay.setVisibility(View.VISIBLE);
        }

        if (discountAry.size() == 0) {
            discountLay.setVisibility(View.GONE);
        } else {
            discountLay.setVisibility(View.VISIBLE);
        }

        if (suggestAry.size() == 0) {
            suggestlay.setVisibility(View.GONE);
        } else {
            suggestlay.setVisibility(View.VISIBLE);
        }

        if (categoryProductAry.size() == 0) {
            categoryproduct_lay.setVisibility(View.GONE);
        } else {
            categoryproduct_lay.setVisibility(View.VISIBLE);
        }

        if (topratedAry.size() == 0) {
            rated_lay.setVisibility(View.GONE);
        } else {
            rated_lay.setVisibility(View.VISIBLE);
        }*/
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            checkDailyDeal();
        }
    };

    @SuppressLint("SetTextI18n")
    private void checkDailyDeal() {
        if (!validTill.equals("") && dealAry.size() > 0) {
            long timeStamp = Long.parseLong(validTill);
            long timeEnd = Long.parseLong(validTime);

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timeStamp * 1000);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            String calDate = DateFormat.format("dd-MM-yyyy", cal).toString();

            long output = cal.getTimeInMillis() / 1000;
            long outputTime = (output) - (System.currentTimeMillis() / 1000);

            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTimeInMillis(timeEnd * 1000);
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endCalendar.set(Calendar.MINUTE, 59);
            endCalendar.set(Calendar.SECOND, 59);
            String currentDate = DateFormat.format("dd-MM-yyyy", endCalendar).toString();

            if (currentDate.equals(calDate)) {
                dealLay.setVisibility(View.VISIBLE);
                dealTimer.setText((TimeUnit.SECONDS.toHours(outputTime) - TimeUnit.DAYS.toHours(TimeUnit.SECONDS.toDays(outputTime))) + " : " +
                        (TimeUnit.SECONDS.toMinutes(outputTime) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.SECONDS.toHours(outputTime))) + " : " +
                        (TimeUnit.SECONDS.toSeconds(outputTime) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.SECONDS.toMinutes(outputTime))));

                mHandler.postDelayed(mRunnable, 1000); //run every second
            } else {
                dealLay.setVisibility(View.GONE);
                mHandler.removeCallbacks(mRunnable);
            }
        } else {
            dealLay.setVisibility(View.GONE);
            mHandler.removeCallbacks(mRunnable);
        }
    }

    public class CategoryViewAdapter extends RecyclerView.Adapter<CategoryViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView name;
            ImageView icon;

            public MyViewHolder(View view) {
                super(view);

                name = (TextView) view.findViewById(R.id.name);
                icon = (ImageView) view.findViewById(R.id.icon);
            }
        }

        public CategoryViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public CategoryViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_category_item, parent, false);

            return new CategoryViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final CategoryViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.name.setText(tempMap.get(Constants.TAG_NAME));
            String img = tempMap.get(Constants.TAG_ICON);
            if (!img.equals("")) {
                Picasso.get().load(img).into(holder.icon);
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }
    }

    /**
     * Adapter for showing banner image
     **/
    class BannerPagerAdapter extends PagerAdapter {
        Context context;
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> data;

        public BannerPagerAdapter(Context act, ArrayList<HashMap<String, String>> newary) {
            this.data = newary;
            this.context = act;
        }

        public int getCount() {
            return data.size();
        }

        public Object instantiateItem(ViewGroup collection, final int position) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.home_banner_image,
                    collection, false);

            ImageView image = (ImageView) itemView.findViewById(R.id.image);
            RelativeLayout mainLay = (RelativeLayout) itemView.findViewById(R.id.mainLay);

            if (position == data.size() - 1) {
                mainLay.setPadding(0, 0, leftPadding, 0);
            }

            String img = data.get(position).get(Constants.TAG_IMAGE);
            if (!img.equals("")) {
                Picasso.get().load(img).into(image);
            }

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data.get(position).get(Constants.TAG_TYPE).equalsIgnoreCase("categories")) {

                        if (!data.get(position).get(Constants.TAG_SUPER_CATEGORYID).equalsIgnoreCase("")) {
                            Intent i = new Intent(getActivity(), CategoryListings.class);
                            i.putExtra("from", "category");
                            i.putExtra("catId", data.get(position).get(Constants.TAG_ID));
                            i.putExtra("subcatId", data.get(position).get(Constants.TAG_SUB_CATEGORYID));
                            i.putExtra("supercatId", data.get(position).get(Constants.TAG_SUPER_CATEGORYID));
                            i.putExtra("catName", data.get(position).get(Constants.TAG_SUPER_CATEGORY));
                            i.putExtra("catState", "super");
                            startActivity(i);
                        } else if (!data.get(position).get(Constants.TAG_SUB_CATEGORYID).equalsIgnoreCase("")) {
                            Intent i = new Intent(getActivity(), CategoryListings.class);
                            i.putExtra("from", "category");
                            i.putExtra("catId", data.get(position).get(Constants.TAG_ID));
                            i.putExtra("subcatId", data.get(position).get(Constants.TAG_SUB_CATEGORYID));
                            i.putExtra("catName", data.get(position).get(Constants.TAG_SUB_CATEGORY));
                            i.putExtra("catState", "sub");
                            startActivity(i);
                        } else if (!data.get(position).get(Constants.TAG_ID).equalsIgnoreCase("")) {
                            Intent i = new Intent(getActivity(), CategoryListings.class);
                            i.putExtra("from", "category");
                            i.putExtra("catId", data.get(position).get(Constants.TAG_ID));
                            i.putExtra("catName", data.get(position).get(Constants.TAG_CATEGORY));
                            i.putExtra("catState", "parent");
                            startActivity(i);
                        }

                    } else if (data.get(position).get(Constants.TAG_TYPE).equalsIgnoreCase("item")) {
                        if (data.get(position).get(Constants.TAG_ITEMSTATUS).equalsIgnoreCase("true")) {
                            ItemsParsing popularItems = new ItemsParsing(getActivity());
                            try {
                                JSONArray productArray = new JSONArray(data.get(position).get(Constants.TAG_PRODUCT));
                                ArrayList<HashMap<String, String>> productList = new ArrayList<HashMap<String, String>>();
                                productList.addAll(popularItems.getItems(productArray));
                                Intent i = new Intent(getActivity(), DetailActivity.class);
                                i.putExtra("items", productList);
                                i.putExtra("position", 0);
                                startActivity(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "Item not available", Toast.LENGTH_SHORT).show();
                        }
                    } else if (Patterns.WEB_URL.matcher(data.get(position).get(Constants.TAG_SLIDER_URL)).matches()) {
                        Intent b = new Intent(Intent.ACTION_VIEW, Uri.parse(data.get(position).get(Constants.TAG_SLIDER_URL)));
                        if (b.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(b);
                        } else
                            Toast.makeText(getContext(), getResources().getString(R.string.browsernot_found), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            ((ViewPager) collection).addView(itemView, 0);
            return itemView;

        }

        @Override
        public float getPageWidth(int position) {
            if (position == data.size() - 1)
                return (1f);
            else
                return (0.975f);
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    interface onSubItemClick {
        public void onItemClick(int pos, HashMap<String, String> map, RecyclerViewAdapter.MyViewHolder holder, int parentPos);
    }


    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> implements onSubItemClick {

        ArrayList<HashMap<String, String>> Items;
        Context context;
        String from = "", arrayType = "";

        public void setOnsubitemclick(onSubItemClick onsubitemclick) {
            this.onsubitemclick = onsubitemclick;
        }

        onSubItemClick onsubitemclick;


        @Override
        public void onItemClick(int pos, HashMap<String, String> map, RecyclerViewAdapter.MyViewHolder holder, int parentPos) {
            Log.e("recyclerposs", "-" + pos + " parentPos " + parentPos);
            if (from.equalsIgnoreCase("slider2")) {
                parentPos = parentPos * 10;
            }
            HashMap<String, String> beforemap = Items.get(parentPos);
            Items.set(parentPos, map);

            if (from.equalsIgnoreCase("slider2")) {
                int otherpos = parentPos + pos + 1;
                Items.set(otherpos, beforemap);
            }

            Spanned htmlAsSpanned = Html.fromHtml(map.get(Constants.TAG_ITEM_DESCRIPTION)); // used by TextView
            holder.description.setText(htmlAsSpanned);
            holder.itemName.setText(map.get(Constants.TAG_ITEM_TITLE));
            String image = map.get(Constants.TAG_IMAGE);
            if (image != null && !image.equals("")) {
                Picasso.get().load(image).into(holder.itemImage);
            }

            if (map.get(Constants.TAG_DEAL_ENABLED) != null) {
                if (map.get(Constants.TAG_DEAL_ENABLED).equalsIgnoreCase("yes") && FantacyApplication.isDealEnabled(map.get(Constants.TAG_VALID_TILL))) {
                    holder.discountPrice.setVisibility(View.VISIBLE);
                    holder.discountPrice.setPaintFlags(holder.discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    String costValue = map.get(Constants.TAG_PRICE);
                    float priceValue = Float.parseFloat(costValue) - (Float.parseFloat(costValue)
                            * ((Float.parseFloat(map.get(Constants.TAG_DISCOUNT_PERCENTAGE))) / 100.0f));

                    holder.itemPrice.setText(map.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.decimal.format(priceValue));
                    holder.discountPrice.setText(map.get(Constants.TAG_CURRENCY) + " " + map.get(Constants.TAG_PRICE));
                    holder.discountPercent.setVisibility(View.GONE);
                    holder.discountPercent.setText(map.get(Constants.TAG_DISCOUNT_PERCENTAGE) + "% off");
                } else {
                    holder.discountPrice.setVisibility(View.GONE);
                    holder.itemPrice.setText(map.get(Constants.TAG_CURRENCY) + " " + map.get(Constants.TAG_PRICE));
                }
            }

            SuggestItemAdapter adapter = (SuggestItemAdapter) holder.suggestView.getAdapter();
            adapter.replaceItem(pos, beforemap);
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView itemName, itemPrice, discountPrice, description, discountPercent;
            CornerImageView itemImage;
            RecyclerView suggestView;
            RelativeLayout mainLay;
            LinearLayout suggest_child, bottomlay, paddinglay, detaillay, parentLay;
            ImageView leftarrow, rightarrow;

            public MyViewHolder(View view) {
                super(view);

                itemName = (TextView) view.findViewById(R.id.itemName);
                itemPrice = (TextView) view.findViewById(R.id.itemPrice);
                discountPercent = (TextView) view.findViewById(R.id.discountPercent);
                description = (TextView) view.findViewById(R.id.description);
                itemImage = (CornerImageView) view.findViewById(R.id.itemImage);
                suggestView = (RecyclerView) view.findViewById(R.id.suggest_view);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);
                parentLay = (LinearLayout) view.findViewById(R.id.parentlay);
                suggest_child = (LinearLayout) view.findViewById(R.id.suggest_child);
                bottomlay = (LinearLayout) view.findViewById(R.id.bottomLay);
                detaillay = (LinearLayout) view.findViewById(R.id.detaillay);
                paddinglay = (LinearLayout) view.findViewById(R.id.paddinglay);
                discountPrice = (TextView) view.findViewById(R.id.discountPrice);
                leftarrow = (ImageView) view.findViewById(R.id.leftarrow);
                rightarrow = (ImageView) view.findViewById(R.id.rightarrow);

                discountPrice.setPaintFlags(discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                mainLay.setOnClickListener(this);
                leftarrow.setOnClickListener(this);
                rightarrow.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.mainLay:
                        if (arrayType.equalsIgnoreCase("popular_stores")) {
                            Intent i = new Intent(getActivity(), StoreProfile.class);
                            i.putExtra("storeId", Items.get(getAdapterPosition()).get(Constants.TAG_STORE_ID));
                            startActivity(i);
                        } else if (from.equalsIgnoreCase("slider2")) {
                            int pos = getAdapterPosition() * 10;
                            Intent i = new Intent(getActivity(), DetailActivity.class);
                            i.putExtra("items",
                                    Items);
                            i.putExtra("position", pos);
                            startActivity(i);
                        } else {
                            Log.e("detail", "product");
                            Intent i = new Intent(getActivity(), DetailActivity.class);
                            i.putExtra("items", Items);
                            i.putExtra("position", getAdapterPosition());
                            startActivity(i);
                        }

                        break;
                    case R.id.leftarrow:
                        LinearLayoutManager layoutManager = (LinearLayoutManager) suggestView.getLayoutManager();
                        suggestView.getLayoutManager().scrollToPosition(layoutManager.findFirstVisibleItemPosition() - 1);
                        break;
                    case R.id.rightarrow:
                        LinearLayoutManager layoutManager2 = (LinearLayoutManager) suggestView.getLayoutManager();
                        suggestView.getLayoutManager().scrollToPosition(layoutManager2.findLastVisibleItemPosition() + 1);
                        break;
                }
            }
        }

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items, String from, String arraytype) {
            this.Items = Items;
            this.context = context;
            this.from = from;
            this.arrayType = arraytype;
        }

        @Override
        public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_items, parent, false);

            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.itemName.setText(tempMap.get(Constants.TAG_ITEM_TITLE));
            holder.paddinglay.setVisibility(View.GONE);
            holder.description.setVisibility(View.GONE);
            holder.bottomlay.setBackgroundResource(R.drawable.home_whitecurve);
            holder.detaillay.setBackgroundResource(R.drawable.home_items_curve_shape);


            if (arrayType.equals("suggested_items")) {
                holder.itemImage.getLayoutParams().width = storeImagewidth;
                holder.mainLay.getLayoutParams().width = storeImagewidth;
                holder.bottomlay.getLayoutParams().width = storeImagewidth;
                holder.suggest_child.setBackgroundResource(R.drawable.allside_curve_white);
                int suggestviewwid = storeImagewidth - FantacyApplication.dpToPx(getActivity(), 20);
                holder.suggestView.getLayoutParams().width = suggestviewwid;
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.suggest_child.getLayoutParams();
                params.setMargins(0, 25, 0, 0);
                holder.suggest_child.setLayoutParams(params);

                int pxl = FantacyApplication.dpToPx(getActivity(), 10);
                holder.suggest_child.setPadding(pxl, pxl, pxl, pxl);

                holder.paddinglay.setVisibility(View.VISIBLE);
                holder.itemPrice.setVisibility(View.VISIBLE);
                holder.suggest_child.setVisibility(View.VISIBLE);
                holder.bottomlay.setVisibility(View.VISIBLE);
                holder.bottomlay.setBackgroundResource(R.drawable.home_items_curve_shape);
                holder.detaillay.setBackgroundResource(R.drawable.home_whitecurve);
                if (Items.size() == 1) {
                    holder.parentLay.getLayoutParams().width = storeImagewidth;
                }
                ArrayList<HashMap<String, String>> childlist = new ArrayList();
                try {
                    JSONArray childarray = new JSONArray(tempMap.get(Constants.TAG_SUGGESTCHILD));
                    ItemsParsing parseing = new ItemsParsing(getActivity());
                    childlist = parseing.getItems(childarray);
                } catch (Exception e) {
                }

                setOnsubitemclick(this);

                if (childlist.size() == 0)
                    holder.suggest_child.setBackgroundResource(R.drawable.home_items_curve_shape);

                if (childlist.size() > 4) {
                    holder.rightarrow.setVisibility(View.VISIBLE);
                    holder.leftarrow.setVisibility(View.VISIBLE);
                } else {
                    holder.rightarrow.setVisibility(View.GONE);
                    holder.leftarrow.setVisibility(View.GONE);
                }

                LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                holder.suggestView.setLayoutManager(manager);
                SuggestItemAdapter adapter = new SuggestItemAdapter(getActivity(), childlist, "", suggestviewwid, onsubitemclick, holder, position);
                holder.suggestView.setAdapter(adapter);
                holder.suggestView.setHasFixedSize(true);

                DividerItemDecoration itemDivider = new DividerItemDecoration(holder.suggestView.getContext(),
                        dealManager.getOrientation());
                itemDivider.setDrawable(getResources().getDrawable(R.drawable.recycler_divider));

                holder.suggestView.addItemDecoration(itemDivider);

                //holder.suggestView.addOnItemTouchListener(recyclerItemClick(getActivity(), holder.suggestView, childlist,""));
                holder.suggestView.setNestedScrollingEnabled(false);
                holder.suggestView.addOnItemTouchListener(mScrollTouchListener);
            } else if (arrayType.equalsIgnoreCase("popular_stores")) {
                holder.mainLay.getLayoutParams().width = storeImagewidth;
                holder.itemImage.getLayoutParams().width = storeImagewidth;
                holder.bottomlay.setBackgroundResource(R.drawable.home_items_curve_shape);
                if (Items.size() == 1) {
                    holder.parentLay.getLayoutParams().width = storeImagewidth;
                }
            } else if (from.equals("deals")) {
                holder.mainLay.getLayoutParams().width = dealWidth;
                holder.itemImage.getLayoutParams().height = dealWidth;
                holder.itemImage.getLayoutParams().width = dealWidth;
                holder.paddinglay.setVisibility(View.VISIBLE);
            } else if (from.equals("slider5")) {

                holder.itemImage.getLayoutParams().width = storeImagewidth;
                holder.bottomlay.getLayoutParams().width = storeImagewidth;
                holder.mainLay.getLayoutParams().width = storeImagewidth;
                holder.bottomlay.setBackgroundResource(R.drawable.home_items_curve_shape);
                if (Items.size() == 1) {
                    holder.parentLay.getLayoutParams().width = storeImagewidth;
                }
            } else if (from.equals("slider2")) {

                //int displaywidth = (display.getWidth() * 3) / 4;
                int displaywidth = Math.round(display.getWidth() * 0.85f);
                Log.e("displaytestch", "diswidth " + displaywidth);
                int height = (displaywidth / 2) * 3;
                //   holder.mainLay.getLayoutParams().width = width;
                holder.parentLay.getLayoutParams().width = displaywidth;

                int twtpxl = FantacyApplication.dpToPx(getActivity(), 20);
                displaywidth = displaywidth - twtpxl;

                holder.itemImage.getLayoutParams().width = displaywidth;
                holder.itemImage.getLayoutParams().height = height;
                holder.bottomlay.getLayoutParams().width = displaywidth;
                holder.bottomlay.setBackgroundResource(R.drawable.home_items_curve_shape);
                holder.suggest_child.setVisibility(View.VISIBLE);
                holder.suggest_child.setBackgroundResource(R.drawable.home_items_curve_shape);
                int pxl = FantacyApplication.dpToPx(getActivity(), 10);
                holder.suggest_child.setPadding(0, pxl, 0, 0);

                int parpxl = FantacyApplication.dpToPx(getActivity(), 10);
                holder.parentLay.setPadding(parpxl, 0, parpxl, 0);

                ArrayList<HashMap<String, String>> childlist = new ArrayList();
                if (position == 0) {
                    int totalpos = Items.size() > 10 ? 10 : Items.size();
                    for (int i = 1; i < totalpos; i++) {
                        childlist.add(Items.get(i));
                        ;
                    }
                } else {
                    int validamt = (position * 10) + 10;
                    int totalpos = Items.size() > validamt ? validamt : Items.size();
                    int pos = (position * 10) + 1;
                    for (int i = pos; i < totalpos; i++) {
                        childlist.add(Items.get(i));
                        ;
                    }
                }
                //  if(Items.size()<=10){

                //}

                if (childlist.size() > 4) {
                    holder.rightarrow.setVisibility(View.VISIBLE);
                    holder.leftarrow.setVisibility(View.VISIBLE);
                } else {
                    holder.rightarrow.setVisibility(View.GONE);
                    holder.leftarrow.setVisibility(View.GONE);
                }

                setOnsubitemclick(this);

                LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                holder.suggestView.setLayoutManager(manager);
                SuggestItemAdapter adapter = new SuggestItemAdapter(getActivity(), childlist, from, displaywidth, onsubitemclick, holder, position);
                holder.suggestView.setAdapter(adapter);
                holder.suggestView.setHasFixedSize(true);

                DividerItemDecoration itemDivider = new DividerItemDecoration(holder.suggestView.getContext(),
                        dealManager.getOrientation());
                itemDivider.setDrawable(getResources().getDrawable(R.drawable.recycler_divider));

                holder.suggestView.addItemDecoration(itemDivider);

                //holder.suggestView.addOnItemTouchListener(recyclerItemClick(getActivity(), holder.suggestView, childlist,""));
                holder.suggestView.setNestedScrollingEnabled(false);
                holder.suggestView.addOnItemTouchListener(mScrollTouchListener);

            } else if (from.equals("slider3")) {
                //grid view
                int width = display.getWidth() / 2;
                int hgt = Math.round(width * 0.60f);
                holder.itemImage.getLayoutParams().height = hgt;
                holder.bottomlay.setBackgroundResource(R.drawable.home_items_curve_shape);
            } else if (from.equals("slider4")) {
                int padwidth = FantacyApplication.dpToPx(getActivity(), 15);
                int totalwid = display.getWidth() - padwidth;
                holder.itemImage.getLayoutParams().width = totalwid;
                holder.mainLay.getLayoutParams().width = totalwid;
                holder.bottomlay.getLayoutParams().width = totalwid;
                holder.description.setVisibility(View.VISIBLE);
                holder.paddinglay.setVisibility(View.VISIBLE);
                Spanned htmlAsSpanned = Html.fromHtml(tempMap.get(Constants.TAG_ITEM_DESCRIPTION)); // used by TextView
                holder.description.setText(htmlAsSpanned);

                holder.suggest_child.setVisibility(View.VISIBLE);
                holder.suggest_child.setBackgroundResource(R.drawable.home_items_curve_shape);
                int pxl = FantacyApplication.dpToPx(getActivity(), 10);
                holder.suggest_child.setPadding(0, pxl, 0, 0);
                ArrayList<HashMap<String, String>> childlist = new ArrayList();
                if (position == 0) {
                    for (int i = 1; i < Items.size(); i++) {
                        childlist.add(Items.get(i));
                        ;
                    }
                }
                setOnsubitemclick(this);

                if (childlist.size() == 0)
                    holder.suggest_child.setVisibility(View.GONE);

                if (childlist.size() > 4) {
                    holder.rightarrow.setVisibility(View.VISIBLE);
                    holder.leftarrow.setVisibility(View.VISIBLE);
                } else {
                    holder.rightarrow.setVisibility(View.GONE);
                    holder.leftarrow.setVisibility(View.GONE);
                }

                LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                holder.suggestView.setLayoutManager(manager);
                SuggestItemAdapter adapter = new SuggestItemAdapter(getActivity(), childlist, from, totalwid, onsubitemclick, holder, position);
                holder.suggestView.setAdapter(adapter);
                holder.suggestView.setHasFixedSize(true);

                DividerItemDecoration itemDivider = new DividerItemDecoration(holder.suggestView.getContext(),
                        dealManager.getOrientation());
                itemDivider.setDrawable(getResources().getDrawable(R.drawable.recycler_divider));

                holder.suggestView.addItemDecoration(itemDivider);

                //holder.suggestView.addOnItemTouchListener(recyclerItemClick(getActivity(), holder.suggestView, childlist,""));
                holder.suggestView.setNestedScrollingEnabled(false);
                holder.suggestView.addOnItemTouchListener(mScrollTouchListener);
                holder.bottomlay.setBackgroundResource(R.drawable.home_items_curve_shape);
                holder.detaillay.setBackgroundResource(R.drawable.home_whitecurve);

            } else if (from.equals("slider1")) {
                Float floathgt = Float.valueOf(itemWidth) * 1.3f;
                int hgt = Math.round(floathgt);
                holder.parentLay.getLayoutParams().width = itemWidth;
                holder.mainLay.getLayoutParams().width = itemWidth;
                holder.itemImage.getLayoutParams().height = hgt;
                holder.itemImage.getLayoutParams().width = itemWidth;
                holder.bottomlay.setBackgroundResource(R.drawable.home_items_curve_shape);
            } else {
                holder.mainLay.getLayoutParams().width = itemWidth;
                holder.itemImage.getLayoutParams().height = itemWidth;
                holder.itemImage.getLayoutParams().width = itemWidth;
            }

            if (arrayType.equalsIgnoreCase("popular_stores")) {
                String image = tempMap.get(Constants.TAG_IMAGE);
                //  image ="https:\\/\\/prodev.hitasoft.in\\/fantacy5.0\\/fantacy\\/images\\/slider\\/1586174340_1.png";
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(holder.itemImage);
                }

                String name = tempMap.get(Constants.TAG_STORE_NAME);
                holder.itemName.setText(name);
                holder.itemPrice.setText(tempMap.get(Constants.TAG_MERCHANT_NAME));
            } else {
                if (from.equalsIgnoreCase("slider2")) {

                    HashMap<String, String> testmap = new HashMap<>();
                    if (position == 0) {
                        testmap = Items.get(0);
                    } else {
                        int poscal = (position * 10);
                        testmap = Items.get(poscal);
                    }
                    String image = testmap.get(Constants.TAG_IMAGE);
                    Log.e("slider2url", "-" + image);
                    if (image != null && !image.equals("")) {
                        Picasso.get().load(image).into(holder.itemImage);
                    }
                    holder.itemName.setText(testmap.get(Constants.TAG_ITEM_TITLE));

                    if (testmap.get(Constants.TAG_DEAL_ENABLED) != null) {
                        if (testmap.get(Constants.TAG_DEAL_ENABLED).equalsIgnoreCase("yes") && FantacyApplication.isDealEnabled(testmap.get(Constants.TAG_VALID_TILL))) {
                            holder.discountPrice.setVisibility(View.VISIBLE);
                            holder.discountPrice.setPaintFlags(holder.discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            String costValue = testmap.get(Constants.TAG_PRICE);
                            float priceValue = Float.parseFloat(costValue) - (Float.parseFloat(costValue)
                                    * ((Float.parseFloat(testmap.get(Constants.TAG_DISCOUNT_PERCENTAGE))) / 100.0f));

                            holder.itemPrice.setText(testmap.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.decimal.format(priceValue));
                            holder.discountPrice.setText(testmap.get(Constants.TAG_CURRENCY) + " " + testmap.get(Constants.TAG_PRICE));
                            // if(!tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE).equalsIgnoreCase("")){
                            //  holder.discountPercent.setVisibility(View.VISIBLE);
                            //}else holder.discountPercent.setVisibility(View.GONE);
                            holder.discountPercent.setVisibility(View.GONE);
                            holder.discountPercent.setText(testmap.get(Constants.TAG_DISCOUNT_PERCENTAGE) + "% off");
                        } else {
                            holder.discountPrice.setVisibility(View.GONE);
                            holder.itemPrice.setText(testmap.get(Constants.TAG_CURRENCY) + " " + testmap.get(Constants.TAG_PRICE));
                        }
                    }
                } else {
                    String image = tempMap.get(Constants.TAG_IMAGE);
                    if (image != null && !image.equals("")) {
                        Picasso.get().load(image).into(holder.itemImage);
                    }

                }

            }

            if (!from.equalsIgnoreCase("slider2")) {
                if (tempMap.get(Constants.TAG_DEAL_ENABLED) != null) {
                    if (tempMap.get(Constants.TAG_DEAL_ENABLED).equalsIgnoreCase("yes") && FantacyApplication.isDealEnabled(tempMap.get(Constants.TAG_VALID_TILL))) {
                        holder.discountPrice.setVisibility(View.VISIBLE);
                        holder.discountPrice.setPaintFlags(holder.discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        String costValue = tempMap.get(Constants.TAG_PRICE);
                        float priceValue = Float.parseFloat(costValue) - (Float.parseFloat(costValue)
                                * ((Float.parseFloat(tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE))) / 100.0f));

                        holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.decimal.format(priceValue));
                        holder.discountPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
                        // if(!tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE).equalsIgnoreCase("")){
                        //  holder.discountPercent.setVisibility(View.VISIBLE);
                        //}else holder.discountPercent.setVisibility(View.GONE);
                        holder.discountPercent.setVisibility(View.GONE);
                        holder.discountPercent.setText(tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE) + "% off");
                    } else {
                        holder.discountPrice.setVisibility(View.GONE);
                        holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            if (from.equalsIgnoreCase("slider2")) {
                if (Items.size() == 10 || Items.size() < 10) {
                    return 1;
                } else if (Items.size() == 20 || Items.size() < 20)
                    return 2;
                else if (Items.size() == 30 || Items.size() < 30)
                    return 3;
                else if (Items.size() == 40 || Items.size() < 40)
                    return 4;
                else if (Items.size() == 50 || Items.size() < 50)
                    return 5;
                else if (Items.size() == 60 || Items.size() < 60)
                    return 6;
                else if (Items.size() == 70 || Items.size() < 70)
                    return 7;
                else if (Items.size() == 80 || Items.size() < 80)
                    return 8;
                else if (Items.size() == 90 || Items.size() < 90)
                    return 9;
                else if (Items.size() == 100 || Items.size() < 100)
                    return 10;

            } else if (from.equalsIgnoreCase("slider4")) {
                return 1;
            }
            return Items.size();
        }

        RecyclerView.OnItemTouchListener mScrollTouchListener = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                int action = e.getAction();
                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        };
    }

    public static class HtmlUtils {

        public static String escapeHtml(CharSequence text) {
            StringBuilder out = new StringBuilder();
            withinStyle(out, text, 0, text.length());
            return out.toString();
        }

        private static void withinStyle(StringBuilder out, CharSequence text,
                                        int start, int end) {
            for (int i = start; i < end; i++) {
                char c = text.charAt(i);

                if (c == '<') {
                    out.append("&lt;");
                } else if (c == '>') {
                    out.append("&gt;");
                } else if (c == '&') {
                    out.append("&amp;");
                } else if (c >= 0xD800 && c <= 0xDFFF) {
                    if (c < 0xDC00 && i + 1 < end) {
                        char d = text.charAt(i + 1);
                        if (d >= 0xDC00 && d <= 0xDFFF) {
                            i++;
                            int codepoint = 0x010000 | (int) c - 0xD800 << 10 | (int) d - 0xDC00;
                            out.append("&#").append(codepoint).append(";");
                        }
                    }
                } else if (c > 0x7E || c < ' ') {
                    out.append("&#").append((int) c).append(";");
                } else if (c == ' ') {
                    while (i + 1 < end && text.charAt(i + 1) == ' ') {
                        out.append("&nbsp;");
                        i++;
                    }

                    out.append(' ');
                } else {
                    out.append(c);
                }
            }
        }
    }

    public class SuggestItemAdapter extends RecyclerView.Adapter<SuggestItemAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;
        String from = "";
        int slide2width = 0, parentPos = 0;
        onSubItemClick onsubclick;
        RecyclerViewAdapter.MyViewHolder myholder;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView itemName, itemPrice, discountPrice;
            CornerImageView itemImage;
            RecyclerView suggestView;
            RelativeLayout mainLay;
            LinearLayout suggest_child, bottomlay, detaillay;

            public MyViewHolder(View view) {
                super(view);

                itemName = (TextView) view.findViewById(R.id.itemName);
                itemPrice = (TextView) view.findViewById(R.id.itemPrice);
                itemImage = (CornerImageView) view.findViewById(R.id.itemImage);
                suggestView = (RecyclerView) view.findViewById(R.id.suggest_view);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);
                suggest_child = (LinearLayout) view.findViewById(R.id.suggest_child);
                bottomlay = (LinearLayout) view.findViewById(R.id.bottomLay);
                discountPrice = (TextView) view.findViewById(R.id.discountPrice);

                bottomlay.setVisibility(View.GONE);
                discountPrice.setPaintFlags(discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                //              if(from.equalsIgnoreCase("slider4")){
                onsubclick.onItemClick(getAdapterPosition(), Items.get(getAdapterPosition()), myholder, parentPos);
               /* }else{
                    Intent i = new Intent(getActivity(), DetailActivity.class);
                    i.putExtra("items", Items);
                    i.putExtra("position", getAdapterPosition());
                    startActivity(i);

                }
*/
            }
        }

        public void replaceItem(int pos, HashMap<String, String> map) {
            Log.e("replaceItemss", "-" + map);
            Items.set(pos, map);
            notifyDataSetChanged();
        }

        public SuggestItemAdapter(Context context, ArrayList<HashMap<String, String>> Items, String from, int width, onSubItemClick onsubclick, RecyclerViewAdapter.MyViewHolder holder, int parentPos) {
            this.Items = Items;
            this.context = context;
            this.from = from;
            this.slide2width = width;
            this.onsubclick = onsubclick;
            this.myholder = holder;
            this.parentPos = parentPos;
        }

        @Override
        public SuggestItemAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.suggest_item, parent, false);

            return new SuggestItemAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final SuggestItemAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.itemName.setText(tempMap.get(Constants.TAG_ITEM_TITLE));

            int itemwid = (storeImagewidth - FantacyApplication.dpToPx(getActivity(), 9)) / 4;
            int suggestwid = (storeImagewidth - FantacyApplication.dpToPx(getActivity(), 29)) / 4;

            if (from.equalsIgnoreCase("slider2")) {
                int sliderwid = (slide2width - FantacyApplication.dpToPx(getActivity(), 9)) / 4;
                holder.mainLay.getLayoutParams().width = sliderwid;
                holder.itemImage.getLayoutParams().height = (itemwid * 3) / 4;
                holder.itemImage.getLayoutParams().width = sliderwid;
            } else if (from.equalsIgnoreCase("slider4")) {
                int sliderwid = (slide2width - FantacyApplication.dpToPx(getActivity(), 9)) / 4;
                holder.mainLay.getLayoutParams().width = sliderwid;
                holder.itemImage.getLayoutParams().height = (itemwid * 3) / 4;
                holder.itemImage.getLayoutParams().width = sliderwid;
            } else {
                holder.mainLay.getLayoutParams().width = suggestwid;
                holder.itemImage.getLayoutParams().height = Math.round(suggestwid * 0.7f);
                holder.itemImage.getLayoutParams().width = suggestwid;
            }


            String image = tempMap.get(Constants.TAG_IMAGE);
            if (image != null && !image.equals("")) {
                Picasso.get().load(image).into(holder.itemImage);
            }


           /* if (tempMap.get(Constants.TAG_DEAL_ENABLED).equalsIgnoreCase("yes") && FantacyApplication.isDealEnabled(tempMap.get(Constants.TAG_VALID_TILL))) {
                holder.discountPrice.setVisibility(View.VISIBLE);
                String costValue = tempMap.get(Constants.TAG_PRICE);
                float priceValue = Float.parseFloat(costValue) - (Float.parseFloat(costValue)
                        * ((Float.parseFloat(tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE))) / 100.0f));

                holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.decimal.format(priceValue));
                holder.discountPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
            } else {
                holder.discountPrice.setVisibility(View.GONE);
                holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
            }*/
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }
    }


    public class StoreViewAdapter extends RecyclerView.Adapter<StoreViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView storeImage;
            TextView itemName;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                storeImage = (ImageView) view.findViewById(R.id.storeImage);
                itemName = (TextView) view.findViewById(R.id.itemName);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);
            }
        }

        public StoreViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public StoreViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_store, parent, false);

            return new StoreViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final StoreViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.storeImage.getLayoutParams().width = storeImagewidth;
            String image = tempMap.get(Constants.TAG_IMAGE);
            //  image ="https:\\/\\/prodev.hitasoft.in\\/fantacy5.0\\/fantacy\\/images\\/slider\\/1586174340_1.png";
            if (image != null && !image.equals("")) {
                Picasso.get().load(image).into(holder.storeImage);
            }

            String name = tempMap.get(Constants.TAG_STORE_NAME);
            holder.itemName.setText(name);
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }
    }


    private RecyclerItemClickListener categoryItemClick(Context context, RecyclerView recyclerView) {

        RecyclerItemClickListener recyclerItemClickListener = new RecyclerItemClickListener(context, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putString("from", "home");
                bundle.putInt("position", position);
                CategoryFragment fragment = new CategoryFragment();
                fragment.setArguments(bundle);
                FragmentMainActivity.switchFragment(getActivity(), fragment);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        });

        return recyclerItemClickListener;
    }

    private RecyclerItemClickListener recyclerItemClick(Context context, final RecyclerView recyclerView, final ArrayList<HashMap<String, String>> Array, final String arrayType) {

        RecyclerItemClickListener recyclerItemClickListener = new RecyclerItemClickListener(context, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (arrayType.equalsIgnoreCase("popular_stores")) {
                    Intent i = new Intent(getActivity(), StoreProfile.class);
                    i.putExtra("storeId", Array.get(position).get(Constants.TAG_STORE_ID));
                    startActivity(i);
                } else {
                    Log.e("detail", "product");
                    Intent i = new Intent(getActivity(), DetailActivity.class);
                    i.putExtra("items", Array);
                    i.putExtra("position", position);
                    startActivity(i);
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        });

        return recyclerItemClickListener;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler = new Handler();
        mHandler.post(mRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.popularAll:
                startIntent(popularAll.getTag() + "");
                break;
            case R.id.dealAll:
            case R.id.dealView:
                Intent j = new Intent(getActivity(), ViewAllActivity.class);
                j.putExtra("from", getString(R.string.daily_deals));
                j.putExtra("key", "deals");
                startActivity(j);
                break;
            case R.id.recentAll:
                startIntent(recentAll.getTag() + "");
                break;
            case R.id.featuredAll:
                startIntent(featuredAll.getTag() + "");
                break;
            case R.id.storeAll:
                startIntent(storeAll.getTag() + "");
                break;
            case R.id.discountall:
                startIntent(discountall.getTag() + "");
                break;
            case R.id.suggestall:
                startIntent(suggestall.getTag() + "");
                break;
            case R.id.categoryproall:
                startIntent(categoryproall.getTag() + "");
                break;
            case R.id.toprateall:
                startIntent(toprateall.getTag() + "");
                break;
        }
    }


    public void startIntent(String data) {
        switch (data) {
            case "categories":
                Intent i = new Intent(getActivity(), ViewAllActivity.class);
                i.putExtra("from", "category_products");
                i.putExtra("key", "categories");
                startActivity(i);
                break;
            case "recently_added":
                Intent j = new Intent(getActivity(), ViewAllActivity.class);
                j.putExtra("from", getString(R.string.new_arrivals));
                j.putExtra("key", "recent");
                startActivity(j);
                break;
            case "discounts":
                Intent n = new Intent(getActivity(), ViewAllActivity.class);
                n.putExtra("from", "Discounts");
                n.putExtra("key", "discounts");
                startActivity(n);
                break;
            case "featured_items":
                Intent k = new Intent(getActivity(), ViewAllActivity.class);
                k.putExtra("from", getString(R.string.featured));
                k.putExtra("key", "featured");
                startActivity(k);
                break;
            case "suggested_items":
                Intent o = new Intent(getActivity(), ViewAllActivity.class);
                o.putExtra("from", "Suggested Items");
                o.putExtra("key", "suggestitems");
                startActivity(o);
                break;

            case "top_rated":
                Intent q = new Intent(getActivity(), ViewAllActivity.class);
                q.putExtra("from", "Top Rated");
                q.putExtra("key", "top_rated");
                startActivity(q);
                break;

            case "popular_stores":
                Intent m = new Intent(getActivity(), ViewAllStores.class);
                m.putExtra("from", getString(R.string.popular_store));
                startActivity(m);
                break;
            case "most_popular":
                Intent l = new Intent(getActivity(), ViewAllActivity.class);
                l.putExtra("from", getString(R.string.popular_products));
                l.putExtra("key", "popular");
                startActivity(l);
                break;

            default:
                Intent s = new Intent(getActivity(), ViewAllActivity.class);
                s.putExtra("from", getString(R.string.popular_products));
                s.putExtra("key", "popular");
                startActivity(s);

        }

    }


    private void getHomeData2() {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_HOME2, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                JSONObject ascendOrderObject = null, designObject = null;
                try {
                    Log.v(TAG, "getHomeDataRes2=" + "" + res);
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }


                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {

                        JSONObject productObject = json.getJSONObject("item_lists");

                        ascendOrderObject = json.getJSONObject("ascending_order");

                        designObject = json.getJSONObject("layout_design");

                        //resetAry();

                        JSONArray popular = productObject.has(Constants.TAG_POPULAR_PRODUCTS) ? productObject.getJSONArray(Constants.TAG_POPULAR_PRODUCTS) : new JSONArray();
                        ItemsParsing popularItems = new ItemsParsing(getActivity());
                        if (popular.length() > 0)
                            popularAry.addAll(popularItems.getItems(popular));


                        JSONArray recent = productObject.has(Constants.TAG_RECENT_PRODUCTS) ? productObject.getJSONArray(Constants.TAG_RECENT_PRODUCTS) : new JSONArray();
                        ItemsParsing recentItems = new ItemsParsing(getActivity());
                        if (recent.length() > 0)
                            recentAry.addAll(recentItems.getItems(recent));

                        JSONArray featured = productObject.has(Constants.TAG_FEATURED_PRODUCTS) ? productObject.getJSONArray(Constants.TAG_FEATURED_PRODUCTS) : new JSONArray();
                        ItemsParsing featuredItems = new ItemsParsing(getActivity());
                        if (featured.length() > 0)
                            featuredAry.addAll(featuredItems.getItems(featured));

                        JSONArray store = productObject.has(Constants.TAG_POPULAR_STORE) ? productObject.getJSONArray(Constants.TAG_POPULAR_STORE) : new JSONArray();
                        for (int i = 0; i < store.length(); i++) {
                            JSONObject temp = store.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();

                            String store_id = DefensiveClass.optString(temp, Constants.TAG_STORE_ID);
                            String store_name = DefensiveClass.optString(temp, Constants.TAG_STORE_NAME);
                            String wifi = DefensiveClass.optString(temp, Constants.TAG_WIFI);
                            String merchant_name = DefensiveClass.optString(temp, Constants.TAG_MERCHANT_NAME);
                            String stat = DefensiveClass.optString(temp, Constants.TAG_STATUS);
                            String image = DefensiveClass.optString(temp, Constants.TAG_IMAGE);

                            map.put(Constants.TAG_STORE_ID, store_id);
                            map.put(Constants.TAG_STORE_NAME, store_name);
                            map.put(Constants.TAG_WIFI, wifi);
                            map.put(Constants.TAG_MERCHANT_NAME, merchant_name);
                            map.put(Constants.TAG_STATUS, stat);
                            map.put(Constants.TAG_IMAGE, image);

                            storeAry.add(map);
                        }


                        JSONArray suggested = productObject.has(Constants.TAG_SUGGESTEDITEMS) ? productObject.getJSONArray(Constants.TAG_SUGGESTEDITEMS) : new JSONArray();
                        ItemsParsing suggestedItems = new ItemsParsing(getActivity());
                        if (suggested.length() > 0)
                            suggestAry.addAll(suggestedItems.getSuggestedItems(suggested));

                        JSONArray discounts = productObject.has(Constants.TAG_DISCOUNTS) ? productObject.getJSONArray(Constants.TAG_DISCOUNTS) : new JSONArray();
                        ItemsParsing discountItems = new ItemsParsing(getActivity());
                        if (discounts.length() > 0)
                            discountAry.addAll(discountItems.getItems(discounts));

                        JSONArray categoryproducts = productObject.has(Constants.TAG_CATEGORYPRODUCT) ? productObject.getJSONArray(Constants.TAG_CATEGORYPRODUCT) : new JSONArray();
                        ItemsParsing categoryproductItems = new ItemsParsing(getActivity());
                        if (categoryproducts.length() > 0)
                            categoryProductAry.addAll(categoryproductItems.getItems(categoryproducts));

                        JSONArray toprated = productObject.has(Constants.TAG_TOPRATED) ? productObject.getJSONArray(Constants.TAG_TOPRATED) : new JSONArray();
                        ItemsParsing topratedItems = new ItemsParsing(getActivity());
                        if (toprated.length() > 0)
                            topratedAry.addAll(topratedItems.getItems(toprated));

                        getHomeData3();

                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           /* Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                            /*Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (categoryAry.size() == 0 && bannerAry.size() == 0 && dealAry.size() == 0 && popularAry.size() == 0 &&
                        recentAry.size() == 0 && featuredAry.size() == 0 && storeAry.size() == 0) {
                    mainLay.setVisibility(View.GONE);
                    progressLay.setVisibility(View.GONE);
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    setData(ascendOrderObject, designObject, 1);

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getHomeDataError: " + error.getMessage());
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                if (categoryAry.size() == 0 && bannerAry.size() == 0 && dealAry.size() == 0 && popularAry.size() == 0 &&
                        recentAry.size() == 0 && featuredAry.size() == 0 && storeAry.size() == 0) {
                    mainLay.setVisibility(View.GONE);
                    progressLay.setVisibility(View.GONE);
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    hideHomeData();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                if (customerId != null) {
                    map.put("user_id", customerId);
                }
                Log.i(TAG, "getHomeDataParams: " + map);
                return map;
            }

            /*@Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJodHRwczpcL1wvbGFhbHNvYnVqLmNvbSIsInN1YiI6ImxhYWxzb2J1ai5jb20iLCJpYXQiOjE2MTMzODE1NTcsImV4cCI6MTYxMzk4NjM1NywiaWQiOjMwLCJlbWFpbCI6Im1vaGFtbWFkc2hvYm9vekBnbWFpbC5jb20ifQ.AzERLlbaikHdEkvCX_93m7RAwhZgHgPodXgvP4t3tHaNnPg7KRfkJZpXONa72_-DS3KG0uJrHyeOIFQx-JYWWA");
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }*/
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }


    private void getHomeData3() {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_HOME3, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                JSONObject ascendOrderObject = null, designObject = null;
                try {
                    Log.v(TAG, "getHomeDataRes3=" + res);
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }


                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {

                        JSONObject productObject = json.getJSONObject("item_lists");

                        ascendOrderObject = json.getJSONObject("ascending_order");

                        designObject = json.getJSONObject("layout_design");

                        //resetAry();

                        JSONArray popular = productObject.has(Constants.TAG_POPULAR_PRODUCTS) ? productObject.getJSONArray(Constants.TAG_POPULAR_PRODUCTS) : new JSONArray();
                        ItemsParsing popularItems = new ItemsParsing(getActivity());
                        if (popular.length() > 0)
                            popularAry.addAll(popularItems.getItems(popular));


                        JSONArray recent = productObject.has(Constants.TAG_RECENT_PRODUCTS) ? productObject.getJSONArray(Constants.TAG_RECENT_PRODUCTS) : new JSONArray();
                        ItemsParsing recentItems = new ItemsParsing(getActivity());
                        if (recent.length() > 0)
                            recentAry.addAll(recentItems.getItems(recent));

                        JSONArray featured = productObject.has(Constants.TAG_FEATURED_PRODUCTS) ? productObject.getJSONArray(Constants.TAG_FEATURED_PRODUCTS) : new JSONArray();
                        ItemsParsing featuredItems = new ItemsParsing(getActivity());
                        if (featured.length() > 0)
                            featuredAry.addAll(featuredItems.getItems(featured));

                        JSONArray store = productObject.has(Constants.TAG_POPULAR_STORE) ? productObject.getJSONArray(Constants.TAG_POPULAR_STORE) : new JSONArray();
                        for (int i = 0; i < store.length(); i++) {
                            JSONObject temp = store.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();

                            String store_id = DefensiveClass.optString(temp, Constants.TAG_STORE_ID);
                            String store_name = DefensiveClass.optString(temp, Constants.TAG_STORE_NAME);
                            String wifi = DefensiveClass.optString(temp, Constants.TAG_WIFI);
                            String merchant_name = DefensiveClass.optString(temp, Constants.TAG_MERCHANT_NAME);
                            String stat = DefensiveClass.optString(temp, Constants.TAG_STATUS);
                            String image = DefensiveClass.optString(temp, Constants.TAG_IMAGE);

                            map.put(Constants.TAG_STORE_ID, store_id);
                            map.put(Constants.TAG_STORE_NAME, store_name);
                            map.put(Constants.TAG_WIFI, wifi);
                            map.put(Constants.TAG_MERCHANT_NAME, merchant_name);
                            map.put(Constants.TAG_STATUS, stat);
                            map.put(Constants.TAG_IMAGE, image);

                            storeAry.add(map);
                        }


                        JSONArray suggested = productObject.has(Constants.TAG_SUGGESTEDITEMS) ? productObject.getJSONArray(Constants.TAG_SUGGESTEDITEMS) : new JSONArray();
                        ItemsParsing suggestedItems = new ItemsParsing(getActivity());
                        if (suggested.length() > 0)
                            suggestAry.addAll(suggestedItems.getSuggestedItems(suggested));

                        JSONArray discounts = productObject.has(Constants.TAG_DISCOUNTS) ? productObject.getJSONArray(Constants.TAG_DISCOUNTS) : new JSONArray();
                        ItemsParsing discountItems = new ItemsParsing(getActivity());
                        if (discounts.length() > 0)
                            discountAry.addAll(discountItems.getItems(discounts));

                        JSONArray categoryproducts = productObject.has(Constants.TAG_CATEGORYPRODUCT) ? productObject.getJSONArray(Constants.TAG_CATEGORYPRODUCT) : new JSONArray();
                        ItemsParsing categoryproductItems = new ItemsParsing(getActivity());
                        if (categoryproducts.length() > 0)
                            categoryProductAry.addAll(categoryproductItems.getItems(categoryproducts));

                        JSONArray toprated = productObject.has(Constants.TAG_TOPRATED) ? productObject.getJSONArray(Constants.TAG_TOPRATED) : new JSONArray();
                        ItemsParsing topratedItems = new ItemsParsing(getActivity());
                        if (toprated.length() > 0)
                            topratedAry.addAll(topratedItems.getItems(toprated));


                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           /* Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                            /*Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (categoryAry.size() == 0 && bannerAry.size() == 0 && dealAry.size() == 0 && popularAry.size() == 0 &&
                        recentAry.size() == 0 && featuredAry.size() == 0 && storeAry.size() == 0) {
                    mainLay.setVisibility(View.GONE);
                    progressLay.setVisibility(View.GONE);
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    setData(ascendOrderObject, designObject, 2);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getHomeDataError: " + error.getMessage());
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                if (categoryAry.size() == 0 && bannerAry.size() == 0 && dealAry.size() == 0 && popularAry.size() == 0 &&
                        recentAry.size() == 0 && featuredAry.size() == 0 && storeAry.size() == 0) {
                    mainLay.setVisibility(View.GONE);
                    progressLay.setVisibility(View.GONE);
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    hideHomeData();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                if (customerId != null) {
                    map.put("user_id", customerId);
                }
                Log.i(TAG, "getHomeDataParams: " + map);
                return map;
            }

            /*@Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJodHRwczpcL1wvbGFhbHNvYnVqLmNvbSIsInN1YiI6ImxhYWxzb2J1ai5jb20iLCJpYXQiOjE2MTMzODE1NTcsImV4cCI6MTYxMzk4NjM1NywiaWQiOjMwLCJlbWFpbCI6Im1vaGFtbWFkc2hvYm9vekBnbWFpbC5jb20ifQ.AzERLlbaikHdEkvCX_93m7RAwhZgHgPodXgvP4t3tHaNnPg7KRfkJZpXONa72_-DS3KG0uJrHyeOIFQx-JYWWA");
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }*/

        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

}
