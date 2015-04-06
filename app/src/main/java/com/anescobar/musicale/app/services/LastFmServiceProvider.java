package com.anescobar.musicale.app.services;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.anescobar.musicale.BuildConfig;
import com.anescobar.musicale.R;
import com.anescobar.musicale.app.services.interfaces.ArtistInfoFetcherTaskListener;
import com.anescobar.musicale.app.services.interfaces.ArtistTopTracksFetcherTaskListener;
import com.anescobar.musicale.app.services.interfaces.ArtistUpcomingEventsFetcherTaskListener;
import com.anescobar.musicale.app.services.interfaces.EventFetcherListener;
import com.anescobar.musicale.app.services.interfaces.VenueEventsFetcherListener;
import com.anescobar.musicale.app.utils.NetworkUtil;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.umass.lastfm.Artist;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.ResponseBuilder;
import de.umass.lastfm.Result;
import de.umass.lastfm.Track;
import de.umass.lastfm.Venue;
import de.umass.util.MapUtilities;

/**
 * Created by Andres Escobar on 9/16/14.
 * Handles all Last.FM API server calls
 */
public class LastFmServiceProvider {
    private static final String SEARCH_RADIUS = "100"; //event search radius distance in KMs
    private NetworkUtil mNetworkUtil = new NetworkUtil();
    private final String mApiKey;
    private Context mContext;

    public LastFmServiceProvider(Context context) {
        mContext = context;
        mApiKey = context.getString(R.string.last_fm_key);
    }

    //publicly accessible method which is called to get Events back from backend
    public void getEvents(Integer pageNumber, LatLng location, EventFetcherListener eventFetcherListener, Context context) throws NetworkErrorException {
        if (mNetworkUtil.isNetworkAvailable(context)) {
            new EventsFetcherTask(location, eventFetcherListener).execute(pageNumber);
        } else {
            throw new NetworkErrorException("Not connected to network...");
        }
    }

    //publicly accessible method which is called to get upcoming events at venue
    public void getUpcomingEventsAtVenue(String venueId, VenueEventsFetcherListener venueEventsFetcherListener, Context context) throws NetworkErrorException {
        if (mNetworkUtil.isNetworkAvailable(context)) {
            new VenueEventsFetcherTask(venueEventsFetcherListener).execute(venueId);
        } else {
            throw new NetworkErrorException("Not connected to network...");
        }
    }

    public void getArtistInfo(String artist, ArtistInfoFetcherTaskListener listener) throws NetworkErrorException {
        if (mNetworkUtil.isNetworkAvailable(mContext)) {
            new ArtistInfoFetcherTask(listener).execute(artist);
        } else {
            throw new NetworkErrorException("Not connected to network...");
        }
    }

    public void getArtistUpcomingEvents(String artist, ArtistUpcomingEventsFetcherTaskListener listener) throws NetworkErrorException {
        if (mNetworkUtil.isNetworkAvailable(mContext)) {
            new ArtistUpcomingEventsFetcherTask(listener).execute(artist);
        } else {
            throw new NetworkErrorException("Not connected to network...");
        }
    }

    public void getArtistTopTracks(String artist, ArtistTopTracksFetcherTaskListener listener) throws NetworkErrorException{
        if (mNetworkUtil.isNetworkAvailable(mContext)) {
            new ArtistTopTracksFetcherListener(listener).execute(artist);
        } else {
            throw new NetworkErrorException("Not connected to network...");
        }
    }

    private class ArtistInfoFetcherTask extends AsyncTask<String, Void, Artist> {
        private ArtistInfoFetcherTaskListener mListener;

        public ArtistInfoFetcherTask(ArtistInfoFetcherTaskListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Artist doInBackground(String... artists) {
            //necessary to fix bug in last fm library
            Caller.getInstance().setCache(null);

            //send server request to get upcoming events for given venue
            return Artist.getInfo(artists[0], mApiKey);
        }

        @Override
        protected void onPreExecute() {
            mListener.onArtistInfoFetcherTaskAboutToStart();
        }

        @Override
        protected void onPostExecute(Artist artist) {
            if (BuildConfig.DEBUG) {
                Result response = Caller.getInstance().getLastResult();

                Log.w("artist_info_seeker_res", response.toString());
            }

            mListener.onArtistInfoFetcherTaskCompleted(artist);
        }
    }

    private class ArtistUpcomingEventsFetcherTask extends AsyncTask<String, Void, PaginatedResult<Event>> {
        private ArtistUpcomingEventsFetcherTaskListener mListener;

        public ArtistUpcomingEventsFetcherTask(ArtistUpcomingEventsFetcherTaskListener listener) {
            this.mListener = listener;
        }

        @Override
        protected PaginatedResult<Event> doInBackground(String... artists) {
            //necessary to fix bug in last fm library
            Caller.getInstance().setCache(null);

            return Artist.getEvents(artists[0], false, 1, 20, mApiKey);
        }

        @Override
        protected void onPreExecute() {
            mListener.onArtistUpcomingEventsFetcherTaskAboutToStart();

        }

        @Override
        protected void onPostExecute(PaginatedResult<Event> events) {
            if (BuildConfig.DEBUG) {
                Result response = Caller.getInstance().getLastResult();

                Log.w("events_finder_response", response.toString());
            }

            mListener.onArtistUpcomingEventsFetcherTaskCompleted(events);
        }
    }

    private class ArtistTopTracksFetcherListener extends AsyncTask<String, Void, Collection<Track>> {
        private ArtistTopTracksFetcherTaskListener mListener;

        public ArtistTopTracksFetcherListener(ArtistTopTracksFetcherTaskListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Collection<Track> doInBackground(String... artists) {
            return Artist.getTopTracks(artists[0], mApiKey);
        }

        @Override
        protected void onPreExecute() {
            mListener.onArtistTopTrackFetcherTaskAboutToStart();
        }

        @Override
        protected void onPostExecute(Collection<Track> tracks) {
            if (BuildConfig.DEBUG) {
                Result response = Caller.getInstance().getLastResult();

                Log.w("top_tracks_response", response.toString());
            }

            mListener.onArtistTopTrackFetcherTaskCompleted(tracks);
        }
    }

    /**
     * Get all events within the specified distance of the location specified by latitude/longitude.<br/>
     * This method only returns the specified page of a paginated result.
     *
     * @param latitude Latitude
     * @param longitude Longitude
     * @param distance Find events within a specified radius (in kilometres)
     * @param page A page number for pagination
     * @param limit The maximum number of items returned per page
     * @param tag search tag
     * @return a {@link PaginatedResult} containing a list of events
     */
    private PaginatedResult<Event> getEventsQuery(double latitude, double longitude, String distance,
                                                  int page, int limit, String tag) {

        Map<String, String> params = new HashMap<>();
        params.put("lat", String.valueOf(latitude));
        params.put("long", String.valueOf(longitude));
        params.put("distance", distance);
        MapUtilities.nullSafePut(params, "page", page);
        MapUtilities.nullSafePut(params, "limit", limit);
        MapUtilities.nullSafePut(params, "tag", tag);

        Result result = Caller.getInstance().call("geo.getEvents", mApiKey , params);

        return ResponseBuilder.buildPaginatedResult(result, Event.class);
    }

    private class EventsFetcherTask extends AsyncTask<Integer, Void, PaginatedResult<Event>> {
        private LatLng mUserLocation;
        private EventFetcherListener mListener;

        public EventsFetcherTask(LatLng userLocation, EventFetcherListener listener) {
            this.mUserLocation = userLocation;
            this.mListener = listener;
        }

        @Override
        protected PaginatedResult<Event> doInBackground(Integer... pageNumbers) {
            //necessary to fix bug in last fm library
            Caller.getInstance().setCache(null);

            //send server request to get events nearby
            return getEventsQuery(
                    mUserLocation.latitude, mUserLocation.longitude, SEARCH_RADIUS, pageNumbers[0], 20, null
            );
        }

        @Override
        protected void onPreExecute() {
            mListener.onEventFetcherTaskAboutToStart();

        }

        @Override
        protected void onPostExecute(PaginatedResult<Event> events) {
            if (BuildConfig.DEBUG) {
                Result response = Caller.getInstance().getLastResult();

                Log.w("events_finder_response", response.toString());
            }

            mListener.onEventFetcherTaskCompleted(events);
        }
    }

    private class VenueEventsFetcherTask extends AsyncTask<String, Void, Collection<Event>> {
        private VenueEventsFetcherListener mListener;

        public VenueEventsFetcherTask(VenueEventsFetcherListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Collection<Event> doInBackground(String... venueIds) {
            //necessary to fix bug in last fm library
            Caller.getInstance().setCache(null);

            //send server request to get upcoming events for given venue
            return Venue.getEvents(venueIds[0], false, mApiKey);
        }

        @Override
        protected void onPreExecute() {
            mListener.onVenueEventsFetcherTaskAboutToStart();

        }

        @Override
        protected void onPostExecute(Collection<Event> events) {
            if (BuildConfig.DEBUG) {
                Result response = Caller.getInstance().getLastResult();
                Log.w("events_finder_response", response.toString());
            }

            mListener.onVenueEventsFetcherTaskCompleted(events);
        }
    }
}
