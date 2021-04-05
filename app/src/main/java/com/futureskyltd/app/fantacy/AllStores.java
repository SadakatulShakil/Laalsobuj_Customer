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
 * Created by hitasoft on 18/7/17.
 */

public class AllStores extends Fragment {

    private static final String TAG = AllStores.class.getSimpleName();
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    RelativeLayout progressLay, nullLay;
    ImageView nullImage;
    TextView nullText;
    EditText searchView;
    ImageView clearBtn;
    String searchKey = "";
    ArrayList<HashMap<String, String>> followersAry = new ArrayList<HashMap<String, String>>();
    DatabaseHandler helper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.liked_main_layout, container, false);

        ((FragmentChangeListener) getActivity()).setNavSelectionItem(R.id.allStores, "AllStores");

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.progresscolor));

        nullLay = (RelativeLayout) view.findViewById(R.id.nullLay);
        nullImage = (ImageView) view.findViewById(R.id.nullImage);
        nullText = (TextView) view.findViewById(R.id.nullText);
        progressLay = (RelativeLayout) view.findViewById(R.id.progress);
        searchView = (EditText) getActivity().findViewById(R.id.searchView);
        clearBtn = (ImageView) getActivity().findViewById(R.id.clearBtn);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        helper = DatabaseHandler.getInstance(getActivity());

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(getActivity(), linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), followersAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        followersAry.clear();

        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchView.clearFocus();
                    searchKey = searchView.getText().toString();
                    FantacyApplication.hideSoftKeyboard(getActivity(), textView);
                    followersAry.clear();
                    getAllstore(0);
                    return true;
                }
                return false;
            }
        });

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

        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getAllstore(current_page * Constants.OVERALL_LIMIT);
                    Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setText(" ");
                searchKey = "";//Remove Previous Search Text
                clearBtn.setVisibility(View.GONE);
                followersAry.clear();
                mScrollListener.resetpagecount();
                getAllstore(0);
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                getAllstore(0);
            }
        });

        getAllstore(0);

        FantacyApplication.setupUI(getActivity(), getView().findViewById(R.id.likedMainLay));
    }

    private void getAllstore(final int offset) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ALL_STORES, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.i(TAG, "getAllStoreRes=" + res);
                    mSwipeRefreshLayout.setEnabled(true);
                    JSONObject json = new JSONObject(res);

                    if (followersAry.size() >= Constants.OVERALL_LIMIT && followersAry.get(followersAry.size() - 1) == null) {
                        followersAry.remove(followersAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(followersAry.size());
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

                            helper.addStoreDetails(store_id, follow_status);
                        }
                        if (mScrollListener != null && followersAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           /* Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                            /*Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
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
                Log.e(TAG, "getAllStoreError: " + error.getMessage());
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
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("search_key", searchKey.trim());
                map.put("offset", Integer.toString(offset));
                Log.i(TAG, "getAllStoreParams: " + map);
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
        if (followersAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
        }
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setEnabled(true);
    }

    private void followStore(final Boolean follow, final int position) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        final String CallingAPI;
        if (follow) // Follow Api
            CallingAPI = Constants.API_FOLLOW_STORE;
        else  // Unfollow Api
            CallingAPI = Constants.API_UNFOLLOW_STORE;

        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.i(TAG, "followStoreURL: " + CallingAPI);
                    Log.v(TAG, "followStoreRes=" + res);
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {
                        if (follow) {
                            followersAry.get(position).put(Constants.TAG_STATUS, "follow");
                            helper.updateStoreDetails(followersAry.get(position).get(Constants.TAG_STORE_ID), "follow");
                        } else {
                            followersAry.get(position).put(Constants.TAG_STATUS, "unfollow");
                            helper.updateStoreDetails(followersAry.get(position).get(Constants.TAG_STORE_ID), "unfollow");
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
                Log.e(TAG, "followStoreError: " + error.getMessage());
                if (follow) {
                    followersAry.get(position).put(Constants.TAG_STATUS, "follow");
                    helper.updateStoreDetails(followersAry.get(position).get(Constants.TAG_STORE_ID), "follow");
                } else {
                    followersAry.get(position).put(Constants.TAG_STATUS, "unfollow");
                    helper.updateStoreDetails(followersAry.get(position).get(Constants.TAG_STORE_ID), "unfollow");
                }
                recyclerViewAdapter.notifyDataSetChanged();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("store_id", followersAry.get(position).get(Constants.TAG_STORE_ID));
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
                SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                String accesstoken = preferences.getString("TOKEN", null);
                switch (v.getId()) {
                    case R.id.userImage:
                    case R.id.mainLay:
                        Intent i = new Intent(getActivity(), StoreProfile.class);
                        i.putExtra("storeId", Items.get(getAdapterPosition()).get(Constants.TAG_STORE_ID));
                        startActivity(i);
                        break;
                    case R.id.followBtn:
                        if (accesstoken != null) {
                            String status = helper.getStoreDetails(Items.get(getAdapterPosition()).get(Constants.TAG_STORE_ID));
                            if (status.equalsIgnoreCase("follow")) {
                                followStore(true, getAdapterPosition());
                                Items.get(getAdapterPosition()).put(Constants.TAG_STATUS, "unfollow");
                                helper.updateStoreDetails(Items.get(getAdapterPosition()).get(Constants.TAG_STORE_ID), "unfollow");
                            } else {
                                followStore(false, getAdapterPosition());
                                Items.get(getAdapterPosition()).put(Constants.TAG_STATUS, "follow");
                                helper.updateStoreDetails(Items.get(getAdapterPosition()).get(Constants.TAG_STORE_ID), "follow");
                            }
                            notifyDataSetChanged();
                        } else {
                            Intent login = new Intent(context, SignInActivity.class);
                            startActivity(login);
                        }
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
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(viewHolder.userImage);
                }
                String status = helper.getStoreDetails(tempMap.get(Constants.TAG_STORE_ID));
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

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }
}