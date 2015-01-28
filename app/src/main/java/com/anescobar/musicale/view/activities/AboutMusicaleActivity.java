package com.anescobar.musicale.view.activities;

import android.os.Bundle;

import com.anescobar.musicale.R;
import com.anescobar.musicale.view.fragments.AboutMusicaleFragment;

public class AboutMusicaleActivity extends BaseActivity {
    private static final String ABOUT_MUSICALE_FRAGMENT_TAG = "aboutMusicaleFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_musicale);

        addFragmentToActivity(R.id.container, new AboutMusicaleFragment(),ABOUT_MUSICALE_FRAGMENT_TAG);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
