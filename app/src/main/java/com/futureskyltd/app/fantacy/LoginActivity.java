package com.futureskyltd.app.fantacy;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.futureskyltd.app.external.CirclePageIndicator;
import com.futureskyltd.app.external.FontCache;
import com.futureskyltd.app.helper.NetworkReceiver;
import com.futureskyltd.app.helper.SharedPrefManager;
import com.futureskyltd.app.utils.Constants;
import com.futureskyltd.app.utils.DefensiveClass;
import com.futureskyltd.app.utils.GetSet;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends BaseActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    public static String logedfirst = "no";
    static ViewPager desPager, viewPager;
    static GoogleApiClient mGoogleApiClient;
    static SharedPreferences.Editor editor;
    ImageView cancel;
    CirclePageIndicator pagerIndicator;
    TabLayout tabLayout;
    CallbackManager callbackManager;
    public CallbackManager callbacksManager;
    SharedPreferences pref;

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
                Log.d(TAG, "addDeviceIdError: " + error.getMessage());
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        cancel = findViewById(R.id.cancel);
        desPager = findViewById(R.id.desPager);
        pagerIndicator = findViewById(R.id.pagerIndicator);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        pref = getApplicationContext().getSharedPreferences("FantacyPref", MODE_PRIVATE);
        editor = pref.edit();

        String[] names = {getString(R.string.signin_des1), getString(R.string.signin_des2), getString(R.string.signin_des3)};
        DesPagerAdapter desPagerAdapter = new DesPagerAdapter(LoginActivity.this, names);
        desPager.setAdapter(desPagerAdapter);
        pagerIndicator.setViewPager(desPager);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        cancel.setOnClickListener(this);

        //initFacebook();
        //initGplus();
        changeTabsFont();
        FantacyApplication.setupUI(LoginActivity.this, findViewById(R.id.parentLay));

        // register connection status listener
        FantacyApplication.getInstance().setConnectivityListener(this);

        printHashKey(getApplicationContext());
    }

    // For set custom font in tab
    private void changeTabsFont() {

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(FontCache.get("font_regular.ttf", this));
                }
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Signin(), getString(R.string.sign_in));
        adapter.addFragment(new Signup(), getString(R.string.sign_up));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    public static void printHashKey(Context pContext) {
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "printHashKey()", e);
        } catch (Exception e) {
            Log.e(TAG, "printHashKey()", e);
        }
    }


    public void initFacebook() {

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject profile, GraphResponse response) {
                                        final HashMap<String, String> fbdata = new HashMap<String, String>();
                                        Log.v(TAG, "initFacebookProfile=" + profile);
                                        // Application code
                                        try {
                                            if (profile.has("email")) {
                                                //getting accessToken//
                                                final String accessToken = loginResult.getAccessToken().getToken();
                                                // getting name of the user
                                                final String name = profile.getString("name");

                                                // getting email of the user
                                                final String email = profile.getString("email");

                                                // getting userId of the user
                                                final String userId = profile.getString("id");

                                                // getting firstName of the user
                                                final String firstName = profile.getString("first_name");

                                                // getting lastName of the user
                                                final String lastName = profile.getString("last_name");

                                                URL image_value = null;
                                                try {
                                                    image_value = new URL("http://graph.facebook.com/" + userId + "/picture?type=large");

                                                } catch (MalformedURLException e) {
                                                    e.printStackTrace();
                                                }
                                                fbdata.put("type", "facebook");
                                                fbdata.put("accessToken", accessToken);
                                                fbdata.put("email", email);
                                                fbdata.put("id", userId);
                                                fbdata.put("firstName", firstName);
                                                fbdata.put("lastName", lastName);
                                                fbdata.put("image", "http://graph.facebook.com/" + userId + "/picture?type=large");
                                                Log.v(TAG, "fbData=" + fbdata);
                                                Log.d(TAG, "fbAccessToken=" + loginResult.getAccessToken().getToken());
                                                LoginActivity.this.runOnUiThread(new Runnable() {

                                                    @SuppressWarnings("unchecked")
                                                    @Override
                                                    public void run() {
                                                        loginSocial(fbdata);
                                                    }
                                                });
                                            } else {
                                                FantacyApplication.showToast(LoginActivity.this, "Please check your Facebook permissions", Toast.LENGTH_SHORT);
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,first_name,last_name");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        FantacyApplication.showToast(LoginActivity.this, "Facebook - Cancelled", Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        FantacyApplication.showToast(LoginActivity.this, "Facebook - " + exception.getMessage(), Toast.LENGTH_SHORT);
                        if (exception instanceof FacebookAuthorizationException) {
                            if (com.facebook.AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                    }
                });

    }

    public void initGplus() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    public void loginSocial(final HashMap<String, String> data) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_LOGIN_SOCIAL, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Log.v(TAG, "loginSocialRes=" + res);
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        String id = DefensiveClass.optString(json, Constants.TAG_USER_ID);
                        String username = DefensiveClass.optString(json, Constants.TAG_USER_NAME);
                        String userimage = DefensiveClass.optString(json, Constants.TAG_USER_IMAGE);
                        String fullname = DefensiveClass.optString(json, Constants.TAG_FULL_NAME);
                        String islogedfirst = DefensiveClass.optString(json, Constants.TAG_IS_LOGGED_FIRST);
                        logedfirst = islogedfirst;
                        GetSet.setLogged(true);

                        GetSet.setUserId(id);
                        GetSet.setEmail(data.get("email"));
                        GetSet.setImageUrl(userimage);
                        GetSet.setUserName(username);
                        GetSet.setFullName(fullname);

                        editor.putBoolean("isLogged", true);
                        editor.putString("userId", id);
                        editor.putString("email", data.get("email"));
                        editor.putString("userName", username);
                        editor.putString("userImage", userimage);
                        editor.putString("fullName", fullname);
                        editor.putBoolean(Constants.IS_SOCIAL_LOGIN, true);
                        editor.commit();

                        addDeviceId(LoginActivity.this);

                    } else if (status.equalsIgnoreCase("false")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals("You cannot login as Admin"))
                            FantacyApplication.showToast(LoginActivity.this, getString(R.string.cannot_login_admin), Toast.LENGTH_SHORT);
                        else if (message.equals("You cannot login as Moderator"))
                            FantacyApplication.showToast(LoginActivity.this, getString(R.string.cannot_login_moderator), Toast.LENGTH_SHORT);
                        else if (message.equals("You Cannot login as Merchant"))
                            FantacyApplication.showToast(LoginActivity.this, getString(R.string.cannot_login_merchant), Toast.LENGTH_SHORT);
                        else if (message.equals("Unable to save the data now"))
                            FantacyApplication.showToast(LoginActivity.this, getString(R.string.unable_to_save), Toast.LENGTH_SHORT);
                    } else if (status.equalsIgnoreCase("error")) {
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                            Intent i = new Intent(LoginActivity.this, PaymentStatus.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.putExtra("from", "block");
                            startActivity(i);
                        } else {
                            Intent i = new Intent(LoginActivity.this, PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);
                        }
                        (LoginActivity.this).finish();
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
                Log.e(TAG, "loginSocialError: " + error.getMessage());
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("type", data.get("type"));
                map.put("email", data.get("email"));
                map.put("id", data.get("id"));
                if (data.get("lastName").equals("")) {
                    map.put("full_name", data.get("firstName"));
                } else {
                    map.put("full_name", data.get("firstName") + " " + data.get("lastName"));
                }
                map.put("imageurl", data.get("image"));
                Log.v(TAG, "loginSocialParams" + map);
                return map;
            }
        };
        dialog.show();
        FantacyApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Funtions for login into G+
     **/
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("handleSignInResult", "handleSignInResult:" + result.isSuccess());
        Log.d("handleSignInResult", "handleSignInResult:" + result.getStatus());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            try {
                GoogleSignInAccount acct = result.getSignInAccount();

                String personName = acct.getDisplayName();
                String email = acct.getEmail();
                String id = acct.getId();
                String personPhoto = "";

                if (acct.getPhotoUrl() == null) {
                    personPhoto = "";
                } else {
                    personPhoto = acct.getPhotoUrl().toString();
                }

                HashMap<String, String> gplusData = new HashMap<String, String>();

                gplusData.put("type", "google");
                gplusData.put("email", email);
                gplusData.put("id", id);
                gplusData.put("firstName", personName);
                gplusData.put("lastName", "");
                gplusData.put("image", personPhoto);

                loginSocial(gplusData);

                Log.v(TAG, "personName" + personName);
                Log.v(TAG, "personPhoto" + personPhoto);
                Log.v(TAG, "email" + email);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Signed out, show unauthenticated UI.
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(TAG, "onConnectionFailed");
    }

   /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);

    }*/

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
            case R.id.cancel:
                finish();
                if (mGoogleApiClient != null) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    // [START_EXCLUDE]
                                    Log.v(TAG, "status=" + status);
                                    // [END_EXCLUDE]
                                }
                            });
                }
                break;
        }
    }

    public static class Signin extends Fragment {

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.login_fragment, container, false);
            LinearLayout fbLay = rootView.findViewById(R.id.fbLay);
            LinearLayout gplusLay = rootView.findViewById(R.id.gplusLay);
            final EditText email = rootView.findViewById((R.id.email));
            final EditText password = rootView.findViewById((R.id.password));
            TextView signin = rootView.findViewById((R.id.signin));
            TextView forgetpassword = rootView.findViewById((R.id.forgetpassword));

            signin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (email.getText().toString().trim().length() == 0) {
                        FantacyApplication.showToast(getActivity(), getString(R.string.please_type_mail), Toast.LENGTH_SHORT);
                        email.requestFocus();
                    } else if (!email.getText().toString().matches(emailPattern)) {
                        FantacyApplication.showToast(getActivity(), getString(R.string.email_error), Toast.LENGTH_SHORT);
                        email.requestFocus();
                    } else if (password.getText().toString().length() == 0) {
                        FantacyApplication.showToast(getActivity(), getString(R.string.please_type_password), Toast.LENGTH_SHORT);
                        password.requestFocus();
                    } else {
                        LoginData(email.getText().toString(), password.getText().toString());
                    }
                }
            });

            forgetpassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    forgotDialog();
                }
            });

            fbLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile", "email"));
                }
            });

            gplusLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    getActivity().startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });

            FantacyApplication.setupUI(getActivity(), rootView.findViewById(R.id.mainLay));

            return rootView;
        }


        private void forgotDialog() {
            final Dialog dialog = new Dialog(getActivity());
            Display display;
            display = getActivity().getWindowManager().getDefaultDisplay();
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.forgot_pass_dialog);
            dialog.getWindow().setLayout(display.getWidth() * 90 / 100, display.getHeight() * 90 / 100);
            dialog.setCancelable(true);

            final EditText email = dialog.findViewById(R.id.editText);
            TextView reset = dialog.findViewById(R.id.reset);
            ImageView cancel = dialog.findViewById(R.id.cancel);

            email.requestFocus();
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    dialog.dismiss();
                }
            });

            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (email.getText().toString().trim().length() == 0) {
//                        FantacyApplication.showToast(getActivity(), getString(R.string.please_type_mail), Toast.LENGTH_SHORT);
                        email.setError(getString(R.string.please_type_mail));
                        email.requestFocus();
                    } else if (!email.getText().toString().matches(emailPattern)) {
//                        FantacyApplication.showToast(getActivity(), getString(R.string.email_error), Toast.LENGTH_SHORT);
                        email.setError(getString(R.string.email_error));
                        email.requestFocus();
                    } else {
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        forgotPassword(email.getText().toString());
                        dialog.dismiss();

                    }
                }
            });

            if (!dialog.isShowing()) {
                dialog.show();
            }
        }

        private void forgotPassword(final String email) {
            final ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getString(R.string.pleasewait));
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_FORGOT_PASSWORD, new Response.Listener<String>() {
                @Override
                public void onResponse(String res) {
                    try {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Log.v(TAG, "forgotDialogRes=" + res);
                        JSONObject json = new JSONObject(res);
                        String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                        String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                        if (status.equals("true")) {
                            FantacyApplication.showToast(getActivity(), getString(R.string.forgot_pswd_success), Toast.LENGTH_SHORT);
                        } else {
                            FantacyApplication.showToast(getActivity(), getString(R.string.user_not_found), Toast.LENGTH_SHORT);
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
                    Log.e(TAG, "forgotDialogError: " + error.getMessage());
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("email", email);
                    return map;
                }
            };
            dialog.show();
            FantacyApplication.getInstance().addToRequestQueue(req);
        }

        private void LoginData(final String email, final String password) {
            final ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getString(R.string.pleasewait));
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_LOGIN, new Response.Listener<String>() {
                @Override
                public void onResponse(String res) {
                    try {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                            getActivity().finish();
                        }
                        Log.d(TAG, "LoginDataRes :" + res);
                        JSONObject json = new JSONObject(res);
                        String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("true")) {
                            String id = DefensiveClass.optString(json, Constants.TAG_USER_ID);
                            String username = DefensiveClass.optString(json, Constants.TAG_USER_NAME);
                            String userimage = DefensiveClass.optString(json, Constants.TAG_USER_IMAGE);
                            String fullname = DefensiveClass.optString(json, Constants.TAG_FULL_NAME);
                            String islogedfirst = DefensiveClass.optString(json, Constants.TAG_IS_LOGGED_FIRST);
                            logedfirst = islogedfirst;
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
                            editor.putBoolean(Constants.IS_SOCIAL_LOGIN, false);
                            editor.commit();

                            addDeviceId(getActivity());

                        } else if (status.equalsIgnoreCase("false")) {
                            EditText email = getView().findViewById((R.id.email));
                            EditText password = getView().findViewById((R.id.password));
                            String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                            if (message.equals("User does not Exist")) {
                                email.setText("");
                                FantacyApplication.showToast(getActivity(), getString(R.string.user_does_not_exist), Toast.LENGTH_SHORT);
                            } else if (message.equals("You cannot login as Admin"))
                                FantacyApplication.showToast(getActivity(), getString(R.string.cannot_login_admin), Toast.LENGTH_SHORT);
                            else if (message.equals("You cannot login as Moderator"))
                                FantacyApplication.showToast(getActivity(), getString(R.string.cannot_login_moderator), Toast.LENGTH_SHORT);
                            else if (message.equals("You cannot login with merchant account"))
                                FantacyApplication.showToast(getActivity(), getString(R.string.cannot_login_merchant), Toast.LENGTH_SHORT);
                            else if (message.equals("Please enter correct email and password"))
                                FantacyApplication.showToast(getActivity(), getString(R.string.enter_correct_username_password), Toast.LENGTH_SHORT);
                            else if (message.equals("Your account has been disbled please contact our support"))
                                FantacyApplication.showToast(getActivity(), getString(R.string.account_disabled), Toast.LENGTH_SHORT);
                            else if (message.equals("Please activate your account by the email sent to you"))
                                FantacyApplication.showToast(getActivity(), getString(R.string.activate_email), Toast.LENGTH_SHORT);
                            password.setText("");
                        } else if (status.equalsIgnoreCase("error")) {
                            String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                            if (message.equals(getString(R.string.admin_error)) || message.equals(getString(R.string.admin_delete_error))) {
                                Intent i = new Intent(getActivity(), PaymentStatus.class);
                                i.putExtra("from", "block");
                                startActivity(i);
                            } else {
                                Intent i = new Intent(getActivity(), PaymentStatus.class);
                                i.putExtra("from", "maintenance");
                                startActivity(i);
                            }
                            (getActivity()).finish();
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
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("email", email);
                    map.put("password", password);
                    Log.i(TAG, "LoginDataParams= " + map);
                    return map;
                }
            };
            dialog.show();
            FantacyApplication.getInstance().addToRequestQueue(req);
        }
    }

    public static class Signup extends Fragment {

        CallbackManager callbackManager;
        EditText name, username, email, password, rePassword, storeName, phone, address, zipCode, infoApaCode;
        TextView signup;
        String districtName = "Rangpur", upazilaName = "Pirgachha";
        LinearLayout fbLay, gplusLay;
        NestedScrollView nestedSignUplay;
        LinearLayout signUpFbLay;
        InputFilter filterWithoutSpace = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        InputFilter filterWithSpace = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.signup_fragment, container, false);

            name = rootView.findViewById((R.id.name));
            username = rootView.findViewById((R.id.username));
            email = rootView.findViewById((R.id.email));
            phone = rootView.findViewById(R.id.userPhone);
            storeName = rootView.findViewById(R.id.storeName);
            address = rootView.findViewById(R.id.address);
            zipCode = rootView.findViewById(R.id.zipcode);
            infoApaCode = rootView.findViewById(R.id.infoApaCode);
            password = rootView.findViewById((R.id.password));
            rePassword = rootView.findViewById((R.id.repassword));
            signup = rootView.findViewById((R.id.signup));
            fbLay = rootView.findViewById(R.id.fbLay);
            gplusLay = rootView.findViewById(R.id.gplusLay);
            signUpFbLay = rootView.findViewById(R.id.signupFbLay);
            nestedSignUplay = rootView.findViewById(R.id.signuplay);
            callbackManager = CallbackManager.Factory.create();

            final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

            name.addTextChangedListener(new FantacyApplication.avoidMultiSpace(name));
            name.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(30)});
            username.setFilters(new InputFilter[]{filterWithoutSpace, new InputFilter.LengthFilter(30)});

            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (name.getText().toString().trim().length() == 0 || username.getText().toString().trim().length() == 0 ||
                            email.getText().toString().trim().length() == 0 || password.getText().toString().trim().length() == 0 ||
                            rePassword.getText().toString().trim().length() == 0) {
                        FantacyApplication.showToast(getActivity(), getString(R.string.please_fill_all), Toast.LENGTH_SHORT);
                    } else if (name.getText().toString().trim().length() < 3) {
                        FantacyApplication.showToast(getActivity(), getString(R.string.fullname_error), Toast.LENGTH_SHORT);
                        name.requestFocus();
                    } else if (username.getText().toString().trim().length() < 3) {
                        FantacyApplication.showToast(getActivity(), getString(R.string.username_error), Toast.LENGTH_SHORT);
                        username.requestFocus();
                    } else if (!email.getText().toString().matches(emailPattern) || email.getText().toString().trim().length() == 0) {
                        FantacyApplication.showToast(getActivity(), getString(R.string.email_error), Toast.LENGTH_SHORT);
                        email.requestFocus();
                    } else if (password.getText().toString().trim().length() < 6) {
                        FantacyApplication.showToast(getActivity(), getString(R.string.password_error), Toast.LENGTH_SHORT);
                        password.requestFocus();
                    } else if (rePassword.getText().toString().trim().length() == 0 || !password.getText().toString().trim().equals(rePassword.getText().toString().trim())) {
                        FantacyApplication.showToast(getActivity(), getString(R.string.repassword_error), Toast.LENGTH_SHORT);
                        password.setText("");
                        rePassword.setText("");
                    } else {
                        SignupData();
                    }
                }
            });

            fbLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile", "email"));

                    setUpFbData();
                }
            });

            /////////////////google sign in///////////////////////
           /* gplusLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    getActivity().startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });*/

            FantacyApplication.setupUI(getActivity(), rootView.findViewById(R.id.mainLay));
            return rootView;


        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        private void setUpFbData() {

            Log.d(TAG, "setUpFbData: " + "success");
            LoginManager.getInstance().logInWithReadPermissions(getActivity(),
                    Arrays.asList("email", "public_profile"));

            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "onSuccess: " + "DOne");
                    handleFacebookLogin(loginResult);
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "onCancel: " + "Login Cancel!");

                }

                @Override
                public void onError(FacebookException error) {
                    Log.d(TAG, "onError: " + "Login Error!");

                }
            });
        }


        private void handleFacebookLogin(LoginResult loginResult) {

            Log.d(TAG, "handleFacebookLogin: " +loginResult.getAccessToken().getToken());
            Profile profile = Profile.getCurrentProfile();
            if (profile != null) {
                String userFbId = profile.getId();
                String userFirstName = profile.getFirstName();
                String userLastName = profile.getLastName();
                String userName = profile.getName();
                String profileImage = profile.getProfilePictureUri(300, 300).toString();

                Log.v(TAG, "handleFacebookLogin: " + userFbId + "  " + userName + "  ");


                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                // Application code
                                try {
                                    //getting accessToken//
                                    String accessToken = loginResult.getAccessToken().getToken();
                                    // getting email of the user
                                    String email = object.getString("email");

                                    // getting userId of the user
                                    long fb_Id = object.getLong("id");
                                    // getting firstName of the user

                                    Log.v(TAG, "fbToken=" + loginResult.getAccessToken().getToken());
                                    Log.v(TAG, "fbId=" + fb_Id);
                                    Log.v(TAG, "fbEmail: " + email);
                                    signUpFbLay.setVisibility(View.GONE);
                                    nestedSignUplay.setVisibility(View.VISIBLE);
                                    Toast.makeText(getContext(), "success!", Toast.LENGTH_SHORT).show();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                request.executeAsync();
            }
        }



        private void SignupData() {
            final ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getString(R.string.pleasewait));
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SIGNUP, new Response.Listener<String>() {
                @Override
                public void onResponse(String res) {
                    Log.v(TAG, "SignupDataRes=" + res);
                    try {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        JSONObject json = new JSONObject(res);
                        String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("true")) {
                            String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                            Display display;
                            display = getActivity().getWindowManager().getDefaultDisplay();
                            final Dialog dialog = new Dialog(getActivity());
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                            dialog.setContentView(R.layout.default_popup);
                            dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.setCancelable(true);

                            TextView title = dialog.findViewById(R.id.title);
                            TextView yes = dialog.findViewById(R.id.yes);
                            title.setText(message);
                            yes.setText(R.string.ok);

                            yes.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    name.setText("");
                                    username.setText("");
                                    email.setText("");
                                    password.setText("");
                                    rePassword.setText("");
                                    viewPager.setCurrentItem(0, true);
                                    dialog.dismiss();
                                }
                            });
                            if (!dialog.isShowing()) {
                                dialog.show();
                            }
                        } else if (status.equalsIgnoreCase("error")) {
                            Intent i = new Intent(getActivity(), PaymentStatus.class);
                            i.putExtra("from", "maintenance");
                            startActivity(i);
                        } else {
                            String error = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                            FantacyApplication.defaultDialog(getActivity(), error);
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
                    Log.e(TAG, "SignupDataError: " + error.getMessage());
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("user_name", username.getText().toString().trim());
                    map.put("full_name", name.getText().toString().trim());
                    map.put("email", email.getText().toString().trim());
                    map.put("phone", phone.getText().toString().trim());
                    map.put("storeName", storeName.getText().toString().trim());
                    map.put("address", address.getText().toString().trim());
                    map.put("zipCode", zipCode.getText().toString().trim());
                    map.put("infoApaCode", infoApaCode.getText().toString().trim());
                    map.put("district", districtName);
                    map.put("upazila", upazilaName);
                    map.put("password", password.getText().toString().trim());
                    Log.v(TAG, "SignupDataParams=" + map);
                    return map;
                }
            };
            dialog.show();
            FantacyApplication.getInstance().addToRequestQueue(req);
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    class DesPagerAdapter extends PagerAdapter {

        Context context;
        LayoutInflater inflater;
        String[] names;

        public DesPagerAdapter(Context act, String[] names) {
            this.names = names;
            this.context = act;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return names.length;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, final int position) {
            ViewGroup itemView = (ViewGroup) inflater.inflate(R.layout.signin_des_text,
                    collection, false);

            TextView name = itemView.findViewById(R.id.name);
            name.setText(names[position]);

            collection.addView(itemView, 0);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((ViewGroup) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}
