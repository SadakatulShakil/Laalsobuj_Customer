package com.futureskyltd.app.fantacy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.ImagePicker;
import com.futureskyltd.app.helper.ImageCompression;
import com.futureskyltd.app.helper.ImageStorage;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hitasoft on 23/8/17.
 */

public class CreateFeed extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    public static final String TAG = CreateFeed.class.getSimpleName();
    public static boolean imageUploading = false;
    ImageView backBtn, itemImage;
    RelativeLayout icon;
    TextView title, saveBtn, name;
    MultiAutoCompleteTextView editText;
    ProgressDialog dialog;
    String viewUrl = "";
    SharedPreferences preferences;
    String accesstoken, customerId;
    SuggestAdapter suggestAdapter;
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
        setContentView(R.layout.create_feed_dialog);

        overridePendingTransition(R.anim.slide_up, R.anim.stay);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);
        customerId = preferences.getString("customer_id", null);

        backBtn = (ImageView) findViewById(R.id.backBtn);
        title = (TextView) findViewById(R.id.title);
        saveBtn = (TextView) findViewById(R.id.saveBtn);
        icon = (RelativeLayout) findViewById(R.id.icon);
        name = (TextView) findViewById(R.id.name);
        editText = (MultiAutoCompleteTextView) findViewById(R.id.editText);

        dialog = new ProgressDialog(CreateFeed.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        title.setText(getString(R.string.create_feed));
        backBtn.setImageResource(R.drawable.close);
        saveBtn.setText(getString(R.string.post));

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        icon.setOnClickListener(this);
        saveBtn.setOnClickListener(this);

        List<String> uNames = new ArrayList<String>();
        final ArrayAdapter<String> TopicName = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, uNames);
        editText.setAdapter(TopicName);
        //  editText.setThreshold(2);
        editText.setTokenizer(new TagTokenizer());
        editText.addTextChangedListener(textWatcher);

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

                            data.put(Constants.TAG_USER_ID, user_id);
                            data.put(Constants.TAG_FULL_NAME, full_name);
                            data.put(Constants.TAG_USER_NAME, user_name);
                            data.put(Constants.TAG_USER_IMAGE, user_image);
                            data.put(Constants.TAG_TYPE, "at");

                            if (!user_id.equals(customerId)) {
                                aList.add(data);
                                uNames.add(user_name);
                            }

                        }
                        if (aList.size() > 0) {
                            suggestAdapter = new SuggestAdapter(CreateFeed.this, R.layout.atmention_layout, uNames, aList);
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
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
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
                List<String> uNames = new ArrayList<String>();
                Log.v(TAG ,"getUserDataRes=" + res);
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
                            suggestAdapter = new SuggestAdapter(CreateFeed.this, R.layout.atmention_layout, uNames, aList);
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
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accesstoken);
                Log.d(TAG, "getHeaders: " + accesstoken);
                return headers;
            }

        };
        FantacyApplication.getInstance().getRequestQueue().cancelAll("#tag");
        FantacyApplication.getInstance().addToRequestQueue(req, "#tag");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == 234) {
            Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
            ImageStorage imageStorage = new ImageStorage(this);
            final String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
            String imageStatus = imageStorage.saveToSdCard(bitmap, timestamp + ".jpg");
            if (imageStatus.equals("success")) {
                File file = imageStorage.getImage(timestamp + ".jpg");
                String filepath = file.getAbsolutePath();
                Log.i(TAG, "selectedImageFile: " + filepath);
                ImageCompression imageCompression = new ImageCompression(CreateFeed.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        imageUploading = true;
                        new uploadImage(name).execute(imagePath);
                    }
                };
                imageCompression.execute(filepath);
            } else {
                FantacyApplication.showToast(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
            }
        }
    }

    private void createFeeds(final String comments) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CREATE_FEEDS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Log.v(TAG, "createFeedsRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        FeedsFragment.refreshFeeds = true;
                        FantacyApplication.showToast(CreateFeed.this, getString(R.string.feed_success), Toast.LENGTH_SHORT);
                        finish();
                        overridePendingTransition(R.anim.stay, R.anim.slide_down);
                    } else if (status.equalsIgnoreCase("false")) {
                        FantacyApplication.showToast(CreateFeed.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
                    } else if (status.equalsIgnoreCase("error")) {
                        FantacyApplication.showToast(CreateFeed.this, DefensiveClass.optString(json, Constants.TAG_MESSAGE), Toast.LENGTH_SHORT);
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
                Log.e(TAG, "createFeedsError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("image", viewUrl);
                map.put("message", comments);
                Log.v(TAG,"createFeedsParams=" + map);
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.slide_down);
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
            dialog.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), true);
        if (imageUploading) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        FantacyApplication.showSnack(this, findViewById(R.id.parentLay), isConnected);
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
                            CreateFeed.this.startActivity(intent);
                        }
                    }
                }
                break;
        }
    }

    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(CreateFeed.this, permissions, requestCode);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                overridePendingTransition(R.anim.stay, R.anim.slide_down);
                break;
            case R.id.saveBtn:
                if (editText.getText().toString().trim().length() == 0 && viewUrl.equals("")) {
                    FantacyApplication.showToast(CreateFeed.this, getString(R.string.please_enter_status), Toast.LENGTH_SHORT);
                } else {
                    createFeeds(editText.getText().toString().trim());
                }
                break;
            case R.id.icon:
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
                } else {
                    ImagePicker.pickImage(this, "Select your image:");
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

    class uploadImage extends AsyncTask<String, String, String> {

        public TextView name;

        public uploadImage(TextView name) {
            this.name = name;
        }

        @Override
        protected String doInBackground(String... imgpath) {
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
            String Json = null;
            try {
                String exsistingFileName = imgpath[0];
                Log.v(" existingFileName", exsistingFileName);
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
                dos.writeBytes("feeds");
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                        + exsistingFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                Log.e("MediaPlayer", "Headers are written");
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                Log.v("buffer", "buffer" + buffer);

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    Log.v("bytesRead", "bytesRead" + bytesRead);
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
            return Json;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!CreateFeed.this.isFinishing()) {
                dialog.show();
            }
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                Log.v(TAG, "uploadImageRes" + res);
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                JSONObject jsonobject = new JSONObject(res);
                String status = jsonobject.getString("status");
                if (status.equalsIgnoreCase("true")) {
                    JSONObject result = jsonobject.getJSONObject("result");
                    String nameImg = DefensiveClass.optString(result, "name");
                    //    viewUrl = DefensiveClass.optString(result, "image");
                    viewUrl = DefensiveClass.optString(result, "name");

                    name.setText(nameImg);
                    Log.v("viewUrl", "viewUrl" + viewUrl);
                    imageUploading = false;

                } else {
                    Toast.makeText(CreateFeed.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(CreateFeed.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Toast.makeText(CreateFeed.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(CreateFeed.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
