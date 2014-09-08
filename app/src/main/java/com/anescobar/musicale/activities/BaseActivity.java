package com.anescobar.musicale.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.utils.EventQueryDetails;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by andres on 9/5/14.
 * Abstract class to be superclass for all activities
 * Includes common methods and functionality
 */
public abstract class BaseActivity extends Activity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    protected LocationClient mLocationClient;
    protected EventQueryDetails mEventQueryDetails = EventQueryDetails.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient = new LocationClient(this, this, this);
    }

    //adds fragment to activity
    protected void addFragmentToActivity(int container, Fragment fragment, String fragmentTag) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(container, fragment, fragmentTag)
                .commit();
    }

    //returns LatLng object for devices current location
    protected LatLng getDevicesCurrentLatLng() {
        //TODO refactor this so that it always sends me a location even if locatoin services if off or doesnt exist
        LatLng currentLocation = null;

        if (mLocationClient != null) {
            currentLocation = new LatLng(mLocationClient.getLastLocation().getLatitude(),
                    mLocationClient.getLastLocation().getLongitude());
        } else {
            Toast.makeText(this, R.string.error_no_network_connectivity, Toast.LENGTH_SHORT).show();
        }
        return currentLocation;
    }

}
