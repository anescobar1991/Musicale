package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.anescobar.musicale.R;
import com.anescobar.musicale.activities.ArtistDetailsActivity;
import com.anescobar.musicale.interfaces.ArtistInfoFetcherTaskListener;
import com.anescobar.musicale.interfaces.ArtistTopTracksFetcherTaskListener;
import com.anescobar.musicale.utils.ArtistInfoSeeker;
import com.squareup.picasso.Picasso;

import java.util.Collection;

import de.umass.lastfm.Artist;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Track;

public class AboutArtistFragment extends Fragment implements ArtistInfoFetcherTaskListener,
        ArtistTopTracksFetcherTaskListener {

    private static final String ARG_ARTIST = "artistArg";
    private AboutEventArtistFragmentInteractionListener mListener;
    private View mView;
    private LinearLayout mSimilarArtistsContainer;
    private LinearLayout mTopTracksContainer;
    private ProgressBar mTracksLoadingProgressBar;

    public AboutArtistFragment() {}

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface AboutEventArtistFragmentInteractionListener {
        public void displayErrorMessage(String message);
    }

    public static AboutArtistFragment newInstance(String artist) {
        AboutArtistFragment fragment = new AboutArtistFragment();

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

    private void setUpView(final Artist artist) {
        TextView artistName = (TextView) mView.findViewById(R.id.fragment_about_artist_artist_name);
        ImageView artistImage = (ImageView) mView.findViewById(R.id.fragment_about_artist_artist_image);
        TextView artistBio = (TextView) mView.findViewById(R.id.fragment_about_artist_bio);
        TextView artistTags = (TextView) mView.findViewById(R.id.fragment_about_artist_tags);
        mSimilarArtistsContainer = (LinearLayout) mView.findViewById(R.id.fragment_about_artist_similar_artists_container);
        mTopTracksContainer = (LinearLayout) mView.findViewById(R.id.fragment_about_artist_top_tracks_container);
        mTracksLoadingProgressBar = (ProgressBar) mView.findViewById(R.id.fragment_about_artist_tracks_loading_progressbar);

        //-------------Loads dynamic data into view------------------
        artistName.setText(artist.getName());
        artistBio.setText(Html.fromHtml(artist.getWikiSummary()).toString());

        Collection<String> tags = artist.getTags();

        String formattedTags = "";

        //add tags to formattedTags string
        for(String tag : tags) {
            formattedTags += tag + ", ";
        }
        if (formattedTags.length() > 0) {
            //remove last comma from formatted tags string
            formattedTags = formattedTags.substring(0, formattedTags.length()-2);

            //sets artistTags textview to display formatted tags string
            artistTags.setText(formattedTags);
        }

        String artistImageUrl = artist.getImageURL(ImageSize.EXTRALARGE);

        //downloads artist image into view if there is image
        if (artistImageUrl.length() > 0) {
            Picasso.with(getActivity()).load(artistImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .resize(400, 400)
                    .into(artistImage);
            //else will load placeholder image into view
        } else {
            artistImage.setImageResource(R.drawable.placeholder);
        }

        //display similar artists on screen
        displaySimilarArtists(artist.getSimilar());
    }

    @Override
    public void onArtistInfoFetcherTaskAboutToStart() {

    }

    @Override
    public void onArtistInfoFetcherTaskCompleted(Artist artist) {
        new ArtistInfoSeeker().getArtistTopTracks(artist.getName(), this);

        setUpView(artist);
    }

    private void setUpArtistCard(final Artist artist, final LinearLayout parentView) {
        LayoutInflater vi = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.artist_card, parentView, false);


        LinearLayout artistCard = (LinearLayout) view.findViewById(R.id.artist_card);
        ImageView artistImageView = (ImageView) view.findViewById(R.id.artist_card_image);
        TextView artistTitleTextView = (TextView) view.findViewById(R.id.artist_card_image_text_field);

        //sets artist card details
        artistTitleTextView.setText(artist.getName());

        String eventImageUrl = artist.getImageURL(ImageSize.EXTRALARGE);
        // if there is an image for the artist load it into view. Else load placeholder into view
        if (eventImageUrl.length() > 0) {
            Picasso.with(getActivity())
                    .load(eventImageUrl)
                    .resize(120, 120)
                    .placeholder(R.drawable.placeholder)
                    .into(artistImageView);
        } else {
            artistImageView.setImageResource(R.drawable.placeholder);
        }

        //sets onClickListener for entire card
        artistCard.setOnClickListener(new LinearLayout.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ArtistDetailsActivity.class);
                intent.putExtra("ARTIST", artist.getName());
                startActivity(intent);
            }
        });

        parentView.addView(view);
    }

    private void addTopTrackLink(final Track track, final LinearLayout parentView) {
        LayoutInflater vi = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.track_link_view, parentView, false);

        LinearLayout trackLayout = (LinearLayout) view.findViewById(R.id.track_view_layout);
        TextView trackTextView = (TextView) view.findViewById(R.id.track_link_view);

        //sets click listener for when user taps on layout
        trackLayout.setOnClickListener(new LinearLayout.OnClickListener() {
            @Override
            public void onClick(View view) {
                //opens track link in browser
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(track.getUrl()));
                startActivity(i);
            }
        });

        trackTextView.setText(track.getName());

        parentView.addView(view);
    }


    @Override
    public void onArtistTopTrackFetcherTaskAboutToStart() {
    }

    @Override
    public void onArtistTopTrackFetcherTaskCompleted(Collection<Track> tracks) {
        //if activity has been killed then no need to attempt to populate view with tracks
        if (getActivity() != null) {
            //displays first 7 top tracks
            int counter = 0;
            for(Track track : tracks) {
                if (counter < 7) {
                    counter ++;
                    addTopTrackLink(track, mTopTracksContainer);
                }
            }
            mTracksLoadingProgressBar.setVisibility(View.GONE);
        }

    }

    private void displaySimilarArtists(Collection<Artist> artists) {
        //only populates view with similar artists if activity hasnt been killed
        if (getActivity() != null) {
            for (Artist artist : artists) {
                setUpArtistCard(artist, mSimilarArtistsContainer);
            }
        }

    }
}