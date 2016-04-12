package com.lazymonster.popularmovies.Utils;

import android.content.Context;
import android.net.Uri;

import com.lazymonster.popularmovies.R;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by LazyMonster on 08/04/2016.
 */
public class UrlHelper {

    public static URL build(Context c, String[] paths) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("movie");
        for (int i = 0; i < paths.length; i++) {
            builder.appendPath(paths[i]);
        }
        builder.appendQueryParameter("api_key", c.getResources().getString(R.string.my_api_key));

        URL url = null;
        try {
            url = new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }
}
