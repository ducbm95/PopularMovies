package com.lazymonster.popularmovies;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lazymonster.popularmovies.Data.MovieContract;
import com.lazymonster.popularmovies.Item.MovieItem;
import com.lazymonster.popularmovies.Utils.HttpHelper;
import com.lazymonster.popularmovies.Utils.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LazyMonster on 01/04/2016.
 */
public class MainFragment extends Fragment {

    private static Context mContext;

    public static List<MovieItem> mMovieItems;
    private MainAdapter mAdapter;

    public static int mCheckedItem;
    private int mLastCheckedItem;

    private MoviesLoader mLoader = null;

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mMovieItems = new ArrayList<>();
        setHasOptionsMenu(true);
        if (!isNetworkAvailable())
            notifyNetworkNotAvailable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        if (MainActivity.mTwoPane) {
            layoutManager.setSpanCount(3);
        }
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MainAdapter(mMovieItems);
        recyclerView.setAdapter(mAdapter);

        mLoader = new MoviesLoader();
        mLoader.execute();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            String[] colors = new String[]{
                    getResources().getString(R.string.popular_title),
                    getResources().getString(R.string.toprated_title),
                    getResources().getString(R.string.favorite)
            };
            builder.setSingleChoiceItems(colors, mCheckedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mCheckedItem = which;
                }
            });
            builder.setTitle(getResources().getString(R.string.choose_sort_order));
            builder.setPositiveButton(getResources().getString(R.string.ok_label), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mCheckedItem != mLastCheckedItem) {
                        if (mCheckedItem == 0 || mCheckedItem == 1) {
                            if (!isNetworkAvailable())
                                notifyNetworkNotAvailable();
                            if (mLoader != null) {
                                mLoader.cancel(true);
                            }
                            mLoader = new MoviesLoader();
                            mLoader.execute();
                        } else {
                            loadFavoriteMovie();
                        }
                        mLastCheckedItem = mCheckedItem;
                        switchToolbarTitle();
                    }
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.cancel_label), null);

            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCheckedItem = 0;
        mMovieItems.clear();
        if (mLoader != null)
            mLoader.cancel(true);
        mLoader = null;
    }

    private void switchToolbarTitle() {
        ActionBar actionBar = ((AppCompatActivity) getContext()).getSupportActionBar();
        if (actionBar != null)
            if (mCheckedItem == 0) {
                actionBar.setTitle(getResources().getString(R.string.popular_title));
            } else if (mCheckedItem == 1) {
                actionBar.setTitle(getResources().getString(R.string.toprated_title));
            } else {
                actionBar.setTitle(getResources().getString(R.string.favorite));
            }
    }

    private void notifyNetworkNotAvailable() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getResources().getString(R.string.error_label));
        builder.setMessage(getResources().getString(R.string.no_connection));
        builder.setNegativeButton(getResources().getString(R.string.back_label), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDestroy();
                getActivity().onBackPressed();
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.offline_label), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCheckedItem = 2;
                loadFavoriteMovie();
                mLastCheckedItem = mCheckedItem;
                switchToolbarTitle();
            }
        });
        builder.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void loadFavoriteMovie() {
        // handle favorite movie
        mMovieItems.clear();
        Cursor c = getContext().getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                MovieContract.MovieEntry.COLUMN_TYPE + "=" + MovieContract.MovieEntry.TYPE_FAVORITED,
                null,
                null
        );
        if (c == null || c.getCount() == 0) {
            Toast.makeText(mContext, getResources().getString(R.string.no_favorite_movie), Toast.LENGTH_LONG).show();
        } else {
            while (c.moveToNext()) {
                MovieItem item = new MovieItem(
                        c.getInt(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID)),
                        c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH)),
                        c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW)),
                        c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DAY)),
                        c.getString(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE)),
                        c.getDouble(c.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE))

                );
                mMovieItems.add(item);
            }
            c.close();

        }
        mAdapter.notifyDataSetChanged();
    }

    class MoviesLoader extends AsyncTask<Void, Void, Void> {

        private final String BASE_POPULAR = "popular";
        private final String BASE_TOP = "top_rated";

        @Override
        protected Void doInBackground(Void... params) {
            String typeQuery;
            if (mCheckedItem == 0)
                typeQuery = BASE_POPULAR;
            else
                typeQuery = BASE_TOP;

            String[] paths = {typeQuery};
            URL url = UrlHelper.build(mContext, paths);

            try {
                HttpHelper helper = new HttpHelper();
                JSONObject object = new JSONObject(helper.getJSON(url.toString()));
                JSONArray arr = object.getJSONArray("results");
                mMovieItems.clear();
                for (int i = 0; i < arr.length(); i++) {
                    Object o = arr.get(i);
                    JSONObject movie = new JSONObject(o.toString());
                    MovieItem item = new MovieItem(
                            movie.getInt("id"),
                            movie.getString("poster_path"),
                            movie.getString("overview"),
                            movie.getString("release_date"),
                            movie.getString("title"),
                            movie.getDouble("vote_average")
                    );
                    mMovieItems.add(item);
                }
            } catch (JSONException | NullPointerException e) {
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
