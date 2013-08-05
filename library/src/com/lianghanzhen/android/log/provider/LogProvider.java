package com.lianghanzhen.android.log.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Content Provider for Log SQLite database.
 */
public final class LogProvider extends ContentProvider {

    private static final String TAG = "LogProvider";

    private static final int TYPE_LOGS = 1;
    private static final int TYPE_LOG_ITEM = 2;

    private static final UriMatcher URI_MATCHER;
    private static final Map<String, String> PROJECTIONS;
    private LogDatabaseHelper mLogHelper;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(LogConstants.AUTHORITY, LogConstants.TABLE_NAME, TYPE_LOGS);
        URI_MATCHER.addURI(LogConstants.AUTHORITY, String.format("%s/#", LogConstants.TABLE_NAME), TYPE_LOG_ITEM);

        PROJECTIONS = new HashMap<String, String>();
        PROJECTIONS.put(LogConstants.Columns._ID, LogConstants.Columns._ID);
        PROJECTIONS.put(LogConstants.Columns.PACKAGE, LogConstants.Columns.PACKAGE);
        PROJECTIONS.put(LogConstants.Columns.CREATED_DATE, LogConstants.Columns.CREATED_DATE);
        PROJECTIONS.put(LogConstants.Columns.LEVEL, LogConstants.Columns.LEVEL);
        PROJECTIONS.put(LogConstants.Columns.TAG, LogConstants.Columns.TAG);
        PROJECTIONS.put(LogConstants.Columns.MESSAGE, LogConstants.Columns.MESSAGE);
        PROJECTIONS.put(LogConstants.Columns.THROWABLE, LogConstants.Columns.THROWABLE);
        PROJECTIONS.put(LogConstants.Columns._COUNT, LogConstants.Columns._COUNT);
    }

    @Override
    public boolean onCreate() {
        mLogHelper = new LogDatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case TYPE_LOGS:
                return LogConstants.Columns.CONTENT_TYPE;
            case TYPE_LOG_ITEM:
                return LogConstants.Columns.CONTENT_ITEM_TYPE;
            default: throw new IllegalArgumentException(String.format("Unknown Uri: %s", uri));
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(LogConstants.TABLE_NAME);
        queryBuilder.setProjectionMap(PROJECTIONS);
        if (URI_MATCHER.match(uri) == TYPE_LOG_ITEM) {
            queryBuilder.appendWhere(String.format("%s=%s", LogConstants.Columns._ID, uri.getPathSegments().get(1)));
        }
        final SQLiteDatabase db = mLogHelper.getReadableDatabase();
        final Cursor result = queryBuilder.query(db, projection, selection, selectionArgs, null, null, !TextUtils.isEmpty(sortOrder) ? sortOrder : LogConstants.Columns.DEFAULT_SORT_ORDER);
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (URI_MATCHER.match(uri) != TYPE_LOGS) {
            throw new IllegalArgumentException(String.format("Unknown Uri: %s", uri));
        }
        final ContentValues values = initialValues != null ? new ContentValues(initialValues) : new ContentValues();
        Long now = Long.valueOf(System.currentTimeMillis());
        if (!values.containsKey(LogConstants.Columns.PACKAGE)) {
            values.put(LogConstants.Columns.PACKAGE, getContext().getPackageName());
        }
        if (!values.containsKey(LogConstants.Columns.CREATED_DATE)) {
            values.put(LogConstants.Columns.CREATED_DATE, now);
        }
        if (!values.containsKey(LogConstants.Columns.TAG)) {
            values.put(LogConstants.Columns.TAG, TAG);
        }
        if (!values.containsKey(LogConstants.Columns.MESSAGE)) {
            values.put(LogConstants.Columns.MESSAGE, "");
        }
        if (!values.containsKey(LogConstants.Columns.THROWABLE)) {
            values.put(LogConstants.Columns.THROWABLE, "");
        }
        final SQLiteDatabase db = mLogHelper.getWritableDatabase();
        final long rowId = db.insert(LogConstants.TABLE_NAME, null, values);
        if (rowId > 0) {
            final Uri logUri = ContentUris.withAppendedId(LogConstants.Columns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(logUri, null);
            return logUri;
        }
        throw new SQLiteException("Failed to insert row into " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        final SQLiteDatabase db = mLogHelper.getWritableDatabase();
        int count;
        switch (URI_MATCHER.match(uri)) {
            case TYPE_LOGS:
                count = db.update(LogConstants.TABLE_NAME, values, where, whereArgs);
                break;
            case TYPE_LOG_ITEM:
                final String newWhere = String.format("%s=%s%s", LogConstants.Columns._ID, uri.getPathSegments().get(1), !TextUtils.isEmpty(where) ? String.format(" AND (%s)", where) : "");
                count = db.update(LogConstants.TABLE_NAME, values, newWhere, whereArgs);
                break;
            default: throw new IllegalArgumentException(String.format("Unknown Uri: %s", uri));
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        final SQLiteDatabase db = mLogHelper.getWritableDatabase();
        int count;
        switch (URI_MATCHER.match(uri)) {
            case TYPE_LOGS:
                count = db.delete(LogConstants.TABLE_NAME, where, whereArgs);
                break;
            case TYPE_LOG_ITEM:
                final String newWhere = String.format("%s=%s%s", LogConstants.Columns._ID, uri.getPathSegments().get(1), !TextUtils.isEmpty(where) ? String.format(" AND (%s)", where) : "");
                count = db.delete(LogConstants.TABLE_NAME, newWhere, whereArgs);
                break;
            default: throw new IllegalArgumentException(String.format("Unknown Uri: %s", uri));
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * SQLite database Open Helper for Log.
     */
    private static final class LogDatabaseHelper extends SQLiteOpenHelper {

        private static final String CREATE_TABLE_SQL = String.format("CREATE TABLE IF NOT EXISTS %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s INTEGER, %s INTEGER, %s TEXT, %s TEXT, %s TEXT);",
                LogConstants.TABLE_NAME, LogConstants.Columns._ID, LogConstants.Columns.PACKAGE, LogConstants.Columns.CREATED_DATE, LogConstants.Columns.LEVEL, LogConstants.Columns.TAG, LogConstants.Columns.MESSAGE, LogConstants.Columns.THROWABLE);
        private static final String DROP_TABLE_SQL = String.format("DROP TABLE IF EXISTS %s;", LogConstants.TABLE_NAME);

        private LogDatabaseHelper(Context context) {
            super(context, LogConstants.DATABASE_NAME, null, LogConstants.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v(TAG, String.format("Create table: %s", LogConstants.TABLE_NAME));
            db.execSQL(CREATE_TABLE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, String.format("Upgrade database %s from version %d to %d", LogConstants.DATABASE_NAME, oldVersion, newVersion));
            db.execSQL(DROP_TABLE_SQL);
            onCreate(db);
        }

    }

}
