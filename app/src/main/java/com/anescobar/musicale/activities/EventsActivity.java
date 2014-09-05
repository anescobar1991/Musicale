package com.anescobar.musicale.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.EventsListViewFragment;
import com.anescobar.musicale.fragments.EventsMapViewFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.umass.lastfm.Event;

public class EventsActivity extends Activity implements ActionBar.OnNavigationListener,
        EventsListViewFragment.OnEventsListViewFragmentInteractionListener,
        EventsMapViewFragment.OnEventsMapViewFragmentInteractionListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String EVENTS_LIST_VIEW_FRAGMENT_TAG = "eventsListViewFragment";
    private static final String EVENTS_MAP_VIEW_FRAGMENT_TAG = "eventsMapViewFragment";
    public static final String LOCATION_SHARED_PREFS_NAME = "LocationPrefs";
    public static final String EVENTS_SHARED_PREFS_NAME = "EventsPrefs";

    private LocationClient mLocationClient;
    private LatLng mCachedLatLng;
    private MenuItem mEventsRefreshIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Gson gson = new Gson();

        //creates new LocationClient instance
        mLocationClient = new LocationClient(this, this, this);

        //Gets user's location(LatLng serialized into string) from sharedPreferences
        SharedPreferences userLocationPreferences = getSharedPreferences(LOCATION_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String serializedLatLng = userLocationPreferences.getString("userCurrentLatLng", null);

        if (serializedLatLng != null) {
            //deserializes userLatLng string into LatLng object
            mCachedLatLng = gson.fromJson(serializedLatLng, LatLng.class);
        }

        displayEventsViewSpinner();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //connects location client
        mLocationClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //no need to get location unless requested by user if there is already location cached
        if (mCachedLatLng == null) {
            //gets current location
            LatLng currentLocation = getDevicesCurrentLatLng();

            //caches currentlocation in sharedPreferences
            cacheUserLatLng(currentLocation);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.events, menu);

        mEventsRefreshIcon = menu.findItem(R.id.action_refresh_events);
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

    /**
     * displays eventsViewSpinner which helps user select which events view they would like to see
     * Should only be displayed when either one of events view screens are visible
     */
    private void displayEventsViewSpinner() {
        ActionBar actionBar = getActionBar();
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.events_view_options, R.layout.actionbar_spinner_item);
        adapter.setDropDownViewResource(R.layout.actionbar_spinner_dropdown_item);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(adapter, this);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long l) {
        switch (itemPosition) {
            case 0:
                // refresh button should be visible
                mEventsRefreshIcon.setVisible(true);

                // adds events list view fragment to activity
                addFragmentToActivity(R.id.activity_events_container, new EventsListViewFragment(),EVENTS_LIST_VIEW_FRAGMENT_TAG);
                break;
            case 1:
                mEventsRefreshIcon.setVisible(false);
                //adds events map view fragment to activity
                addFragmentToActivity(R.id.activity_events_container, new EventsMapViewFragment(), EVENTS_MAP_VIEW_FRAGMENT_TAG);
                break;
        }
        return false;
    }

    //adds fragment to activity
    private void addFragmentToActivity(int container, Fragment fragment, String fragmentTag) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(container, fragment, fragmentTag)
                .commit();
    }


    //caches events to sharedPreferences
    @Override
    public void cacheEvents(int numberOfPagesLoaded, int totalNumberOfPages, ArrayList<Event> events) {
        // stores events arrayList
        if (!events.isEmpty()) {
            Gson gson = new Gson();

            //writes into events shared preferences
            SharedPreferences eventsPreferences = getSharedPreferences(EVENTS_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = eventsPreferences.edit();

            //serialize events ArrayList
            Type listOfEvents = new TypeToken<ArrayList<Event>>() {}.getType();
            String serializedEvents = gson.toJson(events, listOfEvents);

            //puts serialized Events list in bundle for retrieval upon fragment creation
            editor.putString("events", serializedEvents);

            //stores numberOfPagesLoaded so next user session knows what is already cached
            editor.putInt("numberOfPagesLoaded", numberOfPagesLoaded);

            //stores totalNumberOfPages so next user session knows how many total number of pages exist
            editor.putInt("totalNumberOfPages", totalNumberOfPages);

            // commit the new additions!
            editor.apply();
        }
    }
    // stores user's current location in sharedPreferences
    // that way it persists throughout app even when app is not in memory
    @Override
    public void cacheUserLatLng(LatLng userLocation) {
        Gson gson = new Gson();
        String serializedCurrentUserLocation = gson.toJson(userLocation);

        //writes into Location shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(LOCATION_SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

        //stores userCurrentLatLng
        sharedPreferencesEditor.putString("userCurrentLatLng", serializedCurrentUserLocation);

        //commits the new additions!
        sharedPreferencesEditor.apply();
    }

    private void refreshEvents() {
        EventsListViewFragment eventsListViewFragment = (EventsListViewFragment) getFragmentManager().findFragmentByTag(EVENTS_LIST_VIEW_FRAGMENT_TAG);

        //gets currentLocation
        LatLng currentLocation = getDevicesCurrentLatLng();

        //caches currentlocation in sharedPreferences
        cacheUserLatLng(currentLocation);

        eventsListViewFragment.mUserLatLng = currentLocation;

        //calls eventsListViewFragment's getEvents method, which gets events from backend and displays and stores them as needed
        eventsListViewFragment.getEventsFromServer(1,currentLocation);
    }

    //returns LatLng object for devices current location
    private LatLng getDevicesCurrentLatLng() {
        //TODO refactor this so that it always sends me a location even if locatoin services if off or doesnt exist
        LatLng currentLocation = null;

        if (mLocationClient != null) {
            currentLocation = new LatLng(mLocationClient.getLastLocation().getLatitude(),
                    mLocationClient.getLastLocation().getLongitude());
        } else {
            Toast.makeText(this,R.string.error_no_network_connectivity, Toast.LENGTH_SHORT).show();
        }
        return currentLocation;

    }

}