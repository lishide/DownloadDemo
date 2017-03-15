package com.download.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.download.entity.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    //结束下载
    public static final String ACTION_FINISHED = "ACTION_FINISHED";
    //更新UI
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    //下载路径
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/";
    //初始化
    private static final int MSG_INIT = 0;
    //下载任务集合
    private Map<Integer, DownloadTask> mTasks = new LinkedHashMap<>();
    public static final int runThreadCount = 3;
    private InitThread mInitThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获得Activity传来的参数
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.i(TAG, "onStartCommand: ACTION_START：" + fileInfo.toString());
            mInitThread = new InitThread(fileInfo);
            DownloadTask.sExecutorService.execute(mInitThread);
        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.i(TAG, "onStartCommand:ACTION_STOP：" + fileInfo.toString());
            //从集合中取出下载任务
            DownloadTask mDownloadTask = mTasks.get(fileInfo.getId());
            if (mDownloadTask != null) {
                //停止下载任务
                mDownloadTask.isPause = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileinfo = (FileInfo) msg.obj;
                    Log.i(TAG, "init:" + fileinfo.toString());
                    //启动下载任务
                    DownloadTask mDownloadTask = new DownloadTask(DownloadService.this, fileinfo, runThreadCount);
                    mDownloadTask.download();
                    //将下载任务添加到集合中
                    mTasks.put(fileinfo.getId(), mDownloadTask);
                    break;
            }
        }
    };

    /**
     * 初始化 子线程
     */
    class InitThread extends Thread {
        private FileInfo tFileInfo = null;

        public InitThread(FileInfo tFileInfo) {
            this.tFileInfo = tFileInfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            try {
                //连接网络文件
                URL url = new URL(tFileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                int length = -1;
                Log.i(TAG, "getResponseCode==" + conn.getResponseCode());
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //获取文件长度
                    length = conn.getContentLength();
                    Log.i(TAG, "length==" + length);
                }
                if (length < 0) {
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                //在本地创建文件
                File file = new File(dir, tFileInfo.getFileName());
//                if (!file.exists()) {
                    raf = new RandomAccessFile(file, "rwd");
                    //设置本地文件长度
                    raf.setLength(length);
                    tFileInfo.setLength(length);
                    Log.i(TAG, "tFileInfo.getLength==" + tFileInfo.getLength());
                    mHandler.obtainMessage(MSG_INIT, tFileInfo).sendToTarget();
//                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (conn != null) {
                        conn.disconnect();
                    }
                    if (raf != null) {
                        raf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
