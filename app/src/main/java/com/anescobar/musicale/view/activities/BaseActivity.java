package com.anescobar.musicale.view.fragments.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.anescobar.musicale.app.adapters.models.EventQueryDetails;

/**
 * Created by andres on 9/5/14.
 * Abstract class to be superclass for all activities
 * Includes common methods and functionality
 */
public abstract class BaseActivity extends ActionBarActivity {

    protected EventQueryDetails mEventQueryDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEventQueryDetails = EventQueryDetails.getInstance();
    }

    //adds fragment to activity
    protected void addFragmentToActivity(int container, Fragment fragment, String fragmentTag) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(container, fragment, fragmentTag)
                .commit();
    }

}