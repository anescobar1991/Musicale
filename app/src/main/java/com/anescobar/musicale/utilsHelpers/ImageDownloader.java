package com.anescobar.musicale.utilsHelpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by Andres Escobar on 6/17/14.
 * Downloads images given a URL
 */
public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

    private ImageView mImageView;

    public ImageDownloader(ImageView imageView) {
        this.mImageView = imageView;
    }

    protected Bitmap doInBackground(String... urls) {
        Bitmap image = null;
        try {
            InputStream inputStream = new java.net.URL(urls[0]).openStream();
            image = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            Log.w("error", e);
        }
        return image;
    }

    protected void onPostExecute(Bitmap result) {
        mImageView.setImageBitmap(result);
    }
}
