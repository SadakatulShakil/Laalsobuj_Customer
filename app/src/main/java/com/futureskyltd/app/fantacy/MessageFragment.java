package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
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
 * Created by hitasoft on 31/5/17.
 */

public class MessageFragment extends Fragment {

    private static final String TAG = MessageFragment.class.getSimpleName();
    public static boolean refreshMessage = false;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<HashMap<String, String>> messageAry = new ArrayList<HashMap<String, String>>();
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    RelativeLayout progressLay, nullLay;
    ImageView nullImage;
    TextView nullText;
    EditText searchView;
    String searchKey = "";
    private ImageView clearBtn;
    private static boolean isCleared;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_main_layout, container, false);

        ((FragmentChangeListener) getActivity()).setNavSelectionItem(R.id.message, "Message");

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

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(getActivity(), linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), messageAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        messageAry.clear();

        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchView.clearFocus();
                    searchKey = searchView.getText().toString().trim();
                    FantacyApplication.hideSoftKeyboard(getActivity(), textView);
                    messageAry.clear();
                    getMessageData(0);
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
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setText(" ");
                searchKey = "";//Remove Previous Search Text
                clearBtn.setVisibility(View.GONE);
                isCleared = true;
                getMessageData(0);
            }
        });
        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    getMessageData(current_page * Constants.OVERALL_LIMIT);
                    Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
                }
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollListener.resetpagecount();
                messageAry.clear();
                getMessageData(0);
            }
        });

        getMessageData(0);

        FantacyApplication.setupUI(getActivity(), getView().findViewById(R.id.parentLay));
    }

    private void getMessageData(final int offset) {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CONTACT_SELLER_MESSAGE, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getMessageDataRes=" + res);
                    JSONObject json = new JSONObject(res);
                    progressLay.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setEnabled(true);
                    if (messageAry.size() >= Constants.OVERALL_LIMIT && messageAry.get(messageAry.size() - 1) == null) {
                        messageAry.remove(messageAry.size() - 1);
                        recyclerViewAdapter.notifyItemRemoved(messageAry.size());
                    }
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        messageAry.clear();
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }
                    if (isCleared) {
                        isCleared = false;
                        messageAry.clear();
                    }
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray result = json.getJSONArray(Constants.TAG_RESULT);
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject temp = result.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();

                            String chat_id = DefensiveClass.optString(temp, Constants.TAG_CHAT_ID);
                            String item_title = DefensiveClass.optString(temp, Constants.TAG_ITEM_TITLE);
                            String item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                            String image = DefensiveClass.optString(temp, Constants.TAG_IMAGE);
                            String subject = DefensiveClass.optString(temp, Constants.TAG_SUBJECT);
                            String message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);
                            String chat_date = DefensiveClass.optString(temp, Constants.TAG_CHAT_DATE);
                            String shop_id = DefensiveClass.optString(temp, Constants.TAG_SHOP_ID);
                            String shop_name = DefensiveClass.optString(temp, Constants.TAG_SHOP_NAME);
                            String shop_image = DefensiveClass.optString(temp, Constants.TAG_SHOP_IMAGE);
                            String last_replied = DefensiveClass.optString(temp, Constants.TAG_LAST_REPLIED);

                            map.put(Constants.TAG_CHAT_ID, chat_id);
                            map.put(Constants.TAG_ITEM_TITLE, item_title);
                            map.put(Constants.TAG_ITEM_ID, item_id);
                            map.put(Constants.TAG_IMAGE, image);
                            map.put(Constants.TAG_SUBJECT, subject);
                            map.put(Constants.TAG_MESSAGE, message);
                            map.put(Constants.TAG_CHAT_DATE, chat_date);
                            map.put(Constants.TAG_SHOP_ID, shop_id);
                            map.put(Constants.TAG_SHOP_NAME, shop_name);
                            map.put(Constants.TAG_SHOP_IMAGE, shop_image);
                            map.put(Constants.TAG_LAST_REPLIED, last_replied);

                            messageAry.add(map);
                        }
                        if (mScrollListener != null && messageAry.size() >= Constants.OVERALL_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                    }
                    if (messageAry.size() == 0) {
                        nullImage.setImageResource(R.drawable.no_message);
                        nullText.setText(getString(R.string.no_message));
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
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
                Log.e(TAG, "getMessageDataError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (messageAry.size() >= Constants.OVERALL_LIMIT) {
                            messageAry.add(null);
                            recyclerViewAdapter.notifyItemInserted(messageAry.size() - 1);
                        } else if (messageAry.size() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
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
                map.put("search_key", searchKey);
                Log.v(TAG, "getMessageDataParams=" + map);
                return map;
            }

            SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
            String accesstoken = preferences.getString("TOKEN", null);

        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void setErrorLayout() {
        if (messageAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
            }
            mSwipeRefreshLayout.setEnabled(true);
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

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView itemImage;
            TextView itemName, message, date;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                itemImage = (ImageView) view.findViewById(R.id.itemImage);
                itemName = (TextView) view.findViewById(R.id.itemName);
                message = (TextView) view.findViewById(R.id.message);
                date = (TextView) view.findViewById(R.id.date);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);

                itemImage.setOnClickListener(this);
                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.itemImage:
                        getItemDetails(Items.get(getAdapterPosition()).get(Constants.TAG_ITEM_ID), context);
                        break;
                    case R.id.mainLay:
                        Intent i = new Intent(getActivity(), SellerChat.class);
                        i.putExtra("from", "message");
                        i.putExtra("chatid", messageAry.get(getAdapterPosition()).get(Constants.TAG_CHAT_ID));
                        startActivity(i);
                        break;
                }
            }
        }

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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                HashMap<String, String> tempMap = Items.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;
                viewHolder.itemName.setText(tempMap.get(Constants.TAG_ITEM_TITLE));
                viewHolder.message.setText(tempMap.get(Constants.TAG_MESSAGE));
                if (!tempMap.get(Constants.TAG_CHAT_DATE).equals(null) && !tempMap.get(Constants.TAG_CHAT_DATE).equals("")) {
                    long date = Long.parseLong(tempMap.get(Constants.TAG_CHAT_DATE)) * 1000;
                    viewHolder.date.setText(FantacyApplication.getDate(date));
                }
                String image = tempMap.get(Constants.TAG_IMAGE);
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(viewHolder.itemImage);
                }

                if (!tempMap.get(Constants.TAG_LAST_REPLIED).equals("0") && !tempMap.get(Constants.TAG_LAST_REPLIED).equals(GetSet.getUserId())) {
                    viewHolder.message.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                } else {
                    viewHolder.message.setTextColor(ContextCompat.getColor(context, R.color.textSecondary));
                }

                if (FantacyApplication.isRTL(getActivity())) {
                    viewHolder.itemName.setGravity(Gravity.RIGHT);
                } else {
                    viewHolder.itemName.setGravity(Gravity.LEFT);
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

                        String image = "";
                        JSONArray photos = temp.optJSONArray(Constants.TAG_PHOTOS);
                        if (photos == null) {
                            map.put(Constants.TAG_PHOTOS, "");
                        } else if (photos.length() == 0) {
                            map.put(Constants.TAG_PHOTOS, "");
                        } else if (photos.length() > 0) {
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
                        Intent i = new Intent(getActivity(), DetailActivity.class);
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
        if (refreshMessage) {
            refreshMessage = false;
            swipeRefresh();
            mScrollListener.resetpagecount();
            getMessageData(0);
        }
    }
}
