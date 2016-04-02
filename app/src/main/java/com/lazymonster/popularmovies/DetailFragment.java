package com.lazymonster.popularmovies;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by LazyMonster on 01/04/2016.
 */
public class DetailFragment extends Fragment {

    private MovieItem mMovieItem = MainFragment.mMovieItems.get(DetailActivity.mPostion);

    public DetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ImageView thumbnail = (ImageView) view.findViewById(R.id.iv_thumbnail);
        TextView title = (TextView) view.findViewById(R.id.tv_title);
        TextView rating = (TextView) view.findViewById(R.id.tv_rating);
        TextView releaseDay = (TextView) view.findViewById(R.id.tv_releaseday);
        TextView overview = (TextView) view.findViewById(R.id.tv_overview);

        Drawable noImage = getContext().getResources().getDrawable(R.drawable.no_image);
        Picasso.with(getContext())
                .load(mMovieItem.getPosterPath())
                .placeholder(noImage)
                .error(noImage)
                .into(thumbnail);
        title.setText(mMovieItem.getTitle());
        rating.setText("Rating: " + mMovieItem.getVoteAverage() + "/10");
        releaseDay.setText("Release day: "+ mMovieItem.getReleaseDay());
        overview.setText(mMovieItem.getOverview());

        return view;
    }


}
