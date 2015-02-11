package com.anescobar.musicale.app.services.interfaces;

import com.anescobar.musicale.app.models.SpotifyTrack;

/**
 * Created by Andres Escobar on 11/16/14.
 * Callback listener for spotify track info fetcher task
 */
public interface SpotifyTrackInfoTaskListener {

    void onSpotifyTrackInfoFetcherTaskAboutToStart(String trackId);
    void onSpotifyTrackInfoFetcherTaskCompleted(SpotifyTrack track);
}
