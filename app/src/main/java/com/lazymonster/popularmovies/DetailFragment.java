package com.lazymonster.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lazymonster.popularmovies.Data.MovieContract;
import com.lazymonster.popularmovies.Item.MovieItem;
import com.lazymonster.popularmovies.Item.ReviewItem;
import com.lazymonster.popularmovies.Item.TrailerItem;
import com.lazymonster.popularmovies.Utils.HttpHelper;
import com.lazymonster.popularmovies.Utils.UrlHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LazyMonster on 01/04/2016.
 */
public class DetailFragment extends Fragment {

    private Context mContext;
    private MovieItem mMovieItem;
    private DetailAdapter mAdapter;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getContext();
        if (MainFragment.mMovieItems != null) {
            if (MainFragment.mMovieItems.size() > DetailActivity.mPostion)
                mMovieItem = MainFragment.mMovieItems.get(DetailActivity.mPostion);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_detail);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(llm);

        if (mMovieItem != null) {
            mAdapter = new DetailAdapter(mMovieItem);
            recyclerView.setAdapter(mAdapter);

            if (!loadFromDb()) {
                new TrailersLoader().execute(String.valueOf(mMovieItem.getId()));
                new ReviewLoader().execute(String.valueOf(mMovieItem.getId()));
            }
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail, menu);

        if (mMovieItem != null) {
            MenuItem menuItem = menu.findItem(R.id.action_share);
            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_favorite:
                if (mMovieItem == null)
                    break;
                addToDb();
                break;
            case R.id.action_share:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mMovieItem.getTitle() + ": " + mMovieItem.getOverview());
        return shareIntent;
    }

    /**
     * add data trailer and review to db
     */
    private void addToDb() {
        ContentResolver resolver = mContext.getContentResolver();

        Cursor c = resolver.query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=" + mMovieItem.getId(),
                null,
                null
        );
        if (c.moveToFirst()) {
            // Movie data has been inserted into database
            Toast.makeText(mContext, getResources().getString(R.string.already_in_db), Toast.LENGTH_SHORT).show();
        } else {

            ContentValues values = new ContentValues();
            values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovieItem.getId());
            values.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, mMovieItem.getPosterPath());
            values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, mMovieItem.getOverview());
            values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DAY, mMovieItem.getReleaseDay());
            values.put(MovieContract.MovieEntry.COLUMN_TITLE, mMovieItem.getTitle());
            values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, mMovieItem.getVoteAverage());
            values.put(MovieContract.MovieEntry.COLUMN_TYPE, MovieContract.MovieEntry.TYPE_FAVORITED);
            Uri returnUri = resolver.insert(MovieContract.MovieEntry.CONTENT_URI, values);

            if (returnUri != null) {
                for (TrailerItem trailerItem : mMovieItem.getTrailerItems()) {
                    ContentValues values1 = new ContentValues();
                    values1.put(MovieContract.TrailerEntry.COLUMN_KEY, trailerItem.getKey());
                    values1.put(MovieContract.TrailerEntry.COLUMN_NAME, trailerItem.getName());
                    values1.put(MovieContract.TrailerEntry.COLUMN_SITE, trailerItem.getSite());
                    values1.put(MovieContract.TrailerEntry.COLUMN_TYPE, trailerItem.getType());
                    values1.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, returnUri.getLastPathSegment());
                    resolver.insert(MovieContract.TrailerEntry.CONTENT_URI, values1);
                }

                for (ReviewItem reviewItem : mMovieItem.getReviewItems()) {
                    ContentValues values2 = new ContentValues();
                    values2.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, reviewItem.getAuthor());
                    values2.put(MovieContract.ReviewEntry.COLUMN_CONTENT, reviewItem.getContent());
                    values2.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, returnUri.getLastPathSegment());
                    resolver.insert(MovieContract.ReviewEntry.CONTENT_URI, values2);
                }
            }
            Toast.makeText(mContext, getResources().getString(R.string.add_db_success), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * load data of review and trailer from db
     *
     * @return true if data exist
     * false if data don't exist
     */
    private boolean loadFromDb() {
        ContentResolver resolver = mContext.getContentResolver();
        Cursor c = resolver.query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=" + mMovieItem.getId(),
                null,
                null
        );
        if (c.moveToFirst()) {
            int _id = c.getInt(c.getColumnIndex(MovieContract.MovieEntry._ID));

            // add trailer item from db
            Cursor c1 = resolver.query(
                    MovieContract.TrailerEntry.CONTENT_URI,
                    null,
                    MovieContract.TrailerEntry.COLUMN_MOVIE_KEY + "=" + _id,
                    null,
                    null
            );
            List<TrailerItem> trailerItems = new ArrayList<>();
            while (c1.moveToNext()) {
                TrailerItem trailerItem = new TrailerItem(
                        c1.getString(c1.getColumnIndex(MovieContract.TrailerEntry.COLUMN_KEY)),
                        c1.getString(c1.getColumnIndex(MovieContract.TrailerEntry.COLUMN_NAME)),
                        c1.getString(c1.getColumnIndex(MovieContract.TrailerEntry.COLUMN_SITE)),
                        c1.getString(c1.getColumnIndex(MovieContract.TrailerEntry.COLUMN_TYPE))
                );
                trailerItems.add(trailerItem);
            }
            mMovieItem.setTrailerItems(trailerItems);

            // add review item from db
            Cursor c2 = resolver.query(
                    MovieContract.ReviewEntry.CONTENT_URI,
                    null,
                    MovieContract.ReviewEntry.COLUMN_MOVIE_KEY + "=" + _id,
                    null,
                    null
            );
            List<ReviewItem> reviewItems = new ArrayList<>();
            while (c2.moveToNext()) {
                ReviewItem reviewItem = new ReviewItem(
                        c2.getString(c2.getColumnIndex(MovieContract.ReviewEntry.COLUMN_AUTHOR)),
                        c2.getString(c2.getColumnIndex(MovieContract.ReviewEntry.COLUMN_CONTENT))
                );
                reviewItems.add(reviewItem);
            }
            mMovieItem.setReviewItems(reviewItems);

            mAdapter.notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    class TrailersLoader extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... movieId) {
            String[] paths = {movieId[0], "videos"};
            URL url = UrlHelper.build(mContext, paths);

            try {
                HttpHelper helper = new HttpHelper();
                JSONObject json = new JSONObject(helper.getJSON(url.toString()));
                JSONArray array = json.getJSONArray("results");

                List<TrailerItem> trailerItems = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    Object o = array.get(i);
                    JSONObject trailer = new JSONObject(o.toString());

                    TrailerItem item = new TrailerItem(
                            trailer.getString("key"),
                            trailer.getString("name"),
                            trailer.getString("site"),
                            trailer.getString("type")
                    );
                    trailerItems.add(item);
                }
                mMovieItem.setTrailerItems(trailerItems);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter.notifyDataSetChanged();
        }
    }

    class ReviewLoader extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... movieId) {
            String[] paths = {movieId[0], "reviews"};
            URL url = UrlHelper.build(mContext, paths);

            try {
                HttpHelper helper = new HttpHelper();
                JSONObject json = new JSONObject(helper.getJSON(url.toString()));
                JSONArray array = json.getJSONArray("results");

                List<ReviewItem> reviewItems = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    Object o = array.get(i);
                    JSONObject review = new JSONObject(o.toString());

                    ReviewItem item = new ReviewItem(
                            review.getString("author"),
                            review.getString("content")
                    );
                    reviewItems.add(item);
                }
                mMovieItem.setReviewItems(reviewItems);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter.notifyDataSetChanged();
        }
    }
}
