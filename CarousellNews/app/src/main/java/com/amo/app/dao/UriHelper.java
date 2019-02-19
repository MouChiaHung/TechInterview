package com.amo.app.dao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.provider.DocumentFile;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UriHelper {
    private static UriHelper INSTANCE = new UriHelper();

    public static UriHelper getInstance() {
        return INSTANCE;
    }

    @SuppressLint("SimpleDateFormat")
    public String generateFileNameBasedOnTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_hhmmss").format(new Date()) + ".jpeg";
    }

    /**
     * if uri.getScheme.equals("content"), open it with a content resolver.
     * if the uri.Scheme.equals("file"), open it using normal file methods.
     */
    public File toFile(Uri uri) {
        if (uri == null) return null;
        Logger.d(">>> uri path:" + uri.getPath());
        Logger.d(">>> uri string:" + uri.toString());
        return new File(uri.getPath());
    }

    public DocumentFile toDocumentFile(Uri uri) {
        if (uri == null) return null;
        Logger.d(">>> uri path:" + uri.getPath());
        Logger.d(">>> uri string:" + uri.toString());
        return DocumentFile.fromFile(new File(uri.getPath()));
    }

    public Uri toUri(File file) {
        if (file == null) return null;
        Logger.d(">>> file path:" + file.getAbsolutePath());
        return Uri.fromFile(file); //returns an immutable URI reference representing the file
    }

    public String getPath(Uri uri, Context context) {
        if (uri == null) return null;
        if (uri.getScheme() == null) return null;
        Logger.d(">>> uri path:" + uri.getPath());
        Logger.d(">>> uri string:" + uri.toString());
        String path;
        if (uri.getScheme().equals("content")) {
            //Cursor cursor = context.getContentResolver().query(uri, new String[] {MediaStore.Images.ImageColumns.DATA}, null, null, null);
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) {
                Logger.e("!!! cursor is null");
                return null;
            }
            if (cursor.getCount() >= 0) {
                Logger.d("... the numbers of rows:" + cursor.getCount()
                            + "and the numbers of columns:" + cursor.getColumnCount());
                if (cursor.isBeforeFirst()) {
                    while (cursor.moveToNext()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i<cursor.getColumnCount(); i++) {
                            stringBuilder.append("... iterating cursor.getString(" + i +"(" + cursor.getColumnName(i) + ")):" + cursor.getString(i));
                            stringBuilder.append("\n");
                        }
                        Logger.d(stringBuilder.toString());
                    }
                } else {
                    cursor.moveToFirst();
                    do {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i<cursor.getColumnCount(); i++) {
                            stringBuilder.append("... iterating cursor.getString(" + i +"(" + cursor.getColumnName(i) + ")):" + cursor.getString(i));
                            stringBuilder.append("\n");
                        }
                        Logger.d(stringBuilder.toString());
                    } while (cursor.moveToNext());
                }
                path = uri.getPath();
                cursor.close();
                Logger.d("... content scheme:" + uri.getScheme() + "  and return:" + path);
                return path;
            } else {
                path = uri.getPath();
                Logger.d("... content scheme:" + uri.getScheme()
                        + " but the numbers of rows in the cursor is < 0:" + cursor.getCount()
                        + "  and return:" + path);
                return path;
            }
        } else {
            path = uri.getPath();
            Logger.d("... not content scheme:" + uri.getScheme() + "  and return:" + path);
            return path;
        }
    }

    public String getFileName(Uri uri, Context context) {
        if (uri == null) return null;
        if (uri.getScheme() == null) return null;
        Logger.d(">>> uri path:" + uri.getPath());
        Logger.d(">>> uri string:" + uri.toString());
        String path;
        if (uri.getScheme().equals("content")) {
            //Cursor cursor = context.getContentResolver().query(uri, new String[] {MediaStore.Images.ImageColumns.DATA}, null, null, null);
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) {
                Logger.e("!!! cursor is null");
                return null;
            }
            if (cursor.getCount() >= 0) {
                Logger.d("... the numbers of rows:" + cursor.getCount()
                        + "and the numbers of columns:" + cursor.getColumnCount());
                if (cursor.isBeforeFirst()) {
                    while (cursor.moveToNext()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i<cursor.getColumnCount(); i++) {
                            stringBuilder.append("... iterating cursor.getString(" + i +"(" + cursor.getColumnName(i) + ")):" + cursor.getString(i));
                            stringBuilder.append("\n");
                        }
                        Logger.d(stringBuilder.toString());
                    }
                } else {
                    cursor.moveToFirst();
                    do {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i<cursor.getColumnCount(); i++) {
                            stringBuilder.append("... iterating cursor.getString(" + i +"(" + cursor.getColumnName(i) + ")):" + cursor.getString(i));
                            stringBuilder.append("\n");
                        }
                        Logger.d(stringBuilder.toString());
                    } while (cursor.moveToNext());
                }
                cursor.moveToFirst();
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                cursor.close();
                Logger.d("... content scheme:" + uri.getScheme() + "  and return:" + path);
                return path;
            } else {
                path = uri.getLastPathSegment();
                Logger.d("... content scheme:" + uri.getScheme()
                        + " but the numbers of rows in the cursor is < 0:" + cursor.getCount()
                        + "  and return:" + path);
                return path;
            }
        } else {
            path = uri.getLastPathSegment();
            Logger.d("... not content scheme:" + uri.getScheme() + "  and return:" + path);
            return path;
        }
    }
}
