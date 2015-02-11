package com.anescobar.musicale.app.services.interfaces;

import java.util.Collection;

import de.umass.lastfm.Event;

/**
 * Created by Andres Escobar on 9/15/14.
 * Interface to be used as callback for VenueEventsFetcher asynctask
 */
public interface VenueEventsFetcherListener {

    void onVenueEventsFetcherTaskAboutToStart();
    void onVenueEventsFetcherTaskCompleted(Collection<Event> events);
}
