package com.lazymonster.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lazymonster.popularmovies.Item.MovieItem;
import com.lazymonster.popularmovies.Item.ReviewItem;
import com.lazymonster.popularmovies.Item.TrailerItem;
import com.squareup.picasso.Picasso;

/**
 * Created by LazyMonster on 07/04/2016.
 */
public class DetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_CONTENT_DETAIL = 0;
    private static final int VIEW_TYPE_TRAILER = 1;
    private static final int VIEW_TYPE_REVIEW = 2;
    private static final int VIEW_TYPE_HEADER = 3;

    private MovieItem mMovieItem;
    private Context mContext;

    public DetailAdapter(MovieItem movieItem) {
        this.mMovieItem = movieItem;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        RecyclerView.ViewHolder vh = null;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        switch (viewType) {
            case VIEW_TYPE_CONTENT_DETAIL:
                View view1 = inflater.inflate(R.layout.content_detail, parent, false);
                vh = new ViewHolder1(view1);
                break;
            case VIEW_TYPE_TRAILER:
                View view2 = inflater.inflate(R.layout.item_trailer, parent, false);
                vh = new ViewHolder2(view2);
                break;
            case VIEW_TYPE_REVIEW:
                View view3 = inflater.inflate(R.layout.item_review, parent, false);
                vh = new ViewHolder3(view3);
                break;
            case VIEW_TYPE_HEADER:
                View view4 = inflater.inflate(R.layout.item_header, parent, false);
                vh = new ViewHolder4(view4);
                break;
        }
        return vh;
    }

    @Override
    public int getItemCount() {
        // normally, if mMovieItem do not have any trailer and review
        // recyclerView just have 1 item content_detail
        int numItemCount = 1;

        // if mMovieItem have a list of Trailer, numItemCount += trailersSize + 1
        // include trailer's header and list of trailer
        if (mMovieItem.getTrailerItems() != null)
            numItemCount += mMovieItem.getTrailerItems().size() + 1;

        // if mMovieItem have a list of Review, numItemCount += reviewsSize + 1
        // include review's header and list of review
        if (mMovieItem.getReviewItems() != null)
            numItemCount += mMovieItem.getReviewItems().size() + 1;

        return numItemCount;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            ViewHolder1 vh1 = (ViewHolder1) holder;
            Drawable noImage = mContext.getResources().getDrawable(R.drawable.no_image);

            Picasso.with(mContext)
                    .load(mMovieItem.getPosterPath())
                    .placeholder(noImage)
                    .error(noImage)
                    .into(vh1.thumbnail);
            vh1.title.setText(mMovieItem.getTitle());
            vh1.rating.setText("Rating: " + mMovieItem.getVoteAverage() + "/10");
            vh1.releaseDay.setText("Release day: " + mMovieItem.getReleaseDay());
            vh1.overview.setText(mMovieItem.getOverview());
            return;
        }

        if (mMovieItem.getTrailerItems() != null) {
            if (position == 1) {
                ViewHolder4 vh4 = (ViewHolder4) holder;
                vh4.header.setText(mContext.getResources().getString(R.string.header_trailer));
            } else if (position < 2 + mMovieItem.getTrailerItems().size()) {
                // handle a list of trailer
                int currPos = position - 2;
                final TrailerItem trailerItem = mMovieItem.getTrailerItems().get(currPos);
                ViewHolder2 vh2 = (ViewHolder2) holder;
                vh2.title.setText(trailerItem.getName());
                vh2.playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContext.startActivity(new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://www.youtube.com/watch?v=" + trailerItem.getKey()))
                        );
                    }
                });

            } else if (position == 2 + mMovieItem.getTrailerItems().size()) {
                ViewHolder4 vh4 = (ViewHolder4) holder;
                vh4.header.setText(mContext.getResources().getString(R.string.header_review));
            } else if (position < 3 + mMovieItem.getTrailerItems().size() + mMovieItem.getReviewItems().size()) {
                // handle a list of review
                bindReiviewType(holder, position - 3 - mMovieItem.getTrailerItems().size());
            }
        } else {
            if (position == 1) {
                ViewHolder4 vh4 = (ViewHolder4) holder;
                vh4.header.setText(mContext.getResources().getString(R.string.header_review));
            } else if (position < 2 + mMovieItem.getReviewItems().size()) {
                // handle a list of review
                bindReiviewType(holder, position - 2);
            }
        }
    }

    private void bindReiviewType(RecyclerView.ViewHolder holder, int position) {
        ReviewItem reviewItem = mMovieItem.getReviewItems().get(position);
        ViewHolder3 vh3 = (ViewHolder3) holder;
        vh3.authorName.setText(reviewItem.getAuthor());
        vh3.review.setText(reviewItem.getContent());
    }

    @Override
    public int getItemViewType(int position) {
        // if position is 0, view is content_detail
        if (position == 0)
            return VIEW_TYPE_CONTENT_DETAIL;


        if (mMovieItem.getTrailerItems() != null) {
            // if position is 1, view is header of trailer list.
            if (position == 1)
                return VIEW_TYPE_HEADER;

            // if postion < 2 + mMovieItem.getTrailerItems().size() and postion >= 2,
            // view is trailer item
            if (position < 2 + mMovieItem.getTrailerItems().size())
                return VIEW_TYPE_TRAILER;

            // if position is 2 + mMovieItem.getTrailerItems().size(), view is header of review list.
            if (position == 2 + mMovieItem.getTrailerItems().size())
                return VIEW_TYPE_HEADER;

            // if postion < 3 + mMovieItem.getTrailerItems().size() + mMovieItem.getReviewItems().size()
            // and position >= 3 + mMovieItem.getTrailerItems().size(), view is review item
            if (mMovieItem.getTrailerItems() != null)
                if (position < 3 + mMovieItem.getTrailerItems().size() + mMovieItem.getReviewItems().size())
                    return VIEW_TYPE_REVIEW;
        } else {
            // if position is 1, view is header of review list.
            if (position == 1)
                return VIEW_TYPE_HEADER;

            // if postion < 2 + mMovieItem.getReviewItems().size()
            // and position >= 2, view is review item
            if (mMovieItem.getTrailerItems() != null)
                if (position < 2 + mMovieItem.getReviewItems().size())
                    return VIEW_TYPE_REVIEW;
        }

        return position;
    }

    /**
     * Create view for content detail such as: movie photo, movie title, rating, overview...
     */
    class ViewHolder1 extends RecyclerView.ViewHolder {

        public ImageView thumbnail;
        public TextView title;
        public TextView rating;
        public TextView releaseDay;
        public TextView overview;

        public ViewHolder1(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.iv_thumbnail);
            title = (TextView) itemView.findViewById(R.id.tv_title);
            rating = (TextView) itemView.findViewById(R.id.tv_rating);
            releaseDay = (TextView) itemView.findViewById(R.id.tv_releaseday);
            overview = (TextView) itemView.findViewById(R.id.tv_overview);
        }
    }

    /**
     * Create view for a trailer itemView
     */
    class ViewHolder2 extends RecyclerView.ViewHolder {

        public TextView title;
        public Button playBtn;

        public ViewHolder2(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tv_trailer);
            playBtn = (Button) itemView.findViewById(R.id.btn_play_trailer);
        }
    }

    /**
     * Create view for a review itemView
     */
    class ViewHolder3 extends RecyclerView.ViewHolder {

        public ImageView author;
        public TextView authorName;
        public TextView review;

        public ViewHolder3(View itemView) {
            super(itemView);
            author = (ImageView) itemView.findViewById(R.id.iv_author);
            authorName = (TextView) itemView.findViewById(R.id.tv_author_name);
            review = (TextView) itemView.findViewById(R.id.tv_review);
        }
    }

    /**
     * Create view for a header itemView
     * Example: header for trailer list, header for review list
     */
    class ViewHolder4 extends RecyclerView.ViewHolder {

        public TextView header;

        public ViewHolder4(View itemView) {
            super(itemView);
            header = (TextView) itemView.findViewById(R.id.tv_header);
        }
    }
}
