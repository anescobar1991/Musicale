package com.anescobar.musicale.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.anescobar.musicale.R;
import com.anescobar.musicale.view.fragments.EventsListViewFragment;
import com.anescobar.musicale.view.fragments.EventsMapViewFragment;
import com.crashlytics.android.Crashlytics;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class EventsActivity extends BaseActivity {
    private static final String EVENTS_LIST_VIEW_FRAGMENT_TAG = "eventsListViewFragment";
    private static final String EVENTS_MAP_VIEW_FRAGMENT_TAG = "eventsMapViewFragment";

    @InjectView(R.id.musicale_toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //starts crashlytics
        Crashlytics.start(this);

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_events_view, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about_musicale:
                Intent intent = new Intent(this, AboutMusicaleActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_view_in_list:
                addFragmentToActivity(R.id.activity_events_container, new EventsListViewFragment(), EVENTS_LIST_VIEW_FRAGMENT_TAG);
                return true;
            case R.id.action_explore_in_map:
                addFragmentToActivity(R.id.activity_events_container, new EventsMapViewFragment(), EVENTS_MAP_VIEW_FRAGMENT_TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}