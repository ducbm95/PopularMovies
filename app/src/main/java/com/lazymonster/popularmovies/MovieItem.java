package com.lazymonster.popularmovies;

/**
 * Created by LazyMonster on 01/04/2016.
 */
public class MovieItem {

    private String posterPath;
    private String overview;
    private String releaseDay;
    private String title;
    private double voteAverage;

    public MovieItem() {}

    public MovieItem(String posterPath, String overview, String releaseDay,
                     String title, double voteAverage) {
        this.posterPath = "http://image.tmdb.org/t/p/w185/" + posterPath;
        this.overview = overview;
        this.releaseDay = releaseDay;
        this.title = title;
        this.voteAverage = voteAverage;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDay() {
        return releaseDay;
    }

    public void setReleaseDay(String releaseDay) {
        this.releaseDay = releaseDay;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }
}
