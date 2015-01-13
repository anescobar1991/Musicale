package com.anescobar.musicale.view.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.adapters.EventDetailsPagerAdapter;
import com.anescobar.musicale.app.models.ArtistDetails;
import com.anescobar.musicale.app.models.VenueDetails;
import com.anescobar.musicale.view.fragments.AboutArtistFragment;
import com.anescobar.musicale.view.fragments.AboutEventVenueFragment;
import com.anescobar.musicale.view.fragments.EventInfoHeaderFragment;
import com.astuetz.PagerSlidingTabStrip;
import com.google.gson.Gson;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.umass.lastfm.Event;

public class EventDetailsActivity extends BaseActivity
        implements AboutEventVenueFragment.CachedVenueDetailsGetterSetter, AboutArtistFragment.CachedArtistDetailsGetterSetter {

    private static final String EVENT_INFO_HEADER_FRAGMENT = "eventInfoHeaderFragment";
    private VenueDetails mVenueDetails = new VenueDetails();
    private ArtistDetails mHeadlinerDetails = new ArtistDetails();

    @InjectView(R.id.musicale_toolbar) Toolbar mToolbar;
    @InjectView(R.id.event_details_view_pager) ViewPager mPager;
    @InjectView(R.id.event_details_tabs) PagerSlidingTabStrip mTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        ButterKnife.inject(this);

        //gets extras that were passed into activity
        Bundle extras = getIntent().getExtras();

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Gson gson = new Gson();

        setUpView(gson.fromJson(extras.getString("EVENT"), Event.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //if home/up button pressed it will go back to previous fragment
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    //adds event info header fragment and sets view pager with adapter
    private void setUpView(Event event) {
        //add event info header fragment to activity
        addFragmentToActivity(R.id.event_info_header_container, EventInfoHeaderFragment.newInstance(event), EVENT_INFO_HEADER_FRAGMENT);

        //Set the pager with an adapter
        mPager.setAdapter(new EventDetailsPagerAdapter(getSupportFragmentManager(), this, event));
        mPager.setOffscreenPageLimit(1);

        // Bind the tabs to the ViewPager
        mTabs.setViewPager(mPager);
    }

    @Override
    public VenueDetails getVenueDetails() {
        return mVenueDetails;
    }

    @Override
    public void setVenueDetails(VenueDetails venueDetails) {
        mVenueDetails = venueDetails;
    }

    @Override
    public ArtistDetails getArtistDetails() {
        return mHeadlinerDetails;
    }

    @Override
    public void setArtistDetails(ArtistDetails artistDetails) {
        mHeadlinerDetails = artistDetails;
    }
}