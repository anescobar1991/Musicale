package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anescobar.musicale.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Venue;

public class AboutEventVenueFragment extends Fragment {
    private AboutEventVenueFragmentInteractionListener mListener;
    private static final String ARG_EVENT = "eventArg";
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private Venue mVenue;

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

    private void setUpView(final Venue venue, View view) {
        TextView venueName = (TextView) view.findViewById(R.id.fragment_about_venue_venue_name);
        TextView venuePhoneNumberTextView = (TextView) view.findViewById(R.id.fragment_about_venue_phone_number);
        TextView venueUrlTextView = (TextView) view.findViewById(R.id.fragment_about_venue_url);
        TextView venueAddress = (TextView) view.findViewById(R.id.fragment_about_venue_address);
        ImageView venueImage = (ImageView) view.findViewById(R.id.fragment_about_venue_image);
        Button showOtherEventsButton = (Button) view.findViewById(R.id.fragment_about_venue_venue_show_other_events);
        RelativeLayout venueUrlContainer = (RelativeLayout) view.findViewById(R.id.fragment_about_venue_url_container);
        RelativeLayout venuePhoneNumberContainer = (RelativeLayout) view.findViewById(R.id.fragment_about_venue_phone_number_container);

        String venuePhoneNumber = venue.getPhonenumber();
        String venueUrl = venue.getWebsite();
        String venueImageUrl = venue.getImageURL(ImageSize.EXTRALARGE);

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

}