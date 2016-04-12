package com.lazymonster.popularmovies.data;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.lazymonster.popularmovies.Data.MovieContract;

/**
 * Created by LazyMonster on 08/04/2016.
 */
public class TestMovieContract extends AndroidTestCase {

    public void testMovieContractBuilder() {
        Uri movieUri = MovieContract.MovieEntry.buildMovieUri(0);
        assertNotNull("Error: Null uri returned", movieUri);
        assertEquals("Error: movieUri doesn't equal to result" + movieUri.toString(), movieUri.toString(),
                "content://com.lazymonster.popularmovies/movie/0");
    }
}
