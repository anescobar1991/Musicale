package com.anescobar.musicale.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.anescobar.musicale.R;

/**
 * Splash activity
 * @author Andres Escobar
 * @version 7/15/14
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startHomeActivity();
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
