package com.anescobar.musicale.app.services.interfaces;

import com.twitter.sdk.android.core.Session;

/**
 * Created by Andres Escobar on 2/9/15.
 * Callback listener for TwitterGuestSessionFetcher task
 */
public interface TwitterGuestSessionFetcherListener {

    void onTwitterGuestSessionFetcherTaskAboutToStart();
    void onTwitterGuestSessionFetcherTaskSuccessful(Session session);
}
