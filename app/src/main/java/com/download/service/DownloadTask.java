package com.download.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.download.db.FileDAO;
import com.download.db.FileDAOImpl;
import com.download.db.ThreadDAO;
import com.download.db.ThreadDAOImpl;
import com.download.entity.FileInfo;
import com.download.entity.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * use for 下载任务类
 */
public class DownloadTask {
    private static final String TAG = "DownloadTask";

    private Context mContext = null;
    private FileInfo mFileInfo = null;
    private ThreadDAO mThreadDAO = null;
    private FileDAO mFileDAO = null;
    public boolean isPause = false;
    private long mFinished = 0;
    private int mThreadCount = 1;//线程数量
    private List<DownloadThread> mThreadList = null;//线程集合
    public static ExecutorService sExecutorService = Executors.newCachedThreadPool();//线程池

    public DownloadTask(Context mContext, FileInfo mFileInfo, int mThreadCount) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        this.mThreadCount = mThreadCount;
        mThreadDAO = ThreadDAOImpl.getInstance();
        mFileDAO = FileDAOImpl.getInstance();
    }

    public void download() {
        //读取数据库的线程信息
        List<ThreadInfo> threadInfos = mThreadDAO.getThread(mFileInfo.getUrl());
        if (threadInfos.size() == 0) {
            //获得每个线程下载的长度
            long length = mFileInfo.getLength() / mThreadCount;
            for (int i = 0; i < mThreadCount; i++) {
                //初始化线程信息对象
                ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), length * i, (i + 1) * length - 1, 0);
                if (i == mThreadCount - 1) {
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                //添加到线程信息集合中
                threadInfos.add(threadInfo);
                //向数据库插入线程信息
                mThreadDAO.insertThread(threadInfo);
            }
        }
        mThreadList = new ArrayList<>();
        //启动多个线程进行下载
        for (ThreadInfo thread : threadInfos) {
            DownloadThread downloadThread = new DownloadThread(thread);
//            downloadThread.start();
            DownloadTask.sExecutorService.execute(downloadThread);
            //添加线程到集合中
            mThreadList.add(downloadThread);
        }
    }

    /**
     * 数据下载线程
     */
    class DownloadThread extends Thread {
        private ThreadInfo threadInfo = null;
        public boolean isFinished = false;//标示线程是否执行完毕

        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream is = null;
            try {
                URL url = new URL(threadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                //设置下载位置
                long start = threadInfo.getStart() + threadInfo.getFinish();
                conn.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());
                //设置文件写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);

                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                mFinished += threadInfo.getFinish();
                //开始下载
                if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    //读取数据
                    is = conn.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = is.read(buffer)) != -1) {
                        //写入文件
                        raf.write(buffer, 0, len);
                        //累加整个文件下载进度
                        mFinished += len;
                        //累加每个线程完成的进度
                        threadInfo.setFinish(threadInfo.getFinish() + len);
                        //每隔1秒刷新UI
                        if (System.currentTimeMillis() - time > 1000) {//减少UI负载
                            time = System.currentTimeMillis();
                            //发送进度到Activity
                            intent.putExtra("finished", (int) (mFinished * 100 / mFileInfo.getLength()));
                            intent.putExtra("id", mFileInfo.getId());
                            mContext.sendBroadcast(intent);
                            Log.i(TAG, "mFinished id==" + mFileInfo.getId() + ",percent==" + mFinished * 100 / mFileInfo.getLength());
                        }
                        //下载暂停时，保存进度
                        if (isPause) {
                            mThreadDAO.updateThread(mFileInfo.getUrl(), mFileInfo.getId(), threadInfo.getFinish());
                            return;
                        }
                    }
                    //标识线程执行完毕
                    isFinished = true;
                    //检查下载任务是否完成
                    checkAllThreadFinished();
//                    //删除线程信息
//                    mThreadDAO.deleteThread(mFileInfo.getUrl(), mFileInfo.getId());
                }

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
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断所有线程是否都执行完毕
     */
    private synchronized void checkAllThreadFinished() {
        boolean allFinished = true;
        //遍历线程集合，判断线程是否都执行完毕
        for (DownloadThread thread : mThreadList) {
            if (!thread.isFinished) {
                allFinished = false;
                break;
            }
        }
        if (allFinished) {
            //删除线程信息
            mThreadDAO.deleteThread(mFileInfo.getUrl());
            //发送广播通知UI下载结束
            Intent intent = new Intent(DownloadService.ACTION_FINISHED);
            intent.putExtra("fileInfo", mFileInfo);
            mContext.sendBroadcast(intent);
            mFileDAO.insertFile(mFileInfo);

        }
    }
}

