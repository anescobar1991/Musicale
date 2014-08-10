package com.anescobar.musicale.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.anescobar.musicale.R;
import com.anescobar.musicale.utilsHelpers.EventsFinder;
import com.anescobar.musicale.utilsHelpers.SessionManager;
import com.anescobar.musicale.utilsHelpers.NetworkUtil;

import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Result;
import de.umass.lastfm.Session;

/**
 * Splash activity
 * From here if there is a valid session then user is taken to home screen
 * else user is taken to login screen
 * @author Andres Escobar
 * @version 7/15/14
 */
public class SplashActivity extends Activity {

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
                new ValidateSessionTask().execute(session); //calls async task that checks for session validity and acts on results
            } else {
                startLoginActivity(); //session is null so go to login screen
            }
        } else { //no network connection so go to login screen
            startLoginActivity();
        }
    }

    //async task that performs heartbeat check for session using getRecommendedEvents method
    //that way is users stored session is invalid for some reason then user has chance to login before entering app
    private class ValidateSessionTask extends AsyncTask<Session, Void, PaginatedResult<Event>> {

        @Override
        protected PaginatedResult<Event> doInBackground(Session... sessions) {
            Caller.getInstance().setCache(null);
            //calls a generic last fm method simply to find out if session is valid from response
            return new EventsFinder(sessions[0]).getRecommendedEvents(1, 1, 0.00, 0.00);
        }

        protected void onPostExecute(PaginatedResult<Event> events) {
            Result response = Caller.getInstance().getLastResult();
            Log.w("session_getter_error", response.toString());
            if (response.isSuccessful()) {
                startHomeActivity();
            } else {
                startLoginActivity();
            }
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

}
