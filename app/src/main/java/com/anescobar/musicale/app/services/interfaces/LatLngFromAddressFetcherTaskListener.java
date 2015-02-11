package com.anescobar.musicale.app.services.interfaces;

import android.location.Address;

import java.util.List;

/**
 * Callback listener for latlng from address fetcher async task
 * Created by Andres Escobar on 1/25/15.
 */
public interface LatLngFromAddressFetcherTaskListener {
    void onLatLngFromAddressFetcherTaskAboutToStart();
    void onLatLngFromAddressFetcherTaskCompleted(List<Address> addresses);
}
