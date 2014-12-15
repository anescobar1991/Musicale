package com.anescobar.musicale.view.fragments;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.view.activities.EventDetailsActivity;
import com.anescobar.musicale.app.interfaces.EventFetcherListener;
import com.anescobar.musicale.rest.services.EventsFinder;
import com.anescobar.musicale.app.utils.NetworkNotAvailableException;
import com.anescobar.musicale.app.models.EventQueryDetails;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.PaginatedResult;

public class EventsMapViewFragment extends Fragment implements EventFetcherListener,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnCameraChangeListener {

    private MapFragment mMapFragment;
    private EventsMapViewFragmentInteractionListener mListener;
    private GoogleMap mMap;

    @InjectView(R.id.redo_search_button) Button mRedoSearchButton;
    @InjectView(R.id.loading_overlay) RelativeLayout mLoadingOverlay;

    private EventQueryDetails mEventQueryDetails = EventQueryDetails.getInstance();
    private LatLng mLatLng;
    private int mCameraChangeCount = 0; //keeps track of how many times map camera change has occurred
    private HashMap<String, Event> mMarkers = new HashMap<>();
    private ArrayList<LatLng> mMarkerPositions = new ArrayList<>();

    public EventsMapViewFragment() {
        // Required empty public constructor
    }

    //always use this to create new instance of this fragment
    public static EventsMapViewFragment newInstance(LatLng currentLocation) {
        EventsMapViewFragment eventsMapViewFragment = new EventsMapViewFragment();

        final Gson gson = new Gson();
        String serializedCurrentLatLng = gson.toJson(currentLocation, LatLng.class);
        Bundle args = new Bundle();
        args.putString("currentLatLng", serializedCurrentLatLng);
        eventsMapViewFragment.setArguments(args);

        return eventsMapViewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //lets it know that this fragment has its own menu implementation
        setHasOptionsMenu(true);

        //creates MapFragment
        mMapFragment = new MapFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.findItem(R.id.action_refresh_events).setVisible(false);
        menu.findItem(R.id.action_search_events).setVisible(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_events_map_view, container, false);

        ButterKnife.inject(this, rootView);

        //displays map fragment on screen
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.events_map_container, mMapFragment)
                .commit();

        return rootView;
    }

    @OnClick(R.id.redo_search_button)
    public void searchNewLocation() {
        LatLng newLatLng = mMap.getCameraPosition().target;

        //stores new latLng
        mLatLng = newLatLng;

        mListener.storeCurrentLatLng(newLatLng);

        //calls getEvents method, which gets events from backend and displays and stores them as needed
        getEventsFromServer(1, newLatLng);
    }


    @Override
    public void onStart(){
        super.onStart();

        //deserializes latLng string in bundle and stores it
        final Gson gson = new Gson();
        String serializedLatLng = getArguments().getString("currentLatLng", null);

        mLatLng = gson.fromJson(serializedLatLng, LatLng.class);

        //sets up map, with its settings, and adds event markers
        setUpMapIfNeeded(mLatLng);

    }

    @Override
    public void onResume(){
        super.onResume();
        //sets up map, with its settings, and adds event markers
        setUpMapIfNeeded(mLatLng);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (EventsMapViewFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement EventsMapViewFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //gets events from backend
    public void getEventsFromServer(Integer pageNumber, LatLng userLocation) {

        try {
            new EventsFinder().getEvents(pageNumber, userLocation, this, getActivity());
        } catch (NetworkNotAvailableException e) {
            e.printStackTrace();

            Toast.makeText(getActivity(), getString(R.string.error_no_network_connectivity), Toast.LENGTH_SHORT).show();
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
        if (mEventQueryDetails.events.isEmpty()) {
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

        ArrayList<Event> events = mEventQueryDetails.events;
        //iterate through events list
        for (Event event : events) {
            float lat = event.getVenue().getLatitude();
            float lng = event.getVenue().getLongitude();
            LatLng venueLatLng = new LatLng(lat, lng);

            //adds marker that represents Event venue to map
            createMapMarker(venueLatLng, event);
        }
    }

    @Override
    public void onEventFetcherTaskAboutToStart() {
        //hides redo search button if visible
        if (mRedoSearchButton.getVisibility() == View.VISIBLE) {
            mRedoSearchButton.setVisibility(View.GONE);
        }

        //display loading overlay
        mLoadingOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onEventFetcherTaskCompleted(PaginatedResult<Event> eventsNearby) {
        //if last call was successful then load events to screen
        if (Caller.getInstance().getLastResult().isSuccessful()) {
            ArrayList<Event> events= new ArrayList<Event>(eventsNearby.getPageResults());

            //sets variable that keeps track of how many pages of results are cached
            mEventQueryDetails.numberOfEventPagesLoaded = 1;

            //set variable that stores total number of pages
            mEventQueryDetails.totalNumberOfEventPages = eventsNearby.getTotalPages();

            //clears events list before adding events to it
            mEventQueryDetails.events.clear();

            //add events to cache
            mEventQueryDetails.events.addAll(events);

            //set events adapter with new events
            displayEventsInMap();
        } else {
            //if call to backend was not successful
            Toast.makeText(getActivity(),getString(R.string.error_generic),Toast.LENGTH_SHORT).show();
        }

        //hide loading overlay
        mLoadingOverlay.setVisibility(View.GONE);
    }

    private void createMapMarker(LatLng latLng, Event event) {
        Marker eventMarker;
        //if there is already a marker for same spot, creates new marker in a very slightly different spot than original
        //that way user can see ALL markers for same location
        if (mMarkerPositions.contains(latLng)) {
            eventMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latLng.latitude * (Math.random() * (1.000001 - .999999) + .999999),
                            latLng.longitude * (Math.random() * (1.000001 - .999999) + .999999)))

            );
        } else {
            eventMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
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

            TextView eventTitleTextfield = (TextView) infoWindow.findViewById(R.id.event_name);
            TextView venueNameTextfield = (TextView) infoWindow.findViewById(R.id.event_venue_name);
            TextView eventDateTextfield = (TextView) infoWindow.findViewById(R.id.event_date);
            ImageView eventImage = (ImageView) infoWindow.findViewById(R.id.event_image);

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface EventsMapViewFragmentInteractionListener {
        public void storeCurrentLatLng(LatLng latLng);
    }

}