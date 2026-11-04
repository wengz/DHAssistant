package com.example.dhassistant.task.demo;

import androidx.annotation.NonNull;

import com.example.dhassistant.task.BaseTask;
import com.example.dhassistant.task.TaskContext;
import com.example.dhassistant.task.TaskResult;

/**
 * 演示任务：滑动页面并返回。
 *
 * <p>演示「手势派发」与「全局动作」。</p>
 */
public class ScrollAndBackTask extends BaseTask {

    public ScrollAndBackTask() {
        super("demo.scroll_and_back",
                "滑动页面并返回",
                "演示任务：展示滑动与返回键操作");
    }

    @NonNull
    @Override
    public TaskResult execute(@NonNull TaskContext context) {
        for (int i = 0; i < 3; i++) {
            if (!context.swipeUp()) {
                log("第 " + (i + 1) + " 次滑动失败");
                break;
            }
            context.sleep(600);
        }
        boolean back = context.goBack();
        log("返回操作: " + back);
        return TaskResult.success(back ? "已滑动并返回" : "已滑动，返回失败");
    }
}
