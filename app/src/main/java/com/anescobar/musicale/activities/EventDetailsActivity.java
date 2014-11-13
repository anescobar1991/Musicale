package com.anescobar.musicale.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.anescobar.musicale.R;
import com.anescobar.musicale.adapters.EventDetailsPagerAdapter;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.TabPageIndicator;

import java.util.Collection;

import de.umass.lastfm.Event;
import de.umass.lastfm.ImageSize;

public class EventDetailsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        //gets extras that were passed into activity
        Bundle extras = getIntent().getExtras();

        Gson gson = new Gson();

        Toolbar toolbar = (Toolbar) findViewById(R.id.musicale_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //store event locally
        Event event = gson.fromJson(extras.getString("EVENT"), Event.class);

        setUpView(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //if home/up button pressed it will go back to previous fragment
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    //sets up header with event details and loads first tab with venue details
    private void setUpView(Event event) {
        ViewPager pager = (ViewPager) findViewById(R.id.activity_event_details_view_pager);

        //Set the pager with an adapter
        pager.setAdapter(new EventDetailsPagerAdapter(getSupportFragmentManager(), this, event));

        //Bind the title indicator to the adapter
        TabPageIndicator titleIndicator = (TabPageIndicator) findViewById(R.id.activity_event_details_view_pager_title);
        titleIndicator.setViewPager(pager);

        TextView eventTitleTextView = (TextView) findViewById(R.id.activity_event_details_event_title);
        TextView eventArtistsTextView = (TextView) findViewById(R.id.activity_event_details_event_artists);
        TextView venueNameTextView = (TextView) findViewById(R.id.activity_event_details_venue_name);
        TextView eventDateTextView = (TextView) findViewById(R.id.activity_event_details_event_date);
        ImageView eventImageView = (ImageView) findViewById(R.id.activity_event_details_event_image);

        Collection<String> artistList = event.getArtists();

        String formattedArtists = "With ";

        //add artists to formattedArtists string
        for(String artist : artistList) {
                formattedArtists += artist + ", ";
        }
        //remove last comma from formatted artists
        formattedArtists = formattedArtists.substring(0, formattedArtists.length()-2);

        //sets eventsArtists textview to display formatted artists
        eventArtistsTextView.setText(formattedArtists);

        //gets imageUrl
        String eventImageUrl = event.getImageURL(ImageSize.EXTRALARGE);

        // if there is an image for the event, load it into view
        if (eventImageUrl.length() > 0) {
            //set event image
            Picasso.with(this).load(eventImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .resize(320, 360)
                    .centerCrop()
                    .into(eventImageView);
        } else {
            //else load placeholder into view
            eventImageView.setImageResource(R.drawable.placeholder);
        }

        //sets all textviews to display events data
        eventTitleTextView.setText(event.getTitle());
        venueNameTextView.setText("@ " + event.getVenue().getName());
        eventDateTextView.setText(event.getStartDate().toLocaleString().substring(0, 12));
    }

}