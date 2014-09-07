package com.anescobar.musicale.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.anescobar.musicale.R;
import com.anescobar.musicale.fragments.AboutMusicaleFragment;
import com.google.android.gms.common.ConnectionResult;

public class HomeActivity extends BaseActivity
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

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

}