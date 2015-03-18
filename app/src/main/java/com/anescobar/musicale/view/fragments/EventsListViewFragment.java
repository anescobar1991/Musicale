package com.anescobar.musicale.view.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.adapters.EventsAdapter;
import com.anescobar.musicale.app.services.interfaces.EventFetcherListener;
import com.anescobar.musicale.app.models.EventQueryResults;
import com.anescobar.musicale.app.services.exceptions.LocationNotAvailableException;
import com.anescobar.musicale.app.services.exceptions.NetworkNotAvailableException;
import com.anescobar.musicale.app.services.EventsFinder;
import com.anescobar.musicale.view.activities.SearchActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;

public class EventsListViewFragment extends LocationAwareFragment implements EventFetcherListener,
        SwipeRefreshLayout.OnRefreshListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private LinearLayoutManager mLayoutManager;
    private EventsAdapter mAdapter;

    @InjectView(R.id.events_recyclerview) RecyclerView mRecyclerView;
    @InjectView(R.id.events_list_message_container) TextView mMessageContainer;
    @InjectView(R.id.events_list_progressbar) ProgressBar mEventsLoadingProgressBar;
    @InjectView(R.id.events_list_swipe_refresh_layout) SwipeRefreshLayout mEventsListSwipeRefreshLayout;

    private boolean mCurrentlyGettingEvents = false;
    private EventQueryResults mEventQueryResults = EventQueryResults.getInstance();

    private boolean mAdapterSet = false;

    public EventsListViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_list_view, container, false);

        ButterKnife.inject(this, view);

        setHasOptionsMenu(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mEventsListSwipeRefreshLayout.setOnRefreshListener(this);
        mEventsListSwipeRefreshLayout.setColorSchemeResources(
                R.color.accent, R.color.primary, R.color.accent_dark, R.color.primary_dark
        );

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem exploreInMapButton = menu.findItem(R.id.action_explore_in_map);
        MenuItem viewInListButton = menu.findItem(R.id.action_view_in_list);

        exploreInMapButton.setVisible(true);
        viewInListButton.setVisible(false);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onRefresh() {
        refreshEvents(mSearchLocation.searchLatLng);
    }

    @Override
    public void onStart() {
        if (mSearchLocation.searchLatLng != null) {
            loadEventsToView(mSearchLocation.searchLatLng);
        }

        super.onStart();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mSearchLocation.searchLatLng == null) {
            try {
                loadEventsToView(getCurrentLatLng());
            } catch (LocationNotAvailableException e) {
                Toast.makeText(getActivity(), getString(R.string.error_location_services_disabled), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setEventsAdapter() {
        if (!mAdapterSet) {
            mAdapter = new EventsAdapter(getActivity(), mEventQueryResults.events);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int state) {}

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    int loadNextPagePoint = mAdapter.getItemCount()/2;

                    if (mLayoutManager.findFirstVisibleItemPosition() > loadNextPagePoint && !mCurrentlyGettingEvents && mEventQueryResults.totalNumberOfEventPages > mEventQueryResults.numberOfEventPagesLoaded) {
                        getEventsFromServer(mEventQueryResults.numberOfEventPagesLoaded + 1, mSearchLocation.searchLatLng);
                    }
                }
            });

            mAdapterSet = true;
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void getEventsFromServer(Integer pageNumber, LatLng userLocation) {
        try {
            mEventQueryResults.numberOfEventPagesLoaded = pageNumber;
            new EventsFinder().getEvents(pageNumber, userLocation, this, getActivity().getApplicationContext());
        } catch (NetworkNotAvailableException e) {
            e.printStackTrace();

            if (pageNumber == 1) {
                setErrorMessage(getString(R.string.error_no_network_connectivity));
            } else {
                mEventsListSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity(),getString(R.string.error_no_network_connectivity),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onEventFetcherTaskAboutToStart() {
        mCurrentlyGettingEvents = true;
        mMessageContainer.setVisibility(View.GONE);

        if (mEventQueryResults.numberOfEventPagesLoaded == 1) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            if (!mEventsListSwipeRefreshLayout.isRefreshing()) {
                mEventsLoadingProgressBar.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onEventFetcherTaskCompleted(PaginatedResult<Event> eventsNearby) {
        mEventsListSwipeRefreshLayout.setRefreshing(false);
        mCurrentlyGettingEvents = false;

        if (Caller.getInstance().getLastResult().isSuccessful()) {
            ArrayList<Event> events= new ArrayList<>(eventsNearby.getPageResults());

            mEventQueryResults.totalNumberOfEventPages = eventsNearby.getTotalPages();

            if (mEventQueryResults.numberOfEventPagesLoaded == 1) {
                mEventQueryResults.events.clear();
            }
            mEventQueryResults.events.addAll(events);

            setEventsAdapter();
        } else {
            setErrorMessage(getString(R.string.error_generic));
        }

        if (mEventQueryResults.numberOfEventPagesLoaded == 1) {
            //hide loading progressbar in middle of screen
            mEventsLoadingProgressBar.setVisibility(View.GONE);

            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    //loads events and sets adapter that will display them in recycler view
    private void loadEventsToView(LatLng latLng) {
        //if there are no events from previous saved session then fetch events from backend
        //else use events from previous saved session to populate cards
        if (mEventQueryResults.events.isEmpty()) {
            getEventsFromServer(1, latLng);
        } else {
            setEventsAdapter();
        }
    }

    private void setErrorMessage(String message) {
        mEventsListSwipeRefreshLayout.setRefreshing(false);
        mRecyclerView.setVisibility(View.GONE);
        mMessageContainer.setText(message);
        mMessageContainer.setVisibility(View.VISIBLE);
    }

    public void refreshEvents(LatLng searchLatLng) {
        getEventsFromServer(1, searchLatLng);
    }

    @OnClick(R.id.search_fab)
    public void startSearchActivity() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getActivity(), R.anim.slide_in_right, R.anim.slide_out_left);

        getActivity().startActivity(intent, activityOptions.toBundle());
    }

}