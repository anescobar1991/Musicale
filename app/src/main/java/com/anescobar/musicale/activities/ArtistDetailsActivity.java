package com.anescobar.musicale.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.AboutArtistFragment;


public class ArtistDetailsActivity extends FragmentActivity implements
        AboutArtistFragment.AboutEventArtistFragmentInteractionListener {

    public static final String ABOUT_ARTIST_FRAGMENT = "aboutArtistFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_artist);

        //show home/up button on actionbar
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //gets extras that were passed into activity
        Bundle extras = getIntent().getExtras();

        //get passed Event from extras if there are any
        if (extras != null) {

        //store event locally
        String artist = extras.getString("ARTIST");

        //add aboutArtist fragment to activity
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.activity_about_artist_container, AboutArtistFragment.newInstance(artist), ABOUT_ARTIST_FRAGMENT)
                .commit();

        //should never ever be null, but who knows?
        } else {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
        }
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

    @Override
    public void displayErrorMessage(String message) {
        TextView errorMessageContainer = (TextView) findViewById(R.id.activity_about_artist_error_message_container);

        //sets error message container to given message and makes it visible
        errorMessageContainer.setText(message);
        errorMessageContainer.setVisibility(View.VISIBLE);
    }
}