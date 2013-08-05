package com.lianghanzhen.android.log;

import android.util.Log;

public interface L {

    void v(String tag, String message);

    void v(String tag, String message, Throwable throwable);

    void d(String tag, String message);

    void d(String tag, String message, Throwable throwable);

    void i(String tag, String message);

    void i(String tag, String message, Throwable throwable);

    void w(String tag, String message);

    void w(String tag, Throwable throwable);

    void w(String tag, String message, Throwable throwable);

    void e(String tag, String message);

    void e(String tag, String message, Throwable throwable);

    public static final L DEBUG = new L() {

        @Override
        public void v(String tag, String message) {
            Log.v(tag, message);
        }

        @Override
        public void v(String tag, String message, Throwable throwable) {
            Log.v(tag, message, throwable);
        }

        @Override
        public void d(String tag, String message) {
            Log.d(tag, message);
        }

        @Override
        public void d(String tag, String message, Throwable throwable) {
            Log.d(tag, message, throwable);
        }

        @Override
        public void i(String tag, String message) {
            Log.i(tag, message);
        }

        @Override
        public void i(String tag, String message, Throwable throwable) {
            Log.i(tag, message, throwable);
        }

        @Override
        public void w(String tag, String message) {
            Log.w(tag, message);
        }

        @Override
        public void w(String tag, Throwable throwable) {
            Log.w(tag, throwable);
        }

        @Override
        public void w(String tag, String message, Throwable throwable) {
            Log.w(tag, message, throwable);
        }

        @Override
        public void e(String tag, String message) {
            Log.e(tag, message);
        }

        @Override
        public void e(String tag, String message, Throwable throwable) {
            Log.e(tag, message);
        }

    };

    public static final L RELEASE = new L() {

        @Override
        public void v(String tag, String message) {}

        @Override
        public void v(String tag, String message, Throwable throwable) {}

        @Override
        public void d(String tag, String message) {}

        @Override
        public void d(String tag, String message, Throwable throwable) {}

        @Override
        public void i(String tag, String message) {}

        @Override
        public void i(String tag, String message, Throwable throwable) {}

        @Override
        public void w(String tag, String message) {}

        @Override
        public void w(String tag, Throwable throwable) {}

        @Override
        public void w(String tag, String message, Throwable throwable) {}

        @Override
        public void e(String tag, String message) {}

        @Override
        public void e(String tag, String message, Throwable throwable) {}

    };

}
