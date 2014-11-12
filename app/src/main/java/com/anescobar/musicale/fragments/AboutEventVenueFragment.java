package com.anescobar.musicale.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anescobar.musicale.R;
import com.anescobar.musicale.activities.EventDetailsActivity;
import com.anescobar.musicale.interfaces.VenueEventsFetcherListener;
import com.anescobar.musicale.utils.EventsFinder;
import com.anescobar.musicale.utils.NetworkNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Collection;

import de.umass.lastfm.Event;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Venue;

public class AboutEventVenueFragment extends Fragment implements VenueEventsFetcherListener {

    private static final String ARG_EVENT = "eventArg";
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private Venue mVenue;
    private LinearLayout mOtherEventsContainer;
    private ProgressBar mLoadingProgressbar;
    private TextView mErrorMessageContainer;
    private EventsFinder mEventsFinder;
    private RelativeLayout mAboutVenueContainer;

    public AboutEventVenueFragment() {
        // Required empty public constructor
    }

    public static AboutEventVenueFragment newInstance(Venue venue) {
        AboutEventVenueFragment fragment = new AboutEventVenueFragment();

        //creates new instance of Gson
        Gson gson = new Gson();

        //serializes event into string using Gson
        String serializedEvent = gson.toJson(venue, Venue.class);

        //creates new Bundle
        Bundle args = new Bundle();

        //adds serialized event to bundle
        args.putString(ARG_EVENT, serializedEvent);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_about_venue, container, false);

        Bundle args = getArguments();

        String serializedEvent = args.getString(ARG_EVENT, null);

        //gets new instance of EventsFinder
        mEventsFinder = new EventsFinder();

        //gets MapFragment
        mMapFragment = new SupportMapFragment();

        //displays map fragment on screen
        android.support.v4.app.FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_about_venue_map_container, mMapFragment)
                .commit();

        // if there is a serialized event in bundle
        Gson gson = new Gson();

        //deserializes event using Gson
        Venue venue = gson.fromJson(serializedEvent, Venue.class);

        //sets venueLatLng field
        mVenue = venue;

        //sets up view
        setUpView(venue, view);

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        //sets up map, with its settings, and adds event markers
        setUpMapIfNeeded(mVenue);
    }

    @Override
    public void onResume(){
        super.onResume();
        //sets up map, with its settings, and adds event markers
        setUpMapIfNeeded(mVenue);
    }

    //sets up map if it hasnt already been setup,
    private void setUpMapIfNeeded(final Venue venue) {
        LatLng venueLocation = new LatLng(venue.getLatitude(), venue.getLongitude());

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = mMapFragment.getMap();
        }

        //sets map's initial state
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(venueLocation, 14));
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(false);

        //disables user interaction
        mMap.getUiSettings().setAllGesturesEnabled(false);

        //adds marker for venue location
        mMap.addMarker(new MarkerOptions()
                        .position(venueLocation)
        );

        //sets click listener for when user taps anywhere in map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            //create dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Add the buttons
            builder.setPositiveButton(R.string.open_in_map, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    showVenueInMap(venue.getLatitude(), venue.getLongitude(), venue.getName());
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });

            // Set dialog's message
            builder.setMessage(R.string.open_in_map_question);
            // Create the AlertDialog
            builder.show();
            }
        });
    }

    private void setUpView(final Venue venue, final View view) {
        mOtherEventsContainer = (LinearLayout) view.findViewById(R.id.fragment_about_venue_other_events_container);
        mLoadingProgressbar = (ProgressBar) view.findViewById(R.id.fragment_about_venue_loading_progressbar);
        mAboutVenueContainer = (RelativeLayout) view.findViewById(R.id.fragment_about_venue_content);
        mErrorMessageContainer = (TextView) view.findViewById(R.id.fragment_about_venue_error_message_container);

        TextView venueName = (TextView) view.findViewById(R.id.fragment_about_venue_venue_name);
        TextView venuePhoneNumberTextView = (TextView) view.findViewById(R.id.fragment_about_venue_phone_number);
        TextView venueUrlTextView = (TextView) view.findViewById(R.id.fragment_about_venue_url);
        TextView venueAddress = (TextView) view.findViewById(R.id.fragment_about_venue_address);
        ImageView venueImage = (ImageView) view.findViewById(R.id.fragment_about_venue_image);
        RelativeLayout venueUrlContainer = (RelativeLayout) view.findViewById(R.id.fragment_about_venue_url_container);
        RelativeLayout venuePhoneNumberContainer = (RelativeLayout) view.findViewById(R.id.fragment_about_venue_phone_number_container);


        String venuePhoneNumber = venue.getPhonenumber();
        String venueUrl = venue.getWebsite();
        String venueImageUrl = venue.getImageURL(ImageSize.EXTRALARGE);

        //gets venue Events from backend
        getVenueEvents(mVenue.getId());

        //-------------loads all dynamic data into view-----------------

        //downloads venue image into view if there is image
        if (venueImageUrl.length() > 0) {
            Picasso.with(getActivity()).load(venueImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .resize(360, 360)
                    .into(venueImage);
            //else will load placeholder image into view
        } else {
            venueImage.setVisibility(View.GONE);
        }

        //hide venueUrl textview and image if there is no venue Url available
        if (venueUrl.length() == 0) {
            venueUrlContainer.setVisibility(View.GONE);
        } else {
            //display venue Url
            venueUrlTextView.setText(venueUrl);
        }

        //hide venue phone number textview and image if there is no venue phone number available
        if (venuePhoneNumber.length() == 0) {
            venuePhoneNumberContainer.setVisibility(View.GONE);
        } else {
            //display venue phone number
            venuePhoneNumberTextView.setText(venuePhoneNumber);
        }

        venueName.setText(venue.getName());

        venueAddress.setText(venue.getStreet() + " • " + venue.getCity() + " • " + venue.getCountry());
    }

    //sends intent to open Google maps application at specified location with specified label
    private void showVenueInMap(Float venueLat, Float venueLng, String venueName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q=" + venueLat + "," + venueLng + "(" + venueName + ")"));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivity(intent);
        }
    }

    private void setUpEventCard(final Event event, final LinearLayout parentView) {
        LayoutInflater vi = LayoutInflater.from(getActivity());
        View view = vi.inflate(R.layout.event_card, parentView, false);

        RelativeLayout eventCard = (RelativeLayout) view.findViewById(R.id.event_card);
        ImageView eventImage = (ImageView) view.findViewById(R.id.event_card_event_image);
        TextView eventTitleTextView = (TextView) view.findViewById(R.id.event_card_event_title_textfield);
        TextView eventDateTextView = (TextView) view.findViewById(R.id.event_card_event_date_textfield);
        TextView eventVenueNameTextView = (TextView) view.findViewById(R.id.event_card_venue_name_textfield);
        TextView venueLocationTextView = (TextView) view.findViewById(R.id.event_card_venue_location_textfield);

        //sets event card details
        eventTitleTextView.setText(event.getTitle());
        //gets event date as Date object but only needs MMDDYYYY, not the timestamp
        eventDateTextView.setText(event.getStartDate().toLocaleString().substring(0, 12));
        eventVenueNameTextView.setText("@ " + event.getVenue().getName());
        venueLocationTextView.setText(event.getVenue().getCity() + " " + event.getVenue().getCountry());
        String eventImageUrl = event.getImageURL(ImageSize.EXTRALARGE);
        // if there is an image for the event load it into view. Else load placeholder into view
        if (eventImageUrl.length() > 0) {
            Picasso.with(getActivity())
                    .load(eventImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(eventImage);
        } else {
            eventImage.setImageResource(R.drawable.placeholder);
        }

        //sets onClickListener for event card button
        eventCard.setOnClickListener(new RelativeLayout.OnClickListener() {
            public void onClick(View v) {
                Gson gson = new Gson();

                //serialize event using GSON
                String serializedEvent = gson.toJson(event, Event.class);

                //starts EventDetailsActivity
                Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
                intent.putExtra("EVENT", serializedEvent);
                getActivity().startActivity(intent);
            }
        });

        parentView.addView(view);
    }

    @Override
    public void onVenueEventsFetcherTaskAboutToStart() {
    }

    @Override
    public void onVenueEventsFetcherTaskCompleted(Collection<Event> events) {
        //if activity is null(b/c user navigated away from screen) then shouldnt load events to screen
        if (getActivity() != null) {
            mLoadingProgressbar.setVisibility(View.GONE);

            for (Event event : events) {
                setUpEventCard(event, mOtherEventsContainer);
            }

            //sets content area visible
            mAboutVenueContainer.setVisibility(View.VISIBLE);
        }
    }

    private void getVenueEvents(String venueId) {
        try {
            mEventsFinder.getUpcomingEventsAtVenue(venueId, this, getActivity());
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