package com.anescobar.musicale.activities;

import android.app.Activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.EventsListViewFragment;
import com.anescobar.musicale.fragments.EventsMapViewFragment;
import com.anescobar.musicale.fragments.NavigationDrawerFragment;
import com.anescobar.musicale.fragments.SocializeViewFragment;
import com.anescobar.musicale.fragments.TrendsViewFragment;
import com.anescobar.musicale.interfaces.OnEventsFetcherTaskCompleted;
import com.anescobar.musicale.utilsHelpers.EventsFinder;
import com.anescobar.musicale.utilsHelpers.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Session;

public class HomeActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, EventsMapViewFragment.OnEventsMapViewFragmentInteractionListener,
        TrendsViewFragment.OnTrendsViewFragmentInteractionListener, EventsListViewFragment.OnEventsListViewFragmentInteractionListener,
        SocializeViewFragment.OnSocializeViewFragmentInteractionListener, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, ActionBar.OnNavigationListener, OnEventsFetcherTaskCompleted {

    private NavigationDrawerFragment mNavigationDrawerFragment; //Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private CharSequence mTitle; //Used to store the last screen title. For use in {@link #restoreActionBar()}.
    public static final String LOCATION_SHARED_PREFS_NAME = "LocationPrefs";
    public static final String EVENTS_SHARED_PREFS_NAME = "EventsPrefs";
    private LocationClient mLocationClient;
    private boolean mIsEventsViewDisplayed;
    private boolean isMapViewDisplayed;
    private MenuItem mEventSearchIcon;
    private MenuItem mEventsRefreshIcon;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mLocationClient = new LocationClient(this, this, this);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onConnected(Bundle bundle) {
        //gets current location
        LatLng currentLocation = new LatLng(mLocationClient.getLastLocation().getLatitude(),
                mLocationClient.getLastLocation().getLongitude());

        //caches currentlocation in sharedPreferences
        cacheUserLatLng(currentLocation);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                isMapViewDisplayed = false;
                mIsEventsViewDisplayed = false;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, TrendsViewFragment.newInstance("example"))
                        .commit();
                break;
            case 1:
                //set flags which will tell menu to display events spinner
                //spinner will be selected and thus load events view screen on its own
                mIsEventsViewDisplayed = true;
                //displays actionbar icons that should only be visible if in events view
                mEventSearchIcon.setVisible(true);
                mEventsRefreshIcon.setVisible(true);
                break;
            case 2:
                isMapViewDisplayed = false;
                mIsEventsViewDisplayed = false;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SocializeViewFragment.newInstance("example"))
                        .commit();
                break;
            case 3:
                //instantiates new SessionManager
                SessionManager sessionManager = new SessionManager();

                //discards session and logs out
                sessionManager.discardSession(this);

                //user is taken to login screen after logout
                Intent loginActivityIntent = new Intent(this, LoginActivity.class);
                startActivity(loginActivityIntent);
                finish();
                break;
        }
    }

    public void restoreActionBar() {
        if (mIsEventsViewDisplayed && !mNavigationDrawerFragment.isDrawerOpen()) {
            displayEventsViewSpinner();
        } else {

            //restores actionBar to its original state
            mEventSearchIcon.setVisible(false);
            mEventsRefreshIcon.setVisible(false);
            ActionBar bar = getActionBar();
            bar.setTitle(mTitle);
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        mEventSearchIcon = menu.findItem(R.id.action_search_events);
        mEventsRefreshIcon = menu.findItem(R.id.action_refresh_events);
        restoreActionBar();
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * displays eventsViewSpinner
     * Should only be displayed when either one of events view screens are visible
     */
    private void displayEventsViewSpinner() {
        ActionBar actionBar = getActionBar();
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.events_view_options, R.layout.actionbar_spinner_item);
        adapter.setDropDownViewResource(R.layout.actionbar_spinner_dropdown_item);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        actionBar.setListNavigationCallbacks(adapter, this);
        if (isMapViewDisplayed) {
            actionBar.setSelectedNavigationItem(1);
        }
    }

    @Override
    public void onAttachDisplayTitle(int sectionIndex) {
        switch (sectionIndex) {
            case 0:
                mTitle = getString(R.string.title_trends_section);
                break;
            case 1:
                mTitle = getString(R.string.title_events_section);
                break;
            case 2:
                mTitle = getString(R.string.title_socialize_section);
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long l) {
        FragmentManager fragmentManager = getFragmentManager();
        switch (itemPosition) {
            case 0:
                isMapViewDisplayed = false;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new EventsListViewFragment())
                        .commit();
                break;
            case 1:
                isMapViewDisplayed = true;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new EventsMapViewFragment())
                        .commit();
                break;
        }
        return false;
    }

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
     * displays toast on bottom of screen
     * @param message String to be displayed on toast
     * @param toastLength enum to indicate how long toast should remain on screen, either Toast.LENGTH_SHORT, or Toast.LENGTH_LONG
     */
    public void displayToastMessage(String message, int toastLength) {
        Toast toast = Toast.makeText(this, message, toastLength);
        toast.show();
    }

    //    //caches events to sharedPreferences
    public void cacheEvents(int numberOfPagesLoaded, int totalNumberOfPages,ArrayList<Event> events) {
        // stores events arrayList
        if (!events.isEmpty()) {
            Gson gson = new Gson();

            //writes into events shared preferences
            SharedPreferences eventsPreferences = getSharedPreferences(EVENTS_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = eventsPreferences.edit();

            //serialize events ArrayList
            Type listOfEvents = new TypeToken<ArrayList<Event>>() {
            }.getType();
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
    private void cacheUserLatLng(LatLng userLocation) {
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
        //gets currentLocation
        LatLng currentLocation = new LatLng(mLocationClient.getLastLocation().getLatitude(),
                mLocationClient.getLastLocation().getLongitude());

        //caches currentlocation in sharedPreferences
        cacheUserLatLng(currentLocation);

    }

    @Override
    public void onTaskAboutToStart() {

    }

    //this is called on onPostExecute of eventsFetcher asyncTask
    @Override
    public void onTaskCompleted() {
        //creates new fragmentManager
        FragmentManager fragmentManager = getFragmentManager();
        //will refresh
        if (isMapViewDisplayed) {
            //TODO do whatever is necessary for refreshing map
        } else {
            //instantiates new instance of EventsMapViewFragment
            //will load with refreshed events
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new EventsMapViewFragment())
                    .commit();

        }
    }

//    private class EventsFetcherTask extends AsyncTask<Integer, Void, ArrayList<Event>> {
//        private Session session;
//        private LatLng userLocation;
//
//        public EventsFetcherTask(Session session, LatLng userLocation) {
//            this.session = session;
//            this.userLocation = userLocation;
//        }

//        @Override
//        protected ArrayList<Event> doInBackground(Integer... pageNumbers) {
//            ArrayList<Event> events = new ArrayList<Event>();
//
//            //send server request to get events nearby
//            PaginatedResult<Event> rawEventsNearby = new EventsFinder(session)
//                    .getEvents(userLocation.latitude, userLocation.longitude, "30", pageNumbers[0], 20, null);
//
//            //add eventsNearby to arrayList
//            events.addAll(rawEventsNearby.getPageResults());
//
//            mTotalNumberOfPages = rawEventsNearby.getTotalPages();
//
//            return events;
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<Event> events) {
//            //adds new events to mEvents
//            mEvents.addAll(events);
//            setEventsAdapter(events);
//            mEventsLoading.setVisibility(View.GONE);
//        }
//    }

}
