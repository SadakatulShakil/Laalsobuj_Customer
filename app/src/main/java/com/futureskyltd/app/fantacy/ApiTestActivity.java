package com.futureskyltd.app.fantacy;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.futureskyltd.app.Api.ApiInterface;
import com.futureskyltd.app.Api.RetrofitClient;
import com.futureskyltd.app.ApiPojo.ShortAuth.ShortAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ApiTestActivity extends AppCompatActivity {

    private Button clicked;
    public static final String TAG = "Api";
    //public static final String BASE_URL = "http://192.168.2.131/fantacy_dev/api/hometest";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_test);
        clicked = findViewById(R.id.click);

        clicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Retrofit retrofit = RetrofitClient.getRetrofitClient();
                ApiInterface api = retrofit.create(ApiInterface.class);

                Call<ShortAuth> call = api.postBySaveFbData("123", "s@gmail.com", "ABC123");

                call.enqueue(new Callback<ShortAuth>() {
                    @Override
                    public void onResponse(Call<ShortAuth> call, Response<ShortAuth> response) {
                        Log.d(TAG, "onResponse: " +response.code());
                        if(response.code() == 200){
                            ShortAuth shortAuth = response.body();
                            Log.d(TAG, "onResponse: " + shortAuth.toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<ShortAuth> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.getMessage());
                    }
                });

               /* RequestQueue requestQueue = Volley.newRequestQueue(ApiTestActivity.this);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.APITEST, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response);
                        Intent intent = new Intent(ApiTestActivity.this, FragmentMainActivity.class);
                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: "+error.getMessage());
                    }
                }){
                    @Override
                    protected Map<String, String> getParams(){
                        Map<String, String> prams = new HashMap<String, String>();
                        prams.put("user_id", "12345678");
                        prams.put("email", "s@gmail.com");
                        prams.put("accessToken", "axcder44");
                        return prams;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> prams = new HashMap<String, String>();
                        prams.put("accept", "application/json");
                        //prams.put("content-type", "multipart/form-data");
                        //prams.put("content-type", "application/json");
                        return prams;
                    }
                };
                requestQueue.add(stringRequest);*/
            }
        });
    }
}