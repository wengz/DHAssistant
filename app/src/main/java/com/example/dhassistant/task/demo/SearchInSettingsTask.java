package com.example.dhassistant.task.demo;

import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import com.example.dhassistant.task.BaseTask;
import com.example.dhassistant.task.TaskContext;
import com.example.dhassistant.task.TaskResult;

/**
 * 演示任务：在系统设置中搜索关键词。
 *
 * <p>演示「查找元素 → 点击 → 输入文本」的典型流程。</p>
 */
public class SearchInSettingsTask extends BaseTask {

    private static final String KEYWORD = "WLAN";

    public SearchInSettingsTask() {
        super("demo.search_in_settings",
                "在设置中搜索",
                "演示任务：启动设置、点击搜索框并输入关键词");
    }

    @NonNull
    @Override
    public TaskResult execute(@NonNull TaskContext context) {
        // 1. 启动设置
        if (!new OpenSettingsTask().execute(context).isSuccess()) {
            return TaskResult.failure("启动系统设置失败");
        }
        context.sleep(1000);

        // 2. 查找搜索框（不同 ROM 的 id 不一致，这里做多重兜底）
        AccessibilityNodeInfo searchBox = context.findById("com.android.settings:id/search_action_bar");
        if (searchBox == null) {
            searchBox = context.findById("com.android.settings:id/search_action_bar_text");
        }
        if (searchBox == null) {
            searchBox = context.findById("android:id/search_action_bar");
        }
        if (searchBox == null) {
            searchBox = context.findByText("搜索");
        }
        if (searchBox == null) {
            return TaskResult.failure("未找到设置中的搜索框");
        }

        // 3. 点击搜索框
        if (!context.click(searchBox)) {
            return TaskResult.failure("点击搜索框失败");
        }
        context.sleep(500);

        // 4. 输入文本
        AccessibilityNodeInfo input = context.findById("android:id/search_src_text");
        if (input == null) {
            input = context.findById("com.android.settings:id/search_src_text");
        }
        if (input == null) {
            return TaskResult.failure("未找到搜索输入框");
        }
        if (!context.inputText(input, KEYWORD)) {
            return TaskResult.failure("输入关键词失败");
        }
        log("已输入关键词: " + KEYWORD);
        return TaskResult.success("已在设置中搜索: " + KEYWORD);
    }
}
