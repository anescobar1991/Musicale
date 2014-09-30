package com.anescobar.musicale.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.EventsListViewFragment;
import com.anescobar.musicale.fragments.EventsMapViewFragment;
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
                refreshEvents(getCurrentLatLng());
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
        mLatLng = getCurrentLatLng();

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

            //will add new events list view fragment if it hasnt already been added
            addFragmentToActivity(R.id.activity_events_container, EventsListViewFragment.newInstance(mLatLng), EVENTS_LIST_VIEW_FRAGMENT_TAG);

            //handles tabs' appearance to convey to user that list view is currently displayed view
            mListViewTab.setBackground(getResources().getDrawable(R.drawable.default_raised_button));
            mListViewTab.setTextColor(getResources().getColor(R.color.white));

            mMapViewTab.setBackground(getResources().getDrawable(R.drawable.unselected_tab_button));
            mMapViewTab.setTextColor(getResources().getColor(R.color.default_text_grey));
        }

    }

    private void displayEventsMapView() {
        //will add new events map view fragment if it hasnt already been added
        if (!mMapViewDisplayed) {
            mListViewDisplayed = false;
            mMapViewDisplayed = true;

            addFragmentToActivity(R.id.activity_events_container, EventsMapViewFragment.newInstance(mLatLng), EventsActivity.EVENTS_MAP_VIEW_FRAGMENT_TAG);

            //handles tabs' appearance to conveny to user that map view is currently displayed view
            mMapViewTab.setBackground(getResources().getDrawable(R.drawable.default_raised_button));
            mMapViewTab.setTextColor(getResources().getColor(R.color.white));

            mListViewTab.setBackground(getResources().getDrawable(R.drawable.unselected_tab_button));
            mListViewTab.setTextColor(getResources().getColor(R.color.default_text_grey));
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