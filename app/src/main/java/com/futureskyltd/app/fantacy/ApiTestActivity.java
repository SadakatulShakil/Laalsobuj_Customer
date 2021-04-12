package com.futureskyltd.app.fantacy;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.futureskyltd.app.Api.ApiInterface;
import com.futureskyltd.app.Api.RetrofitClient;
import com.futureskyltd.app.ApiPojo.ImageUpload.ImageUpload;
import com.futureskyltd.app.ApiPojo.ShortAuth.ShortAuth;
import com.squareup.picasso.Picasso;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ApiTestActivity extends AppCompatActivity {

    private Button clicked;
    private ImageView imageView;
    private Intent pickIntent, chooseIntent;
    private File proFile;
    private Uri proImageUri;
    private RequestBody requestBody;
    private String result = null;
    SharedPreferences preferences;
    private String accesstoken, customerId;
    public static final String TAG = "Api";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_test);
        clicked = findViewById(R.id.click);
        imageView = findViewById(R.id.imageView);
        preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        accesstoken = preferences.getString("TOKEN", null);
        customerId = preferences.getString("customer_id", null);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        clicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(proImageUri != null){
                    proFile = new File(getRealPathFromUri(proImageUri));
                    requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), proFile);
                    Log.d(TAG, "onImage: " + requestBody.toString());
                }

                Retrofit retrofit = RetrofitClient.getRetrofitClient();
                ApiInterface api = retrofit.create(ApiInterface.class);

                Call<ImageUpload> call = api.postByUploadImage(requestBody);

                call.enqueue(new Callback<ImageUpload>() {
                    @Override
                    public void onResponse(Call<ImageUpload> call, Response<ImageUpload> response) {
                        Log.d(TAG, "onResponse: " +response.code());
                        if(response.code() == 200){

                            Log.d(TAG, "onResponse: " + response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<ImageUpload> call, Throwable t) {
                        Log.d(TAG, "onResponse2: " + t.getMessage());
                        Log.d(TAG, "onResponse3: " + t.getLocalizedMessage());
                        t.printStackTrace();
                    }
                });
                
            }
        });
    }

    private void chooseImage() {
        chooseIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(chooseIntent, 111);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: " + "Activity result success");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 111) {
            if (resultCode == -1 && data != null && data.getData() != null) {
                proImageUri = data.getData();
                String name = proImageUri.getPath();
                Log.d(TAG, "onActivityResult: " + name);
                Picasso.get().load(proImageUri).into(imageView);
                //Picasso.get().load(proImageUri).into(backgroundImage);
            }
        }
    }

    private String getRealPathFromUri(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(ApiTestActivity.this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        if(cursor != null){

            result = cursor.getString(column_index);
        }
        cursor.close();
        return result;
    }
}