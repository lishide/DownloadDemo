package com.download.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.download.MyApplication;
import com.download.entity.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * use for 数据访问接口的实现
 */
public class ThreadDAOImpl implements ThreadDAO {

    private static final String TAG = "ThreadDAOImpl";

    private static ThreadDAOImpl instance;
    private DBHelper mDBHelper;

    private ThreadDAOImpl(Context context) {
        this.mDBHelper = DBHelper.getInstance(context);
    }

    public static synchronized ThreadDAOImpl getInstance() {
        if (instance == null) {
            instance = new ThreadDAOImpl(MyApplication.getInstance());
        }
        return instance;
    }

    @Override
    public synchronized void insertThread(ThreadInfo threadInfo) {
        Log.i(TAG, "insertThread");
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values(?,?,?,?,?)",
                new Object[]{threadInfo.getId(), threadInfo.getUrl(),
                        threadInfo.getStart(), threadInfo.getEnd(), threadInfo.getFinish()});
        db.close();
    }

    @Override
    public synchronized void deleteThread(String url) {
        Log.e("deleteThread: ", "deleteThread");
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ?", new Object[]{url});
        db.close();
    }

    @Override
    public synchronized void updateThread(String url, int thread_id, long finished) {
        Log.i(TAG, "updateThread thread_id == " + thread_id);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?",
                new Object[]{finished, url, thread_id});
        db.close();
    }

    @Override
    public List<ThreadInfo> getThread(String url) {
        Log.i(TAG, "getThread");
        List<ThreadInfo> list = new ArrayList<>();
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ?", new String[]{url});
        while (cursor.moveToNext()) {
            ThreadInfo thread = new ThreadInfo();
            thread.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            thread.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            thread.setStart(cursor.getLong(cursor.getColumnIndex("start")));
            thread.setEnd(cursor.getLong(cursor.getColumnIndex("end")));
            thread.setFinish(cursor.getLong(cursor.getColumnIndex("finished")));
            list.add(thread);
        }
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?",
                new String[]{url, String.valueOf(thread_id)});
        boolean isExist = cursor.moveToNext();
        cursor.close();
        db.close();
        Log.i(TAG, "isExists: " + isExist);
        return isExist;
    }
}
