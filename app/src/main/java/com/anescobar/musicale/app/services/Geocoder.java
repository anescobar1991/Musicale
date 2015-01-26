package com.anescobar.musicale.app.services;

import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;

import com.anescobar.musicale.app.exceptions.NetworkNotAvailableException;
import com.anescobar.musicale.app.interfaces.AddressesFetcherTaskListener;
import com.anescobar.musicale.app.interfaces.LatLngFromAddressFetcherTaskListener;
import com.anescobar.musicale.app.utils.NetworkUtil;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by andres on 1/24/15.
 * class used to get reverse geocode info (returns list of address objects given a lat long)
 */
public class Geocoder {
    private NetworkUtil mNetworkUtil = new NetworkUtil();


    public Geocoder() {
    }

    public void getAddresses(LatLng latLng, AddressesFetcherTaskListener listener, Context context) throws NetworkNotAvailableException {
        if (mNetworkUtil.isNetworkAvailable(context)) {
            new GetAddressesTask(listener, context).execute(latLng);
        } else {
            throw new NetworkNotAvailableException("Not connected to network...");
        }
    }

    public void getLatLngFromAddress(String locationQuery, LatLngFromAddressFetcherTaskListener listener, Context context) throws NetworkNotAvailableException {
        if (mNetworkUtil.isNetworkAvailable(context)) {
            new GetLatLngFromAddressTask(listener, context).execute(locationQuery);
        } else {
            throw new NetworkNotAvailableException("Not connected to network...");
        }
    }

    private class GetAddressesTask extends AsyncTask<LatLng, Void, List<Address>> {
        private AddressesFetcherTaskListener mListener;
        private Context mContext;

        public GetAddressesTask(AddressesFetcherTaskListener listener, Context context) {
            this.mListener = listener;
            this.mContext = context;
        }

        @Override
        protected List<Address> doInBackground(LatLng... params) {
            android.location.Geocoder geocoder =
                    new android.location.Geocoder(mContext, Locale.getDefault());

            List<Address> addresses = new ArrayList<>();

            try {
                addresses = geocoder.getFromLocation(params[0].latitude,
                        params[0].longitude, 10);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPreExecute() {
            mListener.onAddressFetcherTaskAboutToStart();
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            mListener.onAddressFetcherTaskCompleted(addresses);
        }
    }

    private class GetLatLngFromAddressTask extends AsyncTask<String, Void, List<Address>> {
        private LatLngFromAddressFetcherTaskListener mListener;
        private Context mContext;

        public GetLatLngFromAddressTask(LatLngFromAddressFetcherTaskListener listener, Context context) {
            this.mListener = listener;
            this.mContext = context;
        }

        @Override
        protected List<Address> doInBackground(String... params) {
            android.location.Geocoder geocoder =
                    new android.location.Geocoder(mContext, Locale.getDefault());

            List<Address> addresses = new ArrayList<>();

            try {
                addresses = geocoder.getFromLocationName(params[0], 10);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPreExecute() {
            mListener.onLatLngFromAddressFetcherTaskAboutToStart();
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            mListener.onLatLngFromAddressFetcherTaskCompleted(addresses);
        }
    }
}