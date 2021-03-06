package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.futureskyltd.app.Api.ApiInterface;
import com.futureskyltd.app.Api.RetrofitClient;
import com.futureskyltd.app.ApiPojo.GeneralLogIn.GeneralLogIn;
import com.futureskyltd.app.ApiPojo.ShortAuth.ShortAuth;
import com.futureskyltd.app.external.CustomTextView;
import com.google.android.material.appbar.AppBarLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignInActivity extends BaseActivity{

    private CustomTextView signUpBtn;
    private LinearLayout fbSignIn;
    public static final String TAG = "SignIn";
    private CallbackManager callbackManager;
    private AppBarLayout appBarLayout;
    private RelativeLayout relativeLayout;
    private CustomTextView demoText, goBack;
    private EditText email, password;
    private TextView signIn;
    private String fbId;
    private ImageView cancel;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        inItView();

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        fbSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpFbData();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uEmail = email.getText().toString().trim();
                String uPassword = password.getText().toString().trim();

                if (uEmail.isEmpty()) {
                    email.setError(getString(R.string.please_enter_valid_user));
                    email.requestFocus();
                    return;
                }

                if (uPassword.isEmpty()) {
                    password.setError(getString(R.string.password_required));
                    password.requestFocus();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                setGeneralLogIn(uEmail, uPassword);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setGeneralLogIn(final String uEmail, final String uPassword) {

        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getRetrofitClient();
        ApiInterface api = retrofit.create(ApiInterface.class);

        Call<GeneralLogIn> generalLogInCall = api.postByGeneralLogIn(uEmail, uPassword);

        generalLogInCall.enqueue(new Callback<GeneralLogIn>() {
            @Override
            public void onResponse(Call<GeneralLogIn> call, Response<GeneralLogIn> response) {
                if(response.code() == 200){
                    progressBar.setVisibility(View.GONE);
                    GeneralLogIn generalLogIn = response.body();
                    if(generalLogIn.getStatus().equals("true")){
                        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                        preferences.edit().putString("TOKEN",generalLogIn.getToken()).apply();
                        preferences.edit().putString("customer_id", generalLogIn.getUserId()).apply();
                        preferences.edit().putString("customer_name", generalLogIn.getFullName()).apply();
                        preferences.edit().putString("customer_image", generalLogIn.getUserImage()).apply();
                        preferences.edit().putString("first_time_logged", generalLogIn.getFirstTimeLogged()).apply();
                        preferences.edit().putString("social_login", "no").apply();
                        Intent intent = new Intent(SignInActivity.this, FragmentMainActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignInActivity.this, getString(R.string.no_usr_found), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignInActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<GeneralLogIn> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SignInActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: "+t.getLocalizedMessage());
            }
        });

    }

    private void setUpFbData() {

        Log.d(TAG, "setUpFbData: " + "success");

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(SignInActivity.this,
                Arrays.asList("public_profile", "email"));

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Facebook success!");
                Log.d(TAG, "onSuccess: " + loginResult.getAccessToken().getUserId());
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

        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject profile, GraphResponse response) {

                        Log.d(TAG, "onCompleted: " + profile.toString());

                        try {
                            fbId = profile.getString("id");
                            String name = profile.getString("name");
                            String email = profile.getString("email");
                            final String firstName = profile.getString("first_name");

                            final String lastName = profile.getString("last_name");

                            Log.d(TAG, "onResponse: " + fbId + name + email + loginResult.getAccessToken().getToken());


                            Retrofit retrofit = RetrofitClient.getRetrofitClient();
                            ApiInterface api = retrofit.create(ApiInterface.class);

                            Call<ShortAuth> call = api.postBySaveFbData(fbId, email, loginResult.getAccessToken().getToken());

                            call.enqueue(new Callback<ShortAuth>() {
                                @Override
                                public void onResponse(Call<ShortAuth> call, Response<ShortAuth> response) {
                                    Log.d(TAG, "onResponse: " + response.code());
                                    if(response.code()==200){

                                        ShortAuth shortAuth = response.body();
                                        Log.d(TAG, "onResponse: " + shortAuth.toString());
                                        SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);

                                        if (shortAuth.getMessage().equals("incomplete")) {
                                            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                                            intent.putExtra("status", "incomplete");
                                            startActivity(intent);
                                            finish();
                                        }else if (shortAuth.getMessage().equals("enable")) {
                                            preferences.edit().putString("TOKEN",shortAuth.getToken()).apply();
                                            preferences.edit().putString("customer_id", shortAuth.getUser().getId()).apply();
                                            preferences.edit().putString("customer_name", shortAuth.getUser().getFirstName()).apply();
                                            preferences.edit().putString("customer_phone", shortAuth.getUser().getPhoneNo()).apply();
                                            preferences.edit().putString("customer_image", shortAuth.getUser().getProfileImage()).apply();
                                            preferences.edit().putString("social_login", "yes").apply();
                                            Intent intent = new Intent(SignInActivity.this, FragmentMainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }else if (shortAuth.getMessage().equals("disable")) {
                                            Toast.makeText(SignInActivity.this, getString(R.string.admin_block), Toast.LENGTH_SHORT).show();
                                        }else if (shortAuth.getMessage().equals("pending")) {
                                            appBarLayout.setVisibility(View.GONE);
                                            relativeLayout.setVisibility(View.GONE);
                                            demoText.setVisibility(View.VISIBLE);
                                            goBack.setVisibility(View.VISIBLE);
                                            goBack.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    appBarLayout.setVisibility(View.VISIBLE);
                                                    relativeLayout.setVisibility(View.VISIBLE);
                                                    demoText.setVisibility(View.GONE);
                                                    goBack.setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                    }

                                }

                                @Override
                                public void onFailure(Call<ShortAuth> call, Throwable t) {
                                    Toast.makeText(SignInActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "onSignUpFail: " + t.getMessage());
                                }
                            });


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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void inItView() {

        signUpBtn = findViewById(R.id.setSignUp);
        fbSignIn = findViewById(R.id.fbLog);
        appBarLayout = findViewById(R.id.appBar);
        relativeLayout = findViewById(R.id.relay);
        demoText = findViewById(R.id.errorPending);
        goBack = findViewById(R.id.goBack);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signIn = findViewById(R.id.signin);
        progressBar = findViewById(R.id.progressBar);
        cancel = findViewById(R.id.cancel);
    }
}