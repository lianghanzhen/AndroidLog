package com.lianghanzhen.android.log;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class FileLogWriterActivity extends Activity {

    private AsyncLog mFileLog;

    private final View.OnClickListener mAddFileLog = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new LogTester("FileLog", mFileLog).log();
        }
    };

    private final View.OnClickListener mClearFileLog = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mFileLog.clear();
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_log);

        mFileLog = new FileLog(this, FileLog.DeleteStrategy.DAILY);

        findViewById(R.id.add_file_log).setOnClickListener(mAddFileLog);
        findViewById(R.id.clear_file_log).setOnClickListener(mClearFileLog);
    }

    @Override
    public void finish() {
        super.finish();
        mFileLog.quit();
    }

}