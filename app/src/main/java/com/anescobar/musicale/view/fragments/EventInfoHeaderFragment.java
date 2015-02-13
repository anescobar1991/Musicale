package com.anescobar.musicale.view.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anescobar.musicale.R;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Collection;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.umass.lastfm.Event;
import de.umass.lastfm.ImageSize;

public class EventInfoHeaderFragment extends Fragment {
    private static final String ARG_EVENT = "event";

    @InjectView(R.id.event_title) TextView mEventTitleTextView;
    @InjectView(R.id.event_artists) TextView mEventArtistsTextView;
    @InjectView(R.id.event_venue_name) TextView mVenueNameTextView;
    @InjectView(R.id.event_date) TextView mEventDateTextView;
    @InjectView(R.id.event_image) ImageView mEventImageView;

    public EventInfoHeaderFragment() {
    }

    public static EventInfoHeaderFragment newInstance(Event event) {
        EventInfoHeaderFragment eventInfoHeaderFragment = new EventInfoHeaderFragment();

        final Gson gson = new Gson();

        String serializedEvent = gson.toJson(event, Event.class);
        Bundle args = new Bundle();
        args.putString(ARG_EVENT, serializedEvent);
        eventInfoHeaderFragment.setArguments(args);

        return eventInfoHeaderFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();

        View rootview = inflater.inflate(R.layout.fragment_event_info_header, container, false);
        String serializedEvent = args.getString(ARG_EVENT, null);

        final Gson gson = new Gson();

        Event event = gson.fromJson(serializedEvent, Event.class);

        ButterKnife.inject(this, rootview);

        setUpView(event);

        return rootview;
    }

    private void setUpView(Event event) {

        Collection<String> artistList = event.getArtists();

        String formattedArtists = "With ";

        //add artists to formattedArtists string
        for(String artist : artistList) {
            formattedArtists += artist + ", ";
        }
        //remove last comma from formatted artists
        formattedArtists = formattedArtists.substring(0, formattedArtists.length()-2);

        //sets eventsArtists textview to display formatted artists
        mEventArtistsTextView.setText(formattedArtists);

        String eventImageUrl = event.getImageURL(ImageSize.EXTRALARGE);
        if (eventImageUrl.length() > 0) {
            //set event image
            Picasso.with(getActivity()).load(eventImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .resize(320, 360)
                    .centerCrop()
                    .into(mEventImageView);
        } else {
            mEventImageView.setImageResource(R.drawable.placeholder);
        }

        mEventTitleTextView.setText(event.getTitle());
        mVenueNameTextView.setText("@ " + event.getVenue().getName());
        mEventDateTextView.setText(event.getStartDate().toLocaleString().substring(0, 12));
    }
}