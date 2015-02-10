package com.anescobar.musicale.app.models;

import java.util.Collection;

import de.umass.lastfm.Event;
import de.umass.lastfm.Venue;

/**
 * Created by Andres Escobar on 1/11/15.
 * Model for venue details
 */
public class VenueDetails {
    public Venue mVenue;
    public Collection<Event> mUpcomingEvents;

    public VenueDetails() {}
}
