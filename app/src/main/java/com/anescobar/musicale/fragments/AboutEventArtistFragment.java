package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anescobar.musicale.R;
import com.anescobar.musicale.interfaces.ArtistInfoFetcherTaskListener;
import com.anescobar.musicale.utils.ArtistInfoSeeker;
import com.squareup.picasso.Picasso;

import de.umass.lastfm.Artist;
import de.umass.lastfm.ImageSize;

public class AboutEventArtistFragment extends Fragment implements ArtistInfoFetcherTaskListener{

    private static final String ARG_ARTIST = "artistArg";
    private AboutEventArtistFragmentInteractionListener mListener;
    private View mView;

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

        mView = inflater.inflate(R.layout.fragment_about_artist, container, false);

        Bundle args = getArguments();

        String artist = args.getString(ARG_ARTIST, null);


        // if there was an artist passed to fragment
        if (artist != null) {

            //gets artist info
            new ArtistInfoSeeker().getArtistInfo(artist, this);

            // should never happen but just in case...
        } else {
            mListener.displayErrorMessage(getActivity().getString(R.string.error_generic));
        }

        return mView;
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

    private void setUpView(Artist artist) {
        TextView artistName = (TextView) mView.findViewById(R.id.fragment_about_artist_artist_name);
        ImageView artistImage = (ImageView) mView.findViewById(R.id.fragment_about_artist_artist_image);
        TextView artistBio = (TextView) mView.findViewById(R.id.fragment_about_artist_bio);

        //-------------Loads dynamic data into view------------------
//        artistName.setText(artist.getName());
//        artistBio.setText(Html.fromHtml(artist.getWikiSummary()));
////        System.out.println(artist.getWikiSummary());

        String artistImageUrl = artist.getImageURL(ImageSize.EXTRALARGE);

        //downloads artist image into view if there is image
        if (artistImageUrl.length() > 0) {
            Picasso.with(getActivity()).load(artistImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .resize(400, 300)
                    .into(artistImage);
            //else will load placeholder image into view
        } else {
            artistImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onArtistInfoFetcherTaskAboutToStart() {

    }

    @Override
    public void onArtistInfoFetcherTaskCompleted(Artist artist) {
        setUpView(artist);
    }
}