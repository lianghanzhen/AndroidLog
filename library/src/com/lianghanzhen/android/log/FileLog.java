package com.lianghanzhen.android.log;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;

/**
 * Write logs to File
 */
public class FileLog extends AsyncLog {

    private static final String TAG = "FileLog";

    public FileLog(Context context) {
        this(context, null);
    }

    public FileLog(Context context, DeleteStrategy strategy) {
        super(new FileWriter(context, strategy));
    }

    @Override
    protected ArrayList<Marker> readLogs() {
        final ArrayList<Marker> markers = new ArrayList<Marker>();
        ObjectInputStream objectInputStream = null;
        File logFile = ((FileWriter) mLogDispatcher.getLogWriter()).mLogFile;
        try {
            if (logFile != null && logFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(logFile);
                objectInputStream = new ObjectInputStream(fileInputStream);
                Object object;
                while ((object = objectInputStream.readObject()) != null) {
                    if (object instanceof Marker) {
                        markers.add((Marker) object);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            return markers;
        }
    }

    @Override
    protected void clearLogs() {
        ((FileWriter) mLogDispatcher.getLogWriter()).clear();
    }

    @Override
    public void quit() {
        super.quit();
        ((FileWriter) mLogDispatcher.getLogWriter()).close();
    }

    private static long getCreatedDate(File logFile) {
        long createdDate = 0L;
        ObjectInputStream objectInputStream = null;
        try {
            if (logFile != null && logFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(logFile);
                objectInputStream = new ObjectInputStream(fileInputStream);
                Object object = objectInputStream.readObject();
                if (object instanceof Marker) {
                    createdDate = ((Marker) object).getCurrentDate().getTime();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            try {
                objectInputStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return createdDate;
        }
    }

    static final class FileWriter implements AsyncLog.LogWriter {

        private static final String ROOT_DIR = "AndroidLogs";
        private static final String LOG_FILE_FORMAT = "%s.log";

        private File mLogFile;
        /**
         * we keep ObjectOutputStream reference because when write multiple objects to file, it cannot read them correctly.
         */
        private ObjectOutputStream mObjectOutputStream;

        FileWriter(Context context) {
            this(context, null);
        }

        FileWriter(Context context, DeleteStrategy strategy) {
            createLogFile(String.format(LOG_FILE_FORMAT, context.getPackageName()), strategy);
        }

        void clear() {
            close();
            if (mLogFile != null) {
                try {
                    mLogFile.delete();
                    mLogFile.createNewFile();
                    mObjectOutputStream = new ObjectOutputStream(new FileOutputStream(mLogFile));
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        void close() {
            if (mObjectOutputStream != null) {
                try {
                    mObjectOutputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                } finally {
                    mObjectOutputStream = null;
                }
            }
        }

        @Override
        public void writeLog(AsyncLog.Marker marker) {
            try {
                if (mObjectOutputStream == null) {
                    mObjectOutputStream = new ObjectOutputStream(new FileOutputStream(mLogFile));
                }
                mObjectOutputStream.writeObject(marker);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        private void createLogFile(String fileName, DeleteStrategy strategy) {
            final boolean hasExternalStorage = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
            final File rootDir = new File(hasExternalStorage ? Environment.getExternalStorageDirectory() : Environment.getDataDirectory(), ROOT_DIR);
            try {
                if (rootDir.exists() || rootDir.mkdir()) {
                    mLogFile = new File(rootDir, fileName);
                    boolean rebuildLogFile = false;
                    if (mLogFile.exists()) {
                        if (strategy == null || strategy.isDelete(mLogFile)) {
                            mLogFile.delete();
                            mLogFile.createNewFile();
                            rebuildLogFile = true;
                        }
                    } else {
                        mLogFile.createNewFile();
                        rebuildLogFile = true;
                    }
                    openObjectOutputStream(rebuildLogFile);
                }
            } catch (IOException e) {
                Log.w(TAG, e.getMessage(), e);
            }
        }

        private void openObjectOutputStream(boolean rebuildLogFile) {
            ObjectInputStream objectInputStream = null;
            File tmpFile = null;
            FileChannel tmpChannel = null;
            FileChannel channel = null;
            try {
                if (rebuildLogFile) {
                    mObjectOutputStream = new ObjectOutputStream(new FileOutputStream(mLogFile, true));
                } else {
                    tmpFile = new File(mLogFile.getAbsolutePath() + ".tmp");
                    tmpFile.createNewFile();
                    tmpChannel = new RandomAccessFile(tmpFile, "rw").getChannel();
                    channel = new RandomAccessFile(mLogFile, "rw").getChannel();
                    tmpChannel.transferFrom(channel, 0, channel.size());
                    mLogFile.delete();
                    mLogFile.createNewFile();

                    mObjectOutputStream = new ObjectOutputStream(new FileOutputStream(mLogFile, true));
                    objectInputStream = new ObjectInputStream(new FileInputStream(tmpFile));
                    Object object;
                    while ((object = objectInputStream.readObject()) != null) {
                        if (object instanceof Marker) {
                            writeLog((Marker) object);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            } finally {
                if (tmpFile != null) {
                    tmpFile.delete();
                }
                try {
                    if (objectInputStream != null) {
                        objectInputStream.close();
                    }
                    if (tmpChannel != null) {
                        tmpChannel.close();
                    }
                    if (channel != null) {
                        channel.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

    }

    public interface DeleteStrategy {

        boolean isDelete(File logFile);

        static final long DAY_MILLIS = 24 * 60 * 60 * 1000L;
        static final long WEEK_MILLIS = 7 * DAY_MILLIS;
        static final long TWO_MEGABYTES = 2 * 1024 * 1024L;

        public final DeleteStrategy NEVER = new DeleteStrategy() {
            @Override
            public boolean isDelete(File logFile) {
                return false;
            }
        };

        public static final DeleteStrategy ANYWAY = new DeleteStrategy() {
            @Override
            public boolean isDelete(File logFile) {
                return true;
            }
        };

        public static final DeleteStrategy DAILY = new DeleteStrategy() {
            @Override
            public boolean isDelete(File logFile) {
                return (new Date().getTime() - getCreatedDate(logFile)) >= DAY_MILLIS;
            }
        };

        public static final DeleteStrategy WEEKLY = new DeleteStrategy() {
            @Override
            public boolean isDelete(File logFile) {
                return (new Date().getTime() - getCreatedDate(logFile)) >= WEEK_MILLIS;
            }
        };

        public static final DeleteStrategy SIZE_BIGGER_THAN_TWO_MEGABYTES = new DeleteStrategy() {
            @Override
            public boolean isDelete(File logFile) {
                return logFile != null ? logFile.length() >= TWO_MEGABYTES : true;
            }
        };

    }

}
