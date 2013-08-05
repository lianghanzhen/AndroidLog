package com.lianghanzhen.android.log;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * use to write logs into any persistence storage, like, File, SQLite database or network
 */
public class AsyncLog implements L {

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss$SS";

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    protected final LogDispatcher mLogDispatcher;

    public AsyncLog(LogWriter logWriter) {
        mLogDispatcher = new LogDispatcher(logWriter);
        mLogDispatcher.start();
    }

    public final void read(final OnLogReadCompletedListener onLogReadCompletedListener) {
        new Thread() {
            @Override
            public void run() {
                final ArrayList<Marker> markers = readLogs();
                if (onLogReadCompletedListener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onLogReadCompletedListener.onLogReadCompleted(markers);
                        }
                    });
                }
            }
        }.start();
    }

    public final void clear() {
        new Thread() {
            @Override
            public void run() {
                clearLogs();
            }
        }.start();
    }

    protected void clearLogs() {

    }

    protected ArrayList<Marker> readLogs() {
        return new ArrayList<Marker>(0);
    };

    /**
     * 关闭文件日志
     */
    public void quit() {
        mLogDispatcher.quit();
    }

    @Override
    public void v(String tag, String message) {
        mLogDispatcher.add(Log.VERBOSE, tag, message, null);
    }

    @Override
    public void v(String tag, String message, Throwable throwable) {
        mLogDispatcher.add(Log.VERBOSE, tag, message, throwable);
    }

    @Override
    public void d(String tag, String message) {
        mLogDispatcher.add(Log.DEBUG, tag, message, null);
    }

    @Override
    public void d(String tag, String message, Throwable throwable) {
        mLogDispatcher.add(Log.DEBUG, tag, message, throwable);
    }

    @Override
    public void i(String tag, String message) {
        mLogDispatcher.add(Log.INFO, tag, message, null);
    }

    @Override
    public void i(String tag, String message, Throwable throwable) {
        mLogDispatcher.add(Log.INFO, tag, message, throwable);
    }

    @Override
    public void w(String tag, String message) {
        mLogDispatcher.add(Log.WARN, tag, message, null);
    }

    @Override
    public void w(String tag, Throwable throwable) {
        mLogDispatcher.add(Log.WARN, tag, "", throwable);
    }

    @Override
    public void w(String tag, String message, Throwable throwable) {
        mLogDispatcher.add(Log.WARN, tag, message, throwable);
    }

    @Override
    public void e(String tag, String message) {
        mLogDispatcher.add(Log.ERROR, tag, message, null);
    }

    @Override
    public void e(String tag, String message, Throwable throwable) {
        mLogDispatcher.add(Log.ERROR, tag, message, throwable);
    }

    public interface OnLogReadCompletedListener {

        void onLogReadCompleted(ArrayList<Marker> markers);

    }

    public interface LogWriter {

        void writeLog(Marker marker);

    }

    static final class LogDispatcher extends Thread {

        private static final String TAG = "LogDispatcher";

        private final BlockingQueue<Marker> mMarkerQueue;
        private volatile boolean mQuit = false;
        private final LogWriter mLogWriter;

        LogDispatcher(LogWriter logWriter) {
            mMarkerQueue = new ArrayBlockingQueue<Marker>(50);
            mLogWriter = logWriter;
        }

        LogWriter getLogWriter() {
            return mLogWriter;
        }

        void add(int level, String tag, String message, Throwable throwable) {
            try {
                mMarkerQueue.put(new Marker(level, tag, message, Log.getStackTraceString(throwable)));
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        public void quit() {
            mQuit = true;
            interrupt();
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            Marker marker;
            while (true) {
                try {
                    marker = mMarkerQueue.take();
                } catch (InterruptedException e) {
                    if (mQuit) {
                        return;
                    }
                    continue;
                }

                if (mLogWriter != null) {
                    mLogWriter.writeLog(marker);
                }
            }
        }

    }

    public static final class Marker implements Serializable {

        private final Date mCurrentDate;
        private final int mLevel;
        private final String mTag;
        private final String mMessage;
        private final String mThrowable;

        Marker(long currentDate, int level, String tag, String message, String throwable) {
            mCurrentDate = new Date(currentDate);
            mLevel = level;
            mTag = tag;
            mMessage = message;
            mThrowable = throwable;
        }

        Marker(int level, String tag, String message, String throwable) {
            this(System.currentTimeMillis(), level, tag, message, throwable);
        }

        public Date getCurrentDate() {
            return mCurrentDate;
        }

        public int getLevel() {
            return mLevel;
        }

        public String getTag() {
            return mTag;
        }

        public String getMessage() {
            return mMessage;
        }

        public String getThrowable() {
            return mThrowable;
        }

        @Override
        public String toString() {
            return "Marker{" +
                    "Level=" + mLevel +
                    ", CurrentDate=" + new SimpleDateFormat(DATE_FORMAT).format(mCurrentDate) +
                    ", Tag='" + mTag + '\'' +
                    ", Message='" + mMessage + '\'' +
                    ", Throwable=" + mThrowable +
                    '}';
        }

        public String getOutput() {
            final StringBuilder builder = new StringBuilder(new SimpleDateFormat(DATE_FORMAT).format(mCurrentDate)).append('\t');
            if (mMessage != null) {
                builder.append(mMessage).append('\n');
            }
            if (mThrowable != null) {
                if (mMessage == null) {
                    builder.append('\n');
                }
                builder.append(mThrowable);
            }
            return builder.toString();
        }

    }

}
