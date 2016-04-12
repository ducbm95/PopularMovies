package com.lazymonster.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.lazymonster.popularmovies.Data.MovieContract;
import com.lazymonster.popularmovies.Data.MovieDbHepler;

/**
 * Created by LazyMonster on 08/04/2016.
 */
public class TestDb extends AndroidTestCase {
    public void testDb() {
        getContext().deleteDatabase(MovieDbHepler.DB_NAME);

        MovieDbHepler dbHepler = new MovieDbHepler(getContext());
        SQLiteDatabase db = dbHepler.getWritableDatabase();

        assertEquals(true, db.isOpen());

        db.close();
    }

    public void testDbTrailer() {
        getContext().deleteDatabase(MovieDbHepler.DB_NAME);

        MovieDbHepler dbHepler = new MovieDbHepler(getContext());
        SQLiteDatabase db = dbHepler.getWritableDatabase();

        String[] column = {
                MovieContract.TrailerEntry.COLUMN_KEY,
                MovieContract.TrailerEntry.COLUMN_NAME,
                MovieContract.TrailerEntry.COLUMN_SITE,
                MovieContract.TrailerEntry.COLUMN_TYPE
        };

        ContentValues values = new ContentValues();
        values.put(MovieContract.TrailerEntry.COLUMN_KEY, "key");
        values.put(MovieContract.TrailerEntry.COLUMN_NAME, "name");
        values.put(MovieContract.TrailerEntry.COLUMN_SITE, "site");
        values.put(MovieContract.TrailerEntry.COLUMN_TYPE, "type");
        long id = db.insert(
                MovieContract.TrailerEntry.TABLE_NAME,
                null,
                values
        );

        Cursor c = db.query(MovieContract.TrailerEntry.TABLE_NAME,
                column,
                MovieContract.TrailerEntry._ID + " = " + id,
                null, null, null, null);
        boolean b = c.moveToFirst();
        assertEquals(b, true);
        String s = c.getString(c.getColumnIndex(MovieContract.TrailerEntry.COLUMN_KEY));
        assertEquals(s, "key");

        db.close();
    }



}
