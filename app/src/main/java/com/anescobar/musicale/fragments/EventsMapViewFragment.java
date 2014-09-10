package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.activities.EventDetailsActivity;
import com.anescobar.musicale.activities.EventsActivity;
import com.anescobar.musicale.interfaces.OnEventsFetcherTaskCompleted;
import com.anescobar.musicale.utils.EventsFinder;
import com.anescobar.musicale.utils.NetworkUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.PaginatedResult;

/**
* A simple {@link Fragment} subclass.
* Activities that contain this fragment must implement the
* {@link EventsMapViewFragment.OnEventsMapViewFragmentInteractionListener} interface
* to handle interaction home.
*
*/
public class EventsMapViewFragment extends Fragment implements OnEventsFetcherTaskCompleted,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnCameraChangeListener {

    private MapFragment mMapFragment;
    private NetworkUtil mNetworkUtil;
    private GoogleMap mMap;
    private ProgressBar mEventsLoadingProgressbar;
    private Button mRedoSearchButton;
    private OnEventsMapViewFragmentInteractionListener mListener;

    private int mTotalNumberOfPages = 0; // stores how many total pages of events there are
    private int mNumberOfPagesLoaded = 0; //keeps track of how many pages are loaded
    private int mCameraChangeCount = 0; //keeps track of how many times map camera change has occurred
    private LatLng mUserLatLng;
    private HashMap<String, Event> mMarkers = new HashMap<String, Event>();
    private ArrayList<LatLng> mMarkerPositions = new ArrayList<LatLng>();
    private ArrayList<Event> mEvents = new ArrayList<Event>();

    public EventsMapViewFragment() {
        // Required empty public constructor
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnEventsMapViewFragmentInteractionListener {
        public void cacheEvents(int numberOfPagesLoaded, int totalNumberOfPages,ArrayList<Event> events);
        public void cacheUserLatLng(LatLng latLng);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initializes networkUtil class
        mNetworkUtil = new NetworkUtil();

        //gets all sharedPreferences and stores them locally
        getCachedEvents();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_map_view, container, false);
        //creates MapFragment
        mMapFragment = new MapFragment();

        //displays map fragment on screen
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_events_map_view_container, mMapFragment)
                .commit();

        mRedoSearchButton = (Button) view.findViewById(R.id.fragment_events_map_redo_search);
        mEventsLoadingProgressbar = (ProgressBar) view.findViewById(R.id.fragment_events_map_view_events_loading);

        //sets click listener for when user taps anywhere in map
        mRedoSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng newLatLng = mMap.getCameraPosition().target;

                //caches map center to sharedPreferences
                mListener.cacheUserLatLng(newLatLng);

                //calls getEvents method, which gets events from backend and displays and stores them as needed
                getEventsFromServer(1, newLatLng);
            }
        });

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        //sets up map, with its settings, and adds event markers
        setUpMapIfNeeded(mUserLatLng);
    }

    @Override
    public void onResume(){
        super.onResume();
        //sets up map, with its settings, and adds event markers
        setUpMapIfNeeded(mUserLatLng);
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
    public void onPause() {
        //caches all events data to sharedPreferences
        mListener.cacheEvents(mNumberOfPagesLoaded, mTotalNumberOfPages, mEvents);
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //gets events from backend
    public void getEventsFromServer(Integer pageNumber, LatLng userLocation) {

        if (mNetworkUtil.isNetworkAvailable(getActivity())) {
            new EventsFinder(this, userLocation).getEvents(pageNumber);
        } else {
            Toast.makeText(getActivity(),getString(R.string.error_no_network_connectivity),Toast.LENGTH_SHORT).show();
        }
    }

    //sets up map if it hasnt already been setup,
    private void setUpMapIfNeeded(LatLng cachedLocation) {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = mMapFragment.getMap();

            //set custom marker info window adapter
            mMap.setInfoWindowAdapter(new EventMarkerInfoWindowAdapter(getActivity()));
        }
        setUpMap(cachedLocation);
    }

    /**
     * This is where map is setup with home and with current or searched location as center
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(LatLng userLocation) {
        //sets map's initial state
        UiSettings mapSettings = mMap.getUiSettings();

        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
        mMap.setMyLocationEnabled(true);
        mapSettings.setZoomControlsEnabled(false);
        mapSettings.setMyLocationButtonEnabled(false);
        mapSettings.setTiltGesturesEnabled(false);
        mapSettings.setRotateGesturesEnabled(false);

        //if there are no events from previous saved session then fetch events from backend
        //else use events from previous saved session to populate cards
        if (mEvents.isEmpty()) {
            getEventsFromServer(1, userLocation);
        } else {
            displayEventsInMap();
        }

        //sets maps' onInfoWindow click listener
        mMap.setOnInfoWindowClickListener(this);

        //sets maps' onCameraChange listener
        mMap.setOnCameraChangeListener(this);
    }

    //iterates through arrayList of Events and displays them on map
    private void displayEventsInMap() {
        //clears old events from map
        mMap.clear();

        //iterate through events list
        for (Event event : mEvents) {
            float lat = event.getVenue().getLatitude();
            float lng = event.getVenue().getLongitude();
            LatLng venueLatLng = new LatLng(lat, lng);

            //adds marker that represents Event venue to map
            createMapMarker(venueLatLng, event);
        }
    }

    private void getCachedEvents() {
        Gson gson = new Gson();

        //Gets user's location(LatLng serialized into string) from sharedPreferences
        SharedPreferences userLocationPreferences = getActivity().getSharedPreferences(EventsActivity.LOCATION_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String serializedLatLng = userLocationPreferences.getString("userCurrentLatLng", null);
        if (serializedLatLng != null) {
            //deserializes userLatLng string into LatLng object
            mUserLatLng = gson.fromJson(serializedLatLng, LatLng.class);
        } else {
            //if there was no latlng found for some reason
            Toast.makeText(getActivity(),getString(R.string.error_generic),Toast.LENGTH_SHORT).show();
        }

        //Gets Events data from sharedPreferences
        SharedPreferences eventsPreferences = getActivity().getSharedPreferences(EventsActivity.EVENTS_SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        mNumberOfPagesLoaded = eventsPreferences.getInt("numberOfPagesLoaded", 0);
        mTotalNumberOfPages = eventsPreferences.getInt("totalNumberOfPages", 0);
        String serializedEvents = eventsPreferences.getString("events", null);

        //deserializes events if there are any
        if (serializedEvents != null) {
            Type listOfEvents = new TypeToken<ArrayList<Event>>(){}.getType();
            mEvents = gson.fromJson(serializedEvents, listOfEvents);
        }
    }

    @Override
    public void onTaskAboutToStart() {
        //hides redo search button if visible
        if (mRedoSearchButton.getVisibility() == View.VISIBLE) {
            mRedoSearchButton.setVisibility(View.GONE);
        }

        //hides map fragment on screen
        getChildFragmentManager().beginTransaction()
                .hide(mMapFragment)
                .commit();

        //displays progress bar before getting events
        mEventsLoadingProgressbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTaskCompleted(PaginatedResult<Event> eventsNearby) {
        //if last call was successful then load events to screen
        if (Caller.getInstance().getLastResult().isSuccessful()) {
            ArrayList<Event> events= new ArrayList<Event>(eventsNearby.getPageResults());

            //sets variable that keeps track of how many pages of results are cached
            mNumberOfPagesLoaded = 1;

            //set variable that stores total number of pages
            mTotalNumberOfPages = eventsNearby.getTotalPages();

            //clears events list before adding events to it
            mEvents.clear();

            //add events to mEvents
            mEvents.addAll(events);

            //set events adapter with new events
            displayEventsInMap();
        } else {
            //if call to backend was not successful
            Toast.makeText(getActivity(),getString(R.string.error_generic),Toast.LENGTH_SHORT).show();
        }

        //shows map fragment on screen again
        getChildFragmentManager().beginTransaction()
                .show(mMapFragment)
                .commit();

        //hide loading progressbar in middle of screen
        mEventsLoadingProgressbar.setVisibility(View.GONE);
    }

    private void createMapMarker(LatLng latLng, Event event) {
        Marker eventMarker;
        //if there is already a marker for same spot, creates new marker in a very slightly different spot than original
        //that way user can see ALL markers for same location
        if (mMarkerPositions.contains(latLng)) {
            eventMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latLng.latitude * (Math.random() * (1.000001 - .999999) + .999999),
                            latLng.longitude * (Math.random() * (1.000001 - .999999) + .999999)))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.music_live))

            );
        } else {
            eventMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.music_live))
            );
        }
        //mMarkers keeps track of all Event markers that have been put down on map
        mMarkers.put(eventMarker.getId(), event);

        //mMarkerPositions keeps track of the location of all markers that have been put on map
        mMarkerPositions.add(latLng);
    }

    public class EventMarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private Marker mMarkerShowingInfoWindow;
        private Context mContext;

        public EventMarkerInfoWindowAdapter(Context context) {
            mContext = context;
        }

        @Override
        public View getInfoContents(Marker marker) {

            mMarkerShowingInfoWindow = marker;

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

            // Getting view from the layout file info_window_layout
            View infoWindow = inflater.inflate(R.layout.event_info_window, null);

            TextView eventTitleTextfield = (TextView) infoWindow.findViewById(R.id.event_info_window_event_title_textfield);
            TextView venueNameTextfield = (TextView) infoWindow.findViewById(R.id.event_info_window_venue_name_textfield);
            TextView eventDateTextfield = (TextView) infoWindow.findViewById(R.id.event_info_window_event_date_textfield);
            ImageView eventImage = (ImageView) infoWindow.findViewById(R.id.event_info_window_event_image);

            eventTitleTextfield.setText(mMarkers.get(marker.getId()).getTitle());
            //gets event date as Date object but only needs MMDDYYYY, not the timestamp
            eventDateTextfield.setText(mMarkers.get(marker.getId()).getStartDate().toLocaleString().substring(0, 12));
            venueNameTextfield.setText("@ " + mMarkers.get(marker.getId()).getVenue().getName());

            String eventImageUrl = mMarkers.get(marker.getId()).getImageURL(ImageSize.EXTRALARGE);

            //load event image into eventImage imageView if event has an image
            if (eventImageUrl.length() > 0) {
                Picasso.with(mContext)
                        .load(eventImageUrl)
                        .placeholder(R.drawable.placeholder)
                        .into(eventImage, onImageLoaded);
            }


            // Returning the view containing InfoWindow contents
            return infoWindow;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

    /**
    * This method is called after the image has been loaded. It checks if the currently displayed
    * info window is the same info window which has been saved. If it is, then refresh the window
    * to display the newly loaded image.
    */
    private Callback onImageLoaded = new Callback() {

            @Override
            public void onSuccess() {
                if (mMarkerShowingInfoWindow != null && mMarkerShowingInfoWindow.isInfoWindowShown()) {
                    mMarkerShowingInfoWindow.hideInfoWindow();
                    mMarkerShowingInfoWindow.showInfoWindow();
                }
            }

            @Override
            public void onError() {
            }
        };

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Gson gson = new Gson();
        //get Event for marker
        Event event = mMarkers.get(marker.getId());

        //serialize event using GSON
        String serializedEvent = gson.toJson(event, Event.class);

        //starts EventDetailsActivity
        Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
        intent.putExtra("EVENT", serializedEvent);
        startActivity(intent);
    }

    //listener for when user moves map camera
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        mCameraChangeCount ++;

        //first time cameraChanges is always for the initial map setup
        if (mCameraChangeCount > 1) {
            //displays redo search button when user moves map camera position
            mRedoSearchButton.setVisibility(View.VISIBLE);
        }

    }
}