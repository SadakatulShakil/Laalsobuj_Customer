package com.futureskyltd.app.fantacy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by hitasoft on 24/6/17.
 */

public class CartActivity extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, edit, nullImage;
    TextView title, addressName, address1, address2, country, phone, change, itemPrice,
            shippingPrice, taxPrice, grandTotal, continueBtn, nullText, txtPrice;
    RelativeLayout addressLay, progressLay, nullLay, mainLay;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    String item_total, shipping_price, tax, grand_total, currency, shippingId = "0", itemId = "0", size = "", quantity = "", id = "";
    int totalItems = 0;
    RecyclerViewAdapter recyclerViewAdapter;
    NestedScrollView scrollView;
    HashMap<String, String> shipMap = new HashMap<>();
    ArrayList<HashMap<String, String>> itemsAry = new ArrayList<HashMap<String, String>>();
    ProgressDialog dialog;
    Boolean allBuyable = true;
    private SharedPreferences preferences;
    private String accesstoken, customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_main_layout);
        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);
        customerId = preferences.getString("customer_id", null);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        scrollView = (NestedScrollView) findViewById(R.id.scrollView);
        addressLay = (RelativeLayout) findViewById(R.id.addressLay);
        edit = (ImageView) findViewById(R.id.edit);
        addressName = (TextView) findViewById(R.id.addressName);
        address1 = (TextView) findViewById(R.id.address1);
        address2 = (TextView) findViewById(R.id.address2);
        country = (TextView) findViewById(R.id.country);
        phone = (TextView) findViewById(R.id.phone);
        change = (TextView) findViewById(R.id.change);
        itemPrice = (TextView) findViewById(R.id.itemPrice);
        shippingPrice = (TextView) findViewById(R.id.shippingPrice);
        taxPrice = (TextView) findViewById(R.id.taxPrice);
        grandTotal = (TextView) findViewById(R.id.grandTotal);
        continueBtn = (TextView) findViewById(R.id.continueBtn);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        nullImage = (ImageView) findViewById(R.id.nullImage);
        nullText = (TextView) findViewById(R.id.nullText);
        progressLay = (RelativeLayout) findViewById(R.id.progress);
        mainLay = (RelativeLayout) findViewById(R.id.mainLay);
        txtPrice = (TextView) findViewById(R.id.txtPrice);

        dialog = new ProgressDialog(CartActivity.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        title.setText(getString(R.string.cart));

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        shippingId = (String) getIntent().getExtras().get("shippingId");
        itemId = (String) getIntent().getExtras().get("itemId");
        //if (!itemId.equals("0")) {
            //String value[] = itemId.split("-");
            //id = value[0];
        //    size = value[1];
          //  quantity = value[2];
        //}

        Log.e("itemIdd","-"+itemId);
        if (!itemId.equals("0")) {
            id = itemId;
        }

        if(getIntent().hasExtra("size")){
            size = (String) getIntent().getExtras().get("size");
        }
        if(getIntent().hasExtra("quantity")){
            quantity = (String) getIntent().getExtras().get("quantity");
        }

        Log.e("cartValue","itemId- "+itemId+" id -"+id+" size- "+size+" quantity- "+quantity);

        recyclerView.setNestedScrollingEnabled(false);
        backBtn.setOnClickListener(this);
        continueBtn.setOnClickListener(this);
        change.setOnClickListener(this);
        edit.setOnClickListener(this);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        recyclerView.addItemDecoration(itemDivider);

        getCartData();

        recyclerViewAdapter = new RecyclerViewAdapter(this, itemsAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getCartData() {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CART, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getCartDataRes=" + res);
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);
                        itemsAry.clear();
                        allBuyable = true;
                        item_total = DefensiveClass.optString(result, Constants.TAG_ITEM_TOTAL);
                        shipping_price = DefensiveClass.optString(result, Constants.TAG_SHIPPING_PRICE);
                        tax = DefensiveClass.optString(result, Constants.TAG_TAX);
                        grand_total = DefensiveClass.optString(result, Constants.TAG_GRAND_TOTAL);
                        currency = DefensiveClass.optString(result, Constants.TAG_CURRENCY);

                        JSONArray items = result.getJSONArray(Constants.TAG_ITEMS);
                        totalItems = items.length();
                        if (totalItems > 1) {
                            txtPrice.setText(getString(R.string.price) + " (" + totalItems + " " + getString(R.string.items) + ")");
                        } else if (items.length() > 0) {
                            txtPrice.setText(getString(R.string.price) + " (" + totalItems + " " + getString(R.string.item) + ")");
                        }
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject temp = items.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();

                            String cart_id = DefensiveClass.optString(temp, Constants.TAG_CART_ID);
                            String item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                            String item_name = DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME);
                            String item_image = DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE);
                            String price = DefensiveClass.optString(temp, Constants.TAG_PRICE);
                            String mainprice = DefensiveClass.optString(temp, Constants.TAG_MAIN_PRICE);
                            String size = DefensiveClass.optString(temp, Constants.TAG_SIZE);
                            String quantity = DefensiveClass.optString(temp, Constants.TAG_QUANTITY);
                            String total_quantity = DefensiveClass.optString(temp, Constants.TAG_TOTAL_QUANTITY);
                            String shipping_time = DefensiveClass.optString(temp, Constants.TAG_SHIPPING_TIME);
                            String deal_enabled = DefensiveClass.optString(temp, Constants.TAG_DEAL_ENABLED);
                            String discount_percentage = DefensiveClass.optString(temp, Constants.TAG_DISCOUNT_PERCENTAGE);
                            String store_id = DefensiveClass.optString(temp, Constants.TAG_STORE_ID);
                            String store_name = DefensiveClass.optString(temp, Constants.TAG_STORE_NAME);
                            String buyable = DefensiveClass.optString(temp, Constants.TAG_BUYABLE);
                            String cart_message = DefensiveClass.optString(temp, Constants.TAG_CART_MESSAGE);
                            if (buyable.equals("no"))
                                allBuyable = false;

                            map.put(Constants.TAG_CART_ID, cart_id);
                            map.put(Constants.TAG_ITEM_ID, item_id);
                            map.put(Constants.TAG_ITEM_NAME, item_name);
                            map.put(Constants.TAG_ITEM_IMAGE, item_image);
                            map.put(Constants.TAG_PRICE, price);
                            map.put(Constants.TAG_MAIN_PRICE, mainprice);
                            map.put(Constants.TAG_SIZE, size);
                            map.put(Constants.TAG_QUANTITY, quantity);
                            map.put(Constants.TAG_TOTAL_QUANTITY, total_quantity);
                            map.put(Constants.TAG_SHIPPING_TIME, shipping_time);
                            map.put(Constants.TAG_DEAL_ENABLED, deal_enabled);
                            map.put(Constants.TAG_DISCOUNT_PERCENTAGE, discount_percentage);
                            map.put(Constants.TAG_STORE_ID, store_id);
                            map.put(Constants.TAG_STORE_NAME, store_name);
                            map.put(Constants.TAG_BUYABLE, buyable);
                            map.put(Constants.TAG_CART_MESSAGE, cart_message);

                            itemsAry.add(map);
                        }

                        JSONObject shipping = result.optJSONObject(Constants.TAG_SHIPPING);

                        if (shipping != null) {
                            String shipping_id = DefensiveClass.optString(shipping, Constants.TAG_SHIPPING_ID);
                            String full_name = DefensiveClass.optString(shipping, Constants.TAG_FULL_NAME);
                            String nick_name = DefensiveClass.optString(shipping, Constants.TAG_NICK_NAME);
                            String address1 = DefensiveClass.optString(shipping, Constants.TAG_ADDRESS1);
                            String address2 = DefensiveClass.optString(shipping, Constants.TAG_ADDRESS2);
                            String city = DefensiveClass.optString(shipping, Constants.TAG_CITY);
                            String state = DefensiveClass.optString(shipping, Constants.TAG_STATE);
                            String country = DefensiveClass.optString(shipping, Constants.TAG_COUNTRY);
                            String country_id = DefensiveClass.optString(shipping, Constants.TAG_COUNTRY_ID);
                            String zipcode = DefensiveClass.optString(shipping, Constants.TAG_ZIPCODE);
                            String phone = DefensiveClass.optString(shipping, Constants.TAG_PHONE);

                            if (!shipping_id.equals("")) {
                                shipMap.put(Constants.TAG_SHIPPING_ID, shipping_id);
                                shipMap.put(Constants.TAG_FULL_NAME, full_name);
                                shipMap.put(Constants.TAG_NICK_NAME, nick_name);
                                shipMap.put(Constants.TAG_ADDRESS1, address1);
                                shipMap.put(Constants.TAG_ADDRESS2, address2);
                                shipMap.put(Constants.TAG_CITY, city);
                                shipMap.put(Constants.TAG_STATE, state);
                                shipMap.put(Constants.TAG_COUNTRY, country);
                                shipMap.put(Constants.TAG_COUNTRY_ID, country_id);
                                shipMap.put(Constants.TAG_ZIPCODE, zipcode);
                                shipMap.put(Constants.TAG_PHONE_NO, phone);
                            }

                        }
                        setCartData();
                        mainLay.setVisibility(View.VISIBLE);
                        continueBtn.setVisibility(View.VISIBLE);
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                            /*Intent i = new Intent(CartActivity.this, PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*/
                        } else {
                           /* Intent i = new Intent(CartActivity.this, PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*/
                        }
                    } else if (status.equalsIgnoreCase("false")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals("No data found")) {
                            nullImage.setImageResource(R.drawable.no_item);
                            nullText.setText(getString(R.string.no_cart));
                            nullLay.setVisibility(View.VISIBLE);
                            mainLay.setVisibility(View.GONE);
                            continueBtn.setVisibility(View.GONE);
                        } else setErrorLayout();
                    }


                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    progressLay.setVisibility(View.GONE);

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
                Log.e(TAG, "getCartDataError: " + error.getMessage());
                setErrorLayout();
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("shipping_id", shippingId);
                if (!itemId.equals("0")) {
                    map.put("item_id", id);
                    map.put("size", size);
                    map.put("quantity", quantity);
                } else {
                    map.put("item_id", itemId);
                }
                Log.v(TAG, "getCartDataParams=" + map);
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
        if (!dialog.isShowing()) {
            progressLay.setVisibility(View.VISIBLE);
            nullLay.setVisibility(View.GONE);
            mainLay.setVisibility(View.GONE);
            continueBtn.setVisibility(View.GONE);
        }

        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void changeQuantity(final String qty, final int position, final String oldquantity) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CHANGE_CART_QUANTITY, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "changeQuantityRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        if (!itemId.equals("0")) {
                            quantity = qty;
                        }
                        getCartData();

                    } else {
                        HashMap<String, String> temp = itemsAry.get(position);
                        temp.put(Constants.TAG_QUANTITY, oldquantity);
                        itemsAry.remove(position);
                        itemsAry.add(position, temp);
                        recyclerViewAdapter.notifyDataSetChanged();

                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        FantacyApplication.defaultSnack(CartActivity.this.findViewById(R.id.parentLay), message, "error");

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
                FantacyApplication.defaultSnack(CartActivity.this.findViewById(R.id.parentLay), getString(R.string.something_went_wrong), "error");
                Log.e(TAG, "changeQuantityError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);

                if (itemsAry.get(position).get(Constants.TAG_CART_ID).equals("")) {
                    map.put("item_id", id);
                    map.put("cart_id", "0");
                } else {
                    map.put("cart_id", itemsAry.get(position).get(Constants.TAG_CART_ID));
                }
                map.put("quantity", qty);
                map.put(Constants.TAG_SIZE, itemsAry.get(position).containsKey(Constants.TAG_SIZE) ?
                        itemsAry.get(position).get(Constants.TAG_SIZE) : "");
                Log.v(TAG, "changeQuantityParams=" + map);
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

    private void removeCartItem(final String cartid) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_REMOVE_CART, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "removeCartItemRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);

                    if (!status.equalsIgnoreCase("true")) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        FantacyApplication.defaultSnack(CartActivity.this.findViewById(R.id.parentLay), message, "error");
                    } else {
                        getCartData();
                        try {
                            if (!FragmentMainActivity.cartCount.equals("0")) {
                                int count = Integer.parseInt(FragmentMainActivity.cartCount) - 1;
                                FragmentMainActivity.cartCount = String.valueOf(count);
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
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
                FantacyApplication.defaultSnack(CartActivity.this.findViewById(R.id.parentLay), getString(R.string.something_went_wrong), "error");
                Log.e(TAG, "removeCartItemError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("cart_id", cartid);
                Log.v(TAG, "removeCartItemParams=" + map);
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

    private void setErrorLayout() {
        if (itemsAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            mainLay.setVisibility(View.GONE);
            continueBtn.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
        }
    }

    private void setCartData() {

        if (shipMap.isEmpty()) {
            addressLay.setVisibility(View.GONE);
            change.setText(R.string.add_address);
        } else {
            addressLay.setVisibility(View.VISIBLE);
            change.setText(R.string.change_add_address);

            addressName.setText(shipMap.get(Constants.TAG_FULL_NAME));
            address1.setText(shipMap.get(Constants.TAG_ADDRESS1));
            address2.setText(shipMap.get(Constants.TAG_ADDRESS2) + ", " + shipMap.get(Constants.TAG_CITY));
            country.setText(shipMap.get(Constants.TAG_STATE) + ", " + shipMap.get(Constants.TAG_COUNTRY));
            phone.setText(shipMap.get(Constants.TAG_PHONE_NO));
        }

        itemPrice.setText(currency + " " + item_total);
        if (shipping_price.equals("0") || shipping_price.equals(""))
            shippingPrice.setText(getString(R.string.free_text));
        else
            shippingPrice.setText(currency + " " + FantacyApplication.formatPrice(shipping_price));
        taxPrice.setText(currency + " " + FantacyApplication.formatPrice(tax));
        grandTotal.setText(currency + " " + FantacyApplication.formatPrice(grand_total));

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

    private void getItemDetails(final String itemID, final Context context) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ITEM_DETAILS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    ArrayList<HashMap<String, String>> itemsAry = new ArrayList<HashMap<String, String>>();
                    Log.d(TAG, "getItemDetailsRes" + res);
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

                        Intent i = new Intent(CartActivity.this, DetailActivity.class);
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
                Log.e(TAG, "getItemDetailsError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                if(accesstoken != null) {
                    map.put("user_id", customerId);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.continueBtn:
                if (change.getText().toString().equals(getString(R.string.add_address))) {
                    Intent l = new Intent(this, AddAddress.class);
                    l.putExtra("from", "cartAdd");
                    l.putExtra("to", "add");
                    l.putExtra("shippingId", (String) getIntent().getExtras().get("shippingId"));
                    l.putExtra("itemId", (String) getIntent().getExtras().get("itemId"));
                    if(getIntent().hasExtra("size"))
                        l.putExtra("size", (String) getIntent().getExtras().get("size"));
                    l.putExtra("quantity", (String) getIntent().getExtras().get("quantity"));
                    startActivity(l);
                } else if (allBuyable) {
                    Intent i = new Intent(this, PaymentActivity.class);
                    i.putExtra("shippingId", shippingId);
                    i.putExtra("totalItems", totalItems);
                    if (!itemId.equals("0")) {
                        i.putExtra("itemId", id);
                        i.putExtra("size", size);
                        i.putExtra("quantity", quantity);
                    } else {
                        i.putExtra("itemId", itemId);
                    }
                    startActivity(i);
                    //Toast.makeText(this, "Under Construction ! ", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.change:
                if (change.getText().toString().equals(getString(R.string.add_address))) {
                    Intent l = new Intent(this, AddAddress.class);
                    l.putExtra("from", "cartAdd");
                    l.putExtra("to", "add");
                    l.putExtra("shippingId", (String) getIntent().getExtras().get("shippingId"));
                    l.putExtra("itemId", (String) getIntent().getExtras().get("itemId"));
                    if(getIntent().hasExtra("size"))
                        l.putExtra("size", (String) getIntent().getExtras().get("size"));
                    l.putExtra("quantity", (String) getIntent().getExtras().get("quantity"));
                    startActivity(l);
                } else {
                    Intent j = new Intent(this, ShippingAddress.class);
                    j.putExtra("from", "cart");
                    j.putExtra("shippingId", (String) getIntent().getExtras().get("shippingId"));
                    j.putExtra("itemId", (String) getIntent().getExtras().get("itemId"));
                    if(getIntent().hasExtra("size"))
                         j.putExtra("size", (String) getIntent().getExtras().get("size"));
                    j.putExtra("quantity", (String) getIntent().getExtras().get("quantity"));
                    startActivity(j);
                }
                break;
            case R.id.edit:
                Intent k = new Intent(CartActivity.this, AddAddress.class);
                k.putExtra("from", "cartEdit");
                k.putExtra("to", "edit");
                k.putExtra("data", shipMap);
                k.putExtra("shippingId", shipMap.get(Constants.TAG_SHIPPING_ID));
                k.putExtra("itemId", (String) getIntent().getExtras().get("itemId"));
                if(getIntent().hasExtra("size"))
                    k.putExtra("size", (String) getIntent().getExtras().get("size"));
                k.putExtra("quantity", (String) getIntent().getExtras().get("quantity"));
                startActivity(k);
                break;
        }
    }

    /////////////////////Cart Item Adapter for Recycler view.//////////////
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items, parent, false);
            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.itemName.setText(tempMap.get(Constants.TAG_ITEM_NAME));
            holder.itemPrice.setText(currency + " " + tempMap.get(Constants.TAG_PRICE));
            holder.shipping.setText(tempMap.get(Constants.TAG_SHIPPING_TIME));
            holder.quantity.setText(tempMap.get(Constants.TAG_QUANTITY));
            if (tempMap.get(Constants.TAG_BUYABLE).equals("yes")) {
                holder.error.setVisibility(View.GONE);
            } else {
                holder.error.setVisibility(View.VISIBLE);
                if (tempMap.get(Constants.TAG_CART_MESSAGE).equals("Requested Quantity Not Available")) {
                    holder.error.setText(getString(R.string.quantity_error));
                } else {
                    holder.error.setText(tempMap.get(Constants.TAG_CART_MESSAGE));
                }
            }

            if (tempMap.get(Constants.TAG_DEAL_ENABLED).equalsIgnoreCase("yes") && !tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE).equals("0")) {
                holder.discountPrice.setVisibility(View.VISIBLE);
                holder.discountPercent.setVisibility(View.VISIBLE);
                holder.discountPrice.setPaintFlags(holder.discountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                holder.discountPrice.setText(currency + " " + tempMap.get(Constants.TAG_MAIN_PRICE));
                holder.discountPercent.setText(tempMap.get(Constants.TAG_DISCOUNT_PERCENTAGE) + "% off");
            } else {
                holder.discountPrice.setVisibility(View.GONE);
                holder.discountPercent.setVisibility(View.GONE);
            }

            String size = tempMap.get(Constants.TAG_SIZE);
            if (size.equals("")) {
                holder.itemSize.setText(getString(R.string.size) + ": N/A");
            } else {
                holder.itemSize.setText(getString(R.string.size) + ": " + size);
            }

            if (!itemId.equals("0")) {
                holder.cancel.setVisibility(View.GONE);
            } else {
                holder.cancel.setVisibility(View.VISIBLE);
            }

            String image = tempMap.get(Constants.TAG_ITEM_IMAGE);
            if (image != null && !image.equals("")) {
                Picasso.get().load(image).into(holder.itemImage);
            }

            if (Items.get(position).get(Constants.TAG_TOTAL_QUANTITY).equals("1")) {
                holder.qtyPlus.setVisibility(View.INVISIBLE);
                holder.qtyMinus.setVisibility(View.INVISIBLE);
            } else {
                holder.qtyPlus.setVisibility(View.VISIBLE);
                holder.qtyMinus.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView itemImage, cancel, qtyMinus, qtyPlus;
            TextView itemName, itemPrice, itemSize, shipping, quantity, error, discountPrice, discountPercent;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                itemImage = (ImageView) view.findViewById(R.id.itemImage);
                itemName = (TextView) view.findViewById(R.id.itemName);
                itemPrice = (TextView) view.findViewById(R.id.itemPrice);
                itemSize = (TextView) view.findViewById(R.id.itemSize);
                shipping = (TextView) view.findViewById(R.id.shipping);
                quantity = (TextView) view.findViewById(R.id.quantity);
                error = (TextView) view.findViewById(R.id.error);
                cancel = (ImageView) view.findViewById(R.id.cancel);
                qtyMinus = (ImageView) view.findViewById(R.id.qtyMinus);
                qtyPlus = (ImageView) view.findViewById(R.id.qtyPlus);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);
                discountPrice = (TextView) view.findViewById(R.id.discountPrice);
                discountPercent = (TextView) view.findViewById(R.id.discountPercent);

                itemImage.setOnClickListener(this);
                cancel.setOnClickListener(this);
                qtyMinus.setOnClickListener(this);
                qtyPlus.setOnClickListener(this);
            }


            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.itemImage:
                        getItemDetails(Items.get(getAdapterPosition()).get(Constants.TAG_ITEM_ID), context);
                        break;
                    case R.id.cancel:

                        final Dialog dialog = new Dialog(CartActivity.this);
                        Display display = getWindowManager().getDefaultDisplay();
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.setContentView(R.layout.default_popup);
                        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.setCancelable(true);
                        dialog.setCanceledOnTouchOutside(false);

                        TextView title = (TextView) dialog.findViewById(R.id.title);
                        TextView yes = (TextView) dialog.findViewById(R.id.yes);
                        TextView no = (TextView) dialog.findViewById(R.id.no);

                        title.setText(getString(R.string.really_remove_cart));
                        yes.setText(getString(R.string.yes));
                        no.setText(getString(R.string.no));

                        no.setVisibility(View.VISIBLE);

                        yes.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                removeCartItem(Items.get(getAdapterPosition()).get(Constants.TAG_CART_ID));
                                Items.remove(getAdapterPosition());
                                notifyItemRemoved(getAdapterPosition());
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

                        break;
                    case R.id.qtyMinus:
                        if (!Items.get(getAdapterPosition()).get(Constants.TAG_BUYABLE).equals("no")) {
                            String qty = Items.get(getAdapterPosition()).get(Constants.TAG_QUANTITY);
                            if (!qty.equals("1")) {
                                int minusQty = Integer.parseInt(qty) - 1;
                                changeQuantity(String.valueOf(minusQty), getAdapterPosition(), qty);
                                Items.get(getAdapterPosition()).put(Constants.TAG_QUANTITY, String.valueOf(minusQty));
                                notifyDataSetChanged();
                            }
                        }
                        break;
                    case R.id.qtyPlus:
                        if (!Items.get(getAdapterPosition()).get(Constants.TAG_BUYABLE).equals("no")) {
                            String currentqty = Items.get(getAdapterPosition()).get(Constants.TAG_QUANTITY);
                            int qtyp = Integer.parseInt(Items.get(getAdapterPosition()).get(Constants.TAG_QUANTITY));
                            if (qtyp < Integer.parseInt(Items.get(getAdapterPosition()).get(Constants.TAG_TOTAL_QUANTITY))) {
                                qtyp = qtyp + 1;
                                changeQuantity(String.valueOf(qtyp), getAdapterPosition(), currentqty);
                                Items.get(getAdapterPosition()).put(Constants.TAG_QUANTITY, String.valueOf(qtyp));
                                notifyDataSetChanged();
                            }
                        }
                        break;
                }
            }
        }
    }
}
