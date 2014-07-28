package com.anescobar.musicale.utilsHelpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Andres Escobar on 7/14/14.
 * Handles all network related tasks
 */
public class NetworkUtil {

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean networkAvailable = false;
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        Log.w("network_util:", "connected to network!");
                        networkAvailable = true;
                    }
                }
            }
        }
        return networkAvailable;
    }
}
