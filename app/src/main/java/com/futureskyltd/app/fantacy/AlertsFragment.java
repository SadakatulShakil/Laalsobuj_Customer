package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class AlertsFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    AlertsViewAdapter recyclerViewAdapter;
    ArrayList<HashMap<String, String>> alertsAry = new ArrayList<HashMap<String, String>>();
    RelativeLayout progressLay, nullLay;
    ImageView nullImage;
    TextView nullText;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dispute_recyclerview, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));
        nullLay = (RelativeLayout) rootView.findViewById(R.id.nullLay);
        nullImage = (ImageView) rootView.findViewById(R.id.nullImage);
        nullText = (TextView) rootView.findViewById(R.id.nullText);
        progressLay = (RelativeLayout) rootView.findViewById(R.id.progress);
        ((FragmentChangeListener) getActivity()).setNavSelectionItem(R.id.notification, "Alerts");
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(getActivity(),
                linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new AlertsViewAdapter(getActivity(), alertsAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getAlertsData(current_page * Constants.OVERALL_LIMIT);
                    Log.v(TAG, "onLoadMore offset" + (Constants.OVERALL_LIMIT * current_page));
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                alertsAry.clear();
                getAlertsData(0);
            }
        });
        alertsAry.clear();
        getAlertsData(0);
    }

    private void getAlertsData(final int offset) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_LIVE_FEEDS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getAlertsDataRes=" + res);
                    progressLay.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setEnabled(true);
                    if (alertsAry.size() >= Constants.OVERALL_LIMIT && alertsAry.get(alertsAry.size() - 1) == null) {
                        alertsAry.remove(alertsAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(alertsAry.size());
                    }
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }

                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray result = json.getJSONArray(Constants.TAG_RESULT);
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject temp = result.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();

                            String type = DefensiveClass.optString(temp, Constants.TAG_TYPE);

                            map.put(Constants.TAG_TYPE, type);

                            if (type.equalsIgnoreCase("follow")) {
                                String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);

                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_DATE, date);
                            } else if (type.equalsIgnoreCase("admin")) {
                                String admin_type = DefensiveClass.optString(temp, Constants.TAG_ADMIN_TYPE);
                                String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);
                                String item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                                String item_name = DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME);
                                String item_image = DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE);
                                String height = DefensiveClass.optString(temp, Constants.TAG_HEIGHT);
                                String width = DefensiveClass.optString(temp, Constants.TAG_WIDTH);
                                String admin_image = DefensiveClass.optString(temp, Constants.TAG_ADMIN_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);

                                map.put(Constants.TAG_ADMIN_TYPE, admin_type);
                                map.put(Constants.TAG_MESSAGE, message);
                                map.put(Constants.TAG_ITEM_ID, item_id);
                                map.put(Constants.TAG_ITEM_NAME, item_name);
                                map.put(Constants.TAG_ITEM_IMAGE, item_image);
                                map.put(Constants.TAG_HEIGHT, height);
                                map.put(Constants.TAG_WIDTH, width);
                                map.put(Constants.TAG_ADMIN_IMAGE, admin_image);
                                map.put(Constants.TAG_DATE, date);
                            } else if (type.equalsIgnoreCase("order_status")) {
                                String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);
                                String order_id = DefensiveClass.optString(temp, Constants.TAG_ORDER_ID);
                                String order_message = DefensiveClass.optString(temp, Constants.TAG_ORDER_MESSAGE);

                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_DATE, date);
                                map.put(Constants.TAG_ORDER_ID, order_id);
                                map.put(Constants.TAG_ORDER_MESSAGE, order_message);
                            } else if (type.equalsIgnoreCase("dispute")) {
                                String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);
                                String dispute_message = DefensiveClass.optString(temp, Constants.TAG_DISPUTE_MESSAGE);

                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_DATE, date);
                                map.put(Constants.TAG_DISPUTE_MESSAGE, dispute_message);
                            } else if (type.equalsIgnoreCase("review")) {
                                String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);
                                String review = DefensiveClass.optString(temp, Constants.TAG_REVIEW);

                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_DATE, date);
                                map.put(Constants.TAG_REVIEW, review);
                            } else if (type.equalsIgnoreCase("cart_notification")) {
                                String admin_image = DefensiveClass.optString(temp, Constants.TAG_ADMIN_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);

                                map.put(Constants.TAG_ADMIN_IMAGE, admin_image);
                                map.put(Constants.TAG_DATE, date);
                            } else if (type.equalsIgnoreCase("seller_news")) {
                                String store_id = DefensiveClass.optString(temp, Constants.TAG_STORE_ID);
                                String store_name = DefensiveClass.optString(temp, Constants.TAG_STORE_NAME);
                                String store_image = DefensiveClass.optString(temp, Constants.TAG_STORE_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);
                                String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);

                                map.put(Constants.TAG_STORE_ID, store_id);
                                map.put(Constants.TAG_STORE_NAME, store_name);
                                map.put(Constants.TAG_STORE_IMAGE, store_image);
                                map.put(Constants.TAG_DATE, date);
                                map.put(Constants.TAG_MESSAGE, message);
                            } else if (type.equalsIgnoreCase("group_gift")) {
                                String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);
                                String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);

                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_DATE, date);
                                map.put(Constants.TAG_MESSAGE, message);
                            } else if (type.equalsIgnoreCase("chat_message")) {
                                String chat_id = DefensiveClass.optString(temp, Constants.TAG_CHAT_ID);
                                String store_id = DefensiveClass.optString(temp, Constants.TAG_STORE_ID);
                                String store_name = DefensiveClass.optString(temp, Constants.TAG_STORE_NAME);
                                String store_image = DefensiveClass.optString(temp, Constants.TAG_STORE_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);
                                String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);

                                map.put(Constants.TAG_CHAT_ID, chat_id);
                                map.put(Constants.TAG_STORE_ID, store_id);
                                map.put(Constants.TAG_STORE_NAME, store_name);
                                map.put(Constants.TAG_STORE_IMAGE, store_image);
                                map.put(Constants.TAG_DATE, date);
                                map.put(Constants.TAG_MESSAGE, message);
                            } else if (type.equalsIgnoreCase("dispute_message")) {
                                String dispute_id = DefensiveClass.optString(temp, Constants.TAG_DISPUTE_ID);
                                String store_id = DefensiveClass.optString(temp, Constants.TAG_STORE_ID);
                                String store_name = DefensiveClass.optString(temp, Constants.TAG_STORE_NAME);
                                String store_image = DefensiveClass.optString(temp, Constants.TAG_STORE_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);
                                String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);
                                String dispute_status = DefensiveClass.optString(temp, Constants.TAG_DISPUTE_STATUS);

                                map.put(Constants.TAG_DISPUTE_ID, dispute_id);
                                map.put(Constants.TAG_STORE_ID, store_id);
                                map.put(Constants.TAG_STORE_NAME, store_name);
                                map.put(Constants.TAG_STORE_IMAGE, store_image);
                                map.put(Constants.TAG_DATE, date);
                                map.put(Constants.TAG_MESSAGE, message);
                                map.put(Constants.TAG_DISPUTE_STATUS, dispute_status);

                            } else if (type.equalsIgnoreCase("mentioned")) {
                                String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);
                                String item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                                String item_name = DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME);

                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_ITEM_ID, item_id);
                                map.put(Constants.TAG_ITEM_NAME, item_name);
                                map.put(Constants.TAG_DATE, date);
                            } else if (type.equalsIgnoreCase("invite")) {
                                String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);
                                String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);

                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_DATE, date);
                                map.put(Constants.TAG_MESSAGE, message);
                            } else if (type.equalsIgnoreCase("credit")) {
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);
                                String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);

                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_DATE, date);
                                map.put(Constants.TAG_MESSAGE, message);
                            }

                            alertsAry.add(map);
                        }
                        if (mScrollListener != null && alertsAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           /* Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                        /*    Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                        }
                    }
                    recyclerViewAdapter.notifyDataSetChanged();
                    if (alertsAry.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_notification);
                        nullText.setText(getString(R.string.no_alert));
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
                Log.e(TAG, "getAlertsDataError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (alertsAry.size() >= Constants.OVERALL_LIMIT) {
                            alertsAry.add(null);
                            recyclerViewAdapter.notifyItemInserted(alertsAry.size() - 1);
                        } else if (alertsAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
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
                map.put("type", "alert");
                Log.v(TAG, "getAlertsDataParams=" + map);
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
        if (alertsAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
            nullLay.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setEnabled(true);
        }
    }

    public static class AlertsViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        public final int VIEW_TYPE_ITEM = 0;
        public final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;

        public AlertsViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_alerts_item, parent, false);
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

                holder.itemImage.setVisibility(View.GONE);
                holder.newsText.setVisibility(View.GONE);
                holder.userName.setTextColor(ContextCompat.getColor(context, R.color.textPrimary));
                holder.mainLay.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                switch (tempMap.get(Constants.TAG_TYPE)) {
                    case "follow":
                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_USER_NAME), "", context.getString(R.string.is_following_you), tempMap.get(Constants.TAG_USER_ID), "", "follow");

                        if (!tempMap.get(Constants.TAG_USER_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_USER_IMAGE)).into(holder.userImage);
                        }
                        break;
                    case "admin":
                        if (tempMap.get(Constants.TAG_ADMIN_TYPE).equalsIgnoreCase("news")) {
                            //  holder.mainLay.setBackgroundColor(ContextCompat.getColor(context, R.color.admin_news_bg));
                            holder.newsText.setVisibility(View.VISIBLE);

                            holder.userName.setText(R.string.admin);
                            holder.userName.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                            holder.newsText.setText(tempMap.get(Constants.TAG_MESSAGE));
                            if (!tempMap.get(Constants.TAG_ADMIN_IMAGE).equals("")) {
                                Picasso.get().load(tempMap.get(Constants.TAG_ADMIN_IMAGE)).into(holder.userImage);
                            }
                        }
                        break;
                    case "mentioned":
                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_USER_NAME), tempMap.get(Constants.TAG_ITEM_NAME), context.getString(R.string.mentioned_you_on), tempMap.get(Constants.TAG_USER_ID), tempMap.get(Constants.TAG_ITEM_ID), "mentioned");


                        break;
                    case "order_status":
                        holder.userName.setText(Html.fromHtml(tempMap.get(Constants.TAG_ORDER_MESSAGE)));

                        if (!tempMap.get(Constants.TAG_USER_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_USER_IMAGE)).into(holder.userImage);
                        }
                        break;
                    case "dispute":
                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_USER_NAME), "", tempMap.get(Constants.TAG_DISPUTE_MESSAGE), "", "", "dispute");

                        if (!tempMap.get(Constants.TAG_USER_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_USER_IMAGE)).into(holder.userImage);
                        }
                        break;
                    case "review":
                        break;
                    case "cart_notification":
                        holder.userName.setText(context.getString(R.string.cart_notification));

                        if (!tempMap.get(Constants.TAG_ADMIN_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_ADMIN_IMAGE)).into(holder.userImage);
                        }
                        break;
                    case "seller_news":
                        //     holder.mainLay.setBackgroundColor(ContextCompat.getColor(context, R.color.admin_news_bg));
                        holder.newsText.setVisibility(View.VISIBLE);

                        holder.userName.setText(tempMap.get(Constants.TAG_STORE_NAME));
                        holder.userName.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        holder.newsText.setText(tempMap.get(Constants.TAG_MESSAGE));
                        if (!tempMap.get(Constants.TAG_STORE_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_STORE_IMAGE)).into(holder.userImage);
                        }
                        break;
                    case "group_gift":
                        holder.userName.setText(Html.fromHtml(tempMap.get(Constants.TAG_MESSAGE)));

                        if (!tempMap.get(Constants.TAG_USER_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_USER_IMAGE)).into(holder.userImage);
                        }
                        break;
                    case "chat_message":
                        holder.newsText.setVisibility(View.VISIBLE);

                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_STORE_NAME), "", context.getString(R.string.send_chat_message), "", "", "chat");
                        holder.userName.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        holder.newsText.setText(tempMap.get(Constants.TAG_MESSAGE));
                        if (!tempMap.get(Constants.TAG_STORE_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_STORE_IMAGE)).into(holder.userImage);
                        }
                        break;
                    case "dispute_message":
                        holder.newsText.setVisibility(View.VISIBLE);

                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_STORE_NAME), "", context.getString(R.string.send_dispute_message), "", "", "dispute");
                        holder.userName.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        holder.newsText.setText(tempMap.get(Constants.TAG_MESSAGE));
                        if (!tempMap.get(Constants.TAG_STORE_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_STORE_IMAGE)).into(holder.userImage);
                        }
                        break;
                    case "invite":
                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_USER_NAME), "", tempMap.get(Constants.TAG_MESSAGE), "", "", "invite");
                        if (!tempMap.get(Constants.TAG_USER_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_USER_IMAGE)).into(holder.userImage);
                        }
                        break;
                    case "credit":
                        holder.userName.setText(Html.fromHtml(tempMap.get(Constants.TAG_MESSAGE)));
                        if (!tempMap.get(Constants.TAG_USER_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_USER_IMAGE)).into(holder.userImage);
                        }
                        break;
                }

                if (tempMap.get(Constants.TAG_DATE) != null && !tempMap.get(Constants.TAG_DATE).equals("")) {
                    long date = Long.parseLong(tempMap.get(Constants.TAG_DATE)) * 1000;
                    holder.date.setText(FantacyApplication.getDate(date));
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

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            ProgressBar progressBar;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView userImage, itemImage;
            TextView userName, newsText, date;
            LinearLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                userImage = (ImageView) view.findViewById(R.id.userImage);
                itemImage = (ImageView) view.findViewById(R.id.itemImage);
                userName = (TextView) view.findViewById(R.id.userName);
                newsText = (TextView) view.findViewById(R.id.newsText);
                date = (TextView) view.findViewById(R.id.date);
                mainLay = (LinearLayout) view.findViewById(R.id.mainLay);

                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mainLay:
                        switch (Items.get(getAdapterPosition()).get(Constants.TAG_TYPE)) {
                            case "follow": {
                                Intent p = new Intent(context, Profile.class);
                                p.putExtra("userId", Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
                                context.startActivity(p);
                                break;
                            }
                            case "order_status": {
                                Intent p = new Intent(context, OrderDetail.class);
                                p.putExtra("orderId", Items.get(getAdapterPosition()).get(Constants.TAG_ORDER_ID));
                                context.startActivity(p);
                                break;
                            }
                            case "cart_notification": {
                                Intent c = new Intent(context, CartActivity.class);
                                c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                c.putExtra("shippingId", "0");
                                c.putExtra("itemId", "0");
                                context.startActivity(c);
                                break;
                            }
                            case "dispute_message": {
                                Intent c = new Intent(context, DisputeChat.class);
                                c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                c.putExtra("disputeId", Items.get(getAdapterPosition()).get(Constants.TAG_DISPUTE_ID));
                                c.putExtra("from", Items.get(getAdapterPosition()).get(Constants.TAG_DISPUTE_STATUS));
                                context.startActivity(c);
                                break;
                            }
                            case "group_gift": {
                                String giftdetail[] = Items.get(getAdapterPosition()).get(Constants.TAG_MESSAGE).split(":");
                                Intent c = new Intent(context, GroupGiftDetail.class);
                                c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                c.putExtra("giftId", giftdetail[2].trim());
                                context.startActivity(c);
                                //Toast.makeText(context, "Under Construction !", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            case "credit": {
                                Intent c = new Intent(context, GiftHistory.class);
                                c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(c);
                                break;
                            }
                        }
                        break;
                }
            }
        }
    }

    private static void linkTextView(final Context context, TextView view, final String userName, final String itemName, String innerStatus, final String userId, final String itemId, final String type) {

        SpannableStringBuilder spanTxt = new SpannableStringBuilder(userName);
        spanTxt.setSpan(new MyClickableSpan(context) {
            @Override
            public void onClick(View widget) {
                FantacyApplication.showToast(context, userName + ": " + userId, Toast.LENGTH_SHORT);
            }
        }, 0, spanTxt.length(), 0);
        spanTxt.append(" " + innerStatus + " ");
        if (!itemName.equals("")) {
            int count = spanTxt.length();
            spanTxt.append(itemName);
            spanTxt.setSpan(new MyClickableSpan(context) {
                @Override
                public void onClick(View widget) {
                    if (type.equals("follow")) {
                        Intent p = new Intent(context, Profile.class);
                        p.putExtra("userId", userId);
                        context.startActivity(p);
                    }
                    FantacyApplication.showToast(context, itemName + ": " + itemId,
                            Toast.LENGTH_SHORT);
                }
            }, count, spanTxt.length(), 0);
        }

        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    static class MyClickableSpan extends ClickableSpan {
        Context context;

        public MyClickableSpan(Context context) {
            super();
            this.context = context;
        }

        @Override
        public void onClick(View tv) {
            // FantacyApplication.showToast(context, name + ": " + id, Toast.LENGTH_SHORT);
        }

        @Override
        public void updateDrawState(TextPaint ds) {// override updateDrawState
            ds.setUnderlineText(false); // set to false to remove underline
            ds.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
    }
}