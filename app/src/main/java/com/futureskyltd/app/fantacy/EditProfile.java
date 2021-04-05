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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.external.CustomEditText;
import com.futureskyltd.app.external.ImagePicker;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hitasoft on 22/6/17.
 */

public class EditProfile extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = EditProfile.class.getSimpleName();
    public static boolean imageUploading = false;
    ImageView backBtn, cartBtn, searchBtn, edit, backgroundImage, userImage;
    TextView title, userName, email, change, cancelPass, savePass, viewAll,
            addressName, address1, address2, country, phone, addNew, save;
    RelativeLayout passwordLay, addressLay, changePasswordLay;
    EditText oldPassword, newPassword, rePassword;
    ProgressDialog pd;
    String viewUrl = "", imageName = "";
    HashMap<String, String> profileMap = new HashMap<String, String>();
    ArrayList<HashMap<String, String>> addressAry = new ArrayList<>();
    CustomEditText profFullName;
    SharedPreferences preferences;
    String accesstoken, customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);
        customerId = preferences.getString("customer_id", null);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backBtn);
        cartBtn = (ImageView) findViewById(R.id.cartBtn);
        searchBtn = (ImageView) findViewById(R.id.searchBtn);
        edit = (ImageView) findViewById(R.id.edit);
        backgroundImage = (ImageView) findViewById(R.id.backgroundImage);
        userImage = (ImageView) findViewById(R.id.userImage);
        profFullName = (CustomEditText) findViewById(R.id.fullName);
        // profFullName=(ProfNameView)findViewById(R.id.profFullName);
        userName = (TextView) findViewById(R.id.userName);
        email = (TextView) findViewById(R.id.email);
        change = (TextView) findViewById(R.id.change);
        oldPassword = (EditText) findViewById(R.id.oldPassword);
        newPassword = (EditText) findViewById(R.id.newPassword);
        rePassword = (EditText) findViewById(R.id.rePassword);
        cancelPass = (TextView) findViewById(R.id.cancelPass);
        savePass = (TextView) findViewById(R.id.savePass);
        viewAll = (TextView) findViewById(R.id.viewAll);
        addressName = (TextView) findViewById(R.id.addressName);
        address1 = (TextView) findViewById(R.id.address1);
        address2 = (TextView) findViewById(R.id.address2);
        country = (TextView) findViewById(R.id.country);
        phone = (TextView) findViewById(R.id.phone);
        addNew = (TextView) findViewById(R.id.addNew);
        save = (TextView) findViewById(R.id.save);
        passwordLay = (RelativeLayout) findViewById(R.id.passwordLay);
        addressLay = (RelativeLayout) findViewById(R.id.addressLay);
        changePasswordLay = (RelativeLayout) findViewById(R.id.changePasswordLay);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("FantacyPref", MODE_PRIVATE);
        if (pref.getBoolean(Constants.IS_SOCIAL_LOGIN, false)) {
            changePasswordLay.setVisibility(View.GONE);
        } else {
            changePasswordLay.setVisibility(View.VISIBLE);
        }

        title.setText(getString(R.string.edit_profile));

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        cartBtn.setVisibility(View.VISIBLE);
        searchBtn.setVisibility(View.VISIBLE);
        save.setOnClickListener(this);
        cancelPass.setOnClickListener(this);
        savePass.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        viewAll.setOnClickListener(this);
        edit.setOnClickListener(this);
        addNew.setOnClickListener(this);
        change.setOnClickListener(this);
        userImage.setOnClickListener(this);
        cartBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        profileMap = (HashMap<String, String>) getIntent().getExtras().get("data");
        profFullName.setText(profileMap.get(Constants.TAG_FULL_NAME));
        profFullName.setSelection(profFullName.getText().length());
        userName.setText(profileMap.get(Constants.TAG_USER_NAME));
        email.setText(profileMap.get(Constants.TAG_EMAIL));

        Picasso.get().load(profileMap.get(Constants.TAG_USER_IMAGE)).error(R.drawable.temp).placeholder(R.drawable.temp).into(userImage);
        Picasso.get().load(profileMap.get(Constants.TAG_USER_IMAGE)).error(R.drawable.temp).placeholder(R.drawable.temp).into(backgroundImage);

        getAddress();

        pd = new ProgressDialog(EditProfile.this);
        pd.setMessage(getString(R.string.pleasewait));
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);
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
                ImageCompression imageCompression = new ImageCompression(EditProfile.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        imageUploading = true;
                        new uploadImage().execute(imagePath);
                    }
                };
                imageCompression.execute(filepath);
            } else {
                FantacyApplication.defaultSnack(EditProfile.this.findViewById(R.id.parentLay), getString(R.string.something_went_wrong), "alert");
            }
        }
    }

    private void setSettings(final String fullname) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SET_SETTINGS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "setSettingsRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        FantacyApplication.pref = getApplicationContext().getSharedPreferences("FantacyPref",
                                MODE_PRIVATE);
                        FantacyApplication.editor = FantacyApplication.pref.edit();

                        FantacyApplication.editor.putString("fullName", fullname);
                        GetSet.setFullName(fullname);
                        if (FragmentMainActivity.userName != null) {
                            FragmentMainActivity.userName.setText(fullname);
                        }

                        if (!viewUrl.equals("")) {
                            FantacyApplication.editor.putString("userImage", viewUrl);
                            GetSet.setImageUrl(viewUrl);
                            if (FragmentMainActivity.userImage != null) {
                                Picasso.get().load(viewUrl).placeholder(R.drawable.temp).error(R.drawable.temp).into(FragmentMainActivity.userImage);
                            }
                        }
                        FantacyApplication.editor.commit();

                        viewUrl = "";
                        imageName = "";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callProfile(fullname);
                            }
                        });
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
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("user_image", imageName);
                map.put("full_name", fullname);
                Log.v(TAG, "setSettingsParams=" + map);
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

    private void getAddress() {
        final ProgressDialog dialog = new ProgressDialog(EditProfile.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_ADDRESS, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
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
                    }
                    if (addressAry.size() == 0) {
                        addressLay.setVisibility(View.GONE);
                    } else {
                        addressLay.setVisibility(View.VISIBLE);
                        addressName.setText(addressAry.get(0).get(Constants.TAG_FULL_NAME));
                        address1.setText(addressAry.get(0).get(Constants.TAG_ADDRESS1));
                        if (addressAry.get(0).containsKey(Constants.TAG_ADDRESS2))
                            address2.setText(addressAry.get(0).get(Constants.TAG_ADDRESS2));
                        else
                            address2.setVisibility(View.GONE);
                        country.setText(addressAry.get(0).get(Constants.TAG_COUNTRY));
                        phone.setText(addressAry.get(0).get(Constants.TAG_PHONE_NO));
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
                Log.e(TAG, "getAddressError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
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

        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    private void changePassword() {
        final ProgressDialog dialog = new ProgressDialog(EditProfile.this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CHANGE_PASSWORD, new Response.Listener<String>() {

            @Override
            public void onResponse(String res) {
                try {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Log.v(TAG, "changePasswordRes=" + res);
                    JSONObject json = new JSONObject(res);
                    oldPassword.setText("");
                    newPassword.setText("");
                    rePassword.setText("");
                    passwordLay.setVisibility(View.GONE);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                    FantacyApplication.defaultSnack(EditProfile.this.findViewById(R.id.parentLay), message, "alert");
                    if (!status.equals("true")) {
                        if (message.equals("Old Password Incorrect")) {
                            FantacyApplication.showToast(EditProfile.this, getString(R.string.old_password_incorrect), Toast.LENGTH_SHORT);
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
                FantacyApplication.defaultSnack(EditProfile.this.findViewById(R.id.parentLay), getString(R.string.something_went_wrong), "error");

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", customerId);
                map.put("new_password", newPassword.getText().toString());
                if (profileMap.get(Constants.TAG_HAS_PASSWORD).equals("yes")) {
                    map.put("old_password", oldPassword.getText().toString());
                } else {
                    map.put("old_password", "");
                }
                Log.v(TAG, "changePasswordParams=" + map);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fragment_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return FragmentMainActivity.onNavOptionSelected(this, id);
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
                            EditProfile.this.startActivity(intent);
                        }
                    }
                }
                break;
        }
    }

    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(EditProfile.this, permissions, requestCode);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backBtn:
                /*If soft keyboard is open then close it*/
                FantacyApplication.hideSoftKeyboard(EditProfile.this, backBtn);
                finish();
                break;
            case R.id.save:
                setSettings(profFullName.getText().toString().trim());
                //finish();
                break;
            case R.id.viewAll:
                Intent i = new Intent(EditProfile.this, ShippingAddress.class);
                i.putExtra("from", "profile");
                startActivity(i);
                break;
            case R.id.addNew:
                Intent j = new Intent(EditProfile.this, AddAddress.class);
                j.putExtra("from", "profile");
                j.putExtra("to", "add");
                startActivity(j);
                break;
            case R.id.userImage:
            case R.id.edit:
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
                } else {
                    ImagePicker.pickImage(this, "Select your image:");
                }
                break;
            case R.id.change:
                if (passwordLay.getVisibility() == View.VISIBLE) {
                    passwordLay.setVisibility(View.GONE);
                    FantacyApplication.hideSoftKeyboard(EditProfile.this, change);
                } else {
                    passwordLay.setVisibility(View.VISIBLE);
                }
                if (profileMap.get(Constants.TAG_HAS_PASSWORD).equals("no")) {
                    oldPassword.setVisibility(View.GONE);
                    newPassword.setHint(getString(R.string.password));
                }
                break;
            case R.id.cancelPass:
                oldPassword.setText("");
                newPassword.setText("");
                rePassword.setText("");
                passwordLay.setVisibility(View.GONE);
                FantacyApplication.hideSoftKeyboard(EditProfile.this, cancelPass);
                break;
            case R.id.savePass:
                if (profileMap.get(Constants.TAG_HAS_PASSWORD).equals("no")) {
                    if (oldPassword.getText().toString().equals(newPassword.getText().toString())) {
                        FantacyApplication.defaultSnack(EditProfile.this.findViewById(R.id.parentLay), getString(R.string.newpassword_error), "alert");
                    }
                }
                if (newPassword.getText().toString().trim().length() < 6) {
                    FantacyApplication.showToast(EditProfile.this, getString(R.string.password_error), Toast.LENGTH_SHORT);
                    newPassword.requestFocus();
                } else if (rePassword.getText().toString().trim().length() == 0 || !newPassword.getText().toString().equals(rePassword.getText().toString())) {
                    FantacyApplication.showToast(EditProfile.this, getString(R.string.repassword_error), Toast.LENGTH_SHORT);
                } else {
                    FantacyApplication.hideSoftKeyboard(EditProfile.this, savePass);
                    changePassword();
                }
                break;
            case R.id.cartBtn:
                if (accesstoken !=  null) {
                    Intent c = new Intent(this, CartActivity.class);
                    c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    c.putExtra("shippingId", "0");
                    c.putExtra("itemId", "0");
                    startActivity(c);
                } else {
                    Intent login = new Intent(this, SignInActivity.class);
                    startActivity(login);
                }
                break;
            case R.id.searchBtn:
                Intent s = new Intent(this, RecentSearch.class);
                startActivity(s);
                break;
        }
    }

    private void callProfile(String usrName) {
        Intent p = new Intent(EditProfile.this, Profile.class);
        p.putExtra("userId", customerId);
        p.putExtra("userName", usrName);
        p.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        p.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(p);
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
                dos.writeBytes("user");
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
            try {
                jsonobject = new JSONObject(Json);
                Log.v("json", "json" + Json);
                status = jsonobject.getString("status");
                if (status.equals("true")) {

                    JSONObject image = jsonobject.getJSONObject("result");
                    imageName = DefensiveClass.optString(image, "name");
                    viewUrl = DefensiveClass.optString(image, "image");

                    //    imagesAry.add(viewUrl);
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
            if (!EditProfile.this.isFinishing()) {
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
                Picasso.get().load(viewUrl).error(R.drawable.temp).placeholder(R.drawable.temp).into(userImage);
                Picasso.get().load(viewUrl).error(R.drawable.temp).placeholder(R.drawable.temp).into(backgroundImage);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
}
