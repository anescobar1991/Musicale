package com.anescobar.musicale.view.fragments.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.adapters.interfaces.EventFetcherListener;
import com.anescobar.musicale.rest.models.services.EventsFinder;
import com.anescobar.musicale.app.adapters.utils.LocationNotAvailableException;
import com.anescobar.musicale.app.adapters.utils.NetworkNotAvailableException;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        //starts crashlytics
        Crashlytics.start(this);
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
        initializeApp();
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                initializeApp();
            }
        });
        builder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        // Set dialog's message
        builder.setMessage(R.string.error_no_network_connectivity);
        // Create the AlertDialog
        builder.show();
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
            initializeApp();
        }
    }

    public void initializeApp() {
        try {
            new EventsFinder().getEvents(1, getCurrentLatLng(), this, this);
        } catch (LocationNotAvailableException e) {
            e.printStackTrace();

            //create dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Add the buttons
            builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    initializeApp();
                }
            });
            builder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            // Set dialog's message
            builder.setMessage(R.string.error_location_services_disabled);
            // Create the AlertDialog
            builder.show();
        } catch (NetworkNotAvailableException e) {
            e.printStackTrace();

            //create dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Add the buttons
            builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    initializeApp();
                }
            });
            builder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            // Set dialog's message
            builder.setMessage(R.string.error_no_network_connectivity);
            // Create the AlertDialog
            builder.show();
        }
    }

}
