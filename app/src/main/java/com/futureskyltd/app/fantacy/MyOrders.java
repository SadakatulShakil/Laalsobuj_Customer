package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.EndlessRecyclerOnScrollListener;
import com.futureskyltd.app.helper.FragmentChangeListener;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 19/7/17.
 */

public class MyOrders extends Fragment {

    private static final String TAG = MyOrders.class.getSimpleName();
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<HashMap<String, String>> ordersAry = new ArrayList<HashMap<String, String>>();
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    RelativeLayout progressLay, nullLay;
    public static boolean refreshorder = false;
    ImageView nullImage;
    TextView nullText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.myorders_main_layout, container, false);

        ((FragmentChangeListener) getActivity()).setNavSelectionItem(R.id.myOrders, "MyOrders");

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));
        nullLay = (RelativeLayout) view.findViewById(R.id.nullLay);
        nullImage = (ImageView) view.findViewById(R.id.nullImage);
        nullText = (TextView) view.findViewById(R.id.nullText);
        progressLay = (RelativeLayout) view.findViewById(R.id.progress);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(getActivity(), linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.emptyspace));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), ordersAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                Log.v(TAG, "onLoadMoreOffset:" + current_page);
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getOrdersData(current_page * Constants.OVERALL_LIMIT);
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                ordersAry.clear();
                getOrdersData(0);
            }
        });

        ordersAry.clear();
        getOrdersData(0);
    }


    private void getOrdersData(final int offset) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_MYORDERS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getOrdersDataRes=" + res);
                    JSONObject json = new JSONObject(res);

                    progressLay.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setEnabled(true);
                    if (ordersAry.size() >= Constants.OVERALL_LIMIT && ordersAry.get(ordersAry.size() - 1) == null) {
                        ordersAry.remove(ordersAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(ordersAry.size());
                    }
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray result = json.getJSONArray(Constants.TAG_RESULT);

                        for (int i = 0; i < result.length(); i++) {
                            JSONObject temp = result.getJSONObject(i);

                            String order_id = DefensiveClass.optString(temp, Constants.TAG_ORDER_ID);
                            String grand_total = DefensiveClass.optString(temp, Constants.TAG_GRAND_TOTAL);
                            String currency = DefensiveClass.optString(temp, Constants.TAG_CURRENCY);
                            String sale_date = DefensiveClass.optString(temp, Constants.TAG_SALE_DATE);
                            String order_status = DefensiveClass.optString(temp, Constants.TAG_STATUS);
                            String item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                            String item_image = DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE);
                            String item_name = DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME);

                            JSONObject expected_delivery = temp.getJSONObject(Constants.TAG_EXPECTED_DELIVERY);
                            String from = DefensiveClass.optString(expected_delivery, Constants.TAG_FROM);
                            String to = DefensiveClass.optString(expected_delivery, Constants.TAG_TO);

                            HashMap<String, String> map = new HashMap<>();

                            map.put(Constants.TAG_ORDER_ID, order_id);
                            map.put(Constants.TAG_GRAND_TOTAL, grand_total);
                            map.put(Constants.TAG_CURRENCY, currency);
                            map.put(Constants.TAG_SALE_DATE, sale_date);
                            map.put(Constants.TAG_STATUS, order_status);
                            map.put(Constants.TAG_ITEM_ID, item_id);
                            map.put(Constants.TAG_ITEM_IMAGE, item_image);
                            map.put(Constants.TAG_ITEM_NAME, item_name);
                            map.put(Constants.TAG_FROM, from);
                            map.put(Constants.TAG_TO, to);

                            ordersAry.add(map);
                        }

                        if (mScrollListener != null && ordersAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                            Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);
                        } else {
                            Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);
                        }
                    }
                    recyclerViewAdapter.notifyDataSetChanged();
                    if (ordersAry.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_order);
                        nullText.setText(getString(R.string.no_order));
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
                Log.e(TAG, "getOrdersDataError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ordersAry.size() >= Constants.OVERALL_LIMIT) {
                            ordersAry.add(null);
                            recyclerViewAdapter.notifyItemInserted(ordersAry.size() - 1);
                        } else if (ordersAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                            progressLay.setVisibility(View.VISIBLE);
                            mSwipeRefreshLayout.setEnabled(false);
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                Log.i(TAG, "getOrdersDataParams: " + map);
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
        if (ordersAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
            }
            nullLay.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (refreshorder) {
            refreshorder = false;
            mScrollListener.resetpagecount();
            ordersAry.clear();
            getOrdersData(0);
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

            ImageView itemImage;
            TextView itemName, status, date;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                itemImage = (ImageView) view.findViewById(R.id.itemImage);
                itemName = (TextView) view.findViewById(R.id.itemName);
                status = (TextView) view.findViewById(R.id.status);
                date = (TextView) view.findViewById(R.id.date);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);

                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mainLay:
                        Intent i = new Intent(getActivity(), OrderDetail.class);
                        i.putExtra("orderId", Items.get(getAdapterPosition()).get(Constants.TAG_ORDER_ID));
                        i.putExtra("orderStatus", Items.get(getAdapterPosition()).get(Constants.TAG_STATUS));
                        startActivity(i);
                        break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return Items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.myorders_list_item, parent, false);
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
                viewHolder.itemName.setText(tempMap.get(Constants.TAG_ITEM_NAME));
                viewHolder.status.setText(tempMap.get(Constants.TAG_STATUS));

                switch (tempMap.get(Constants.TAG_STATUS)) {
                    case "Cancelled":
                        viewHolder.status.setTextColor(ContextCompat.getColor(context, R.color.red));
                        break;
                    case "Pending":
                        viewHolder.status.setTextColor(ContextCompat.getColor(context, R.color.orange));
                        break;
                    case "Processing":
                        viewHolder.status.setTextColor(ContextCompat.getColor(context, R.color.green));
                        break;
                    default:
                        viewHolder.status.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        break;
                }


                if (!tempMap.get(Constants.TAG_SALE_DATE).equals(null) && !tempMap.get(Constants.TAG_SALE_DATE).equals("")) {
                    long date = Long.parseLong(tempMap.get(Constants.TAG_SALE_DATE)) * 1000;
                    viewHolder.date.setText(FantacyApplication.getDate(date));
                }

                if (FantacyApplication.isRTL(context)) {
                    viewHolder.itemName.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                } else {
                    viewHolder.itemName.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                }

                String image = tempMap.get(Constants.TAG_ITEM_IMAGE);
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(viewHolder.itemImage);
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
}
