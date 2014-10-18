package com.anescobar.musicale.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.interfaces.EventFetcherListener;
import com.anescobar.musicale.utils.EventsFinder;
import com.anescobar.musicale.utils.NetworkUtil;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;

import java.util.ArrayList;

import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;

/**
 * Splash activity
 * @author Andres Escobar
 * @version 7/15/14
 */
public class SplashActivity extends LocationAwareActivity implements EventFetcherListener {

    private NetworkUtil mNetworkUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNetworkUtil = new NetworkUtil();

        setContentView(R.layout.activity_splash);

        //starts crashlytics
        Crashlytics.start(this);
    }

    /**
     * sends intent to start home activity
     */
    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * sends intent to start events activity
     */
    private void startEventsActivity() {
        Intent intent = new Intent(this, EventsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mNetworkUtil.isNetworkAvailable(this)) {
            new EventsFinder().getEvents(1, getCurrentLatLng(), this);
        } else {
            Toast.makeText(this, getString(R.string.error_no_network_connectivity), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, R.string.error_no_network_connectivity, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventFetcherTaskAboutToStart() {
    }

    @Override
    public void onEventFetcherTaskCompleted(PaginatedResult<Event> eventsNearby) {
        //if last call was successful then load events to screen
        if (Caller.getInstance().getLastResult().isSuccessful()) {
            ArrayList<Event> events= new ArrayList<Event>(eventsNearby.getPageResults());

            //store total number of pages
            mEventQueryDetails.totalNumberOfEventPages = eventsNearby.getTotalPages();

            //store number of pages loaded
            mEventQueryDetails.numberOfEventPagesLoaded = 1;

            //store events
            mEventQueryDetails.events.addAll(events);

            //start events activity
            startEventsActivity();
        } else {
            //if call to backend was not successful
            Toast.makeText(this,getString(R.string.error_generic),Toast.LENGTH_SHORT).show();
        }

    }
}
