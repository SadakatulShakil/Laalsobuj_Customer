package com.futureskyltd.app.fantacy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.futureskyltd.app.external.LinkEllipseTextView;
import com.futureskyltd.app.helper.FragmentChangeListener;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 22/9/17.
 */
public class FeedsFragment extends Fragment {

    private final String TAG = FeedsFragment.class.getSimpleName();
    RecyclerView recyclerView;
    TextView createfeed;
    LinearLayoutManager linearLayoutManager;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    RelativeLayout progressLay, nullLay;
    ImageView nullImage;
    TextView nullText;
    Display display;
    public static boolean refreshFeeds = false;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<HashMap<String, String>> feedsAry = new ArrayList<HashMap<String, String>>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dispute_recyclerview, container, false);


        createfeed = (TextView) rootView.findViewById(R.id.button);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));
        nullLay = (RelativeLayout) rootView.findViewById(R.id.nullLay);
        nullImage = (ImageView) rootView.findViewById(R.id.nullImage);
        nullText = (TextView) rootView.findViewById(R.id.nullText);
        progressLay = (RelativeLayout) rootView.findViewById(R.id.progress);

        ((FragmentChangeListener) getActivity()).setNavSelectionItem(R.id.liveFeeds, "Live Feeds");

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        createfeed.setVisibility(View.VISIBLE);
        display = getActivity().getWindowManager().getDefaultDisplay();

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(getActivity(), linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.emptyspace));
        recyclerView.addItemDecoration(itemDivider);

        createfeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), CreateFeed.class);
                startActivity(i);
            }
        });

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), feedsAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                Log.v(TAG, "onLoadMoreOffset:" + current_page);
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getFeedsData(current_page * Constants.OVERALL_LIMIT);
                } else {
                    /*Reset the page count while refreshing, otherwise offset increased by one*/
                    mScrollListener.resetpagecount();
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                feedsAry.clear();
                getFeedsData(0);
            }
        });

        feedsAry.clear();
        getFeedsData(0);
    }


    private void getFeedsData(final int offset) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_LIVE_FEEDS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getFeedsDataRes=" + res);
                    progressLay.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setEnabled(true);
                    if (feedsAry.size() >= Constants.OVERALL_LIMIT && feedsAry.get(feedsAry.size() - 1) == null) {
                        feedsAry.remove(feedsAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(feedsAry.size());
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

                            if (type.equalsIgnoreCase("add_item")) {
                                String store_id = DefensiveClass.optString(temp, Constants.TAG_STORE_ID);
                                String store_name = DefensiveClass.optString(temp, Constants.TAG_STORE_NAME);
                                String store_image = DefensiveClass.optString(temp, Constants.TAG_STORE_IMAGE);
                                String item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                                String item_name = DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME);
                                String item_image = DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE);
                                String height = DefensiveClass.optString(temp, Constants.TAG_HEIGHT);
                                String width = DefensiveClass.optString(temp, Constants.TAG_WIDTH);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);

                                map.put(Constants.TAG_STORE_ID, store_id);
                                map.put(Constants.TAG_STORE_NAME, store_name);
                                map.put(Constants.TAG_STORE_IMAGE, store_image);
                                map.put(Constants.TAG_ITEM_ID, item_id);
                                map.put(Constants.TAG_ID, item_id);
                                map.put(Constants.TAG_ITEM_NAME, item_name);
                                map.put(Constants.TAG_ITEM_IMAGE, item_image);
                                map.put(Constants.TAG_HEIGHT, height);
                                map.put(Constants.TAG_WIDTH, width);
                                map.put(Constants.TAG_DATE, date);
                            } else if (type.equalsIgnoreCase("comment")) {
                               // String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_id = "289";
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                                String item_name = DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME);
                                String item_image = DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE);
                                String height = DefensiveClass.optString(temp, Constants.TAG_HEIGHT);
                                String width = DefensiveClass.optString(temp, Constants.TAG_WIDTH);
                                String comment = DefensiveClass.optString(temp, Constants.TAG_COMMENT);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);

                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_ITEM_ID, item_id);
                                map.put(Constants.TAG_ITEM_NAME, item_name);
                                map.put(Constants.TAG_ITEM_IMAGE, item_image);
                                map.put(Constants.TAG_HEIGHT, height);
                                map.put(Constants.TAG_WIDTH, width);
                                map.put(Constants.TAG_COMMENT, comment);
                                map.put(Constants.TAG_DATE, date);
                            } else if (type.equalsIgnoreCase("status_share")) {
                                String feed_id = DefensiveClass.optString(temp, Constants.TAG_FEED_ID);
                                String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);
                                String status_image = DefensiveClass.optString(temp, Constants.TAG_STATUS_IMAGE);
                                String height = DefensiveClass.optString(temp, Constants.TAG_HEIGHT);
                                String width = DefensiveClass.optString(temp, Constants.TAG_WIDTH);
                                String liked = DefensiveClass.optString(temp, Constants.TAG_LIKED);
                                String likes_count = DefensiveClass.optString(temp, Constants.TAG_LIKES_COUNT);
                                String comments_count = DefensiveClass.optString(temp, Constants.TAG_COMMENTS_COUNT);
                                String shared_feed_id = DefensiveClass.optString(temp, Constants.TAG_SHARED_FEED_ID);
                                String shared_user_id = DefensiveClass.optString(temp, Constants.TAG_SHARED_USER_ID);
                                String shared_user_name = DefensiveClass.optString(temp, Constants.TAG_SHARED_USER_NAME);
                                // String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_id = "289";
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);

                                map.put(Constants.TAG_FEED_ID, feed_id);
                                map.put(Constants.TAG_MESSAGE, message);
                                map.put(Constants.TAG_STATUS_IMAGE, status_image);
                                map.put(Constants.TAG_HEIGHT, height);
                                map.put(Constants.TAG_WIDTH, width);
                                map.put(Constants.TAG_LIKED, liked);
                                map.put(Constants.TAG_LIKES_COUNT, likes_count);
                                map.put(Constants.TAG_COMMENTS_COUNT, comments_count);
                                map.put(Constants.TAG_SHARED_FEED_ID, shared_feed_id);
                                map.put(Constants.TAG_SHARED_USER_ID, shared_user_id);
                                map.put(Constants.TAG_SHARED_USER_NAME, shared_user_name);
                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_DATE, date);
                            } else if (type.equalsIgnoreCase("status")) {
                                String feed_id = DefensiveClass.optString(temp, Constants.TAG_FEED_ID);
                                String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);
                                String status_image = DefensiveClass.optString(temp, Constants.TAG_STATUS_IMAGE);
                                String height = DefensiveClass.optString(temp, Constants.TAG_HEIGHT);
                                String width = DefensiveClass.optString(temp, Constants.TAG_WIDTH);
                                String liked = DefensiveClass.optString(temp, Constants.TAG_LIKED);
                                String likes_count = DefensiveClass.optString(temp, Constants.TAG_LIKES_COUNT);
                                String comments_count = DefensiveClass.optString(temp, Constants.TAG_COMMENTS_COUNT);
                                // String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_id = "289";
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);

                                map.put(Constants.TAG_FEED_ID, feed_id);
                                map.put(Constants.TAG_MESSAGE, message);
                                map.put(Constants.TAG_STATUS_IMAGE, status_image);
                                map.put(Constants.TAG_HEIGHT, height);
                                map.put(Constants.TAG_WIDTH, width);
                                map.put(Constants.TAG_LIKED, liked);
                                map.put(Constants.TAG_LIKES_COUNT, likes_count);
                                map.put(Constants.TAG_COMMENTS_COUNT, comments_count);
                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_DATE, date);
                            } else if (type.equalsIgnoreCase("liked")) {
                                // String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_id = "289";
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                                String item_name = DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME);
                                String item_image = DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE);
                                String height = DefensiveClass.optString(temp, Constants.TAG_HEIGHT);
                                String width = DefensiveClass.optString(temp, Constants.TAG_WIDTH);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);
                                String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);

                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_ITEM_ID, item_id);
                                map.put(Constants.TAG_ITEM_NAME, item_name);
                                map.put(Constants.TAG_ITEM_IMAGE, item_image);
                                map.put(Constants.TAG_HEIGHT, height);
                                map.put(Constants.TAG_WIDTH, width);
                                map.put(Constants.TAG_DATE, date);
                                map.put(Constants.TAG_MESSAGE, message);
                            } else if (type.equalsIgnoreCase("mentioned")) {
                                // String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_id = "289";
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                                String item_name = DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME);
                                String item_image = DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE);
                                String height = DefensiveClass.optString(temp, Constants.TAG_HEIGHT);
                                String width = DefensiveClass.optString(temp, Constants.TAG_WIDTH);
                                String comment = DefensiveClass.optString(temp, Constants.TAG_COMMENT);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);

                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_ITEM_ID, item_id);
                                map.put(Constants.TAG_ITEM_NAME, item_name);
                                map.put(Constants.TAG_ITEM_IMAGE, item_image);
                                map.put(Constants.TAG_HEIGHT, height);
                                map.put(Constants.TAG_WIDTH, width);
                                map.put(Constants.TAG_COMMENT, comment);
                                map.put(Constants.TAG_DATE, date);
                            } else if (type.equalsIgnoreCase("mentioned_status")) {
                                String feed_id = DefensiveClass.optString(temp, Constants.TAG_FEED_ID);
                                // String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_id = "289";
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String comment = DefensiveClass.optString(temp, Constants.TAG_COMMENTS);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);

                                map.put(Constants.TAG_FEED_ID, feed_id);
                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_COMMENT, comment);
                                map.put(Constants.TAG_DATE, date);
                            } else if (type.equalsIgnoreCase("checkin")) {
                                String feed_id = DefensiveClass.optString(temp, Constants.TAG_FEED_ID);
                                String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);
                                String liked = DefensiveClass.optString(temp, Constants.TAG_LIKED);
                                String likes_count = DefensiveClass.optString(temp, Constants.TAG_LIKES_COUNT);
                                String comments_count = DefensiveClass.optString(temp, Constants.TAG_COMMENTS_COUNT);
                                // String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                                String user_id = "289";
                                String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                                String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                                String store_id = DefensiveClass.optString(temp, Constants.TAG_STORE_ID);
                                String store_name = DefensiveClass.optString(temp, Constants.TAG_STORE_NAME);
                                String store_image = DefensiveClass.optString(temp, Constants.TAG_STORE_IMAGE);
                                String date = DefensiveClass.optString(temp, Constants.TAG_DATE);

                                map.put(Constants.TAG_FEED_ID, feed_id);
                                map.put(Constants.TAG_MESSAGE, message);
                                map.put(Constants.TAG_LIKED, liked);
                                map.put(Constants.TAG_LIKES_COUNT, likes_count);
                                map.put(Constants.TAG_COMMENTS_COUNT, comments_count);
                                map.put(Constants.TAG_USER_ID, user_id);
                                map.put(Constants.TAG_USER_NAME, user_name);
                                map.put(Constants.TAG_USER_IMAGE, user_image);
                                map.put(Constants.TAG_STORE_ID, store_id);
                                map.put(Constants.TAG_STORE_NAME, store_name);
                                map.put(Constants.TAG_STORE_IMAGE, store_image);
                                map.put(Constants.TAG_DATE, date);
                            }

                            feedsAry.add(map);
                        }

                        if (mScrollListener != null && feedsAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                            /*Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                           /* Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                        }
                    }
                    recyclerViewAdapter.notifyDataSetChanged();
                    if (feedsAry.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_notification);
                        nullText.setText(getString(R.string.no_notification));
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
                Log.e(TAG, "getFeedsDataError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (feedsAry.size() >= Constants.OVERALL_LIMIT) {
                            feedsAry.add(null);
                            recyclerViewAdapter.notifyItemInserted(feedsAry.size() - 1);
                        } else if (feedsAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                            progressLay.setVisibility(View.VISIBLE);
                            mSwipeRefreshLayout.setEnabled(false);
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", "289");
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                map.put("type", "feeds");
                Log.v(TAG, "getFeedsDataParams=" + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void setErrorLayout() {
        if (feedsAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
            nullLay.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (refreshFeeds) {
            refreshFeeds = false;
            mScrollListener.resetpagecount();
            feedsAry.clear();
            getFeedsData(0);
        }
    }


    public void sharePost(final String feedid, final Context context) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SHARE_POST, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.i(TAG, "sharePostRes: " + res);
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        FantacyApplication.showToast(context, getString(R.string.shared_successfully), Toast.LENGTH_SHORT);
                        feedsAry.clear();
                        getFeedsData(0);
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
                Log.e(TAG, "sharePostError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", "289");
                map.put("feed_id", feedid);
                Log.i(TAG, "sharePostParams: " + map);
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

        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    public void postLike(final int position) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_POST_LIKE, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        if (feedsAry.get(position).get(Constants.TAG_LIKED).equals("yes")) {
                            feedsAry.get(position).put(Constants.TAG_LIKED, "no");
                        } else
                            feedsAry.get(position).put(Constants.TAG_LIKED, "yes");
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
                if (feedsAry.get(position).get(Constants.TAG_LIKED).equals("yes")) {
                    feedsAry.get(position).put(Constants.TAG_LIKED, "no");
                } else
                    feedsAry.get(position).put(Constants.TAG_LIKED, "yes");
                recyclerViewAdapter.notifyDataSetChanged();
                Log.e(TAG, "postLikeError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", "289");
                map.put("feed_id", feedsAry.get(position).get(Constants.TAG_FEED_ID));
                Log.v(TAG, "postLikeParams=" + map);
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

            ImageView userImage, itemImage, cancel, likeicon;
            TextView userName, date, likes, comments, like, comment, share;
            LinearLayout likeLay, commentLay, shareLay, bottomLay;
            LinkEllipseTextView message;
            TextView buttonViewOption;

            public MyViewHolder(View view) {
                super(view);

                userImage = (ImageView) view.findViewById(R.id.userImage);
                itemImage = (ImageView) view.findViewById(R.id.itemImage);
                cancel = (ImageView) view.findViewById(R.id.cancel);
                userName = (TextView) view.findViewById(R.id.userName);
                message = (LinkEllipseTextView) view.findViewById(R.id.message);
                date = (TextView) view.findViewById(R.id.date);
                likes = (TextView) view.findViewById(R.id.likes);
                likeicon = (ImageView) view.findViewById(R.id.likeicon);
                comments = (TextView) view.findViewById(R.id.comments);
                like = (TextView) view.findViewById(R.id.like);
                comment = (TextView) view.findViewById(R.id.comment);
                share = (TextView) view.findViewById(R.id.share);
                likeLay = (LinearLayout) view.findViewById(R.id.likeLay);
                commentLay = (LinearLayout) view.findViewById(R.id.commentLay);
                shareLay = (LinearLayout) view.findViewById(R.id.shareLay);
                bottomLay = (LinearLayout) view.findViewById(R.id.bottomLay);
                buttonViewOption = (TextView) view.findViewById(R.id.textViewOptions);

                itemImage.setOnClickListener(this);
                commentLay.setOnClickListener(this);
                shareLay.setOnClickListener(this);
                comments.setOnClickListener(this);
                userImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.comments:
                    case R.id.commentLay:
                        final HashMap<String, String> tempMap = Items.get(getAdapterPosition());
                        Intent i = new Intent(context, AllComments.class);
                        i.putExtra("from", "feeds");
                        i.putExtra("feedId", tempMap.get(Constants.TAG_FEED_ID));
                        i.putExtra("image", tempMap.get(Constants.TAG_USER_IMAGE));
                        i.putExtra("itemTitle", tempMap.get(Constants.TAG_USER_NAME));
                        context.startActivity(i);
                        break;
                    case R.id.shareLay:
                        final HashMap<String, String> templist = Items.get(getAdapterPosition());
                        sharePost(templist.get(Constants.TAG_FEED_ID), context);
                        break;
                    case R.id.itemImage:
                        if (Items.get(getAdapterPosition()).get(Constants.TAG_ITEM_ID) != null &&
                                !Items.get(getAdapterPosition()).get(Constants.TAG_ITEM_ID).equals("")) {
                            getItemDetails(Items.get(getAdapterPosition()).get(Constants.TAG_ITEM_ID), getActivity());
                        }
                        break;
                    case R.id.userImage:
                        if (Items.get(getAdapterPosition()).get(Constants.TAG_TYPE).equals("add_item")) {
                            Intent p = new Intent(context, StoreProfile.class);
                            p.putExtra("storeId", Items.get(getAdapterPosition()).get(Constants.TAG_STORE_ID));
                            startActivity(p);
                        } else {
                            Intent p = new Intent(context, Profile.class);
                            p.putExtra("userId", Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
                            startActivity(p);
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
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_feeds_item, parent, false);
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
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
            if (viewHolder instanceof MyViewHolder) {
                MyViewHolder holder = (MyViewHolder) viewHolder;
                final HashMap<String, String> tempMap = Items.get(position);

                holder.bottomLay.setVisibility(View.GONE);
                holder.cancel.setVisibility(View.GONE);
                Picasso.get().load(R.drawable.error_drawable).into(holder.itemImage);
                holder.itemImage.setVisibility(View.GONE);
                holder.message.setVisibility(View.GONE);
                holder.message.setIsLinkable(true);
                switch (tempMap.get(Constants.TAG_TYPE)) {
                    case "add_item":
                        holder.message.setVisibility(View.VISIBLE);
                        holder.itemImage.setVisibility(View.VISIBLE);

                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_STORE_NAME), tempMap.get(Constants.TAG_ITEM_NAME), context.getString(R.string.added_the_item), tempMap.get(Constants.TAG_STORE_ID), tempMap.get(Constants.TAG_ITEM_ID));

                        holder.message.setText(Html.fromHtml(tempMap.get(Constants.TAG_ITEM_NAME)));
                        if (!tempMap.get(Constants.TAG_STORE_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_STORE_IMAGE)).into(holder.userImage);
                        }
                        if (!tempMap.get(Constants.TAG_ITEM_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.itemImage);
                        }
                        break;

                    case "comment":
                        holder.message.setVisibility(View.VISIBLE);
                        holder.itemImage.setVisibility(View.VISIBLE);

                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_USER_NAME), tempMap.get(Constants.TAG_ITEM_NAME), context.getString(R.string.commented_on), tempMap.get(Constants.TAG_USER_ID), tempMap.get(Constants.TAG_ITEM_ID));
                        holder.message.setText(Html.fromHtml(tempMap.get(Constants.TAG_COMMENT)));
                        if (!tempMap.get(Constants.TAG_USER_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_USER_IMAGE)).into(holder.userImage);
                        }
                        if (!tempMap.get(Constants.TAG_ITEM_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.itemImage);
                        }
                        break;

                    case "status_share":
                        holder.bottomLay.setVisibility(View.VISIBLE);

                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_USER_NAME), tempMap.get(Constants.TAG_SHARED_USER_NAME), context.getString(R.string.shared), tempMap.get(Constants.TAG_USER_ID), tempMap.get(Constants.TAG_SHARED_USER_ID));
                        if (!tempMap.get(Constants.TAG_USER_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_USER_IMAGE)).into(holder.userImage);
                        }
                        if (!tempMap.get(Constants.TAG_STATUS_IMAGE).equals("")) {
                            holder.itemImage.setVisibility(View.VISIBLE);
                            Picasso.get().load(tempMap.get(Constants.TAG_STATUS_IMAGE)).placeholder(R.drawable.error_drawable).error(R.drawable.error_drawable).into(holder.itemImage);
                        }
                        if (!tempMap.get(Constants.TAG_MESSAGE).equals("")) {
                            holder.message.setVisibility(View.VISIBLE);
                            holder.message.setText(Html.fromHtml(tempMap.get(Constants.TAG_MESSAGE)));

                        }
                        if (tempMap.get(Constants.TAG_LIKED).equals("yes"))
                            holder.likeicon.setImageResource(R.drawable.liked);
                        else {
                            holder.likeicon.setImageResource(R.drawable.unlike);
                        }

                        if (Integer.parseInt(tempMap.get(Constants.TAG_LIKES_COUNT)) <= 1)
                            holder.likes.setText(tempMap.get(Constants.TAG_LIKES_COUNT) + " Like");
                        else
                            holder.likes.setText(tempMap.get(Constants.TAG_LIKES_COUNT) + " Likes");

                        if (Integer.parseInt(tempMap.get(Constants.TAG_COMMENTS_COUNT)) <= 1)
                            holder.comments.setText(tempMap.get(Constants.TAG_COMMENTS_COUNT) + " Comment");
                        else
                            holder.comments.setText(tempMap.get(Constants.TAG_COMMENTS_COUNT) + " Comments");

                        break;

                    case "status":
                        holder.bottomLay.setVisibility(View.VISIBLE);

                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_USER_NAME), "", context.getString(R.string.posted_status), tempMap.get(Constants.TAG_USER_ID), "");

                        if (!tempMap.get(Constants.TAG_USER_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_USER_IMAGE)).into(holder.userImage);
                        }
                        if (!tempMap.get(Constants.TAG_STATUS_IMAGE).equals("")) {
                            holder.itemImage.setVisibility(View.VISIBLE);
                            Picasso.get().load(tempMap.get(Constants.TAG_STATUS_IMAGE)).placeholder(R.drawable.error_drawable).error(R.drawable.error_drawable).into(holder.itemImage);
                        }
                        if (!tempMap.get(Constants.TAG_MESSAGE).equals("")) {
                            holder.message.setVisibility(View.VISIBLE);
                            holder.message.setText(Html.fromHtml(tempMap.get(Constants.TAG_MESSAGE)));

                        }
                        if (tempMap.get(Constants.TAG_LIKED).equals("yes"))
                            holder.likeicon.setImageResource(R.drawable.liked);
                        else {
                            holder.likeicon.setImageResource(R.drawable.unlike);
                        }

                        if (Integer.parseInt(tempMap.get(Constants.TAG_LIKES_COUNT)) <= 1)
                            holder.likes.setText(tempMap.get(Constants.TAG_LIKES_COUNT) + " Like");
                        else
                            holder.likes.setText(tempMap.get(Constants.TAG_LIKES_COUNT) + " Likes");

                        if (Integer.parseInt(tempMap.get(Constants.TAG_COMMENTS_COUNT)) <= 1)
                            holder.comments.setText(tempMap.get(Constants.TAG_COMMENTS_COUNT) + " Comment");
                        else
                            holder.comments.setText(tempMap.get(Constants.TAG_COMMENTS_COUNT) + " Comments");

                        break;

                    case "liked":
                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_USER_NAME), tempMap.get(Constants.TAG_ITEM_NAME), context.getString(R.string.liked_this), tempMap.get(Constants.TAG_USER_ID), tempMap.get(Constants.TAG_ITEM_ID));

                        if (!tempMap.get(Constants.TAG_USER_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_USER_IMAGE)).into(holder.userImage);
                        }
                        if (!tempMap.get(Constants.TAG_ITEM_IMAGE).equals("")) {
                            holder.itemImage.setVisibility(View.VISIBLE);
                            Picasso.get().load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.itemImage);
                        }

                        if (!tempMap.get(Constants.TAG_MESSAGE).equals("")) {
                            holder.message.setVisibility(View.VISIBLE);
                            holder.message.setText(Html.fromHtml(tempMap.get(Constants.TAG_MESSAGE)));
                        }
                        break;

                    case "mentioned":
                        holder.message.setVisibility(View.GONE);
                        holder.itemImage.setVisibility(View.VISIBLE);
                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_USER_NAME), tempMap.get(Constants.TAG_ITEM_NAME), context.getString(R.string.mentioned_you_on), tempMap.get(Constants.TAG_USER_ID), tempMap.get(Constants.TAG_ITEM_ID));

                        if (!tempMap.get(Constants.TAG_USER_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_USER_IMAGE)).into(holder.userImage);
                        }
                        if (!tempMap.get(Constants.TAG_ITEM_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.itemImage);
                        }
                        break;

                    case "mentioned_status":
                        holder.message.setVisibility(View.VISIBLE);

                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_USER_NAME), "", context.getString(R.string.mentioned_you_on_status), tempMap.get(Constants.TAG_USER_ID), "");

                        holder.message.setText(Html.fromHtml(tempMap.get(Constants.TAG_COMMENT)));
                        if (!tempMap.get(Constants.TAG_USER_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_USER_IMAGE)).into(holder.userImage);
                        }
                        break;

                    case "checkin":
                        holder.bottomLay.setVisibility(View.VISIBLE);
                        holder.itemImage.setVisibility(View.VISIBLE);

                        linkTextView(context, holder.userName, tempMap.get(Constants.TAG_USER_NAME), tempMap.get(Constants.TAG_STORE_NAME), context.getString(R.string.at), tempMap.get(Constants.TAG_USER_ID), tempMap.get(Constants.TAG_STORE_ID));

                        if (!tempMap.get(Constants.TAG_USER_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_USER_IMAGE)).into(holder.userImage);
                        }
                        if (!tempMap.get(Constants.TAG_STORE_IMAGE).equals("")) {
                            Picasso.get().load(tempMap.get(Constants.TAG_STORE_IMAGE)).into(holder.itemImage);
                        }
                        if (tempMap.get(Constants.TAG_LIKED).equals("yes"))
                            holder.likeicon.setImageResource(R.drawable.liked);
                        else {
                            holder.likeicon.setImageResource(R.drawable.unlike);
                        }
                        if (Integer.parseInt(tempMap.get(Constants.TAG_LIKES_COUNT)) <= 1)
                            holder.likes.setText(tempMap.get(Constants.TAG_LIKES_COUNT) + " Like");
                        else
                            holder.likes.setText(tempMap.get(Constants.TAG_LIKES_COUNT) + " Likes");

                        if (Integer.parseInt(tempMap.get(Constants.TAG_COMMENTS_COUNT)) <= 1)
                            holder.comments.setText(tempMap.get(Constants.TAG_COMMENTS_COUNT) + " Comment");
                        else
                            holder.comments.setText(tempMap.get(Constants.TAG_COMMENTS_COUNT) + " Comments");

                        break;
                }
                if (tempMap.get(Constants.TAG_USER_ID) != null && GetSet.getUserId().equals(tempMap.get((Constants.TAG_USER_ID)))) {
                    holder.buttonViewOption.setVisibility(View.VISIBLE);

                } else
                    holder.buttonViewOption.setVisibility(View.GONE);

                holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String[] values = new String[]{getString(R.string.delete)};

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                                R.layout.option_row_item, android.R.id.text1, values);
                        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = layoutInflater.inflate(R.layout.option_layout, null);
                        if (FantacyApplication.isRTL(context)) {
                            layout.setAnimation(AnimationUtils.loadAnimation(context, R.anim.grow_from_topleft_to_bottomright));
                        } else {
                            layout.setAnimation(AnimationUtils.loadAnimation(context, R.anim.grow_from_topright_to_bottomleft));
                        }
                        final PopupWindow popup = new PopupWindow(context);
                        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        popup.setContentView(layout);
                        popup.setWidth(display.getWidth() * 50 / 100);
                        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                        popup.setFocusable(true);
                        //popup.showAtLocation(v, Gravity.TOP|Gravity.LEFT,0,v.getHeight());

                        final ListView lv = (ListView) layout.findViewById(R.id.listView);
                        lv.setAdapter(adapter);
                        popup.showAsDropDown(view, -((display.getWidth() * 45 / 100)), -60);

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int pos, long id) {
                                switch (pos) {
                                    case 0:
                                        deleteDialog(position, tempMap.get(Constants.TAG_FEED_ID));
                                        popup.dismiss();
                                        break;
                                }
                            }
                        });
                    }
                });

                if (!tempMap.get(Constants.TAG_DATE).equals(null) && !tempMap.get(Constants.TAG_DATE).equals("")) {
                    long date = Long.parseLong(tempMap.get(Constants.TAG_DATE)) * 1000;
                    holder.date.setText(FantacyApplication.getDate(date));
                }

                MovementMethod m = holder.message.getMovementMethod();
                if ((m == null) || !(m instanceof LinkMovementMethod)) {
                    if (holder.message.getLinksClickable()) {
                        holder.message.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                }
                holder.likes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent i = new Intent(getActivity(), FindFriends.class);
                        i.putExtra("to", "feedlikes");
                        i.putExtra("feedId", tempMap.get(Constants.TAG_FEED_ID));
                        startActivity(i);

                    }
                });
                holder.likeLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (tempMap.get(Constants.TAG_LIKED).equals("yes")) {
                            tempMap.put(Constants.TAG_LIKED, "no");
                            int tempcount = Integer.parseInt(tempMap.get(Constants.TAG_LIKES_COUNT)) - 1;
                            tempMap.put(Constants.TAG_LIKES_COUNT, String.valueOf(tempcount));
                            notifyDataSetChanged();
                            postLike(position);
                        } else {
                            tempMap.put(Constants.TAG_LIKED, "yes");
                            int tempcount = Integer.parseInt(tempMap.get(Constants.TAG_LIKES_COUNT)) + 1;
                            tempMap.put(Constants.TAG_LIKES_COUNT, String.valueOf(tempcount));
                            notifyDataSetChanged();
                            postLike(position);
                        }
                    }
                });

                holder.message.setOnTextLinkClickListener(new LinkEllipseTextView.TextLinkClickListener() {

                    @Override
                    public void onTextLinkClick(View textView, String clickedString) {
                        if (clickedString.contains("@")) {
                            Intent p = new Intent(context, Profile.class);
                            p.putExtra("userId", "");
                            p.putExtra("userName", clickedString.replace("@", ""));
                            startActivity(p);
                        }

                        if (clickedString.contains("#")) {
                            Intent i = new Intent(context, HashTag.class);
                            i.putExtra("key", clickedString.replace("#", ""));
                            startActivity(i);
                        }
                    }
                });
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

    private void deleteDialog(final int position, final String feedid) {
        Display display;
        display = getActivity().getWindowManager().getDefaultDisplay();
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView yes = (TextView) dialog.findViewById(R.id.yes);
        TextView no = (TextView) dialog.findViewById(R.id.no);

        title.setText(R.string.delete_feed_msg);
        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFeed(feedid, position);
                dialog.dismiss();
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

    private void linkTextView(final Context context, TextView view, final String userName, final String itemName, final String innerStatus, final String userId, final String itemId) {

        SpannableStringBuilder spanTxt = new SpannableStringBuilder(userName);
        spanTxt.setSpan(new MyClickableSpan(context) {
            @Override
            public void onClick(View widget) {
                    if(innerStatus.equalsIgnoreCase(context.getString(R.string.added_the_item))){
                        Intent i = new Intent(context, StoreProfile.class);
                        i.putExtra("storeId", userId);
                        startActivity(i);
                    }else {

                        Intent p = new Intent(context, Profile.class);
                        p.putExtra("userId", userId);
                        context.startActivity(p);
                    }
                // FantacyApplication.showToast(context, userName + ": " + userId, Toast.LENGTH_SHORT);
            }
        }, 0, spanTxt.length(), 0);

        spanTxt.append(" " + innerStatus + " ");

        if (!itemName.equals("")) {
            int count = spanTxt.length();
            spanTxt.append(itemName);
            spanTxt.setSpan(new MyClickableSpan(context) {
                @Override
                public void onClick(View widget) {
                    if (innerStatus.equals(context.getString(R.string.shared))) {
                        Intent p = new Intent(context, Profile.class);
                        p.putExtra("userId", itemId);
                        context.startActivity(p);
                    } else if (innerStatus.equals(context.getString(R.string.at))) {
                        Intent i = new Intent(context, StoreProfile.class);
                        i.putExtra("storeId", itemId);
                        context.startActivity(i);
                    } else {
                        getItemDetails(itemId, context);
                    }
                    //FantacyApplication.showToast(context, itemName + ": " + itemId, Toast.LENGTH_SHORT);
                }
            }, count, spanTxt.length(), 0);

            if (innerStatus.equals(context.getString(R.string.shared))) {
                spanTxt.append(context.getString(R.string.s_status));
            }
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
            ds.setTypeface(Typeface.create(ds.getTypeface(), Typeface.BOLD));
        }
    }

    private void deleteFeed(final String feedID, final int position) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_DELTETE_POST, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    Log.v(TAG, "deleteFeedRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        FantacyApplication.showToast(getActivity(), getString(R.string.post_deleted), Toast.LENGTH_SHORT);
                        feedsAry.remove(position);
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
                Log.e(TAG, "deleteFeedError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", "289");
                map.put("feed_id", feedID);
                Log.v(TAG, "deleteFeedParams=" + map);
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
        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void getItemDetails(final String itemID, final Context context) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ITEM_DETAILS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    ArrayList<HashMap<String, String>> itemsAry = new ArrayList<HashMap<String, String>>();
                    Log.v(TAG, "getItemDetailsRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject temp = json.optJSONObject(Constants.TAG_RESULT);
                        HashMap<String, String> map = new HashMap<>();
                        String id = DefensiveClass.optString(temp, Constants.TAG_ID);
                        String item_title = DefensiveClass.optString(temp, Constants.TAG_ITEM_TITLE);
                        String item_description = DefensiveClass.optString(temp, Constants.TAG_ITEM_DESCRIPTION);
                        String currency = DefensiveClass.optString(temp, Constants.TAG_CURRENCY);
                        String mainprice = DefensiveClass.optString(temp, Constants.TAG_MAIN_PRICE);
                        String price = DefensiveClass.optString(temp, Constants.TAG_PRICE);
                        String deal_enabled = DefensiveClass.optString(temp, Constants.TAG_DEAL_ENABLED);
                        String discount_percentage = DefensiveClass.optString(temp, Constants.TAG_DISCOUNT_PERCENTAGE);
                        String valid_till = DefensiveClass.optString(temp, Constants.TAG_VALID_TILL);
                        String quantity = DefensiveClass.optString(temp, Constants.TAG_QUANTITY);
                        String cod = DefensiveClass.optString(temp, Constants.TAG_COD);
                        String liked = DefensiveClass.optString(temp, Constants.TAG_LIKED);
                        String report = DefensiveClass.optString(temp, Constants.TAG_REPORT);
                        String reward_points = DefensiveClass.optString(temp, Constants.TAG_REWARD_POINTS);
                        String share_seller = DefensiveClass.optString(temp, Constants.TAG_SHARE_SELLER);
                        String share_user = DefensiveClass.optString(temp, Constants.TAG_SHARE_USER);
                        String approve = DefensiveClass.optString(temp, Constants.TAG_APPROVE);
                        String buy_type = DefensiveClass.optString(temp, Constants.TAG_BUY_TYPE);
                        String affiliate_link = DefensiveClass.optString(temp, Constants.TAG_AFFILIATE_LINK);
                        String shipping_time = DefensiveClass.optString(temp, Constants.TAG_SHIPPING_TIME);
                        String product_url = DefensiveClass.optString(temp, Constants.TAG_PRODUCT_URL);
                        String shop_id = DefensiveClass.optString(temp, Constants.TAG_SHOP_ID);
                        String shop_name = DefensiveClass.optString(temp, Constants.TAG_SHOP_NAME);
                        String shop_image = DefensiveClass.optString(temp, Constants.TAG_SHOP_IMAGE);
                        String store_follow = DefensiveClass.optString(temp, Constants.TAG_STORE_FOLLOW);
                        String average_rating = DefensiveClass.optString(temp, Constants.TAG_AVERAGE_RATING);

                        JSONArray size = temp.optJSONArray(Constants.TAG_SIZE);
                        if (size == null) {
                            map.put(Constants.TAG_SIZE, "");
                        } else if (size.length() == 0) {
                            map.put(Constants.TAG_SIZE, "");
                        } else {
                            map.put(Constants.TAG_SIZE, size.toString());
                        }

                        JSONArray photos = temp.optJSONArray(Constants.TAG_PHOTOS);
                        if (photos == null) {
                            map.put(Constants.TAG_PHOTOS, "");
                        } else if (photos.length() == 0) {
                            map.put(Constants.TAG_PHOTOS, "");
                        } else {
                            map.put(Constants.TAG_PHOTOS, photos.toString());
                        }

                        JSONArray selfies = temp.optJSONArray(Constants.TAG_PRODUCT_SELFIES);
                        if (selfies == null) {
                            map.put(Constants.TAG_PRODUCT_SELFIES, "");
                        } else if (selfies.length() == 0) {
                            map.put(Constants.TAG_PRODUCT_SELFIES, "");
                        } else {
                            map.put(Constants.TAG_PRODUCT_SELFIES, selfies.toString());
                        }

                        JSONArray comments = temp.optJSONArray(Constants.TAG_RECENT_COMMENTS);
                        if (comments == null) {
                            map.put(Constants.TAG_RECENT_COMMENTS, "");
                        } else if (comments.length() == 0) {
                            map.put(Constants.TAG_RECENT_COMMENTS, "");
                        } else {
                            map.put(Constants.TAG_RECENT_COMMENTS, comments.toString());
                        }

                        JSONArray storeProducts = temp.optJSONArray(Constants.TAG_STORE_PRODUCTS);
                        if (storeProducts == null) {
                            map.put(Constants.TAG_STORE_PRODUCTS, "");
                        } else if (storeProducts.length() == 0) {
                            map.put(Constants.TAG_STORE_PRODUCTS, "");
                        } else {
                            map.put(Constants.TAG_STORE_PRODUCTS, storeProducts.toString());
                        }

                        JSONArray similarProducts = temp.optJSONArray(Constants.TAG_SIMILAR_PRODUCTS);
                        if (similarProducts == null) {
                            map.put(Constants.TAG_SIMILAR_PRODUCTS, "");
                        } else if (similarProducts.length() == 0) {
                            map.put(Constants.TAG_SIMILAR_PRODUCTS, "");
                        } else {
                            map.put(Constants.TAG_SIMILAR_PRODUCTS, similarProducts.toString());
                        }

                        map.put(Constants.TAG_ID, id);
                        map.put(Constants.TAG_ITEM_TITLE, item_title);
                        map.put(Constants.TAG_ITEM_DESCRIPTION, item_description);
                        map.put(Constants.TAG_CURRENCY, currency);
                        map.put(Constants.TAG_MAIN_PRICE, mainprice);
                        map.put(Constants.TAG_PRICE, price);
                        map.put(Constants.TAG_DEAL_ENABLED, deal_enabled);
                        map.put(Constants.TAG_DISCOUNT_PERCENTAGE, discount_percentage);
                        map.put(Constants.TAG_VALID_TILL, valid_till);
                        map.put(Constants.TAG_QUANTITY, quantity);
                        map.put(Constants.TAG_COD, cod);
                        map.put(Constants.TAG_LIKED, liked);
                        map.put(Constants.TAG_REPORT, report);
                        map.put(Constants.TAG_REWARD_POINTS, reward_points);
                        map.put(Constants.TAG_SHARE_SELLER, share_seller);
                        map.put(Constants.TAG_SHARE_USER, share_user);
                        map.put(Constants.TAG_APPROVE, approve);
                        map.put(Constants.TAG_BUY_TYPE, buy_type);
                        map.put(Constants.TAG_AFFILIATE_LINK, affiliate_link);
                        map.put(Constants.TAG_SHIPPING_TIME, shipping_time);
                        map.put(Constants.TAG_PRODUCT_URL, product_url);
                        map.put(Constants.TAG_SHOP_ID, shop_id);
                        map.put(Constants.TAG_SHOP_NAME, shop_name);
                        map.put(Constants.TAG_SHOP_IMAGE, shop_image);
                        map.put(Constants.TAG_STORE_FOLLOW, store_follow);
                        map.put(Constants.TAG_AVERAGE_RATING, average_rating);

                        itemsAry.add(0, map);
                        Intent i = new Intent(getActivity(), DetailActivity.class);
                        i.putExtra("items", itemsAry);
                        i.putExtra("position", 0);
                        startActivity(i);
                    }else  Toast.makeText(getActivity().getApplicationContext(),""+getResources().getString(R.string.item_not_found),Toast.LENGTH_SHORT).show();

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
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Toast.makeText(getActivity().getApplicationContext(),""+getResources().getString(R.string.item_not_found),Toast.LENGTH_SHORT).show();
                Log.e(TAG, "getItemDetailsError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                if (GetSet.isLogged()) {
                    map.put("user_id", "289");
                }
                map.put("item_id", itemID);
                Log.v(TAG, "getItemDetailsParams=" + map);
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
        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }
}