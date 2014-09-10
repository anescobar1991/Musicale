package com.anescobar.musicale.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;

/**
 * Splash activity
 * @author Andres Escobar
 * @version 7/15/14
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startHomeActivity();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //connects location client
        mLocationClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //no need to get location unless requested by user if there is already location cached
        if (mCachedLatLng == null) {
            //gets current location
            LatLng currentLocation = getDevicesCurrentLatLng();

            //caches currentlocation in sharedPreferences
            cacheUserLatLng(currentLocation);
        }
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**
     * sends intent to start home activity
     */
    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
