package com.anescobar.musicale.app.interfaces;

import java.util.Collection;

import de.umass.lastfm.Track;

/**
 * Created by andres on 10/23/14.
 */
public interface ArtistTopTracksFetcherTaskListener {
    void onArtistTopTrackFetcherTaskAboutToStart();
    void onArtistTopTrackFetcherTaskCompleted(Collection<Track> tracks);
}
