package com.lianghanzhen.android.log;

import android.util.Log;

/**
 * Write test data
 */
public class LogTester {

    private final String mTag;
    private final L l;
    private int mCount = 7;

    LogTester(String tag, L l) {
        mTag = tag;
        this.l = l;
    }

    public void log() {
        while (mCount >= Log.VERBOSE) {
            switch (mCount) {
                case Log.VERBOSE:
                    l.v(mTag, "VERBOSE");
                    l.v(mTag, "VERBOSE EXCEPTION", new IllegalArgumentException());
                    break;
                case Log.DEBUG:
                    l.d(mTag, "DEBUG");
                    l.d(mTag, "DEBUG EXCEPTION", new IllegalArgumentException());
                    break;
                case Log.INFO:
                    l.i(mTag, "INFO");
                    l.i(mTag, "INFO EXCEPTION", new IllegalArgumentException());
                    break;
                case Log.WARN:
                    l.w(mTag, "WARN");
                    l.w(mTag, new IllegalAccessError());
                    l.w(mTag, "WARN EXCEPTION", new IllegalArgumentException());
                    break;
                default:
                    l.e(mTag, "ERROR");
                    l.e(mTag, "ERROR EXCEPTION", new IllegalArgumentException());
                    break;
            }
            mCount--;
        }
    }

}
