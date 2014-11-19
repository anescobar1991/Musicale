package com.anescobar.musicale.app.adapters.models;

import java.util.ArrayList;

import de.umass.lastfm.Event;

/**
 * Singleton class to maintain event query details
 * Created by andres on 9/7/14.
 */
public class EventQueryDetails {
    private static EventQueryDetails sInstance = null;

    public ArrayList<Event> events = new ArrayList<Event>();
    public int totalNumberOfEventPages = 0;
    public int numberOfEventPagesLoaded = 0;

    private EventQueryDetails() {
    }

    public static EventQueryDetails getInstance() {
        if (sInstance == null) {
            sInstance = new EventQueryDetails();
        }
        return sInstance;
    }

}
