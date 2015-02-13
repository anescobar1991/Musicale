package com.anescobar.musicale.view.fragments;


import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;

import com.anescobar.musicale.app.services.exceptions.LocationNotAvailableException;
import com.anescobar.musicale.app.models.SearchLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

/**
 * This fragment should be extended by any fragment that needs location services
 * It adds GoogleAPiClient and some methods to be used with it
 */
public abstract class LocationAwareFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    protected GoogleApiClient mGoogleApiClient;
    protected SearchLocation mSearchLocation = SearchLocation.getInstance();


    public LocationAwareFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    protected LatLng getCurrentLatLng() throws LocationNotAvailableException {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (lastLocation != null) {
            mSearchLocation.searchLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        } else {
            throw new LocationNotAvailableException("LastLocation not available");
        }

        return mSearchLocation.searchLatLng;
    }
}
