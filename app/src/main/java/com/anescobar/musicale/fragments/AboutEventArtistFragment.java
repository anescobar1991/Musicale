package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anescobar.musicale.R;
import com.anescobar.musicale.interfaces.ArtistInfoFetcherTaskListener;
import com.anescobar.musicale.utils.ArtistInfoSeeker;
import com.google.gson.Gson;

import de.umass.lastfm.Artist;

public class AboutEventArtistFragment extends Fragment implements ArtistInfoFetcherTaskListener{

    private static final String ARG_ARTIST = "artistArg";
    private AboutEventArtistFragmentInteractionListener mListener;

    public AboutEventArtistFragment() {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface AboutEventArtistFragmentInteractionListener {
        public void displayErrorMessage(String message);
    }

    public static AboutEventArtistFragment newInstance(String artist) {
        AboutEventArtistFragment fragment = new AboutEventArtistFragment();

        //creates new Bundle
        Bundle args = new Bundle();

        //adds serialized event to bundle
        args.putString(ARG_ARTIST, artist);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about_artist, container, false);

        Bundle args = getArguments();

        String artist = args.getString(ARG_ARTIST, null);


        // if there was an artist passed to fragment
        if (artist != null) {

            //sets up view
            setUpView(artist, view);

            // should never happen but just in case...
        } else {
            mListener.displayErrorMessage(getActivity().getString(R.string.error_generic));
        }

        return inflater.inflate(
                R.layout.fragment_about_artist, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AboutEventArtistFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AboutEventArtistFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setUpView(String artist, View view) {
        new ArtistInfoSeeker().getArtistInfo(artist, this);
    }

    @Override
    public void onArtistInfoFetcherTaskAboutToStart() {

    }

    @Override
    public void onArtistInfoFetcherTaskCompleted(Artist artist) {
    System.out.println(new Gson().toJson(artist, Artist.class));
    }
}