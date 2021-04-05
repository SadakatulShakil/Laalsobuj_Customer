package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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
import com.futureskyltd.app.helper.FragmentChangeListener;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Created by hitasoft on 14/6/17.
 */

public class LikedItems extends Fragment {

    private static final String TAG = LikedItems.class.getSimpleName();
    private String from = "", userId = "";
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<HashMap<String, String>> itemsAry = new ArrayList<HashMap<String, String>>();
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    RelativeLayout progressLay, nullLay;
    ImageView nullImage;
    TextView nullText;
    DatabaseHandler helper;

    public static LikedItems newInstance(String from, String userId) {
        LikedItems fragment = new LikedItems();
        Bundle args = new Bundle();
        args.putString("from", from);
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.liked_main_layout, container, false);
        if (getArguments() != null) {
            from = getArguments().getString("from");
            userId = getArguments().getString("userId");
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));
        nullLay = (RelativeLayout) view.findViewById(R.id.nullLay);
        nullImage = (ImageView) view.findViewById(R.id.nullImage);
        nullText = (TextView) view.findViewById(R.id.nullText);
        progressLay = (RelativeLayout) view.findViewById(R.id.progress);

        if (from.equals("menu")) {
            ((FragmentChangeListener) getActivity()).setNavSelectionItem(R.id.liked, "Liked");
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) nullLay.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.topMargin = FantacyApplication.dpToPx(getActivity(), 80);
            nullLay.setLayoutParams(params);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        helper = DatabaseHandler.getInstance(getActivity());

        DividerItemDecoration itemDivider = new DividerItemDecoration(getActivity(), linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), itemsAry);
        recyclerView.setAdapter(recyclerViewAdapter);
        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
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
        itemsAry.clear();
        getLikedData(0);
    }

    private void getLikedData(final int offset) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_LIKEDITEM, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.i(TAG, "getLikedDataRes= " + res);
                    JSONObject json = new JSONObject(res);
                    progressLay.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setEnabled(true);
                    if (itemsAry.size() >= Constants.OVERALL_LIMIT && itemsAry.get(itemsAry.size() - 1) == null) {
                        itemsAry.remove(itemsAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(itemsAry.size());
                    }
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        itemsAry.clear();
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {

                        JSONArray items = json.getJSONArray(Constants.TAG_RESULT);
                        ItemsParsing allItems = new ItemsParsing(getActivity());
                        itemsAry.addAll(allItems.getItems(items));


                        if (mScrollListener != null && itemsAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           /* if (PaymentStatus.activity == null) {
                                Intent i = new Intent(getActivity(), PaymentStatus.class);
                                if (userId.equals(GetSet.getUserId())) {
                                    i.putExtra("from", "block");
                                } else {
                                    i.putExtra("from", "other_user_block");
                                }
                                startActivity(i);
                                onDestroy();
                            }*/
                        } else {
                            /*Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                        }
                    }
                    if (itemsAry.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_liked);
                        nullText.setText(getString(R.string.no_item_wishlist));
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
                            if (from.equals("profile")) {
                                swipeRefresh();
                            } else {
                                progressLay.setVisibility(View.VISIBLE);
                                mSwipeRefreshLayout.setEnabled(false);
                            }
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();
                if (accesstoken != null) {
                    map.put("logged_user_id", "289");
                }
                map.put("user_id", userId);
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                Log.i(TAG, "getLikedDataParams: " + map);
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
        if (itemsAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
            }
            nullLay.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setEnabled(true);
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
            String accesstoken = preferences.getString("TOKEN", null);
            String customerId = preferences.getString("customer_id", null);
            ImageView itemImage, cancel;
            TextView itemName, itemPrice, discountPrice;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                itemImage = (ImageView) view.findViewById(R.id.itemImage);
                itemName = (TextView) view.findViewById(R.id.itemName);
                itemPrice = (TextView) view.findViewById(R.id.itemPrice);
                cancel = (ImageView) view.findViewById(R.id.cancel);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);
                discountPrice = (TextView) view.findViewById(R.id.discountPrice);

                discountPrice.setPaintFlags(discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                mainLay.setOnClickListener(this);
                cancel.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mainLay:
                        Intent i = new Intent(getActivity(), DetailActivity.class);
                        i.putExtra("items", Items);
                        i.putExtra("position", getAdapterPosition());
                        startActivity(i);
                        break;
                    case R.id.cancel:
                        if (accesstoken != null) {
                            if (userId.equals(customerId)) {
                                removeItem(getAdapterPosition(), Items.get(getAdapterPosition()));
                                Items.get(getAdapterPosition()).put(Constants.TAG_LIKED, "no");
                                helper.updateItemDetails(Items.get(getAdapterPosition()).get(Constants.TAG_ID), Constants.TAG_LIKED, Items.get(getAdapterPosition()).get(Constants.TAG_LIKED));
                                Items.remove(getAdapterPosition());
                                notifyItemRemoved(getAdapterPosition());
                            } else {
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
                            }

                        } else {
                            Intent login = new Intent(context, SignInActivity.class);
                            startActivity(login);
                        }
                        break;
                }
            }
        }

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.liked_items, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
            String accesstoken = preferences.getString("TOKEN", null);
            String customerId = preferences.getString("customer_id", null);
            if (holder instanceof MyViewHolder) {
                HashMap<String, String> tempMap = Items.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;
                viewHolder.itemName.setText(tempMap.get(Constants.TAG_ITEM_TITLE));
                viewHolder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
                String image = tempMap.get(Constants.TAG_IMAGE);
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(viewHolder.itemImage);
                }
                if (!userId.equals(customerId)) {
                    if (helper.getItemDetails(tempMap.get(Constants.TAG_ID), Constants.TAG_LIKED).equalsIgnoreCase("yes")) {
                        viewHolder.cancel.setImageResource(R.drawable.liked);
                    } else {
                        viewHolder.cancel.setImageResource(R.drawable.unlike);
                    }
                }

                if (FantacyApplication.isRTL(getActivity())) {
                    viewHolder.itemName.setGravity(Gravity.RIGHT);
                } else {
                    viewHolder.itemName.setGravity(Gravity.LEFT);
                }

                if (tempMap.get(Constants.TAG_DEAL_ENABLED).equalsIgnoreCase("yes") && FantacyApplication.isDealEnabled(tempMap.get(Constants.TAG_VALID_TILL))) {
                    viewHolder.discountPrice.setVisibility(View.VISIBLE);
                    String costValue = tempMap.get(Constants.TAG_PRICE);
                    float priceValue = Float.parseFloat(costValue) - (Float.parseFloat(costValue)
                            * ((Float.parseFloat(tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE))) / 100.0f));

                    viewHolder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.decimal.format(priceValue));
                    viewHolder.discountPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
                } else {
                    viewHolder.discountPrice.setVisibility(View.GONE);
                    viewHolder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
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
    }

    private void likeItem(final int position, final String check) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ITEMLIKE, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    Log.i(TAG, "likeItemRes= " + res);
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
                Log.i(TAG, "likeItemParams= " + map);
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

    private void removeItem(final int position, final HashMap<String, String> removemap) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ITEMLIKE, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.i(TAG, "removeItemRes= " + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        removemap.put(Constants.TAG_LIKED, "yes");
                        helper.updateItemDetails(removemap.get(Constants.TAG_ID), Constants.TAG_LIKED, removemap.get(Constants.TAG_LIKED));
                        itemsAry.add(position, removemap);
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
                Log.e(TAG, "removeItemError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("item_id", removemap.get(Constants.TAG_ID));
                Log.i(TAG, "removeItemParams= " + map);
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

    @Override
    public void onResume() {
        super.onResume();
        recyclerViewAdapter.notifyDataSetChanged();
    }
}
