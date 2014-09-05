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
import de.umass.util.MapUtilities;

/**
 * Created by Andres Escobar on 8/2/14.
 * Performs search for Last FM events
 */
public class EventsFinder {
    private OnEventsFetcherTaskCompleted mListener;
    private LatLng mLocation;
    private static final String API_KEY = "824f19ce3c166a10c7b9858e3dfc3235";

    public EventsFinder(OnEventsFetcherTaskCompleted listener, LatLng location) {
        this.mListener = listener;
        this.mLocation = location;
    }

    //publicly accessible method which is called to get Events back from backend
    public void getEvents(Integer pageNumber) {
        new EventsFetcherTask(mLocation, mListener).execute(pageNumber);
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
        Result result = Caller.getInstance().call("geo.getEvents", API_KEY , params);

        return ResponseBuilder.buildPaginatedResult(result, Event.class);
    }

    private class EventsFetcherTask extends AsyncTask<Integer, Void, PaginatedResult<Event>> {
        private LatLng mUserLocation;
        private OnEventsFetcherTaskCompleted mListener;

        public EventsFetcherTask(LatLng userLocation, OnEventsFetcherTaskCompleted listener) {
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