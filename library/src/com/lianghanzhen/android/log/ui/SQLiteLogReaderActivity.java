package com.lianghanzhen.android.log.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.lianghanzhen.android.log.AsyncLog;
import com.lianghanzhen.android.log.R;
import com.lianghanzhen.android.log.SQLiteLog;

import java.util.ArrayList;

/**
 * Activity for reading SQLiteLog
 */
public final class SQLiteLogReaderActivity extends Activity {

    private int mCurrentId = R.id.log__sqlite_verbose;
    private final ArrayList<AsyncLog.Marker> mMarkers = new ArrayList<AsyncLog.Marker>();

    private TextView mSQLiteLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log__sqlite_log);

        mSQLiteLogView = (TextView) findViewById(R.id.log__sqlite_log);

        new SQLiteLog(this).read(new AsyncLog.OnLogReadCompletedListener() {
            @Override
            public void onLogReadCompleted(ArrayList<AsyncLog.Marker> markers) {
                mMarkers.addAll(markers);
                refreshSQLiteLogView();
            }
        });
        configOnClickListener(R.id.log__sqlite_verbose);
        configOnClickListener(R.id.log__sqlite_debug);
        configOnClickListener(R.id.log__sqlite_info);
        configOnClickListener(R.id.log__sqlite_warn);
        configOnClickListener(R.id.log__sqlite_error);
    }

    private void refreshSQLiteLogView() {
        final int level;
        if (mCurrentId == R.id.log__sqlite_verbose) {
            level = Log.VERBOSE;
        } else if (mCurrentId == R.id.log__sqlite_debug) {
            level = Log.DEBUG;
        } else if (mCurrentId == R.id.log__sqlite_info) {
            level = Log.INFO;
        } else if (mCurrentId == R.id.log__sqlite_warn) {
            level = Log.WARN;
        } else {
            level = Log.ERROR;
        }
        final StringBuilder builder = new StringBuilder();
        int count = 0;
        for (AsyncLog.Marker marker : mMarkers) {
            if (marker.getLevel() >= level) {
                builder.append('\n').append(marker.getOutput());
                count++;
            }
        }
        builder.insert(0, "Log Size: " + count);
        mSQLiteLogView.setText(builder.toString());
    }

    private void configOnClickListener(final int id) {
        findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentId = id;
                refreshSQLiteLogView();
            }
        });
    }

}
