package com.anescobar.musicale.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Andres Escobar on 6/17/14.
 * Util class for all HTTP connections
 */
public class HTTPUtil {

    public static String downloadUrl(URL requestUrl) throws IOException {
        InputStream inputStream = null;

        try {
            URLConnection urlConnection = requestUrl.openConnection();
            urlConnection.setConnectTimeout(20000);
            urlConnection.setReadTimeout(20000);
            inputStream = urlConnection.getInputStream();

            return streamToString(inputStream);

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private static String streamToString(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

}