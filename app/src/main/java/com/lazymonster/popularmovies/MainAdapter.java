package com.lazymonster.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lazymonster.popularmovies.Item.MovieItem;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by LazyMonster on 01/04/2016.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private static final String DETAILFRAGMENT_TAG = "detail_fragment_tag";

    private List<MovieItem> mMovieItems;
    private Context context;

    public MainAdapter(List<MovieItem> movieItems) {
        this.mMovieItems = movieItems;
    }

    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_movie, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MainAdapter.ViewHolder holder, final int position) {
        Drawable noImage = context.getResources().getDrawable(R.drawable.no_image);

        Picasso.with(context)
                .setIndicatorsEnabled(true);
        Picasso.with(context)
                .load(mMovieItems.get(position).getPosterPath())
                .placeholder(noImage)
                .error(noImage)
                .into(holder.image);

        int width = getScreenWidth();
        if (!MainActivity.mTwoPane) {
            holder.image.getLayoutParams().width = width / 2;
        } else {
            holder.image.getLayoutParams().width = width / 6;
        }
        holder.image.getLayoutParams().height = (int) (1.5 * holder.image.getLayoutParams().width);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.mTwoPane) {
                    Intent i = new Intent(context, DetailActivity.class);
                    i.putExtra("position", position);
                    context.startActivity(i);
                } else {
                    DetailActivity.mPostion = position;
                    ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_detail, new DetailFragment(), DETAILFRAGMENT_TAG)
                            .commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovieItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }

    private int getScreenWidth() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        return width;
    }
}
