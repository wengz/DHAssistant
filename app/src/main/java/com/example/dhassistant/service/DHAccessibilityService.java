package com.example.dhassistant.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dhassistant.task.TaskContext;
import com.example.dhassistant.task.TaskResult;
import com.example.dhassistant.ui.overlay.OverlayController;
import com.example.dhassistant.util.LogUtil;
import com.example.dhassistant.util.MainThread;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动化核心：无障碍服务。
 *
 * <p>职责：</p>
 * <ol>
 *     <li>接收无障碍事件并转发给 {@link AccessibilityEventDispatcher}；</li>
 *     <li>作为 {@link AccessibilityBridge} 的实现，对外提供查找/点击/手势等能力；</li>
 *     <li>作为悬浮窗的宿主，在服务连接后创建悬浮球与任务面板。</li>
 * </ol>
 */
public class DHAccessibilityService extends AccessibilityService implements AccessibilityBridge {

    private static DHAccessibilityService instance;

    private final AccessibilityEventDispatcher eventDispatcher = new AccessibilityEventDispatcher();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        LogUtil.i("无障碍服务已连接");
        // 初始化服务信息
        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.flags = (info.flags
                    | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                    | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
                    | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS);
            info.eventTypes = AccessibilityServiceInfo.EVENT_TYPE_WINDOW_STATE_CHANGED
                    | AccessibilityServiceInfo.EVENT_TYPE_WINDOW_CONTENT_CHANGED
                    | AccessibilityServiceInfo.EVENT_TYPE_VIEW_CLICKED
                    | AccessibilityServiceInfo.EVENT_TYPE_VIEW_FOCUSED
                    | AccessibilityServiceInfo.EVENT_TYPE_VIEW_SCROLLED;
            info.notificationTimeout = 100;
            setServiceInfo(info);
        }
        // 创建悬浮球
        OverlayController.get().attach(this);
    }

    @Override
    public void onAccessibilityEvent(@NonNull AccessibilityEvent event) {
        eventDispatcher.dispatch(event);
    }

    @Override
    public void onInterrupt() {
        LogUtil.w("无障碍服务被中断");
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        LogUtil.i("无障碍服务已断开");
        OverlayController.get().detach();
        instance = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        OverlayController.get().detach();
        instance = null;
        super.onDestroy();
    }

    /**
     * 获取服务单例（未连接时返回 null）。
     */
    @Nullable
    public static DHAccessibilityService getInstance() {
        return instance;
    }

    public AccessibilityEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    // ------------------------------------------------------------------
    // AccessibilityBridge 实现
    // ------------------------------------------------------------------

    @Nullable
    @Override
    public AccessibilityNodeInfo getRootInActiveWindow() {
        // 注意：必须调用 super，否则会与接口默认实现形成递归
        return super.getRootInActiveWindow();
    }

    @NonNull
    @Override
    public List<AccessibilityNodeInfo> findNodesByViewId(@NonNull String viewId) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return new ArrayList<>();
        }
        List<AccessibilityNodeInfo> result = root.findAccessibilityNodeInfosByViewId(viewId);
        return result == null ? new ArrayList<>() : result;
    }

    @NonNull
    @Override
    public List<AccessibilityNodeInfo> findNodesByText(@NonNull String text) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return new ArrayList<>();
        }
        List<AccessibilityNodeInfo> result = root.findAccessibilityNodeInfosByText(text);
        return result == null ? new ArrayList<>() : result;
    }

    @NonNull
    @Override
    public List<AccessibilityNodeInfo> findNodesByContentDescription(@NonNull String desc) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return new ArrayList<>();
        }
        List<AccessibilityNodeInfo> result = root.findAccessibilityNodeInfosByContentDescription(desc);
        return result == null ? new ArrayList<>() : result;
    }

    @NonNull
    @Override
    public List<AccessibilityWindowInfo> getWindows() {
        List<AccessibilityWindowInfo> windows = super.getWindows();
        return windows == null ? new ArrayList<>() : windows;
    }

    @Override
    public boolean performGlobalAction(int action) {
        return super.performGlobalAction(action);
    }

    @Override
    public boolean dispatchSwipe(int direction) {
        // 手势派发必须发生在主线程
        return MainThread.callSync(() -> {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root == null) {
                return false;
            }
            Rect bounds = new Rect();
            root.getBoundsInScreen(bounds);
            if (bounds.width() <= 0 || bounds.height() <= 0) {
                return false;
            }
            Path path = buildSwipePath(direction, bounds);
            GestureDescription gesture = new GestureDescription.Builder()
                    .addStroke(new GestureDescription.StrokeDescription(path, 0L, 400L))
                    .build();
            return dispatchGesture(gesture, null);
        });
    }

    @Override
    public boolean launchApp(@NonNull String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            LogUtil.w("无法启动应用: " + packageName);
            return false;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return launchActivity(intent);
    }

    @Override
    public boolean launchActivity(@NonNull Intent intent) {
        try {
            return MainThread.callSync(() -> startActivity(intent));
        } catch (Throwable t) {
            LogUtil.e("startActivity 失败", t);
            return false;
        }
    }

    @NonNull
    @Override
    public Context getAppContext() {
        return getApplicationContext();
    }

    @NonNull
    @Override
    public Path buildSwipePath(int direction) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        Rect bounds = new Rect();
        if (root != null) {
            root.getBoundsInScreen(bounds);
        }
        return buildSwipePath(direction, bounds);
    }

    /**
     * 依据方向与窗口区域构造滑动路径。
     *
     * @param direction 0=上, 1=下, 2=左, 3=右
     */
    @NonNull
    private Path buildSwipePath(int direction, @NonNull Rect bounds) {
        int width = bounds.width() > 0 ? bounds.width() : getResources().getDisplayMetrics().widthPixels;
        int height = bounds.height() > 0 ? bounds.height() : getResources().getDisplayMetrics().heightPixels;
        int centerX = bounds.left + width / 2;
        int centerY = bounds.top + height / 2;
        int stepX = Math.max(width / 4, 100);
        int stepY = Math.max(height / 4, 100);

        Path path = new Path();
        switch (direction) {
            case 0: // 上滑
                path.moveTo(centerX, centerY + stepY);
                path.lineTo(centerX, centerY - stepY);
                break;
            case 1: // 下滑
                path.moveTo(centerX, centerY - stepY);
                path.lineTo(centerX, centerY + stepY);
                break;
            case 2: // 左滑
                path.moveTo(centerX + stepX, centerY);
                path.lineTo(centerX - stepX, centerY);
                break;
            case 3: // 右滑
                path.moveTo(centerX - stepX, centerY);
                path.lineTo(centerX + stepX, centerY);
                break;
            default:
                path.moveTo(centerX, centerY);
                break;
        }
        return path;
    }

    // ------------------------------------------------------------------
    // 任务执行辅助
    // ------------------------------------------------------------------

    /**
     * 构造任务上下文。
     */
    @NonNull
    public TaskContext createTaskContext() {
        return new TaskContext(this);
    }

    /**
     * 在后台线程执行任务，并在主线程回调结果。
     */
    public void executeTask(@NonNull com.example.dhassistant.task.AutomationTask task,
                            @NonNull TaskCallback callback) {
        final TaskContext context = createTaskContext();
        Thread thread = new Thread(() -> {
            TaskResult result;
            try {
                result = task.execute(context);
            } catch (Throwable t) {
                LogUtil.e("任务执行异常: " + task.getId(), t);
                result = TaskResult.failure(String.valueOf(t.getMessage()));
            }
            final TaskResult finalResult = result;
            MainThread.post(() -> callback.onFinished(finalResult));
        }, "dh-task-" + task.getId());
        thread.setDaemon(true);
        thread.start();
    }

    public interface TaskCallback {
        void onFinished(@NonNull TaskResult result);
    }
}
