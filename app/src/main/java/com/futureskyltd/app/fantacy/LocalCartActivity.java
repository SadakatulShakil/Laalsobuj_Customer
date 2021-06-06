package com.futureskyltd.app.fantacy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.futureskyltd.app.Adapter.LocalCartAdapter;
import com.futureskyltd.app.ApiPojo.LocalCartModel;
import com.futureskyltd.app.external.CustomTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocalCartActivity extends AppCompatActivity {
    private ImageView backBtn;
    private TextView title;
    private TextView txtPriceWithQnty, totalProductPrice;
    private RecyclerView localCartRecyclerView;
    private String localCartJson;
    private ArrayList<String> localCartModelArrayList;
    private ArrayList<Map<String, String>> localCartArrayList;
    private Map<String, Map<String, String>> getLocalCartMap;
    private LocalCartAdapter localCartAdapter;
    int price= 0;
    private String buyAblePrice;
    private String nodeId;
    private CustomTextView checkOutBtn;
    private SharedPreferences preferences;
    private RelativeLayout mainLay, nullLay;
    public static final String TAG = "Local";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_cart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        localCartRecyclerView = findViewById(R.id.localCartRecyclerView);
        txtPriceWithQnty = findViewById(R.id.txtPrice);
        totalProductPrice = findViewById(R.id.itemPrice);
        checkOutBtn = findViewById(R.id.checkOutBtn);
        mainLay = findViewById(R.id.mainLay);
        nullLay = findViewById(R.id.nullLay);
        title.setText(getString(R.string.cart));
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        localCartJson = preferences.getString("localCart", null);
        //localCartJson = intent.getStringExtra("localCartMap");
        if(localCartJson != null){
            mainLay.setVisibility(View.VISIBLE);
            nullLay.setVisibility(View.GONE);

            getLocalCartMap = new Gson().fromJson(
                    localCartJson, new TypeToken<HashMap<String, Map<String, String>>>() {}.getType()
            );

            Log.d(TAG, "onCreate: "+ getLocalCartMap.size()+"...."+FragmentMainActivity.cartCount);


            Iterator it = getLocalCartMap.entrySet().iterator();

            while (it.hasNext()){
                Map.Entry pairs = (Map.Entry) it.next();
                Log.d(TAG, "onTest: " + pairs.getKey()+"...."+pairs.getValue());
            }

            localCartModelArrayList = new ArrayList<>(getLocalCartMap.keySet());
            Log.d(TAG, "onTest1: " +localCartModelArrayList);
            for(String parent : localCartModelArrayList){
                nodeId = parent;
                Log.d(TAG, "onTest2: " + parent);
            }

            localCartArrayList = new ArrayList<Map<String, String>>(getLocalCartMap.values());
            for(Map<String, String> data : localCartArrayList){
                Log.d(TAG, "onTest3: " + data.get("item_title"));
                Log.d(TAG, "onTest3: " + localCartArrayList.size());

                price  += Integer.parseInt(data.get("item_price"));
            }
            buyAblePrice = String.valueOf(price);
            Log.d(TAG, "onPrice: "+ price);

            txtPriceWithQnty.setText("Price "+"("+localCartArrayList.size()+"items)");
            totalProductPrice.setText("৳ "+buyAblePrice);
        }else {
            mainLay.setVisibility(View.GONE);
             nullLay.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "onCreateArraySize: " + localCartJson);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(LocalCartActivity.this, FragmentMainActivity.class);
                startActivity(intent1);
                finish();
            }
        });

        checkOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i3 = new Intent(LocalCartActivity.this, SignInActivity.class);
                startActivity(i3);
                finish();
            }
        });
        localCartAdapter = new LocalCartAdapter(LocalCartActivity.this, localCartArrayList);
        localCartRecyclerView.setLayoutManager(new LinearLayoutManager(LocalCartActivity.this));
        localCartRecyclerView.setAdapter(localCartAdapter);
        localCartAdapter.notifyDataSetChanged();
    }
}