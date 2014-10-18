package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anescobar.musicale.R;
import com.anescobar.musicale.interfaces.ArtistInfoFetcherTaskListener;
import com.anescobar.musicale.utils.ArtistInfoSeeker;
import com.squareup.picasso.Picasso;

import java.util.Collection;

import de.umass.lastfm.Artist;
import de.umass.lastfm.ImageSize;

public class AboutEventArtistFragment extends Fragment implements ArtistInfoFetcherTaskListener{

    private static final String ARG_ARTIST = "artistArg";
    private AboutEventArtistFragmentInteractionListener mListener;
    private View mView;
    private Button mSimilarArtistsButton;
    private LinearLayout mSimilarArtistsContainer;

    public AboutEventArtistFragment() {}

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

    private void setUpView(final Artist artist) {
        TextView artistName = (TextView) mView.findViewById(R.id.fragment_about_artist_artist_name);
        ImageView artistImage = (ImageView) mView.findViewById(R.id.fragment_about_artist_artist_image);
        TextView artistBio = (TextView) mView.findViewById(R.id.fragment_about_artist_bio);
        TextView artistTags = (TextView) mView.findViewById(R.id.fragment_about_artist_tags);
        mSimilarArtistsButton = (Button) mView.findViewById(R.id.fragment_about_artist_show_related_artists_button);
        mSimilarArtistsContainer = (LinearLayout) mView.findViewById(R.id.fragment_about_artist_similar_artists_container);


        //-------------Sets similarArtists button on click listener----------------
        mSimilarArtistsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displaySimilarArtists(artist.getSimilar());
            }
        });

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

    private void setUpArtistCard(final Artist artist, final LinearLayout parentView) {
        LayoutInflater vi = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.artist_card, parentView, false);


        CardView artistCard = (CardView) view.findViewById(R.id.artist_card);
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
        artistCard.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(artist.getUrl()));
                startActivity(i);
            }
        });

        parentView.addView(view);
    }

    private void displaySimilarArtists(Collection<Artist> artists) {
        mSimilarArtistsButton.setVisibility(View.GONE);

        for (Artist artist : artists) {
            setUpArtistCard(artist, mSimilarArtistsContainer);
        }

        //sets other Events area visible
        mSimilarArtistsContainer.setVisibility(View.VISIBLE);
    }
}