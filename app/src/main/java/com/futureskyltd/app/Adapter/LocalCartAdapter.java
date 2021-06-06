package com.futureskyltd.app.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.futureskyltd.app.fantacy.LocalCartActivity;
import com.futureskyltd.app.fantacy.R;
import com.futureskyltd.app.fantacy.SignInActivity;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LocalCartAdapter extends RecyclerView.Adapter<LocalCartAdapter.viewHolder> {
    private Context context;
    private ArrayList<Map<String, String>> localCartArrayList = new ArrayList<>();
    private HashMap<String, Map<String, String >> updateLocalCart;
    private String nodeId;
    private SharedPreferences preferences;
    public static final String TAG = "context";
    private String jsonMap;

    public LocalCartAdapter(Context context, ArrayList<Map<String, String>> localCartArrayList) {
        this.context = context;
        this.localCartArrayList = localCartArrayList;
    }

    @NonNull
    @Override
    public LocalCartAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items, parent, false);
        return new LocalCartAdapter.viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LocalCartAdapter.viewHolder holder, int position) {
        holder.itemName.setText(localCartArrayList.get(position).get("item_title"));
        holder.itemPrice.setText("৳ "+localCartArrayList.get(position).get("item_price"));
        holder.shipping.setText(localCartArrayList.get(position).get("shipping_time"));
        holder.quantity.setText(localCartArrayList.get(position).get("qty"));
        String size = localCartArrayList.get(position).get("size");
        if (size.equals("")) {
            holder.itemSize.setText("Size"+ ": N/A");
        } else {
            holder.itemSize.setText("Size"+ ": " + size);
        }

        Picasso.get().load(R.drawable.all_store).into(holder.itemImage);

        holder.qtyPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i1 = new Intent(context, SignInActivity.class);
                context.startActivity(i1);

            }
        });
        holder.qtyMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i2 = new Intent(context, SignInActivity.class);
                context.startActivity(i2);

            }
        });

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Toast.makeText(context, localCartArrayList.get(position).get("item_id"), Toast.LENGTH_SHORT).show();
                    String id = localCartArrayList.get(position).get("item_id");
                    /*Map<String, String> pos = localCartArrayList.get(position);
                    String itemPosition = String.valueOf(pos);*/
                Log.d(TAG, "onClick: "+ localCartArrayList.size());
                for(int i = 0; i<localCartArrayList.size(); i++){
                    if(id.equals(localCartArrayList.get(i).get("item_id"))){
                        localCartArrayList.remove(i);
                        break;
                    }
                }
                Log.d(TAG, "onClick: "+"1481246");
                Log.d(TAG, "onClickList: "+ localCartArrayList);

               /* JSONArray jsonArray = new JSONArray();
                for (int i=0; i < localCartArrayList.size(); i++) {
                    JSONObject obj = new JSONObject();
                    JSONObject objWithId = new JSONObject();
                    try {
                        obj.put("item_id", localCartArrayList.get(i).get("item_id"));
                        obj.put("item_title", localCartArrayList.get(i).get("item_title"));
                        obj.put("item_price", localCartArrayList.get(i).get("item_price"));
                        obj.put("size", localCartArrayList.get(i).get("size"));
                        obj.put("item_main_price", localCartArrayList.get(i).get("item_main_price"));
                        obj.put("qty", localCartArrayList.get(i).get("qty"));
                        obj.put("shipping_time", localCartArrayList.get(i).get("shipping_time"));

                        //String object = obj.toString().replace("[",)
                        objWithId.put(localCartArrayList.get(i).get("item_id"), obj);
                    } catch (JSONException e) {
                        Log.d(TAG, "onClick: "+e.getMessage());
                    }
                    jsonArray.put(objWithId);
                }
                Log.d(TAG, "onClickArray: " +jsonArray.toString());*/

                Map<String, Map<String, String>> product = new HashMap<>();
                for (Map<String, String> hashProduct : localCartArrayList ) {
                   product.put(hashProduct.get("item_id"),hashProduct);

                    Log.d(TAG, "onClick: "+ hashProduct);
                }
                jsonMap = new Gson().toJson(product);
                Log.d(TAG, "onClickMap: " + jsonMap);

                /*preferences = context.getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                preferences.edit().putString("localCart", null).apply();*/
                preferences = context.getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                preferences.edit().putString("localCart", jsonMap).apply();

                Intent intent = new Intent(context, LocalCartActivity.class);
                context.startActivity(intent);
                ((Activity)context).finish();
               /* Map<String, Map<String, Map<String, String>>> productMap = new HashMap<>();
                for (int i =0 ; i<localCartArrayList.size(); i++) {
                    productMap.put(localCartArrayList.get(i).get("item_id"), product);
                }
                jsonMap = new Gson().toJson(productMap);

                Log.d(TAG, "onClickJson: " + jsonMap);*/
            }
        });
    }


    @Override
    public int getItemCount() {
        return localCartArrayList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        private ImageView itemImage, qtyMinus, qtyPlus, cancel;
        private TextView itemName, itemPrice, itemSize, shipping, quantity;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemSize = itemView.findViewById(R.id.itemSize);
            shipping = itemView.findViewById(R.id.shipping);
            quantity = itemView.findViewById(R.id.quantity);
            qtyPlus = itemView.findViewById(R.id.qtyPlus);
            qtyMinus = itemView.findViewById(R.id.qtyMinus);
            cancel = itemView.findViewById(R.id.cancel);
        }
    }
}
