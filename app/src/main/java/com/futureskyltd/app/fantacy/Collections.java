package com.futureskyltd.app.fantacy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.CornerImageView;
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
 * Created by hitasoft on 15/6/17.
 */

public class Collections extends Fragment implements View.OnClickListener {

    private static final String TAG = Collections.class.getSimpleName();
    public static boolean refreshCollections = false;
    RecyclerView recyclerView;
    TextView create;
    int itemWidth;
    Display display;
    RelativeLayout progressLay, nullLay, parentLay;
    ImageView nullImage;
    TextView nullText;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<HashMap<String, ArrayList<HashMap<String, String>>>> collectionsAry = new ArrayList<HashMap<String, ArrayList<HashMap<String, String>>>>();
    private String from = "", userId = "";
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private Context context;

    public static Collections newInstance(String from, String userId) {
        Collections fragment = new Collections();
        Bundle args = new Bundle();
        args.putString("from", from);
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.collection_main_layout, container, false);
        Log.i(TAG, "Collections: onCreateView");

        if (getArguments() != null) {
            from = getArguments().getString("from");
            userId = getArguments().getString("userId");
        }
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(context, R.color.progresscolor));

        nullLay = (RelativeLayout) view.findViewById(R.id.nullLay);
        parentLay = (RelativeLayout) view.findViewById(R.id.parentLay);
        nullImage = (ImageView) view.findViewById(R.id.nullImage);
        nullText = (TextView) view.findViewById(R.id.nullText);
        progressLay = (RelativeLayout) view.findViewById(R.id.progress);
        create = (TextView) view.findViewById(R.id.create);

        if (from.equals("menu")) {
            ((FragmentChangeListener) getActivity()).setNavSelectionItem(R.id.collection, "Collection");
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) nullLay.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.topMargin = FantacyApplication.dpToPx(getActivity(), 80);
            nullLay.setLayoutParams(params);

            if (userId.equals(customerId)) {
                create.setVisibility(View.VISIBLE);
            } else {
                create.setVisibility(View.GONE);
            }
        }

        create.setOnClickListener(this);

        display = getActivity().getWindowManager().getDefaultDisplay();
        itemWidth = FantacyApplication.dpToPx(getActivity(), 100);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(context, linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.emptyspace));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), collectionsAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        collectionsAry.clear();

        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getCollectionData(current_page * Constants.OVERALL_LIMIT);
                    Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                getCollectionData(0);
            }
        });

        getCollectionData(0);
    }

    private void getCollectionData(final int offset) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_MY_COLLECTION, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getCollectionDataRes=" + res);
                    JSONObject json = new JSONObject(res);
                    progressLay.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setEnabled(true);
                    if (collectionsAry.size() >= Constants.OVERALL_LIMIT && collectionsAry.get(collectionsAry.size() - 1) == null) {
                        collectionsAry.remove(collectionsAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(collectionsAry.size());
                    }
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        collectionsAry.clear();
                        mSwipeRefreshLayout.setRefreshing(false);// This hides the spinner
                    }
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray result = json.getJSONArray(Constants.TAG_RESULT);

                        for (int i = 0; i < result.length(); i++) {
                            JSONObject temp = result.getJSONObject(i);

                            ArrayList<HashMap<String, String>> itemsAry = new ArrayList<HashMap<String, String>>();
                            ArrayList<HashMap<String, String>> colAry = new ArrayList<HashMap<String, String>>();

                            HashMap<String, ArrayList<HashMap<String, String>>> hashmap = new HashMap<String, ArrayList<HashMap<String, String>>>();

                            HashMap<String, String> map = new HashMap<>();

                            String collection_id = DefensiveClass.optString(temp, Constants.TAG_COLLECTION_ID);
                            String collection_name = DefensiveClass.optString(temp, Constants.TAG_COLLECTION_NAME);
                            String type = DefensiveClass.optString(temp, Constants.TAG_TYPE);
                            String total_items = DefensiveClass.optString(temp, Constants.TAG_TOTAL_ITEMS);

                            map.put(Constants.TAG_COLLECTION_ID, collection_id);
                            map.put(Constants.TAG_COLLECTION_NAME, collection_name);
                            map.put(Constants.TAG_TYPE, type);
                            map.put(Constants.TAG_TOTAL_ITEMS, total_items);

                            colAry.add(map);

                            JSONArray items = temp.getJSONArray(Constants.TAG_ITEMS);
                            ItemsParsing allItems = new ItemsParsing(getActivity());
                            itemsAry.addAll(allItems.getItems(items));

                            hashmap.put("data", colAry);
                            hashmap.put("items", itemsAry);
                            collectionsAry.add(hashmap);
                        }
                        if (mScrollListener != null && collectionsAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                            /*if (PaymentStatus.activity == null) {
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
                    if (collectionsAry.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_list);
                        nullText.setText(getString(R.string.no_collection));
                        nullLay.setVisibility(View.VISIBLE);
                        parentLay.setBackgroundColor(getActivity().getResources().getColor(R.color.white));
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
                Log.e(TAG, "getCollectionDataError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (collectionsAry.size() >= Constants.OVERALL_LIMIT) {
                            collectionsAry.add(null);
                            recyclerViewAdapter.notifyItemInserted(collectionsAry.size() - 1);
                        } else if (collectionsAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
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
                map.put("user_id", userId);
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                Log.v(TAG, "getCollectionDataParams=" + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void setErrorLayout() {
        if (collectionsAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
            nullLay.setVisibility(View.VISIBLE);
            parentLay.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            mSwipeRefreshLayout.setEnabled(true);
        }
    }

    private void createCollection() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.create_collection_dialog);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, display.getHeight() * 40 / 100);
        dialog.setCancelable(false);

        ImageView cancel = (ImageView) dialog.findViewById(R.id.cancel);
        final EditText editText = (EditText) dialog.findViewById(R.id.editText);
        TextView create = (TextView) dialog.findViewById(R.id.create);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().trim().length() == 0) {
                    editText.setError(getString(R.string.please_enter_collection_name));
                } else {
                    createCollection(editText.getText().toString());
                    editText.setText("");
                    dialog.dismiss();
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                }
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void createCollection(final String name) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CREATE_COLLECTION, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    Log.i(TAG, "createCollectionRes= " + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        swipeRefresh();
                        FantacyApplication.showToast(getActivity(), getString(R.string.collection_success), Toast.LENGTH_SHORT);
                        mScrollListener.resetpagecount();
                        getCollectionData(0);
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
                Log.e(TAG, "createCollectionError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                //    map.put("item_id", itemsAry.get(position).get(Constants.TAG_ID));
                map.put("collection_name", name);
                Log.i(TAG, "createCollectionParams: " + map);
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

    private void swipeRefresh() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (refreshCollections) {
            refreshCollections = false;
            swipeRefresh();
            mScrollListener.resetpagecount();
            getCollectionData(0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create:
                createCollection();
                break;
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, ArrayList<HashMap<String, String>>>> Items;
        Context context;
        DatabaseHandler helper;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, ArrayList<HashMap<String, String>>>> Items) {
            this.Items = Items;
            this.context = context;
            this.helper = DatabaseHandler.getInstance(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_list_item, parent, false);
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
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            try {
                if (viewHolder instanceof MyViewHolder) {
                    MyViewHolder holder = (MyViewHolder) viewHolder;
                    HashMap<String, ArrayList<HashMap<String, String>>> temp = Items.get(position);
                    ArrayList<HashMap<String, String>> griditems = temp.get("data");
                    HashMap<String, String> tempMap = griditems.get(0);
                    ArrayList<HashMap<String, String>> itemsArray = temp.get("items");

                    holder.collectionName.setText(tempMap.get(Constants.TAG_COLLECTION_NAME));
                    holder.itemsCount.setText(tempMap.get(Constants.TAG_TOTAL_ITEMS) + " " + getString(R.string.items));

                    if (FantacyApplication.isRTL(getActivity())) {
                        holder.collectionName.setGravity(Gravity.RIGHT);
                    } else {
                        holder.collectionName.setGravity(Gravity.LEFT);
                    }
                    if (itemsArray != null && itemsArray.size() != 0) {
                        holder.collectionList.setVisibility(View.VISIBLE);
                        holder.collectionList.setHasFixedSize(true);
                        holder.collectionList.getLayoutParams().height = FantacyApplication.dpToPx(context, 100);
                        LinearLayoutManager collectionManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                        holder.collectionList.setLayoutManager(collectionManager);

                        ItemViewAdapter collectionViewAdapter = new ItemViewAdapter("item", context, itemsArray);
                        holder.collectionList.setAdapter(collectionViewAdapter);
                    } else {
                        holder.collectionList.setVisibility(View.GONE);
                    }
                } else if (viewHolder instanceof LoadingViewHolder) {
                    LoadingViewHolder loadingViewHolder = (LoadingViewHolder) viewHolder;
                    loadingViewHolder.progressBar.setIndeterminate(true);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

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
            TextView collectionName, itemsCount, viewAll;
            RecyclerView collectionList;

            public MyViewHolder(View view) {
                super(view);

                itemImage = (ImageView) view.findViewById(R.id.itemImage);
                collectionName = (TextView) view.findViewById(R.id.collectionName);
                itemsCount = (TextView) view.findViewById(R.id.itemsCount);
                viewAll = (TextView) view.findViewById(R.id.viewAll);
                cancel = (ImageView) view.findViewById(R.id.cancel);
                collectionList = (RecyclerView) view.findViewById(R.id.collectionList);

                viewAll.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.viewAll:
                        Intent i = new Intent(getActivity(), ViewCollection.class);
                        if (userId.equals(customerId)) {
                            i.putExtra("from", "profile");
                        } else {
                            i.putExtra("from", "other");
                        }
                        i.putExtra("id", Items.get(getAdapterPosition()).get("data").get(0).get(Constants.TAG_COLLECTION_ID));
                        startActivity(i);
                        break;
                }
            }
        }
    }

    public class ItemViewAdapter extends RecyclerView.Adapter<ItemViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;
        String from = "";

        public ItemViewAdapter(String from, Context context, ArrayList<HashMap<String, String>> Items) {
            this.from = from;
            this.Items = Items;
            this.context = context;
        }

        public ArrayList<HashMap<String, String>> getItems() {
            return Items;
        }

        @Override
        public ItemViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.collections_item_image, parent, false);

            return new ItemViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ItemViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.image.getLayoutParams().width = itemWidth;
            holder.image.getLayoutParams().height = itemWidth;

            String image = tempMap.get(Constants.TAG_IMAGE);
            if (image != null && !image.equals("")) {
                Picasso.get().load(image).centerCrop().fit().into(holder.image);
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            CornerImageView image;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                image = (CornerImageView) view.findViewById(R.id.image);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);

                image.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.image:
                        Intent i = new Intent(getActivity(), DetailActivity.class);
                        i.putExtra("items", Items);
                        i.putExtra("position", getAdapterPosition());
                        startActivity(i);
                        break;
                }
            }
        }
    }
}
