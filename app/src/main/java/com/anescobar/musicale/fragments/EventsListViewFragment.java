package com.anescobar.musicale.fragments;

import android.app.Activity;
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

import com.anescobar.musicale.R;
import com.anescobar.musicale.adapters.EventsAdapter;
import com.anescobar.musicale.utilsHelpers.EventsFinder;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;

import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Session;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventsListViewFragment.OnEventsListViewFragmentInteractionListener} interface
 * to handle interaction home.
 * Use the {@link EventsListViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class EventsListViewFragment extends Fragment implements RecyclerView.OnScrollListener{
    private static final String ARG_SESSION_STRING = "com.anescobar.musicale.fragments.EventsMapViewFragment.session";
    private static final String ARG_USER_LOCATION_STRING = "com.anescobar.musicale.fragments.EventsMapViewFragment.userLocation";
    private OnEventsListViewFragmentInteractionListener mListener;
    private LinearLayoutManager mLayoutManager;
    private Button mLoadMoreEventsButton;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;

    private boolean mAdapterSet = false; //flag to make sure adapter is only set once
    private int mTotalNumberOfPages = 0; // stores how many total pages of events there are
    private int mNumberOfPagesLoaded = 0; //keeps track of how many pages are loaded
    private ArrayList<Event> mEvents = new ArrayList<Event>();
    private LatLng mUserLocation;
    private Session mSession;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnEventsListViewFragmentInteractionListener {
        public void onAttachDisplayTitle(int sectionIndex);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userLocationString LatLng object serialized as string to rep users current location
     * @return A new instance of fragment EventsListViewFragment.
     */
//    public static EventsListViewFragment newInstance(String userLocationString) {
//        EventsListViewFragment fragment = new EventsListViewFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_USER_LOCATION_STRING, userLocationString);
//        fragment.setArguments(args);
//        return fragment;
//    }

    public EventsListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = new Gson();

        //gets arguments and deserializes them
        if (getArguments() != null) {
            String sessionString = getArguments().getString(ARG_SESSION_STRING);
            String userLocationString = getArguments().getString(ARG_USER_LOCATION_STRING);
            mSession = gson.fromJson(sessionString, Session.class);
            mUserLocation = gson.fromJson(userLocationString, LatLng.class);
        }

        //gets saved instance data if any
        if (savedInstanceState != null ) {
            ArrayList<String> serializedEvents = savedInstanceState.getStringArrayList("events");
            //loops through mEvents, deserializes strings, and then adds it to serializedEvents arrayList
            for (String serializedEvent : serializedEvents) {
                mEvents.add(gson.fromJson(serializedEvent, Event.class));
            }
            mNumberOfPagesLoaded = savedInstanceState.getInt("numberOfPagesLoaded");
            mTotalNumberOfPages = savedInstanceState.getInt("totalNumberofPages");
            System.out.println("savedInstanceState is null");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_list_view, container, false);

        mLoadMoreEventsButton = (Button) view.findViewById(R.id.fragment_events_list_view_load_more_button);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_events_listView_event_cards_holder);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mLoadMoreEventsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayLoadMoreEventsButton(false); //hide load more events button
                getEvents(mNumberOfPagesLoaded + 1, mSession, mUserLocation);
                System.out.println(mNumberOfPagesLoaded);
            }
        });

        //if there are no events from previous saved session then fetch events from backend
        //else use events from previous saved session to populate cards
        if (mEvents.isEmpty()) {
            getEvents(1, mSession, mUserLocation);
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
    public void onSaveInstanceState(Bundle outState) {
        System.out.println("onsavedinstancecalled");
        super.onSaveInstanceState(outState);
        // no point doing this is there are no events in list
        if (!mEvents.isEmpty()) {
            Gson gson = new Gson();
            ArrayList<String> serializedEvents = new ArrayList<String>();

            //loops through mEvents, serializes each Event object, and then adds it to serializedEvents arrayList
            for (Event event : mEvents) {
                serializedEvents.add(gson.toJson(event));
            }

            //puts serializedEvents list in bundle for retrieval upon fragment creation
            outState.putStringArrayList("events", serializedEvents);

            //saves total number of pages of events
            outState.putInt("totalNumberofPages", mTotalNumberOfPages);

            //saves number of pages of events which have already been queried for and stored in arrayList
            outState.putInt("numberOfPagesLoaded", mNumberOfPagesLoaded);

        }
    }

    @Override
    public void onResume() {
        System.out.println("onResume of LoginFragment");
        super.onResume();
    }

    @Override
    public void onPause() {
        System.out.println("OnPause of loginFragment");
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
        //checking for better responsiveness
        if (state == 2 || state == 0 ) {
            int itemCount = mAdapter.getItemCount() - 1;
            //if user has scrolled to bottom of recycle view
            if (mLayoutManager.findLastCompletelyVisibleItemPosition() == itemCount) {
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
        //sets mEvents to events so that way it will be saved
        mEvents = events;

        if (!mAdapterSet) {
            //sets the onScrollListener that will inform us of when user has scrolled to bottom of recycleView
            mRecyclerView.setOnScrollListener(this);

            // Create the adapter
            mAdapter = new EventsAdapter(getActivity(), events);
            mRecyclerView.setAdapter(mAdapter);

            //sets mAdapterSet to true, this way next time instead of new adapter being created notifyDateChanged is called
            mAdapterSet = true;
        } else {
            System.out.println("in else statement");
            mAdapter.notifyDataSetChanged();
        }
    }

    private void getEvents(Integer pageNumber, Session session, LatLng userLocation) {
        mNumberOfPagesLoaded ++;

        //TODO getEvents
        new EventsFetcherTask(session, userLocation).execute(pageNumber);

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
            //TODO set mTotalPages
            ArrayList<Event> events = new ArrayList<Event>();

            //send server request to get recommended events
            PaginatedResult<Event> rawRecommendedEvents = new EventsFinder(session)
                    .getRecommendedEvents(1, 20, userLocation.latitude, userLocation.longitude);

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

        protected void onPreExecute() {
        }

        protected void onPostExecute(ArrayList<Event> events) {
            setEventsAdapter(events);
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
}
