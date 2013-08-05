package com.lianghanzhen.android.log;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.lianghanzhen.android.log.provider.LogConstants;

import java.util.ArrayList;

/**
 * Write logs to SQLite database
 */
public class SQLiteLog extends AsyncLog {

    public SQLiteLog(Context context) {
        super(new SQLiteWriter(context));
    }

    @Override
    protected ArrayList<Marker> readLogs() {
        final ArrayList<Marker> markers = new ArrayList<Marker>();
        final SQLiteWriter writer = (SQLiteWriter) mLogDispatcher.getLogWriter();
        final Cursor cursor = writer.getLogResolver().query(LogConstants.Columns.CONTENT_URI, null, String.format("%s='%s'", LogConstants.Columns.PACKAGE, writer.getPackageName()), null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                final long createDate = cursor.getLong(cursor.getColumnIndexOrThrow(LogConstants.Columns.CREATED_DATE));
                final int level = cursor.getInt(cursor.getColumnIndexOrThrow(LogConstants.Columns.LEVEL));
                final String tag = cursor.getString(cursor.getColumnIndexOrThrow(LogConstants.Columns.TAG));
                final String message = cursor.getString(cursor.getColumnIndexOrThrow(LogConstants.Columns.MESSAGE));
                final String throwable = cursor.getString(cursor.getColumnIndexOrThrow(LogConstants.Columns.THROWABLE));
                markers.add(new Marker(createDate, level, tag, message, throwable));
            }
        }
        return markers;
    }

    @Override
    public void clearLogs() {
        ((SQLiteWriter) mLogDispatcher.getLogWriter()).getLogResolver().delete(LogConstants.Columns.CONTENT_URI, null, null);
    }

    static final class SQLiteWriter implements LogWriter {

        private final String mPackageName;
        private final ContentResolver mLogResolver;

        SQLiteWriter(Context context) {
            mPackageName = context.getPackageName();
            mLogResolver = context.getContentResolver();
        }

        ContentResolver getLogResolver() {
            return mLogResolver;
        }

        String getPackageName() {
            return mPackageName;
        }

        @Override
        public void writeLog(Marker marker) {
            ContentValues values = new ContentValues();
            values.put(LogConstants.Columns.PACKAGE, mPackageName);
            values.put(LogConstants.Columns.CREATED_DATE, marker.getCurrentDate().getTime());
            values.put(LogConstants.Columns.LEVEL, marker.getLevel());
            values.put(LogConstants.Columns.TAG, marker.getTag());
            values.put(LogConstants.Columns.MESSAGE, marker.getMessage());
            values.put(LogConstants.Columns.THROWABLE, marker.getThrowable());
            mLogResolver.insert(LogConstants.Columns.CONTENT_URI, values);
        }

    }

}
