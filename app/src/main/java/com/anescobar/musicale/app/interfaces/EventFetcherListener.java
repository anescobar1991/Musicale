package com.anescobar.musicale.app.adapters.interfaces;

import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;

/**
 * Created by Andres Escobar on 8/11/14.
 * Interface to be used as callback for EventsFetcher asynctask
 */
public interface EventFetcherListener {
    void onEventFetcherTaskAboutToStart();
    void onEventFetcherTaskCompleted(PaginatedResult<Event> events);
}