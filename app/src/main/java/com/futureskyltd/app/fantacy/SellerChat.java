package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.ReverseRecyclerOnScrollListener;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.squareup.picasso.Picasso;

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
 * Created by hitasoft on 1/6/17.
 */

public class SellerChat extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, abItemImage, sendBtn;
    TextView abItemName, abShopName;
    RelativeLayout abItemLay;
    EditText chatEdit;
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    LinearLayoutManager linearLayoutManager;
    RelativeLayout progressLay, nullLay;
    ProgressBar loadmoreprogress;
    ImageView nullImage;
    TextView nullText;
    String chatid, from, itemID;
    ArrayList<HashMap<String, String>> backup = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> detailMap = new HashMap<String, String>();
    ArrayList<HashMap<String, String>> chatAry = new ArrayList<HashMap<String, String>>();
    Boolean firstload = true;
    private ReverseRecyclerOnScrollListener mScrollListener = null;

    public static String getTime(long timeStamp) {

        try {
            DateFormat sdf = new SimpleDateFormat("hh:mm a");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seller_chat_layout);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        abItemLay = (RelativeLayout) findViewById(R.id.abItemLay);
        abItemImage = (ImageView) findViewById(R.id.abItemImage);
        abItemName = (TextView) findViewById(R.id.abItemName);
        abShopName = (TextView) findViewById(R.id.abShopName);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        sendBtn = (ImageView) findViewById(R.id.sendBtn);
        chatEdit = (EditText) findViewById(R.id.chatEdit);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        nullImage = (ImageView) findViewById(R.id.nullImage);
        nullText = (TextView) findViewById(R.id.nullText);
        progressLay = (RelativeLayout) findViewById(R.id.progress);
        loadmoreprogress = (ProgressBar) findViewById(R.id.loadmoreprogress);
        backBtn.setVisibility(View.VISIBLE);
        abItemLay.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
        abItemLay.setOnClickListener(this);

        from = (String) getIntent().getExtras().get("from");
        chatid = (String) getIntent().getExtras().get("chatid");

        linearLayoutManager = new LinearLayoutManager(SellerChat.this, LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(this, chatAry);
        recyclerView.setAdapter(recyclerViewAdapter);


        getChats(0);

        mScrollListener = new ReverseRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                loadmoreprogress.setVisibility(View.VISIBLE);
                getChats(current_page * Constants.OVERALL_LIMIT);
                Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        //   FantacyApplication.setupUI(this, findViewById(R.id.parentLay));

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getChats(final int offset) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_VIEW_SELLER_MESSAGE, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {

                    Log.v(TAG, "getChatsRes=" + res);
                    JSONObject json = new JSONObject(res);
                    progressLay.setVisibility(View.GONE);
                    loadmoreprogress.setVisibility(View.GONE);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);

                        String chat_id = DefensiveClass.optString(result, Constants.TAG_CHAT_ID);
                        String item_title = DefensiveClass.optString(result, Constants.TAG_ITEM_TITLE);
                        String item_id = DefensiveClass.optString(result, Constants.TAG_ITEM_ID);
                        String image = DefensiveClass.optString(result, Constants.TAG_IMAGE);
                        String subject = DefensiveClass.optString(result, Constants.TAG_SUBJECT);
                        String shop_id = DefensiveClass.optString(result, Constants.TAG_SHOP_ID);
                        String shop_name = DefensiveClass.optString(result, Constants.TAG_SHOP_NAME);
                        String shop_image = DefensiveClass.optString(result, Constants.TAG_SHOP_IMAGE);
                        itemID = item_id;

                        detailMap.put(Constants.TAG_CHAT_ID, chat_id);
                        detailMap.put(Constants.TAG_ITEM_TITLE, item_title);
                        detailMap.put(Constants.TAG_ITEM_ID, item_id);
                        detailMap.put(Constants.TAG_IMAGE, image);
                        detailMap.put(Constants.TAG_SUBJECT, subject);
                        detailMap.put(Constants.TAG_SHOP_ID, shop_id);
                        detailMap.put(Constants.TAG_SHOP_NAME, shop_name);
                        detailMap.put(Constants.TAG_SHOP_IMAGE, shop_image);

                        JSONArray messages = result.optJSONArray(Constants.TAG_MESSAGES);
                        if (messages != null) {
                            ArrayList<HashMap<String, String>> tempAry = new ArrayList<HashMap<String, String>>();

                            for (int p = 0; p < messages.length(); p++) {
                                JSONObject pobj = messages.getJSONObject(p);

                                HashMap<String, String> pmap = new HashMap<>();

                                String message = DefensiveClass.optString(pobj, Constants.TAG_MESSAGE);
                                String user_name = DefensiveClass.optString(pobj, Constants.TAG_USER_NAME);
                                String full_name = DefensiveClass.optString(pobj, Constants.TAG_FULL_NAME);
                                String user_id = DefensiveClass.optString(pobj, Constants.TAG_USER_ID);
                                String user_image = DefensiveClass.optString(pobj, Constants.TAG_USER_IMAGE);
                                String chat_date = DefensiveClass.optString(pobj, Constants.TAG_CHAT_DATE);

                                pmap.put(Constants.TAG_MESSAGE, message);
                                pmap.put(Constants.TAG_USER_NAME, user_name);
                                pmap.put(Constants.TAG_FULL_NAME, full_name);
                                pmap.put(Constants.TAG_USER_ID, user_id);
                                pmap.put(Constants.TAG_USER_IMAGE, user_image);
                                pmap.put(Constants.TAG_CHAT_DATE, chat_date);

                                tempAry.add(pmap);
                            }

                            chatAry.addAll(tempAry);

                            if (mScrollListener != null) {
                                mScrollListener.setLoading(false);
                            }
                        }

                        abItemName.setText(detailMap.get(Constants.TAG_ITEM_TITLE));
                        abShopName.setText(getString(R.string.by) + " " + detailMap.get(Constants.TAG_SHOP_NAME));
                        if (!detailMap.get(Constants.TAG_IMAGE).equals("")) {
                            Picasso.get().load(detailMap.get(Constants.TAG_IMAGE)).into(abItemImage);
                        }
                    }
                    recyclerViewAdapter.notifyDataSetChanged();
                    if (firstload) {
                        firstload = false;
                        recyclerView.scrollToPosition(0);
                    }
                    if (chatAry.size() == 0) {
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
                loadmoreprogress.setVisibility(View.GONE);
                Log.e(TAG, "getChatsError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                SellerChat.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (chatAry.size() == 0) {
                            progressLay.setVisibility(View.VISIBLE);
                        }
                    }
                });
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                map.put("chat_id", chatid);
                Log.v(TAG, "getChatsParams=" + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void setErrorLayout() {
        if (chatAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
        }
    }

    private void sendChat(final String message) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SEND_SELLER_MESSAGE, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "sendChatRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {

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

                Log.e(TAG, "sendChatError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("chat_id", detailMap.get(Constants.TAG_CHAT_ID));
                map.put("subject", detailMap.get(Constants.TAG_SUBJECT));
                map.put("item_id", detailMap.get(Constants.TAG_ITEM_ID));
                map.put("shop_id", detailMap.get(Constants.TAG_SHOP_ID));
                map.put("message", message);
                Log.v(TAG, "sendChatParams=" + map);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
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
    public void onBackPressed() {
        super.onBackPressed();
        if (from.equals("message"))
            MessageFragment.refreshMessage = true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                if (from.equals("message"))
                    MessageFragment.refreshMessage = true;
                finish();
                break;
            case R.id.sendBtn:
                if (chatEdit.getText().toString().trim().length() == 0) {
                    chatEdit.setError(getString(R.string.please_enter_message));
//                    FantacyApplication.showToast(SellerChat.this, getString(R.string.please_enter_message), Toast.LENGTH_SHORT);
                } else {
                    sendChat(chatEdit.getText().toString().trim());
                    HashMap<String, String> pmap = new HashMap<>();
                    pmap.put(Constants.TAG_MESSAGE, chatEdit.getText().toString().trim());
                    pmap.put(Constants.TAG_USER_NAME, GetSet.getUserName());
                    pmap.put(Constants.TAG_FULL_NAME, GetSet.getFullName());
                    pmap.put(Constants.TAG_USER_ID, GetSet.getUserId());
                    pmap.put(Constants.TAG_USER_IMAGE, GetSet.getImageUrl());
                    pmap.put(Constants.TAG_CHAT_DATE, String.valueOf(System.currentTimeMillis() / 1000L));
                    backup.clear();
                    backup.addAll(chatAry);
                    chatAry.clear();
                    chatAry.add(pmap);
                    chatAry.addAll(backup);
                    chatEdit.setText("");
                    recyclerView.scrollToPosition(0);
                    recyclerViewAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.abItemLay:
                getItemDetails(itemID, this);
                break;
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public int getItemViewType(int position) {
            return Items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

            if (viewHolder instanceof MyViewHolder) {
                HashMap<String, String> tempMap = Items.get(position);
                MyViewHolder holder = (MyViewHolder) viewHolder;
                holder.leftLay.setVisibility(View.GONE);
                holder.rightLay.setVisibility(View.GONE);
                holder.dateLay.setVisibility(View.GONE);
                if (tempMap.get(Constants.TAG_USER_ID).equals(GetSet.getUserId())) {
                    holder.rightLay.setVisibility(View.VISIBLE);
                    holder.rightMsg.setText(tempMap.get(Constants.TAG_MESSAGE));
                    holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.TAG_CHAT_DATE)) * 1000));
                    String image = tempMap.get(Constants.TAG_USER_IMAGE);
                    if (image != null && !image.equals("")) {
                        Picasso.get().load(image).into(holder.rightImage);
                    }

                    if (position < getItemCount() - 1) {
                        if (Items.get(position + 1).get(Constants.TAG_USER_ID).equals(Items.get(position).get(Constants.TAG_USER_ID))) {
                            holder.rightImage.setVisibility(View.INVISIBLE);
                        } else {
                            holder.rightImage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        holder.rightImage.setVisibility(View.VISIBLE);
                    }
                } else {
                    holder.leftLay.setVisibility(View.VISIBLE);
                    holder.leftMsg.setText(tempMap.get(Constants.TAG_MESSAGE));
                    holder.leftTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.TAG_CHAT_DATE)) * 1000));
                    String image = tempMap.get(Constants.TAG_USER_IMAGE);
                    if (image != null && !image.equals("")) {
                        Picasso.get().load(image).into(holder.leftImage);
                    }

                    if (position < getItemCount() - 1) {
                        if (Items.get(position + 1).get(Constants.TAG_USER_ID).equals(Items.get(position).get(Constants.TAG_USER_ID))) {
                            holder.leftImage.setVisibility(View.INVISIBLE);
                        } else {
                            holder.leftImage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        holder.leftImage.setVisibility(View.VISIBLE);
                    }
                }

                try {
                    long date = Long.parseLong(tempMap.get(Constants.TAG_CHAT_DATE)) * 1000;
                    String chatDate = FantacyApplication.getDate(date);
                    if (position == getItemCount() - 1) {
                        holder.dateLay.setVisibility(View.VISIBLE);
                        holder.date.setText(chatDate);
                    } else {
                        String ldate = FantacyApplication.getDate(Long.parseLong(Items.get(position + 1).get(Constants.TAG_CHAT_DATE)) * 1000);
                        if (ldate.equals(chatDate)) {
                            holder.dateLay.setVisibility(View.GONE);
                        } else {
                            holder.dateLay.setVisibility(View.VISIBLE);
                            holder.date.setText(chatDate);
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
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
            public ProgressBar progressBar;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView date, leftMsg, rightMsg, leftTime, rightTime;
            ImageView leftImage, rightImage;
            RelativeLayout dateLay, leftLay, rightLay;

            public MyViewHolder(View view) {
                super(view);

                date = (TextView) view.findViewById(R.id.date);
                leftMsg = (TextView) view.findViewById(R.id.leftMsg);
                rightMsg = (TextView) view.findViewById(R.id.rightMsg);
                leftTime = (TextView) view.findViewById(R.id.leftTime);
                rightTime = (TextView) view.findViewById(R.id.rightTime);
                leftImage = (ImageView) view.findViewById(R.id.leftImage);
                rightImage = (ImageView) view.findViewById(R.id.rightImage);
                dateLay = (RelativeLayout) view.findViewById(R.id.dateLay);
                leftLay = (RelativeLayout) view.findViewById(R.id.leftLay);
                rightLay = (RelativeLayout) view.findViewById(R.id.rightLay);
            }
        }
    }

    private void getItemDetails(final String itemId, final Context context) {

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
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

                        String image = "";
                        JSONArray photos = temp.optJSONArray(Constants.TAG_PHOTOS);
                        if (photos == null) {
                            map.put(Constants.TAG_PHOTOS, "");
                        } else if (photos.length() == 0) {
                            map.put(Constants.TAG_PHOTOS, "");
                        } else {
                            JSONObject pobj = photos.getJSONObject(0);
                            image = DefensiveClass.optString(pobj, Constants.TAG_IMAGE_350);
                            map.put(Constants.TAG_PHOTOS, photos.toString());
                        }
                        map.put(Constants.TAG_IMAGE, image);

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
                        Intent i = new Intent(context, DetailActivity.class);
                        i.putExtra("items", itemsAry);
                        i.putExtra("position", 0);
                        startActivity(i);
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
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.e(TAG, "getItemDetails: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                if (GetSet.isLogged()) {
                    map.put("user_id", GetSet.getUserId());
                }
                map.put("item_id", itemId);
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
