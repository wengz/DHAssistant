package com.example.dhassistant.task.demo;

import android.content.Intent;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.example.dhassistant.task.BaseTask;
import com.example.dhassistant.task.TaskContext;
import com.example.dhassistant.task.TaskResult;

/**
 * 演示任务：启动系统设置。
 *
 * <p>任务实现只需关注「做什么」，具体能力由 {@link TaskContext} 提供。</p>
 */
public class OpenSettingsTask extends BaseTask {

    public OpenSettingsTask() {
        super("demo.open_settings",
                "打开系统设置",
                "演示任务：启动设置应用并等待界面就绪");
    }

    @NonNull
    @Override
    public TaskResult execute(@NonNull TaskContext context) {
        log("准备启动系统设置");
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        boolean started = context.getBridge().launchActivity(intent);
        if (!started) {
            return TaskResult.failure("启动系统设置失败");
        }
        context.sleep(1200);
        log("系统设置已启动");
        return TaskResult.success("已打开系统设置");
    }
}
