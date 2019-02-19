package com.amo.app.dao;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.orhanobut.logger.Logger;

public class CardsProvider extends ContentProvider {
    private final static String CONTENT_PROVIDER_URI_AUTHORITY = "com.amo.app.dao.CardsProvider";
    private final static String CONTENT_PROVIDER_URI_TABLE = SQLHelper.DEFAULT_DATABASE_NAME;
    private final static String CONTENT_PROVIDER_MIME_TYPE = "vnd.android.cursor.dir" + "/" + "app.cards";
    private final static int CONTENT_PROVIDER_URI_CODE_ROOT = 0;
    private final static int CONTENT_PROVIDER_URI_CODE_CARDS = 1;
    private final static String CONTENT_PROVIDER_URI_STRING = "content://" + CONTENT_PROVIDER_URI_AUTHORITY + "/" + CONTENT_PROVIDER_URI_TABLE;
    private final static UriMatcher mMatcher = new UriMatcher(CONTENT_PROVIDER_URI_CODE_ROOT);
    public final static Uri CONTENT_PROVIDER_URI = Uri.parse(CONTENT_PROVIDER_URI_STRING);
    private SQLDao mDao;
    static {
        mMatcher.addURI(CONTENT_PROVIDER_URI_AUTHORITY, CONTENT_PROVIDER_URI_TABLE, CONTENT_PROVIDER_URI_CODE_CARDS);
    }

    /**
     * returns MIME type of data of intent represented by URI identified with this content provider
     * may be used for intent-filter
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        Logger.d(">>>");
        String type = null;
        switch (mMatcher.match(uri)) {
            case CONTENT_PROVIDER_URI_CODE_CARDS:
                type = CONTENT_PROVIDER_MIME_TYPE;
                break;
            default:
                break;
        }
        return type;
    }

    @Override
    public boolean onCreate() {
        Logger.d(">>>");
        Log.d("APP", "[CardsProvider#onCreate()]>>>"); //invoked prior to MainActivity#onCreate()
        boolean ret = false;
        if (mDao == null ) {
            mDao = SQLDao.getInstance();
        }
        try {
            if (mDao.create(getContext(), Class.forName("com.amo.app.entity.Card"), CONTENT_PROVIDER_URI_TABLE) != null) ret = true;
        } catch (ClassNotFoundException e) {
            Logger.e("!!! did not find class of Card");
            e.printStackTrace();
        }
        return ret;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Logger.d(">>>");
        if (mMatcher.match(uri) != CONTENT_PROVIDER_URI_CODE_CARDS) {
            Logger.e("!!! matcher failed");
            return null;
        }
        if (mDao == null) {
            Logger.e("!!! dao is null");
            return null;
        }
        /**
         * equals to call:
         * SQLiteDatabase db = mDao.getSQLiteDatabase(CONTENT_PROVIDER_URI_TABLE);
         * Cursor cursor = db.query(CONTENT_PROVIDER_URI_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
         */
        Cursor cursor = mDao.query(CONTENT_PROVIDER_URI_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Logger.d(">>>");
        int num_affected = 0;
        if (mMatcher.match(uri) != CONTENT_PROVIDER_URI_CODE_CARDS) {
            Logger.e("!!! matcher failed");
            return num_affected;
        }
        if (mDao == null) {
            Logger.e("!!! dao is null");
            return num_affected;
        }
        num_affected = mDao.update(CONTENT_PROVIDER_URI_TABLE, values, selection, selectionArgs);
        return num_affected;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Logger.d(">>>");
        Uri new_row_uri = null;
        long new_row_id;
        if (mMatcher.match(uri) != CONTENT_PROVIDER_URI_CODE_CARDS) {
            Logger.e("!!! matcher failed");
            return new_row_uri;
        }
        if (mDao == null) {
            Logger.e("!!! dao is null");
            return new_row_uri;
        }
        new_row_id = mDao.insert(CONTENT_PROVIDER_URI_TABLE, "_id", values);
        new_row_uri = ContentUris.withAppendedId(CONTENT_PROVIDER_URI, new_row_id);
        getContext().getContentResolver().notifyChange(new_row_uri, null);
        return new_row_uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Logger.d(">>>");
        int num_affected = 0;
        if (mMatcher.match(uri) != CONTENT_PROVIDER_URI_CODE_CARDS) {
            Logger.e("!!! matcher failed");
            return num_affected;
        }
        if (mDao == null) {
            Logger.e("!!! dao is null");
            return num_affected;
        }
        num_affected = mDao.delete(CONTENT_PROVIDER_URI_TABLE, selection, selectionArgs);
        return num_affected;
    }
}
