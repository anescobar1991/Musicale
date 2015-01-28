package com.anescobar.musicale.view.fragments;


import android.app.ActivityOptions;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.exceptions.LocationNotAvailableException;
import com.anescobar.musicale.app.exceptions.NetworkNotAvailableException;
import com.anescobar.musicale.app.interfaces.AddressesFetcherTaskListener;
import com.anescobar.musicale.app.interfaces.LatLngFromAddressFetcherTaskListener;
import com.anescobar.musicale.app.models.EventQueryResults;
import com.anescobar.musicale.app.models.SearchLocation;
import com.anescobar.musicale.app.services.Geocoder;
import com.anescobar.musicale.view.activities.EventsActivity;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTouch;

public class SearchFragment extends LocationAwareFragment implements AddressesFetcherTaskListener, LatLngFromAddressFetcherTaskListener {

    private SearchLocation mSearchLocation = SearchLocation.getInstance();

//    @InjectView(R.id.keyword_search_edit_text) EditText mKeywordSearchField;
    @InjectView(R.id.search_area_edit_text) EditText mSearchAreaField;
    @InjectView(R.id.submit_search_button) Button mSubmitSearchButton;
    @InjectView(R.id.loading_progressbar) ProgressBar mLoadingProgressbar;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        ButterKnife.inject(this, rootView);

//        //hides clear text button on launch if there is no text in field
//        if (mKeywordSearchField.getText().length() == 0) {
//            mKeywordSearchField.getCompoundDrawables()[2].setAlpha(0);
//        }

        return rootView;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mSearchLocation.mSearchArea != null) {
            mSearchAreaField.setText(mSearchLocation.mSearchArea);
        } else {
            getAndDisplayCurrentLocation();
        }
    }

// Will use this in next iteration

//    @SuppressWarnings("unused") // it's actually used, just injected by Butter Knife
//    @OnTextChanged(R.id.keyword_search_edit_text) void onTextChanged(CharSequence text) {
//        if (text.length() > 0) {
//            mKeywordSearchField.getCompoundDrawables()[2].setAlpha(255);
//        } else if (text.length() == 0) {
//            mKeywordSearchField.getCompoundDrawables()[2].setAlpha(0);
//        }
//    }
//
//    //onTouch listener for keyword search clear button
//    @SuppressWarnings("unused") // it's actually used, just injected by Butter Knife
//    @OnTouch(R.id.keyword_search_edit_text) boolean onClearTextButtonTouch(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_UP) {
//            int leftEdgeOfRightDrawable = mKeywordSearchField.getRight()
//                    - mKeywordSearchField.getCompoundDrawables()[2].getBounds().width();
//            if (event.getRawX() >= leftEdgeOfRightDrawable) {
//                mKeywordSearchField.setText("");
//                return true;
//            }
//        }
//        return false;
//    }

    @OnClick(R.id.submit_search_button)
    public void submitSearch() {
        try {
            new Geocoder().getLatLngFromAddress(mSearchAreaField.getText().toString(), this, getActivity().getApplicationContext());
        } catch (NetworkNotAvailableException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),R.string.error_no_network_connectivity, Toast.LENGTH_SHORT).show();
        }
    }

    //onTouch listener for keyword search clear button
    @SuppressWarnings("unused") // it's actually used, just injected by Butter Knife
    @OnTouch(R.id.search_area_edit_text) boolean onUseCurrentLocationButtonTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int leftEdgeOfRightDrawable = mSearchAreaField.getRight()
                    - mSearchAreaField.getCompoundDrawables()[2].getBounds().width();
            if (event.getRawX() >= leftEdgeOfRightDrawable) {
                getAndDisplayCurrentLocation();
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
        if (addresses.isEmpty()) {
            Toast.makeText(getActivity(),R.string.error_generic, Toast.LENGTH_SHORT).show();
        } else {
            mSearchLocation.mSearchArea = sanitizeAddressToDisplay(addresses.get(0));
            mSearchAreaField.setText(sanitizeAddressToDisplay(addresses.get(0)));
        }
    }

    private void getAndDisplayCurrentLocation() {
        try {
            new Geocoder().getAddresses(getCurrentLatLng(), this, getActivity().getApplicationContext());
        } catch (NetworkNotAvailableException e) {
            Toast.makeText(getActivity(),R.string.error_no_network_connectivity, Toast.LENGTH_SHORT).show();
        } catch (LocationNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private String sanitizeAddressToDisplay(Address address) {
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

        return displayLocation;
    }

    @Override
    public void onLatLngFromAddressFetcherTaskAboutToStart() {
        mSubmitSearchButton.setVisibility(View.GONE);
        mLoadingProgressbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLatLngFromAddressFetcherTaskCompleted(List<Address> addresses) {
        mLoadingProgressbar.setVisibility(View.GONE);
        mSubmitSearchButton.setVisibility(View.VISIBLE);

        if (addresses.isEmpty()) {
            Toast.makeText(getActivity(), R.string.location_not_resolvable_error, Toast.LENGTH_SHORT).show();
        } else {
            mSearchLocation.clearInstance();
            EventQueryResults.getInstance().clearInstance();

            mSearchLocation.mSearchArea = sanitizeAddressToDisplay(addresses.get(0));
            mSearchLocation.mSearchLatLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());

            Intent intent = new Intent(getActivity(), EventsActivity.class);
            ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getActivity().getApplicationContext(), R.anim.slide_in_right, R.anim.slide_out_left);

            startActivity(intent, activityOptions.toBundle());
        }
    }
}