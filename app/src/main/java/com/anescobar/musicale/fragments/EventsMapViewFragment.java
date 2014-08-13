package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anescobar.musicale.R;
import com.anescobar.musicale.activities.HomeActivity;
import com.anescobar.musicale.utilsHelpers.SessionManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.umass.lastfm.Event;
import de.umass.lastfm.Session;

/**
* A simple {@link Fragment} subclass.
* Activities that contain this fragment must implement the
* {@link EventsMapViewFragment.OnEventsMapViewFragmentInteractionListener} interface
* to handle interaction home.
*
*/
public class EventsMapViewFragment extends Fragment {
    private Session mSession;
    private MapFragment mMapFragment;
    private ArrayList<Event> mEvents = new ArrayList<Event>();
    private GoogleMap mMap;
    private LatLng mUserLatLng;
    private OnEventsMapViewFragmentInteractionListener mListener;

    public EventsMapViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getCachedEvents();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_map_view, container, false);
        mMapFragment = new MapFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.eventsMapView_framelayout_container, mMapFragment)
                .commit();

        return view;
    }

    // Called at the start of the visible lifetime.
    @Override
    public void onStart(){
        super.onStart();
        setUpMapIfNeeded(mUserLatLng, mSession);
        // Apply any required UI change now that the Fragment is visible.
    }

    // Called at the start of the active lifetime.
    @Override
    public void onResume(){
        super.onResume();
        setUpMapIfNeeded(mUserLatLng, mSession);
        // Resume any paused UI updates, threads, or processes required
        // by the Fragment but suspended when it became inactive.
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnEventsMapViewFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnEventMapViewFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnEventsMapViewFragmentInteractionListener {
        public void onAttachDisplayTitle(int sectionIndex);
    }

    private void setUpMapIfNeeded(LatLng userLocation, Session session) {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = mMapFragment.getMap();
            // Check if we were successful in obtaining the map.
        }
        setUpMap(userLocation, session);
    }

    /**
     * This is where map is setup with home and with current or searched location as center
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(LatLng userLocation, Session session) {
        //TODO set up map here
        mMap.setBuildingsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }

    private void getCachedEvents() {
        Gson gson = new Gson();

        // Gets session from sharedPreferences
        SharedPreferences sessionPreferences = getActivity().getSharedPreferences(SessionManager.SESSION_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String serializedSession = sessionPreferences.getString("userSession", null);
        if (serializedSession != null) {
            mSession = gson.fromJson(serializedSession, Session.class);
        } //TODO here is where we check for and act on errors

        //Gets user's location(LatLng serialized into string) from sharedPreferences
        SharedPreferences userLocationPreferences = getActivity().getSharedPreferences(HomeActivity.LOCATION_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String serializedLatLng = userLocationPreferences.getString("userCurrentLatLng", null);
        if (serializedLatLng != null) {
            //deserializes userLatLng string into LatLng object
            mUserLatLng = gson.fromJson(serializedLatLng, LatLng.class);
        } //TODO here is where we check for and act on errors

        //Gets Events data from sharedPreferences
        SharedPreferences eventsPreferences = getActivity().getSharedPreferences(HomeActivity.EVENTS_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        //

        String serializedEvents = eventsPreferences.getString("events", null);

        //deserializes events if there are any
        if (serializedEvents != null) {
            Type listOfEvents = new TypeToken<ArrayList<Event>>(){}.getType();
            mEvents = gson.fromJson(serializedEvents, listOfEvents);

        }
    }
}
