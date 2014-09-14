package com.anescobar.musicale.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.EventsListViewFragment;
import com.anescobar.musicale.fragments.EventsMapViewFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;

public class EventsActivity extends LocationAwareActivity implements
        EventsListViewFragment.EventListViewFragmentInteractionListener,
        EventsMapViewFragment.EventMapViewFragmentInteractionListener {

    public static final String EVENTS_LIST_VIEW_FRAGMENT_TAG = "eventsListViewFragment";
    public static final String EVENTS_MAP_VIEW_FRAGMENT_TAG = "eventsMapViewFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.events, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle actionbar item selection
        switch (item.getItemId()) {
            case R.id.action_refresh_events:
                refreshEvents(getCurrentLatLng());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshEvents(LatLng userLatLng) {
        EventsListViewFragment eventsListViewFragment = (EventsListViewFragment) getFragmentManager().findFragmentByTag(EVENTS_LIST_VIEW_FRAGMENT_TAG);

        //stores new current location
        eventsListViewFragment.setCurrentLatLng(userLatLng);

        //calls eventsListViewFragment's getEvents method, which gets events from backend and displays and stores them as needed
        eventsListViewFragment.getEventsFromServer(1,userLatLng);
    }

    @Override
    public void onConnected(Bundle bundle) {
        //add events fragment to activity once location client is connected
        addFragmentToActivity(R.id.activity_events_container, EventsListViewFragment.newInstance(getCurrentLatLng()), EVENTS_LIST_VIEW_FRAGMENT_TAG);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public void setMapViewTitleInActionBar() {
        getActionBar().setTitle(R.string.title_events_map_view);
    }

    public void setListViewTitleInActionBar() {
        getActionBar().setTitle(R.string.title_events_activity);
    }
}