package com.lazymonster.popularmovies;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LazyMonster on 01/04/2016.
 */
public class MainFragment extends Fragment {

    private static Context context;

    public static List<MovieItem> mMovieItems;
    private MoviesAdapter mAdapter;

    public static int mCheckedItem;
    private int mLastCheckedItem;

    private MoviesLoader mLoader = null;

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        setHasOptionsMenu(true);
        if (!isNetworkAvailable())
            notifyNetworkNotAvailable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        mMovieItems = new ArrayList<>();
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new MoviesAdapter(mMovieItems);
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
                    getResources().getString(R.string.toprated_title)
            };
            builder.setSingleChoiceItems(colors, mCheckedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mCheckedItem = which;
                }
            });
            builder.setTitle("Choose sort order");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mCheckedItem != mLastCheckedItem) {
                        if (!isNetworkAvailable())
                            notifyNetworkNotAvailable();
                        mLoader = new MoviesLoader();
                        mLoader.execute();
                        mLastCheckedItem = mCheckedItem;
                        switchToolbarTitle();
                    }
                }
            });
            builder.setNegativeButton("Cancel", null);

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
            } else {
                actionBar.setTitle(getResources().getString(R.string.toprated_title));
            }
    }

    private void notifyNetworkNotAvailable() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Error");
        builder.setMessage("No internet connection");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDestroy();
                getActivity().onBackPressed();
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

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(typeQuery)
                    .appendQueryParameter("api_key", context.getResources().getString(R.string.my_api_key));

            URL url = null;
            try {
                url = new URL(builder.build().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                HttpHelper helper = new HttpHelper();
                JSONObject object = new JSONObject(helper.getJSON(url.toString()));
                JSONArray arr = object.getJSONArray("results");
                mMovieItems.clear();
                for (int i = 0; i < arr.length(); i++) {
                    Object o = arr.get(i);
                    JSONObject movie = new JSONObject(o.toString());
                    MovieItem item = new MovieItem(
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
