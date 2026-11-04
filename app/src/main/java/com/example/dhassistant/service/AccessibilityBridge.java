package com.example.dhassistant.service;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.List;

/**
 * 无障碍能力桥接接口。
 *
 * <p>把 {@link DHAccessibilityService} 中与「自动化」相关的能力抽出来，
 * 这样任务层（{@code task} 包）不必直接依赖具体的 Service 实现，
 * 方便单元测试与后续扩展（例如接入 Appium / UIAutomator 等）。</p>
 */
public interface AccessibilityBridge {

    /**
     * 获取当前活动窗口的根节点，可能为 null。
     */
    AccessibilityNodeInfo getRootInActiveWindow();

    /**
     * 按 viewId 查找节点（可能多个）。
     */
    List<AccessibilityNodeInfo> findNodesByViewId(String viewId);

    /**
     * 按文本查找节点（可能多个）。
     */
    List<AccessibilityNodeInfo> findNodesByText(String text);

    /**
     * 按 contentDescription 查找节点（可能多个）。
     */
    List<AccessibilityNodeInfo> findNodesByContentDescription(String desc);

    /**
     * 当前所有窗口信息。
     */
    List<AccessibilityWindowInfo> getWindows();

    /**
     * 执行全局动作（如返回、HOME、通知栏）。
     *
     * @param action {@link AccessibilityServiceInfo#GLOBAL_ACTION_BACK} 等
     */
    boolean performGlobalAction(int action);

    /**
     * 在指定方向执行一次滑动手势（用于不可滚动的窗口）。
     *
     * @param direction 0=上, 1=下, 2=左, 3=右
     */
    boolean dispatchSwipe(int direction);

    /**
     * 启动指定包名的应用。
     */
    boolean launchApp(String packageName);

    /**
     * 启动指定的 Activity。
     */
    boolean launchActivity(Intent intent);

    /**
     * 应用上下文。
     */
    Context getAppContext();

    /**
     * 手势路径构造辅助类（供任务层构造复杂手势时使用）。
     */
    Path buildSwipePath(int direction);
}
