package com.anescobar.musicale.view.fragments;


import android.location.Address;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.exceptions.LocationNotAvailableException;
import com.anescobar.musicale.app.exceptions.NetworkNotAvailableException;
import com.anescobar.musicale.app.interfaces.AddressesFetcherTaskListener;
import com.anescobar.musicale.app.models.SearchLocation;
import com.anescobar.musicale.rest.services.Geocoder;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;
import butterknife.OnTouch;

public class SearchFragment extends LocationAwareFragment implements AddressesFetcherTaskListener{

    private SearchLocation mSearchLocation = SearchLocation.getInstance();

    @InjectView(R.id.keyword_search_edit_text) EditText mKeywordSearchField;
    @InjectView(R.id.search_area_edit_text) EditText mSearchAreaField;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        ButterKnife.inject(this, rootView);

        //hides clear text button on launch if there is no text in field
        if (mKeywordSearchField.getText().length() == 0) {
            mKeywordSearchField.getCompoundDrawables()[2].setAlpha(0);
        }

        return rootView;
    }

    @Override
    public void onConnected(Bundle bundle) {
        displayCurrentLocationInSearchAreaField();
    }

    @OnTextChanged(R.id.keyword_search_edit_text) void onTextChanged(CharSequence text) {
        if (text.length() > 0) {
            mKeywordSearchField.getCompoundDrawables()[2].setAlpha(255);
        } else if (text.length() == 0) {
            mKeywordSearchField.getCompoundDrawables()[2].setAlpha(0);
        }
    }

    //onTouch listener for keyword search clear button
    @OnTouch(R.id.keyword_search_edit_text) boolean onClearTextButtonTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int leftEdgeOfRightDrawable = mKeywordSearchField.getRight()
                    - mKeywordSearchField.getCompoundDrawables()[2].getBounds().width();
            if (event.getRawX() >= leftEdgeOfRightDrawable) {
                mKeywordSearchField.setText("");
                return true;
            }
        }
        return false;
    }

    //onTouch listener for keyword search clear button
    @OnTouch(R.id.search_area_edit_text) boolean onUseCurrentLocationButtonTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int leftEdgeOfRightDrawable = mSearchAreaField.getRight()
                    - mSearchAreaField.getCompoundDrawables()[2].getBounds().width();
            if (event.getRawX() >= leftEdgeOfRightDrawable) {
                displayCurrentLocationInSearchAreaField();
                return true;
            }
        }
        return false;
    }


    @Override
    public void onAddressFetcherTaskAboutToStart() {
    }

    @Override
    public void onAddressFetcherTaskCompleted(List<Address> addresses) {
        Address address = addresses.get(0);
        String displayLocation;

        if (address.getLocality() == null) {
            displayLocation = address.getFeatureName();
        } else {
            displayLocation = address.getLocality();
        }

        if (address.getAdminArea() != null) {
            displayLocation += " " + address.getAdminArea();
        }

        if (address.getCountryCode() != null) {
            displayLocation += " " + address.getCountryName();
        }

        mSearchAreaField.setText(displayLocation);
    }

    private void displayCurrentLocationInSearchAreaField() {
        try {
            if (mSearchLocation.mSearchLatLng != null) {
                new Geocoder().getAddresses(mSearchLocation.mSearchLatLng, this, getActivity().getApplicationContext());
            } else {
                new Geocoder().getAddresses(getCurrentLatLng(), this, getActivity().getApplicationContext());
            }
        } catch (NetworkNotAvailableException e) {
            Toast.makeText(getActivity(),R.string.error_no_network_connectivity, Toast.LENGTH_SHORT).show();
        } catch (LocationNotAvailableException e) {
            e.printStackTrace();
        }
    }
}