package com.anescobar.musicale.view.fragments;

import android.app.ActivityOptions;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.services.exceptions.LocationNotAvailableException;
import com.anescobar.musicale.view.activities.EventDetailsActivity;
import com.anescobar.musicale.app.services.interfaces.EventFetcherListener;
import com.anescobar.musicale.app.services.EventsFinder;
import com.anescobar.musicale.app.services.exceptions.NetworkNotAvailableException;
import com.anescobar.musicale.app.models.EventQueryResults;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
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
import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.PaginatedResult;

public class EventsMapViewFragment extends LocationAwareFragment implements GoogleMap.OnInfoWindowClickListener,
        EventFetcherListener {

    private MapFragment mMapFragment;
    private GoogleMap mMap;

    @InjectView(R.id.loading_overlay) RelativeLayout mLoadingOverlay;

    private EventQueryResults mEventQueryResults = EventQueryResults.getInstance();
    private HashMap<String, Event> mMarkers = new HashMap<>();
    private ArrayList<LatLng> mMarkerPositions = new ArrayList<>();

    public EventsMapViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mMapFragment = new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_events_map_view, container, false);

        ButterKnife.inject(this, rootView);

        setHasOptionsMenu(true);

        //displays map fragment on screen
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.events_map_container, mMapFragment)
                .commit();

        return rootView;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mSearchLocation.searchLatLng == null) {
            try {
                setUpMapIfNeeded(getCurrentLatLng());
            } catch (LocationNotAvailableException e) {
                Toast.makeText(getActivity(),R.string.error_location_services_disabled, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        if (mSearchLocation.searchLatLng != null) {
            setUpMapIfNeeded(mSearchLocation.searchLatLng);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem exploreInMapButton = menu.findItem(R.id.action_explore_in_map);
        MenuItem viewInListButton = menu.findItem(R.id.action_view_in_list);
        MenuItem refreshEventsButton = menu.findItem(R.id.action_refresh_event_list);

        exploreInMapButton.setVisible(false);
        viewInListButton.setVisible(true);
        refreshEventsButton.setVisible(false);

        super.onPrepareOptionsMenu(menu);
    }

    public void getEventsFromServer(Integer pageNumber, LatLng searchAreaLatLng) {
        try {
            new EventsFinder().getEvents(pageNumber, searchAreaLatLng, this, getActivity());
        } catch (NetworkNotAvailableException e) {
            Toast.makeText(getActivity(), getString(R.string.error_no_network_connectivity), Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpMapIfNeeded(LatLng searchAreaLatLng) {
        if (mMap == null) {
            mMap = mMapFragment.getMap();

            mMap.setInfoWindowAdapter(new EventMarkerInfoWindowAdapter(getActivity()));
        }
        setUpMap(searchAreaLatLng);
    }

    /**
     * This is where map is setup with home and with current or searched location as center
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(LatLng searchAreaLatLng) {
        UiSettings mapSettings = mMap.getUiSettings();
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchAreaLatLng, 10));
        mMap.setMyLocationEnabled(true);
        mapSettings.setZoomControlsEnabled(false);
        mapSettings.setMyLocationButtonEnabled(false);
        mapSettings.setTiltGesturesEnabled(false);
        mapSettings.setRotateGesturesEnabled(false);

        if (mEventQueryResults.events.isEmpty()) {
            getEventsFromServer(1, searchAreaLatLng);
        } else {
            displayEventsInMap();
        }

        mMap.setOnInfoWindowClickListener(this);
    }

    private void displayEventsInMap() {
        mMap.clear();

        ArrayList<Event> events = mEventQueryResults.events;
        for (Event event : events) {
            float lat = event.getVenue().getLatitude();
            float lng = event.getVenue().getLongitude();
            LatLng venueLatLng = new LatLng(lat, lng);

            createMapMarker(venueLatLng, event);
        }
    }

    @Override
    public void onEventFetcherTaskAboutToStart() {
        mLoadingOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onEventFetcherTaskCompleted(PaginatedResult<Event> eventsNearby) {
        if (Caller.getInstance().getLastResult().isSuccessful()) {
            ArrayList<Event> events= new ArrayList<>(eventsNearby.getPageResults());

            mEventQueryResults.numberOfEventPagesLoaded = 1;
            mEventQueryResults.totalNumberOfEventPages = eventsNearby.getTotalPages();
            mEventQueryResults.events.clear();
            mEventQueryResults.events.addAll(events);

            displayEventsInMap();
        } else {
            Toast.makeText(getActivity(),getString(R.string.error_generic),Toast.LENGTH_SHORT).show();
        }

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
            if (eventImageUrl.length() > 0) {
                Picasso.with(mContext)
                        .load(eventImageUrl)
                        .placeholder(R.drawable.placeholder)
                        .into(eventImage, onImageLoaded);
            }


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
            public void onError() {}
        };
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Gson gson = new Gson();
        Event event = mMarkers.get(marker.getId());

        String serializedEvent = gson.toJson(event, Event.class);

        Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getActivity().getApplicationContext(), R.anim.slide_in_right, R.anim.slide_out_left);

        intent.putExtra("EVENT", serializedEvent);
        startActivity(intent, activityOptions.toBundle());
    }
}