package com.anescobar.musicale.app.models;

import java.util.Collection;

import de.umass.lastfm.Artist;
import de.umass.lastfm.Event;
import de.umass.lastfm.Track;

/**
 * Created by Andres Escobar on 1/11/15.
 * Model for artist details
 */
public class ArtistDetails {
    public Artist mArtist;
    public Collection<Track> mTopTracks;
    public Collection<Event> mUpcomingEvents;

    public ArtistDetails() {}
}
