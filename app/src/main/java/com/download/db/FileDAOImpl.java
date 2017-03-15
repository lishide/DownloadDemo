package com.download.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.download.MyApplication;
import com.download.entity.FileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * use for 数据访问接口的实现
 */
public class FileDAOImpl implements FileDAO {

    private static final String TAG = "FileDAOImpl";

    private static FileDAOImpl instance;
    private DBHelper mDBHelper;

    private FileDAOImpl(Context context) {
        this.mDBHelper = DBHelper.getInstance(context);
    }

    public static synchronized FileDAOImpl getInstance() {
        if (instance == null) {
            instance = new FileDAOImpl(MyApplication.getInstance());
        }
        return instance;
    }

    @Override
    public synchronized void insertFile(FileInfo fileInfo) {
        Log.i(TAG, "insertFile");
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("insert into file_info(file_id,url,name,length,finish) values(?,?,?,?,?)",
                new Object[]{fileInfo.getId(), fileInfo.getUrl(), fileInfo.getFileName(), fileInfo.getLength(), fileInfo.getFinish()});
        db.close();
    }

    @Override
    public synchronized void deleteFile(String url) {
        Log.e(TAG, "deleteFile");
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.execSQL("delete from file_info where url = ?", new Object[]{url});
        db.close();
    }

    @Override
    public List<FileInfo> getFile(String url) {
        Log.i(TAG, "getFile");
        List<FileInfo> list = new ArrayList<>();
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from file_info where url = ?", new String[]{url});
        while (cursor.moveToNext()) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setId(cursor.getInt(cursor.getColumnIndex("file_id")));
            fileInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            fileInfo.setFileName(cursor.getString(cursor.getColumnIndex("name")));
            fileInfo.setLength(cursor.getLong(cursor.getColumnIndex("length")));
            fileInfo.setFinish(cursor.getLong(cursor.getColumnIndex("finish")));
            list.add(fileInfo);
        }
        cursor.close();
        db.close();
        return list;
    }

}
