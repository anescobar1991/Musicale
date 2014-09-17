package com.anescobar.musicale.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.anescobar.musicale.interfaces.ArtistInfoFetcherTaskListener;
import com.anescobar.musicale.interfaces.ArtistUpcomingEventsFetcherTaskListener;

import de.umass.lastfm.Artist;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Result;

/**
 * Created by Andres Escobar on 9/16/14.
 * Handles all server calls to get artist details from Last.Fm API
 */
public class ArtistInfoSeeker {

    private static final String API_KEY = "824f19ce3c166a10c7b9858e3dfc3235";

    public ArtistInfoSeeker() {
    }

    public void getArtistInfo(String artist, ArtistInfoFetcherTaskListener listener) {
        new ArtistInfoFetcherTask(listener).execute(artist);
    }

    public void getArtistUpcomingEvents(String artist, ArtistUpcomingEventsFetcherTaskListener listener) {
        new ArtistUpcomingEventsFetcherTask(listener).execute(artist);
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
            Log.w("events_finder_response", response.toString());

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
}
