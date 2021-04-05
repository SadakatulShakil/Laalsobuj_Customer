package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

/**
 * Created by hitasoft on 19/6/17.
 */

public class Followers extends Fragment {

    private static final String TAG = Followers.class.getSimpleName();
    private String from = "", userId = "";
    ImageView nullImage;
    TextView nullText;
    RelativeLayout nullLay;
    RecyclerView recyclerView;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    DatabaseHandler helper;
    ArrayList<HashMap<String, String>> followersAry = new ArrayList<HashMap<String, String>>();

    public static Followers newInstance(String from, String userId) {
        Followers fragment = new Followers();
        Bundle args = new Bundle();
        args.putString("from", from);
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.liked_main_layout, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        nullLay = (RelativeLayout) view.findViewById(R.id.nullLay);
        nullImage = (ImageView) view.findViewById(R.id.nullImage);
        nullText = (TextView) view.findViewById(R.id.nullText);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));

        if (getArguments() != null) {
            from = getArguments().getString("from");
            userId = getArguments().getString("userId");
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

        helper = DatabaseHandler.getInstance(getActivity());

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(getActivity(),
                linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), followersAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getFollowers(current_page * Constants.OVERALL_LIMIT);
                    Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                followersAry.clear();
                getFollowers(0);
            }
        });

        followersAry.clear();
        getFollowers(0);
    }

    private void getFollowers(final int offset) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        String CallingAPI;
        if (from.equals("profile")) // Profile followers Api
            CallingAPI = Constants.API_FOLLOWERS;
        else  // Store followers Api
            CallingAPI = Constants.API_STORE_FOLLOWERS;

        Log.i(TAG, "getFollowersURL: " + CallingAPI);
        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getFollowersRes=" + res);
                    JSONObject json = new JSONObject(res);
                    if (followersAry.size() >= Constants.OVERALL_LIMIT && followersAry.get(followersAry.size() - 1) == null) {
                        followersAry.remove(followersAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(followersAry.size());
                    }

                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                        followersAry.clear();
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

                            followersAry.add(map);

                            helper.addUserDetails(user_id, follow_status);
                        }

                        if (mScrollListener != null && followersAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    }

                    if (followersAry.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                        nullImage.setImageResource(R.drawable.no_friends);
                        nullText.setText(getString(R.string.no_followers));
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
                Log.e(TAG, "getFollowersError: " + error.getMessage());
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
                        if (followersAry.size() >= Constants.OVERALL_LIMIT) {
                            followersAry.add(null);
                            recyclerViewAdapter.notifyItemInserted(followersAry.size() - 1);
                        } else if (followersAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                            swipeRefresh();
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();
                if (from.equals("profile")) {
                    map.put("user_id", userId);
                    if (accesstoken != null) {
                        map.put("logged_user_id", "289");
                    }
                } else {
                    map.put("store_id", userId);
                    if (accesstoken != null) {
                        map.put("logged_user_id", "289");
                    } /*else {
                        map.put("user_id", "");
                    }*/
                }
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                Log.v(TAG, "getFollowersParams=" + map);
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
        if (followersAry.size() == 0) {
            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
            }
            nullLay.setVisibility(View.VISIBLE);
        }
    }

    private void followUser(final Boolean follow, final int position) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        final String CallingAPI;
        if (follow) // Follow Api
            CallingAPI = Constants.API_FOLLOW_USER;
        else  // Unfollow Api
            CallingAPI = Constants.API_UNFOLLOW_USER;
        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                Log.i(TAG, "followUserRes: " + res + " " + followersAry.get(position).get(Constants.TAG_USER_ID) + " " + position + " " + follow);
                try {
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    /*Check Failure case. Success case already updated in local db and adapter*/
                    if (!status.equalsIgnoreCase("true")) {
                        if (follow) {
                            followersAry.get(position).put(Constants.TAG_STATUS, "follow");
                            helper.updateUserDetails(followersAry.get(position).get(Constants.TAG_USER_ID), "follow");
                        } else {
                            followersAry.get(position).put(Constants.TAG_STATUS, "unfollow");
                            helper.updateUserDetails(followersAry.get(position).get(Constants.TAG_USER_ID), "unfollow");
                        }
                        recyclerViewAdapter.notifyItemChanged(position);
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
                Log.e(TAG, "getFollowersError: " + error.getMessage());
                if (follow) {
                    followersAry.get(position).put(Constants.TAG_STATUS, "follow");
                    helper.updateUserDetails(followersAry.get(position).get(Constants.TAG_USER_ID), "follow");
                } else {
                    followersAry.get(position).put(Constants.TAG_STATUS, "unfollow");
                    helper.updateUserDetails(followersAry.get(position).get(Constants.TAG_USER_ID), "unfollow");
                }
                recyclerViewAdapter.notifyItemChanged(position);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                switch (from) {
                    case "profile":
                    case "store":
                        map.put("user_id", "289");
                        break;
                    default:
                        map.put("user_id", userId);
                        break;
                }
                map.put("follow_id", followersAry.get(position).get(Constants.TAG_USER_ID));
                Log.i(TAG, "followUserParams: " + map + " " + follow + " " + CallingAPI);
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
            LinearLayout contentLay;

            public MyViewHolder(View view) {
                super(view);

                userImage = (ImageView) view.findViewById(R.id.userImage);
                fullName = (TextView) view.findViewById(R.id.fullName);
                userName = (TextView) view.findViewById(R.id.userName);
                followBtn = (ImageView) view.findViewById(R.id.followBtn);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);
                contentLay = (LinearLayout) view.findViewById(R.id.contentLay);

                userImage.setOnClickListener(this);
                contentLay.setOnClickListener(this);
//                mainLay.setOnClickListener(this);
                followBtn.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                String accesstoken = preferences.getString("TOKEN", null);

                switch (v.getId()) {
                    case R.id.userImage:
                    case R.id.contentLay:
//                    case R.id.mainLay:
                        Intent p = new Intent(getActivity(), Profile.class);
                        p.putExtra("userId", Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
                        startActivity(p);
                        break;
                    case R.id.followBtn:
                        if (accesstoken != null) {
                            String status = helper.getUserDetails(Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
                            if (status.equalsIgnoreCase("follow")) {
                                Items.get(getAdapterPosition()).put(Constants.TAG_STATUS, "unfollow");
                                helper.updateUserDetails(followersAry.get(getAdapterPosition()).get(Constants.TAG_USER_ID), "unfollow");
                                followUser(true, getAdapterPosition());
                            } else {
                                Items.get(getAdapterPosition()).put(Constants.TAG_STATUS, "follow");
                                helper.updateUserDetails(followersAry.get(getAdapterPosition()).get(Constants.TAG_USER_ID), "follow");
                                followUser(false, getAdapterPosition());
                            }
                            notifyItemChanged(getAdapterPosition());
                        } else {
                            Intent login = new Intent(context, LoginActivity.class);
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
                MyViewHolder viewHolder = (MyViewHolder) holder;
                viewHolder.fullName.setText(tempMap.get(Constants.TAG_FULL_NAME));
                viewHolder.userName.setText("@" + tempMap.get(Constants.TAG_USER_NAME));

                String image = tempMap.get(Constants.TAG_USER_IMAGE);
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(viewHolder.userImage);
                }

                if (GetSet.isLogged() && tempMap.get(Constants.TAG_USER_ID).equals(GetSet.getUserId())) {
                    viewHolder.followBtn.setVisibility(View.GONE);
                } else {
                    String status = helper.getUserDetails(tempMap.get(Constants.TAG_USER_ID));
                    viewHolder.followBtn.setVisibility(View.VISIBLE);
                    if (status.equalsIgnoreCase("follow")) {
                        viewHolder.followBtn.setImageResource(R.drawable.user_unfollow);
                        viewHolder.followBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.primary_color_sharp_corner));
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

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

}
