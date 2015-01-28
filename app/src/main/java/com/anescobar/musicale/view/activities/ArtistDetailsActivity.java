package com.anescobar.musicale.view.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.models.ArtistDetails;
import com.anescobar.musicale.view.fragments.AboutArtistFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ArtistDetailsActivity extends BaseActivity implements AboutArtistFragment.CachedArtistDetailsGetterSetter {

    public static final String ABOUT_ARTIST_FRAGMENT = "aboutArtistFragment";
    private ArtistDetails mArtistDetails = new ArtistDetails();

    @InjectView(R.id.musicale_toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_artist);

        ButterKnife.inject(this);

        //gets extras that were passed into activity
        Bundle extras = getIntent().getExtras();

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //get passed Event from extras if there are any
        if (extras != null) {

        //add aboutArtist fragment to activity
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.about_artist_container, AboutArtistFragment.newInstance(extras.getString("ARTIST")), ABOUT_ARTIST_FRAGMENT)
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
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public ArtistDetails getArtistDetails() {
        return mArtistDetails;
    }

    @Override
    public void setArtistDetails(ArtistDetails artistDetails) {
        mArtistDetails = artistDetails;
    }
}