package com.anescobar.musicale.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Andres Escobar on 2/10/15.
 * Extends application and keeps global state stuff
 */
public class MusicaleApp extends Application {
    // TODO: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "CCedkOaSl9mpDCANsDJPatKKm";
    private static final String TWITTER_SECRET = "Bx6w5Q2Nv8UXEBI2CcSAXjcLsWjKBbMmiOB4iiSHM4HHUW4Ik4";

    @Override
    public void onCreate() {
        super.onCreate();

        TwitterAuthConfig authConfig =
                new TwitterAuthConfig(TWITTER_KEY,
                        TWITTER_SECRET);

        Fabric.with(this, new Crashlytics(),
                new Twitter(authConfig));
    }
}
