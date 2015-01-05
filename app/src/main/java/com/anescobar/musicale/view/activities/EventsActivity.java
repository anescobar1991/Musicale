package com.anescobar.musicale.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.utils.exceptions.LocationNotAvailableException;
import com.anescobar.musicale.view.fragments.EventsListViewFragment;
import com.anescobar.musicale.view.fragments.EventsMapViewFragment;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class EventsActivity extends LocationAwareActivity implements
        EventsMapViewFragment.EventsMapViewFragmentInteractionListener,
        EventsListViewFragment.EventsListViewFragmentInteractionListener {

    private static final String EVENTS_LIST_VIEW_FRAGMENT_TAG = "eventsListViewFragment";
    private static final String EVENTS_MAP_VIEW_FRAGMENT_TAG = "eventsMapViewFragment";

    @InjectView(R.id.event_map_tab) Button mMapViewTab;
    @InjectView(R.id.event_list_tab) Button mListViewTab;
    @InjectView(R.id.musicale_toolbar) Toolbar mToolbar;

    private boolean mMapViewDisplayed = false;
    private boolean mListViewDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        ButterKnife.inject(this);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle(R.string.title_events_activity);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.events, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about_musicale:
                //starts About Musicale activity
                Intent intent = new Intent(this, AboutMusicaleActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        //gets latLng and stores it once location client is connected
        try {
            mLatLng = getCurrentLatLng();
        } catch (LocationNotAvailableException e) {
            Crashlytics.logException(e);
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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void storeCurrentLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    @OnClick(R.id.event_list_tab)
    public void displayEventsListView() {
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

    @OnClick(R.id.event_map_tab)
    public void displayEventsMapView() {
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
}