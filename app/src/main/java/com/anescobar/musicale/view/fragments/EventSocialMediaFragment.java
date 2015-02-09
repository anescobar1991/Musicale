package com.anescobar.musicale.view.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.anescobar.musicale.R;
import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.SearchService;
import com.twitter.sdk.android.tweetui.TweetViewAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class EventSocialMediaFragment extends Fragment {
    private static final int SEARCH_COUNT = 20;
    private static final String SEARCH_RESULT_TYPE = "recent";
    private static final String SEARCH_QUERY = "#grammys";

    private long maxId;


    private TweetViewAdapter mTweetsAdapter;

    private boolean mTweetsLoading = false;
    private boolean mEndOfSearchResults = false;

    @InjectView(R.id.event_tweets_list) ListView mTweetsList;

    public EventSocialMediaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_social_media, container, false);

        ButterKnife.inject(this, view);

        TwitterCore.getInstance().logInGuest(new Callback<AppSession>() {
            @Override
            public void success(Result<AppSession> result) {
                setupView();
            }

            @Override
            public void failure(TwitterException exception) {
                // unable to get an AppSession with guest auth
            }
        });

        return view;
    }

    public void setupView() {
        mTweetsAdapter = new TweetViewAdapter(getActivity());
        mTweetsList.setAdapter(mTweetsAdapter);
        loadTweets();

        mTweetsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if ((firstVisibleItem + visibleItemCount == totalItemCount) &&
                        totalItemCount != 0) {
                    if (!mTweetsLoading && !mEndOfSearchResults) {
                        mTweetsLoading = true;
                        loadTweets();
                    }
                }
            }
        });
    }

    private void loadTweets() {
        final SearchService service = Twitter.getApiClient().getSearchService();
        service.tweets(SEARCH_QUERY, null, null, null, SEARCH_RESULT_TYPE, SEARCH_COUNT, null, null,
                maxId, true, new Callback<Search>() {
                    @Override
                    public void success(Result<Search> searchResult) {
                        final List<Tweet> tweets = searchResult.data.tweets;
                        mTweetsAdapter.getTweets().addAll(tweets);
                        //TODO if results are empty then need to display message
                        mTweetsAdapter.notifyDataSetChanged();

                        if (tweets.size() > 0) {
                            maxId = tweets.get(tweets.size() - 1).id - 1;
                        } else {
                            mEndOfSearchResults = true;
                        }
                        mTweetsLoading = false;
                    }

                    @Override
                    public void failure(TwitterException error) {
                        Crashlytics.logException(error);

                        //TODO do something here if response has error

                        mTweetsLoading = false;
                    }
                }
        );
    }
}