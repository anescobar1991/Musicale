package com.anescobar.musicale.view.fragments;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anescobar.musicale.R;
import com.anescobar.musicale.app.services.LastFmServiceProvider;
import com.anescobar.musicale.app.services.interfaces.ArtistInfoFetcherTaskListener;
import com.anescobar.musicale.app.services.interfaces.ArtistTopTracksFetcherTaskListener;
import com.anescobar.musicale.app.services.interfaces.ArtistUpcomingEventsFetcherTaskListener;
import com.anescobar.musicale.app.services.interfaces.SpotifyTrackInfoTaskListener;
import com.anescobar.musicale.app.models.ArtistDetails;
import com.anescobar.musicale.app.models.SpotifyTrack;

import com.anescobar.musicale.app.services.SpotifyServiceProvider;
import com.anescobar.musicale.view.activities.ArtistDetailsActivity;
import com.anescobar.musicale.view.activities.EventDetailsActivity;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.umass.lastfm.Artist;
import de.umass.lastfm.Event;
import de.umass.lastfm.ImageSize;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Track;

public class AboutArtistFragment extends BaseFragment implements ArtistInfoFetcherTaskListener,
        ArtistTopTracksFetcherTaskListener, ArtistUpcomingEventsFetcherTaskListener,
        SpotifyTrackInfoTaskListener {

    private static final String ARG_ARTIST = "artistArg";
    private ArtistDetails mArtistDetails = new ArtistDetails();
    private CachedArtistDetailsGetterSetter mCachedArtistDetailsGetterSetter;
    private ArrayList<View> mTrackLinks = new ArrayList<>();
    private LastFmServiceProvider mLastFmServiceProvider;

    @InjectView(R.id.content) LinearLayout mContainer;
    @InjectView(R.id.about_artist_progressbar) ProgressBar mContentLoadingProgressBar;
    @InjectView(R.id.message_container) TextView mErrorMessageContainer;
    @InjectView(R.id.about_artist_card) CardView mAboutArtistCard;
    @InjectView(R.id.artist_name) TextView mArtistName;
    @InjectView(R.id.artist_bio) TextView mArtistBio;
    @InjectView(R.id.artist_tags) TextView mArtistTags;
    @InjectView(R.id.artist_image) ImageView mArtistImage;
    @InjectView(R.id.upcoming_events_container) LinearLayout mUpcomingEventsContainer;
    @InjectView(R.id.artist_upcoming_events_card) CardView mUpcomingEventsCard;
    @InjectView(R.id.similar_artists_container) LinearLayout mSimilarArtistsContainer;
    @InjectView(R.id.artist_similar_artists_card) CardView mSimilarArtistsCard;
    @InjectView(R.id.top_tracks_container) LinearLayout mTopTracksContainer;
    @InjectView(R.id.artist_top_tracks_card) CardView mTopTracksCard;

    private MediaPlayer mMediaPlayer;
    private String mArtist;

    public AboutArtistFragment() {
    }

    public interface CachedArtistDetailsGetterSetter {
        ArtistDetails getArtistDetails();
        void setArtistDetails(ArtistDetails artistDetails);
    }

    public static AboutArtistFragment newInstance(String artist) {
        AboutArtistFragment fragment = new AboutArtistFragment();

        Bundle args = new Bundle();

        args.putString(ARG_ARTIST, artist);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_about_artist, container, false);

        ButterKnife.inject(this, rootView);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mArtist = getArguments().getString(ARG_ARTIST, null);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAnalyticsUtil.sendAnalyticsScreenHit(getClass().getSimpleName());
        mLastFmServiceProvider = new LastFmServiceProvider(getActivity().getApplicationContext());

        try {
            if (mCachedArtistDetailsGetterSetter.getArtistDetails().artist != null) {
                setUpView(mCachedArtistDetailsGetterSetter.getArtistDetails().artist);
            } else {
                mLastFmServiceProvider.getArtistInfo(mArtist, this);
            }

            if (mCachedArtistDetailsGetterSetter.getArtistDetails().upcomingEvents != null) {
                populateUpcomingEventsContainer(mCachedArtistDetailsGetterSetter.getArtistDetails().upcomingEvents);
            } else {
                mLastFmServiceProvider.getArtistUpcomingEvents(mArtist, this);
            }

            if (mCachedArtistDetailsGetterSetter.getArtistDetails().topTracks != null) {
                populateTopTracksContainer(mCachedArtistDetailsGetterSetter.getArtistDetails().topTracks);
            }

        } catch (NetworkErrorException e) {
            e.printStackTrace();

            displayErrorMessage(getString(R.string.error_no_network_connectivity));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mCachedArtistDetailsGetterSetter.setArtistDetails(mArtistDetails);

        mMediaPlayer.release();

        //reset all track's play buttons
        resetPlayButtonForTracksNotPlaying(null);
    }

    @Override
    public void onResume() {
        super.onResume();

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCachedArtistDetailsGetterSetter = (CachedArtistDetailsGetterSetter) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement mCachedArtistDetailsGetterSetter");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCachedArtistDetailsGetterSetter = null;
    }

    private void setUpView(final Artist artist) {
        mArtistName.setText(artist.getName());

        if (!artist.getWikiSummary().isEmpty()) {
            mArtistBio.setText(Html.fromHtml(artist.getWikiSummary()).toString());
        } else {
            mArtistBio.setVisibility(View.GONE);
        }

        Collection<String> tags = artist.getTags();

        String formattedTags = "";

        for(String tag : tags) {
            formattedTags += tag + ", ";
        }
        if (formattedTags.length() > 0) {
            //remove last comma from formatted tags string
            formattedTags = formattedTags.substring(0, formattedTags.length()-2);

            mArtistTags.setText(formattedTags.toUpperCase());
        }

        String artistImageUrl = artist.getImageURL(ImageSize.EXTRALARGE);
        if (artistImageUrl.length() > 0) {
            Picasso.with(getActivity()).load(artistImageUrl)
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .resize(400, 400)
                    .into(mArtistImage);
        } else {
            mArtistImage.setImageResource(R.drawable.placeholder);
        }

        displaySimilarArtists(artist.getSimilar());

        mAboutArtistCard.setVisibility(View.VISIBLE);
        mContainer.setVisibility(View.VISIBLE);
        mContentLoadingProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onArtistInfoFetcherTaskAboutToStart() {}

    @Override
    public void onArtistInfoFetcherTaskCompleted(Artist artist) {
        if (getActivity() != null) {
            mArtistDetails.artist = artist;
            try {
                mLastFmServiceProvider.getArtistTopTracks(artist.getName(), this);
            } catch (NetworkErrorException e) {
                displayErrorMessage(getString(R.string.error_no_network_connectivity));
            }

            setUpView(artist);
        }
    }

    private void setUpArtistCard(final Artist artist, final LinearLayout parentView) {
        LayoutInflater vi = LayoutInflater.from(getActivity());
        View view = vi.inflate(R.layout.artist_card, parentView, false);

        LinearLayout artistCard = (LinearLayout) view.findViewById(R.id.artist_card);
        ImageView artistImageView = (ImageView) view.findViewById(R.id.artist_image);
        TextView artistTitleTextView = (TextView) view.findViewById(R.id.artist_name);

        artistTitleTextView.setText(artist.getName());

        String eventImageUrl = artist.getImageURL(ImageSize.EXTRALARGE);
        if (eventImageUrl.length() > 0) {
            Picasso.with(getActivity())
                    .load(eventImageUrl)
                    .resize(120, 120)
                    .placeholder(R.drawable.placeholder)
                    .into(artistImageView);
        } else {
            artistImageView.setImageResource(R.drawable.placeholder);
        }

        artistCard.setOnClickListener(new LinearLayout.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ArtistDetailsActivity.class);
                intent.putExtra("ARTIST", artist.getName());

                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getActivity().getApplicationContext(), R.anim.slide_in_right, R.anim.slide_out_left);
                getActivity().startActivity(intent, activityOptions.toBundle());
            }
        });

        parentView.addView(view);
    }

    private void addTopTrackLink(final Track track, final LinearLayout parentView) {
        LayoutInflater vi = LayoutInflater.from(getActivity());
        View view = vi.inflate(R.layout.track_link_view, parentView, false);

        final String trackId;

        if (track.getMbid().length() > 0) {
            trackId = track.getMbid();
        } else {
            trackId = Integer.toString(new Random().nextInt()); //if no mbid then use randomly generated ID
        }

        view.setTag(trackId);
        mTrackLinks.add(view);

        LinearLayout trackLayout = (LinearLayout) view.findViewById(R.id.track_view_layout);
        TextView trackTextView = (TextView) view.findViewById(R.id.track_link_name);

        trackTextView.setText(track.getName());

        trackLayout.setOnClickListener(new LinearLayout.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAnalyticsUtil.sendAnalyticsEvent(getString(R.string.ga_action_event), getString(R.string.ga_event_track_played), null);


                getSpotifyTrackInfo(track.getName(), track.getArtist(), trackId);
            }
        });

        parentView.addView(view);
    }


    @Override
    public void onArtistTopTrackFetcherTaskAboutToStart() {}

    @Override
    public void onArtistTopTrackFetcherTaskCompleted(Collection<Track> tracks) {
        //if activity has been killed then no need to attempt to populate view with tracks
        if (getActivity() != null && !tracks.isEmpty()) {
            mArtistDetails.topTracks = tracks;

            populateTopTracksContainer(tracks);
        }
    }

    private void populateTopTracksContainer(Collection<Track> tracks) {
        int counter = 0;
        for(Track track : tracks) {
            if (counter < 5) {
                counter ++;
                addTopTrackLink(track, mTopTracksContainer);
            }
        }

        mTopTracksCard.setVisibility(View.VISIBLE);
        mContainer.setVisibility(View.VISIBLE);
        mContentLoadingProgressBar.setVisibility(View.GONE);
    }

    private void displaySimilarArtists(Collection<Artist> artists) {
        //only populates view with similar artists if activity hasnt been killed
        if (getActivity() != null) {
            if (!artists.isEmpty()) {
                populateSimilarArtistsContainer(artists);

                mSimilarArtistsCard.setVisibility(View.VISIBLE);
                mContainer.setVisibility(View.VISIBLE);
                mContentLoadingProgressBar.setVisibility(View.GONE);
            }
        }
    }

    private void populateSimilarArtistsContainer(Collection<Artist> artists) {
        for (Artist artist : artists) {
            setUpArtistCard(artist, mSimilarArtistsContainer);
        }
    }

    @Override
    public void onArtistUpcomingEventsFetcherTaskAboutToStart() {}

    @Override
    public void onArtistUpcomingEventsFetcherTaskCompleted(PaginatedResult<Event> events) {
        //if activity is null(b/c user navigated away from screen) then shouldnt load events to screen
        if (getActivity() != null) {
            mArtistDetails.upcomingEvents = events.getPageResults();

            if (!events.isEmpty()) {
                populateUpcomingEventsContainer(events.getPageResults());
            }
        }
    }

    private void populateUpcomingEventsContainer(Collection<Event> events) {
        for (Event event : events) {
            setUpEventCard(event, mUpcomingEventsContainer);
        }

        mUpcomingEventsCard.setVisibility(View.VISIBLE);
        mContainer.setVisibility(View.VISIBLE);
        mContentLoadingProgressBar.setVisibility(View.GONE);
    }

    private void setUpEventCard(final Event event, final LinearLayout parentView) {
        LayoutInflater vi = LayoutInflater.from(getActivity());
        View view = vi.inflate(R.layout.artist_upcoming_event, parentView, false);

        RelativeLayout upcomingEventCard = (RelativeLayout) view.findViewById(R.id.artist_upcoming_event_card);
        ImageView venueImageImageView = (ImageView) view.findViewById(R.id.venue_image);
        TextView venueNameTextView = (TextView) view.findViewById(R.id.event_venue_name);
        TextView venueLocationTextView = (TextView) view.findViewById(R.id.venue_location);
        TextView eventDateTextView = (TextView) view.findViewById(R.id.event_date);

        eventDateTextView.setText(event.getStartDate().toLocaleString().substring(0, 12));
        venueNameTextView.setText("@ " + event.getVenue().getName());
        venueLocationTextView.setText(event.getVenue().getCity() + " " + event.getVenue().getCountry());

        String eventImageUrl = event.getVenue().getImageURL(ImageSize.EXTRALARGE);
        if (eventImageUrl.length() > 0) {
            Picasso.with(getActivity())
                    .load(eventImageUrl)
                    .resize(120, 120)
                    .placeholder(R.drawable.placeholder)
                    .into(venueImageImageView);
        } else {
            venueImageImageView.setImageResource(R.drawable.placeholder);
        }

        upcomingEventCard.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Gson gson = new Gson();

                String serializedEvent = gson.toJson(event, Event.class);

                Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
                intent.putExtra("EVENT", serializedEvent);
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getActivity().getApplicationContext(), R.anim.slide_in_right, R.anim.slide_out_left);

                getActivity().startActivity(intent, activityOptions.toBundle());
            }
        });

        parentView.addView(view);
    }

    private void getSpotifyTrackInfo(String trackName, String artistName, String trackId) {
        try {
            new SpotifyServiceProvider().getTrackInfo(artistName, trackName, this, getActivity().getApplicationContext(), trackId);
        } catch (NetworkErrorException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), getString(R.string.error_no_network_connectivity),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSpotifyTrackInfoFetcherTaskAboutToStart(String trackId) {
        View view = mTopTracksContainer.findViewWithTag(trackId);

        //parent activity would be null if user has navigated away from this screen
        if (getActivity() != null) {
            resetPlayButtonForTracksNotPlaying(trackId);

            ImageView playButton = (ImageView) view.findViewById(R.id.track_play_button);
            ProgressBar previewLoading = (ProgressBar) view.findViewById(R.id.track_loading_preview);

            playButton.setVisibility(View.GONE);
            previewLoading.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSpotifyTrackInfoFetcherTaskCompleted(final SpotifyTrack track) {
        View view = mTopTracksContainer.findViewWithTag(track.trackId);

        //parent activity would be null if user has navigated away from this screen
        if (getActivity() != null) {
            final ImageView playButton = (ImageView) view.findViewById(R.id.track_play_button);
            final ProgressBar previewLoading = (ProgressBar) view.findViewById(R.id.track_loading_preview);
            final ImageView stopButton = (ImageView) view.findViewById(R.id.track_stop_button);

            if (track.previewUrl != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mMediaPlayer.isPlaying()) {
                            resetPlayButtonForTracksNotPlaying(track.trackId);

                            playButton.setVisibility(View.VISIBLE);
                            previewLoading.setVisibility(View.GONE);
                            stopButton.setVisibility(View.GONE);

                            mMediaPlayer.reset();
                        } else {
                            getSpotifyTrackInfo(track.trackName, track.artistName, track.trackId);
                        }
                    }
                });
                try {

                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(track.previewUrl);
                    mMediaPlayer.prepareAsync();

                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            playButton.setVisibility(View.GONE);
                            previewLoading.setVisibility(View.GONE);
                            stopButton.setVisibility(View.VISIBLE);
                            mp.start();
                        }
                    });

                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            stopButton.setVisibility(View.GONE);
                            playButton.setVisibility(View.VISIBLE);

                            mMediaPlayer.reset();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();

                    previewLoading.setVisibility(View.GONE);
                    playButton.setVisibility(View.VISIBLE);

                    Toast.makeText(getActivity(), getString(R.string.preview_not_available),Toast.LENGTH_SHORT).show();
                }

            } else {
                previewLoading.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);

                Toast.makeText(getActivity(), getString(R.string.preview_not_available),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resetPlayButtonForTracksNotPlaying(String currentlyPlayingTrackId) {
        for (View trackLink : mTrackLinks) {
            if (trackLink.getTag() != currentlyPlayingTrackId) {
                final ImageView playButton = (ImageView) trackLink.findViewById(R.id.track_play_button);
                final ProgressBar previewLoading = (ProgressBar) trackLink.findViewById(R.id.track_loading_preview);
                final ImageView stopButton = (ImageView) trackLink.findViewById(R.id.track_stop_button);

                playButton.setVisibility(View.VISIBLE);
                previewLoading.setVisibility(View.GONE);
                stopButton.setVisibility(View.GONE);
            }
        }
    }

    private void displayErrorMessage(String message) {
        mContentLoadingProgressBar.setVisibility(View.GONE);
        mErrorMessageContainer.setText(message);
        mErrorMessageContainer.setVisibility(View.VISIBLE);
    }
}