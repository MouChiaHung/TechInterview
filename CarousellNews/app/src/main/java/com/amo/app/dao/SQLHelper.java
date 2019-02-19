package com.amo.app.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.orhanobut.logger.Logger;

import java.lang.reflect.Field;

public class SQLHelper extends SQLiteOpenHelper {
    public static String DEFAULT_DATABASE_NAME = "cards";
    public static String DEFAULT_DATABASE_CMD =
            "create table if not exists " +
                    DEFAULT_DATABASE_NAME +
                    "(" +
                    "_id integer primary key autoincrement, " +
                    "profile_photo text" + "," +
                    "profile_title text" + "," +
                    "profile_name text" + "," +
                    "article_title text" + "," +
                    "article_content text" + "," +
                    "article_count text" + ")";
    private static String DATABASE_CMD = DEFAULT_DATABASE_CMD;
    private static String DATABASE_NAME = DEFAULT_DATABASE_NAME;
    private static final int DATABASE_INIT_VERSION = 1;

    public SQLHelper(Context context) {
        super(context, DATABASE_NAME + ".db", null, DATABASE_INIT_VERSION);
        Logger.d(">>>");
    }

    /**
     * sets the name of table which is going to be created
     * called before setSQLCreateCmd()
     */
    public void setTableName(String table_name) {
        DATABASE_NAME = table_name;
    }

    /**
     * executes custom SQL create command
     */
    public void setSQLCreateCmd(String databaseCmd) {
        DATABASE_CMD = databaseCmd;
    }

    /**
     * executes auto-gen SQL create command to create a table having columns with the same names of model fields
     */
    public void setSQLCreateCmd(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        String fieldName;
        Class<?> fieldType;
        int fieldCount = fields.length;
        Logger.d("... fieldCount:" + fieldCount);
        int i = 0;
        DATABASE_CMD = "create table if not exists " + DATABASE_NAME +
                        "(" +
                        "_id integer primary key autoincrement, ";
        for (Field field : fields) {
            if (!field.isAccessible()) field.setAccessible(true);
            fieldName = field.getName();
            fieldType = field.getType();
            /**
             * ignores compiler-created field that never appears in source code
             */
            if (field.isSynthetic()) {
                Logger.e("!!! ignores synthetic field:" +  field.getName());
                continue;
            }
            if (fieldName.compareTo("") == 0) {
                Logger.e("!!! field name is empty");
                continue;
            }
            if (i == fieldCount - 1) {
                Logger.d("... field at " + (i+1) + "named " + fieldName);
                if (fieldType.equals(String.class)) DATABASE_CMD += fieldName + " text" + ")";
                else if (fieldType.equals(int.class) || fieldType.equals(Integer.class))  DATABASE_CMD += fieldName + " integer" + ")";
            } else {
                Logger.d("... field at " + (i+1) + "named " + fieldName);
                if (fieldType.equals(String.class)) DATABASE_CMD += fieldName + " text" + ",";
                else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) DATABASE_CMD += fieldName + " integer" + ",";
            }
            i++;
        }
        /**
         * fixes fieldCount is not as same as the defined
         */
        StringBuilder stringBuilder = new StringBuilder(DATABASE_CMD);
        stringBuilder.setCharAt(stringBuilder.lastIndexOf(","), ')');
        DATABASE_CMD = stringBuilder.toString();
        Logger.d("... now sql cmd is " + DATABASE_CMD);
    }

    public static String getTableName() {
        return DATABASE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.d(">>>");
        db.execSQL(DATABASE_CMD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.d(">>>");
        switch (oldVersion) {
            case DATABASE_INIT_VERSION:
                String sqlCmdDrop = "drop table if exists " + DATABASE_NAME;
                String sqlCmdCreate = DATABASE_CMD;
                db.execSQL(sqlCmdDrop);
                db.execSQL(sqlCmdCreate);
                break;
            default:
                break;
        }
    }
}
