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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anescobar.musicale.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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

        return new ViewHolder(view);
    }

    // Replace the contents of a view. This is invoked by the layout manager.
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //sets event card details
        holder.mEventTitleTextView.setText(mEvents.get(position).getTitle());
        //gets event date as Date object but only needs MMDDYYYY, not the timestamp
        holder.mEventDateTextView.setText(mEvents.get(position).getStartDate().toLocaleString().substring(0, 12));
        holder.mEventVenueNameTextView.setText(mEvents.get(position).getVenue().getName());
        holder.mVenueLocationTextView.setText(mEvents.get(position).getVenue().getCity() + " " + mEvents.get(position).getVenue().getCountry());
        String eventImageUrl = mEvents.get(position).getImageURL(ImageSize.EXTRALARGE);
        // if there is an image for the event load it into view. Else load placeholder into view
        if (eventImageUrl.length() > 0) {
            Picasso.with(mContext)
                    .load(eventImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.mEventImage);
        } else {
            holder.mEventImage.setImageResource(R.drawable.placeholder);
        }
        //sets onClickListener for moreDetails button
        holder.mMoreDetailsButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //opens event url in browser for now
                //TODO send intent to eventsDetails activity when that screen is completed
                String eventUrl = mEvents.get(position).getUrl();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(eventUrl));
                mContext.startActivity(browserIntent);
            }
        });
        //sets onClickListener for view in map button
        holder.mViewInMapButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //shows venue location in maps app for now
                //TODO link to map view if possible
                Float venueLat = mEvents.get(position).getVenue().getLatitude();
                Float venueLng = mEvents.get(position).getVenue().getLongitude();
                String venueName = mEvents.get(position).getVenue().getName();
                showMap(Uri.parse("geo:0,0?q=" + venueLat +"," + venueLng + "(" + venueName + ")"));
            }
        });
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    // Create the ViewHolder class to keep references to your views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mEventImage;
        public TextView mEventTitleTextView;
        public TextView mEventDateTextView;
        public TextView mEventVenueNameTextView;
        public TextView mVenueLocationTextView;
        public Button mViewInMapButton;
        public Button mMoreDetailsButton;

        /**
         * Constructor
         * @param view The container view which holds the elements from the row item xml
         */
        public ViewHolder(View view) {
            super(view);

            mEventImage = (ImageView) view.findViewById(R.id.event_card_event_image);
            mEventTitleTextView = (TextView) view.findViewById(R.id.event_card_event_title_textfield);
            mEventDateTextView = (TextView) view.findViewById(R.id.event_Card_event_date_textfield);
            mEventVenueNameTextView = (TextView) view.findViewById(R.id.event_card_venue_name_textfield);
            mVenueLocationTextView = (TextView) view.findViewById(R.id.event_card_venue_location_textfield);
            mViewInMapButton = (Button) view.findViewById(R.id.event_card_show_in_map_button);
            mMoreDetailsButton = (Button) view.findViewById(R.id.event_card_more_details_button);
        }

    }

    //opens Maps app with URI parameters
    private void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(intent);
        }
    }
}