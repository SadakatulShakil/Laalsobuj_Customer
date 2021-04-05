package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.EndlessRecyclerOnScrollListener;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 23/6/17.
 */

public class StoreNews extends Fragment {

    private static final String TAG = StoreNews.class.getSimpleName();
    private String from = "", storeId = "";
    ImageView nullImage;
    TextView nullText;
    RelativeLayout nullLay;
    RecyclerView recyclerView;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<HashMap<String, String>> newsAry = new ArrayList<HashMap<String, String>>();

    public static StoreNews newInstance(String from, String storeId) {
        StoreNews fragment = new StoreNews();
        Bundle args = new Bundle();
        args.putString("from", from);
        args.putString("storeId", storeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.liked_main_layout, container, false);
        Log.i(TAG, "StoreNews: onCreateView");

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

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(getActivity(),
                linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), newsAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getNews(current_page * Constants.OVERALL_LIMIT);
                    Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                newsAry.clear();
                getNews(0);
            }
        });

        newsAry.clear();
        getNews(0);
    }

    private void getNews(final int offset) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_STORE_NEWS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getNewsRes=" + res);
                    JSONObject json = new JSONObject(res);
                    if (newsAry.size() >= Constants.OVERALL_LIMIT && newsAry.get(newsAry.size() - 1) == null) {
                        newsAry.remove(newsAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(newsAry.size());
                    }

                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray result = json.getJSONArray(Constants.TAG_RESULT);
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject temp = result.getJSONObject(i);

                            HashMap<String, String> map = new HashMap<String, String>();

                            String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);
                            String date = DefensiveClass.optString(temp, Constants.TAG_DATE);

                            map.put(Constants.TAG_MESSAGE, message);
                            map.put(Constants.TAG_DATE, date);

                            newsAry.add(map);
                        }

                        if (mScrollListener != null && newsAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    }

                    if (newsAry.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                        nullImage.setImageResource(R.drawable.no_message);
                        nullText.setText(getString(R.string.no_news));
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
                Log.e(TAG, "getNewsError: " + error.getMessage());
                setErrorLayout();
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (newsAry.size() >= Constants.OVERALL_LIMIT) {
                            newsAry.add(null);
                            recyclerViewAdapter.notifyItemInserted(newsAry.size() - 1);
                        } else if (newsAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                            swipeRefresh();
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();
                if (GetSet.isLogged())
                    map.put("user_id", GetSet.getUserId());
                map.put("store_id", storeId);
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                Log.v(TAG, "getNewsParams=" + map);
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
        if (newsAry.size() == 0) {
            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
            }
            nullLay.setVisibility(View.VISIBLE);
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

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView date, message;

            public MyViewHolder(View view) {
                super(view);

                date = (TextView) view.findViewById(R.id.date);
                message = (TextView) view.findViewById(R.id.message);
            }
        }

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_list_items, parent, false);
                return new MyViewHolder(view);
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
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                HashMap<String, String> tempMap = Items.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;

                viewHolder.message.setText(tempMap.get(Constants.TAG_MESSAGE));

                if (!tempMap.get(Constants.TAG_DATE).equals(null) && !tempMap.get(Constants.TAG_DATE).equals("")) {
                    long date = Long.parseLong(tempMap.get(Constants.TAG_DATE)) * 1000;
                    viewHolder.date.setText(getDate(date));
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

    /**
     * To convert timestamp to Date
     **/
    public static String getDate(long timeStamp) {

        try {
            DateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }
}
