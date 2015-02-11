package com.anescobar.musicale.app.services;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.anescobar.musicale.app.services.interfaces.SpotifyTrackInfoTaskListener;
import com.anescobar.musicale.app.services.exceptions.NetworkNotAvailableException;
import com.anescobar.musicale.app.utils.NetworkUtil;
import com.anescobar.musicale.app.models.SpotifyTrack;
import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Andres Escobar on 11/16/14.
 * Class to get spotify track info details from spotify web api service
 */
public class SpotifyTrackInfoSeeker {
    private NetworkUtil mNetworkUtil = new NetworkUtil();
    private static final String SPOTIFY_API_URL = "https://api.spotify.com/v1/search?";
    private static final String LOG_TAG = "SpotifyTrackInfoSeeker";

    public SpotifyTrackInfoSeeker() {
    }

    public void getTrackInfo(String artistName, String trackName,
                             final SpotifyTrackInfoTaskListener listener, final Context context, final String trackId) throws NetworkNotAvailableException {

        if (mNetworkUtil.isNetworkAvailable(context)) {
            //let listener know that task is about to start
            listener.onSpotifyTrackInfoFetcherTaskAboutToStart(trackId);

            OkHttpClient client = new OkHttpClient();

            //create url for request
            String sanitizedArtist = null;
            String sanitizedTrackName = null;
            try {
                sanitizedTrackName = URLEncoder.encode(trackName, "UTF-8");
                sanitizedArtist = URLEncoder.encode(artistName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String searchQuery = "artist:" + sanitizedArtist + "+track:" + sanitizedTrackName;

            String url = SPOTIFY_API_URL + "type=track&limit=1&q=" + searchQuery;
            final Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                Handler mainHandler = new Handler(context.getMainLooper());
                @Override
                public void onFailure(final Request request, final IOException e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(LOG_TAG, request.toString());
                        }
                    });
                }

                @Override
                public void onResponse(final Response response) throws IOException {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(LOG_TAG, response.toString());
                            try {
                                JSONObject jObject = new JSONObject(streamToString(response.body().byteStream()));

                                SpotifyTrack track = new SpotifyTrack();
                                track.trackId = trackId;

                                //if there are results for search query then populate model from response
                                if (jObject.getJSONObject("tracks").getInt("total") > 0) {
                                    track.artistName = jObject.getJSONObject("tracks").getJSONArray("items").getJSONObject(0).getJSONArray("artists").getJSONObject(0).getString("name");
                                    track.popularity = jObject.getJSONObject("tracks").getJSONArray("items").getJSONObject(0).getInt("popularity");
                                    track.previewUrl = jObject.getJSONObject("tracks").getJSONArray("items").getJSONObject(0).getString("preview_url");
                                    track.trackName = jObject.getJSONObject("tracks").getJSONArray("items").getJSONObject(0).getString("name");
                                    track.fullTrackUrl = jObject.getJSONObject("tracks").getJSONArray("items").getJSONObject(0).getJSONObject("external_urls").getString("spotify");
                                }
                                //return Spotify Track model to calling class using callback
                                listener.onSpotifyTrackInfoFetcherTaskCompleted(track);
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }
                        }
                    });
                }
            });
        } else {
            throw new NetworkNotAvailableException("Not connected to network...");
        }
    }

    private String streamToString(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

}