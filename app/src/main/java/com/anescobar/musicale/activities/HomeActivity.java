package com.anescobar.musicale.activities;

import android.app.Activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.EventsListViewFragment;
import com.anescobar.musicale.fragments.EventsMapViewFragment;
import com.anescobar.musicale.fragments.NavigationDrawerFragment;
import com.anescobar.musicale.fragments.SocializeViewFragment;
import com.anescobar.musicale.fragments.TrendsViewFragment;
import com.anescobar.musicale.utilsHelpers.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

public class HomeActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, EventsMapViewFragment.OnEventsMapViewFragmentInteractionListener,
        TrendsViewFragment.OnTrendsViewFragmentInteractionListener, EventsListViewFragment.OnEventsListViewFragmentInteractionListener,
        SocializeViewFragment.OnSocializeViewFragmentInteractionListener, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, ActionBar.OnNavigationListener{

    private NavigationDrawerFragment mNavigationDrawerFragment; //Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private CharSequence mTitle; //Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private SessionManager mSessionManager = new SessionManager();
    private String mSessionString; //Needs to be be deserialized using GSON into a last FM Session object
    private String mUserLocationString; //Needs to be be deserialized using GSON into a LatLng object
    private LocationClient mLocationClient;
    private boolean mIsEventsViewDisplayed;
    private MenuItem mEventSearchIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //gets Strings passed from splash screen activity
        //sets variables with these strings for later use
        Intent intent = getIntent();
        mSessionString = intent.getStringExtra("com.anescobar.musicale.activities.HomeActivity.session");
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
        Gson gson = new Gson();
        mUserLocationString = gson.toJson(new LatLng(
                mLocationClient.getLastLocation().getLatitude(),mLocationClient.getLastLocation().getLongitude()
        ));
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
                fragmentManager.beginTransaction()
                        .replace(R.id.container, TrendsViewFragment.newInstance("example"))
                        .commit();
                break;
            case 1:
                mEventSearchIcon.setVisible(true);
                displayEventsViewSpinner(); //spinner will by default take care of instantiating fragment
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SocializeViewFragment.newInstance("example"))
                        .commit();
                break;
            case 3:
                //discards session and logs out
                mSessionManager.discardSession(this);
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
            mEventSearchIcon.setVisible(false);
//            restores actionBar to its original state
            ActionBar bar = getActionBar();
            bar.setTitle(mTitle);
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        mEventSearchIcon = menu.findItem(R.id.action_search);
        restoreActionBar(); //sets title to that of appropriate fragment
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * displays eventsViewSpinner
     * Should only be displayed when either one of home view screens are visible
     */
    private void displayEventsViewSpinner() {
        ActionBar bar = getActionBar();
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.events_view_options, R.layout.actionbar_spinner_item);

        bar.setDisplayShowTitleEnabled(false);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setListNavigationCallbacks(adapter, this);

    }

    /**
     * displays toast on bottom of screen
     * @param message String to be displayed on toast
     * @param toastLength enum to indicate how long toast should remain on screen, either Toast.LENGTH_SHORT, or Toast.LENGTH_LONG
     */
    private void displayToastMessage(String message, int toastLength) {
        Toast toast = Toast.makeText(this, message, toastLength);
        toast.show();
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
    public void onEventsViewAttached(boolean isAttached) {
        mIsEventsViewDisplayed = isAttached;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long l) {
        FragmentManager fragmentManager = getFragmentManager();
        switch (itemPosition) {
            case 0:
                System.out.println("list view selected");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, EventsListViewFragment.newInstance(mSessionString, mUserLocationString))
                        .commit();
                break;
            case 1:
                System.out.println("map view selected");
                fragmentManager.beginTransaction()
                        .replace(R.id.container, EventsMapViewFragment.newInstance(mSessionString, mUserLocationString))
                        .commit();
                break;
        }
        return false;
    }
}
