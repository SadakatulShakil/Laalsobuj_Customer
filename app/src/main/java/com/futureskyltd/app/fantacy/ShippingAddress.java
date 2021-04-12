package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
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
import com.futureskyltd.app.utils.GetSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 7/7/17.
 */

public class ShippingAddress extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = ShippingAddress.class.getSimpleName();
    ImageView backBtn, nullImage;
    TextView title, bottomBtn, addAddress, nullText;
    String from = "";
    int selectedAddress;
    Display display;
    RecyclerView recyclerView;
    RelativeLayout progress, nullLay, mainLay;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<HashMap<String, String>> addressAry = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shipping_main_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        bottomBtn = (TextView) findViewById(R.id.bottomBtn);
        addAddress = (TextView) findViewById(R.id.addAddress);
        progress = (RelativeLayout) findViewById(R.id.progress);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        mainLay = (RelativeLayout) findViewById(R.id.mainLay);
        nullText = (TextView) findViewById(R.id.nullText);
        nullImage = (ImageView) findViewById(R.id.nullImage);

        from = getIntent().getExtras().getString("from");


        if (from.equals("profile")) {
            title.setText(getString(R.string.address_book));
            addAddress.setVisibility(View.GONE);
            bottomBtn.setText(getString(R.string.add_address));
        } else {
            addAddress.setVisibility(View.VISIBLE);
            bottomBtn.setText(getString(R.string.deliver_here));
            title.setText(getString(R.string.primary_address));
        }

        display = this.getWindowManager().getDefaultDisplay();

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        bottomBtn.setOnClickListener(this);
        addAddress.setOnClickListener(this);

        progress.setVisibility(View.VISIBLE);
        mainLay.setVisibility(View.GONE);
        nullLay.setVisibility(View.GONE);

        getAddress();

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(this,
                linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.emptyspace));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(this, addressAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getAddress() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_ADDRESS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getAddressRes=" + res);
                    JSONObject json = new JSONObject(res);

                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray result = json.getJSONArray(Constants.TAG_RESULT);

                        for (int i = 0; i < result.length(); i++) {
                            HashMap<String, String> map = new HashMap<String, String>();
                            JSONObject temp = result.getJSONObject(i);
                            String shippingid = DefensiveClass.optInt(temp, Constants.TAG_SHIPPING_ID);
                            String nickname = DefensiveClass.optString(temp, Constants.TAG_NICK_NAME);
                            String fullname = DefensiveClass.optString(temp, Constants.TAG_FULL_NAME);
                            String country_id = DefensiveClass.optString(temp, Constants.TAG_COUNTRY_ID);
                            String country_name = DefensiveClass.optString(temp, Constants.TAG_COUNTRY);
                            String state = DefensiveClass.optString(temp, Constants.TAG_STATE);
                            String address1 = DefensiveClass.optString(temp, Constants.TAG_ADDRESS1);
                            String address2 = DefensiveClass.optString(temp, Constants.TAG_ADDRESS2);
                            String city = DefensiveClass.optString(temp, Constants.TAG_CITY);
                            String zipcode = DefensiveClass.optString(temp, Constants.TAG_ZIPCODE);
                            String phone = DefensiveClass.optString(temp, Constants.TAG_PHONE_NO);
                            String defaults = DefensiveClass.optString(temp, Constants.TAG_DEFAULT);

                            map.put(Constants.TAG_SHIPPING_ID, shippingid);
                            map.put(Constants.TAG_NICK_NAME, nickname);
                            map.put(Constants.TAG_FULL_NAME, fullname);
                            map.put(Constants.TAG_COUNTRY_ID, country_id);
                            map.put(Constants.TAG_COUNTRY, country_name);
                            map.put(Constants.TAG_STATE, state);
                            map.put(Constants.TAG_ADDRESS1, address1);
                            if (!TextUtils.isEmpty(address2))
                                map.put(Constants.TAG_ADDRESS2, address2);
                            map.put(Constants.TAG_CITY, city);
                            map.put(Constants.TAG_ZIPCODE, zipcode);
                            map.put(Constants.TAG_PHONE_NO, phone);
                            map.put(Constants.TAG_DEFAULT, defaults);

                            if (defaults.equals("1")) {
                                addressAry.add(0, map);
                            } else {
                                addressAry.add(map);
                            }
                        }

                        for (int i = 0; i < addressAry.size(); i++) {
                            String defaults = addressAry.get(i).get(Constants.TAG_DEFAULT);
                            String shippingid = addressAry.get(i).get(Constants.TAG_SHIPPING_ID);
                            if (from.equals("cart")) {
                                if (getIntent().getExtras().get("shippingId").equals("0")) {
                                    if (defaults.equals("1")) {
                                        selectedAddress = 0;
                                    }
                                } else if (getIntent().getExtras().get("shippingId").equals(shippingid)) {
                                    selectedAddress = i;
                                }
                            }
                        }
                    }

                    recyclerViewAdapter.notifyDataSetChanged();

                    if (addressAry.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                        nullText.setText(getString(R.string.no_address));
                        nullImage.setImageResource(R.drawable.no_order);
                        progress.setVisibility(View.GONE);
                        mainLay.setVisibility(View.VISIBLE);
                    } else {
                        progress.setVisibility(View.GONE);
                        mainLay.setVisibility(View.VISIBLE);
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
                Log.e(TAG, "getAddressError: " + error.getMessage());
                setErrorLayout();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
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
        progress.setVisibility(View.GONE);
        mainLay.setVisibility(View.GONE);
        nullLay.setVisibility(View.VISIBLE);
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView selectedIcon, options;
            TextView fullName, addressLine, phone, defaultText;
            RelativeLayout main;
            View divider;

            public MyViewHolder(View view) {
                super(view);

                selectedIcon = (ImageView) view.findViewById(R.id.selectedIcon);
                options = (ImageView) view.findViewById(R.id.options);
                fullName = (TextView) view.findViewById(R.id.fullName);
                addressLine = (TextView) view.findViewById(R.id.addressLine);
                phone = (TextView) view.findViewById(R.id.phone);
                defaultText = (TextView) view.findViewById(R.id.defaultText);
                main = (RelativeLayout) view.findViewById(R.id.main);
                divider = (View) view.findViewById(R.id.divider);

                main.setOnClickListener(this);
                options.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.main:
                        selectedAddress = getAdapterPosition();
                        notifyDataSetChanged();
                        break;
                    case R.id.options:
                        String[] values;
                        if (from.equals("profile")) {
                            if (Items.get(getAdapterPosition()).get(Constants.TAG_DEFAULT).equals("1")) {
                                values = new String[]{getString(R.string.edit)};
                            } else {
                                values = new String[]{getString(R.string.edit), getString(R.string.remove), getString(R.string.primary_address)};
                            }
                        } else {
                            values = new String[]{getString(R.string.edit)};
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ShippingAddress.this,
                                R.layout.option_row_item, android.R.id.text1, values);
                        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = layoutInflater.inflate(R.layout.option_layout, null);
                        if (FantacyApplication.isRTL(ShippingAddress.this)) {
                            layout.setAnimation(AnimationUtils.loadAnimation(ShippingAddress.this, R.anim.grow_from_topleft_to_bottomright));
                        } else {
                            layout.setAnimation(AnimationUtils.loadAnimation(ShippingAddress.this, R.anim.grow_from_topright_to_bottomleft));
                        }
                        final PopupWindow popup = new PopupWindow(ShippingAddress.this);
                        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        popup.setContentView(layout);
                        popup.setWidth(display.getWidth() * 50 / 100);
                        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                        popup.setFocusable(true);
                        //popup.showAtLocation(v, Gravity.TOP|Gravity.LEFT,0,v.getHeight());

                        final ListView lv = (ListView) layout.findViewById(R.id.listView);
                        lv.setAdapter(adapter);
                        popup.showAsDropDown(v, -((display.getWidth() * 45 / 100)), -60);

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int pos, long id) {
                                switch (pos) {
                                    case 0:
                                        Intent i = new Intent(ShippingAddress.this, AddAddress.class);
                                        i.putExtra("from", from);
                                        i.putExtra("to", "edit");
                                        i.putExtra("data", Items.get(getAdapterPosition()));
                                        if (from.equals("cart")) {
                                            i.putExtra("shippingId", addressAry.get(selectedAddress).get(Constants.TAG_SHIPPING_ID));
                                            i.putExtra("itemId", (String) getIntent().getExtras().get("itemId"));
                                            if(getIntent().hasExtra("size"))
                                              i.putExtra("size", (String) getIntent().getExtras().get("size"));
                                            i.putExtra("quantity", (String) getIntent().getExtras().get("quantity"));
                                        }
                                        startActivity(i);
                                        popup.dismiss();
                                        break;
                                    case 1:
                                        removeOrDefaultAddress("remove", Items.get(getAdapterPosition()).get(Constants.TAG_SHIPPING_ID), getAdapterPosition());
                                        popup.dismiss();
                                        break;
                                    case 2:
                                        removeOrDefaultAddress("default", Items.get(getAdapterPosition()).get(Constants.TAG_SHIPPING_ID), getAdapterPosition());
                                        popup.dismiss();
                                        break;
                                }
                            }
                        });
                        break;
                }
            }
        }

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.address_item_layout, parent, false);

            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.fullName.setText(tempMap.get(Constants.TAG_FULL_NAME));
            if (tempMap.containsKey(Constants.TAG_ADDRESS2)) {
                holder.addressLine.setText(tempMap.get(Constants.TAG_ADDRESS1) + ", " + tempMap.get(Constants.TAG_ADDRESS2) + ", "
                        + tempMap.get(Constants.TAG_CITY) + ", " + tempMap.get(Constants.TAG_STATE) + ", " + tempMap.get(Constants.TAG_COUNTRY));
            } else {
                holder.addressLine.setText(tempMap.get(Constants.TAG_ADDRESS1) + ", " +
                        tempMap.get(Constants.TAG_CITY) + ", " + tempMap.get(Constants.TAG_STATE) + ", " + tempMap.get(Constants.TAG_COUNTRY));
            }
            holder.phone.setText(tempMap.get(Constants.TAG_PHONE_NO));

            if (tempMap.get(Constants.TAG_DEFAULT).equals("1") && from.equals("profile")) {
                holder.divider.setVisibility(View.VISIBLE);
                holder.defaultText.setVisibility(View.VISIBLE);
            } else {
                holder.divider.setVisibility(View.GONE);
                holder.defaultText.setVisibility(View.GONE);
            }

            if (from.equals("profile")) {
                holder.selectedIcon.setVisibility(View.GONE);
            } else {
                holder.selectedIcon.setVisibility(View.VISIBLE);
                if (selectedAddress == position) {
                    holder.selectedIcon.setImageResource(R.drawable.radio_select);
                } else {
                    holder.selectedIcon.setImageResource(R.drawable.radio_unselect);
                }
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }
    }

    private void removeOrDefaultAddress(final String from, final String shippingId, final int position) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        final ProgressDialog dialog = new ProgressDialog(ShippingAddress.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        String url = "";
        if (from.equals("remove")) {
            url = Constants.API_REMOVE_ADDRESS;
        } else {
            url = Constants.API_DEFAULT_ADDRESS;
        }
        StringRequest req = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    Log.i(TAG, "removeOrDefaultAddressRes= " + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        if (from.equals("remove")) {
                            addressAry.remove(position);
                            recyclerViewAdapter.notifyItemRemoved(position);
                        } else {
                            for (int i = 0; i < addressAry.size(); i++) {
                                addressAry.get(i).put(Constants.TAG_DEFAULT, "0");
                            }
                            addressAry.get(position).put(Constants.TAG_DEFAULT, "1");
                            HashMap<String, String> map = new HashMap<>();
                            map.putAll(addressAry.get(position));
                            addressAry.remove(position);
                            addressAry.add(0, map);
                            recyclerViewAdapter.notifyDataSetChanged();
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
                Log.e(TAG, "removeOrDefaultAddressError: " + error.getMessage());
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
                Log.i(TAG, "removeOrDefaultAddressParams= " + map);
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
    protected void onResume() {
        super.onResume();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), NetworkReceiver.isConnected());
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
            case R.id.addAddress:
                Intent i = new Intent(this, AddAddress.class);
                i.putExtra("from", "cartAdd");
                i.putExtra("to", "add");
                i.putExtra("shippingId", (String) getIntent().getExtras().get("shippingId"));
                i.putExtra("itemId", (String) getIntent().getExtras().get("itemId"));
                if(getIntent().hasExtra("size"))
                    i.putExtra("size", (String) getIntent().getExtras().get("size"));
                i.putExtra("quantity", (String) getIntent().getExtras().get("quantity"));
                startActivity(i);
                break;
            case R.id.bottomBtn:
                if (from.equals("profile")) {
                    Intent j = new Intent(this, AddAddress.class);
                    j.putExtra("from", from);
                    j.putExtra("to", "add");
                    startActivity(j);
                } else {
                    Intent c = new Intent(this, CartActivity.class);
                    c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    c.putExtra("shippingId", addressAry.get(selectedAddress).get(Constants.TAG_SHIPPING_ID));
                    c.putExtra("itemId", (String) getIntent().getExtras().get("itemId"));
                    if(getIntent().hasExtra("size"))
                        c.putExtra("size", (String) getIntent().getExtras().get("size"));
                    c.putExtra("quantity", (String) getIntent().getExtras().get("quantity"));
                    startActivity(c);
                }
                break;
        }
    }
}
