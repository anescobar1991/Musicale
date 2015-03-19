package com.anescobar.musicale.view.fragments;

import android.support.v4.app.Fragment;

import com.anescobar.musicale.app.MusicaleApp;
import com.anescobar.musicale.app.utils.AnalyticsUtil;

/**
 * Created by andres on 03/17/15.
 * Abstract class to be superclass for all fragments
 * Includes common methods and functionality
 */
public abstract class BaseFragment extends Fragment {
    protected AnalyticsUtil mAnalyticsUtil;

    @Override
    public void onStart() {
        super.onStart();

        mAnalyticsUtil = ((MusicaleApp) getActivity().getApplication()).analyticsUtil;
    }
}
