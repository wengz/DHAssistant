package com.example.dhassistant.ui.overlay;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dhassistant.R;
import com.example.dhassistant.service.DHAccessibilityService;
import com.example.dhassistant.task.AutomationTask;
import com.example.dhassistant.task.TaskRegistry;
import com.example.dhassistant.task.TaskResult;
import com.example.dhassistant.util.LogUtil;

/**
 * 悬浮窗控制器。
 *
 * <p>职责：</p>
 * <ol>
 *     <li>管理小球 + 主面板的生命周期（互相切换）；</li>
 *     <li>接收任务点击事件并通过 {@link DHAccessibilityService#executeTask} 执行；</li>
 *     <li>处理执行结果并刷新面板状态。</li>
 * </ol>
 */
public final class OverlayController {

    private static final OverlayController INSTANCE = new OverlayController();

    public static OverlayController get() {
        return INSTANCE;
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    private DHAccessibilityService service;

    private FloatingBallView ballView;
    private TaskPanelView panelView;

    private boolean taskRunning;

    private OverlayController() {
    }

    /**
     * 在 {@link DHAccessibilityService#onServiceConnected()} 中调用。
     */
    public void attach(@NonNull DHAccessibilityService service) {
        this.service = service;
        Context context = service.getApplicationContext();
        ballView = new FloatingBallView(context);
        ballView.setOnClickListener(this::showPanel);
        panelView = new TaskPanelView(context);
        panelView.setTasks(TaskRegistry.getTasks());
        panelView.setOnCloseListener(this::collapsePanel);
        panelView.setOnTaskClickListener(this::onTaskClicked);
        // 默认显示小球
        mainHandler.post(() -> {
            if (ballView != null) {
                ballView.show();
            }
        });
    }

    /**
     * 在无障碍服务解绑时调用。
     */
    public void detach() {
        if (ballView != null) {
            ballView.hide();
            ballView = null;
        }
        if (panelView != null) {
            panelView.hide();
            panelView = null;
        }
        service = null;
        mainHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 主动显示小球（来自 MainActivity 按钮）。
     */
    public void showBall() {
        ensureAttached();
        if (ballView == null) {
            return;
        }
        ballView.show();
    }

    /**
     * 主动隐藏所有悬浮窗。
     */
    public void hideAll() {
        if (ballView != null) {
            ballView.hide();
        }
        if (panelView != null) {
            panelView.hide();
        }
    }

    private void ensureAttached() {
        if (service == null) {
            LogUtil.w("OverlayController 未附加到无障碍服务");
        }
    }

    private void showPanel() {
        if (panelView == null || service == null) {
            return;
        }
        if (!WindowPermissionHelper.canDrawOverlays(service)) {
            LogUtil.w("无悬浮窗权限，无法显示面板");
            return;
        }
        if (ballView != null) {
            ballView.hide();
        }
        panelView.show();
        panelView.setStatus(service.getString(R.string.panel_idle),
                TaskPanelView.StatusColor.IDLE);
    }

    private void collapsePanel() {
        if (panelView != null) {
            panelView.hide();
        }
        if (ballView != null) {
            ballView.show();
        }
    }

    private void onTaskClicked(@NonNull AutomationTask task) {
        if (taskRunning) {
            LogUtil.i("任务正在执行中，忽略: " + task.getId());
            return;
        }
        if (service == null) {
            LogUtil.w("无障碍服务未连接，无法执行任务");
            return;
        }
        taskRunning = true;
        if (panelView != null) {
            panelView.setStatus(
                    service.getString(R.string.panel_running, task.getName()),
                    TaskPanelView.StatusColor.RUNNING);
        }
        service.executeTask(task, result -> onTaskFinished(task, result));
    }

    private void onTaskFinished(@NonNull AutomationTask task, @NonNull TaskResult result) {
        taskRunning = false;
        if (panelView == null || service == null) {
            return;
        }
        if (result.isSuccess()) {
            panelView.setStatus(
                    service.getString(R.string.panel_success, task.getName()),
                    TaskPanelView.StatusColor.SUCCESS);
        } else {
            panelView.setStatus(
                    service.getString(R.string.panel_failed,
                            task.getName() + (result.getMessage().isEmpty() ? "" : "： " + result.getMessage())),
                    TaskPanelView.StatusColor.ERROR);
        }
    }
}
