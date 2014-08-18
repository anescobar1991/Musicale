package com.anescobar.musicale.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.anescobar.musicale.activities.HomeActivity;
import com.anescobar.musicale.interfaces.OnSessionHeartbeatCheckCompleted;
import com.google.gson.Gson;

import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Result;
import de.umass.lastfm.Session;
import de.umass.lastfm.User;

/**
 * Created by Andres Escobar on 7/14/14.
 * Class to manage user's session
 */
public class SessionManager {
    public static final String SESSION_SHARED_PREFS_NAME = "SessionPrefs";

    //validates if session is valid, callback method will return boolean isSessionValid to calling activity
    public void validateSession(Session session, OnSessionHeartbeatCheckCompleted listener) {
        new ValidateSessionTask(listener).execute(session);
    }

    /**
     * Caches session to sharedPreferences where it is persisted even when app is out of memory
     * @param  session session to be cached
     * @param context activity context
     */
    public void cacheSession(Session session, Context context) {
        Gson gson = new Gson();
        String sessionString = gson.toJson(session);

        SharedPreferences sharedPreferences = context.getSharedPreferences(SESSION_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString("userSession", sessionString);
        sharedPreferencesEditor.apply();
    }

    /**
     * gets session from sharedPreferences
     * @param context activity context
     * @return session - Will be null if no session exists yet.
     */
    public Session getSession(Context context) {
        Session session = null;
        SharedPreferences sp = context.getSharedPreferences(SESSION_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String sessionString = sp.getString("userSession", null);
        if (sessionString != null) {
            Gson gson = new Gson();
            session = gson.fromJson(sessionString, Session.class);
        }
        return session;
    }

    /**
     * discards current session, location info, and events stored from sharedPreferences
     * @param context  activity context
     */
    public void discardSession(Context context) {
        SharedPreferences sessionPreferences = context.getSharedPreferences(SESSION_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        sessionPreferences.edit()
                .clear()
                .apply();

        SharedPreferences locationPreferences = context.getSharedPreferences(HomeActivity.LOCATION_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        locationPreferences.edit()
                .clear()
                .apply();

        SharedPreferences eventsPreferences = context.getSharedPreferences(HomeActivity.EVENTS_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        eventsPreferences.edit()
                .clear()
                .apply();
    }

    //async task that performs heartbeat check for session using getRecommendedEvents method
    private class ValidateSessionTask extends AsyncTask<Session, Void, PaginatedResult<Event>> {
        private OnSessionHeartbeatCheckCompleted mListener;

        public ValidateSessionTask(OnSessionHeartbeatCheckCompleted listener) {
            this.mListener = listener;
        }

        @Override
        protected PaginatedResult<Event> doInBackground(Session... sessions) {
            Caller.getInstance().setCache(null);

            //calls a generic last fm method simply to find out if session is valid from response
            return User.getRecommendedEvents(1, sessions[0]);
        }
        @Override
        protected void onPostExecute(PaginatedResult<Event> events) {
            //gets result object for last call
            Result response = Caller.getInstance().getLastResult();
            Log.w("session_getter_error", response.toString());

            //informs callback method of whether last call with session given to validate was successful
            mListener.onHeartbeatCheckTaskCompleted(response.isSuccessful());
        }
    }

}
