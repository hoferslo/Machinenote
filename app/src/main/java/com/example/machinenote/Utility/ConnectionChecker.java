package com.example.machinenote.Utility;

import android.content.Context;

import com.example.machinenote.ApiManager;

public class ConnectionChecker implements Runnable {
    private final ApiManager apiManager;
    private final ConnectionCallback callback;
    private final int timeout = 1000; // Timeout in milliseconds

    public ConnectionChecker(Context context, ConnectionCallback callback) {
        this.apiManager = new ApiManager(context, timeout);
        this.callback = callback;
    }

    @Override
    public void run() {
        // Perform the connection check on a separate thread
        apiManager.checkServerConnection(new ApiManager.ConnectionCallback() {
            @Override
            public void onSuccess() {
                // Callback needs to be executed on the main thread
                callback.onSuccess();
            }

            @Override
            public void onFailure(String errorMessage) {
                // Callback needs to be executed on the main thread
                callback.onFailure(errorMessage);
            }
        });
    }


    public interface ConnectionCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }
}
