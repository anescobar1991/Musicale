package com.anescobar.musicale.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.anescobar.musicale.R;
import com.anescobar.musicale.interfaces.OnSessionHeartbeatCheckCompleted;
import com.anescobar.musicale.utilsHelpers.SessionManager;
import com.anescobar.musicale.utilsHelpers.NetworkUtil;

import de.umass.lastfm.Session;

/**
 * Splash activity
 * From here if there is a valid session then user is taken to home screen
 * else user is taken to login screen
 * @author Andres Escobar
 * @version 7/15/14
 */
public class SplashActivity extends Activity implements OnSessionHeartbeatCheckCompleted {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        NetworkUtil networkUtil = new NetworkUtil();
        SessionManager sessionManager = new SessionManager();

        //onCreate attempt to get session and start home activity with session
        //if there is no valid session then goes to login screen
        if (networkUtil.isNetworkAvailable(this)) {
            Session session = sessionManager.getSession(this);
            if (session != null) { // if session exists then need to check for its validity
                new SessionManager().validateSession(session, this); //calls async task that checks for session validity and acts on results
            } else {
                startLoginActivity(); //session is null so go to login screen
            }
        } else { //no network connection so go to login screen
            startLoginActivity();
        }
    }

    /**
     * sends intent to start login activity
     */
    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
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
    public void onHeartbeatCheckTaskCompleted(boolean sessionValid) {
        if (sessionValid) {
            startHomeActivity();
        } else {
            startLoginActivity();
        }
    }
}
