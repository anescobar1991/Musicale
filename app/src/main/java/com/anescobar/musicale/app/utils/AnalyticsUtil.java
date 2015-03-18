package com.anescobar.musicale.app.utils;

import android.app.Application;

import com.anescobar.musicale.app.MusicaleApp;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by andres on 3/17/15.
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
}
