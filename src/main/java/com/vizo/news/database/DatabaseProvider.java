package com.vizo.news.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Custom content provider for Vizo Database
 *
 * @author nine3_marks
 */
public class DatabaseProvider extends ContentProvider {

    public static final String AUTHORITY = "com.vizo.news.provider";
    public static final String VERSION = "v1";

    public static final Uri CATEGORIES_URI = Uri.parse("content://" + AUTHORITY
            + "/" + VERSION + "/" + DatabaseConstants.CATEGORIES_TABLE + "/");
    public static final Uri GLANCES_URI = Uri.parse("content://" + AUTHORITY
            + "/" + VERSION + "/" + DatabaseConstants.GLANCES_TABLE + "/");
    public static final Uri GLANCED_URI = Uri.parse("content://" + AUTHORITY
            + "/" + VERSION + "/" + DatabaseConstants.GLANCED_TABLE + "/");

    private static final int CATEGORIES_ENUM = 0;
    private static final int GLANCES_ENUM = 1;
    private static final int GLANCED_ENUM = 2;

    private static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, VERSION + "/"
                + DatabaseConstants.CATEGORIES_TABLE + "/", CATEGORIES_ENUM);
        uriMatcher.addURI(AUTHORITY, VERSION + "/"
                + DatabaseConstants.GLANCES_TABLE + "/", GLANCES_ENUM);
        uriMatcher.addURI(AUTHORITY, VERSION + "/"
                + DatabaseConstants.GLANCED_TABLE + "/", GLANCED_ENUM);
    }

    private VizoDatabase databaseHelper;

    @Override
    public boolean onCreate() {
        databaseHelper = new VizoDatabase(getContext()
                .getApplicationContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case CATEGORIES_ENUM:
                return db.delete(DatabaseConstants.CATEGORIES_TABLE, selection,
                        selectionArgs);
            case GLANCES_ENUM:
                return db.delete(DatabaseConstants.GLANCES_TABLE, selection,
                        selectionArgs);
            case GLANCED_ENUM:
                return db.delete(DatabaseConstants.GLANCED_TABLE, selection,
                        selectionArgs);
            default:
                return 0;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Uri resultUri = null;

        long recordId = -1;
        switch (uriMatcher.match(uri)) {
            case CATEGORIES_ENUM:
                recordId = db.insertWithOnConflict(DatabaseConstants.CATEGORIES_TABLE,
                        null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case GLANCES_ENUM:
                recordId = db.insertWithOnConflict(DatabaseConstants.GLANCES_TABLE,
                        null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case GLANCED_ENUM:
                recordId = db.insertWithOnConflict(DatabaseConstants.GLANCED_TABLE,
                        null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case UriMatcher.NO_MATCH:
                Log.e(this.getClass().getName(),
                        "No tables found for Insert() INTO: " + uri.toString());
                recordId = -1;
                break;
        }
        if (recordId == -1) {
            // when fails to insert
            Log.e(this.getClass().getName(),
                    "Failed to execute: Insert() INTO: " + uri.toString());
        }

        return resultUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = databaseHelper.getReadableDatabase();

        switch (uriMatcher.match(uri)) {
            case CATEGORIES_ENUM:
                return db.query(DatabaseConstants.CATEGORIES_TABLE, projection,
                        selection, selectionArgs, null, null, sortOrder);
            case GLANCES_ENUM:
                return db.query(DatabaseConstants.GLANCES_TABLE, projection,
                        selection, selectionArgs, null, null, sortOrder);
            case GLANCED_ENUM:
                return db.query(DatabaseConstants.GLANCED_TABLE, projection,
                        selection, selectionArgs, null, null, sortOrder);
            case UriMatcher.NO_MATCH:
                Log.e(this.getClass().getName(), "No tables matching query() for: "
                        + uri.toString());
                return null;
        }
        return null;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        final SQLiteDatabase db = databaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case CATEGORIES_ENUM:
                return db.update(DatabaseConstants.CATEGORIES_TABLE, values,
                        selection, selectionArgs);
            case GLANCES_ENUM:
                return db.update(DatabaseConstants.GLANCES_TABLE, values,
                        selection, selectionArgs);
            case GLANCED_ENUM:
                return db.update(DatabaseConstants.GLANCED_TABLE, values,
                        selection, selectionArgs);
            default:
                Log.e(this.getClass().getName(),
                        "No tables matching update() for: " + uri.toString());
                return -1;
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}