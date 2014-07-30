package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anescobar.musicale.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

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
public class EventsListViewFragment extends Fragment {
    private static final String ARG_SESSION_STRING = "com.anescobar.musicale.fragments.EventsMapViewFragment.session";
    private static final String ARG_USER_LOCATION_STRING = "com.anescobar.musicale.fragments.EventsMapViewFragment.userLocation";
    private static final int SECTION_INDEX = 1; //index that identifies fragment for activity to display correct title
    private OnEventsListViewFragmentInteractionListener mListener;
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events_list_view, container, false);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnEventsListViewFragmentInteractionListener) activity;
            mListener.onAttachDisplayTitle(SECTION_INDEX); //sets title, index tells activity to display correct title
            mListener.onEventsViewAttached(true);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onEventsViewAttached(false);
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnEventsListViewFragmentInteractionListener {
        public void onAttachDisplayTitle(int sectionIndex);
        public void onEventsViewAttached(boolean isAttached);
    }

}
