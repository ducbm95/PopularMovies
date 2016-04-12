package com.lazymonster.popularmovies.Item;

/**
 * Created by LazyMonster on 07/04/2016.
 */
public class ReviewItem {

    private String author;
    private String content;

    public ReviewItem(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
