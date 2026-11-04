package com.example.dhassistant.ui.overlay;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.dhassistant.R;
import com.example.dhassistant.task.AutomationTask;
import com.example.dhassistant.task.TaskResult;
import com.example.dhassistant.util.LogUtil;

import java.util.List;

/**
 * 主面板悬浮窗：展示预设任务列表。
 */
class TaskPanelView {

    private final Context appContext;
    private final WindowManager windowManager;

    private final View rootView;
    private final LinearLayout listContainer;
    private final TextView statusView;
    private final ImageView closeButton;

    private WindowManager.LayoutParams layoutParams;
    private boolean shown;

    private OnTaskClickListener taskClickListener;
    private OnCloseListener closeListener;

    interface OnTaskClickListener {
        void onTaskClicked(@NonNull AutomationTask task);
    }

    interface OnCloseListener {
        void onClose();
    }

    TaskPanelView(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        this.windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);

        rootView = LayoutInflater.from(appContext).inflate(R.layout.layout_overlay_panel, null);
        listContainer = rootView.findViewById(R.id.overlay_panel_list);
        statusView = rootView.findViewById(R.id.overlay_panel_status);
        closeButton = rootView.findViewById(R.id.overlay_panel_close);

        closeButton.setOnClickListener(v -> {
            if (closeListener != null) {
                closeListener.onClose();
            }
        });
    }

    void setOnTaskClickListener(OnTaskClickListener listener) {
        this.taskClickListener = listener;
    }

    void setOnCloseListener(OnCloseListener listener) {
        this.closeListener = listener;
    }

    void setTasks(@NonNull List<AutomationTask> tasks) {
        listContainer.removeAllViews();
        for (AutomationTask task : tasks) {
            listContainer.addView(buildItemView(task));
        }
    }

    void setStatus(@NonNull String status, @NonNull StatusColor color) {
        statusView.setText(status);
        int colorRes;
        switch (color) {
            case RUNNING:
                colorRes = R.color.overlay_status_running;
                break;
            case SUCCESS:
                colorRes = R.color.overlay_status_ok;
                break;
            case ERROR:
                colorRes = R.color.overlay_status_error;
                break;
            default:
                colorRes = R.color.overlay_panel_sub;
                break;
        }
        statusView.setTextColor(appContext.getColor(colorRes));
    }

    void show() {
        if (shown) {
            return;
        }
        if (!WindowPermissionHelper.canDrawOverlays(appContext)) {
            LogUtil.w("未授予悬浮窗权限，无法显示面板");
            return;
        }
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                chooseWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.x = dp(16);
        layoutParams.y = dp(120);
        try {
            windowManager.addView(rootView, layoutParams);
            shown = true;
        } catch (Throwable t) {
            LogUtil.e("显示任务面板失败", t);
        }
    }

    void hide() {
        if (!shown) {
            return;
        }
        try {
            windowManager.removeView(rootView);
        } catch (Throwable t) {
            LogUtil.w("移除任务面板失败: " + t.getMessage());
        }
        shown = false;
    }

    boolean isShown() {
        return shown;
    }

    @NonNull
    private View buildItemView(@NonNull AutomationTask task) {
        View itemView = LayoutInflater.from(appContext).inflate(R.layout.layout_overlay_item, listContainer, false);
        TextView name = itemView.findViewById(R.id.overlay_item_name);
        TextView desc = itemView.findViewById(R.id.overlay_item_desc);
        name.setText(task.getName());
        desc.setText(task.getDescription());
        itemView.setOnClickListener(v -> {
            if (taskClickListener != null) {
                taskClickListener.onTaskClicked(task);
            }
        });
        return itemView;
    }

    private int chooseWindowType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        return WindowManager.LayoutParams.TYPE_PHONE;
    }

    private int dp(int value) {
        float density = appContext.getResources().getDisplayMetrics().density;
        return (int) (value * density + 0.5f);
    }

    enum StatusColor {
        IDLE,
        RUNNING,
        SUCCESS,
        ERROR
    }
}
