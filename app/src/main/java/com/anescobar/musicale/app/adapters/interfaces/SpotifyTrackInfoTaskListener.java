package com.anescobar.musicale.app.adapters.interfaces;

import android.view.View;

import com.anescobar.musicale.rest.models.SpotifyTrack;

import de.umass.lastfm.Artist;

/**
 * Created by Andres Escobar on 11/16/14.
 * Callback listener for spotify track info fetcher task
 */
public interface SpotifyTrackInfoTaskListener {

    void onSpotifyTrackInfoFetcherTaskAboutToStart(View view);
    void onSpotifyTrackInfoFetcherTaskCompleted(SpotifyTrack track, View view);
}
