package com.anescobar.musicale.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.EventsListViewFragment;
import com.google.android.gms.maps.model.LatLng;

public class EventsListViewActivity extends BaseActivity implements
        EventsListViewFragment.OnEventsListViewFragmentInteractionListener {

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
                refreshEvents(getCurrentLatLng());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client
        mLocationProvider.connectClient();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationProvider.disconnectClient();
        super.onStop();
    }

    private void refreshEvents(LatLng userLatLng) {
        EventsListViewFragment eventsListViewFragment = (EventsListViewFragment) getFragmentManager().findFragmentByTag(EVENTS_LIST_VIEW_FRAGMENT_TAG);

        //stores new current location
        mEventQueryDetails.currentLatLng = userLatLng;

        //calls eventsListViewFragment's getEvents method, which gets events from backend and displays and stores them as needed
        eventsListViewFragment.getEventsFromServer(1,userLatLng);
    }

    @Override
    public void onConnectionResult(boolean success) {
//        if (success) {
//            refreshEvents(mLocationProvider.getCurrentLatLng());
//        } else {
//            Toast.makeText(this, R.string.error_no_network_connectivity, Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public LatLng getCurrentLatLng() {
        return mLocationProvider.getCurrentLatLng();
    }
}