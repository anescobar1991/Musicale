package com.anescobar.musicale.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.EventsListViewFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;

public class EventsActivity extends BaseActivity {

    private static final String EVENTS_LIST_VIEW_FRAGMENT_TAG = "eventsListViewFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        addFragmentToActivity(R.id.activity_events_container, new EventsListViewFragment(),EVENTS_LIST_VIEW_FRAGMENT_TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.events, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh_events:
                refreshEvents();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //connects location client
        mLocationClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    private void refreshEvents() {
        EventsListViewFragment eventsListViewFragment = (EventsListViewFragment) getFragmentManager().findFragmentByTag(EVENTS_LIST_VIEW_FRAGMENT_TAG);

        //gets currentLatLng
        LatLng currentLocation = getDevicesCurrentLatLng();

        //stores new current location
        mEventQueryDetails.currentLatLng = currentLocation;

        //calls eventsListViewFragment's getEvents method, which gets events from backend and displays and stores them as needed
        eventsListViewFragment.getEventsFromServer(1,currentLocation);
    }

}