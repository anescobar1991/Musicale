package com.anescobar.musicale.view.fragments;

import android.support.v4.app.Fragment;

import com.anescobar.musicale.app.MusicaleApp;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public abstract class BaseFragment extends Fragment {

    protected void sendAnalyticsScreenHit(String screenName) {
        Tracker t = ((MusicaleApp) getActivity().getApplication()).getTracker(
                MusicaleApp.TrackerName.APP_TRACKER);

        t.setScreenName(screenName);
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

}
