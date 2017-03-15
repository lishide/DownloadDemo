package com.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * use for 数据库操作类
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "download.db";
    private static DBHelper helper = null;//静态对象引用

    private static final String SQL_CREATE = "create table thread_info(_id integer primary key autoincrement," +
            "thread_id integer,url text,start long,end long,finished long)";
    private static final String SQL_CREATE_FILE = "create table file_info(_id integer primary key autoincrement," +
            "file_id integer,url text,name text,length long,finish long)";
    private static final String SQL_DROP = "drop table if exists thread_info";
    private static final String SQL_DROP_FILE = "drop table if exists file_info";
    private static final int VERSION = 1;


    private DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    /**
     * 获得单例对象
     *
     * @param context 上下文
     * @return 单例对象
     */
    public static DBHelper getInstance(Context context) {
        if (helper == null) {
            helper = new DBHelper(context);
        }
        return helper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
        db.execSQL(SQL_CREATE_FILE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        db.execSQL(SQL_CREATE);

        db.execSQL(SQL_DROP_FILE);
        db.execSQL(SQL_CREATE_FILE);
    }
}
