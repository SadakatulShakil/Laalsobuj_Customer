package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.EndlessRecyclerOnScrollListener;
import com.futureskyltd.app.helper.DatabaseHandler;
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

public class WelcomeScreen extends BaseActivity {
    private static final String TAG = WelcomeScreen.class.getSimpleName();
    String type;
    ImageView backBtn, nullImage;
    TextView title, nullText;
    EditText searchView;
    RelativeLayout progressLay, nullLay;
    RecyclerView recyclerView;
    String searchKey = "";
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    AllStoreAdapter allstoreadapter;
    ArrayList<HashMap<String, String>> friendsAry = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> followersAry = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getStringExtra("type");
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        switch (type) {
            case "first":
                setContentView(R.layout.welcome_screen);
                this.setFinishOnTouchOutside(false);
                TextView next = (TextView) findViewById(R.id.continueBtn);
                TextView skip = (TextView) findViewById(R.id.skipBtn);
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(WelcomeScreen.this, WelcomeScreen.class);
                        i.putExtra("type", "second");
                        WelcomeScreen.this.startActivity(i);
                        finish();
                    }
                });
                skip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
                break;
            case "second":
                setContentView(R.layout.welcome_list);
                this.setFinishOnTouchOutside(false);
                TextView friendsnext = (TextView) findViewById(R.id.next);
                friendsnext.setVisibility(View.VISIBLE);
                friendsnext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(WelcomeScreen.this, WelcomeScreen.class);
                        i.putExtra("type", "third");
                        WelcomeScreen.this.startActivity(i);
                        finish();
                    }
                });
                title = (TextView) findViewById(R.id.title);
                backBtn = (ImageView) findViewById(R.id.backBtn);
                recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
                searchView = (EditText) findViewById(R.id.searchView);
                nullLay = (RelativeLayout) findViewById(R.id.nullLay);
                nullImage = (ImageView) findViewById(R.id.nullImage);
                nullText = (TextView) findViewById(R.id.nullText);
                progressLay = (RelativeLayout) findViewById(R.id.progress);
                mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

                title.setText(getString(R.string.follow_peoples));
                mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.progresscolor));

                title.setVisibility(View.VISIBLE);

                linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(linearLayoutManager);

                DividerItemDecoration itemDivider = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
                itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
                recyclerView.addItemDecoration(itemDivider);

                recyclerViewAdapter = new RecyclerViewAdapter(this, friendsAry);
                recyclerView.setAdapter(recyclerViewAdapter);

                searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            searchView.clearFocus();
                            searchKey = searchView.getText().toString();
                            FantacyApplication.hideSoftKeyboard(WelcomeScreen.this, textView);
                            friendsAry.clear();
                            mScrollListener.resetpagecount();
                            getFriends(0);
                            return true;
                        }

                        return false;
                    }
                });

                mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
                    @Override
                    public void onLoadMore(int current_page) {
                        if (!mSwipeRefreshLayout.isRefreshing()) {
                            getFriends(current_page * Constants.OVERALL_LIMIT);
                            Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                        }
                    }
                };
                recyclerView.addOnScrollListener(mScrollListener);

                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mScrollListener.resetpagecount();
                        friendsAry.clear();
                        getFriends(0);
                    }
                });

                getFriends(0);
                break;

            case "third":
                setContentView(R.layout.welcome_list);
                this.setFinishOnTouchOutside(false);
                TextView allstorenext = (TextView) findViewById(R.id.next);
                allstorenext.setVisibility(View.VISIBLE);
                title = (TextView) findViewById(R.id.title);
                backBtn = (ImageView) findViewById(R.id.backBtn);
                recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
                searchView = (EditText) findViewById(R.id.searchView);
                nullLay = (RelativeLayout) findViewById(R.id.nullLay);
                nullImage = (ImageView) findViewById(R.id.nullImage);
                nullText = (TextView) findViewById(R.id.nullText);
                progressLay = (RelativeLayout) findViewById(R.id.progress);
                mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

                title.setText(getString(R.string.follow_stores));
                mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.progresscolor));
                searchView.setHint(getString(R.string.search_stores));
                title.setVisibility(View.VISIBLE);
                linearLayoutManager = new LinearLayoutManager(WelcomeScreen.this, LinearLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(linearLayoutManager);

                DividerItemDecoration itemDivider1 = new DividerItemDecoration(WelcomeScreen.this, linearLayoutManager.getOrientation());
                itemDivider1.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
                recyclerView.addItemDecoration(itemDivider1);

                allstoreadapter = new AllStoreAdapter(WelcomeScreen.this, followersAry);
                recyclerView.setAdapter(allstoreadapter);

                followersAry.clear();
                getAllStores(0);

                searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            searchView.clearFocus();
                            searchKey = searchView.getText().toString();
                            FantacyApplication.hideSoftKeyboard(WelcomeScreen.this, textView);
                            followersAry.clear();
                            getAllStores(0);
                            return true;
                        }
                        return false;
                    }
                });

                mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
                    @Override
                    public void onLoadMore(int current_page) {
                        if (!mSwipeRefreshLayout.isRefreshing()) {
                            getAllStores(current_page * Constants.OVERALL_LIMIT);
                            Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                        }
                    }
                };
                recyclerView.addOnScrollListener(mScrollListener);
                allstorenext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mScrollListener.resetpagecount();
                        getAllStores(0);
                        Log.v("Onrefresh:", "On refresh");
                    }
                });
                break;

        }
    }

    private void getFriends(final int offset) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_FIND_FRIENDS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getFriendsRes=" + res);
                    JSONObject json = new JSONObject(res);
                    if (friendsAry.size() >= Constants.OVERALL_LIMIT && friendsAry.get(friendsAry.size() - 1) == null) {
                        friendsAry.remove(friendsAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(friendsAry.size());
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
                            String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                            String full_name = DefensiveClass.optString(temp, Constants.TAG_FULL_NAME);
                            String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                            String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                            String follow_status = DefensiveClass.optString(temp, Constants.TAG_STATUS);

                            map.put(Constants.TAG_USER_ID, user_id);
                            map.put(Constants.TAG_FULL_NAME, full_name);
                            map.put(Constants.TAG_USER_NAME, user_name);
                            map.put(Constants.TAG_USER_IMAGE, user_image);
                            map.put(Constants.TAG_STATUS, follow_status);

                            friendsAry.add(map);
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(false);
                        }
                    }

                    if (friendsAry.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                        nullImage.setImageResource(R.drawable.no_friends);
                        nullText.setText(getString(R.string.no_friends));
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                    progressLay.setVisibility(View.GONE);
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
                Log.e(TAG, "getFriendsError: " + error.getMessage());

                setErrorLayout();
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (friendsAry.size() >= Constants.OVERALL_LIMIT) {
                            friendsAry.add(null);
                            recyclerViewAdapter.notifyItemInserted(friendsAry.size() - 1);
                        } else if (friendsAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                            progressLay.setVisibility(View.VISIBLE);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();

                if (GetSet.isLogged()) {
                    map.put("user_id", GetSet.getUserId());
                }
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("search_key", searchKey);
                map.put("offset", Integer.toString(offset));
                Log.v(TAG, "getFriendsParams=" + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void followUser(final Boolean follow, final int position) {

        String CallingAPI;
        if (follow) // Follow Api
            CallingAPI = Constants.API_FOLLOW_USER;
        else  // Unfollow Api
            CallingAPI = Constants.API_UNFOLLOW_USER;

        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        if (follow) {
                            friendsAry.get(position).put(Constants.TAG_STATUS, "follow");
                        } else {
                            friendsAry.get(position).put(Constants.TAG_STATUS, "unfollow");
                        }
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
                Log.e(TAG, "followUserError: " + error.getMessage());
                if (follow) {
                    friendsAry.get(position).put(Constants.TAG_STATUS, "follow");
                } else {
                    friendsAry.get(position).put(Constants.TAG_STATUS, "unfollow");
                }
                recyclerViewAdapter.notifyDataSetChanged();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("follow_id", friendsAry.get(position).get(Constants.TAG_USER_ID));
                Log.i(TAG, "followUserParams= " + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;
        DatabaseHandler helper;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
            this.helper = DatabaseHandler.getInstance(context);
        }

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView userImage, followBtn;
            TextView fullName, userName;

            public MyViewHolder(View view) {
                super(view);

                userImage = (ImageView) view.findViewById(R.id.userImage);
                fullName = (TextView) view.findViewById(R.id.fullName);
                userName = (TextView) view.findViewById(R.id.userName);
                followBtn = (ImageView) view.findViewById(R.id.followBtn);
                followBtn.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.followBtn:
                        String status = Items.get(getAdapterPosition()).get(Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("follow")) {
                            followUser(true, getAdapterPosition());
                            Items.get(getAdapterPosition()).put(Constants.TAG_STATUS, "unfollow");
                        } else {
                            followUser(false, getAdapterPosition());
                            Items.get(getAdapterPosition()).put(Constants.TAG_STATUS, "follow");
                        }
                        notifyDataSetChanged();
                        //   helper.updateItemDetails(Items.get(getAdapterPosition()).get(Constants.TAG_ID), Constants.TAG_LIKED, Items.get(getAdapterPosition()).get(Constants.TAG_LIKED));
                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return Items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.followers_list_items, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                HashMap<String, String> tempMap = Items.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;
                viewHolder.fullName.setText(tempMap.get(Constants.TAG_FULL_NAME));
                viewHolder.userName.setText("@" + tempMap.get(Constants.TAG_USER_NAME));

                String image = tempMap.get(Constants.TAG_USER_IMAGE);
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(viewHolder.userImage);
                }

                String status = tempMap.get(Constants.TAG_STATUS);
                if (GetSet.isLogged() && tempMap.get(Constants.TAG_USER_ID).equals(GetSet.getUserId())) {
                    viewHolder.followBtn.setVisibility(View.GONE);
                } else {
                    viewHolder.followBtn.setVisibility(View.VISIBLE);
                    if (status.equalsIgnoreCase("follow")) {
                        viewHolder.followBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.primary_color_sharp_corner));
                        viewHolder.followBtn.setImageResource(R.drawable.user_unfollow);
                    } else {
                        viewHolder.followBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        viewHolder.followBtn.setImageResource(R.drawable.user_follow);
                    }
                }
            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }
    }

    private void getAllStores(final int offset) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ALL_STORES, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);

                    if (followersAry.size() >= Constants.OVERALL_LIMIT && followersAry.get(followersAry.size() - 1) == null) {
                        followersAry.remove(followersAry.size() - 1);
                        allstoreadapter.notifyItemRemoved(followersAry.size());
                    }
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        followersAry.clear();// This hides the spinner
                    }

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray result = json.getJSONArray(Constants.TAG_RESULT);
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject temp = result.getJSONObject(i);

                            HashMap<String, String> map = new HashMap<String, String>();
                            String store_id = DefensiveClass.optString(temp, Constants.TAG_STORE_ID);
                            String store_name = DefensiveClass.optString(temp, Constants.TAG_STORE_NAME);
                            String wifi = DefensiveClass.optString(temp, Constants.TAG_WIFI);
                            String merchant_name = DefensiveClass.optString(temp, Constants.TAG_MERCHANT_NAME);
                            String follow_status = DefensiveClass.optString(temp, Constants.TAG_STATUS);
                            String image = DefensiveClass.optString(temp, Constants.TAG_IMAGE);

                            map.put(Constants.TAG_STORE_ID, store_id);
                            map.put(Constants.TAG_STORE_NAME, store_name);
                            map.put(Constants.TAG_WIFI, wifi);
                            map.put(Constants.TAG_MERCHANT_NAME, merchant_name);
                            map.put(Constants.TAG_STATUS, follow_status);
                            map.put(Constants.TAG_IMAGE, image);

                            followersAry.add(map);
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(false);
                        }
                    }
                    if (followersAry.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_stores);
                        nullText.setText(getString(R.string.no_stores));
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                    progressLay.setVisibility(View.GONE);
                    allstoreadapter.notifyDataSetChanged();
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
                Log.e(TAG, "getAllStoresError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                WelcomeScreen.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (followersAry.size() >= Constants.OVERALL_LIMIT) {
                            followersAry.add(null);
                            allstoreadapter.notifyItemInserted(followersAry.size() - 1);
                        } else if (followersAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                            progressLay.setVisibility(View.VISIBLE);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();
                if (GetSet.isLogged()) {
                    map.put("user_id", GetSet.getUserId());
                }
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("search_key", searchKey);
                map.put("offset", Integer.toString(offset));
                Log.i(TAG, "getAllStoresParams= " + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void setErrorLayout() {
        if (followersAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
        }
    }


    private void followStore(final Boolean follow, final int position) {

        String CallingAPI;
        if (follow) // Follow Api
            CallingAPI = Constants.API_FOLLOW_STORE;
        else  // Unfollow Api
            CallingAPI = Constants.API_UNFOLLOW_STORE;

        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.i(TAG, "followStoreRes= " + res);
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        if (follow) {
                            followersAry.get(position).put(Constants.TAG_STATUS, "follow");
                        } else {
                            followersAry.get(position).put(Constants.TAG_STATUS, "unfollow");
                        }
                        allstoreadapter.notifyDataSetChanged();
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
                Log.e(TAG, "followStoreError: " + error.getMessage());
                if (follow) {
                    followersAry.get(position).put(Constants.TAG_STATUS, "follow");
                } else {
                    followersAry.get(position).put(Constants.TAG_STATUS, "unfollow");
                }
                allstoreadapter.notifyDataSetChanged();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("store_id", followersAry.get(position).get(Constants.TAG_STORE_ID));
                Log.i(TAG, "followStoreParams: " + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    public class AllStoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;
        DatabaseHandler helper;

        public AllStoreAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
            this.helper = DatabaseHandler.getInstance(context);
        }

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView userImage, followBtn;
            TextView fullName, userName;

            public MyViewHolder(View view) {
                super(view);

                userImage = (ImageView) view.findViewById(R.id.userImage);
                fullName = (TextView) view.findViewById(R.id.fullName);
                userName = (TextView) view.findViewById(R.id.userName);
                followBtn = (ImageView) view.findViewById(R.id.followBtn);
                followBtn.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.followBtn:
                        String status = Items.get(getAdapterPosition()).get(Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("follow")) {
                            followStore(true, getAdapterPosition());
                            Items.get(getAdapterPosition()).put(Constants.TAG_STATUS, "unfollow");
                        } else {
                            followStore(false, getAdapterPosition());
                            Items.get(getAdapterPosition()).put(Constants.TAG_STATUS, "follow");
                        }
                        notifyDataSetChanged();
                        //   helper.updateItemDetails(Items.get(getAdapterPosition()).get(Constants.TAG_ID), Constants.TAG_LIKED, Items.get(getAdapterPosition()).get(Constants.TAG_LIKED));
                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return Items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.followers_list_items, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                HashMap<String, String> tempMap = Items.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;
                viewHolder.fullName.setText(tempMap.get(Constants.TAG_STORE_NAME));
                viewHolder.userName.setText("@" + tempMap.get(Constants.TAG_MERCHANT_NAME));

                String image = tempMap.get(Constants.TAG_IMAGE);
                if (image != null && image != null && !image.equals("")) {
                    Picasso.get().load(image).into(viewHolder.userImage);
                }
                String status = tempMap.get(Constants.TAG_STATUS);
                viewHolder.followBtn.setVisibility(View.VISIBLE);
                if (status.equalsIgnoreCase("follow")) {
                    viewHolder.followBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.primary_color_sharp_corner));
                    viewHolder.followBtn.setImageResource(R.drawable.store_unfollow);
                } else {
                    viewHolder.followBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    viewHolder.followBtn.setImageResource(R.drawable.store_follow);
                }

            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }
    }

}
