package com.futureskyltd.app.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.futureskyltd.app.fantacy.R;
import com.futureskyltd.app.utils.Constants;

/**
 * Created by hitasoft on 6/6/17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "Database";
    private static final int DATABASE_VERSION = 2;

    // Table Names
    private static final String TABLE_ITEMS = "items";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_STORES = "stores";

    private static final String TAG = "DatabaseHandler";

    private static DatabaseHandler sInstance;

    public static synchronized DatabaseHandler getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHandler(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private DatabaseHandler(Context context) {
        super(context, context.getString(R.string.app_name) + DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        /*Latest Change- Include new Column "Constants.TAG_LIKE_COUNT" in ITEMS_TABLE*/
        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS +
                "(" +
                Constants.TAG_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                Constants.TAG_ITEM_ID + " TEXT NOT NULL UNIQUE," +
                Constants.TAG_LIKED + " TEXT," +
                Constants.TAG_LIKE_COUNT + " TEXT," +
                Constants.TAG_REPORT + " TEXT," +
                Constants.TAG_SHARE_USER + " TEXT" +
                ")";

        db.execSQL(CREATE_ITEMS_TABLE);

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS +
                "(" +
                Constants.TAG_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                Constants.TAG_STATUS + " TEXT" +
                ")";

        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_STORES_TABLE = "CREATE TABLE " + TABLE_STORES +
                "(" +
                Constants.TAG_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                Constants.TAG_STATUS + " TEXT" +
                ")";

        db.execSQL(CREATE_STORES_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STORES);
            onCreate(db);
        }
    }

    // Insert a item into the database
    public void addItemDetails(String itemId, String liked,String likeCount, String report, String shareUser) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            boolean exists = isItemIdExist(db, itemId);
            ContentValues values = new ContentValues();
            values.put(Constants.TAG_ITEM_ID, itemId);
            values.put(Constants.TAG_LIKED, liked);
            values.put(Constants.TAG_LIKE_COUNT, likeCount);
            values.put(Constants.TAG_REPORT, report);
            values.put(Constants.TAG_SHARE_USER, shareUser);

            if (!exists) {
                db.insert(TABLE_ITEMS, null, values);
            } else {
                db.update(TABLE_ITEMS, values, Constants.TAG_ITEM_ID + " =? ",
                        new String[]{itemId});
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isItemIdExist(SQLiteDatabase db, String itemId) {
        long line = DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + TABLE_ITEMS + " WHERE " + Constants.TAG_ITEM_ID + "=?",
                new String[]{itemId});
        return line > 0;
    }

    public String getItemDetails(String itemId, String key){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, new String[] { key }, Constants.TAG_ITEM_ID + "=?",
                new String[] { itemId }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        return cursor.getString(0);
    }

    public int updateItemDetails(String itemId, String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(key, value);

        // updating row
        return db.update(TABLE_ITEMS, values, Constants.TAG_ITEM_ID + " = ?",
                new String[] { itemId });
    }

    public void getAllItems() {

        String selectQuery = "SELECT  * FROM " + TABLE_ITEMS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Log.v("Items", "Id="+cursor.getString(0)+"ItemId="+cursor.getString(1)+" Liked="+cursor.getString(2)+" Report="+cursor.getString(3)+" Share="+cursor.getString(4));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    // Delete all posts and users in the database
    public void deleteAllItems() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_ITEMS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all posts and users");
        } finally {
            db.endTransaction();
        }
    }

    // Insert a user into the database
    public void addUserDetails(String id, String status) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            boolean exists = isUserIdExist(db, id);
            ContentValues values = new ContentValues();
            values.put(Constants.TAG_ID, id);
            values.put(Constants.TAG_STATUS, status);

            if (!exists) {
                db.insert(TABLE_USERS, null, values);
            } else {
                db.update(TABLE_USERS, values, Constants.TAG_ID + " =? ",
                        new String[]{id});
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isUserIdExist(SQLiteDatabase db, String id) {
        long line = DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + Constants.TAG_ID + "=?",
                new String[]{id});
        return line > 0;
    }

    public int updateUserDetails(String id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.TAG_STATUS, status);

        // updating row
        return db.update(TABLE_USERS, values, Constants.TAG_ID + " = ?",
                new String[] { id });
    }

    public String getUserDetails(String id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS, new String[] { Constants.TAG_STATUS }, Constants.TAG_ID + "=?",
                new String[] { id }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        return cursor.getString(0);
    }

    // Insert a user into the database
    public void addStoreDetails(String id, String status) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            boolean exists = isStoreIdExist(db, id);
            ContentValues values = new ContentValues();
            values.put(Constants.TAG_ID, id);
            values.put(Constants.TAG_STATUS, status);

            if (!exists) {
                db.insert(TABLE_STORES, null, values);
            } else {
                db.update(TABLE_STORES, values, Constants.TAG_ID + " =? ",
                        new String[]{id});
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isStoreIdExist(SQLiteDatabase db, String id) {
        long line = DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + TABLE_STORES + " WHERE " + Constants.TAG_ID + "=?",
                new String[]{id});
        return line > 0;
    }

    public int updateStoreDetails(String id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.TAG_STATUS, status);

        // updating row
        return db.update(TABLE_STORES, values, Constants.TAG_ID + " = ?",
                new String[] { id });
    }

    public String getStoreDetails(String id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_STORES, new String[] { Constants.TAG_STATUS }, Constants.TAG_ID + "=?",
                new String[] { id }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        return cursor.getString(0);
    }
}
