package com.example.dhassistant.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 主线程调度工具。
 *
 * <p>自动化任务通常运行在子线程（避免阻塞无障碍回调），
 * 但手势派发、窗口操作等 API 需要在主线程调用，
 * 因此统一通过本类切换线程。</p>
 */
public final class MainThread {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private MainThread() {
    }

    /** 投递到主线程执行。 */
    public static void post(@NonNull Runnable runnable) {
        HANDLER.post(runnable);
    }

    /** 投递到主线程延迟执行。 */
    public static void postDelayed(@NonNull Runnable runnable, long delayMillis) {
        HANDLER.postDelayed(runnable, delayMillis);
    }

    /**
     * 在主线程同步执行并返回结果（会阻塞调用线程）。
     */
    public static <T> T callSync(@NonNull Callable<T> callable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        final AtomicReference<T> result = new AtomicReference<>();
        final AtomicReference<Exception> error = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        HANDLER.post(() -> {
            try {
                result.set(callable.call());
            } catch (Exception e) {
                error.set(e);
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (error.get() != null) {
            throw new IllegalStateException(error.get());
        }
        return result.get();
    }
}
