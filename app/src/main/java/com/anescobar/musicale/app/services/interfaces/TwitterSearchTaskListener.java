package com.anescobar.musicale.app.services.interfaces;

import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

/**
 * Created by Andres Escobar on 2/9/15.
 * Callback listener for TwitterSearch task
 */
public interface TwitterSearchTaskListener {

    void onTwitterSearchTaskAboutToStart();
    void onTwitterSearchTaskSuccessful(List<Tweet> tweets);
}
