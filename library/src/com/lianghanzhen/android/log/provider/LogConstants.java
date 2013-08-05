package com.lianghanzhen.android.log.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Constants that used for SQLite database.
 */
public final class LogConstants {

    static final String DATABASE_NAME = "log.db";
    static final int DATABASE_VERSION = 1;
    static final String TABLE_NAME = "logs";

    static final String AUTHORITY = "com.lianghanzhen.android.log.provider";

    private LogConstants() {}

    /**
     * SQLite database column names for table {@link TABLE_NAME}
     */
    public static final class Columns implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.parse(String.format("content://%s/%s", AUTHORITY, TABLE_NAME));
        static final String CONTENT_TYPE = String.format("vnd.android.cursor.dir/vnd.%s.%s", AUTHORITY, TABLE_NAME);
        static final String CONTENT_ITEM_TYPE = String.format("vnd.android.cursor.item/vnd.%s.%s", AUTHORITY, TABLE_NAME);

        public static final String PACKAGE = "package";
        public static final String CREATED_DATE = "created";
        public static final String LEVEL = "level";
        public static final String TAG = "tag";
        public static final String MESSAGE = "message";
        public static final String THROWABLE = "throwable";
        public static final String DEFAULT_SORT_ORDER = String.format("%s ASC", CREATED_DATE);

        private Columns() {}

    }

}
