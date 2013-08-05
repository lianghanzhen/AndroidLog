package com.lianghanzhen.android.log.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.lianghanzhen.android.log.AsyncLog;
import com.lianghanzhen.android.log.FileLog;
import com.lianghanzhen.android.log.R;

import java.util.ArrayList;

/**
 * Activity for reading FileLog
 */
public final class FileLogReaderActivity extends Activity {

    private int mCurrentId = R.id.log__file_verbose;
    private final ArrayList<AsyncLog.Marker> mMarkers = new ArrayList<AsyncLog.Marker>();

    private TextView mFileLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log__file_log);

        mFileLogView = (TextView) findViewById(R.id.log__file_log);

        new FileLog(this, FileLog.DeleteStrategy.NEVER).read(new AsyncLog.OnLogReadCompletedListener() {
            @Override
            public void onLogReadCompleted(ArrayList<AsyncLog.Marker> markers) {
                mMarkers.addAll(markers);
                refreshFileLogView();
            }
        });
        configOnClickListener(R.id.log__file_verbose);
        configOnClickListener(R.id.log__file_debug);
        configOnClickListener(R.id.log__file_info);
        configOnClickListener(R.id.log__file_warn);
        configOnClickListener(R.id.log__file_error);
    }

    private void refreshFileLogView() {
        final int level;
        if (mCurrentId == R.id.log__file_verbose) {
            level = Log.VERBOSE;
        } else if (mCurrentId == R.id.log__file_debug) {
            level = Log.DEBUG;
        } else if (mCurrentId == R.id.log__file_info) {
            level = Log.INFO;
        } else if (mCurrentId == R.id.log__file_warn) {
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
        mFileLogView.setText(builder.toString());
    }

    private void configOnClickListener(final int id) {
        findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentId = id;
                refreshFileLogView();
            }
        });
    }

}
