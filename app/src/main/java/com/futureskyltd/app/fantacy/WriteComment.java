package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hitasoft on 25/5/17.
 */

public class WriteComment extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    String testString="";
    private final String TAG = this.getClass().getSimpleName();
    ImageView backBtn, itemImage;
    TextView title, writeComment, itemName;
    String itemId = "", itemTitle = "", image = "", from = "", to = "", comment = "", commentId = "";
    MultiAutoCompleteTextView editText;
    SuggestAdapter suggestAdapter;
    private ArrayList<String> uNames;
    TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            if (text.lastIndexOf("@") > text.lastIndexOf(" ")) {
                String tag = text.substring(text.lastIndexOf("@"), text.length());
                // search for @mention...
                Log.v("@tag", "@tag=" + tag);
                getUserData(tag.replace("@", ""));
            }

            if (text.lastIndexOf("#") > text.lastIndexOf(" ")) {
                String tag = text.substring(text.lastIndexOf("#"), text.length());
                // search for #hashtag...
                Log.v("#tag", "#tag=" + tag);
                getHashTag(tag.replace("#", ""));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_comment);

        overridePendingTransition(R.anim.slide_up, R.anim.stay);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        title = (TextView) findViewById(R.id.title);
        writeComment = (TextView) findViewById(R.id.writeComment);

        itemImage = (ImageView) findViewById(R.id.itemImage);
        itemName = (TextView) findViewById(R.id.itemName);
        editText = (MultiAutoCompleteTextView) findViewById(R.id.editText);

        backBtn.setImageResource(R.drawable.close);
        writeComment.setText(getString(R.string.post));

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        writeComment.setVisibility(View.VISIBLE);

        InputFilter filterArray = new InputFilter.LengthFilter(180);
//        editText.setFilters(new InputFilter[]{FantacyApplication.EMOJI_FILTER, filterArray});
        backBtn.setOnClickListener(this);
        writeComment.setOnClickListener(this);

        from = (String) getIntent().getExtras().get("from");
        to = (String) getIntent().getExtras().get("to");
        itemId = (String) getIntent().getExtras().get("itemId");
        itemTitle = (String) getIntent().getExtras().get("itemTitle");
        image = (String) getIntent().getExtras().get("image");

        if (to.equals("add")) {
            title.setText(getString(R.string.write_a_comment));
        } else {
            comment = (String) getIntent().getExtras().get("comment");
            commentId = (String) getIntent().getExtras().get("commentId");
            title.setText(getString(R.string.edit_a_comment));
            editText.setText(Html.fromHtml(comment).toString());
            editText.setSelection(Html.fromHtml(comment).toString().length());
        }

        itemName.setText(itemTitle);
        if (image != null && !image.equals("")) {
            Picasso.get().load(image).into(itemImage);
        }
        List<String> uNames = new ArrayList<String>();
        final ArrayAdapter<String> TopicName = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, uNames);
        editText.setAdapter(TopicName);
        //  editText.setThreshold(2);
        editText.setTokenizer(new TagTokenizer());
        editText.addTextChangedListener(textWatcher);

        FantacyApplication.setupUI(this, findViewById(R.id.parentLay));

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
    }

    private void getUserData(final String key) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_AT_USER_TAG, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
                List<String> uNames = new ArrayList<String>();
                Log.v(TAG, "getUserDataRes=" + res);
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray result = json.getJSONArray("result");

                        for (int i = 0; i < result.length(); i++) {
                            HashMap<String, String> data = new HashMap<String, String>();

                            JSONObject temp = result.getJSONObject(i);

                            String user_id = DefensiveClass.optString(temp, Constants.TAG_USER_ID);
                            String full_name = DefensiveClass.optString(temp, Constants.TAG_FULL_NAME);
                            String user_name = DefensiveClass.optString(temp, Constants.TAG_USER_NAME);
                            String user_image = DefensiveClass.optString(temp, Constants.TAG_USER_IMAGE);
                            String username_url = DefensiveClass.optString(temp, Constants.TAG_USERNAME_URL);

                            data.put(Constants.TAG_USER_ID, user_id);
                            data.put(Constants.TAG_FULL_NAME, full_name);
                            data.put(Constants.TAG_USER_NAME, user_name);
                            data.put(Constants.TAG_USER_IMAGE, user_image);
                            data.put(Constants.TAG_TYPE, "at");
                            data.put(Constants.TAG_USERNAME_URL, username_url);

                            if (!user_id.equals(GetSet.getUserId())) {
                                aList.add(data);
                                uNames.add(user_name);
                            }

                        }
                        if (aList.size() > 0) {
                            suggestAdapter = new SuggestAdapter(WriteComment.this, R.layout.atmention_layout, uNames, aList);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (editText != null) {
                                        editText.setAdapter(suggestAdapter);
                                        editText.showDropDown();
                                    }
                                }
                            });
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getUserDataError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("key", key);
                Log.v(TAG, "getUserDataParams=" + map);
                return map;
            }
        };
        FantacyApplication.getInstance().getRequestQueue().cancelAll("@tag");
        FantacyApplication.getInstance().addToRequestQueue(req, "@tag");
    }

    private void getHashTag(final String key) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_HASHTAG, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
                uNames = new ArrayList<String>();
                Log.v(TAG, "getHashTagRes" + res);
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        JSONArray result = json.optJSONArray("result");

                        if (result != null) {
                            for (int i = 0; i < result.length(); i++) {

                                String tag = result.getString(i);

                                HashMap<String, String> data = new HashMap<String, String>();
                                data.put("tag", tag);
                                data.put(Constants.TAG_TYPE, "hash");

                                if (!aList.contains(data)) {
                                    uNames.add(tag);
                                    aList.add(data);
                                }
                            }
                        }

                        if (aList.size() > 0) {
                            suggestAdapter = new SuggestAdapter(WriteComment.this, R.layout.atmention_layout, uNames, aList);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (editText != null) {
                                        editText.setAdapter(suggestAdapter);
                                        editText.showDropDown();

                                    }
                                }
                            });
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getHashTagError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("key", key);
                Log.v(TAG, "getHashTagParams" + map);
                return map;
            }
        };
        FantacyApplication.getInstance().getRequestQueue().cancelAll("#tag");
        FantacyApplication.getInstance().addToRequestQueue(req, "#tag");
    }

    private void sendComment() {
        final ProgressDialog dialog = new ProgressDialog(WriteComment.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final String CallingAPI;
        if (from != null && from.equals("feedallComment"))
            CallingAPI = Constants.API_POST_FEED_COMMENTS;
        else
            CallingAPI = Constants.API_SEND_COMMENTS;
        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                Log.i(TAG, "sendCommentRes: " + res);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        FantacyApplication.showToast(WriteComment.this, getString(R.string.comment_posted_successfully), Toast.LENGTH_SHORT);
                        if (from.equals("allComment") || from.equals("feedallComment")) {
                            Intent i = new Intent(WriteComment.this, AllComments.class);
                            setResult(RESULT_OK, i);
                            finish();
                        } else {
                            Intent i = new Intent(WriteComment.this, DetailActivity.class);
                            setResult(RESULT_OK, i);
                            finish();
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
                Log.e(TAG, "sendCommentError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                if (from.equals("feedallComment")) {
                    map.put("feed_id", itemId);
                    FeedsFragment.refreshFeeds = true;
                } else
                    map.put("item_id", itemId);
                map.put("comment", editText.getText().toString().trim());

                Log.v(TAG, "sendCommentURL=" + CallingAPI);
                Log.v(TAG, "sendCommentParams=" + map.toString());
                return map;
            }
        };

        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void editComment() {
        final ProgressDialog dialog = new ProgressDialog(WriteComment.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        String CallingAPI;
        if (from != null && from.equals("feedallComment"))
            CallingAPI = Constants.API_EDIT_POST_COMMENT;
        else
            CallingAPI = Constants.API_EDIT_COMMENT;
        StringRequest req = new StringRequest(Request.Method.POST, CallingAPI, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.v(TAG, "editCommentRes=" + res);
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        FantacyApplication.showToast(WriteComment.this, getString(R.string.comment_edited_successfully), Toast.LENGTH_SHORT);
                        if (from.equals("allComment") || from.equals("feedallComment")) {
                            Intent i = new Intent(WriteComment.this, AllComments.class);
                            setResult(RESULT_OK, i);
                            finish();
                        } else {
                            Intent i = new Intent(WriteComment.this, DetailActivity.class);
                            setResult(RESULT_OK, i);
                            finish();
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
                Log.e(TAG, "editCommentError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                if (from.equals("feedallComment")) {
                    map.put("feed_id", itemId);
                    FeedsFragment.refreshFeeds = true;
                } else
                    map.put("item_id", itemId);
                map.put("comment", editText.getText().toString().trim());
                map.put("comment_id", commentId);
                Log.v(TAG, "editCommentParams=" + map);
                return map;
            }
        };

        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.slide_down);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                overridePendingTransition(R.anim.stay, R.anim.slide_down);
                break;
            case R.id.writeComment:
                FantacyApplication.hideSoftKeyboard(WriteComment.this, editText);
                if (editText.getText().toString().trim().length() == 0) {
                    FantacyApplication.showToast(WriteComment.this, getString(R.string.please_enter_comment), Toast.LENGTH_SHORT);
                } else {
                    if (to.equals("add")) {
                        sendComment();
                    } else {
                        editComment();
                    }
                }
                break;
        }
    }

    /**
     * Adapter for suggestion list
     **/
    public class SuggestAdapter extends ArrayAdapter<String> {
        Context context;
        List<HashMap<String, String>> Items;
        List<String> Names;

        public SuggestAdapter(Context context, int ResourceId, List<String> names, List<HashMap<String, String>> objects) {
            super(context, ResourceId, names);
            this.context = context;
            this.Items = objects;
            this.Names = names;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.atmention_layout, parent, false);//layout
            } else {
                view = convertView;
                view.forceLayout();
            }

            ImageView userImage = (ImageView) view.findViewById(R.id.userImage);
            TextView userName = (TextView) view.findViewById(R.id.userName);

            HashMap<String, String> hm = Items.get(position);

            if (hm.get(Constants.TAG_TYPE).equals("hash")) {
                userImage.setVisibility(View.GONE);
                userName.setText(Names.get(position));
            } else {
                userImage.setVisibility(View.VISIBLE);

                userName.setText(hm.get(Constants.TAG_USER_NAME));
                String image = hm.get(Constants.TAG_USER_IMAGE);
                if (image != null && !image.equals("")) {
                    Picasso.get().load(image).into(userImage);
                }
            }

            return view;
        }
    }

    class TagTokenizer implements MultiAutoCompleteTextView.Tokenizer {

        @Override
        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();
            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }
            if (text instanceof Spanned) {
                SpannableString sp = new SpannableString(text + " ");
                TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
                return sp;
            } else {
                return text + " ";
            }
        }

        @Override
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;
            while (i > 0 && (text.charAt(i - 1) != '@' && text.charAt(i - 1) != '#')) {
                i--;
            }
            while (i < cursor && (text.charAt(i) == '@' || text.charAt(i) == '#')) {
                i++;
            }
            return i;
        }

        @Override
        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();
            while (i < len) {
                if (text.charAt(i) == ' ') {
                    return i;
                } else {
                    i++;
                }
            }
            return len;
        }
    }
}
