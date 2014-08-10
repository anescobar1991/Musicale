package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anescobar.musicale.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import de.umass.lastfm.Session;

/**
* A simple {@link Fragment} subclass.
* Activities that contain this fragment must implement the
* {@link EventsMapViewFragment.OnEventsMapViewFragmentInteractionListener} interface
* to handle interaction home.
* Use the {@link EventsMapViewFragment#newInstance} factory method to
* create an instance of this fragment.
*
*/
public class EventsMapViewFragment extends Fragment {
    private static final String ARG_SESSION_STRING = "com.anescobar.musicale.fragments.EventsMapViewFragment.session";
    private static final String ARG_USER_LOCATION_STRING = "com.anescobar.musicale.fragments.EventsMapViewFragment.userLocation";
    private Session mSession;
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private LatLng mUserLocation;
    private OnEventsMapViewFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param sessionString sessionString to be converted to a session object using GSON
     * @param userLocationString userLocationString to be converted to a LatLng object using GSON
     * @return A new instance of fragment EventsMapViewFragment.
     */
//    public static EventsMapViewFragment newInstance(String sessionString, String userLocationString) {
//        EventsMapViewFragment fragment = new EventsMapViewFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_SESSION_STRING, sessionString);
//        args.putString(ARG_USER_LOCATION_STRING, userLocationString);
//        fragment.setArguments(args);
//        return fragment;
//    }

    public EventsMapViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //gets arguments and deserializes them
        if (getArguments() != null) {
            String sessionString = getArguments().getString(ARG_SESSION_STRING);
            String userLocationString = getArguments().getString(ARG_USER_LOCATION_STRING);
            Gson gson = new Gson();
            mSession = gson.fromJson(sessionString, Session.class);
            mUserLocation = gson.fromJson(userLocationString, LatLng.class);
        }
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
        setUpMapIfNeeded(mUserLocation, mSession);
        // Apply any required UI change now that the Fragment is visible.
    }

    // Called at the start of the active lifetime.
    @Override
    public void onResume(){
        super.onResume();
        setUpMapIfNeeded(mUserLocation, mSession);
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
}
