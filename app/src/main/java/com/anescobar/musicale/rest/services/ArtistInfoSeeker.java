package com.anescobar.musicale.rest.models.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.anescobar.musicale.app.adapters.interfaces.ArtistInfoFetcherTaskListener;
import com.anescobar.musicale.app.adapters.interfaces.ArtistTopTracksFetcherTaskListener;
import com.anescobar.musicale.app.adapters.interfaces.ArtistUpcomingEventsFetcherTaskListener;
import com.anescobar.musicale.app.adapters.utils.NetworkNotAvailableException;
import com.anescobar.musicale.app.adapters.utils.NetworkUtil;

import java.util.Collection;

import de.umass.lastfm.Artist;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Result;
import de.umass.lastfm.Track;

/**
 * Created by Andres Escobar on 9/16/14.
 * Handles all server calls to get artist details from Last.Fm API
 */
public class ArtistInfoSeeker {
    private NetworkUtil mNetworkUtil = new NetworkUtil();
    private static final String API_KEY = "824f19ce3c166a10c7b9858e3dfc3235";

    public ArtistInfoSeeker() {
    }

    public void getArtistInfo(String artist, ArtistInfoFetcherTaskListener listener, Context context) throws NetworkNotAvailableException {
        if (mNetworkUtil.isNetworkAvailable(context)) {
            new ArtistInfoFetcherTask(listener).execute(artist);
        } else {
            throw new NetworkNotAvailableException("Not connected to network...");
        }
    }

    public void getArtistUpcomingEvents(String artist, ArtistUpcomingEventsFetcherTaskListener listener, Context context) throws NetworkNotAvailableException {
        if (mNetworkUtil.isNetworkAvailable(context)) {
            new ArtistUpcomingEventsFetcherTask(listener).execute(artist);
        } else {
            throw new NetworkNotAvailableException("Not connected to network...");
        }
    }

    public void getArtistTopTracks(String artist, ArtistTopTracksFetcherTaskListener listener, Context context) throws NetworkNotAvailableException{
        if (mNetworkUtil.isNetworkAvailable(context)) {
            new ArtistTopTracksFetcherListener(listener).execute(artist);
        } else {
            throw new NetworkNotAvailableException("Not connected to network...");
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
            return Artist.getInfo(artists[0], API_KEY);
        }

        @Override
        protected void onPreExecute() {
            mListener.onArtistInfoFetcherTaskAboutToStart();

        }

        @Override
        protected void onPostExecute(Artist artist) {
            Result response = Caller.getInstance().getLastResult();
            Log.w("artist_info_seeker_response", response.toString());

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

            //send server request to get upcoming events for given artist
            return Artist.getEvents(artists[0], false, 1, 20, API_KEY);
        }

        @Override
        protected void onPreExecute() {
            mListener.onArtistUpcomingEventsFetcherTaskAboutToStart();

        }

        @Override
        protected void onPostExecute(PaginatedResult<Event> events) {
            Result response = Caller.getInstance().getLastResult();
            Log.w("events_finder_response", response.toString());

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
            return Artist.getTopTracks(artists[0], API_KEY);
        }

        @Override
        protected void onPreExecute() {
            mListener.onArtistTopTrackFetcherTaskAboutToStart();
        }

        @Override
        protected void onPostExecute(Collection<Track> tracks) {
            Result response = Caller.getInstance().getLastResult();
            Log.w("top_tracks_response", response.toString());

            mListener.onArtistTopTrackFetcherTaskCompleted(tracks);
        }
    }
}
