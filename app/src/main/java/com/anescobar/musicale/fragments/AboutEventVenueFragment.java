package com.anescobar.musicale.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.anescobar.musicale.R;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Venue;

public class AboutEventVenueFragment extends Fragment {
    private OnAboutEventVenueFragmentInteractionListener mListener;
    private static final String ARG_EVENT = "eventArg";

    public AboutEventVenueFragment() {
        // Required empty public constructor
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnAboutEventVenueFragmentInteractionListener {
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
        args.putString(ARG_EVENT ,serializedEvent);
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

        // if there is a serialized event in bundle
        if (serializedEvent != null) {
            Gson gson = new Gson();

            //deserializes event using Gson
            Venue venue = gson.fromJson(serializedEvent, Venue.class);

            //sets up view
            setUpView(venue, view);

        // should never happen but just in case...
        } else {
            mListener.displayErrorMessage(getActivity().getString(R.string.error_generic));
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnAboutEventVenueFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnAboutEventVenueFragmentInteractionListener");
        }
    }

    private void setUpView(Venue venue, View view) {
        TextView venueName = (TextView) view.findViewById(R.id.fragment_about_venue_venue_name);
        TextView streetAddress = (TextView) view.findViewById(R.id.fragment_about_venue_street);
        TextView cityPostalCountry = (TextView) view.findViewById(R.id.fragment_about_venue_city_postal_country);
        ImageView venueImage = (ImageView) view.findViewById(R.id.fragment_about_venue_image);
        Button showInMapButton = (Button) view.findViewById(R.id.fragment_about_venue_venue_show_in_map);
        Button showOtherEvents = (Button) view.findViewById(R.id.fragment_about_venue_venue_show_other_events);

        //loads all dynamic data into view
        venueName.setText(venue.getName());
        streetAddress.setText(venue.getStreet());
        cityPostalCountry.setText(venue.getCity() + " " + venue.getPostal() + " " + venue.getCountry());

        System.out.println(venue.getImageURL(ImageSize.EXTRALARGE));

        String venueImageUrl = venue.getImageURL(ImageSize.EXTRALARGE);

        //downloads venue image into view if there is image
        if (venueImageUrl.length() > 0) {
            Picasso.with(getActivity()).load(venueImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .resize(720, 360)
                    .into(venueImage);
         //else will load placeholder image into view
        } else {
            venueImage.setImageResource(R.drawable.placeholder);
        }
    }
}