package com.example.dhassistant.ui.overlay;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.example.dhassistant.R;
import com.example.dhassistant.util.LogUtil;

/**
 * 可拖拽的小标识悬浮窗。
 *
 * <p>点击展开主面板；拖动后吸附到屏幕左/右边缘。</p>
 */
class FloatingBallView {

    private final Context appContext;
    private final WindowManager windowManager;

    private final View rootView;
    private final FrameLayout container;

    private WindowManager.LayoutParams layoutParams;

    private boolean shown;

    private float lastRawX;
    private float lastRawY;
    private boolean dragging;

    private OnClickListener clickListener;

    interface OnClickListener {
        void onClick();
    }

    FloatingBallView(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        this.windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);

        rootView = LayoutInflater.from(appContext).inflate(R.layout.layout_overlay_ball, null);
        container = (FrameLayout) rootView;
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(@NonNull View v, @NonNull MotionEvent event) {
                handleTouch(event);
                return true;
            }
        });
    }

    void setOnClickListener(OnClickListener listener) {
        this.clickListener = listener;
    }

    void show() {
        if (shown) {
            return;
        }
        if (!WindowPermissionHelper.canDrawOverlays(appContext)) {
            LogUtil.w("未授予悬浮窗权限，无法显示小球");
            return;
        }
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                chooseWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.x = 0;
        layoutParams.y = dp(120);
        try {
            windowManager.addView(rootView, layoutParams);
            shown = true;
        } catch (Throwable t) {
            LogUtil.e("显示悬浮球失败", t);
        }
    }

    void hide() {
        if (!shown) {
            return;
        }
        try {
            windowManager.removeView(rootView);
        } catch (Throwable t) {
            LogUtil.w("移除悬浮球失败: " + t.getMessage());
        }
        shown = false;
    }

    boolean isShown() {
        return shown;
    }

    void setPosition(int x, int y) {
        if (layoutParams == null) {
            return;
        }
        layoutParams.x = x;
        layoutParams.y = y;
        if (shown) {
            try {
                windowManager.updateViewLayout(rootView, layoutParams);
            } catch (Throwable t) {
                LogUtil.w("更新悬浮球位置失败: " + t.getMessage());
            }
        }
    }

    private void handleTouch(@NonNull MotionEvent event) {
        int screenWidth = appContext.getResources().getDisplayMetrics().widthPixels;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastRawX = event.getRawX();
                lastRawY = event.getRawY();
                dragging = false;
                break;
            case MotionEvent.ACTION_MOVE: {
                float dx = event.getRawX() - lastRawX;
                float dy = event.getRawY() - lastRawY;
                if (!dragging && Math.hypot(dx, dy) > dp(8)) {
                    dragging = true;
                }
                if (dragging) {
                    int newX = clampInt((int) event.getRawX() - dp(28), 0,
                            Math.max(0, screenWidth - dp(56)));
                    int newY = clampInt((int) event.getRawY() - dp(28), 0,
                            appContext.getResources().getDisplayMetrics().heightPixels - dp(56));
                    setPosition(newX, newY);
                }
                lastRawX = event.getRawX();
                lastRawY = event.getRawY();
                break;
            }
            case MotionEvent.ACTION_UP:
                if (!dragging) {
                    if (clickListener != null) {
                        clickListener.onClick();
                    }
                } else {
                    // 吸附到最近的边缘
                    int centerX = (int) event.getRawX();
                    int targetX = centerX > screenWidth / 2
                            ? screenWidth - dp(56)
                            : 0;
                    setPosition(targetX, layoutParams.y);
                }
                break;
            default:
                break;
        }
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

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
