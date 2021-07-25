package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import com.futureskyltd.app.helper.DatabaseHandler;
import com.futureskyltd.app.helper.NetworkReceiver;
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
 * Created by hitasoft on 10/7/17.
 */

public class FindFriends extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener,
        NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = FindFriends.class.getSimpleName();
    ImageView backBtn, nullImage;
    TextView title, nullText;
    EditText searchView;
    RelativeLayout progressLay, nullLay;
    RecyclerView recyclerView;
    String searchKey = "";
    FrameLayout searchLay;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    String from, feedId;
    ArrayList<HashMap<String, String>> friendsAry = new ArrayList<HashMap<String, String>>();
    private ImageView clearBtn;
    DatabaseHandler helper;
    String accesstoken, customerId, customerName;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_friends);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);
        customerId = preferences.getString("customer_id", null);
        customerName = preferences.getString("customer_name",null);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        searchView = (EditText) findViewById(R.id.searchView);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        nullImage = (ImageView) findViewById(R.id.nullImage);
        searchLay = (FrameLayout) findViewById(R.id.searchLay);
        nullText = (TextView) findViewById(R.id.nullText);
        progressLay = (RelativeLayout) findViewById(R.id.progress);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        clearBtn = (ImageView) findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(this);
        from = (String) getIntent().getExtras().get("to");
        title.setText(getString(R.string.find_friends));
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.progresscolor));

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count != 0) {
                    clearBtn.setVisibility(View.VISIBLE);
                } else {
                    clearBtn.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backBtn.setOnClickListener(this);
        if (from.equals("feedlikes")) {
            feedId = (String) getIntent().getExtras().get("feedId");
            searchView.setVisibility(View.GONE);
            searchLay.setVisibility(View.GONE);
            title.setText(R.string.liked_users);
        }

        searchView.setOnEditorActionListener(this);
        helper = DatabaseHandler.getInstance(this);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(this,
                linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        recyclerView.addItemDecoration(itemDivider);
        recyclerView.setHasFixedSize(true);

        recyclerViewAdapter = new RecyclerViewAdapter(this, friendsAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchView.clearFocus();
                    searchKey = searchView.getText().toString();
                    FantacyApplication.hideSoftKeyboard(FindFriends.this, textView);
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

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getFriends(final int offset) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        final String CallingAPI;
        if (from.equals("feedlikes")) {
            CallingAPI = Constants.API_LIKED_USERS;
        } else {
            CallingAPI = Constants.API_FIND_FRIENDS;
        }
        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.i(TAG, "getFriendsURL: " + CallingAPI);
                    Log.v(TAG, "getFriendsRes=" + res);
                    mSwipeRefreshLayout.setEnabled(true);
                    JSONObject json = new JSONObject(res);
                    if (friendsAry.size() >= Constants.OVERALL_LIMIT && friendsAry.get(friendsAry.size() - 1) == null) {
                        friendsAry.remove(friendsAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(friendsAry.size());
                    }

                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                        friendsAry.clear();
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

                            helper.addUserDetails(user_id, follow_status);
                        }

                        if (mScrollListener != null && friendsAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           /* Intent i = new Intent(FindFriends.this, PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                            /*Intent i = new Intent(FindFriends.this, PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
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
                            mSwipeRefreshLayout.setEnabled(false);
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();

                if (accesstoken != null) {
                    map.put("user_id", customerId);
                }
                if (from.equals("feedlikes"))
                    map.put("feed_id", feedId);
                else
                    map.put("search_key", searchKey.trim());
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));

                Log.v(TAG, "getFriendsRes=" + map);
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

    private void followUser(final Boolean follow, final int position) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        final String CallingAPI;
        if (follow) // Follow Api
            CallingAPI = Constants.API_FOLLOW_USER;
        else  // Unfollow Api
            CallingAPI = Constants.API_UNFOLLOW_USER;

        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.i(TAG, "followUserURL: " + CallingAPI);
                    Log.i(TAG, "followUserRes= " + res);
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        if (follow) {
                            friendsAry.get(position).put(Constants.TAG_STATUS, "follow");
                            helper.updateUserDetails(friendsAry.get(position).get(Constants.TAG_USER_ID), "follow");
                        } else {
                            friendsAry.get(position).put(Constants.TAG_STATUS, "unfollow");
                            helper.updateUserDetails(friendsAry.get(position).get(Constants.TAG_USER_ID), "unfollow");
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
                    helper.updateUserDetails(friendsAry.get(position).get(Constants.TAG_USER_ID), "follow");
                } else {
                    friendsAry.get(position).put(Constants.TAG_STATUS, "unfollow");
                    helper.updateUserDetails(friendsAry.get(position).get(Constants.TAG_USER_ID), "unfollow");
                }
                recyclerViewAdapter.notifyDataSetChanged();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("follow_id", friendsAry.get(position).get(Constants.TAG_USER_ID));
                Log.i(TAG, "followUserParams: " + map);
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
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                userImage = (ImageView) view.findViewById(R.id.userImage);
                fullName = (TextView) view.findViewById(R.id.fullName);
                userName = (TextView) view.findViewById(R.id.userName);
                followBtn = (ImageView) view.findViewById(R.id.followBtn);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);

                userImage.setOnClickListener(this);
                followBtn.setOnClickListener(this);
                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.userImage:
                    case R.id.mainLay:
                        Intent p = new Intent(FindFriends.this, Profile.class);
                        p.putExtra("userId", Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
                        p.putExtra("follow_status", Items.get(getAdapterPosition()).get("status"));
                        Log.d(TAG, "onClick: "+ Items.get(getAdapterPosition()).get("status"));
                        //p.putExtra("userId", Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
                        startActivity(p);
                        break;
                    case R.id.followBtn:
                        if (accesstoken != null) {
                            String status = helper.getUserDetails(Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
                            if (status.equalsIgnoreCase("follow")) {
                                followUser(true, getAdapterPosition());
                                Items.get(getAdapterPosition()).put(Constants.TAG_STATUS, "unfollow");
                                helper.updateUserDetails(Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID), "unfollow");
                            } else {
                                followUser(false, getAdapterPosition());
                                Items.get(getAdapterPosition()).put(Constants.TAG_STATUS, "follow");
                                helper.updateUserDetails(Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID), "follow");
                            }
                            notifyDataSetChanged();
                        } else {
                            Intent login = new Intent(context, SignInActivity.class);
                            startActivity(login);
                        }
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
                Log.d(TAG, "onBindViewHolder: "+ tempMap.toString());
                MyViewHolder viewHolder = (MyViewHolder) holder;
                viewHolder.fullName.setText(tempMap.get(Constants.TAG_FULL_NAME));
                viewHolder.userName.setText("@" + tempMap.get(Constants.TAG_USER_NAME));

                String image = tempMap.get(Constants.TAG_USER_IMAGE);
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(viewHolder.userImage);
                }else{
                    Picasso.get().load(R.mipmap.appicon_round).into(viewHolder.userImage);
                }

                String status = helper.getUserDetails(tempMap.get(Constants.TAG_USER_ID));
                if (accesstoken != null && tempMap.get(Constants.TAG_USER_ID).equals(customerId)) {
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

    private void setErrorLayout() {
        if (friendsAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
        }
        mSwipeRefreshLayout.setEnabled(true);
    }

    /**
     * for click search icon from keyboard
     **/
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
            Log.i("TAG", "Enter pressed");
            try {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NetworkReceiver.isConnected()) {
            FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
        } else {
            FantacyApplication.showSnack(this, findViewById(R.id.parentLay), false);
        }
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.notifyDataSetChanged();
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
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.clearBtn:
                searchView.setText("");
                searchKey = "";//Remove Previous Search Text
                clearBtn.setVisibility(View.GONE);
                friendsAry.clear();
                mScrollListener.resetpagecount();
                getFriends(0);
                break;
        }

    }
}
