package com.anescobar.musicale.utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import de.umass.lastfm.Event;

/**
 * Singleton class to maintain event query details
 * Created by andres on 9/7/14.
 */
public class EventQueryDetails {
    private static EventQueryDetails mInstance = null;

    public ArrayList<Event> events = new ArrayList<Event>();
    public LatLng currentLatLng;
    public int totalNumberOfEventPages = 0;
    public int numberOfEventPagesLoaded = 0;

    private EventQueryDetails() {
    }

    public static EventQueryDetails getInstance() {
        if (mInstance == null) {
            mInstance = new EventQueryDetails();
        }
        return mInstance;
    }

}
