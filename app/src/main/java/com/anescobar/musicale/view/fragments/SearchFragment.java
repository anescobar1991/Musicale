package com.anescobar.musicale.view.fragments;


import android.app.ActivityOptions;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.services.exceptions.LocationNotAvailableException;
import com.anescobar.musicale.app.services.exceptions.NetworkNotAvailableException;
import com.anescobar.musicale.app.services.interfaces.AddressesFetcherTaskListener;
import com.anescobar.musicale.app.services.interfaces.LatLngFromAddressFetcherTaskListener;
import com.anescobar.musicale.app.models.EventQueryResults;
import com.anescobar.musicale.app.models.SearchLocation;
import com.anescobar.musicale.app.services.Geocoder;
import com.anescobar.musicale.view.activities.EventsActivity;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import butterknife.OnTouch;

public class SearchFragment extends LocationAwareFragment implements AddressesFetcherTaskListener, LatLngFromAddressFetcherTaskListener {

    private SearchLocation mSearchLocation = SearchLocation.getInstance();

//    @InjectView(R.id.keyword_search_edit_text) EditText mKeywordSearchField;
    @InjectView(R.id.search_area_edit_text) EditText mSearchAreaField;
    @InjectView(R.id.loading_progressbar) ProgressBar mLoadingProgressbar;
    @InjectView(R.id.use_current_loc_button) Button mUseCurrentLocButton;
    @InjectView(R.id.use_current_loc_button_shadow) View mCurrentLocButtonShadow;

    public SearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        ButterKnife.inject(this, rootView);

//        //hides clear text button on launch if there is no text in field
//        if (mKeywordSearchField.getText().length() == 0) {
//            mKeywordSearchField.getCompoundDrawables()[2].setAlpha(0);
//        }
        if (mSearchAreaField.getText().length() == 0) {
//            mSubmitSearchButton.setEnabled(false);
            mSearchAreaField.getCompoundDrawables()[2].setAlpha(0);
        }

        return rootView;
    }

    @Override
    public void onConnected(Bundle bundle) {}

    @SuppressWarnings("unused") // it's actually used, just injected by Butter Knife
    @OnEditorAction(R.id.search_area_edit_text) boolean onEditorAction(KeyEvent key) {
        submitSearch();
        return true;
    }
    // Will use this in next iteration

//    @OnTextChanged(R.id.keyword_search_edit_text) void onTextChanged(CharSequence text) {
//        if (text.length() > 0) {
//            mKeywordSearchField.getCompoundDrawables()[2].setAlpha(255);
//        } else if (text.length() == 0) {
//            mKeywordSearchField.getCompoundDrawables()[2].setAlpha(0);
//        }
//    }
//
//    //onTouch listener for keyword search clear button
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

    @OnTextChanged(R.id.search_area_edit_text) void onTextChanged(CharSequence text) {
        if (text.length() > 0) {
            mSearchAreaField.getCompoundDrawables()[2].setAlpha(255);
        } else if (text.length() == 0) {
            mSearchAreaField.getCompoundDrawables()[2].setAlpha(0);
        }
    }

    //onTouch listener for search area clear button
    @OnTouch(R.id.search_area_edit_text) boolean onClearTextButtonTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int leftEdgeOfRightDrawable = mSearchAreaField.getRight()
                    - mSearchAreaField.getCompoundDrawables()[2].getBounds().width();
            if (event.getRawX() >= leftEdgeOfRightDrawable) {
                mSearchAreaField.setText("");
                return true;
            }
        }
        return false;
    }

    @OnClick(R.id.use_current_loc_button)
    public void useCurrentLocation(View view) {
        try {
            if (mGoogleApiClient.isConnected()) {
                new Geocoder().getAddresses(getCurrentLatLng(), this, getActivity().getApplicationContext());
            } else {
                useCurrentLocation(view);
            }
        } catch (NetworkNotAvailableException e) {
            Toast.makeText(getActivity(), R.string.error_no_network_connectivity, Toast.LENGTH_SHORT).show();
        } catch (LocationNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void submitSearch() {
        if (mSearchAreaField.getText().length() > 0) {
            mAnalyticsUtil.sendAnalyticsEvent(getString(R.string.ga_action_event), getString(R.string.ga_event_search_submitted), mSearchAreaField.getText().toString());
            try {
                new Geocoder().getLatLngFromAddress(mSearchAreaField.getText().toString(), this, getActivity().getApplicationContext());
            } catch (NetworkNotAvailableException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(),R.string.error_no_network_connectivity, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAddressFetcherTaskAboutToStart() {
        mLoadingProgressbar.setVisibility(View.VISIBLE);
        mUseCurrentLocButton.setVisibility(View.GONE);
        mCurrentLocButtonShadow.setVisibility(View.GONE);
    }

    @Override
    public void onAddressFetcherTaskCompleted(List<Address> addresses) {
        mLoadingProgressbar.setVisibility(View.GONE);
        mUseCurrentLocButton.setVisibility(View.VISIBLE);
        mCurrentLocButtonShadow.setVisibility(View.VISIBLE);

        if (addresses.isEmpty()) {
            Toast.makeText(getActivity(),R.string.error_generic, Toast.LENGTH_SHORT).show();
        } else {
            mSearchLocation.clearInstance();
            EventQueryResults.getInstance().clearInstance();

            mSearchLocation.searchArea = getSanitizedAddress(addresses.get(0));
            mSearchLocation.searchLatLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());

            startEventsActivity();
        }
    }

    @Override
    public void onLatLngFromAddressFetcherTaskAboutToStart() {
        mLoadingProgressbar.setVisibility(View.VISIBLE);
        mUseCurrentLocButton.setVisibility(View.GONE);
        mCurrentLocButtonShadow.setVisibility(View.GONE);
    }

    @Override
    public void onLatLngFromAddressFetcherTaskCompleted(List<Address> addresses) {
        mLoadingProgressbar.setVisibility(View.GONE);
        mUseCurrentLocButton.setVisibility(View.VISIBLE);
        mCurrentLocButtonShadow.setVisibility(View.VISIBLE);

        if (addresses.isEmpty()) {
            Toast.makeText(getActivity(), R.string.location_not_resolvable_error, Toast.LENGTH_SHORT).show();
        } else {
            mSearchLocation.clearInstance();
            EventQueryResults.getInstance().clearInstance();

            mSearchLocation.searchArea = getSanitizedAddress(addresses.get(0));
            mSearchLocation.searchLatLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());

            startEventsActivity();
        }
    }

    private void startEventsActivity() {
        Intent intent = new Intent(getActivity(), EventsActivity.class);
        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getActivity().getApplicationContext(), R.anim.slide_in_right, R.anim.slide_out_left);

        getActivity().startActivity(intent, activityOptions.toBundle());
    }

    private String getSanitizedAddress(Address address) {
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
}