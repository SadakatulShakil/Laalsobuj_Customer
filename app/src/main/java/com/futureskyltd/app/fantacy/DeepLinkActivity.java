package com.futureskyltd.app.fantacy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.futureskyltd.app.helper.SharedPrefManager;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 25/9/17.
 */

public class DeepLinkActivity extends BaseActivity {

    private static final String TAG = DeepLinkActivity.class.getSimpleName();
    String itemId = "";
    SharedPreferences pref;
    static SharedPreferences.Editor editor;
    SharedPreferences preferences;
    String accesstoken, customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deep_link_activity);

        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);
        customerId = preferences.getString("customer_id", null);


        Intent in = getIntent();
        Uri data = in.getData();
        pref = getApplicationContext().getSharedPreferences("FantacyPref", MODE_PRIVATE);
        editor = pref.edit();
        String dataString = data.toString();

        if (dataString.contains("/listing/")) {
            String encodedString = data.toString().substring(data.toString().lastIndexOf("/") + 1, data.toString().length());
            Log.v(TAG, "encodedString=" + encodedString);

            try {
                byte[] byteData = Base64.decode(encodedString, Base64.DEFAULT);
                String decodeString = new String(byteData, "UTF-8");
                Log.v(TAG, "decodeString=" + decodeString);
                itemId = decodeString.substring(0, decodeString.lastIndexOf("_"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            getItemDetails(itemId, this);
        } else if (dataString.contains("/gifts/")) {
            String giftId = data.toString().substring(data.toString().lastIndexOf("/") + 1, data.toString().length());
            finish();
            Intent i = new Intent(this, GroupGiftDetail.class);
            i.putExtra("giftId", giftId);
            startActivity(i);
            Toast.makeText(this, "Under Construction !", Toast.LENGTH_SHORT).show();
        } else if (dataString.contains("/verification/")) {
            String encodedString = data.toString().substring(data.toString().lastIndexOf("/") + 1, data.toString().length());
            Log.v(TAG, "encodedString=" + encodedString);
            String[] result = encodedString.split("~");

            try {
                byte[] byteData1 = Base64.decode(result[0], Base64.DEFAULT);
                String email = new String(byteData1, "UTF-8");

                byte[] byteData2 = Base64.decode(result[2], Base64.DEFAULT);
                String password = new String(byteData2, "UTF-8");
                Log.v(TAG, "result=" + email + "" + password);
                //LoginData(email, password);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void getItemDetails(final String itemID, final Context context) {
        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        String accesstoken = preferences.getString("TOKEN", null);
        String customerId = preferences.getString("customer_id", null);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ITEM_DETAILS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {

                try {
                    ArrayList<HashMap<String, String>> itemsAry = new ArrayList<HashMap<String, String>>();
                    Log.v(TAG, "getItemDetailsRes=" + res);
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

                        String image = "";
                        JSONArray photos = temp.optJSONArray(Constants.TAG_PHOTOS);
                        if (photos == null) {
                            map.put(Constants.TAG_PHOTOS, "");
                        } else if (photos.length() == 0) {
                            map.put(Constants.TAG_PHOTOS, "");
                        } else if (photos.length() > 0) {
                            JSONObject pobj = photos.getJSONObject(0);
                            image = DefensiveClass.optString(pobj, Constants.TAG_IMAGE_350);
                            map.put(Constants.TAG_PHOTOS, photos.toString());
                        }
                        map.put(Constants.TAG_IMAGE, image);

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
                        finish();
                        Intent i = new Intent(DeepLinkActivity.this, DetailActivity.class);
                        i.putExtra("items", itemsAry);
                        i.putExtra("position", 0);
                        i.putExtra("is", "deeplink");
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

                Log.e(TAG, "getItemDetailsError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                if (GetSet.isLogged()) {
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
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

   /* private void LoginData(final String email, final String password) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "LoginDataRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        String id = DefensiveClass.optString(json, Constants.TAG_USER_ID);
                        String username = DefensiveClass.optString(json, Constants.TAG_USER_NAME);
                        String userimage = DefensiveClass.optString(json, Constants.TAG_USER_IMAGE);
                        String fullname = DefensiveClass.optString(json, Constants.TAG_FULL_NAME);
                        String islogedfirst = DefensiveClass.optString(json, Constants.TAG_IS_LOGGED_FIRST);
                        LoginActivity.logedfirst = islogedfirst;
                        GetSet.setLogged(true);

                        GetSet.setUserId(id);
                        GetSet.setEmail(email);
                        GetSet.setImageUrl(userimage);
                        GetSet.setUserName(username);
                        GetSet.setFullName(fullname);

                        editor.putBoolean("isLogged", true);
                        editor.putString("userId", id);
                        editor.putString("email", email);
                        editor.putString("userName", username);
                        editor.putString("userImage", userimage);
                        editor.putString("fullName", fullname);
                        editor.putString("language", "English");
                        editor.commit();

                        addDeviceId(DeepLinkActivity.this);

                    } else if (status.equalsIgnoreCase("false")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        switch (message) {
                            case "User does not Exist":
                                FantacyApplication.showToast(DeepLinkActivity.this, getString(R.string.user_does_not_exist), Toast.LENGTH_SHORT);
                                break;
                            case "You cannot login as Admin":
                                FantacyApplication.showToast(DeepLinkActivity.this, getString(R.string.cannot_login_admin), Toast.LENGTH_SHORT);
                                break;
                            case "You cannot login as Moderator":
                                FantacyApplication.showToast(DeepLinkActivity.this, getString(R.string.cannot_login_moderator), Toast.LENGTH_SHORT);
                                break;
                            case "You cannot login with merchant account":
                                FantacyApplication.showToast(DeepLinkActivity.this, getString(R.string.cannot_login_merchant), Toast.LENGTH_SHORT);
                                break;
                            case "Please enter correct email and password":
                                FantacyApplication.showToast(DeepLinkActivity.this, getString(R.string.enter_correct_username_password), Toast.LENGTH_SHORT);
                                break;
                            case "Your account has been disbled please contact our support":
                                FantacyApplication.showToast(DeepLinkActivity.this, getString(R.string.account_disabled), Toast.LENGTH_SHORT);
                                break;
                            case "Please activate your account by the email sent to you":
                                FantacyApplication.showToast(DeepLinkActivity.this, getString(R.string.activate_email), Toast.LENGTH_SHORT);
                                break;
                            default:
                                FantacyApplication.showToast(DeepLinkActivity.this, message, Toast.LENGTH_SHORT);
                                break;
                        }
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                           *//* Intent i = new Intent(DeepLinkActivity.this, PaymentStatus.class);
                            i.putExtra("from", "block");
                            startActivity(i);*//*
                        } else {
                           *//* Intent i = new Intent(DeepLinkActivity.this, PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);*//*
                        }
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
                Log.e(TAG, "LoginDataError: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("email", email);
                map.put("password", password);
                return map;
            }
        };
        FantacyApplication.getInstance().addToRequestQueue(req);
    }*/

    private static void addDeviceId(final Context context) {

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        final String token = SharedPrefManager.getInstance(context).getDeviceToken();
        final String deviceId = android.provider.Settings.Secure.getString(context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PUSH_NOTIFICATION_REGISTER,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "addDeviceIdRes=" + res);
                        try {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {
                                Intent sucess = new Intent(context, FragmentMainActivity.class);
                                context.startActivity(sucess);
                                ((Activity) context).finish();
                            } else {
                                FantacyApplication.showToast(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT);
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
                Log.e(TAG, "addDeviceIdError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("device_token", token);
                map.put("device_type", "1");
                map.put("device_id", deviceId);
                //    map.put("device_mode", "1");
                Log.v(TAG, "addDeviceIdParams=" + map);
                return map;
            }
        };
        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }
}
