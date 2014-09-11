package com.anescobar.musicale.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.EventsMapViewFragment;
import com.google.android.gms.common.ConnectionResult;

public class EventsMapViewActivity extends LocationAwareActivity {

    public static final String EVENTS_MAP_VIEW_FRAGMENT_TAG = "eventsMapViewFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_map_view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.events_map_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        //add map fragment to activity only when locationClient is connected
        addFragmentToActivity(R.id.activity_events_map_view_container, EventsMapViewFragment.newInstance(getCurrentLatLng()), EVENTS_MAP_VIEW_FRAGMENT_TAG);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
