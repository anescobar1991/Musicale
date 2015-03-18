package com.anescobar.musicale.view.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.anescobar.musicale.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class AboutMusicaleFragment extends BaseFragment {

    @InjectView(R.id.picasso_license) WebView mPicassoLicenseWebview;
    @InjectView(R.id.butterknife_license) WebView mButterKnifeLicenseWebview;
    @InjectView(R.id.gson_license) WebView mGsonLicenseWebview;
    @InjectView(R.id.okhttp_license) WebView mOkHttpLicenseWebview;
    @InjectView(R.id.PagerSlidingTabStrip_license) WebView mPagerSlidingTabStripLicenseWebview;
    @InjectView(R.id.floating_action_button_license) WebView mFloatingActionButtonLicenseWebview;

    public AboutMusicaleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_about_musicale, container, false);

        ButterKnife.inject(this, view);

        setUpLicenseViews();

        return view;
    }

    @OnClick(R.id.email_address_row)
    public void openEmailClient() {
        Intent send = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode("musicaleappteam@gmail.com");
        Uri uri = Uri.parse(uriText);

        send.setData(uri);
        startActivity(Intent.createChooser(send, "Send mail..."));
    }

    @OnClick(R.id.last_fm_link)
    public void openLastFmInBrowser() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.last_fm_url)));
        startActivity(i);
    }

    @OnClick(R.id.spotify_link)
    public void openSpotifyInBrowser() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.spotify_url)));
        startActivity(i);
    }

    private void setUpLicenseViews() {
        mPicassoLicenseWebview.loadUrl("file:///android_asset/picasso_license.html");
        mButterKnifeLicenseWebview.loadUrl("file:///android_asset/butter_knife_license.html");
        mGsonLicenseWebview.loadUrl("file:///android_asset/gson_license.html");
        mOkHttpLicenseWebview.loadUrl("file:///android_asset/okHttp_license.html");
        mPagerSlidingTabStripLicenseWebview.loadUrl("file:///android_asset/pagerSlidingTabStripLicense.html");
        mFloatingActionButtonLicenseWebview.loadUrl("file:///android_asset/floating_action_button_license.html");
    }
}