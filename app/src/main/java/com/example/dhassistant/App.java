package com.example.dhassistant;

import android.app.Application;

/**
 * 应用入口。
 *
 * 说明：
 * - 悬浮窗由无障碍服务（{@link com.example.dhassistant.service.DHAccessibilityService}）创建，
 *   这样即使用户从未打开过 MainActivity，悬浮球也能正常出现；
 * - 这里只保留全局初始化的位置，方便以后放置全局配置、崩溃统计等。
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 全局初始化写在这里
    }
}
