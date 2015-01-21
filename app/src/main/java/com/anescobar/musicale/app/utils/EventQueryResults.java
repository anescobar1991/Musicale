package com.anescobar.musicale.app.utils;

import java.util.ArrayList;

import de.umass.lastfm.Event;

/**
 * Singleton class to maintain event query details globally across app
 * Created by andres on 9/7/14.
 */
public class EventQueryResults {
    private static EventQueryResults sInstance = null;

    public ArrayList<Event> events = new ArrayList<Event>();
    public int totalNumberOfEventPages = 0;
    public int numberOfEventPagesLoaded = 0;

    private EventQueryResults() {
    }

    public static EventQueryResults getInstance() {
        if (sInstance == null) {
            sInstance = new EventQueryResults();
        }
        return sInstance;
    }

}
