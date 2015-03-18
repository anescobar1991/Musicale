package com.anescobar.musicale.view.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by andres on 9/5/14.
 * Abstract class to be superclass for all activities
 * Includes common methods and functionality
 */

public abstract class BaseActivity extends ActionBarActivity {
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getSupportFragmentManager();
    }

    protected void addFragmentToActivity(int container, Fragment fragment, String fragmentTag) {
        mFragmentManager.beginTransaction()
                .replace(container, fragment, fragmentTag)
                .commit();
    }

    protected Fragment getFragmentByTag(String tag) {
        return mFragmentManager.findFragmentByTag(tag);
    }
}