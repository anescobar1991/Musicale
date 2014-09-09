package com.anescobar.musicale.activities;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.EventsMapViewFragment;
import com.google.android.gms.maps.model.LatLng;

public class EventsMapViewActivity extends BaseActivity implements
        EventsMapViewFragment.OnEventsMapViewFragmentInteractionListener{

    public static final String EVENTS_MAP_VIEW_FRAGMENT_TAG = "eventsMapViewFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_map_view);

        //adds events map view to activity
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.activity_events_map_view_container, new EventsMapViewFragment(), EVENTS_MAP_VIEW_FRAGMENT_TAG)
                .commit();
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
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
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

    @Override
    public void onConnectionResult(boolean result) {
    }

    @Override
    public LatLng getCurrentLatLng() {
        return mLocationProvider.getCurrentLatLng();
    }
}
