package com.anescobar.musicale.app.services.interfaces;

import android.location.Address;

import java.util.List;

/**
 * Created by andres on 1/24/15.
 * Callback listener for artist info fetcher async task
 */
public interface AddressesFetcherTaskListener {
    void onAddressFetcherTaskAboutToStart();
    void onAddressFetcherTaskCompleted(List<Address> addresses);
}