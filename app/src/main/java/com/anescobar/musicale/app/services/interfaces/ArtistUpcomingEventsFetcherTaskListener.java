package com.anescobar.musicale.app.services.interfaces;

import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;

/**
 * Created by Andres Escobar on 9/16/14.
 * Callback listener for artist upcoming events fetcher async task
 */
public interface ArtistUpcomingEventsFetcherTaskListener {
    void onArtistUpcomingEventsFetcherTaskAboutToStart();
    void onArtistUpcomingEventsFetcherTaskCompleted(PaginatedResult<Event> events);
}
