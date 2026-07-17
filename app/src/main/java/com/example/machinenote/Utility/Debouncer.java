package com.example.machinenote.Utility;

import android.os.Handler;
import android.os.Looper;

public class Debouncer {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private final int delayMillis;

    public Debouncer(int delayMillis) {
        this.delayMillis = delayMillis;
    }

    public void run(Runnable runnable) {
        if (this.runnable != null) {
            handler.removeCallbacks(this.runnable);
        }
        this.runnable = runnable;
        handler.postDelayed(this.runnable, delayMillis);
    }
}