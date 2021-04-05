package com.futureskyltd.app.fantacy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.GridRecyclerOnScrollListener;
import com.futureskyltd.app.helper.DatabaseHandler;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.futureskyltd.app.utils.ItemsParsing;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 15/6/17.
 */

public class ViewCollection extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, cartBtn, searchBtn, delete, edit;
    TextView title, itemsCount, collectionName;
    RecyclerView recyclerView;
    Display display;
    int itemWidth, itemHeight;
    private GridRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    RelativeLayout progressLay, nullLay;
    ImageView nullImage;
    TextView nullText;
    String id = "", collection_id, from = "";
    RelativeLayout collectionLay;
    RecyclerViewAdapter itemAdapter;
    GridLayoutManager itemManager;
    ArrayList<HashMap<String, String>> itemsAry = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_collection_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        cartBtn = (ImageView) findViewById(R.id.cartBtn);
        searchBtn = (ImageView) findViewById(R.id.searchBtn);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        collectionLay = (RelativeLayout) findViewById(R.id.collectionLay);
        collectionName = (TextView) findViewById(R.id.collectionName);
        itemsCount = (TextView) findViewById(R.id.itemsCount);
        delete = (ImageView) findViewById(R.id.delete);
        edit = (ImageView) findViewById(R.id.edit);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        nullImage = (ImageView) findViewById(R.id.nullImage);
        nullText = (TextView) findViewById(R.id.nullText);
        progressLay = (RelativeLayout) findViewById(R.id.progress);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(ViewCollection.this, R.color.progresscolor));
        id = getIntent().getExtras().getString("id");
        from = getIntent().getExtras().getString("from");

        title.setText(getString(R.string.collection));

        FragmentMainActivity.setCartBadge(findViewById(R.id.parentLay), ViewCollection.this);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        cartBtn.setVisibility(View.VISIBLE);
        searchBtn.setVisibility(View.VISIBLE);
        collectionLay.setVisibility(View.GONE);

        backBtn.setOnClickListener(this);
        edit.setOnClickListener(this);
        delete.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        cartBtn.setOnClickListener(this);

        if (from.equals("profile")) {
            edit.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        } else {
            edit.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        }

        display = getWindowManager().getDefaultDisplay();

        itemWidth = (display.getWidth() * 50 / 100) - FantacyApplication.dpToPx(this, 1);
        itemHeight = display.getWidth() * 60 / 100;

        recyclerView.setHasFixedSize(true);
        itemManager = new GridLayoutManager(ViewCollection.this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        itemAdapter = new RecyclerViewAdapter(ViewCollection.this, itemsAry);
        recyclerView.setAdapter(itemAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                itemsAry.clear();
                getAllItems(0);
            }
        });
        mScrollListener = new GridRecyclerOnScrollListener(itemManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getAllItems(current_page * Constants.OVERALL_LIMIT);
                    Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        itemManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (itemAdapter.getItemViewType(position) == 0) ? 1 : 2;
            }

        });

        getAllItems(0);

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getAllItems(final int offset) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_VIEW_COLLECTION, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    Log.v(TAG,"getAllItemsRes=" + res);
                    progressLay.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setEnabled(true);
                    if (itemsAry.size() >= Constants.OVERALL_LIMIT && itemsAry.get(itemsAry.size() - 1) == null) {
                        itemsAry.remove(itemsAry.size() - 1);
                        itemAdapter.notifyItemRemoved(itemsAry.size());
                    }
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        itemsAry.clear();
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);

                        collection_id = DefensiveClass.optString(result, Constants.TAG_COLLECTION_ID);
                        String collection_name = DefensiveClass.optString(result, Constants.TAG_COLLECTION_NAME);
                        String type = DefensiveClass.optString(result, Constants.TAG_TYPE);
                        String total_items = DefensiveClass.optString(result, Constants.TAG_TOTAL_ITEMS);

                        collectionLay.setVisibility(View.VISIBLE);
                        collectionName.setText(collection_name);
                        itemsCount.setText(total_items + " " + getString(R.string.items));

                        JSONArray items = result.getJSONArray(Constants.TAG_ITEMS);
                        ItemsParsing allItems = new ItemsParsing(ViewCollection.this);
                        itemsAry.addAll(allItems.getItems(items));

                        if (mScrollListener != null && itemsAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    }
                    itemAdapter.notifyDataSetChanged();
                    if (itemsAry.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_list);
                        nullText.setText(getString(R.string.no_item_list));
                        nullLay.setVisibility(View.VISIBLE);
                        itemsCount.setText("0 " + getString(R.string.items));
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
                Log.e(TAG, "getAllItemsError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                ViewCollection.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (itemsAry.size() >= Constants.OVERALL_LIMIT) {
                            itemsAry.add(null);
                            itemAdapter.notifyItemInserted(itemsAry.size() - 1);
                        } else if (itemsAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
                            progressLay.setVisibility(View.VISIBLE);
                            mSwipeRefreshLayout.setEnabled(false);
                        }
                        if (mScrollListener != null) {
                            mScrollListener.setLoading(true);
                        }
                    }
                });

                Map<String, String> map = new HashMap<String, String>();
                if (GetSet.isLogged()) {
                    map.put("user_id", GetSet.getUserId());
                }
                map.put("collection_id", id);
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                Log.v(TAG, "getAllItemsParams=" + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void setErrorLayout() {
        if (itemsAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
            nullLay.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setEnabled(true);
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;
        DatabaseHandler helper;
        int padding;

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            ProgressBar progressBar;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView itemName, itemPrice, discountPrice;
            ImageView itemImage, likedBtn;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                itemName = (TextView) view.findViewById(R.id.itemName);
                itemPrice = (TextView) view.findViewById(R.id.itemPrice);
                itemImage = (ImageView) view.findViewById(R.id.itemImage);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);
                likedBtn = (ImageView) view.findViewById(R.id.likedBtn);
                discountPrice = (TextView) view.findViewById(R.id.discountPrice);

                discountPrice.setPaintFlags(discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                likedBtn.setOnClickListener(this);
                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.likedBtn:
                        if (GetSet.isLogged()) {
                            updateCollection("0", Items.get(getAdapterPosition()).get(Constants.TAG_ID));
                            Items.remove(getAdapterPosition());
                            notifyItemRemoved(getAdapterPosition());
                        } else {
                            Intent login = new Intent(context, LoginActivity.class);
                            startActivity(login);
                        }
                        break;
                    case R.id.mainLay:
                        Intent i = new Intent(ViewCollection.this, DetailActivity.class);
                        i.putExtra("items", itemsAry);
                        i.putExtra("position", getAdapterPosition());
                        startActivity(i);
                        break;
                }
            }
        }

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
            this.helper = DatabaseHandler.getInstance(context);
            padding = FantacyApplication.dpToPx(context, 15);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items, parent, false);
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

                holder.itemName.setText(tempMap.get(Constants.TAG_ITEM_TITLE));
                holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));

                holder.mainLay.getLayoutParams().width = itemWidth;
                holder.itemImage.getLayoutParams().height = itemWidth;

                String image = tempMap.get(Constants.TAG_IMAGE);
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(holder.itemImage);
                }

                if (from.equals("profile")) {
                    holder.likedBtn.setPadding(padding, padding, padding, padding);
                    holder.likedBtn.setImageResource(R.drawable.cancel);
                    holder.likedBtn.setVisibility(View.VISIBLE);
                } else {
                    holder.likedBtn.setVisibility(View.GONE);
                }

                if (tempMap.get(Constants.TAG_DEAL_ENABLED).equalsIgnoreCase("yes") && FantacyApplication.isDealEnabled(tempMap.get(Constants.TAG_VALID_TILL))) {
                    holder.discountPrice.setVisibility(View.VISIBLE);
                    String costValue = tempMap.get(Constants.TAG_PRICE);
                    float priceValue = Float.parseFloat(costValue) - (Float.parseFloat(costValue)
                            * ((Float.parseFloat(tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE))) / 100.0f));

                    holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + FantacyApplication.decimal.format(priceValue));
                    holder.discountPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
                } else {
                    holder.discountPrice.setVisibility(View.GONE);
                    holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY) + " " + tempMap.get(Constants.TAG_PRICE));
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
    }

    private void editCollection() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.create_collection_dialog);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, display.getHeight() * 40 / 100);
        dialog.setCancelable(false);

        ImageView cancel = (ImageView) dialog.findViewById(R.id.cancel);
        final EditText editText = (EditText) dialog.findViewById(R.id.editText);
        TextView create = (TextView) dialog.findViewById(R.id.create);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView sub_title = (TextView) dialog.findViewById(R.id.sub_title);

        title.setText(R.string.edit_your_collection);
        sub_title.setText(R.string.change_collection_name);
        create.setText(R.string.save);
        final String oldstring = collectionName.getText().toString();
        editText.setText(oldstring);
        editText.setSelection(oldstring.length());

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!oldstring.equals(editText.getText().toString())) {
                    dialog.dismiss();
                    saveCollection(editText.getText().toString());
                } else {
                    dialog.dismiss();
                }
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void saveCollection(final String Name) {
        final ProgressDialog dialog = new ProgressDialog(ViewCollection.this);
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
                    Log.v(TAG, "saveCollectionRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        FantacyApplication.showToast(ViewCollection.this, getString(R.string.collection_success), Toast.LENGTH_SHORT);
                        collectionName.setText(Name);
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
                Log.e(TAG, "saveCollectionError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("collection_id", collection_id);
                map.put("collection_name", Name);
                Log.v(TAG, "saveCollectionParams=" + map);
                return map;
            }
        };
        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void DeleteCollection() {
        final ProgressDialog dialog = new ProgressDialog(ViewCollection.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_DELTETE_COLLECTION, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    Log.v(TAG, "DeleteCollectionRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        Collections.refreshCollections = true;
                        finish();
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
                Log.e(TAG, "DeleteCollectionError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("collection_id", collection_id);
                Log.v(TAG, "DeleteCollectionParams=" + map);
                return map;
            }
        };
        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void deleteCollection() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView yes = (TextView) dialog.findViewById(R.id.yes);
        TextView no = (TextView) dialog.findViewById(R.id.no);

        title.setText(R.string.delete_collection_msg);
        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteCollection();
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

    private void updateCollection(final String checked, final String itemId) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_UPDATE_COLLECTIONS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "updateCollectionRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (!status.equalsIgnoreCase("true")) {

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
                Log.e(TAG, "updateCollectionError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("item_id", itemId);
                map.put("collection_id", collection_id);
                map.put("checked", checked);
                Log.v(TAG, "updateCollectionParams=" + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fragment_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return FragmentMainActivity.onNavOptionSelected(this, id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (itemAdapter != null) {
            itemAdapter.notifyDataSetChanged();
        }
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Collections.refreshCollections = true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                Collections.refreshCollections = true;
                finish();
                break;
            case R.id.delete:
                deleteCollection();
                break;
            case R.id.edit:
                editCollection();
                break;
            case R.id.searchBtn:
                Intent s = new Intent(this, RecentSearch.class);
                startActivity(s);
                break;
            case R.id.cartBtn:
                if (GetSet.isLogged()) {
                    Intent c = new Intent(this, CartActivity.class);
                    c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    c.putExtra("shippingId", "0");
                    c.putExtra("itemId", "0");
                    startActivity(c);
                } else {
                    Intent login = new Intent(this, LoginActivity.class);
                    startActivity(login);
                }
                break;
        }
    }
}
