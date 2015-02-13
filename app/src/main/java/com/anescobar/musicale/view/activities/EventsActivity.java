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

import com.crashlytics.android.Crashlytics;

public class EventsActivity extends BaseActivity {


    private static final String EVENTS_LIST_VIEW_FRAGMENT_TAG = "eventsListViewFragment";
    private static final String EVENTS_MAP_VIEW_FRAGMENT_TAG = "eventsMapViewFragment";

    @InjectView(R.id.musicale_toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_events);

        ButterKnife.inject(this);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle(R.string.title_events_activity);
        }

        addFragmentToActivity(R.id.activity_events_container, new EventsListViewFragment(), EVENTS_LIST_VIEW_FRAGMENT_TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

                eventsListViewFragment.refreshEvents(SearchLocation.getInstance().searchLatLng);
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