package com.anescobar.musicale.app.utils;

import android.app.Application;

import com.anescobar.musicale.app.MusicaleApp;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Andres Escobar on 3/17/15.
 * Class to handle Google Analytics hits
 */
public class AnalyticsUtil {
    private Application mApplication;

    public AnalyticsUtil(Application application) {
        this.mApplication = application;
    }


    public void sendAnalyticsScreenHit(String screenName) {
        Tracker t = ((MusicaleApp) mApplication).getTracker(
                MusicaleApp.TrackerName.APP_TRACKER);

        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void sendAnalyticsEvent(String category, String action, String label) {
        Tracker t = ((MusicaleApp) mApplication).getTracker(
                MusicaleApp.TrackerName.APP_TRACKER);

        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }
}
