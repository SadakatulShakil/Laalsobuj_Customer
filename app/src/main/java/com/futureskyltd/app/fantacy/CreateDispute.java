package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by hitasoft on 21/7/17.
 */

public class CreateDispute extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn;
    TextView title, orderId, price, itemTotal, createDispute;
    EditText messageEdit;
    Spinner querySpin;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    String order_id = "", grand_total = "", currency = "", selectedItem = "", selectedQuery;
    ArrayList<String> queryList = new ArrayList<String>();
    ArrayList<HashMap<String, String>> itemsAry = new ArrayList<>();
    ProgressDialog pdialog;
    RelativeLayout disputeLayout, progress;
    SharedPreferences preferences;
    String accesstoken, customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_dispute_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);
        customerId = preferences.getString("customer_id", null);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        messageEdit = (EditText) findViewById(R.id.messageEdit);
        querySpin = (Spinner) findViewById(R.id.querySpin);
        orderId = (TextView) findViewById(R.id.orderId);
        price = (TextView) findViewById(R.id.price);
        itemTotal = (TextView) findViewById(R.id.itemTotal);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        createDispute = (TextView) findViewById(R.id.createDispute);
        disputeLayout = (RelativeLayout) findViewById(R.id.disputeLayout);
        progress = (RelativeLayout) findViewById(R.id.progress);
        title.setText(getString(R.string.create_dispute));
        pdialog = new ProgressDialog(CreateDispute.this);
        pdialog.setMessage(getString(R.string.pleasewait));
        pdialog.setCancelable(false);
        pdialog.setCanceledOnTouchOutside(false);
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        createDispute.setOnClickListener(this);

        order_id = getIntent().getExtras().getString("order_id");
        grand_total = getIntent().getExtras().getString("grand_total");
        currency = getIntent().getExtras().getString("currency");
        itemsAry = (ArrayList<HashMap<String, String>>) getIntent().getExtras().get("items");

        orderId.setText(order_id);
        price.setText(currency + " " + grand_total);
        itemTotal.setText(Integer.toString(itemsAry.size()));

        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_ITEM_ID, "");
        map.put(Constants.TAG_ITEM_IMAGE, "");
        map.put(Constants.TAG_ITEM_NAME, getString(R.string.all_products));
        itemsAry.add(map);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(this, itemsAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        queryList.add(getString(R.string.select_query));

        progress.setVisibility(View.VISIBLE);
        disputeLayout.setVisibility(View.GONE);
        getData();
        /*queryList.add("Not working");
        queryList.add("Wrong product");
        queryList.add("Others");*/

/*        ArrayAdapter queryadapter = new ArrayAdapter<String>(this, R.layout.spinner_item, R.id.textView, queryList);
        queryadapter.setDropDownViewResource(R.layout.spinner_item);
        querySpin.setAdapter(queryadapter);*/

       /* querySpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedQuery = queryList.get(position);

                TextView textView = ((LinearLayout) parent.getChildAt(0)).findViewById(R.id.textView);
                textView.setTextColor(getResources().getColor(R.color.textSecondary));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });*/

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView itemImage, tick;
            TextView itemName;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);
                itemImage = (ImageView) view.findViewById(R.id.itemImage);
                itemName = (TextView) view.findViewById(R.id.itemName);
                tick = (ImageView) view.findViewById(R.id.tick);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);

                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mainLay:
                        selectedItem = Items.get(getAdapterPosition()).get(Constants.TAG_ITEM_ID);
                        notifyDataSetChanged();
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
                    .inflate(R.layout.dispute_item_selection, parent, false);
            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);

            holder.itemName.setText(tempMap.get(Constants.TAG_ITEM_NAME));

            if (selectedItem.equals(tempMap.get(Constants.TAG_ITEM_ID))) {
                holder.tick.setVisibility(View.VISIBLE);
            } else {
                holder.tick.setVisibility(View.INVISIBLE);
            }
            String image = tempMap.get(Constants.TAG_ITEM_IMAGE);
            if (image != null && !image.equals("")) {
                holder.itemImage.setVisibility(View.VISIBLE);
                Picasso.get().load(image).into(holder.itemImage);
            } else {
                holder.itemImage.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NetworkReceiver.isConnected()) {
            FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
        } else {
            FantacyApplication.showSnack(this, findViewById(R.id.parentLay), false);
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
            case R.id.createDispute:
                if (selectedItem.equals("")) {
                    for (int i = 0; i < itemsAry.size(); i++)
                        if (i == 0)
                            selectedItem = itemsAry.get(i).get(Constants.TAG_ITEM_ID).trim();
                        else
                            selectedItem = selectedItem.trim() + "," + itemsAry.get(i).get(Constants.TAG_ITEM_ID).trim();
                }
                if (selectedQuery.equals(queryList.get(0))) {
                    FantacyApplication.showToast(CreateDispute.this, getString(R.string.select_query), Toast.LENGTH_SHORT);
                } else if (messageEdit.getText().toString().trim().length() == 0) {
                    FantacyApplication.showToast(CreateDispute.this, getString(R.string.type_your_message), Toast.LENGTH_SHORT);
                } else {
                    createDispute();
                }
                break;
        }
    }

    private void getData() {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADMIN_DATAS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "getDataRes=" + res);

                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONObject result = json.getJSONObject(Constants.TAG_RESULT);
                        JSONArray disputeQueries = result.optJSONArray(Constants.TAG_DISPUTE_QUERIES);
                        if (disputeQueries != null) {
                            for (int i = 0; i < disputeQueries.length(); i++) {
                                JSONObject temp = disputeQueries.getJSONObject(i);
                                String subject = DefensiveClass.optString(temp, Constants.TAG_SUBJECT);
                                queryList.add(subject);
                            }
                        }
                    }
                    if (pdialog.isShowing()) {
                        pdialog.dismiss();
                    }

                    ArrayAdapter queryadapter = new ArrayAdapter<String>(CreateDispute.this,
                            R.layout.spinner_item, R.id.textView, queryList);
                    queryadapter.setDropDownViewResource(R.layout.spinner_item);
                    querySpin.setAdapter(queryadapter);
                    querySpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view,
                                                   int position, long id) {
                            selectedQuery = queryList.get(position);
                            if (selectedQuery.equalsIgnoreCase(getString(R.string.select_query))) {
                                selectedQuery = "";
                            }

                            try {
                                TextView textView = ((LinearLayout) parent.getChildAt(0)).findViewById(R.id.textView);
                                textView.setTextColor(getResources().getColor(R.color.textSecondary));
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {

                        }
                    });
                    progress.setVisibility(View.GONE);
                    disputeLayout.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    setErrorLayout();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    setErrorLayout();
                } catch (Exception e) {
                    e.printStackTrace();
                    setErrorLayout();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getDataError: " + error.getMessage());
                setErrorLayout();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void setErrorLayout() {
        if (pdialog.isShowing()) {
            pdialog.dismiss();
        }
        progress.setVisibility(View.GONE);
        disputeLayout.setVisibility(View.GONE);
    }

    private void createDispute() {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CREATE_DISPUTE, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                Log.i(TAG, "createDisputeRes: " + res);
                try {
                    if (pdialog.isShowing()) {
                        pdialog.dismiss();
                    }
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        finish();
                        overridePendingTransition(R.anim.stay, R.anim.slide_down);
                    } else {
                        String error = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        FantacyApplication.defaultSnack(CreateDispute.this.findViewById(R.id.parentLay), error, "error");
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
                if (pdialog.isShowing()) {
                    pdialog.dismiss();
                }
                Log.e(TAG, "createDisputeError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id",customerId);
                map.put("order_id", order_id);
                map.put("item_ids", selectedItem);
                map.put("title", selectedQuery);
                map.put("message", messageEdit.getText().toString());
                Log.i(TAG, "createDisputeParams: " + map);
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
        pdialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }
}
