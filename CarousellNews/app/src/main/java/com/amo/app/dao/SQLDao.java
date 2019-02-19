package com.amo.app.dao;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.orhanobut.logger.Logger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class SQLDao {
    protected SQLHelper mSqlHelper;
    protected HashMap<String, SQLiteDatabase> mDBs = new HashMap<>();
    private final static int FAIL = -1;
    private final static int FAIL_NULL_DB_OBJECT = -2;
    private final static int SUCCEED = 0;
    private static SQLDao mInstance;

    /**
     * gets a singleton dao object managing all databases of tables
     * should be called before create()
     */
    public static SQLDao getInstance() {
        Logger.d(">>>");
        if (mInstance == null ) {
            Logger.d("... going to create the singleton instance");
            mInstance = new SQLDao();
        }
        return mInstance;
    }

    /**
     * gets a singleton database object presenting the default database of table
     * should be called after create()
     */
    public SQLiteDatabase getDefaultSQLiteDatabase() {
        return mDBs.get(SQLHelper.DEFAULT_DATABASE_NAME);
    }

    /**
     * gets a singleton database object presenting the database of table
     * should be called after create()
     */
    public SQLiteDatabase getSQLiteDatabase(String table_name) {
        return mDBs.get(table_name);
    }

    /**
     * creates default table
     * return: sql database of table name
     */
    public SQLiteDatabase create(Context context) {
        Logger.d(">>> table name:" + SQLHelper.DEFAULT_DATABASE_NAME);
        if (mSqlHelper == null) {
            mSqlHelper = new SQLHelper(context);
        }
        SQLiteDatabase db = mSqlHelper.getWritableDatabase();
        if (mDBs.put(SQLHelper.DEFAULT_DATABASE_NAME, db) != null) {
            Logger.d("... mDBs previously contained a mapping in hash map");
        }
        return mDBs.get(SQLHelper.DEFAULT_DATABASE_NAME);
    }

    /**
     * creates custom table
     * return: sql database of table name
     */
    public SQLiteDatabase create(Context context, String sql_cmd, String table_name) {
        Logger.d(">>> table name:" + table_name);
        if (mSqlHelper == null) {
            mSqlHelper = new SQLHelper(context);
        }
        mSqlHelper.setTableName(table_name);
        mSqlHelper.setSQLCreateCmd(sql_cmd);
        SQLiteDatabase db = mSqlHelper.getWritableDatabase();
        if (mDBs.put(table_name, db) != null) {
            Logger.d("... mDBs previously contained a mapping in hash map");
        }
        return mDBs.get(table_name);
    }


    /**
     * creates custom table having columns named same as the name of model fields passed by class
     * return: sql database of table name
     */
    public SQLiteDatabase create(Context context, Class<?> clazz, String table_name) {
        Logger.d(">>> table name:" + table_name);
        if (mSqlHelper == null) {
            mSqlHelper = new SQLHelper(context);
        }
        mSqlHelper.setTableName(table_name);
        mSqlHelper.setSQLCreateCmd(clazz);
        SQLiteDatabase db = mSqlHelper.getWritableDatabase();
        if (mDBs.put(table_name, db) != null) {
            Logger.d("... mDBs previously contained a mapping in hash map");
        }
        return mDBs.get(table_name);
    }

    /**
     * queries default table
     */
    public Cursor query(String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        Logger.d(">>> table name:" + SQLHelper.DEFAULT_DATABASE_NAME);
        SQLiteDatabase db = mDBs.get(SQLHelper.DEFAULT_DATABASE_NAME);
        Cursor cursor;
        if (db == null) return null;
        cursor = db.query(SQLHelper.DEFAULT_DATABASE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
        return cursor;
    }

    /**
     * queries custom table
     */
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        Logger.d(">>> table name:" + table);
        SQLiteDatabase db = mDBs.get(table);
        Cursor cursor;
        if (db == null) return null;
        cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        return cursor;
    }

    /**
     * updates default table
     */
    public int update(ContentValues values, String whereClause, String[] whereArgs) {
        Logger.d(">>> table name:" + SQLHelper.DEFAULT_DATABASE_NAME);
        int num_affected;
        SQLiteDatabase db = mDBs.get(SQLHelper.DEFAULT_DATABASE_NAME);
        if (db == null) return FAIL_NULL_DB_OBJECT;
        num_affected = db.update(SQLHelper.DEFAULT_DATABASE_NAME, values, whereClause, whereArgs);
        return num_affected;
    }

    /**
     * updates custom table
     */
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        Logger.d(">>> table name:" + table);
        int num_affected;
        SQLiteDatabase db = mDBs.get(table);
        if (db == null) return FAIL_NULL_DB_OBJECT;
        num_affected = db.update(table, values, whereClause, whereArgs);
        return num_affected;
    }

    /**
     * inserts default table
     */
    public long insert(String nullColumnHack, ContentValues values) {
        Logger.d(">>> table name:" + SQLHelper.DEFAULT_DATABASE_NAME);
        long new_row_id;
        SQLiteDatabase db = mDBs.get(SQLHelper.DEFAULT_DATABASE_NAME);
        if (db == null) return FAIL_NULL_DB_OBJECT;
        new_row_id = db.insert(SQLHelper.DEFAULT_DATABASE_NAME, nullColumnHack, values);
        return new_row_id;
    }

    /**
     * inserts custom table
     */
    public long insert(String table, String nullColumnHack, ContentValues values) {
        Logger.d(">>> table name:" + table);
        long new_row_id;
        SQLiteDatabase db = mDBs.get(table);
        if (db == null) return FAIL_NULL_DB_OBJECT;
        new_row_id = db.insert(table, nullColumnHack, values);
        return new_row_id;
    }

    /**
     * deletes default table
     */
    public int delete(String whereClause, String[] whereArgs) {
        Logger.d(">>> table name:" + SQLHelper.DEFAULT_DATABASE_NAME);
        int num_affected;
        SQLiteDatabase db = mDBs.get(SQLHelper.DEFAULT_DATABASE_NAME);
        if (db == null) return FAIL_NULL_DB_OBJECT;
        num_affected = db.delete(SQLHelper.DEFAULT_DATABASE_NAME, whereClause, whereArgs);
        return num_affected;
    }

    /**
     * deletes default table
     */
    public int delete(String table, String whereClause, String[] whereArgs) {
        Logger.d(">>> table name:" + table);
        int num_affected;
        SQLiteDatabase db = mDBs.get(table);
        if (db == null) return FAIL_NULL_DB_OBJECT;
        num_affected = db.delete(table, whereClause, whereArgs);
        return num_affected;
    }

    /**
     * marshals a data model to a content value
     */
    public int parseModelToContentValues(Object model, ContentValues values) throws IllegalAccessException {
        Logger.d(">>>");
        if (values.size() > 0) {
            values.clear();
        }
        Class<?> clazz = model.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Class<?> fieldType;
        String fieldName;
        for (Field field : fields) {
            if (!field.isAccessible()) field.setAccessible(true);
            fieldType = field.getType();
            fieldName = field.getName();
            /**
             * ignores compiler-created field that never appears in source code
             */
            if (field.isSynthetic()) {
                Logger.e("!!! ignores synthetic field:" +  field.getName());
                continue;
            }
            if (field.get(model) == null) {
                Logger.e("!!! field value of " + fieldName + "is null");
                continue;
            }
            if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
                Logger.d("... field value of " + fieldName + "is " + field.getInt(model));
                values.put(fieldName, field.getInt(model));
            } else if (fieldType.equals(String.class)) {
                Logger.d("... field value of " + fieldName + "is " + (String) field.get(model));
                values.put(fieldName, (String) field.get(model));
            } else if (fieldType.equals(byte[].class)) {
                values.put(fieldName, (byte[]) field.get(model));
            }
        }
        return SUCCEED;
    }

    /**
     * de-marshals a cursor to the result set returned by a database query to a data model
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int parseCursorToModels(Cursor cursor, List models, Class<?> clazzModelType) throws InstantiationException, IllegalAccessException {
        Logger.d(">>>");
        Field[] fields = clazzModelType.getDeclaredFields();
        Class<?> fieldType;
        String fieldName;
        if (cursor.getCount() == 0) {
            Logger.e("!!! numbers of rows of this cursor is 0");
            return FAIL;
        }
        while (cursor.moveToNext()) {
            Logger.d("... cursor at " + cursor.getPosition());
            Object model = clazzModelType.newInstance();
            for (Field field : fields) {
                /**
                 * ignores compiler-created field that never appears in source code
                 */
                if (field.isSynthetic()) {
                    Logger.e("!!! ignores synthetic field:" +  field.getName());
                    continue;
                }
                if (!field.isAccessible()) field.setAccessible(true);
                fieldType = field.getType();
                fieldName = field.getName();
                if (cursor.isNull(cursor.getColumnIndex(fieldName))) {
                    Logger.e("!!! value of cursor at row " + cursor.getPosition() + " in column(" + fieldName + ") is null");
                    continue;
                }
                if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
                    Logger.d("... value of cursor at row " + cursor.getPosition() + " in column(" + fieldName + ") is " + cursor.getInt(cursor.getColumnIndex(fieldName)));
                    field.set(model, cursor.getInt(cursor.getColumnIndex(fieldName)));
                } else if (fieldType.equals(String.class)) {
                    Logger.d("... value of cursor at row " + cursor.getPosition() + " in column(" + fieldName + ") is " + cursor.getString(cursor.getColumnIndex(fieldName)));
                    field.set(model, cursor.getString(cursor.getColumnIndex(fieldName)));
                } else if (fieldType.equals(byte[].class)) {
                    field.set(model, cursor.getBlob(cursor.getColumnIndex(fieldName)));
                }
            }
            models.add(model);
        }
        return SUCCEED;
    }
}
