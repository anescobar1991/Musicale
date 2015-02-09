package com.anescobar.musicale.app.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.anescobar.musicale.R;
import com.anescobar.musicale.view.fragments.AboutArtistFragment;
import com.anescobar.musicale.view.fragments.EventSocialMediaFragment;
import com.anescobar.musicale.view.fragments.AboutEventVenueFragment;

import java.util.ArrayList;

import de.umass.lastfm.Event;

/**
 * Created by andres on 8/25/14.
 * Pager adapter for event details view pager
 * Not much to it, pretty self explanatory...
 */
public class EventDetailsPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<String> titles = new ArrayList<>();
    private Event mEvent;

    public EventDetailsPagerAdapter(FragmentManager fm, Context context, Event event) {
        super(fm);

        mEvent = event;
        //add titles to lists
        titles.add(context.getString(R.string.event_details_about_venue_tab));
        titles.add(context.getString(R.string.event_details_about_artist_tab));
        titles.add(context.getString(R.string.event_details_social_media_tab));
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = AboutEventVenueFragment.newInstance(mEvent.getVenue());
                break;
            case 1:
                fragment = AboutArtistFragment.newInstance(mEvent.getHeadliner());
                break;
            case 2:
                fragment = new EventSocialMediaFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public String getPageTitle(int position) {
        return titles.get(position);
    }
}