package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.activities.HomeActivity;
import com.anescobar.musicale.adapters.EventsAdapter;
import com.anescobar.musicale.interfaces.OnEventsFetcherTaskCompleted;
import com.anescobar.musicale.utilsHelpers.EventsFinder;
import com.anescobar.musicale.utilsHelpers.NetworkUtil;
import com.anescobar.musicale.utilsHelpers.SessionManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Session;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventsListViewFragment.OnEventsListViewFragmentInteractionListener} interface
 * to handle interaction home.
 *
 */
public class EventsListViewFragment extends Fragment implements RecyclerView.OnScrollListener,
        OnEventsFetcherTaskCompleted {

    private OnEventsListViewFragmentInteractionListener mListener;
    private LinearLayoutManager mLayoutManager;
    private Button mLoadMoreEventsButton;
    private EventsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mEventsLoading;
    private NetworkUtil mNetworkUtil;

    private boolean mAdapterSet = false;
    private int mTotalNumberOfPages = 0; // stores how many total pages of events there are
    private int mNumberOfPagesLoaded = 0; //keeps track of how many pages are loaded
    private ArrayList<Event> mEvents = new ArrayList<Event>();
    private LatLng mUserLatLng;
    private Session mSession;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnEventsListViewFragmentInteractionListener {
        public void displayToastMessage(String message, int toastLength);
        public void cacheEvents(int numberOfPagesLoaded, int totalNumberOfPages,ArrayList<Event> events);
    }

    public EventsListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initializes networkUtil class
        mNetworkUtil = new NetworkUtil();

        //gets all sharedPreferences and stores them locally
        setCachedSettings();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_list_view, container, false);

        mLoadMoreEventsButton = (Button) view.findViewById(R.id.fragment_events_list_view_load_more_button);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_events_listView_event_cards_holder);
        mEventsLoading = (ProgressBar) view.findViewById(R.id.fragment_events_list_view_loading);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //sets on clickListener for load more events button
        mLoadMoreEventsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayLoadMoreEventsButton(false); //hide load more events button
                getEvents(mNumberOfPagesLoaded + 1, mSession, mUserLatLng);
            }
        });

        //if there are no events from previous saved session then fetch events from backend
        //else use events from previous saved session to populate cards
        if (mEvents.isEmpty()) {
            getEvents(1, mSession, mUserLatLng);
        } else {
            setEventsAdapter();
        }

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnEventsListViewFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onPause() {
        //caches all events data to sharedPreferences
        mListener.cacheEvents(mNumberOfPagesLoaded, mTotalNumberOfPages,mEvents);
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
            if (mLayoutManager.findLastCompletelyVisibleItemPosition() == itemCount && mTotalNumberOfPages > mNumberOfPagesLoaded) {
                mRecyclerView.scrollToPosition(itemCount); //scroll to last item to fix bug of button being on top of last item
                displayLoadMoreEventsButton(true); //display load more events Button
            } else if (mLayoutManager.findFirstCompletelyVisibleItemPosition() != itemCount) {
                displayLoadMoreEventsButton(false); //hide load more events button
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
            mAdapter = new EventsAdapter(getActivity(), mEvents);

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

    public void refreshEvents() {
        //forces fragment from getting cached settings
        setCachedSettings();

        //with this flag set to false adapter will set itself up again
        mAdapterSet = false;

        //sets adapter with cached settings
        setEventsAdapter();
    }

    //gets events from backend, keeps track of how many pages are already loaded and cached
    private void getEvents(Integer pageNumber, Session session, LatLng userLocation) {
        mNumberOfPagesLoaded ++;
        if (mNetworkUtil.isNetworkAvailable(getActivity())) {
            new EventsFinder(session, this, userLocation).getEvents(pageNumber);
        } else {
            mListener.displayToastMessage(getString(R.string.error_no_network_connectivity), Toast.LENGTH_SHORT);
        }
    }

    private void displayLoadMoreEventsButton(boolean display) {
        LinearLayout viewContainer = (LinearLayout) getActivity().findViewById(R.id.fragment_events_list_view_container);

        if (display) {
            viewContainer.setWeightSum(25); // weightSum is changed to account for removal of button from view
            mLoadMoreEventsButton.setVisibility(View.VISIBLE);
        } else {
            viewContainer.setWeightSum(24); // weightSum is changed to account for removal of button from view
            mLoadMoreEventsButton.setVisibility(View.GONE);
        }
    }

    //called onPreExecute of eventsFetcherTask
    @Override
    public void onTaskAboutToStart() {
        //display loading progressbar
        mEventsLoading.setVisibility(View.VISIBLE);
    }

    //called onPostExecute of eventsFetcherTask
    @Override
    public void onTaskCompleted(PaginatedResult<Event> eventsNearby) {
        ArrayList<Event> events= new ArrayList<Event>(eventsNearby.getPageResults());

        //add events to mEvents
        mEvents.addAll(events);

        mTotalNumberOfPages = eventsNearby.getTotalPages();

        //set events adapter with new events
        setEventsAdapter();

        //hide loading progressbar
        mEventsLoading.setVisibility(View.GONE);
    }

    private void setCachedSettings() {
        Gson gson = new Gson();

        // Gets session from sharedPreferences
        SharedPreferences sessionPreferences = getActivity().getSharedPreferences(SessionManager.SESSION_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String serializedSession = sessionPreferences.getString("userSession", null);
        if (serializedSession != null) {
            mSession = gson.fromJson(serializedSession, Session.class);
        } //TODO here is where we check for and act on errors

        //Gets user's location(LatLng serialized into string) from sharedPreferences
        SharedPreferences userLocationPreferences = getActivity().getSharedPreferences(HomeActivity.LOCATION_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String serializedLatLng = userLocationPreferences.getString("userCurrentLatLng", null);
        if (serializedLatLng != null) {
            //deserializes userLatLng string into LatLng object
            mUserLatLng = gson.fromJson(serializedLatLng, LatLng.class);
        } //TODO here is where we check for and act on errors

        //Gets Events data from sharedPreferences
        SharedPreferences eventsPreferences = getActivity().getSharedPreferences(HomeActivity.EVENTS_SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        mNumberOfPagesLoaded = eventsPreferences.getInt("numberOfPagesLoaded", 0);
        mTotalNumberOfPages = eventsPreferences.getInt("totalNumberOfPages", 0);
        String serializedEvents = eventsPreferences.getString("events", null);

        //deserializes events if there are any
        if (serializedEvents != null) {
            Type listOfEvents = new TypeToken<ArrayList<Event>>(){}.getType();
            mEvents = gson.fromJson(serializedEvents, listOfEvents);
        }
    }

}
