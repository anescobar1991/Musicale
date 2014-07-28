package com.anescobar.musicale.utilsHelpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import de.umass.lastfm.Session;

/**
 * Created by Andres Escobar on 7/14/14.
 * Class to manage user's session
 */
public class SessionManager {

    /**
     * Caches session to sharedPreferences where it is persisted even when app is out of memory
     * else user is taken to login screen
     * @param  session session to be cached
     * @param context activity context
     */
    public void cacheSession(Session session, Context context) {
        Gson gson = new Gson();
        String sessionString = gson.toJson(session);

        SharedPreferences sharedPreferences = context.getSharedPreferences("userSession", 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.remove("session");
        sharedPreferencesEditor.putString("session", sessionString);
        sharedPreferencesEditor.apply();
    }

    /**
     * gets session from sharedPreferences
     * else user is taken to login screen
     * @param context activity context
     * @return session - Will be null if no session exists yet.
     */
    public Session getSession(Context context) {
        Session session = null;
        SharedPreferences sp = context.getSharedPreferences("userSession", 0);
        String sessionString = sp.getString("session", null);
        if (sessionString != null) {
            Gson gson = new Gson();
            session = gson.fromJson(sessionString, Session.class);
        }
        return session;
    }

    /**
     * discards current session from sharedPreferences
     * if session is not valid then shows alert view to inform user that session is invalid and they must login again
     * @param context  activity context
     */
    public void discardSession(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userSession", 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.clear();
        sharedPreferencesEditor.apply();
    }


}
