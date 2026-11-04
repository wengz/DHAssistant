package com.example.dhassistant.service;

import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;

import com.example.dhassistant.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 无障碍事件分发器。
 *
 * <p>某些自动化任务需要「监听」界面变化（例如等待某个窗口出现），
 * 可以通过 {@link #registerListener} 注册回调。</p>
 */
public class AccessibilityEventDispatcher {

    public interface Listener {
        void onAccessibilityEvent(@NonNull AccessibilityEvent event);
    }

    private final List<Listener> listeners = new ArrayList<>();

    public void registerListener(@NonNull Listener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void unregisterListener(@NonNull Listener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void dispatch(@NonNull AccessibilityEvent event) {
        List<Listener> snapshot;
        synchronized (listeners) {
            snapshot = new ArrayList<>(listeners);
        }
        for (Listener listener : snapshot) {
            try {
                listener.onAccessibilityEvent(event);
            } catch (Throwable t) {
                LogUtil.e("accessibility listener error", t);
            }
        }
    }
}
