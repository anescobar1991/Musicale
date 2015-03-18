package com.anescobar.musicale.view.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

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

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        addFragmentToActivity(R.id.about_artist_container, AboutArtistFragment.newInstance(getIntent().getExtras().getString("ARTIST")), ABOUT_ARTIST_FRAGMENT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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