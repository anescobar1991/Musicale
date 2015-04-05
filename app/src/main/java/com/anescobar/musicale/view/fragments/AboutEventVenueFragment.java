package com.anescobar.musicale.view.fragments;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.services.LastFmServiceProvider;
import com.anescobar.musicale.app.services.interfaces.VenueEventsFetcherListener;
import com.anescobar.musicale.app.models.VenueDetails;
import com.anescobar.musicale.app.services.exceptions.NetworkNotAvailableException;
import com.anescobar.musicale.view.activities.EventDetailsActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Collection;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.umass.lastfm.Event;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Venue;

public class AboutEventVenueFragment extends BaseFragment implements VenueEventsFetcherListener {

    private static final String ARG_VENUE = "venueArg";
    private LastFmServiceProvider mLastFmServiceProvider;
    private CachedVenueDetailsGetterSetter mCachedVenueDetailsGetterSetter;
    private VenueDetails mVenueDetails = new VenueDetails();

    @InjectView(R.id.about_venue_progressbar) ProgressBar mLoadingProgressbar;
    @InjectView(R.id.about_venue_content) LinearLayout mAboutVenueContainer;
    @InjectView(R.id.about_venue_message_container) TextView mErrorMessageContainer;
    @InjectView(R.id.venue_other_events_container) LinearLayout mOtherEventsContainer;
    @InjectView(R.id.venue_upcoming_events_card) CardView mUpcomingEventsCard;
    @InjectView(R.id.venue_name) TextView mVenueName;
    @InjectView(R.id.venue_phone_number) TextView mVenuePhoneNumberTextView;
    @InjectView(R.id.venue_url) TextView mVenueUrlTextView;
    @InjectView(R.id.venue_address) TextView mVenueAddress;
    @InjectView(R.id.venue_image) ImageView mVenueImage;


    public AboutEventVenueFragment() {}

    public interface CachedVenueDetailsGetterSetter {
        VenueDetails getVenueDetails();
        void setVenueDetails(VenueDetails venueDetails);
    }

    public static AboutEventVenueFragment newInstance(Venue venue) {
        AboutEventVenueFragment fragment = new AboutEventVenueFragment();

        Gson gson = new Gson();

        String serializedVenue = gson.toJson(venue, Venue.class);
        Bundle args = new Bundle();

        args.putString(ARG_VENUE, serializedVenue);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCachedVenueDetailsGetterSetter = (CachedVenueDetailsGetterSetter) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CachedVenueDetailsGetterSetter");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCachedVenueDetailsGetterSetter = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_about_venue, container, false);

        ButterKnife.inject(this, view);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mLastFmServiceProvider = new LastFmServiceProvider(getActivity().getApplicationContext());

        Gson gson = new Gson();

        String serializedEvent = getArguments().getString(ARG_VENUE, null);
        mVenueDetails.venue = gson.fromJson(serializedEvent, Venue.class);

        setUpView(mVenueDetails.venue);
    }

    @Override
    public void onPause() {
        mCachedVenueDetailsGetterSetter.setVenueDetails(mVenueDetails);
        super.onPause();
    }

    //sets up map if it hasnt already been setup,
    private void setUpMapIfNeeded(final Venue venue) {
        SupportMapFragment mapFragment;

        final LatLng venueLocation = new LatLng(venue.getLatitude(), venue.getLongitude());
        GoogleMapOptions options = new GoogleMapOptions();

        options.camera(CameraPosition.fromLatLngZoom(venueLocation, 14));
        options.liteMode(true);

        mapFragment = SupportMapFragment.newInstance(options);

        //displays map fragment on screen
        android.support.v4.app.FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.venue_map_container, mapFragment)
                .commit();

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                //adds marker for venue location
                map.addMarker(new MarkerOptions()
                                .position(venueLocation)
                );
            }
        });
    }

    private void setUpView(final Venue venue) {
        if (mCachedVenueDetailsGetterSetter.getVenueDetails().upcomingEvents != null) {
            populateOtherEventsContainer(mCachedVenueDetailsGetterSetter.getVenueDetails().upcomingEvents);
        } else {
            getVenueEvents(mVenueDetails.venue.getId());
        }

        if (venue.getImageURL(ImageSize.EXTRALARGE).length() > 0) {
            Picasso.with(getActivity()).load(venue.getImageURL(ImageSize.EXTRALARGE))
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .resize(360, 360)
                    .into(mVenueImage);
        } else {
            mVenueImage.setImageResource(R.drawable.placeholder);
        }

        if (venue.getWebsite().length() == 0) {
            mVenueUrlTextView.setVisibility(View.GONE);
        } else {
            mVenueUrlTextView.setText(venue.getWebsite());
        }

        if (venue.getPhonenumber().length() == 0) {
            mVenuePhoneNumberTextView.setVisibility(View.GONE);
        } else {
            mVenuePhoneNumberTextView.setText(venue.getPhonenumber());
        }

        mVenueName.setText(venue.getName());

        if (venue.getStreet().length() == 0) {
            mVenueAddress.setText(venue.getCity() + getString(R.string.event_address_separator) + venue.getCountry());
        } else if (venue.getCity().length() == 0) {
            mVenueAddress.setText(venue.getStreet() + getString(R.string.event_address_separator) + venue.getCountry());
        } else if (venue.getCountry().length() == 0) {
            mVenueAddress.setText(venue.getStreet() + getString(R.string.event_address_separator) + venue.getCity());
        } else {
            mVenueAddress.setText(venue.getStreet() + getString(R.string.event_address_separator) + venue.getCity() + getString(R.string.event_address_separator) + venue.getCountry());
        }

        setUpMapIfNeeded(mVenueDetails.venue);
    }

    private void setUpEventCard(final Event event, final ViewGroup parentView) {
        LayoutInflater vi = LayoutInflater.from(getActivity());
        View view = vi.inflate(R.layout.event_card, parentView, false);

        RelativeLayout eventCard = (RelativeLayout) view.findViewById(R.id.event_card);
        ImageView eventImage = (ImageView) view.findViewById(R.id.event_image);
        TextView eventTitleTextView = (TextView) view.findViewById(R.id.event_name);
        TextView eventDateTextView = (TextView) view.findViewById(R.id.event_date);
        TextView eventVenueNameTextView = (TextView) view.findViewById(R.id.event_venue_name);
        TextView venueLocationTextView = (TextView) view.findViewById(R.id.event_venue_location);

        eventTitleTextView.setText(event.getTitle());
        //gets event date as Date object but only needs MMDDYYYY, not the timestamp
        eventDateTextView.setText(event.getStartDate().toLocaleString().substring(0, 12));
        eventVenueNameTextView.setText("@ " + event.getVenue().getName());
        venueLocationTextView.setText(event.getVenue().getCity() + " " + event.getVenue().getCountry());

        String eventImageUrl = event.getImageURL(ImageSize.EXTRALARGE);
        if (eventImageUrl.length() > 0) {
            Picasso.with(getActivity())
                    .load(eventImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(eventImage);
        } else {
            eventImage.setImageResource(R.drawable.placeholder);
        }

        eventCard.setOnClickListener(new RelativeLayout.OnClickListener() {
            public void onClick(View v) {
                Gson gson = new Gson();

                String serializedEvent = gson.toJson(event, Event.class);

                Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
                intent.putExtra("EVENT", serializedEvent);
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getActivity().getApplicationContext(), R.anim.slide_in_right, R.anim.slide_out_left);

                getActivity().startActivity(intent, activityOptions.toBundle());
            }
        });

        parentView.addView(view);
    }

    @Override
    public void onVenueEventsFetcherTaskAboutToStart() {}

    @Override
    public void onVenueEventsFetcherTaskCompleted(Collection<Event> events) {
        mVenueDetails.upcomingEvents = events;
        populateOtherEventsContainer(events);
    }

    private void populateOtherEventsContainer(Collection<Event> events) {
        //if activity is null (b/c user navigated away from screen) then shouldnt load events to screen
        if (getActivity() != null) {
            if(!events.isEmpty()) {
                for (Event event : events) {
                    setUpEventCard(event, mOtherEventsContainer);
                }
            } else {
                mUpcomingEventsCard.setVisibility(View.GONE);
            }

            mLoadingProgressbar.setVisibility(View.GONE);

            mAboutVenueContainer.setVisibility(View.VISIBLE);
        }
    }

    private void getVenueEvents(String venueId) {
        try {
            mLastFmServiceProvider.getUpcomingEventsAtVenue(venueId, this, getActivity().getApplicationContext());
        } catch (NetworkNotAvailableException e) {
            e.printStackTrace();

            displayErrorMessage(getString(R.string.error_no_network_connectivity));
        }
    }

    private void displayErrorMessage(String message) {
        mLoadingProgressbar.setVisibility(View.GONE);

        mErrorMessageContainer.setText(message);
        mErrorMessageContainer.setVisibility(View.VISIBLE);
    }

}