package com.anescobar.musicale.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.umass.lastfm.Event;
import de.umass.lastfm.ImageSize;

/**
 * Created by Andres Escobar on 8/3/14.
 * Adapter for EventList view. Gets arrayList of events and populates recycleView with them
 * Also handles view logic for event cards
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
    private ArrayList<Event> mEvents;
    private static Context mContext;

    // Adapter's Constructor
    public EventsAdapter(Context context, ArrayList<Event> events) {
        mEvents = events;
        mContext = context;
    }

    // Create new views. This is invoked by the layout manager.
    @Override
    public EventsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_card, parent, false);

        return new ViewHolder(view, mEvents);
    }

    // Replace the contents of a view. This is invoked by the layout manager.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //sets event card details
        holder.mEventTitleTextView.setText(mEvents.get(position).getTitle());
        holder.mEventDateTextView.setText(mEvents.get(position).getStartDate().toLocaleString().substring(0, 12));
        holder.mEventVenueNameTextView.setText(mEvents.get(position).getVenue().getName());
        holder.mVenueLocationTextView.setText(mEvents.get(position).getVenue().getCity() + " " + mEvents.get(position).getVenue().getCountry());
        String eventUrl = mEvents.get(position).getImageURL(ImageSize.EXTRALARGE);
        if (eventUrl.length() != 0) {
            Picasso.with(mContext)
                    .load(eventUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.mEventImage);
        }
    }

    //adds events and notifies adapter that data set has changed and it should update view
    public void addEvents() {
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    // Create the ViewHolder class to keep references to your views
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ArrayList<Event> mEvents;
        public ImageView mEventImage;
        public TextView mEventTitleTextView;
        public TextView mEventDateTextView;
        public TextView mEventVenueNameTextView;
        public ImageView mMapPinImage;
        public TextView mVenueLocationTextView;
        public Button mEventBuzzButton;
        public TextView mMoreEventDetailsButton;

        /**
         * Constructor
         * @param view The container view which holds the elements from the row item xml
         */
        public ViewHolder(View view, ArrayList<Event> events) {
            super(view);
            this.mEvents = events;

            mEventImage = (ImageView) view.findViewById(R.id.eventCard_imageView_eventImage);
            mEventTitleTextView = (TextView) view.findViewById(R.id.eventCard_textView_eventTitle);
            mEventDateTextView = (TextView) view.findViewById(R.id.eventCard_textView_eventDate);
            mEventVenueNameTextView = (TextView) view.findViewById(R.id.eventCard_textView_venueName);
            mMapPinImage = (ImageView) view.findViewById(R.id.eventCard_imageView_mapPin);
            mVenueLocationTextView = (TextView) view.findViewById(R.id.eventCard_textView_venueLocation);
            mEventBuzzButton = (Button) view.findViewById(R.id.eventsCard_button_eventBuzz);
            mMoreEventDetailsButton = (Button) view.findViewById(R.id.eventsCard_button_moreDetails);

            mMapPinImage.setOnClickListener(this);
            mMoreEventDetailsButton.setOnClickListener(this);
            mEventBuzzButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == mMoreEventDetailsButton.getId()) {
                //opens event url in browser
                String eventUrl = mEvents.get(getPosition()).getUrl();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(eventUrl));
                mContext.startActivity(browserIntent);
            } else if (view.getId() == mMapPinImage.getId()) {
                //shows venue location in maps app
                Float venueLat = mEvents.get(getPosition()).getVenue().getLatitude();
                Float venueLng = mEvents.get(getPosition()).getVenue().getLongitude();
                String venueName = mEvents.get(getPosition()).getVenue().getName();
                showMap(Uri.parse("geo:0,0?q=" + venueLat +"," + venueLng + "(" + venueName + ")"));
            } else if (view.getId() == mEventBuzzButton.getId()) {
                Toast.makeText(mContext, "Buzz about this event button tapped", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(intent);
        }
    }
}