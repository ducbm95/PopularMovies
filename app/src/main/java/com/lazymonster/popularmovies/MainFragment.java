package com.lazymonster.popularmovies;

import android.app.Activity;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LazyMonster on 01/04/2016.
 */
public class MainFragment extends Fragment {

    public static List<MovieItem> mMovieItems;
    private MoviesAdapter mAdapter;
    public static int mCheckedItem;
    private int mLastCheckedItem;

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

        new LoadMovies().execute();
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
                    "Most Popular",
                    "Highest-rated"
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
                        mMovieItems.clear();
                        new LoadMovies().execute();
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

    private void switchToolbarTitle() {
        ActionBar actionBar = ((AppCompatActivity) getContext()).getSupportActionBar();
        if (actionBar != null)
            if (mCheckedItem == 0) {
                actionBar.setTitle(getResources().getString(R.string.popular_title));
            } else {
                actionBar.setTitle(getResources().getString(R.string.toprated_title));
            }
    }

    class LoadMovies extends AsyncTask<Void, Void, Void> {

        private final String BASE_POPULAR_URL = "http://api.themoviedb.org/3/movie/popular?api_key=";
        private final String BASE_TOP_URL = "http://api.themoviedb.org/3/movie/top_rated?api_key=";

        @Override
        protected Void doInBackground(Void... params) {
            String url = null;
            if (mCheckedItem == 0)
                url = BASE_POPULAR_URL;
            else
                url = BASE_TOP_URL;
            url += getResources().getString(R.string.my_api_key);
            HttpHelper helper = new HttpHelper();
            try {
                JSONObject object = new JSONObject(helper.getJSON(url));
                JSONArray arr = object.getJSONArray("results");

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
            } catch (JSONException e) {
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
