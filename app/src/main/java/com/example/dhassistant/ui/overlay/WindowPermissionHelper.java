package com.example.dhassistant.ui.overlay;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

/**
 * 悬浮窗权限工具。
 */
public final class WindowPermissionHelper {

    private WindowPermissionHelper() {
    }

    /**
     * 是否已获得「显示在其它应用上方」权限。
     */
    public static boolean canDrawOverlays(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(context);
    }

    /**
     * 跳转到悬浮窗权限设置页。
     */
    @NonNull
    public static Intent buildOverlayPermissionIntent(@NonNull Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
