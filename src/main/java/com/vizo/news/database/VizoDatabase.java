package com.vizo.news.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Custom database helper class
 *
 * @author nine3_marks
 */
public class VizoDatabase extends SQLiteOpenHelper {

    private final String TAG = "VizoDatabase";

    public final static String DB_NAME = "vizo.news.db";
    private final static int VERSION = 1;
    private final String TEXT = " text";
    private final String INTEGER = " integer";

    public VizoDatabase(Context applicationContext) {
        super(applicationContext, DB_NAME, null, VERSION);
        Log.d(TAG, "constructor()...");
    }

    private final String CATEGORIES_SQL = "CREATE TABLE IF NOT EXISTS "
            + DatabaseConstants.CATEGORIES_TABLE + " ("
            + DatabaseConstants.CATEGORY_TABLE_ID + TEXT + " NOT NULL, "
            + DatabaseConstants.CATEGORY_NAME + TEXT + " NOT NULL, "
            + DatabaseConstants.CATEGORY_IMAGE_URL + TEXT + ")";

    private final String GLANCES_SQL = "CREATE TABLE IF NOT EXISTS "
            + DatabaseConstants.GLANCES_TABLE + " ("
            + DatabaseConstants.GLANCE_ID + TEXT + " NOT NULL, "
            + DatabaseConstants.CATEGORY_ID + TEXT + " NOT NULL, "
            + DatabaseConstants.TITLE + TEXT + " NOT NULL, "
            + DatabaseConstants.DESCRIPTION + TEXT + " NOT NULL, "
            + DatabaseConstants.IMAGE_URL + TEXT + " NOT NULL, "
            + DatabaseConstants.IMAGE_SUB_URL + TEXT + ", "
            + DatabaseConstants.IMAGE_CREDIT + TEXT + ", "
            + DatabaseConstants.MODIFIED_DATE + TEXT + " NOT NULL, "
            + DatabaseConstants.LANG + TEXT + " NOT NULL, "
            + DatabaseConstants.SYNC_STATE + TEXT + ", "
            + DatabaseConstants.STATE_OF_DAY + INTEGER + ", "
            + DatabaseConstants.IS_FAVORITE + INTEGER + ")";

    private final String GLANCED_SQL = "CREATE TABLE IF NOT EXISTS "
            + DatabaseConstants.GLANCED_TABLE + " ("
            + DatabaseConstants.GLANCE_ID + TEXT + " NOT NULL, "
            + DatabaseConstants.CATEGORY_ID + TEXT + " NOT NULL, "
            + DatabaseConstants.TITLE + TEXT + " NOT NULL, "
            + DatabaseConstants.DESCRIPTION + TEXT + " NOT NULL, "
            + DatabaseConstants.IMAGE_URL + TEXT + " NOT NULL, "
            + DatabaseConstants.IMAGE_SUB_URL + TEXT + ", "
            + DatabaseConstants.IMAGE_CREDIT + TEXT + ", "
            + DatabaseConstants.MODIFIED_DATE + TEXT + " NOT NULL, "
            + DatabaseConstants.LANG + TEXT + " NOT NULL, "
            + DatabaseConstants.SYNC_STATE + TEXT + ", "
            + DatabaseConstants.STATE_OF_DAY + INTEGER + ", "
            + DatabaseConstants.IS_FAVORITE + INTEGER + ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate() - Creating Database IF NOT EXISTS....");
        db.execSQL(CATEGORIES_SQL);
        db.execSQL(GLANCES_SQL);
        db.execSQL(GLANCED_SQL);
    }

    /**
     * Drops and recreates all tables
     */
    public void dropAndRecreateTables() {
        Log.i(TAG, "dropAndRecreateTables().....");
        SQLiteDatabase db = getWritableDatabase();

        String drop = "DROP TABLE IF EXISTS ";

        db.execSQL(drop + DatabaseConstants.CATEGORIES_TABLE);
        db.execSQL(drop + DatabaseConstants.GLANCES_TABLE);
        db.execSQL(drop + DatabaseConstants.GLANCED_TABLE);

        onCreate(db);

        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int previousVersion,
                          int nextVersion) {
        dropAndRecreateTables();
    }

}
