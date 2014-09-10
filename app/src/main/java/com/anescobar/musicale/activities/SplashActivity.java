package com.anescobar.musicale.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;

/**
 * Splash activity
 * @author Andres Escobar
 * @version 7/15/14
 */
public class SplashActivity extends LocationAwareActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        //starts crashlytics
        Crashlytics.start(this);
    }

    /**
     * sends intent to start home activity
     */
    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //starts home activity upon location client being connected
        startHomeActivity();
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, R.string.error_no_network_connectivity, Toast.LENGTH_SHORT).show();
    }
}
