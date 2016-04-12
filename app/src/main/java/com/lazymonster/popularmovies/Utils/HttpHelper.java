package com.lazymonster.popularmovies.Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by LazyMonster on 01/04/2016.
 */
public class HttpHelper {

    public String getJSON(String jsonUrl) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonResponse = null;

        try {
            URL url = new URL(jsonUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream ip = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (ip == null) {
                return null;
            }
            InputStreamReader isr = new InputStreamReader(ip);
            reader = new BufferedReader(isr);

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            jsonResponse = buffer.toString();

        } catch (Exception e) {

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {}
            }
        }
        return jsonResponse;
    }
}
