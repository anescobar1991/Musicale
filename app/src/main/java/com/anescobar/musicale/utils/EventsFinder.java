package com.anescobar.musicale.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.anescobar.musicale.interfaces.OnEventsFetcherTaskCompleted;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.ResponseBuilder;
import de.umass.lastfm.Result;
import de.umass.lastfm.Session;
import de.umass.util.MapUtilities;

/**
 * Created by Andres Escobar on 8/2/14.
 * Performs search for Last FM events
 */
public class EventsFinder {
    private Session mSession;
    private OnEventsFetcherTaskCompleted mListener;
    private LatLng mLocation;

    public EventsFinder(Session session, OnEventsFetcherTaskCompleted listener, LatLng location) {
        this.mSession = session;
        this.mListener = listener;
        this.mLocation = location;
    }

    //publicly accessible method which is called to get Events back from backend
    public void getEvents(Integer pageNumber) {
        new EventsFetcherTask(mSession, mLocation, mListener).execute(pageNumber);
    }


    private PaginatedResult<Event> getRecommendedEvents(int page, int limit,
                                                              double lat, double lng) {
        Result result = Caller.getInstance().call("user.getRecommendedEvents", mSession, "page", String.valueOf(page),
                "user", mSession.getUsername(), "latitude", String.valueOf(lat), "longitude", String.valueOf(lng),
                "limit", String.valueOf(limit));
        return ResponseBuilder.buildPaginatedResult(result, Event.class);
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

        Map<String, String> params = new HashMap<String, String>();
        params.put("lat", String.valueOf(latitude));
        params.put("long", String.valueOf(longitude));
        params.put("distance", distance);
        MapUtilities.nullSafePut(params, "page", page);
        MapUtilities.nullSafePut(params, "limit", limit);
        MapUtilities.nullSafePut(params, "tag", tag);
        Result result = Caller.getInstance().call("geo.getEvents", mSession.getApiKey(), params);

        return ResponseBuilder.buildPaginatedResult(result, Event.class);
    }

    private class EventsFetcherTask extends AsyncTask<Integer, Void, PaginatedResult<Event>> {
        private Session mSession;
        private LatLng mUserLocation;
        private OnEventsFetcherTaskCompleted mListener;

        public EventsFetcherTask(Session session, LatLng userLocation, OnEventsFetcherTaskCompleted listener) {
            this.mSession = session;
            this.mUserLocation = userLocation;
            this.mListener = listener;
        }

        @Override
        protected PaginatedResult<Event> doInBackground(Integer... pageNumbers) {
            //necessary to fix bug in last fm library
            Caller.getInstance().setCache(null);

            //send server request to get events nearby
            return getEventsQuery(
                    mUserLocation.latitude, mUserLocation.longitude, "30", pageNumbers[0], 20, null
            );
        }

        @Override
        protected void onPreExecute() {
            mListener.onTaskAboutToStart();

        }

        @Override
        protected void onPostExecute(PaginatedResult<Event> events) {
            Result response = Caller.getInstance().getLastResult();
            Log.w("events_finder_response", response.toString());

            mListener.onTaskCompleted(events);
        }
    }

}