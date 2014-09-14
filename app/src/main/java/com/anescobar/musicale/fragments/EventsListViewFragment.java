package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.activities.EventsActivity;
import com.anescobar.musicale.adapters.EventsAdapter;
import com.anescobar.musicale.interfaces.EventFetcherListener;
import com.anescobar.musicale.utils.EventsFinder;
import com.anescobar.musicale.utils.NetworkUtil;
import com.anescobar.musicale.models.EventQueryDetails;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;

import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class EventsListViewFragment extends Fragment implements RecyclerView.OnScrollListener,
        EventFetcherListener {

    private EventListViewFragmentInteractionListener mListener;
    private LinearLayoutManager mLayoutManager;
    private Button mLoadMoreEventsButton;
    private EventsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mEventsLoadingProgressBar;
    private NetworkUtil mNetworkUtil;
    private SmoothProgressBar mMoreEventsLoadingProgressBar;
    private Button mExploreInMapButton;

    private LatLng mLatLng;

    private EventQueryDetails mEventQueryDetails = EventQueryDetails.getInstance();

    private boolean mAdapterSet = false;

    //always use this to create new instance of this fragment
    public static EventsListViewFragment newInstance(LatLng currentLocation) {
        EventsListViewFragment eventsListViewFragment = new EventsListViewFragment();

        final Gson gson = new Gson();
        String serializedCurrentLatLng = gson.toJson(currentLocation, LatLng.class);
        Bundle args = new Bundle();
        args.putString("currentLatLng", serializedCurrentLatLng);
        eventsListViewFragment.setArguments(args);

        return eventsListViewFragment;
    }

        public EventsListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (EventListViewFragmentInteractionListener) activity;
            mListener.setListViewTitleInActionBar();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement EventMapViewFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //lets it know that this fragment has its own menu implementation
        setHasOptionsMenu(true);

        //initializes networkUtil class
        mNetworkUtil = new NetworkUtil();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_list_view, container, false);

        mLoadMoreEventsButton = (Button) view.findViewById(R.id.fragment_events_list_view_load_more_button);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_events_listView_event_cards_holder);
        mEventsLoadingProgressBar = (ProgressBar) view.findViewById(R.id.fragment_events_list_view_loading);
        mMoreEventsLoadingProgressBar = (SmoothProgressBar) view.findViewById(R.id.fragment_events_list_view_more_events_loading);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mExploreInMapButton = (Button) view.findViewById(R.id.fragment_events_list_view_explore_in_map);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //sets on clickListener for load more events button
        mLoadMoreEventsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getEventsFromServer(mEventQueryDetails.numberOfEventPagesLoaded + 1, mLatLng);
            }
        });

        //sets on clickListener for explore in map button
        mExploreInMapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mListener.addFragmentToActivity(R.id.activity_events_container, EventsMapViewFragment.newInstance(mLatLng), EventsActivity.EVENTS_MAP_VIEW_FRAGMENT_TAG);

            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.findItem(R.id.action_refresh_events).setVisible(true);
        menu.findItem(R.id.action_search_events).setVisible(true);
    }


    // Called at the start of the visible lifetime.
    @Override
    public void onStart(){
        super.onStart();

        //gets serialized latlng string from bundle and deserializes it
        final Gson gson = new Gson();
        String serializedLatLng = getArguments().getString("currentLatLng", null);

        if (serializedLatLng != null) {
            LatLng currentLatLng = gson.fromJson(serializedLatLng, LatLng.class);

            //adds events to view
            loadEventsToView(currentLatLng);

            //stores latLng
            mLatLng = currentLatLng;
        } else {
            //TODO catch this error scenario
        }
    }

    //makes sure that load more vents button is only displayed when scrolled to bottom of screen
    //and when there are more pages left of events
    @Override
    public void onScrollStateChanged(int state) {
        //if scroll state is settled or settling then check if load more button should be displayed
        //checking both for better responsiveness
        if (state == RecyclerView.SCROLL_STATE_SETTLING || state == RecyclerView.SCROLL_STATE_IDLE ) {
            int itemCount = mAdapter.getItemCount() - 1;

            //if user has scrolled to bottom of recycle view and there are still pages of events left
            if (mLayoutManager.findLastCompletelyVisibleItemPosition() == itemCount && mEventQueryDetails.totalNumberOfEventPages > mEventQueryDetails.numberOfEventPagesLoaded) {
                //scroll to last item to fix bug of button being on top of last item
                mRecyclerView.scrollToPosition(itemCount);

                //display load more events Button
                displayLoadMoreEventsButton(true);
            } else if (mLayoutManager.findFirstCompletelyVisibleItemPosition() != itemCount) {
                //hide load more events button
                displayLoadMoreEventsButton(false);
            }
        }
    }

    @Override
    public void onScrolled(int i, int i2) {
    }

    private void setEventsAdapter() {
        // only sets up adapter if it hasnt been setup already
        if (!mAdapterSet) {
            // Create the adapter
            mAdapter = new EventsAdapter(getActivity(), mEventQueryDetails.events);

            //set recycler view with adapter
            mRecyclerView.setAdapter(mAdapter);

            //sets the onScrollListener that will inform us of when user has scrolled to bottom of recycleView
            mRecyclerView.setOnScrollListener(this);

            mAdapterSet = true;
        } else {
            //notifies adapter of data set change so that it can update view
            mAdapter.notifyDataSetChanged();
        }
    }

    //gets events from backend, keeps track of how many pages are already loaded and cached
    public void getEventsFromServer(Integer pageNumber, LatLng userLocation) {

        if (mNetworkUtil.isNetworkAvailable(getActivity())) {
            mEventQueryDetails.numberOfEventPagesLoaded = pageNumber;

            new EventsFinder(this, userLocation).getEvents(pageNumber);
        } else {
            Toast.makeText(getActivity(),getString(R.string.error_no_network_connectivity),Toast.LENGTH_SHORT).show();
        }
    }

    private void displayLoadMoreEventsButton(boolean display) {
        LinearLayout viewContainer = (LinearLayout) getActivity().findViewById(R.id.fragment_events_list_view_container);

        if (display) {
            // weightSum is changed to account for removal of button from view
            viewContainer.setWeightSum(25);
            mLoadMoreEventsButton.setVisibility(View.VISIBLE);
        } else {
            // weightSum is changed to account for removal of button from view
            viewContainer.setWeightSum(24);
            mLoadMoreEventsButton.setVisibility(View.GONE);
        }
    }

    //called onPreExecute of eventsFetcherTask
    @Override
    public void onTaskAboutToStart() {
        //display loading progressbar at bottom of screen if it is loading more events after first page
        if (mEventQueryDetails.numberOfEventPagesLoaded > 1) {
            mLoadMoreEventsButton.setVisibility(View.GONE);
            mMoreEventsLoadingProgressBar.setVisibility(View.VISIBLE);
            //display loading progressbar in middle of screen if it is loading first page of events
        } else {
            //hide explore in map button
            mExploreInMapButton.setVisibility(View.GONE);

            displayLoadMoreEventsButton(false);
            mRecyclerView.setVisibility(View.INVISIBLE);
            mEventsLoadingProgressBar.setVisibility(View.VISIBLE);
        }
    }

    //called onPostExecute of eventsFetcherTask
    @Override
    public void onTaskCompleted(PaginatedResult<Event> eventsNearby) {
        //display explore in map button
        mExploreInMapButton.setVisibility(View.VISIBLE);

        //if last call was successful then load events to screen
        if (Caller.getInstance().getLastResult().isSuccessful()) {
            ArrayList<Event> events= new ArrayList<Event>(eventsNearby.getPageResults());

            //set variable that stores total number of pages
            mEventQueryDetails.totalNumberOfEventPages = eventsNearby.getTotalPages();

            if (mEventQueryDetails.numberOfEventPagesLoaded == 1) {
                //clears events list before adding events to it
                mEventQueryDetails.events.clear();
            }
            //add events to mEvents
            mEventQueryDetails.events.addAll(events);

            //set events adapter with new events
            setEventsAdapter();
        } else {
            //if call to backend was not successful
            Toast.makeText(getActivity(),getString(R.string.error_generic),Toast.LENGTH_SHORT).show();
        }

        if (mEventQueryDetails.numberOfEventPagesLoaded == 1) {
            //hide loading progressbar in middle of screen
            mEventsLoadingProgressBar.setVisibility(View.GONE);

            //scrolls to top of recycler view because brand new set of events was loaded
            mRecyclerView.scrollToPosition(0);

            mRecyclerView.setVisibility(View.VISIBLE);
            //hides loading progressbar at bottom of screen if it is loading more events after first page
        } else {
            mMoreEventsLoadingProgressBar.setVisibility(View.GONE);
            LinearLayout viewContainer = (LinearLayout) getActivity().findViewById(R.id.fragment_events_list_view_container);
            viewContainer.setWeightSum(24);
        }
    }

    //loads events and sets adapter that will display them in recycler view
    private void loadEventsToView(LatLng latLng) {
        //if there are no events from previous saved session then fetch events from backend
        //else use events from previous saved session to populate cards
        if (mEventQueryDetails.events.isEmpty()) {
            getEventsFromServer(1, latLng);
        } else {
            setEventsAdapter();
        }
    }

    public void setCurrentLatLng(LatLng newLatLng) {
        mLatLng = newLatLng;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface EventListViewFragmentInteractionListener {
        public void addFragmentToActivity(int container, Fragment fragment, String fragmentTag);
        public void setListViewTitleInActionBar();
    }
}