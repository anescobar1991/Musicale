package com.anescobar.musicale.activities;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.AboutMusicaleFragment;

public class HomeActivity extends Activity
        implements AboutMusicaleFragment.OnAboutMusicaleFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

    }

    public void showNearbyEvents(View view) {
        Intent intent = new Intent(this, EventsActivity.class);
        startActivity(intent);
    }

}