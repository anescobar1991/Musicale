package com.anescobar.musicale.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.AboutArtistFragment;


public class ArtistDetailsActivity extends ActionBarActivity {

    public static final String ABOUT_ARTIST_FRAGMENT = "aboutArtistFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_artist);

        //gets extras that were passed into activity
        Bundle extras = getIntent().getExtras();

        Toolbar toolbar = (Toolbar) findViewById(R.id.musicale_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

}