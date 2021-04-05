package com.futureskyltd.app.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.futureskyltd.app.fantacy.FantacyApplication;

/**
 * Created by hitasoft on 4/3/17.
 */

public class NetworkReceiver extends BroadcastReceiver {

    public static ConnectivityReceiverListener connectivityReceiverListener;

    public NetworkReceiver() {
        super();
    }

    public NetworkReceiver(ConnectivityReceiverListener listener) {
        connectivityReceiverListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        if (connectivityReceiverListener != null) {
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected());
        }
    }

    public static boolean isConnected() {
        ConnectivityManager
                cm = (ConnectivityManager) FantacyApplication.getInstance().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }


    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }
}
