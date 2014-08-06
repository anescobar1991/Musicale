package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.anescobar.musicale.R;
import com.anescobar.musicale.adapters.EventListAdapter;
import com.anescobar.musicale.utilsHelpers.EventsFinder;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;

import de.umass.lastfm.Caller;
import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Result;
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
    private LinearLayout mViewContainer;
    private Button mLoadMoreEventsButton;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private int mNumberOfPagesLoaded = 0; //keeps track of how many pages are loaded
    private LatLng mUserLocation;
    private Session mSession;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param sessionString session object serialized as string used for lastFm auth purposes
     * @param userLocationString LatLng object serialized as string to rep users current location
     * @return A new instance of fragment EventsListViewFragment.
     */
    public static EventsListViewFragment newInstance(String sessionString, String userLocationString) {
        EventsListViewFragment fragment = new EventsListViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SESSION_STRING, sessionString);
        args.putString(ARG_USER_LOCATION_STRING, userLocationString);
        fragment.setArguments(args);
        return fragment;
    }
    public EventsListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //gets arguments and deserializes them
        if (getArguments() != null) {
            String sessionString = getArguments().getString(ARG_SESSION_STRING);
            String userLocationString = getArguments().getString(ARG_USER_LOCATION_STRING);
            Gson gson = new Gson();
            mSession = gson.fromJson(sessionString, Session.class);
            mUserLocation = gson.fromJson(userLocationString, LatLng.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_list_view, container, false);

        mViewContainer = (LinearLayout) view.findViewById(R.id.fragment_eventsListView_linearLayout_view_container);
        mLoadMoreEventsButton = (Button) view.findViewById(R.id.fragment_eventsListView_button_load_more_button);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_eventsListView_recyclerView_eventCardListHolder);

        // loads events to view
        addEventsToView(1);

        mLoadMoreEventsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleLoadMoreEventsButtonVisibility(false); //display load more events button
                addEventsToView(mNumberOfPagesLoaded + 1);
            }
        });

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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (state == 0) {
            int itemCount = mAdapter.getItemCount() - 1;
            //if user has stopped scrolling and is at bottom of recycle view
            if (mLayoutManager.findLastCompletelyVisibleItemPosition() == itemCount) {
                mRecyclerView.scrollToPosition(itemCount);
                toggleLoadMoreEventsButtonVisibility(true); //display load more events Button
            } else if (mLayoutManager.findFirstCompletelyVisibleItemPosition() != itemCount) {
                toggleLoadMoreEventsButtonVisibility(false); //hide load more events button
            }
        }
    }

    @Override
    public void onScrolled(int i, int i2) {
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnEventsListViewFragmentInteractionListener {
        public void onAttachDisplayTitle(int sectionIndex);
    }

    private void addEventsToView(Integer pageNumber) {
        mNumberOfPagesLoaded ++;

        //TODO use real data for this
        // Data set used by the adapter. This data will be displayed.
        ArrayList<String> eventsList = new ArrayList<String>();
        for (int i= 0; i < pageNumber * 25; i++) {
            eventsList.add("Event " + i);
        }
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //sets the onScrollListener that will inform us of when user has scrolled to bottom of recycleView
        mRecyclerView.setOnScrollListener(this);

        // Create the adapter
        mAdapter = new EventListAdapter(getActivity(), eventsList);
        mRecyclerView.setAdapter(mAdapter);
    }

    //async task to add events to list
    private class AddEventsToViewTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... pageNumbers) {
            addEventsToView(pageNumbers[0]);

            return null;
        }

        protected void onPreExecute() {
        }

        protected void onPostExecute() {
        }
    }

    private void toggleLoadMoreEventsButtonVisibility(boolean display) {
        if (display) {
            mViewContainer.setWeightSum(25); // weightSum is changed to account for removal of button from view
            mLoadMoreEventsButton.setVisibility(View.VISIBLE);
        } else {
            mViewContainer.setWeightSum(24); // weightSum is changed to account for removal of button from view
            mLoadMoreEventsButton.setVisibility(View.GONE);
        }

    }
}
