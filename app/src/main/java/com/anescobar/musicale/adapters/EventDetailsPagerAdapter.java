package com.anescobar.musicale.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.AboutEventArtistFragment;
import com.anescobar.musicale.fragments.AboutEventBuzzFragment;
import com.anescobar.musicale.fragments.AboutEventVenueFragment;

import java.util.ArrayList;

import de.umass.lastfm.Event;

/**
 * Created by andres on 8/25/14.
 * Pager adapter for event details view pager
 * Not much to it, pretty self explanatory...
 */
public class EventDetailsPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    private ArrayList<String> titles = new ArrayList<String>();

    public EventDetailsPagerAdapter(FragmentManager fm, Context context, Event event) {
        super(fm);
        //add fragments and corresponding titles to lists
        fragments.add(AboutEventVenueFragment.newInstance(event.getVenue()));
        titles.add(context.getString(R.string.event_details_about_venue_tab));
        fragments.add(AboutEventArtistFragment.newInstance(event.getHeadliner()));
        titles.add(context.getString(R.string.event_details_about_artist_tab));
        fragments.add(new AboutEventBuzzFragment());
        titles.add(context.getString(R.string.event_details_social_media_tab));
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public String getPageTitle(int position) {
        return titles.get(position);
    }
}