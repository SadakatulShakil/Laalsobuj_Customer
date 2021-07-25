package com.futureskyltd.app.fantacy;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.futureskyltd.app.utils.GetSet;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SplashActivity extends BaseActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private static int SPLASH_TIME_OUT = 2000;
    public static SharedPreferences pref;
    public static Editor editor;
    String[] languages, langCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        pref = getApplicationContext().getSharedPreferences("FantacyPref", MODE_PRIVATE);
        if (pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(pref.getString("userId", null));
            GetSet.setUserName(pref.getString("userName", null));
            GetSet.setEmail(pref.getString("email", null));
            GetSet.setFullName(pref.getString("fullName", null));
            GetSet.setImageUrl(pref.getString("userImage", null));
        }

        if (ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            openActivity();
        }

    }

    private void openActivity() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this,
                        FragmentMainActivity.class);
                startActivity(i);
                finish();
                /*Intent i = new Intent(SplashActivity.this,
                        ApiTestActivity.class);
                startActivity(i);
                finish();*/


                /*SharedPreferences preferences = getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
                String retrievedToken  = preferences.getString("TOKEN",null);
                if(retrievedToken == null){
                    Intent i = new Intent(SplashActivity.this,
                            SignInActivity.class);
                    startActivity(i);
                    finish();
                }*/
               /* if(retrievedToken != null){
                    Intent i = new Intent(SplashActivity.this,
                        FragmentMainActivity.class);
                startActivity(i);
                finish();
                }*/


                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(TAG, "requestCode=" + requestCode);
        switch (requestCode) {
            case 100:
                openActivity();
                break;
        }
    }

}


	

