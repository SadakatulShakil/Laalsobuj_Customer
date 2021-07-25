package com.futureskyltd.app.fantacy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCommerzInitialization;
import com.sslwireless.sslcommerzlibrary.model.response.SSLCTransactionInfoModel;
import com.sslwireless.sslcommerzlibrary.model.util.SSLCCurrencyType;
import com.sslwireless.sslcommerzlibrary.model.util.SSLCSdkType;
import com.sslwireless.sslcommerzlibrary.view.singleton.IntegrateSSLCommerz;
import com.sslwireless.sslcommerzlibrary.viewmodel.listener.SSLCTransactionResponseListener;

import pl.droidsonroids.gif.GifImageView;

public class OrderPaymentActivity extends AppCompatActivity{

    GifImageView paymentView, successView, failedView;
    TextView backToHome;
    private String paymentStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_payment);
        inItView();

        Intent intent = getIntent();
        String grandTotal = intent.getStringExtra("amount");
        paymentStatus = intent.getStringExtra("paymentStatus");
        /*int mainBalance = Integer.parseInt(grandTotal);

        final SSLCommerzInitialization sslCommerzInitialization = new SSLCommerzInitialization(
                "futur5fb0b9d53986c",
                "futur5fb0b9d53986c@ssl",
                 mainBalance, SSLCCurrencyType.BDT,
                "123456789098765",
                "T-Shirt",
                 SSLCSdkType.TESTBOX);



        IntegrateSSLCommerz
                .getInstance(OrderPaymentActivity.this)
                .addSSLCommerzInitialization(sslCommerzInitialization)
                .buildApiCall(this);*/

        if(paymentStatus.equals("success")){
            successView.setVisibility(View.VISIBLE);
            backToHome.setVisibility(View.VISIBLE);
            paymentView.setVisibility(View.GONE);
            failedView.setVisibility(View.GONE);

        }else if(paymentStatus.equals("fail")){
            failedView.setVisibility(View.VISIBLE);
            backToHome.setVisibility(View.VISIBLE);
            paymentView.setVisibility(View.GONE);
            successView.setVisibility(View.GONE);

        }else if(paymentStatus.equals("validationError")){
            Toast.makeText(this, "Marchant Validation Error !", Toast.LENGTH_SHORT).show();
            paymentView.setVisibility(View.VISIBLE);
            backToHome.setVisibility(View.VISIBLE);
            failedView.setVisibility(View.GONE);
            successView.setVisibility(View.GONE);

        }
        backToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                FragmentMainActivity.orders = true;
                Intent main = new Intent(OrderPaymentActivity.this, FragmentMainActivity.class);
                main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(main);
            }
        });

    }

    private void inItView() {
        paymentView = findViewById(R.id.paymentView);
        successView = findViewById(R.id.successView);
        backToHome = findViewById(R.id.backToHomeBtn);
        failedView = findViewById(R.id.failedView);
    }

}