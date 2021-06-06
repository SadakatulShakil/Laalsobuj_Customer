package com.futureskyltd.app.fantacy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.futureskyltd.app.Adapter.DistrictAdapter;
import com.futureskyltd.app.Adapter.InfoApaAdapter;
import com.futureskyltd.app.Adapter.UpazilaAdapter;
import com.futureskyltd.app.Api.ApiInterface;
import com.futureskyltd.app.Api.RetrofitClient;
import com.futureskyltd.app.ApiPojo.District.District;
import com.futureskyltd.app.ApiPojo.District.DistrictList;
import com.futureskyltd.app.ApiPojo.InfoApa.Apa;
import com.futureskyltd.app.ApiPojo.InfoApa.InfoApaList;
import com.futureskyltd.app.ApiPojo.ShortAuth.ShortAuth;
import com.futureskyltd.app.ApiPojo.SignUpComplete.SignUpComplete;
import com.futureskyltd.app.ApiPojo.Upazila.Upazila;
import com.futureskyltd.app.ApiPojo.Upazila.UpazilatList;
import com.futureskyltd.app.external.CustomEditText;
import com.futureskyltd.app.external.CustomTextView;
import com.google.android.material.appbar.AppBarLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignUpActivity extends AppCompatActivity {

    private LinearLayout fbLoginLay, normalSignUpLay;
    public static final String TAG = "SignUp";
    private AppBarLayout demoAppBarLay;
    private CallbackManager callbackManager;
    private String districtName, upazilaName, infoApaName, fbId;
    private int district_id, upazila_id, infoApa_id;
    private ArrayList<District> mDistrictList = new ArrayList<>();
    private ArrayList<Upazila> mUpazilaList = new ArrayList<>();
    private ArrayList<Apa> mInfoApaList = new ArrayList<>();
    private DistrictAdapter mDistrictAdapter;
    private UpazilaAdapter mUpazilaAdapter;
    private InfoApaAdapter mInfoApaAdapter;
    private Spinner districtSpinner, upazilaSpinner, infoApaSpinner;
    private ProgressBar progressBar;
    private CustomTextView signUpBtn, errorMessage, goBack, signUpWithPhone;
    private CustomEditText nameEt, userNameEt, userEmailEt, userPhoneEt, userStoreNameEt, userAddressEt, userZipEt, passwordEt, confirmPasswordEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        inItView();


        fbLoginLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(SignUpActivity.this, FragmentMainActivity.class);
                startActivity(intent);
                finish();*/
                setUpFbData();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: "+ "success");
                setSignUp();
            }
        });

        //////Get District List/////////
        Retrofit retrofit = RetrofitClient.getRetrofitClient();
        ApiInterface api = retrofit.create(ApiInterface.class);

        Call<DistrictList> districtCall = api.getByDistrict();

        districtCall.enqueue(new Callback<DistrictList>() {
            @Override
            public void onResponse(Call<DistrictList> call, Response<DistrictList> response) {
                Log.d(TAG, "onResponse: " + response.code());
                DistrictList districtList = response.body();
                mDistrictList = (ArrayList<District>) districtList.getDistricts();
                Log.d(TAG, "onResponse: " + districtList.toString());
                Log.d(TAG, "onResponse: "+mDistrictList.size());
                mDistrictAdapter = new DistrictAdapter(SignUpActivity.this, mDistrictList);
                districtSpinner.setAdapter(mDistrictAdapter);

                districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        District clickedDistrict = (District) parent.getItemAtPosition(position);

                        districtName = clickedDistrict.getDistrict();
                        district_id = clickedDistrict.getId();

                        getUpazilaList(district_id);

                        Toast.makeText(SignUpActivity.this, districtName +" is selected !"+" id: "+ district_id, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

            }

            @Override
            public void onFailure(Call<DistrictList> call, Throwable t) {
                Log.d(TAG, "onGetDistrictFailure: " + t.getMessage());
            }
        });

    }

    private void setSignUp() {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String name = nameEt.getText().toString().trim();
        String userName = userNameEt.getText().toString().trim();
        String email = userEmailEt.getText().toString().trim();
        String phone = userPhoneEt.getText().toString().trim();
        String storeName = userStoreNameEt.getText().toString().trim();
        String address = userAddressEt.getText().toString().trim();
        String zip = userZipEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();
        String confirmPassword = confirmPasswordEt.getText().toString().trim();
        String facebookId =  fbId;
        int disId = district_id;
        int upId = upazila_id;
        int apaId = infoApa_id;

        if (name.isEmpty()) {
            nameEt.setError("Name is required!");
            nameEt.requestFocus();
            return;
        }
        if (userName.isEmpty()) {
            userNameEt.setError("userName is required!");
            userNameEt.requestFocus();
            return;
        }
        if (email.isEmpty() || !email.matches(emailPattern)) {
            userEmailEt.setError("Email is required!");
            userEmailEt.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            userPhoneEt.setError("phone number is required!");
            userPhoneEt.requestFocus();
            return;
        }
        if (storeName.isEmpty()) {
            userStoreNameEt.setError("storeName is required!");
            userStoreNameEt.requestFocus();
            return;
        }
        if (address.isEmpty()) {
            userAddressEt.setError("address is required!");
            userAddressEt.requestFocus();
            return;
        }
        if (zip.isEmpty()) {
            userZipEt.setError("zip is required!");
            userZipEt.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordEt.setError("password is required!");
            passwordEt.requestFocus();
            return;
        }
        if (confirmPassword.isEmpty()) {
            confirmPasswordEt.setError("confirmPassword is required!");
            confirmPasswordEt.requestFocus();
            return;
        }

        doSignUp(name, userName, email, phone, storeName, disId, upId, apaId, address, zip, password, confirmPassword, facebookId);

    }

    private void doSignUp(String name, String userName, String email, String phone,
                          String storeName, int disId, int upId, int apaId, String address,
                          String zip, String password, String confirmPassword, String facebookId) {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "doSignUp: "+disId+ " "+upId+ " "+apaId+ " "+facebookId);
        Retrofit retrofit = RetrofitClient.getRetrofitClient();
        ApiInterface api = retrofit.create(ApiInterface.class);

        Call<SignUpComplete> signUpCall = api.postByCompleteSignUp(name, userName, email, phone, password,
                confirmPassword, storeName, disId, upId, apaId, address, zip, facebookId);

        signUpCall.enqueue(new Callback<SignUpComplete>() {
            @Override
            public void onResponse(Call<SignUpComplete> call, Response<SignUpComplete> response) {
                Log.d(TAG, "onSignUpRes: " + response.code());
                if(response.code() == 200){
                    progressBar.setVisibility(View.GONE);
                    SignUpComplete signUpComplete = response.body();
                    Log.d(TAG, "onSignUpRes: "+ signUpComplete.toString());

                    if (signUpComplete.getMessage().equals("incomplete")) {
                        demoAppBarLay.setVisibility(View.GONE);
                        fbLoginLay.setVisibility(View.GONE);
                        signUpWithPhone.setVisibility(View.GONE);
                        normalSignUpLay.setVisibility(View.VISIBLE);
                    }else if (signUpComplete.getMessage().equals("enable")) {
                        Intent intent = new Intent(SignUpActivity.this, FragmentMainActivity.class);
                        startActivity(intent);
                        finish();
                    }else if (signUpComplete.getMessage().equals("disable")) {
                        Toast.makeText(SignUpActivity.this, "You are Blocked Your Account deleted by Admin! ", Toast.LENGTH_SHORT).show();
                    }else if (signUpComplete.getMessage().equals("pending")) {

                        demoAppBarLay.setVisibility(View.VISIBLE);
                        signUpWithPhone.setVisibility(View.GONE);
                        fbLoginLay.setVisibility(View.GONE);
                        normalSignUpLay.setVisibility(View.GONE);
                        errorMessage.setVisibility(View.VISIBLE);
                        goBack.setVisibility(View.VISIBLE);

                        goBack.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                    else if(signUpComplete.getMessage().equals("error")){
                        if(signUpComplete.getError().getEmail() != null){
                            userEmailEt.setError(signUpComplete.getError().getEmail().getEmpty());
                            userEmailEt.requestFocus();
                        }
                        if(signUpComplete.getError().getPhoneNo() != null){
                            userPhoneEt.setError(signUpComplete.getError().getPhoneNo().getNumeric());
                            userPhoneEt.requestFocus();
                        }
                    }
                }


            }

            @Override
            public void onFailure(Call<SignUpComplete> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "onsignUpFailure: " + t.getMessage());
            }
        });

    }


    private void getUpazilaList(int district_id) {
        Retrofit retrofit = RetrofitClient.getRetrofitClient();
        ApiInterface api = retrofit.create(ApiInterface.class);

        Call<UpazilatList> upazilaCall = api.postByUpazila(district_id);

        upazilaCall.enqueue(new Callback<UpazilatList>() {
            @Override
            public void onResponse(Call<UpazilatList> call, Response<UpazilatList> response) {
                Log.d(TAG, "onResponse: " + response.code());
                UpazilatList upazilatList = response.body();
                mUpazilaList = (ArrayList<Upazila>) upazilatList.getUpazila();
                Log.d(TAG, "onResponse: " + upazilatList.toString());
                mUpazilaAdapter = new UpazilaAdapter(SignUpActivity.this, mUpazilaList);
                upazilaSpinner.setAdapter(mUpazilaAdapter);

                upazilaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Upazila clickedUpazila = (Upazila) parent.getItemAtPosition(position);

                        upazilaName = clickedUpazila.getUpazila();
                        upazila_id = clickedUpazila.getId();

                        getInfoApaList(district_id, upazila_id);

                        Toast.makeText(SignUpActivity.this, upazilaName +" is selected !"+" id: "+ upazila_id, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onFailure(Call<UpazilatList> call, Throwable t) {
                Log.d(TAG, "onGetUpazilaFailure: " + t.getMessage());
            }
        });
    }

    private void getInfoApaList(int district_id, int upazila_id) {

        Retrofit retrofit = RetrofitClient.getRetrofitClient();
        ApiInterface api = retrofit.create(ApiInterface.class);

        Call<InfoApaList> infoApaCall = api.postByInfoApa(district_id, upazila_id);

        infoApaCall.enqueue(new Callback<InfoApaList>() {
            @Override
            public void onResponse(Call<InfoApaList> call, Response<InfoApaList> response) {

                Log.d(TAG, "onResponse: " + response.code());
                InfoApaList infoApaList = response.body();
                mInfoApaList = (ArrayList<Apa>) infoApaList.getApa();
                Log.d(TAG, "onResponse: " + infoApaList.toString());
                mInfoApaAdapter = new InfoApaAdapter(SignUpActivity.this, mInfoApaList);
                infoApaSpinner.setAdapter(mInfoApaAdapter);

                infoApaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Apa clickedInfoApa = (Apa) parent.getItemAtPosition(position);

                        infoApaName = clickedInfoApa.getName();
                        infoApa_id = clickedInfoApa.getId();

                        Toast.makeText(SignUpActivity.this, infoApaName +" is selected !"+" id: "+ infoApa_id, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

            }

            @Override
            public void onFailure(Call<InfoApaList> call, Throwable t) {
                Log.d(TAG, "onGetInfoApaFailure: " + t.getMessage());
            }
        });

    }

    private void setUpFbData() {
        Log.d(TAG, "setUpFbData: " + "success");

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(SignUpActivity.this,
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
                                        Log.d(TAG, "onResponse1: " + shortAuth.toString());

                                    if (shortAuth.getMessage().equals("incomplete")) {
                                        demoAppBarLay.setVisibility(View.GONE);
                                        fbLoginLay.setVisibility(View.GONE);
                                        signUpWithPhone.setVisibility(View.GONE);
                                        normalSignUpLay.setVisibility(View.VISIBLE);
                                    }else if (shortAuth.getMessage().equals("enable")) {
                                            Intent intent = new Intent(SignUpActivity.this, FragmentMainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }else if (shortAuth.getMessage().equals("disable")) {
                                        Toast.makeText(SignUpActivity.this, "You are Blocked Your Account deleted by Admin! ", Toast.LENGTH_SHORT).show();
                                    }else if (shortAuth.getMessage().equals("pending")) {

                                        demoAppBarLay.setVisibility(View.VISIBLE);
                                        fbLoginLay.setVisibility(View.GONE);
                                        signUpWithPhone.setVisibility(View.GONE);
                                        normalSignUpLay.setVisibility(View.GONE);
                                        errorMessage.setVisibility(View.VISIBLE);
                                        goBack.setVisibility(View.VISIBLE);

                                        goBack.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    }
                                    }

                                }

                                @Override
                                public void onFailure(Call<ShortAuth> call, Throwable t) {
                                    Toast.makeText(SignUpActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
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
        fbLoginLay = findViewById(R.id.fbLay);
        normalSignUpLay = findViewById(R.id.signuplay);
        demoAppBarLay = findViewById(R.id.appBar);

        districtSpinner = findViewById(R.id.userDistrictSpinner);
        upazilaSpinner = findViewById(R.id.userUpazilaSpinner);
        infoApaSpinner = findViewById(R.id.infoApaSpinner);

        nameEt = findViewById(R.id.name);
        userNameEt = findViewById(R.id.username);
        userEmailEt = findViewById(R.id.email);
        userPhoneEt = findViewById(R.id.userPhone);
        userStoreNameEt = findViewById(R.id.storeName);
        userAddressEt = findViewById(R.id.address);
        userZipEt = findViewById(R.id.zipcode);

        passwordEt = findViewById(R.id.password);
        confirmPasswordEt = findViewById(R.id.repassword);

        signUpBtn = findViewById(R.id.signup);
        errorMessage = findViewById(R.id.errorPending);
        progressBar = findViewById(R.id.progressBar);

        goBack = findViewById(R.id.goBack);
        signUpWithPhone = findViewById(R.id.signinWithPhone);

    }
}