package com.anescobar.musicale.app.services.interfaces;

import java.util.Collection;

import de.umass.lastfm.Track;

/**
 * Created by andres on 10/23/14.
 * Interface to be used as callback for ArtistTopTracksFetcher asynctask
 */
public interface ArtistTopTracksFetcherTaskListener {
    void onArtistTopTrackFetcherTaskAboutToStart();
    void onArtistTopTrackFetcherTaskCompleted(Collection<Track> tracks);
}
