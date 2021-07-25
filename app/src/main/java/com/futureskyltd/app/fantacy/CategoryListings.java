package com.futureskyltd.app.fantacy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.GridRecyclerOnScrollListener;
import com.futureskyltd.app.external.RangeSeekBar;
import com.futureskyltd.app.helper.CategoryRefreshListener;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.ItemsParsing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by hitasoft on 3/6/17.
 */

public class CategoryListings extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, CategoryRefreshListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, cartBtn, searchBtn, sortImage, filterImage;
    TextView title, sort, filter, clear, apply, minPrice, maxPrice, distance;
    RecyclerView recyclerView, sortList, sectionList, contentList;
    LinearLayout sortLay, filterLay, sortOptionLay, priceLay, distanceLay, priceSeekLay;
    RelativeLayout filterOptionLay;
    Display display;
    SeekBar distanceBar;
    RangeSeekBar priceBar;
    int itemWidth, itemHeight, sortPosition;
    String catId = "", catName = "", catState = "", subcatId = "", searchKey = "", supercatId = "";
    RecyclerViewAdapter itemAdapter;
    SortViewAdapter sortViewAdapter;
    SectionViewAdapter sectionViewAdapter;
    ContentViewAdapter contentViewAdapter;
    LinearLayoutManager sortManager, sectionManager, contentManager;
    GridLayoutManager itemManager;
    ArrayList<HashMap<String, String>> itemsAry = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> categoryAry = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> productTypeAry = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> colorAry = new ArrayList<HashMap<String, String>>();
    ArrayList<String> selectedCat = new ArrayList<String>();
    ArrayList<String> selectedProductType = new ArrayList<String>();
    private GridRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    RelativeLayout progressLay, nullLay;
    ImageView nullImage;
    TextView nullText;
    Boolean affliate_only = false, buy_only = false;
    String stringCat = "", stringColor = "", selectedsort = "popularity", from = "", sectionPosition = "";
    private Location mylocation;
    private GoogleApiClient googleApiClient;
    private final static int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;
    private int priceMinimum = 1;
    private int priceMaximum = 5000;
    SharedPreferences preferences;
    Map<String, Map<String, String>> getRetMainMap;
    private String localCartCount ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_listing_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        cartBtn = (ImageView) findViewById(R.id.cartBtn);
        searchBtn = (ImageView) findViewById(R.id.searchBtn);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        sortLay = (LinearLayout) findViewById(R.id.sortLay);
        filterLay = (LinearLayout) findViewById(R.id.filterLay);
        sortOptionLay = (LinearLayout) findViewById(R.id.sortOptionLay);
        sortList = (RecyclerView) findViewById(R.id.sortList);
        sort = (TextView) findViewById(R.id.sort);
        filter = (TextView) findViewById(R.id.filter);
        sortImage = (ImageView) findViewById(R.id.sortImage);
        filterImage = (ImageView) findViewById(R.id.filterImage);
        sectionList = (RecyclerView) findViewById(R.id.sectionList);
        contentList = (RecyclerView) findViewById(R.id.contentList);
        filterOptionLay = (RelativeLayout) findViewById(R.id.filterOptionLay);
        clear = (TextView) findViewById(R.id.clear);
        apply = (TextView) findViewById(R.id.apply);
        minPrice = (TextView) findViewById(R.id.minPrice);
        maxPrice = (TextView) findViewById(R.id.maxPrice);
        distance = (TextView) findViewById(R.id.distance);
        priceLay = (LinearLayout) findViewById(R.id.priceLay);
        distanceLay = (LinearLayout) findViewById(R.id.distanceLay);
        priceSeekLay = (LinearLayout) findViewById(R.id.priceSeekLay);
        distanceBar = (SeekBar) findViewById(R.id.distanceBar);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        nullImage = (ImageView) findViewById(R.id.nullImage);
        nullText = (TextView) findViewById(R.id.nullText);
        progressLay = (RelativeLayout) findViewById(R.id.progress);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(CategoryListings.this, R.color.progresscolor));


        from = (String) getIntent().getExtras().get("from");
        if (from.equals("category")) {
            catState = (String) getIntent().getExtras().get("catState");
            catId = (String) getIntent().getExtras().get("catId");
            catName = (String) getIntent().getExtras().get("catName");
            if (catState.equals("sub") || catState.equals("super")) {
                subcatId = (String) getIntent().getExtras().get("subcatId");
            }
            if (catState.equals("super")) {
                supercatId = (String) getIntent().getExtras().get("supercatId");
                selectedCat.add(supercatId);
            }

            title.setText(catName);
        } else {
            searchKey = (String) getIntent().getExtras().get("searchKey");
            if (searchKey.equals(null)) {
                searchKey = "";
            }

            if (from.equals("search")) {
                title.setText(searchKey);
            } else {
                title.setText(getString(R.string.barcode));
            }
        }

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
        //FragmentMainActivity.setCartBadge(findViewById(R.id.parentLay),DetailActivity.this, localCartCount);
        FragmentMainActivity.setCartBadge(findViewById(R.id.parentLay), CategoryListings.this);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        cartBtn.setVisibility(View.VISIBLE);
        searchBtn.setVisibility(View.VISIBLE);

        sortLay.setOnClickListener(this);
        filterLay.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        sortOptionLay.setOnClickListener(this);
        filterOptionLay.setOnClickListener(this);
        clear.setOnClickListener(this);
        apply.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        cartBtn.setOnClickListener(this);

        display = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        itemWidth = (displayMetrics.widthPixels * 50 / 100) - FantacyApplication.dpToPx(this, 1);
        itemHeight = displayMetrics.heightPixels * 60 / 100;

        recyclerView.setHasFixedSize(true);
        itemManager = new GridLayoutManager(CategoryListings.this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);


        itemAdapter = new RecyclerViewAdapter(CategoryListings.this, itemsAry);
        recyclerView.setAdapter(itemAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                getAllItems(0);
            }
        });
        mScrollListener = new GridRecyclerOnScrollListener(itemManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getAllItems(current_page * Constants.OVERALL_LIMIT);
                    Log.v(TAG, "onLoadMore offset" + (Constants.OVERALL_LIMIT * current_page));
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        itemManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (itemAdapter.getItemViewType(position) == 0) ? 1 : 2;
            }

        });

        getAllItems(0);

        String[] sortAry = new String[]{getString(R.string.popularity), getString(R.string.price_low_high), getString(R.string.price_high_low), getString(R.string.newest), getString(R.string.oldest)};
        sortList.setHasFixedSize(true);
        sortManager = new LinearLayoutManager(CategoryListings.this, LinearLayoutManager.VERTICAL, false);
        sortList.setLayoutManager(sortManager);
        sortViewAdapter = new SortViewAdapter(CategoryListings.this, sortAry);
        sortList.setAdapter(sortViewAdapter);

        sectionPosition = getString(R.string.categories);
        String[] sectionAry = new String[]{getString(R.string.categories), getString(R.string.price), getString(R.string.color), getString(R.string.near_me)};
        sectionList.setHasFixedSize(true);
        sectionManager = new LinearLayoutManager(CategoryListings.this, LinearLayoutManager.VERTICAL, false);
        sectionList.setLayoutManager(sectionManager);

        DividerItemDecoration sectionDivider = new DividerItemDecoration(CategoryListings.this, sectionManager.getOrientation());
        sectionDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        sectionList.addItemDecoration(sectionDivider);

        sectionViewAdapter = new SectionViewAdapter(CategoryListings.this, sectionAry);
        sectionList.setAdapter(sectionViewAdapter);

        contentList.setHasFixedSize(true);
        contentManager = new LinearLayoutManager(CategoryListings.this, LinearLayoutManager.VERTICAL, false);
        contentList.setLayoutManager(contentManager);

        contentViewAdapter = new ContentViewAdapter(sectionPosition, CategoryListings.this, categoryAry);
        contentList.setAdapter(contentViewAdapter);

        getProductType();

        priceBar = new RangeSeekBar<Integer>(priceMinimum, priceMaximum, CategoryListings.this);
        priceBar.setSelectedMinValue(priceMinimum);
        priceBar.setSelectedMaxValue(priceMaximum);
        priceBar.setDefaultColor(getResources().getColor(R.color.colorPrimary));
        priceBar.setNotifyWhileDragging(true);
        priceBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                // handle changed range values
                minPrice.setText("$ " + minValue);
                maxPrice.setText("$ " + maxValue);
            }

        });
        priceSeekLay.addView(priceBar);

        distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                distance.setText(i + " M.");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getAllItems(final int offset) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_ITEMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.d(TAG, "getAllItemsRes=" + offset + " " + res);
                    JSONObject json = new JSONObject(res);
                    progressLay.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    mSwipeRefreshLayout.setEnabled(true);
                    if (itemsAry.size() >= Constants.OVERALL_LIMIT && itemsAry.get(itemsAry.size() - 1) == null) {
                        itemsAry.remove(itemsAry.size() - 1);
                        itemAdapter.notifyItemRemoved(itemsAry.size());
                    }
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        itemsAry.clear();
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        if (offset == 0) {
                            itemsAry.clear();
                        }
                        JSONArray items = json.getJSONArray(Constants.TAG_ITEMS);
                        ItemsParsing allItems = new ItemsParsing(CategoryListings.this);
                        itemsAry.addAll(allItems.getItems(items));
                        if (mScrollListener != null && itemsAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           /* Intent i = new Intent(CategoryListings.this, PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                            /*Intent i = new Intent(CategoryListings.this, PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                        }
                    }
                    itemAdapter.notifyDataSetChanged();
                    if (itemsAry.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_item);
                        nullText.setText(getString(R.string.no_item));
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
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
                setErrorLayout();
                Log.e(TAG, "getAllItemsError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                CategoryListings.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (itemsAry.size() >= Constants.OVERALL_LIMIT) {
                            itemsAry.add(null);
                            itemAdapter.notifyItemInserted(itemsAry.size() - 1);
                        } else if (itemsAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                            progressLay.setVisibility(View.VISIBLE);
                            nullLay.setVisibility(View.GONE);
                            mSwipeRefreshLayout.setEnabled(false);
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();

                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                if (from.equals("barcode")) {
                    map.put("barcode", searchKey);
                } else {
                    map.put("search_key", searchKey);
                    map.put("sort", selectedsort);
                }
                if(accesstoken != null)
                    map.put("user_id", customerId);
                if (catState != null && !catState.equals(""))
                    map.put("category_id", catId);
                if (catState.equals("sub") || catState.equals("super"))
                    map.put("subcat_id", subcatId);
                if (catState.equals("super"))
                    map.put("supercat_id", supercatId);
                if (!stringCat.equals("") && catState.equals("super"))
                    map.put("supercat_id", stringCat);
                if (!stringColor.equals(""))
                    map.put("color", stringColor);

                if (distanceBar.getProgress() != 0 && mylocation != null) {
                    map.put("distance", String.valueOf(distanceBar.getProgress()));
                    map.put("lat", String.valueOf(mylocation.getLatitude()));
                    map.put("lon", String.valueOf(mylocation.getLongitude()));
                }

                try {
                    if (priceBar != null) {
                        if (!String.valueOf(priceBar.getSelectedMinValue()).equals("1") || !String.valueOf(priceBar.getSelectedMaxValue()).equals("5000")) {
                            map.put("price_min", String.valueOf(priceBar.getSelectedMinValue()));
                            map.put("price_max", String.valueOf(priceBar.getSelectedMaxValue()));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "getAllItemsParams=" + map);
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
        if (itemsAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
        }
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setEnabled(true);
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            ProgressBar progressBar;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView itemName, itemPrice, discountPrice;
            ImageView itemImage, likedBtn;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                itemName = (TextView) view.findViewById(R.id.itemName);
                itemPrice = (TextView) view.findViewById(R.id.itemPrice);
                itemImage = (ImageView) view.findViewById(R.id.itemImage);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);
                likedBtn = (ImageView) view.findViewById(R.id.likedBtn);
                discountPrice = (TextView) view.findViewById(R.id.discountPrice);

                discountPrice.setPaintFlags(discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                likedBtn.setOnClickListener(this);
                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                String accesstoken = preferences.getString("TOKEN", null);
                switch (v.getId()) {
                    case R.id.likedBtn:
                        if (accesstoken != null) {
                            String liked = Items.get(getAdapterPosition()).get(Constants.TAG_LIKED);
                            if (liked.equalsIgnoreCase("yes")) {
                                Items.get(getAdapterPosition()).put(Constants.TAG_LIKED, "no");
                            } else {
                                Items.get(getAdapterPosition()).put(Constants.TAG_LIKED, "yes");
                            }
                            notifyDataSetChanged();
                        } else {
                            Intent login = new Intent(context, LoginActivity.class);
                            startActivity(login);
                        }
                        break;
                    case R.id.mainLay:
                        Intent i = new Intent(context, DetailActivity.class);
                        i.putExtra("items", itemsAry);
                        i.putExtra("position", getAdapterPosition());
                        if(from!=null) {
                            String type = "";
                            if(from.equalsIgnoreCase("search")){
                                type ="search";
                            }else {
                                type = "";
                            }
                            i.putExtra("type", type);
                        }
                        startActivity(i);
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
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items, parent, false);
                return new MyViewHolder(itemView);
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
                holder.itemName.setText(tempMap.get(Constants.TAG_ITEM_TITLE));

                holder.mainLay.getLayoutParams().width = itemWidth;
                holder.itemImage.getLayoutParams().height = itemWidth;
                if (position % 2 == 0) {
                    holder.mainLay.setPadding(FantacyApplication.dpToPx(context, 1), 0, 0, FantacyApplication.dpToPx(context, 1));
                } else {
                    holder.mainLay.setPadding(0, 0, 0, FantacyApplication.dpToPx(context, 1));
                }

                String image = tempMap.get(Constants.TAG_IMAGE);
                if (!image.equals("")) {
                    Picasso.get().load(image).into(holder.itemImage);
                }
                if (tempMap.get(Constants.TAG_LIKED).equalsIgnoreCase("yes")) {
                    holder.likedBtn.setImageResource(R.drawable.liked);
                } else {
                    holder.likedBtn.setImageResource(R.drawable.unlike);
                }

                if (tempMap.get(Constants.TAG_DEAL_ENABLED).equalsIgnoreCase("yes") && FantacyApplication.isDealEnabled(tempMap.get(Constants.TAG_VALID_TILL))) {
                    holder.discountPrice.setVisibility(View.VISIBLE);
                    String costValue = tempMap.get(Constants.TAG_PRICE);
                    float priceValue = Float.parseFloat(costValue) - (Float.parseFloat(costValue)
                            * ((Float.parseFloat(tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE))) / 100.0f));

                    holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.decimal.format(priceValue));
                    holder.discountPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
                } else {
                    holder.discountPrice.setVisibility(View.GONE);
                    holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
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
    }


    public class SortViewAdapter extends RecyclerView.Adapter<SortViewAdapter.MyViewHolder> {

        String[] data = new String[5];
        Context context;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView name;
            ImageView icon;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                name = (TextView) view.findViewById(R.id.name);
                icon = (ImageView) view.findViewById(R.id.icon);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);

                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mainLay:
                        sortPosition = getAdapterPosition();
                        notifyDataSetChanged();

                        switch (sortPosition) {
                            case 0:
                                selectedsort = "popularity";
                                break;
                            case 1:
                                selectedsort = "lowtohigh";
                                break;
                            case 2:
                                selectedsort = "hightolow";
                                break;
                            case 3:
                                selectedsort = "newest";
                                break;
                            case 4:
                                selectedsort = "oldest";
                                break;
                            default:
                                selectedsort = "popularity";
                                break;
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sortOptionLay.setVisibility(View.GONE);

                                itemsAry.clear();
                                itemAdapter.notifyDataSetChanged();
                                mScrollListener.resetpagecount();
                                getAllItems(0);
                            }
                        }, 200);

                        break;
                }
            }
        }

        public SortViewAdapter(Context context, String[] temp) {
            this.data = temp;
            this.context = context;
        }

        @Override
        public SortViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.category_first_row, parent, false);

            return new SortViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final SortViewAdapter.MyViewHolder holder, int position) {

            holder.name.setText(data[position]);

            if (position == sortPosition) {
                holder.icon.setImageResource(R.drawable.radio_select);
            } else {
                holder.icon.setImageResource(R.drawable.radio_unselect);
            }
        }

        @Override
        public int getItemCount() {
            return data.length;
        }
    }

    private void getCategory() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PRODUCT_BEFORE_ADD, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    categoryAry.clear();
                    colorAry.clear();
                    Log.i(TAG, "getCategoryRes: " + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray category = json.getJSONArray(Constants.TAG_CATEGORY);
                        for (int i = 0; i < category.length(); i++) {
                            JSONArray sub_category = category.getJSONObject(i).optJSONArray(Constants.TAG_SUB_CATEGORY);
                            for (int j = 0; j < sub_category.length(); j++) {
                                JSONObject sub = sub_category.getJSONObject(j);
                                JSONArray super_category = sub.optJSONArray(Constants.TAG_SUPER_CATEGORY);
                                if (super_category.toString().contains(supercatId) && catState.equals("super")) {
                                    //    subCatId = subid;
                                    for (int k = 0; k < super_category.length(); k++) {
                                        JSONObject supercat = super_category.getJSONObject(k);
                                        String superid = DefensiveClass.optString(supercat, Constants.TAG_ID);
                                        String supername = DefensiveClass.optString(supercat, Constants.TAG_NAME);
                                        HashMap<String, String> map = new HashMap<>();
                                        map.put(Constants.TAG_ID, superid);
                                        map.put(Constants.TAG_NAME, supername);
                                        categoryAry.add(map);
                                    }
                                    break;
                                }
                            }
                        }
                        if (categoryAry.size() == 0) {
                            if (catState.equals("parent")) {
                                HashMap<String, String> vsmap = new HashMap<>();
                                vsmap.put(Constants.TAG_ID, catId);
                                vsmap.put(Constants.TAG_NAME, catName);
                                categoryAry.add(vsmap);
                                selectedCat.add(catId);
                            } else if (catState.equals("sub")) {
                                HashMap<String, String> vsmap = new HashMap<>();
                                vsmap.put(Constants.TAG_ID, subcatId);
                                vsmap.put(Constants.TAG_NAME, catName);
                                categoryAry.add(vsmap);
                                selectedCat.add(subcatId);
                            }
                        } else {
                            HashMap<String, String> vsmap = new HashMap<>();
                            vsmap.put(Constants.TAG_ID, "0");
                            vsmap.put(Constants.TAG_NAME, getString(R.string.view_all));
                            categoryAry.add(vsmap);
                        }

                        contentViewAdapter.notifyDataSetChanged();

                        JSONArray color = json.getJSONArray(Constants.TAG_COLOR);
                        for (int i = 0; i < color.length(); i++) {
                            JSONObject temp = color.getJSONObject(i);

                            String id = DefensiveClass.optString(temp, String.valueOf(i + 1));
                            String name = DefensiveClass.optString(temp, Constants.TAG_NAME);
                            HashMap<String, String> map = new HashMap<>();
                            map.put(Constants.TAG_ID, id);
                            map.put(Constants.TAG_NAME, name);
                            colorAry.add(map);
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
                Log.e(TAG, "getCategoryError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
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

    public class SectionViewAdapter extends RecyclerView.Adapter<SectionViewAdapter.MyViewHolder> {

        String[] data = new String[5];
        Context context;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView name;
            View line;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                name = (TextView) view.findViewById(R.id.name);
                line = (View) view.findViewById(R.id.view);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);

                /*if (sectionPosition == -1) {
                    sectionPosition = 0;
                    contentList.setVisibility(View.VISIBLE);
                    priceLay.setVisibility(View.GONE);
                    distanceLay.setVisibility(View.GONE);
                    contentViewAdapter = new ContentViewAdapter("category", CategoryListings.this, categoryAry);
                    contentList.setAdapter(contentViewAdapter);
                }*/

                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mainLay:
                        sectionPosition = data[getAdapterPosition()];
                        if (sectionPosition.equals(getString(R.string.categories))) {
                            contentList.setVisibility(View.VISIBLE);
                            priceLay.setVisibility(View.GONE);
                            distanceLay.setVisibility(View.GONE);
                            contentViewAdapter = new ContentViewAdapter(getString(R.string.categories), CategoryListings.this, categoryAry);
                            contentList.setAdapter(contentViewAdapter);
                        } else if (sectionPosition.equals(getString(R.string.price))) {
                            contentList.setVisibility(View.GONE);
                            priceLay.setVisibility(View.VISIBLE);
                            distanceLay.setVisibility(View.GONE);
                        } else if (sectionPosition.equals(getString(R.string.color))) {
                            contentList.setVisibility(View.VISIBLE);
                            priceLay.setVisibility(View.GONE);
                            distanceLay.setVisibility(View.GONE);
                            contentViewAdapter = new ContentViewAdapter(getString(R.string.color), CategoryListings.this, colorAry);
                            contentList.setAdapter(contentViewAdapter);
                        } else if (sectionPosition.equals(getString(R.string.near_me))) {
                            if (googleApiClient == null) {
                                setUpGClient();
                                contentList.setVisibility(View.GONE);
                                priceLay.setVisibility(View.GONE);
                                distanceLay.setVisibility(View.GONE);
                            } else if (mylocation == null) {
                                getMyLocation();
                                contentList.setVisibility(View.GONE);
                                priceLay.setVisibility(View.GONE);
                                distanceLay.setVisibility(View.GONE);
                            } else {
                                contentList.setVisibility(View.GONE);
                                priceLay.setVisibility(View.GONE);
                                distanceLay.setVisibility(View.VISIBLE);
                            }
                        } else if (sectionPosition.equals(getString(R.string.product_type))) {
                            contentList.setVisibility(View.VISIBLE);
                            priceLay.setVisibility(View.GONE);
                            distanceLay.setVisibility(View.GONE);
                            contentViewAdapter = new ContentViewAdapter(getString(R.string.product_type), CategoryListings.this, productTypeAry);
                            contentList.setAdapter(contentViewAdapter);
                        }
                        notifyDataSetChanged();
                        break;
                }
            }
        }

        public SectionViewAdapter(Context context, String[] temp) {
            this.data = temp;
            this.context = context;
        }

        @Override
        public SectionViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.filter_section_list_item, parent, false);

            return new SectionViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final SectionViewAdapter.MyViewHolder holder, int position) {

            holder.name.setText(data[position]);

            if (data[position].equals(sectionPosition)) {
                holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                holder.name.setBackgroundColor(getResources().getColor(R.color.bg));
                holder.line.setVisibility(View.GONE);
            } else {
                holder.name.setTextColor(getResources().getColor(R.color.textPrimary));
                holder.name.setBackgroundColor(getResources().getColor(R.color.white));
                holder.line.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return data.length;
        }
    }

    public class ContentViewAdapter extends RecyclerView.Adapter<ContentViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items = new ArrayList<>();
        Context context;
        String from;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView name;
            ImageView icon;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                name = (TextView) view.findViewById(R.id.name);
                icon = (ImageView) view.findViewById(R.id.icon);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);

                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mainLay:
                        sortPosition = getAdapterPosition();
                        if (from.equals(getString(R.string.categories))) {
                            if (catState.equals("super")) {
                                if (Items.get(getAdapterPosition()).get(Constants.TAG_ID).equals("0")) {
                                    selectedCat.clear();
                                    selectedCat.add(Items.get(getAdapterPosition()).get(Constants.TAG_ID));
                                } else {
                                    if (selectedCat.contains(Items.get(getAdapterPosition()).get(Constants.TAG_ID))) {
                                        selectedCat.remove(Items.get(getAdapterPosition()).get(Constants.TAG_ID));
                                    } else {
                                        selectedCat.add(Items.get(getAdapterPosition()).get(Constants.TAG_ID));
                                    }
                                    selectedCat.remove("0");
                                }
                            }
                        } else if (from.equals(getString(R.string.product_type))) {
                            if (selectedProductType.contains(Items.get(getAdapterPosition()).get(Constants.TAG_ID))) {
                                selectedProductType.remove(Items.get(getAdapterPosition()).get(Constants.TAG_ID));
                            } else {
                                selectedProductType.add(Items.get(getAdapterPosition()).get(Constants.TAG_ID));
                            }
                        } else if (from.equals(getString(R.string.color))) {
                            if (stringColor.equals(Items.get(getAdapterPosition()).get(Constants.TAG_NAME))) {
                                stringColor = "";
                            } else {
                                stringColor = Items.get(getAdapterPosition()).get(Constants.TAG_NAME);
                            }
                        }
                        notifyDataSetChanged();

                        break;
                }
            }
        }

        public ContentViewAdapter(String from, Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
            this.from = from;
        }

        @Override
        public ContentViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.filter_content_items, parent, false);

            return new ContentViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ContentViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.name.setText(tempMap.get(Constants.TAG_NAME));

            if (from.equals(getString(R.string.categories))) {
                if (selectedCat.contains(tempMap.get(Constants.TAG_ID))) {
                    holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                    holder.icon.setVisibility(View.VISIBLE);
                } else {
                    holder.name.setTextColor(getResources().getColor(R.color.textPrimary));
                    holder.icon.setVisibility(View.INVISIBLE);
                }
            } else if (from.equals(getString(R.string.product_type))) {
                if (selectedProductType.contains(tempMap.get(Constants.TAG_ID))) {
                    holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                    holder.icon.setVisibility(View.VISIBLE);
                } else {
                    holder.name.setTextColor(getResources().getColor(R.color.textPrimary));
                    holder.icon.setVisibility(View.INVISIBLE);
                }
            } else if (from.equals(getString(R.string.color))) {
                if (stringColor.equals(Items.get(position).get(Constants.TAG_NAME))) {
                    holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                    holder.icon.setVisibility(View.VISIBLE);
                } else {
                    holder.name.setTextColor(getResources().getColor(R.color.textPrimary));
                    holder.icon.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }
    }

    private void getProductType() {
        for (int i = 0; i < 3; i++) {
            HashMap<String, String> map = new HashMap<>();
            switch (i) {
                case 0:
                    map.put(Constants.TAG_ID, "1");
                    map.put(Constants.TAG_NAME, getString(R.string.affiliate_products));
                    break;
                case 1:
                    map.put(Constants.TAG_ID, "2");
                    map.put(Constants.TAG_NAME, getString(R.string.shop_products));
                    break;
                case 2:
                    map.put(Constants.TAG_ID, "0");
                    map.put(Constants.TAG_NAME, getString(R.string.view_all));
                    break;
            }
            productTypeAry.add(map);
        }
    }

    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    private void checkPermissions() {
        int permissionLocation = ContextCompat.checkSelfPermission(CategoryListings.this,
                ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        } else {
            getMyLocation();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        int permissionLocation = ContextCompat.checkSelfPermission(CategoryListings.this,
                ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mylocation = location;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkPermissions();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void getMyLocation() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(CategoryListings.this,
                        ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(3000);
                    locationRequest.setFastestInterval(3000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(googleApiClient, locationRequest, CategoryListings.this);
                    PendingResult result =
                            LocationServices.SettingsApi
                                    .checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied.
                                    // You can initialize location requests here.
                                    int permissionLocation = ContextCompat
                                            .checkSelfPermission(CategoryListings.this,
                                                    ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                        mylocation = LocationServices.FusedLocationApi
                                                .getLastLocation(googleApiClient);
                                        Log.v("mylocation", "mylocation=" + mylocation);
                                        sectionPosition = getString(R.string.near_me);
                                        contentList.setVisibility(View.GONE);
                                        priceLay.setVisibility(View.GONE);
                                        distanceLay.setVisibility(View.VISIBLE);
                                        sectionViewAdapter.notifyDataSetChanged();
                                    }
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied.
                                    // But could be fixed by showing the user a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        // Ask to turn on GPS automatically
                                        status.startResolutionForResult(CategoryListings.this,
                                                REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied. However, we have no way to fix the
                                    // settings so we won't show the dialog.
                                    //finish();
                                    break;
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getMyLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        sectionPosition = getString(R.string.near_me);
                        contentList.setVisibility(View.VISIBLE);
                        priceLay.setVisibility(View.GONE);
                        distanceLay.setVisibility(View.GONE);
                        sectionViewAdapter.notifyDataSetChanged();
                        contentViewAdapter = new ContentViewAdapter(getString(R.string.categories), CategoryListings.this, categoryAry);
                        contentList.setAdapter(contentViewAdapter);
                        break;
                }
                break;
        }
    }

    public void switchContent(Fragment fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            // Replace fragmentContainer with your container id
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.content_frame);
            // Return if the class are the same
            if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass()))
                return;
            fragmentManager.beginTransaction().addToBackStack(null)
                    .replace(R.id.content_frame, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCategorySelected() {
        filter.setTextColor(getResources().getColor(R.color.colorPrimary));
        filterImage.setColorFilter(getResources().getColor(R.color.colorPrimary));
        filterOptionLay.setVisibility(View.VISIBLE);
        getCategory();
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
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.sortLay:
                getSupportFragmentManager().popBackStack();
                sort.setTextColor(getResources().getColor(R.color.colorPrimary));
                sortImage.setColorFilter(getResources().getColor(R.color.colorPrimary));
                filterOptionLay.setVisibility(View.GONE);
                sortOptionLay.setVisibility(View.VISIBLE);
                if (filterLay.getVisibility() == View.VISIBLE) {
                    filter.setTextColor(getResources().getColor(R.color.textPrimary));
                    filterImage.setColorFilter(null);
                    filterOptionLay.setVisibility(View.GONE);
                }
                break;
            case R.id.filterLay:
                sortOptionLay.setVisibility(View.GONE);
                if (from.equals("search")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("from", "filter");
                    bundle.putInt("position", 0);
                    CategoryFragment fragment = new CategoryFragment();
                    fragment.setArguments(bundle);
                    switchContent(fragment);
                } else {
                    filter.setTextColor(getResources().getColor(R.color.colorPrimary));
                    filterImage.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    filterOptionLay.setVisibility(View.VISIBLE);
                    getCategory();

                    if (catState.equals("super") && categoryAry.size() == 0) {
                        sectionViewAdapter.notifyDataSetChanged();
                    }
                    if (sortLay.getVisibility() == View.VISIBLE) {
                        sort.setTextColor(getResources().getColor(R.color.textPrimary));
                        sortImage.setColorFilter(null);
                        sortOptionLay.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.sortOptionLay:
                sort.setTextColor(getResources().getColor(R.color.textPrimary));
                sortImage.setColorFilter(null);
                sortOptionLay.setVisibility(View.GONE);
                break;
            case R.id.clear:
                filter.setTextColor(getResources().getColor(R.color.textPrimary));
                filterImage.setColorFilter(null);
                resetFilter();
                filterOptionLay.setVisibility(View.GONE);
                itemsAry.clear();
                getAllItems(0);
                break;
            case R.id.apply:
                filter.setTextColor(getResources().getColor(R.color.colorPrimary));
                filterImage.setColorFilter(getResources().getColor(R.color.colorPrimary));
                filterOptionLay.setVisibility(View.GONE);

                if (selectedCat.contains("0")) {
                    stringCat = "0";
                } else {
                    stringCat = selectedCat.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(", ", ",");
                }

                if (selectedProductType.contains("1")) {
                    affliate_only = true;
                } else if (selectedProductType.contains("2")) {
                    buy_only = true;
                }

                mScrollListener.resetpagecount();
                itemsAry.clear();
                getAllItems(0);
                break;
            case R.id.searchBtn:
                Intent s = new Intent(this, RecentSearch.class);
                s.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

    private void resetFilter() {
        sectionList.getChildAt(0).performClick();
        stringColor = "";
        distanceBar.setProgress(0);
        priceBar.setSelectedMaxValue(priceMaximum);
        priceBar.setSelectedMinValue(priceMinimum);
        maxPrice.setText("$ " + priceMaximum);
        minPrice.setText("$ " + priceMinimum);
        sectionList.scrollToPosition(0);
    }
}
