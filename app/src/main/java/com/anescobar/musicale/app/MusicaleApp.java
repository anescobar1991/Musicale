package com.anescobar.musicale.app;

import android.app.Application;

import com.anescobar.musicale.R;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Andres Escobar on 2/10/15.
 * Extends application and keeps global state stuff
 */
public class MusicaleApp extends Application {
    // TODO: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "CCedkOaSl9mpDCANsDJPatKKm";
    private static final String TWITTER_SECRET = "Bx6w5Q2Nv8UXEBI2CcSAXjcLsWjKBbMmiOB4iiSHM4HHUW4Ik4";
    private static final String PROPERTY_ID = "UA-60833039-1";

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        TwitterAuthConfig authConfig =
                new TwitterAuthConfig(TWITTER_KEY,
                        TWITTER_SECRET);

        Fabric.with(this, new Crashlytics(),
                new Twitter(authConfig));
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t;
            if (trackerId == TrackerName.APP_TRACKER) {
                t = analytics.newTracker(R.xml.app_tracker);
            } else {
                t = analytics.newTracker(PROPERTY_ID);
            }
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
}
