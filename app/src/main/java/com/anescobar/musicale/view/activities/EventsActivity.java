package com.anescobar.musicale.view.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.models.SearchLocation;
import com.anescobar.musicale.view.fragments.EventsListViewFragment;
import com.anescobar.musicale.view.fragments.EventsMapViewFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.twitter.sdk.android.Twitter;
import io.fabric.sdk.android.Fabric;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.core.TwitterAuthConfig;

public class EventsActivity extends BaseActivity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "CCedkOaSl9mpDCANsDJPatKKm";
    private static final String TWITTER_SECRET = "Bx6w5Q2Nv8UXEBI2CcSAXjcLsWjKBbMmiOB4iiSHM4HHUW4Ik4";


    private static final String EVENTS_LIST_VIEW_FRAGMENT_TAG = "eventsListViewFragment";
    private static final String EVENTS_MAP_VIEW_FRAGMENT_TAG = "eventsMapViewFragment";

    @InjectView(R.id.musicale_toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_events);

        ButterKnife.inject(this);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);

        Fabric.with(this, new Crashlytics(), new Twitter(authConfig));

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle(R.string.title_events_activity);
        }

        addFragmentToActivity(R.id.activity_events_container, new EventsListViewFragment(), EVENTS_LIST_VIEW_FRAGMENT_TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_events_view, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about_musicale:
                Intent intent = new Intent(this, AboutMusicaleActivity.class);
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(this, R.anim.push_down_in, R.anim.abc_fade_out);

                Crashlytics.log("User entered About Musicale screen");

                startActivity(intent, activityOptions.toBundle());
                return true;
            case R.id.action_refresh_event_list:
                EventsListViewFragment eventsListViewFragment = (EventsListViewFragment) getFragmentByTag(EVENTS_LIST_VIEW_FRAGMENT_TAG);

                eventsListViewFragment.refreshEvents(SearchLocation.getInstance().mSearchLatLng);
                return true;
            case R.id.action_view_in_list:
                addFragmentToActivity(R.id.activity_events_container, new EventsListViewFragment(), EVENTS_LIST_VIEW_FRAGMENT_TAG);
                return true;
            case R.id.action_explore_in_map:
                addFragmentToActivity(R.id.activity_events_container, new EventsMapViewFragment(), EVENTS_MAP_VIEW_FRAGMENT_TAG);

                Crashlytics.log("User entered explore in map screen");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}