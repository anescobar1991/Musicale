package com.anescobar.musicale.app.models;

import java.util.Collection;

import de.umass.lastfm.Artist;
import de.umass.lastfm.Event;
import de.umass.lastfm.Track;

/**
 * Created by andres on 1/11/15.
 */
public class ArtistDetails {
    public Artist mArtist;
    public Collection<Track> mTopTracks;
    public Collection<Event> mUpcomingEvents;

    public ArtistDetails() {}
}
