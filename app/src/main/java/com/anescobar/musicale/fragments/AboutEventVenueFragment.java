package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.activities.EventDetailsActivity;
import com.anescobar.musicale.interfaces.VenueEventsFetcherListener;
import com.anescobar.musicale.utils.EventsFinder;
import com.anescobar.musicale.utils.NetworkUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Collection;

import de.umass.lastfm.Event;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Venue;

public class AboutEventVenueFragment extends Fragment implements VenueEventsFetcherListener {

    private AboutEventVenueFragmentInteractionListener mListener;
    private static final String ARG_EVENT = "eventArg";
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private Venue mVenue;
    private NetworkUtil mNetworkUtil;
    private LinearLayout mOtherEventsContainer;
    private ProgressBar mLoadingProgressbar;
    private EventsFinder mEventsFinder;
    private Button mShowOtherEventsButton;

    public AboutEventVenueFragment() {
        // Required empty public constructor
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface AboutEventVenueFragmentInteractionListener {
        public void displayErrorMessage(String message);
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

        //instantiates new networkUtil
        mNetworkUtil = new NetworkUtil();

        //gets MapFragment
        mMapFragment = new SupportMapFragment();

        //displays map fragment on screen
        android.support.v4.app.FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_about_venue_map_container, mMapFragment)
                .commit();

        // if there is a serialized event in bundle
        if (serializedEvent != null) {
            Gson gson = new Gson();

            //deserializes event using Gson
            Venue venue = gson.fromJson(serializedEvent, Venue.class);

            //sets venueLatLng field
            mVenue = venue;

            //sets up view
            setUpView(venue, view);

        // should never happen but just in case...
        } else {
            mListener.displayErrorMessage(getActivity().getString(R.string.error_generic));
        }

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
        mMap.setBuildingsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(venueLocation, 14));

        //adds marker for venue location
        mMap.addMarker(new MarkerOptions()
                        .position(venueLocation)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.music_live))
        );
        //disables user interaction
        mMap.getUiSettings().setAllGesturesEnabled(false);

        //sets click listener for when user taps anywhere in map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                showVenueInMap(venue.getLatitude(), venue.getLongitude(), venue.getName());
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AboutEventVenueFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement AboutEventVenueFragmentInteractionListener");
        }
    }

    private void setUpView(final Venue venue, final View view) {
        mOtherEventsContainer = (LinearLayout) view.findViewById(R.id.fragment_about_venue_other_events_container);
        mLoadingProgressbar = (ProgressBar) view.findViewById(R.id.fragment_about_venue_other_events_loading);
        mShowOtherEventsButton = (Button) view.findViewById(R.id.fragment_about_venue_venue_show_other_events);
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

        //sets on clickListener for view other events at venue button
        mShowOtherEventsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //displays other events section
                displayOtherEventsAtVenue(view);
            }
        });

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

    private void displayOtherEventsAtVenue(View view) {
        if (mNetworkUtil.isNetworkAvailable(getActivity())) {
            LinearLayout otherEventsContainer = (LinearLayout) view.findViewById(R.id.fragment_about_venue_other_events_container);

            //hide show other events button
            mShowOtherEventsButton.setVisibility(View.GONE);

            //sets other Events area visible
            otherEventsContainer.setVisibility(View.VISIBLE);

            //gets venue Events from backend
            getVenueEvents(mVenue.getId());
        } else {
            Toast.makeText(getActivity(), R.string.error_no_network_connectivity, Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpEventCard(final Event event, final LinearLayout parentView) {
        LayoutInflater vi = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.event_card, parentView, false);

        ImageView eventImage = (ImageView) view.findViewById(R.id.event_card_event_image);
        TextView eventTitleTextView = (TextView) view.findViewById(R.id.event_card_event_title_textfield);
        TextView eventDateTextView = (TextView) view.findViewById(R.id.event_card_event_date_textfield);
        TextView eventVenueNameTextView = (TextView) view.findViewById(R.id.event_card_venue_name_textfield);
        TextView venueLocationTextView = (TextView) view.findViewById(R.id.event_card_venue_location_textfield);
        Button viewInMapButton = (Button) view.findViewById(R.id.event_card_show_in_map_button);
        Button moreDetailsButton = (Button) view.findViewById(R.id.event_card_more_details_button);

        //hides view in map button as it is not necessary in this case
        viewInMapButton.setVisibility(View.GONE);

        //padding padding must differ from default to account for view in map button not being displayed
        moreDetailsButton.setPadding(0, 0, 0, 0);

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

        //sets onClickListener for moreDetails button
        moreDetailsButton.setOnClickListener(new Button.OnClickListener() {
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
        mLoadingProgressbar.setVisibility(View.GONE);

        for (Event event : events) {
            setUpEventCard(event, mOtherEventsContainer);
        }
    }

    private void getVenueEvents(String venueId) {
        mEventsFinder.getUpcomingEventsAtVenue(venueId, this);
    }

}