package com.anescobar.musicale.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.anescobar.musicale.utils.EventQueryDetails;
import com.anescobar.musicale.utils.LocationProvider;

/**
 * Created by andres on 9/5/14.
 * Abstract class to be superclass for all activities
 * Includes common methods and functionality
 */
public abstract class BaseActivity extends Activity implements
        LocationProvider.LocationClientConnectionListener {

    protected EventQueryDetails mEventQueryDetails = EventQueryDetails.getInstance();
    protected LocationProvider mLocationProvider = LocationProvider.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationProvider.initialize(this);
    }

    //adds fragment to activity
    protected void addFragmentToActivity(int container, Fragment fragment, String fragmentTag) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(container, fragment, fragmentTag)
                .commit();
    }
}