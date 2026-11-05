package com.example.dhassistant.task;

import android.graphics.Path;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dhassistant.service.AccessibilityBridge;
import com.example.dhassistant.util.LogUtil;

import java.util.List;

/**
 * 任务执行上下文。
 *
 * <p>封装了常用操作：查找节点、点击、输入、等待元素出现、滑动、启动应用等。
 * 任务实现类只依赖本类，不直接接触 {@code AccessibilityService}，便于维护与测试。</p>
 */
public class TaskContext {

    /** 默认单次查找超时时间（毫秒） */
    public static final long DEFAULT_TIMEOUT = 5000L;

    /** 轮询间隔（毫秒） */
    private static final long POLL_INTERVAL = 100L;

    private final AccessibilityBridge bridge;

    public TaskContext(@NonNull AccessibilityBridge bridge) {
        this.bridge = bridge;
    }

    @NonNull
    public AccessibilityBridge getBridge() {
        return bridge;
    }

    // ------------------------------------------------------------------
    // 查找
    // ------------------------------------------------------------------

    /**
     * 按 viewId 查找第一个匹配节点。
     */
    @Nullable
    public AccessibilityNodeInfo findById(@NonNull String viewId) {
        List<AccessibilityNodeInfo> nodes = bridge.findNodesByViewId(viewId);
        return (nodes == null || nodes.isEmpty()) ? null : nodes.get(0);
    }

    /**
     * 按文本查找第一个匹配节点。
     */
    @Nullable
    public AccessibilityNodeInfo findByText(@NonNull String text) {
        List<AccessibilityNodeInfo> nodes = bridge.findNodesByText(text);
        return (nodes == null || nodes.isEmpty()) ? null : nodes.get(0);
    }

    /**
     * 按 contentDescription 查找第一个匹配节点。
     */
    @Nullable
    public AccessibilityNodeInfo findByDesc(@NonNull String desc) {
        List<AccessibilityNodeInfo> nodes = bridge.findNodesByContentDescription(desc);
        return (nodes == null || nodes.isEmpty()) ? null : nodes.get(0);
    }

    /**
     * 等待某个 viewId 出现，超时返回 null。
     */
    @Nullable
    public AccessibilityNodeInfo waitForId(@NonNull String viewId, long timeoutMillis) {
        long deadline = SystemClock.uptimeMillis() + timeoutMillis;
        while (SystemClock.uptimeMillis() < deadline) {
            AccessibilityNodeInfo node = findById(viewId);
            if (node != null) {
                return node;
            }
            sleep(POLL_INTERVAL);
        }
        return null;
    }

    /**
     * 等待某个文本出现，超时返回 null。
     */
    @Nullable
    public AccessibilityNodeInfo waitForText(@NonNull String text, long timeoutMillis) {
        long deadline = SystemClock.uptimeMillis() + timeoutMillis;
        while (SystemClock.uptimeMillis() < deadline) {
            AccessibilityNodeInfo node = findByText(text);
            if (node != null) {
                return node;
            }
            sleep(POLL_INTERVAL);
        }
        return null;
    }

    // ------------------------------------------------------------------
    // 操作
    // ------------------------------------------------------------------

    /**
     * 点击节点（优先 ACTION_CLICK，失败时退化为点击父节点）。
     */
    public boolean click(@Nullable AccessibilityNodeInfo node) {
        if (node == null) {
            return false;
        }
        if (node.isEnabled() && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            return true;
        }
        AccessibilityNodeInfo parent = node.getParent();
        if (parent != null && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            return true;
        }
        return false;
    }

    /**
     * 等待并点击 viewId 对应的节点。
     */
    public boolean clickById(@NonNull String viewId) {
        AccessibilityNodeInfo node = waitForId(viewId, DEFAULT_TIMEOUT);
        return click(node);
    }

    /**
     * 等待并点击文本对应的节点。
     */
    public boolean clickByText(@NonNull String text) {
        AccessibilityNodeInfo node = waitForText(text, DEFAULT_TIMEOUT);
        return click(node);
    }

    /**
     * 给节点输入文本（先聚焦再设置文本）。
     */
    public boolean inputText(@Nullable AccessibilityNodeInfo node, @NonNull String text) {
        if (node == null) {
            return false;
        }
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }

    /**
     * 执行全局动作（返回 / HOME / 通知栏 等）。
     */
    public boolean performGlobalAction(int action) {
        return bridge.performGlobalAction_(action);
    }

    /**
     * 返回键。
     */
    public boolean goBack() {
        return bridge.performGlobalAction_(
                android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);
    }

    /**
     * 启动应用。
     */
    public boolean launchApp(@NonNull String packageName) {
        return bridge.launchApp(packageName);
    }

    /**
     * 向上滑动（列表向下翻页）。
     */
    public boolean swipeUp() {
        return bridge.dispatchSwipe(0);
    }

    /**
     * 向下滑动（列表向上翻页）。
     */
    public boolean swipeDown() {
        return bridge.dispatchSwipe(1);
    }

    /**
     * 构造滑动路径（供自定义手势使用）。
     */
    @NonNull
    public Path buildSwipePath(int direction) {
        return bridge.buildSwipePath(direction);
    }

    /**
     * 睡眠。
     */
    public void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            SystemClock.sleep(millis);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            LogUtil.w("task interrupted while sleeping");
        }
    }
}
