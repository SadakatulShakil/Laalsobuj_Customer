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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.helper.LocaleManager;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hitasoft on 10/7/17.
 */

public class LocationNCurrency extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = LocationNCurrency.class.getSimpleName();
    ImageView backBtn;
    TextView title;
    String from = "";
    public static String selectedLang = "", selectedLangCode = "", selectedCurrency = "", selectedID = "";
    public static boolean changedCurrency = false;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    //String[] languages, langCode;
    List<String> langCode;
    SharedPreferences pref;
    static SharedPreferences.Editor editor;
    ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_currency);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        from = getIntent().getExtras().getString("from");

        if (from.equals("lang")) {
            pref = getApplicationContext().getSharedPreferences("LangPref", MODE_PRIVATE);
            editor = pref.edit();
            //    selectedLang = pref.getString("language", Constants.LANGUAGE);
            title.setText(getString(R.string.language));
            data = (ArrayList<HashMap<String, String>>) getIntent().getExtras().get("data");
            Log.d(TAG, "onCreate: " + data);
        } else {
            title.setText(getString(R.string.currency));
            data = (ArrayList<HashMap<String, String>>) getIntent().getExtras().get("data");
        }

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        backBtn.setOnClickListener(this);

        //  languages = getResources().getStringArray(R.array.languages);
        langCode = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.languageCode)));

        Log.d(TAG, "onLangCode: " + langCode);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDivider = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
        itemDivider.setDrawable(getResources().getDrawable(R.drawable.horizontal_line));
        recyclerView.addItemDecoration(itemDivider);

        recyclerViewAdapter = new RecyclerViewAdapter(this, data);
        recyclerView.setAdapter(recyclerViewAdapter);

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView icon;
            TextView name;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                icon = (ImageView) view.findViewById(R.id.icon);
                name = (TextView) view.findViewById(R.id.name);
                mainLay = (RelativeLayout) view.findViewById(R.id.mainLay);
                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.mainLay:
                        if (from.equals("lang")) {
                            if (langCode.contains(Items.get(getAdapterPosition()).get(Constants.TAG_CODE))) {
                                selectedLang = Items.get(getAdapterPosition()).get(Constants.TAG_NAME);
                                selectedLangCode = Items.get(getAdapterPosition()).get(Constants.TAG_CODE);
                                editor.putString(Constants.TAG_LANGUAGE, selectedLang);
                                editor.putString(Constants.TAG_CODE, selectedLangCode);
                                editor.commit();
                                Log.d(TAG, "onClick: " + selectedLang+"="+selectedLangCode);
                                setSettings();
                            } else {
                                FantacyApplication.showToast(context, getString(R.string.language_not_found), Toast.LENGTH_SHORT);
                            }
                        } else {
                            selectedID = Items.get(getAdapterPosition()).get(Constants.TAG_CURRENCY_ID);
                            selectedCurrency = Items.get(getAdapterPosition()).get(Constants.TAG_CURRENCY_CODE);

                            setSettings();
                        }
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
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_first_row, parent, false);
            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {
            holder.icon.setImageResource(R.drawable.tick_color);
            if (from.equals("lang")) {
                holder.name.setText(Items.get(position).get(Constants.TAG_NAME));
                if (Items.get(position).get(Constants.TAG_CODE).equals(selectedLangCode)) {
                    holder.icon.setVisibility(View.VISIBLE);
                } else {
                    holder.icon.setVisibility(View.GONE);
                }
            } else {
                holder.name.setText(Items.get(position).get(Constants.TAG_CURRENCY_CODE));
                if (Items.get(position).get(Constants.TAG_CURRENCY_ID).equals(selectedID)) {
                    holder.icon.setVisibility(View.VISIBLE);
                } else {
                    holder.icon.setVisibility(View.GONE);
                }
            }

        }

        @Override
        public int getItemCount() {
            return Items.size();
        }
    }

    /**
     * function for change the selected language
     **/
    public void setLocale(String lang) {
        LocaleManager.setNewLocale(this, lang);
        Intent refresh = new Intent(LocationNCurrency.this, FragmentMainActivity.class);
        startActivity(refresh);
        finish();
    }

    private void setSettings() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);

        final ProgressDialog dialog = new ProgressDialog(LocationNCurrency.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SET_SETTINGS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Log.d(TAG, "setSettingsRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        if (from.equals("lang")) {
                            setLocale(selectedLangCode);
                        }
                        Intent refresh = new Intent(LocationNCurrency.this, FragmentMainActivity.class);
                        startActivity(refresh);
                        finish();
                    } else {
                        Toast.makeText(LocationNCurrency.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                if (from.equals("lang")) {
                    map.put("language", LocationNCurrency.selectedLangCode);
                } else {
                    map.put("currency_id", LocationNCurrency.selectedID);
                }
                map.put("user_id", customerId);
                Log.d(TAG, "setSettingsParams=" + map);
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
        }
    }
}
