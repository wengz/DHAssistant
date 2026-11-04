package com.example.dhassistant.util;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * 无障碍服务状态查询工具。
 */
public final class AccessibilityHelper {

    private AccessibilityHelper() {
    }

    /**
     * 当前应用的无障碍服务是否已开启。
     */
    public static boolean isServiceEnabled(Context context) {
        if (context == null) {
            return false;
        }
        AccessibilityManager manager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager == null || !manager.isEnabled()) {
            return false;
        }
        List<android.accessibilityservice.AccessibilityServiceInfo> services =
                manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        if (services == null) {
            return false;
        }
        String target = new ComponentName(context, "com.example.dhassistant.service.DHAccessibilityService")
                .flattenToString();
        for (android.accessibilityservice.AccessibilityServiceInfo info : services) {
            if (info == null || info.resolveInfo == null || info.resolveInfo.serviceInfo == null) {
                continue;
            }
            String current = new ComponentName(info.resolveInfo.serviceInfo.packageName,
                    info.resolveInfo.serviceInfo.name).flattenToString();
            if (target.equals(current)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 读取系统设置中保存的已开启无障碍服务字符串（便于排查问题）。
     */
    @Nullable
    public static String getEnabledServicesSetting(Context context) {
        if (context == null) {
            return null;
        }
        try {
            String value = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            return TextUtils.isEmpty(value) ? null : value;
        } catch (Throwable t) {
            LogUtil.w("read enabled accessibility services failed: " + t.getMessage());
            return null;
        }
    }
}
