package com.anescobar.musicale.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Collection;

import de.umass.lastfm.Event;
import de.umass.lastfm.ImageSize;

public class EventDetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        //show home/up button on actionbar
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        //get passed Event from extras
        if (extras != null) {
            Gson gson = new Gson();

            //store event locally
            Event event = gson.fromJson(extras.getString("EVENT"), Event.class);

            setUpView(event);
        //should never ever be null, but who knows?
        } else {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.event_details, menu);
        return true;
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

    //sets up header with event details
    private void setUpView(Event event) {
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

        // if there is an image for the event, load it into view. Else load placeholder into view
        if (eventImageUrl.length() > 0) {
            //set event image
            Picasso.with(this).load(eventImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .resize(320, 360)
                    .centerCrop()
                    .into(eventImageView);
        } else {
            eventImageView.setImageResource(R.drawable.placeholder);
        }

        //sets all textviews to display events data
        eventTitleTextView.setText(event.getTitle());
        venueNameTextView.setText("@ " + event.getVenue().getName());
        eventDateTextView.setText(event.getStartDate().toLocaleString().substring(0, 12));
    }
}
