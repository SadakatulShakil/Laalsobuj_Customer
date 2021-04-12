package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
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
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.GridRecyclerOnScrollListener;
import com.futureskyltd.app.helper.DatabaseHandler;
import com.futureskyltd.app.helper.EqualSpacingItemDecoration;
import com.futureskyltd.app.helper.NetworkReceiver;
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

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Created by hitasoft on 30/5/17.
 */

public class ViewAllActivity extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, cartBtn, searchBtn;
    TextView title;
    RecyclerView recyclerView;
    Display display;
    int itemWidth, itemHeight;
    String from = "", keyword = "";
    RecyclerViewAdapter itemAdapter;
    GridLayoutManager itemManager;
    ArrayList<HashMap<String, String>> itemsAry = new ArrayList<HashMap<String, String>>();
    private GridRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    RelativeLayout progressLay, nullLay;
    ImageView nullImage;
    TextView nullText;
    DatabaseHandler helper;
    private SharedPreferences preferences;
    private String accesstoken, customerId="", customerName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewall_main_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        cartBtn = (ImageView) findViewById(R.id.cartBtn);
        searchBtn = (ImageView) findViewById(R.id.searchBtn);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        nullImage = (ImageView) findViewById(R.id.nullImage);
        nullText = (TextView) findViewById(R.id.nullText);
        progressLay = (RelativeLayout) findViewById(R.id.progress);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        from = (String) getIntent().getExtras().get("from");
        keyword = (String) getIntent().getExtras().get("key");

        title.setText(from);
        display = getWindowManager().getDefaultDisplay();
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.progresscolor));
        helper = DatabaseHandler.getInstance(ViewAllActivity.this);
        FragmentMainActivity.setCartBadge(findViewById(R.id.parentLay), ViewAllActivity.this);
        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);
        customerId = preferences.getString("customer_id", null);
        customerName = preferences.getString("customer_name", null);
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        cartBtn.setVisibility(View.VISIBLE);
        searchBtn.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        cartBtn.setOnClickListener(this);

        itemWidth = (display.getWidth() * 50 / 100) - FantacyApplication.dpToPx(this, 1);
        itemHeight = display.getWidth() * 60 / 100;

        recyclerView.setHasFixedSize(true);
        itemManager = new GridLayoutManager(ViewAllActivity.this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        itemAdapter = new RecyclerViewAdapter(ViewAllActivity.this, itemsAry);
        recyclerView.setAdapter(itemAdapter);

        recyclerView.addItemDecoration(new EqualSpacingItemDecoration(15));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                itemsAry.clear();
                getAllItems(0);
            }
        });
        mScrollListener = new GridRecyclerOnScrollListener(itemManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getAllItems(current_page * Constants.OVERALL_LIMIT);
                    Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
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

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getAllItems(final int offset) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_VIEWALLITEMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getAllItemsRes=" + res);
                    JSONObject json = new JSONObject(res);
                    progressLay.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setEnabled(true);
                    if (itemsAry.size() >= Constants.OVERALL_LIMIT && itemsAry.get(itemsAry.size() - 1) == null) {
                        itemsAry.remove(itemsAry.size() - 1);
                        itemAdapter.notifyItemRemoved(itemsAry.size());
                    }
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray items = json.getJSONArray(Constants.TAG_ITEMS);
                        ItemsParsing allItems = new ItemsParsing(ViewAllActivity.this);
                        itemsAry.addAll(allItems.getItems(items));

                        if (mScrollListener != null && itemsAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    }
                    if (itemsAry.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_item);
                        nullText.setText(getString(R.string.no_deal));
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                    itemAdapter.notifyDataSetChanged();
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

                ViewAllActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (itemsAry.size() >= Constants.OVERALL_LIMIT) {
                            itemsAry.add(null);
                            itemAdapter.notifyItemInserted(itemsAry.size() - 1);
                        } else if (itemsAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                            progressLay.setVisibility(View.VISIBLE);
                            mSwipeRefreshLayout.setEnabled(false);
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();
                if (accesstoken !=null) {
                    map.put("user_id", customerId);
                }
                map.put("type", keyword);
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                Log.v(TAG, "getAllItemsParams=" + map);
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
        FantacyApplication.getInstance().addToRequestQueue(req, "viewAll");
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

    private void likeItem(final int position, final String check) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ITEMLIKE, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.i(TAG, "likeItemRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        if (check.equals("yes"))
                            itemsAry.get(position).put(Constants.TAG_LIKED, "no");
                        else
                            itemsAry.get(position).put(Constants.TAG_LIKED, "yes");
                        itemAdapter.notifyDataSetChanged();
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
                if (check.equals("yes"))
                    itemsAry.get(position).put(Constants.TAG_LIKED, "no");
                else
                    itemsAry.get(position).put(Constants.TAG_LIKED, "yes");
                itemAdapter.notifyDataSetChanged();
                Log.e(TAG, "likeItemError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("item_id", itemsAry.get(position).get(Constants.TAG_ID));
                Log.i(TAG, "likeItemParams: " + map);
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
        public final int VIEW_TYPE_ITEM = 0;
        public final int VIEW_TYPE_LOADING = 1;
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
            LinearLayout paddinglay;

            public MyViewHolder(View view) {
                super(view);

                itemName = (TextView) view.findViewById(R.id.itemName);
                itemPrice = (TextView) view.findViewById(R.id.itemPrice);
                itemImage = (ImageView) view.findViewById(R.id.itemImage);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);
                paddinglay = (LinearLayout) view.findViewById(R.id.paddinglay);
                likedBtn = (ImageView) view.findViewById(R.id.likedBtn);
                discountPrice = (TextView) view.findViewById(R.id.discountPrice);

                likedBtn.setOnClickListener(this);
                mainLay.setOnClickListener(this);
                discountPrice.setPaintFlags(discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.likedBtn:
                        if (accesstoken != null) {
                            String liked = Items.get(getAdapterPosition()).get(Constants.TAG_LIKED);
                            int likeCount = getLikedCountFromLocal(Items.get(getAdapterPosition()));
                            if (liked.equalsIgnoreCase("yes")) {
                                likeItem(getAdapterPosition(), "no");
                                Items.get(getAdapterPosition()).put(Constants.TAG_LIKED, "no");
                                likeCount = likeCount - 1;
                            } else {
                                likeItem(getAdapterPosition(), "yes");
                                likeCount = likeCount + 1;
                                Items.get(getAdapterPosition()).put(Constants.TAG_LIKED, "yes");
                            }
                            helper.updateItemDetails(Items.get(getAdapterPosition()).get(Constants.TAG_ID), Constants.TAG_LIKED, Items.get(getAdapterPosition()).get(Constants.TAG_LIKED));
                            helper.updateItemDetails(Items.get(getAdapterPosition()).get(Constants.TAG_ID), Constants.TAG_LIKE_COUNT, "" + likeCount);
                            notifyDataSetChanged();
                        } else {
                            Intent login = new Intent(context, SignInActivity.class);
                            startActivity(login);
                        }
                        break;
                    case R.id.mainLay:
                        Intent i = new Intent(ViewAllActivity.this, DetailActivity.class);
                        i.putExtra("items", itemsAry);
                        i.putExtra("position", getAdapterPosition());
                        startActivity(i);
                        break;
                }
            }

            private int getLikedCountFromLocal(HashMap<String, String> hashMap) {
                int likedCount;
                if (helper.getItemDetails(hashMap.get(Constants.TAG_ID), Constants.TAG_LIKE_COUNT) != "")
                    likedCount = Integer.parseInt(helper.getItemDetails(hashMap.get(Constants.TAG_ID), Constants.TAG_LIKE_COUNT));
                else
                    likedCount = 0;
                return likedCount;
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
                holder.paddinglay.setVisibility(View.VISIBLE);
                holder.mainLay.getLayoutParams().width = itemWidth;
                holder.itemImage.getLayoutParams().height = itemWidth;
                //    holder.mainLay.getLayoutParams().height = itemHeight;

                if (position % 2 == 0) {
                    holder.mainLay.setPadding(FantacyApplication.dpToPx(context, 1), 0, 0, FantacyApplication.dpToPx(context, 1));
                } else {
                    holder.mainLay.setPadding(0, 0, 0, FantacyApplication.dpToPx(context, 1));
                }

                String image = tempMap.get(Constants.TAG_IMAGE);
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(holder.itemImage);
                }
                holder.likedBtn.setVisibility(View.VISIBLE);
                if (helper.getItemDetails(tempMap.get(Constants.TAG_ID), Constants.TAG_LIKED).equalsIgnoreCase("yes")) {
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
        if (itemAdapter != null) {
            itemAdapter.notifyDataSetChanged();
        }
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
            case R.id.searchBtn:
                Intent s = new Intent(this, RecentSearch.class);
                startActivity(s);
                break;
            case R.id.cartBtn:
                if (accesstoken !=null) {
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
