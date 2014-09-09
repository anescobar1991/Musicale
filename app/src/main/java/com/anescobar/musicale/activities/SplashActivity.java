package com.anescobar.musicale.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.utils.LocationProvider;
import com.crashlytics.android.Crashlytics;

/**
 * Splash activity
 * @author Andres Escobar
 * @version 7/15/14
 */
public class SplashActivity extends BaseActivity {

    private LocationProvider mLocationProvider = LocationProvider.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //starts crashlytics
        Crashlytics.start(this);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client
        mLocationProvider.connectClient();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationProvider.disconnectClient();
        super.onStop();
    }

    @Override
    public void onConnectionResult(boolean success) {
        if (success) {
            mEventQueryDetails.currentLatLng = mLocationProvider.getCurrentLatLng();

            startHomeActivity();
        } else {
            Toast.makeText(this, R.string.error_no_network_connectivity, Toast.LENGTH_SHORT).show();
        }
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
