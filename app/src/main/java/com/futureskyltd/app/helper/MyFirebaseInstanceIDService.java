/*
package com.hitasoft.app.helper;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.hitasoft.app.fantacy.FantacyApplication;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

*/
/**
 * Created by Hitasoft on 03/11/16.
 *//*



public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        storeToken(refreshedToken);
    }

    private void storeToken(String token) {
        //saving the token on shared preferences
        SharedPrefManager.getInstance(getApplicationContext()).saveDeviceToken(token);

        //get the logined user details from preference
        FantacyApplication.pref = getApplicationContext().getSharedPreferences("FantacyPref",
                MODE_PRIVATE);
        FantacyApplication.editor = FantacyApplication.pref.edit();

        if (FantacyApplication.pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(FantacyApplication.pref.getString("userId", null));

            addDeviceId();
        }
    }

    private void addDeviceId() {

        final String token = SharedPrefManager.getInstance(this).getDeviceToken();
        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PUSH_NOTIFICATION_REGISTER,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String res) {
                        Log.v("res", "res=" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
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
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }

        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("user_id", GetSet.getUserId());
                map.put("device_token", token);
                map.put("device_type", "1");
                map.put("device_id", deviceId);
                // map.put("device_mode", "1");
                Log.v("params", "params=" + map);
                return map;
            }
        };

        FantacyApplication.getInstance().addToRequestQueue(req, TAG);
    }

}*/
