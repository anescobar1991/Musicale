package com.anescobar.musicale.activities;

import android.os.Bundle;

import com.anescobar.musicale.utils.LocationNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by andres on 9/9/14.
 * Abstract class to be superclass for all activities that require location services
 * Includes common methods and setup for location services
 */
public abstract class LocationAwareActivity extends BaseActivity implements
        GooglePlayServicesClient.OnConnectionFailedListener,
        GooglePlayServicesClient.ConnectionCallbacks {

    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //instantiates the location client
        mLocationClient = new LocationClient(this, this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    public LatLng getCurrentLatLng() throws LocationNotAvailableException {
        LatLng currentLatLng;

        if (mLocationClient.getLastLocation() == null) {
            throw new LocationNotAvailableException("LastLocation not available");
        } else {
            currentLatLng = new LatLng(mLocationClient.getLastLocation().getLatitude(),
                    mLocationClient.getLastLocation().getLongitude());
        }
        return currentLatLng;
    }
}

