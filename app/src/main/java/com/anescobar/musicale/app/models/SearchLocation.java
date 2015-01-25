package com.anescobar.musicale.app.models;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;

/**
 * Singleton class to maintain user's search location globally across app
 * Created by andres on 1/19/15.
 */
public class SearchLocation {
    private static SearchLocation sInstance = null;

    public LatLng mSearchLatLng;
    public Address searchArea;

    private SearchLocation() {
    }

    public static SearchLocation getInstance() {
        if (sInstance == null) {
            sInstance = new SearchLocation();
        }
        return sInstance;
    }
}

