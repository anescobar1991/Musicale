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

    private Event mEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        ButterKnife.inject(this);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Gson gson = new Gson();

        mEvent = gson.fromJson(getIntent().
                getExtras().
                getString("EVENT"), Event.class);

        addFragmentToActivity(R.id.event_info_header_container, EventInfoHeaderFragment.newInstance(mEvent), EVENT_INFO_HEADER_FRAGMENT);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the pager with an adapter
        mPager.setAdapter(new EventDetailsPagerAdapter(getSupportFragmentManager(), this, mEvent));

        // Bind the tabs to the ViewPager
        mTabs.setViewPager(mPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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