package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
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
import com.futureskyltd.app.helper.DatabaseHandler;
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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Created by hitasoft on 23/6/17.
 */

public class StoreProducts extends Fragment {

    private static final String TAG = StoreProducts.class.getSimpleName();
    private String from = "", storeId = "";
    ImageView nullImage;
    TextView nullText;
    RelativeLayout nullLay;
    Display display;
    int itemWidth, itemHeight;
    RecyclerView recyclerView;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    GridLayoutManager itemManager;
    RecyclerViewAdapter recyclerViewAdapter;
    DatabaseHandler helper;
    ArrayList<HashMap<String, String>> itemsAry = new ArrayList<HashMap<String, String>>();

    public static StoreProducts newInstance(String from, String storeId) {
        StoreProducts fragment = new StoreProducts();
        Bundle args = new Bundle();
        args.putString("from", from);
        args.putString("storeId", storeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.products_main_layout, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        nullLay = (RelativeLayout) view.findViewById(R.id.nullLay);
        nullImage = (ImageView) view.findViewById(R.id.nullImage);
        nullText = (TextView) view.findViewById(R.id.nullText);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));

        if (getArguments() != null) {
            from = getArguments().getString("from");
            storeId = getArguments().getString("storeId");
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

        display = getActivity().getWindowManager().getDefaultDisplay();
        helper = DatabaseHandler.getInstance(getActivity());

        itemWidth = (display.getWidth() * 50 / 100) - FantacyApplication.dpToPx(getActivity(), 1);
        itemHeight = display.getWidth() * 60 / 100;

        recyclerView.setHasFixedSize(true);
        itemManager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), itemsAry);
        recyclerView.setAdapter(recyclerViewAdapter);
        DividerItemDecoration itemDivider = new DividerItemDecoration(recyclerView.getContext(),
                itemManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        recyclerView.addItemDecoration(itemDivider);
        mScrollListener = new EndlessRecyclerOnScrollListener(itemManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getLikedData(current_page * Constants.OVERALL_LIMIT);
                    Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                itemsAry.clear();
                getLikedData(0);
            }
        });

        itemManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (recyclerViewAdapter.getItemViewType(position) == 0) ? 1 : 2;
            }

        });

        itemsAry.clear();
        getLikedData(0);
    }

    private void getLikedData(final int offset) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_STORE_PRODUCTS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);

                    if (itemsAry.size() >= Constants.OVERALL_LIMIT && itemsAry.get(itemsAry.size() - 1) == null) {
                        itemsAry.remove(itemsAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(itemsAry.size());
                    }
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }
                    Log.v(TAG, "getLikedDataRes=" + res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {

                        JSONArray items = json.getJSONArray(Constants.TAG_ITEMS);
                        ItemsParsing allItems = new ItemsParsing(getActivity());
                        itemsAry.addAll(allItems.getItems(items));


                        if (mScrollListener != null && itemsAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    }
                    if (itemsAry.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_item);
                        nullText.setText(getString(R.string.no_item));
                        nullLay.setVisibility(View.VISIBLE);
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
                setErrorLayout();
                Log.e(TAG, "getLikedDataError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (itemsAry.size() >= Constants.OVERALL_LIMIT) {
                            itemsAry.add(null);
                            recyclerViewAdapter.notifyItemInserted(itemsAry.size() - 1);
                        } else if (itemsAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                            swipeRefresh();
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();
                if (GetSet.isLogged()) {
                    map.put("user_id", GetSet.getUserId());
                }
                map.put("store_id", storeId);
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                Log.i(TAG, "getLikedDataParams: " + map);
                return map;
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
        if (itemsAry.size() == 0) {
            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
            }
            nullLay.setVisibility(View.VISIBLE);
        }
    }

    private void likeItem(final int position, final String check) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ITEMLIKE, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.i(TAG, "likeItemRes: " + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        if (check.equals("yes"))
                            itemsAry.get(position).put(Constants.TAG_LIKED, "no");
                        else
                            itemsAry.get(position).put(Constants.TAG_LIKED, "yes");
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
                if (check.equals("yes"))
                    itemsAry.get(position).put(Constants.TAG_LIKED, "no");
                else
                    itemsAry.get(position).put(Constants.TAG_LIKED, "yes");
                recyclerViewAdapter.notifyDataSetChanged();
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
                switch (v.getId()) {
                    case R.id.likedBtn:
                        if (GetSet.isLogged()) {
                            String liked = Items.get(getAdapterPosition()).get(Constants.TAG_LIKED);
                            if (liked.equalsIgnoreCase("yes")) {
                                likeItem(getAdapterPosition(), "no");
                                Items.get(getAdapterPosition()).put(Constants.TAG_LIKED, "no");
                            } else {
                                likeItem(getAdapterPosition(), "yes");
                                Items.get(getAdapterPosition()).put(Constants.TAG_LIKED, "yes");
                            }
                            helper.updateItemDetails(Items.get(getAdapterPosition()).get(Constants.TAG_ID), Constants.TAG_LIKED, Items.get(getAdapterPosition()).get(Constants.TAG_LIKED));
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
}
