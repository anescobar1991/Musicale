package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
public class EventsListViewFragment extends Fragment implements RecyclerView.OnScrollListener{
    private OnEventsListViewFragmentInteractionListener mListener;
    private LinearLayoutManager mLayoutManager;
    private Button mLoadMoreEventsButton;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mEventsLoading;
    private NetworkUtil mNetworkUtil;

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
    }

    public EventsListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initializes networkUtil class
        mNetworkUtil = new NetworkUtil();
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
        //
        mNumberOfPagesLoaded = eventsPreferences.getInt("numberOfPagesLoaded", 0);
        mTotalNumberOfPages = eventsPreferences.getInt("totalNumberOfPages", 0);
        String serializedEvents = eventsPreferences.getString("events", null);

        //deserializes events if there are any
        if (serializedEvents != null) {
            Type listOfEvents = new TypeToken<ArrayList<Event>>(){}.getType();
            mEvents = gson.fromJson(serializedEvents, listOfEvents);
        }
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
            setEventsAdapter(mEvents);
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
        cacheEvents(mNumberOfPagesLoaded, mTotalNumberOfPages,mEvents);
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
        //if scroll state is settled or settling
        //checking both for better responsiveness
        if (state == 2 || state == 0 ) {
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

    private void setEventsAdapter(ArrayList<Event> events) {
        mEventsLoading.setVisibility(View.VISIBLE);

        // Create the adapter
        mAdapter = new EventsAdapter(getActivity(), mEvents);

        //set recycler view with adapter
        mRecyclerView.setAdapter(mAdapter);

        //sets the onScrollListener that will inform us of when user has scrolled to bottom of recycleView
        mRecyclerView.setOnScrollListener(this);

        mEventsLoading.setVisibility(View.GONE);
    }

    private void getEvents(Integer pageNumber, Session session, LatLng userLocation) {
        mNumberOfPagesLoaded ++;
        if (mNetworkUtil.isNetworkAvailable(getActivity())) {
            new EventsFetcherTask(session, userLocation).execute(pageNumber);
        } else {
            mListener.displayToastMessage(getString(R.string.error_no_network_connectivity), Toast.LENGTH_SHORT);
        }

    }

    private class EventsFetcherTask extends AsyncTask<Integer, Void, ArrayList<Event>> {
        private Session session;
        private LatLng userLocation;

        public EventsFetcherTask(Session session, LatLng userLocation) {
            this.session = session;
            this.userLocation = userLocation;
        }

        @Override
        protected ArrayList<Event> doInBackground(Integer... pageNumbers) {
            ArrayList<Event> events = new ArrayList<Event>();

            //send server request to get recommended events
//            PaginatedResult<Event> rawRecommendedEvents = new EventsFinder(session)
//                    .getRecommendedEvents(1, 20, userLocation.latitude, userLocation.longitude);

            //send server request to get events nearby
            PaginatedResult<Event> rawEventsNearby = new EventsFinder(session)
                    .getEvents(userLocation.latitude, userLocation.longitude, "30", pageNumbers[0], 20, null);

            //add recommendedEvents to arrayList
//            events.addAll(rawRecommendedEvents.getPageResults());

            //add eventsNearby to arrayList
            events.addAll(rawEventsNearby.getPageResults());

            mTotalNumberOfPages = rawEventsNearby.getTotalPages();

            return events;
        }
        @Override
        protected void onPreExecute() {
            mEventsLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList<Event> events) {
            //adds new events to mEvents
            mEvents.addAll(events);
            setEventsAdapter(events);
            mEventsLoading.setVisibility(View.GONE);
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

    //caches events to sharedPreferences
    private void cacheEvents(int numberOfPagesLoaded, int totalNumberOfPages,ArrayList<Event> events) {
        // stores events arrayList
        if (!events.isEmpty()) {
            Gson gson = new Gson();

            //writes into events shared preferences
            SharedPreferences eventsPreferences = getActivity().getSharedPreferences(HomeActivity.EVENTS_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = eventsPreferences.edit();

            //serialize events ArrayList
            Type listOfEvents = new TypeToken<ArrayList<Event>>(){}.getType();
            String serializedEvents = gson.toJson(events, listOfEvents);

            //puts serialized Events list in bundle for retrieval upon fragment creation
            editor.putString("events", serializedEvents);

            //stores numberOfPagesLoaded so next user session knows what is already cached
            editor.putInt("numberOfPagesLoaded", numberOfPagesLoaded);

            //stores totalNumberOfPages so next user session knows how many total number of pages exist
            editor.putInt("totalNumberOfPages", totalNumberOfPages);

            // commit the new additions!
            editor.apply();
        }
    }

}
