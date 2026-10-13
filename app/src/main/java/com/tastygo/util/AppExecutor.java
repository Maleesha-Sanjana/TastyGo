package com.tastygo.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutor {

    private static volatile AppExecutor sInstance;

    private final ExecutorService diskIO;
    private final Executor mainThread;
    private final Handler mainHandler;

    public interface Callback<T> {
        void onComplete(T result);
    }

    public interface Task<T> {
        T execute();
    }

    private AppExecutor() {
        this.diskIO = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.mainThread = mainHandler::post;
    }

    public static AppExecutor getInstance() {
        if (sInstance == null) {
            synchronized (AppExecutor.class) {
                if (sInstance == null) {
                    sInstance = new AppExecutor();
                }
            }
        }
        return sInstance;
    }

    public ExecutorService diskIO() {
        return diskIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    public Handler mainHandler() {
        return mainHandler;
    }

    /**
     * Executes a task on the background thread pool and delivers result on the main UI thread.
     */
    public <T> void execute(Task<T> task, Callback<T> callback) {
        diskIO.execute(() -> {
            T result = task.execute();
            if (callback != null) {
                mainHandler.post(() -> callback.onComplete(result));
            }
        });
    }

    /**
     * Runs a simple runnable in the background without returning a result.
     */
    public void execute(Runnable runnable) {
        diskIO.execute(runnable);
    }
}
