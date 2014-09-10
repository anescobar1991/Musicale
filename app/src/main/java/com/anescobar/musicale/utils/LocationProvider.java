package com.anescobar.musicale.utils;

import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by andres on 9/8/14.
 */
//        //TODO refactor this so that it always sends me a location even if locatoin services if off or doesnt exist
public class LocationProvider implements GooglePlayServicesClient.OnConnectionFailedListener,
        GooglePlayServicesClient.ConnectionCallbacks {

    public static LocationProvider mLocationProvider = new LocationProvider();
    private LocationClient mLocationClient;
    private LocationClientConnectionListener mListener;

    public interface LocationClientConnectionListener {
        public void onConnectionResult(boolean result);
    }

    public static LocationProvider getInstance() {
        if (mLocationProvider == null) {
            mLocationProvider = new LocationProvider();
        }
        return  mLocationProvider;
    }

    public void initialize(Activity activity) {
        mLocationClient = new LocationClient(activity, this, this);
        mListener = (LocationClientConnectionListener) activity;
    }

    public void connectClient() {
        mLocationClient.connect();
    }

    public void disconnectClient() {
        mLocationClient.disconnect();
    }

    public boolean isConnected() {
        return mLocationClient.isConnected();
    }

    public LatLng getCurrentLatLng() {
        Location location = mLocationClient.getLastLocation();
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onConnected(Bundle bundle) {
        mListener.onConnectionResult(true);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mListener.onConnectionResult(false);
    }
}