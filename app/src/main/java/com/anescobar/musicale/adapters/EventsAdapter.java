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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

import de.umass.lastfm.Event;

/**
 * Created by Andres Escobar on 8/3/14.
 * Adapter for EventList view. Gets arrayList of events and populates recycleView with them
 * Also handles view logic for event cards
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> implements View.OnClickListener {
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
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_card, parent, false);

        // Set the view to the ViewHolder
        ViewHolder holder = new ViewHolder(v);
        holder.mMapPinImage.setOnClickListener(EventsAdapter.this);
        holder.mEventBuzzButton.setOnClickListener(EventsAdapter.this);
        holder.mMoreEventDetailsButton.setOnClickListener(EventsAdapter.this);

        holder.mEventBuzzButton.setTag(holder);
        holder.mMapPinImage.setTag(holder);
        holder.mMoreEventDetailsButton.setTag(holder);

        return holder;
    }

    // Replace the contents of a view. This is invoked by the layout manager.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //TODO populate events cards here

        // Get element from your dataset at this position and set the text for the specified element
        holder.mEventTitleTextView.setText(mEvents.get(position).getTitle());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    @Override
    public void onClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (view.getId() == holder.mMoreEventDetailsButton.getId()) {
            Toast.makeText(mContext, holder.mMoreEventDetailsButton.getText(), Toast.LENGTH_SHORT).show();
            //TODO hardcoded for now
        } else if (view.getId() == holder.mMapPinImage.getId()) {
            showMap(Uri.parse("geo:0,0?q=34.99,-106.61(Cynthia Woods Mitchell Pavilion)"));
        } else if (view.getId() == holder.mEventBuzzButton.getId()) {
            Toast.makeText(mContext, "Buzz about this event button tapped", Toast.LENGTH_SHORT).show();
        }
    }

    // Create the ViewHolder class to keep references to your views
    public static class ViewHolder extends RecyclerView.ViewHolder {
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
        public ViewHolder(View view) {
            super(view);
            mEventImage = (ImageView) view.findViewById(R.id.eventCard_imageView_eventImage);
            mEventTitleTextView = (TextView) view.findViewById(R.id.eventCard_textView_eventTitle);
            mEventDateTextView = (TextView) view.findViewById(R.id.eventCard_textView_eventDate);
            mEventVenueNameTextView = (TextView) view.findViewById(R.id.eventCard_textView_venueName);
            mMapPinImage = (ImageView) view.findViewById(R.id.eventCard_imageView_mapPin);
            mVenueLocationTextView = (TextView) view.findViewById(R.id.eventCard_textView_venueLocation);
            mEventBuzzButton = (Button) view.findViewById(R.id.eventsCard_button_eventBuzz);
            mMoreEventDetailsButton = (Button) view.findViewById(R.id.eventsCard_button_moreDetails);
        }
    }

    private void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(intent);
        }
    }
}