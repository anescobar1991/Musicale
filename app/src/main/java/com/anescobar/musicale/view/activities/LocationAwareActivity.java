package com.anescobar.musicale.view.activities;

import android.location.Location;
import android.os.Bundle;

import com.anescobar.musicale.app.exceptions.LocationNotAvailableException;
import com.anescobar.musicale.app.utils.SearchLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by andres on 9/9/14.
 * Abstract class to be superclass for all activities that require location services
 * Includes common methods and setup for location services
 */
public abstract class LocationAwareActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mGoogleApiClient;
    public SearchLocation mSearchLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchLocation = SearchLocation.getInstance();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    public LatLng getCurrentLatLng() throws LocationNotAvailableException {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (lastLocation == null) {
            throw new LocationNotAvailableException("LastLocation not available");
        }

        mSearchLocation.mSearchLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

        return mSearchLocation.mSearchLatLng;
    }

    public LatLng getSearchAreaLatLng() throws LocationNotAvailableException {
        if (mSearchLocation.mSearchLatLng == null) {
            throw new LocationNotAvailableException("SearchAreaLatLng is null, no lat lng has yet been cached");
        }
        return mSearchLocation.mSearchLatLng;
    }
}