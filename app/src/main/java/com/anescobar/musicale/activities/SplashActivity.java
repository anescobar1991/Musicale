package com.anescobar.musicale.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.anescobar.musicale.R;
import com.anescobar.musicale.utilsHelpers.SessionManager;
import com.anescobar.musicale.utilsHelpers.NetworkUtil;
import com.google.gson.Gson;

import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Result;
import de.umass.lastfm.Session;
import de.umass.lastfm.User;

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
                new ValidateSessionTask(session).execute(session); //calls async task that checks for session validity and acts on results
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
     * sends session object to home activity in bundle
     */
    private void startHomeActivity(Session session) {
        Gson gson = new Gson();
        String sessionString = gson.toJson(session); //serialized session object
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("com.anescobar.musicale.activities.HomeActivity.session", sessionString); //Puts session object to to Home Activity Intent
        startActivity(intent);
        finish();
    }

    //async task that performs heartbeat check for session using getRecommendedEvents method
    private class ValidateSessionTask extends AsyncTask<Session, Void, PaginatedResult<Event>> {
        private Session session;

        public ValidateSessionTask(Session session) {
            this.session = session;
        }

        @Override
        protected PaginatedResult<Event> doInBackground(Session... sessions) {
            Caller.getInstance().setCache(null);

            return User.getRecommendedEvents(sessions[0]);
        }

        protected void onPostExecute(PaginatedResult<Event> events) {
            Result response = Caller.getInstance().getLastResult();
            Log.w("session_getter_error", response.toString());
            if (events.isEmpty()) {
                startLoginActivity();
            } else {
                startHomeActivity(session);
            }
        }
    }

}
