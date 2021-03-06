package com.anescobar.musicale.app.adapters;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anescobar.musicale.R;
import com.anescobar.musicale.view.activities.EventDetailsActivity;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.umass.lastfm.Event;
import de.umass.lastfm.ImageSize;

/**
 * Created by Andres Escobar on 8/3/14.
 * Adapter for EventList view. Gets arrayList of events and populates events cards that are placed into
 * recycleView
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
    private ArrayList<Event> mEvents;
    private Context mContext;

    public EventsAdapter(Context context, ArrayList<Event> events) {
        mEvents = events;
        mContext = context;
    }

    @Override
    public EventsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mEventTitleTextView.setText(mEvents.get(position).getTitle());
        //gets event date as Date object but only needs MMDDYYYY, not the timestamp
        holder.mEventDateTextView.setText(mEvents.get(position).getStartDate().toLocaleString().substring(0, 12));
        holder.mEventVenueNameTextView.setText("@ " + mEvents.get(position).getVenue().getName());
        holder.mVenueLocationTextView.setText(mEvents.get(position).getVenue().getCity() + " " + mEvents.get(position).getVenue().getCountry());

        String eventImageUrl = mEvents.get(position).getImageURL(ImageSize.EXTRALARGE);
        if (eventImageUrl.length() > 0) {
            Picasso.with(mContext)
                    .load(eventImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.mEventImage);
        } else {
            holder.mEventImage.setImageResource(R.drawable.placeholder);
        }

        holder.mEventCard.setOnClickListener(new RelativeLayout.OnClickListener() {
            public void onClick(View v) {
                Gson gson = new Gson();

                String serializedEvent = gson.toJson(mEvents.get(position), Event.class);

                Intent intent = new Intent(mContext, EventDetailsActivity.class);
                intent.putExtra("EVENT", serializedEvent);
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(mContext, R.anim.slide_in_right, R.anim.slide_out_left);

                mContext.startActivity(intent, activityOptions.toBundle());
            }
        });
    }


    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.event_card) RelativeLayout mEventCard;
        @InjectView(R.id.event_image) ImageView mEventImage;
        @InjectView(R.id.event_name) TextView mEventTitleTextView;
        @InjectView(R.id.event_date) TextView mEventDateTextView;
        @InjectView(R.id.event_venue_name) TextView mEventVenueNameTextView;
        @InjectView(R.id.event_venue_location) TextView mVenueLocationTextView;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

}