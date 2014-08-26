package com.anescobar.musicale.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.anescobar.musicale.fragments.AboutEventArtistFragment;
import com.anescobar.musicale.fragments.AboutEventBuzzFragment;
import com.anescobar.musicale.fragments.AboutEventVenueFragment;

import java.util.ArrayList;

/**
 * Created by andres on 8/25/14.
 * Pager adapter
 */
public class EventDetailsPagerAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> fragments = new ArrayList<Fragment>();
    ArrayList<String> titles = new ArrayList<String>();

    public EventDetailsPagerAdapter(FragmentManager fm) {
        super(fm);
        //add fragments and corresponding titles to lists
        fragments.add(new AboutEventVenueFragment());
        titles.add("About venue");
        fragments.add(new AboutEventArtistFragment());
        titles.add("About artist");
        fragments.add(new AboutEventBuzzFragment());
        titles.add("Word on the street");
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