package com.anescobar.musicale.view.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.exceptions.NetworkNotAvailableException;
import com.anescobar.musicale.app.interfaces.TwitterGuestSessionFetcherListener;
import com.anescobar.musicale.app.interfaces.TwitterSearchTaskListener;
import com.anescobar.musicale.app.services.TwitterService;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetViewAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class EventSocialMediaFragment extends Fragment implements TwitterGuestSessionFetcherListener, TwitterSearchTaskListener {

    private TweetViewAdapter<? extends com.twitter.sdk.android.tweetui.BaseTweetView> mTweetsAdapter;
    private long mMaxId;
    private boolean mTweetsCurrentlyLoading = false;
    private boolean mEndOfSearchResults = false;
    private TwitterService mTwitterService;

    @InjectView(R.id.event_tweets_list) ListView mTweetsList;
    @InjectView(R.id.tweets_loading_progressbar) ProgressBar mLoadingProgressBar;
    @InjectView(R.id.tweets_message_container) TextView mMessageContainer;

    public EventSocialMediaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_social_media, container, false);
        mTwitterService = new TwitterService();

        ButterKnife.inject(this, view);

        try {
            mTwitterService.loginAsGuest(this, getActivity().getApplicationContext());
        } catch (NetworkNotAvailableException e) {
            mLoadingProgressBar.setVisibility(View.GONE);
            mMessageContainer.setText(R.string.error_no_network_connectivity);
            mMessageContainer.setVisibility(View.VISIBLE);
        }

        return view;
    }

    public void setupView(final Session session) {
        mTweetsAdapter = new TweetViewAdapter<>(getActivity());
        mTweetsList.setAdapter(mTweetsAdapter);
        mTweetsList.setOnScrollListener(new ListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    if (!mTweetsCurrentlyLoading && !mEndOfSearchResults) {
                        mTweetsCurrentlyLoading = true;
                        loadTweets(session);
                    }
                }
            }
        });
    }

    private void loadTweets(Session session) {
        //TODO need to use the artists that are playing in the event AND the venue name as query
        try {
            mTwitterService.searchForTweets(this, getActivity().getApplicationContext(),
                    session, "asflkasjdlkfjaklfjadf", mMaxId);
        } catch (NetworkNotAvailableException e) {
            mLoadingProgressBar.setVisibility(View.GONE);
            mMessageContainer.setText(R.string.error_no_network_connectivity);
            mMessageContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTwitterGuestSessionFetcherTaskAboutToStart() {}

    @Override
    public void onTwitterGuestSessionFetcherTaskSuccessful(Session session) {
        setupView(session);
    }

    @Override
    public void onTwitterSearchTaskAboutToStart() {}

    @Override
    public void onTwitterSearchTaskSuccessful(List<Tweet> tweets) {
        mLoadingProgressBar.setVisibility(View.GONE);
        mTweetsCurrentlyLoading = false;

        if (!tweets.isEmpty()) {
            mTweetsAdapter.getTweets().addAll(tweets);
            mTweetsAdapter.notifyDataSetChanged();

            if (tweets.size() > 0) {
                mMaxId = tweets.get(tweets.size() - 1).id - 1;
            } else {
                mEndOfSearchResults = true;
            }
        } else {
            mMessageContainer.setText(R.string.no_tweets_found);
            mMessageContainer.setVisibility(View.VISIBLE);
        }
    }
}