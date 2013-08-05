package com.lianghanzhen.android.log;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class SQLiteLogWriterActivity extends Activity {

    private AsyncLog mSQLiteLog;

    private final View.OnClickListener mAddSQLiteLog = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new LogTester("SQLiteLog", mSQLiteLog).log();
        }
    };

    private final View.OnClickListener mClearSQLiteLog = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mSQLiteLog.clear();
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sqlite_log);

        mSQLiteLog = new SQLiteLog(this);

        findViewById(R.id.add_sqlite_log).setOnClickListener(mAddSQLiteLog);
        findViewById(R.id.clear_sqlite_log).setOnClickListener(mClearSQLiteLog);
    }

    @Override
    public void finish() {
        super.finish();
        mSQLiteLog.quit();
    }

}