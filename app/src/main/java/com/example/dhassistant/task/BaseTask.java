package com.example.dhassistant.task;

import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 任务基类，负责标题/描述等样板代码。
 *
 * <p>新增任务时推荐继承本类：</p>
 * <pre>{@code
 * public class MyTask extends BaseTask {
 *     public MyTask() {
 *         super("demo.my_task", "我的任务", "描述");
 *     }
 *
 *     @NonNull
 *     @Override
 *     public TaskResult execute(@NonNull TaskContext context) {
 *         ...
 *     }
 * }
 * }</pre>
 */
public abstract class BaseTask implements AutomationTask {

    private final String id;
    private final String name;
    private final String description;

    public BaseTask(@NonNull String id, @NonNull String name, @NonNull String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public AccessibilityNodeInfo findById(TaskContext context, String id) {
        return  findById(context, id, 5);
    }

    public AccessibilityNodeInfo findById(TaskContext context, String id, int timeoutSec) {
        AccessibilityNodeInfo node = null;
        int tryCount = 0;
        while (node == null && tryCount++ < timeoutSec*10) {
            node = context.findById(id);
            context.sleep(100);
        }

        if (node != null) {
            return node;
        } else {
            throw new IllegalStateException("findById timeout id="+id);
        }
    }

    public void click(TaskContext context, AccessibilityNodeInfo node) {
        click(context, node, 5);
    }

    public void click(TaskContext context, AccessibilityNodeInfo node, int timeoutSec) {
        int tryCount = 0;
        boolean clickResult;
        while(!(clickResult = context.click(node)) && tryCount++ < timeoutSec*10){
            context.sleep(100);
        }
        if (!clickResult) {
            throw new IllegalStateException("click timeout ndoe="+node);
        }
    }

    @NonNull
    @Override
    public String getId() {
        return id;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * 任务图标资源 id，0 表示使用默认图标。
     */
    public int getIconRes() {
        return 0;
    }

    /**
     * 供子类记录日志使用。
     */
    protected void log(@Nullable String message) {
        if (message != null) {
            com.example.dhassistant.util.LogUtil.d("[" + id + "] " + message);
        }
    }
}
