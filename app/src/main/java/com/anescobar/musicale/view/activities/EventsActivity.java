package com.anescobar.musicale.view.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.utils.LocationNotAvailableException;
import com.anescobar.musicale.view.fragments.EventsListViewFragment;
import com.anescobar.musicale.view.fragments.EventsMapViewFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;

public class EventsActivity extends LocationAwareActivity implements
        EventsMapViewFragment.EventsMapViewFragmentInteractionListener {

    private static final String EVENTS_LIST_VIEW_FRAGMENT_TAG = "eventsListViewFragment";
    private static final String EVENTS_MAP_VIEW_FRAGMENT_TAG = "eventsMapViewFragment";
    private Button mListViewTab;
    private Button mMapViewTab;
    private boolean mMapViewDisplayed = false;
    private boolean mListViewDisplayed = false;
    private LatLng mLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Toolbar toolbar = (Toolbar) findViewById(R.id.musicale_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mListViewTab = (Button) findViewById(R.id.activity_event_list_tab);
        mMapViewTab = (Button) findViewById(R.id.activity_event_map_tab);

        //sets on clickListener for explore in map button
        mMapViewTab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            //displays events map view fragment
            displayEventsMapView();
            }
        });

        //sets on clickListener for view in list button
        mListViewTab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //displays events list view fragment
                displayEventsListView();
            }
        });
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
                try {
                    refreshEvents(getCurrentLatLng());
                } catch (LocationNotAvailableException e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.error_location_services_disabled), Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshEvents(LatLng userLatLng) {
        EventsListViewFragment eventsListViewFragment = (EventsListViewFragment) getFragmentManager().findFragmentByTag(EVENTS_LIST_VIEW_FRAGMENT_TAG);

        //stores new current location
        mLatLng = userLatLng;

        //calls eventsListViewFragment's getEvents method, which gets events from backend and displays and stores them as needed
        eventsListViewFragment.getEventsFromServer(1,userLatLng);
    }

    @Override
    public void onConnected(Bundle bundle) {
        //gets latLng and stores it once location client is connected
        try {
            mLatLng = getCurrentLatLng();
        } catch (LocationNotAvailableException e) {
            e.printStackTrace();
        }

        //add events fragment to activity once location client is connected
        displayEventsListView();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDisconnected() {

    }

    private void displayEventsListView() {
        if (!mListViewDisplayed) {
            mMapViewDisplayed = false;
            mListViewDisplayed = true;

            //handles tabs' appearance to convey to user that list view is currently displayed view
            mListViewTab.setBackground(getResources().getDrawable(R.drawable.default_raised_button));
            mListViewTab.setTextColor(getResources().getColor(R.color.white));

            mMapViewTab.setBackground(getResources().getDrawable(R.drawable.unselected_tab_button));
            mMapViewTab.setTextColor(getResources().getColor(R.color.default_text_grey));

            //will add new events list view fragment if it hasnt already been added
            addFragmentToActivity(R.id.activity_events_container, EventsListViewFragment.newInstance(mLatLng), EVENTS_LIST_VIEW_FRAGMENT_TAG);
        }

    }

    private void displayEventsMapView() {
        //will add new events map view fragment if it hasnt already been added
        if (!mMapViewDisplayed) {
            mListViewDisplayed = false;
            mMapViewDisplayed = true;

            //handles tabs' appearance to conveny to user that map view is currently displayed view
            mMapViewTab.setBackground(getResources().getDrawable(R.drawable.default_raised_button));
            mMapViewTab.setTextColor(getResources().getColor(R.color.white));

            mListViewTab.setBackground(getResources().getDrawable(R.drawable.unselected_tab_button));
            mListViewTab.setTextColor(getResources().getColor(R.color.default_text_grey));

            addFragmentToActivity(R.id.activity_events_container, EventsMapViewFragment.newInstance(mLatLng), EventsActivity.EVENTS_MAP_VIEW_FRAGMENT_TAG);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void storeCurrentLatLng(LatLng latLng) {
        mLatLng = latLng;
    }
}