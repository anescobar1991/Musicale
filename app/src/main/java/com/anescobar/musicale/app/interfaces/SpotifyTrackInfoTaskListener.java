package com.anescobar.musicale.app.interfaces;

import android.view.View;

import com.anescobar.musicale.rest.models.SpotifyTrack;

import de.umass.lastfm.Artist;

/**
 * Created by Andres Escobar on 11/16/14.
 * Callback listener for spotify track info fetcher task
 */
public interface SpotifyTrackInfoTaskListener {

    void onSpotifyTrackInfoFetcherTaskAboutToStart(String trackId);
    void onSpotifyTrackInfoFetcherTaskCompleted(SpotifyTrack track);
}
