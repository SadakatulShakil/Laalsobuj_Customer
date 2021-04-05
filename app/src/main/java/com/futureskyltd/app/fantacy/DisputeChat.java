package com.futureskyltd.app.fantacy;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.futureskyltd.app.external.ImagePicker;
import com.futureskyltd.app.external.ReverseRecyclerOnScrollListener;
import com.futureskyltd.app.helper.ImageCompression;
import com.futureskyltd.app.helper.ImageStorage;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hitasoft on 10/7/17.
 */

public class DisputeChat extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getSimpleName();
    public static boolean imageUploading = false;
    ImageView backBtn, itemImage, sendBtn, attachBtn;
    TextView itemName, id, amount, accept, title;
    EditText chatEdit;
    RecyclerView recyclerView;
    RelativeLayout progressLay, nullLay, bottomLay;
    ProgressBar loadmoreprogress;
    ImageView nullImage;
    TextView nullText;
    RecyclerViewAdapter recyclerViewAdapter;
    LinearLayoutManager linearLayoutManager;
    HashMap<String, String> detailMap = new HashMap<String, String>();
    ArrayList<HashMap<String, String>> backup = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> chatAry = new ArrayList<HashMap<String, String>>();
    String disputeId, from = "";
    Boolean firstload = true;
    ProgressDialog pd;
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
        setContentView(R.layout.dispute_chat_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        itemImage = (ImageView) findViewById(R.id.itemImage);
        attachBtn = (ImageView) findViewById(R.id.attachBtn);
        title = (TextView) findViewById(R.id.title);
        itemName = (TextView) findViewById(R.id.itemName);
        id = (TextView) findViewById(R.id.id);
        amount = (TextView) findViewById(R.id.amount);
        accept = (TextView) findViewById(R.id.accept);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        sendBtn = (ImageView) findViewById(R.id.sendBtn);
        chatEdit = (EditText) findViewById(R.id.chatEdit);
        nullLay = (RelativeLayout) findViewById(R.id.nullLay);
        bottomLay = (RelativeLayout) findViewById(R.id.bottomLay);
        nullImage = (ImageView) findViewById(R.id.nullImage);
        nullText = (TextView) findViewById(R.id.nullText);
        progressLay = (RelativeLayout) findViewById(R.id.progress);
        loadmoreprogress = (ProgressBar) findViewById(R.id.loadmoreprogress);

        backBtn.setVisibility(View.VISIBLE);
        attachBtn.setVisibility(View.VISIBLE);


        title.setVisibility(View.VISIBLE);

        pd = new ProgressDialog(DisputeChat.this);
        pd.setMessage(getString(R.string.pleasewait));
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);

        title.setText(getString(R.string.dispute));
        disputeId = (String) getIntent().getExtras().get("disputeId");
        from = (String) getIntent().getExtras().get("from");
        backBtn.setOnClickListener(this);
        accept.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
        attachBtn.setOnClickListener(this);

        switch (from) {
            case "close":
            case "Closed":
                accept.setVisibility(View.GONE);
                attachBtn.setVisibility(View.GONE);
                bottomLay.setVisibility(View.GONE);
                break;
            case "Accepeted":
            case "Accepted":
                accept.setText(getString(R.string.resolve));
                accept.setVisibility(View.VISIBLE);
                attachBtn.setVisibility(View.VISIBLE);
                bottomLay.setVisibility(View.VISIBLE);
                break;
            default:
                accept.setText(getString(R.string.cancel));
                accept.setVisibility(View.VISIBLE);
                attachBtn.setVisibility(View.VISIBLE);
                bottomLay.setVisibility(View.VISIBLE);
                break;
        }

        getChats(0);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewAdapter = new RecyclerViewAdapter(this, chatAry);
        recyclerView.setAdapter(recyclerViewAdapter);

        mScrollListener = new ReverseRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                loadmoreprogress.setVisibility(View.VISIBLE);
                getChats(current_page * Constants.OVERALL_LIMIT);
                Log.v("offset:", "On offset" + (Constants.OVERALL_LIMIT * current_page));
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);
        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getChats(final int offset) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_DISPUTE_CHAT, new Response.Listener<String>() {
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

                        String dispute_id = DefensiveClass.optString(result, Constants.TAG_DISPUTE_ID);
                        String dispute_status = DefensiveClass.optString(result, Constants.TAG_STATUS);
                        String currency = DefensiveClass.optString(result, Constants.TAG_CURRENCY);
                        String dispute_amount = DefensiveClass.optString(result, Constants.TAG_PRICE);
                        String item_title = DefensiveClass.optString(result, Constants.TAG_ITEM_TITLE);
                        String item_id = DefensiveClass.optString(result, Constants.TAG_ITEM_ID);
                        String image = DefensiveClass.optString(result, Constants.TAG_IMAGE);
                        String dispute_date = DefensiveClass.optString(result, Constants.TAG_DISPUTE_DATE);
                        String shop_id = DefensiveClass.optString(result, Constants.TAG_SHOP_ID);
                        String shop_name = DefensiveClass.optString(result, Constants.TAG_SHOP_NAME);
                        String shop_image = DefensiveClass.optString(result, Constants.TAG_SHOP_IMAGE);
                        String last_replied = DefensiveClass.optString(result, Constants.TAG_LAST_REPLIED);
                        String last_read = DefensiveClass.optString(result, Constants.TAG_LAST_READ);

                        detailMap.put(Constants.TAG_DISPUTE_ID, dispute_id);
                        detailMap.put(Constants.TAG_STATUS, dispute_status);
                        detailMap.put(Constants.TAG_CURRENCY, currency);
                        detailMap.put(Constants.TAG_DISPUTE_AMOUNT, dispute_amount);
                        detailMap.put(Constants.TAG_ITEM_TITLE, item_title);
                        detailMap.put(Constants.TAG_ITEM_ID, item_id);
                        detailMap.put(Constants.TAG_IMAGE, image);
                        detailMap.put(Constants.TAG_DISPUTE_DATE, dispute_date);
                        detailMap.put(Constants.TAG_SHOP_ID, shop_id);
                        detailMap.put(Constants.TAG_SHOP_NAME, shop_name);
                        detailMap.put(Constants.TAG_SHOP_IMAGE, shop_image);
                        detailMap.put(Constants.TAG_LAST_REPLIED, last_replied);
                        detailMap.put(Constants.TAG_LAST_READ, last_read);

                        JSONArray messages = result.getJSONArray(Constants.TAG_MESSAGES);
                        for (int p = 0; p < messages.length(); p++) {
                            JSONObject pobj = messages.getJSONObject(p);

                            HashMap<String, String> pmap = new HashMap<>();

                            String type = DefensiveClass.optString(pobj, Constants.TAG_TYPE);
                            String attachment = DefensiveClass.optString(pobj, Constants.TAG_ATTACHMENT);
                            String message = DefensiveClass.optString(pobj, Constants.TAG_MESSAGE);
                            String user_name = DefensiveClass.optString(pobj, Constants.TAG_USER_NAME);
                            String full_name = DefensiveClass.optString(pobj, Constants.TAG_FULL_NAME);
                            String user_id = DefensiveClass.optString(pobj, Constants.TAG_USER_ID);
                            String user_image = DefensiveClass.optString(pobj, Constants.TAG_USER_IMAGE);
                            String chat_date = DefensiveClass.optString(pobj, Constants.TAG_CHAT_DATE);

                            pmap.put(Constants.TAG_TYPE, type);
                            pmap.put(Constants.TAG_ATTACHMENT, attachment);
                            pmap.put(Constants.TAG_MESSAGE, message);
                            pmap.put(Constants.TAG_USER_NAME, user_name);
                            pmap.put(Constants.TAG_FULL_NAME, full_name);
                            pmap.put(Constants.TAG_USER_ID, user_id);
                            pmap.put(Constants.TAG_USER_IMAGE, user_image);
                            pmap.put(Constants.TAG_CHAT_DATE, chat_date);

                            chatAry.add(pmap);
                        }

                        if (mScrollListener != null) {
                            mScrollListener.setLoading(false);
                        }
                    }
                    recyclerViewAdapter.notifyDataSetChanged();
                    if (firstload) {
                        firstload = false;
                        recyclerView.scrollToPosition(0);
                        itemName.setText(detailMap.get(Constants.TAG_ITEM_TITLE));
                        amount.setText(detailMap.get(Constants.TAG_CURRENCY) + " " + detailMap.get(Constants.TAG_DISPUTE_AMOUNT));
                        id.setText(getString(R.string.id) + " : " + detailMap.get(Constants.TAG_DISPUTE_ID));
                        if (!detailMap.get(Constants.TAG_IMAGE).equals("")) {
                            Picasso.get().load(detailMap.get(Constants.TAG_IMAGE)).into(itemImage);
                        }
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
                DisputeChat.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (chatAry.size() == 0) {
                            progressLay.setVisibility(View.VISIBLE);
                        }
                    }
                });
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("limit", Integer.toString(Constants.OVERALL_LIMIT));
                map.put("offset", Integer.toString(offset));
                map.put("dispute_id", disputeId);
                Log.v(TAG, "getChatsParams=" + map);
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
        if (chatAry.size() == 0) {
            progressLay.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
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
        if (imageUploading) {
            pd.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
        if (imageUploading) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    private void cancelDispute() {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CANCEL_DISPUTE, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    Log.v(TAG, "cancelDisputeRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        Dispute.refreshBoth = true;
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
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.e(TAG, "getChatsError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("dispute_id", detailMap.get(Constants.TAG_DISPUTE_ID));
                if (from.equals("active"))
                    map.put("type", "cancel");
                else
                    map.put("type", "resolve");

                Log.v(TAG, "cancelDisputeParams=" + map);
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

    private void sendChat(final String message, final String image) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SEND_DISPUTE_MESSAGE, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "sendChatRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        Log.i(TAG, "sendChat: Sent Successfully");
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
                map.put("user_id", customerId);
                map.put("dispute_id", detailMap.get(Constants.TAG_DISPUTE_ID));
                map.put("message", message);
                map.put("attachment", image);
                Log.v(TAG, "sendChatParams=" + map);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == 234) {
            Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
            ImageStorage imageStorage = new ImageStorage(this);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
            String imageStatus = imageStorage.saveToSdCard(bitmap, timestamp + ".jpg");
            if (imageStatus.equals("success")) {
                File file = imageStorage.getImage(timestamp + ".jpg");
                String filepath = file.getAbsolutePath();
                Log.i("selectedImageFile", "selectedImageFile: " + filepath);
                ImageCompression imageCompression = new ImageCompression(DisputeChat.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        imageUploading = true;
                        new uploadImage().execute(imagePath);
                    }
                };
                imageCompression.execute(filepath);
            } else {
                FantacyApplication.showToast(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(this, "Select your image:");
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            requestPermission(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.camera_permission_description, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + getApplication().getPackageName()));
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            DisputeChat.this.startActivity(intent);
                        }
                    }
                }
                break;
        }
    }

    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(DisputeChat.this, permissions, requestCode);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.accept:
                final Dialog dialog = new Dialog(DisputeChat.this);
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
                if (from.equals("Accepeted")) {
                    title.setText(getString(R.string.really_resolve_dispute));
                } else {
                    title.setText(getString(R.string.really_cancel_dispute));
                }

                yes.setText(getString(R.string.yes));
                no.setText(getString(R.string.no));

                no.setVisibility(View.VISIBLE);

                yes.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        cancelDispute();
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
            case R.id.attachBtn:
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
                } else {
                    ImagePicker.pickImage(this, "Select your image:");
                }
                break;
            case R.id.sendBtn:
                if (chatEdit.getText().toString().trim().length() == 0) {
                    FantacyApplication.showToast(this, getString(R.string.text_error), Toast.LENGTH_SHORT);

                } else if (chatEdit.getText().toString().trim().length() > 0) {
                    sendChat(chatEdit.getText().toString().trim(), "");

                    HashMap<String, String> pmap = new HashMap<>();
                    Dispute.refreshBoth = true;
                    pmap.put(Constants.TAG_TYPE, "message");
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

                    recyclerViewAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(0);
                }
                break;
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item, parent, false);

            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {
            SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
            String accesstoken = preferences.getString("TOKEN", null);
            String customerId = preferences.getString("customer_id", null);


            HashMap<String, String> tempMap = Items.get(position);

            holder.leftLay.setVisibility(View.GONE);
            holder.rightLay.setVisibility(View.GONE);
            holder.dateLay.setVisibility(View.GONE);
            if (tempMap.get(Constants.TAG_USER_ID).equals(customerId)) {
                holder.rightLay.setVisibility(View.VISIBLE);
                if (tempMap.get(Constants.TAG_TYPE).equals("attachment")) {
                    holder.rightMsg.setVisibility(View.GONE);
                    holder.rightAttach.setVisibility(View.VISIBLE);
                    Picasso.get().load(tempMap.get(Constants.TAG_ATTACHMENT)).into(holder.rightAttach);
                } else {
                    holder.rightMsg.setVisibility(View.VISIBLE);
                    holder.rightAttach.setVisibility(View.GONE);
                    holder.rightMsg.setText(tempMap.get(Constants.TAG_MESSAGE));
                }

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

                if (tempMap.get(Constants.TAG_TYPE).equals("attachment")) {
                    holder.leftMsg.setVisibility(View.GONE);
                    holder.leftAttach.setVisibility(View.VISIBLE);
                    Picasso.get().load(tempMap.get(Constants.TAG_ATTACHMENT)).into(holder.leftAttach);
                } else {
                    holder.leftMsg.setVisibility(View.VISIBLE);
                    holder.leftAttach.setVisibility(View.GONE);
                    holder.leftMsg.setText(tempMap.get(Constants.TAG_MESSAGE));
                }
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

        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView date, leftMsg, rightMsg, leftTime, rightTime;
            ImageView leftImage, rightImage, leftAttach, rightAttach;
            RelativeLayout dateLay, leftLay, rightLay;

            public MyViewHolder(View view) {
                super(view);

                date = (TextView) view.findViewById(R.id.date);
                leftMsg = (TextView) view.findViewById(R.id.leftMsg);
                leftAttach = (ImageView) view.findViewById(R.id.left_image);
                rightAttach = (ImageView) view.findViewById(R.id.right_image);
                rightMsg = (TextView) view.findViewById(R.id.rightMsg);
                leftTime = (TextView) view.findViewById(R.id.leftTime);
                rightTime = (TextView) view.findViewById(R.id.rightTime);
                leftImage = (ImageView) view.findViewById(R.id.leftImage);
                rightImage = (ImageView) view.findViewById(R.id.rightImage);
                dateLay = (RelativeLayout) view.findViewById(R.id.dateLay);
                leftLay = (RelativeLayout) view.findViewById(R.id.leftLay);
                rightLay = (RelativeLayout) view.findViewById(R.id.rightLay);

                rightAttach.setOnClickListener(this);
                leftAttach.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.right_image:
                    case R.id.left_image:
                        ArrayList<HashMap<String, String>> imagesAry = new ArrayList<HashMap<String, String>>();
                        HashMap<String, String> map = new HashMap<>();
                        map.put(Constants.TAG_IMAGE, Items.get(getAdapterPosition()).get(Constants.TAG_ATTACHMENT));
                        imagesAry.add(map);
                        Intent intent = new Intent(DisputeChat.this, FullImage.class);
                        intent.putExtra("from", "dispute");
                        intent.putExtra("position", "0");
                        intent.putExtra("images", imagesAry);
                        Pair<View, String> bodyPair = Pair.create(view, "dispute");
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(DisputeChat.this, bodyPair);
                        ActivityCompat.startActivity(DisputeChat.this, intent, options.toBundle());
                        break;
                }
            }
        }
    }

    class uploadImage extends AsyncTask<String, Integer, Integer> {

        JSONObject jsonobject = null;
        String Json = "";
        String status;
        String exsistingFileName = "";

        @Override
        protected Integer doInBackground(String... imgpath) {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            StringBuilder builder = new StringBuilder();
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            String urlString = Constants.IMAGE_UPLOAD_URL;
            try {
                exsistingFileName = imgpath[0];
                Log.v(TAG, "existingFileName= " + exsistingFileName);
                FileInputStream fileInputStream = new FileInputStream(new File(exsistingFileName));
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"type\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("dispute");
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                        + exsistingFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                Log.e("MediaPlayer", "Headers are written");
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                Log.v(TAG, "buffer" + buffer);

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    Log.v(TAG, "bytesRead" + bytesRead);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;
                Log.v("in", "" + in);
                while ((inputLine = in.readLine()) != null)
                    builder.append(inputLine);

                Log.e("MediaPlayer", "File is written");
                fileInputStream.close();
                Json = builder.toString();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e("MediaPlayer", "error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e("MediaPlayer", "error: " + ioe.getMessage(), ioe);
            }
            try {
                inStream = new DataInputStream(conn.getInputStream());
                String str;
                while ((str = inStream.readLine()) != null) {
                    Log.e("MediaPlayer", "Server Response" + str);
                }
                inStream.close();
            } catch (IOException ioex) {
                Log.e("MediaPlayer", "error: " + ioex.getMessage(), ioex);
            }
            try {
                jsonobject = new JSONObject(Json);
                Log.v(TAG, "UploadImageJson=" + Json);
                status = jsonobject.getString("status");
                if (status.equals("true")) {
                    JSONObject image = jsonobject.getJSONObject("result");
                    String name = DefensiveClass.optString(image, "name");
                    final String viewUrl = DefensiveClass.optString(image, "image");

                    if (!viewUrl.equals("")) {
                        sendChat("", name);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                HashMap<String, String> pmap = new HashMap<>();
                                Dispute.refreshBoth = true;
                                pmap.put(Constants.TAG_TYPE, "attachment");
                                pmap.put(Constants.TAG_MESSAGE, "");
                                pmap.put(Constants.TAG_USER_NAME, GetSet.getUserName());
                                pmap.put(Constants.TAG_FULL_NAME, GetSet.getFullName());
                                pmap.put(Constants.TAG_USER_ID, GetSet.getUserId());
                                pmap.put(Constants.TAG_ATTACHMENT, viewUrl);
                                pmap.put(Constants.TAG_USER_IMAGE, GetSet.getImageUrl());
                                pmap.put(Constants.TAG_CHAT_DATE, String.valueOf(System.currentTimeMillis() / 1000L));

                                backup.clear();
                                backup.addAll(chatAry);
                                chatAry.clear();
                                chatAry.add(pmap);
                                chatAry.addAll(backup);
                                chatEdit.setText("");

                                recyclerViewAdapter.notifyDataSetChanged();
                                recyclerView.scrollToPosition(0);
                            }
                        });
                    }
                }

            } catch (JSONException e) {
                status = "false";
                e.printStackTrace();
            } catch (NullPointerException e) {
                status = "false";
                e.printStackTrace();
            } catch (Exception e) {
                status = "false";
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!DisputeChat.this.isFinishing()) {
                pd.show();
            }
        }

        @Override
        protected void onPostExecute(Integer unused) {
            try {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                imageUploading = false;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
}

