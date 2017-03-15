package com.download;

import android.app.Application;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

public class MyApplication extends Application {
    private static MyApplication instance;
    private static final String TAG_stack = "ActivityStack";

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        UEHandler ueHandler = new UEHandler();
        Thread.setDefaultUncaughtExceptionHandler(ueHandler);

    }

    private class UEHandler implements UncaughtExceptionHandler {
        private static final String TAG = "MythouCrashHandler---->";
        private UncaughtExceptionHandler defaultUEH;

        // 构造函数，获取默认的处理方法
        public UEHandler() {
            this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        }

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            // 获取跟踪的栈信息，除了系统栈信息，还把手机型号、系统版本、编译版本的唯一标示
            StackTraceElement[] trace = ex.getStackTrace();
            StackTraceElement[] trace2 = new StackTraceElement[trace.length + 3];
            System.arraycopy(trace, 0, trace2, 0, trace.length);
            trace2[trace.length + 0] = new StackTraceElement("Android", "MODEL", android.os.Build.MODEL, -1);
            trace2[trace.length + 1] = new StackTraceElement("Android", "VERSION", android.os.Build.VERSION.RELEASE, -1);
            trace2[trace.length + 2] = new StackTraceElement("Android", "FINGERPRINT", android.os.Build.FINGERPRINT, -1);
            // 追加信息，因为后面会回调默认的处理方法
            ex.setStackTrace(trace2);
            ex.printStackTrace(printWriter);
            // 把上面获取的堆栈信息转为字符串，打印出来
            String stacktrace = result.toString();
            printWriter.close();
            Log.d(TAG, stacktrace);

            // 这里把刚才异常堆栈信息写入SD卡的Log日志里面
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                writeLog(stacktrace);
            }

            defaultUEH.uncaughtException(thread, ex);
        }

        // 写入Log信息的方法，写入到SD卡里面
        private void writeLog(String log) {
            CharSequence timestamp = DateFormat.format("yyyyMMdd_HHmmss_", System.currentTimeMillis()) + "" + (System.currentTimeMillis() % 1000);

            try {
                String filename = Environment.getExternalStorageDirectory().getPath() + File.separator + "Download" + File.separator
                        + "Log" + File.separator;
                File dirFile = new File(filename);
                if (!dirFile.exists()) {
                    dirFile.mkdirs();
                }
                filename += "E_" + timestamp + ".log";
                FileOutputStream stream = new FileOutputStream(filename);
                OutputStreamWriter output = new OutputStreamWriter(stream);
                BufferedWriter bw = new BufferedWriter(output);
                // 写入相关Log到文件
                bw.write(timestamp.toString());
                bw.newLine();
                bw.write(log);
                bw.newLine();
                bw.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
