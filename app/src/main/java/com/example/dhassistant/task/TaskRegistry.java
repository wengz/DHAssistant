package com.example.dhassistant.task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dhassistant.util.LogUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 预设任务注册表。
 *
 * <p>面板中的任务列表来自这里。后续补充具体自动化任务时，只需要：</p>
 * <ol>
 *     <li>新建 {@code MyTask extends BaseTask}；</li>
 *     <li>在 {@link #registerDefaultTasks()} 中调用 {@code register(new MyTask())}；</li>
 *     <li>编译安装即可在悬浮面板中看到新任务。</li>
 * </ol>
 */
public final class TaskRegistry {

    /** 使用 LinkedHashMap 保证面板中任务顺序稳定 */
    private static final Map<String, AutomationTask> TASKS = new LinkedHashMap<>();

    static {
        registerDefaultTasks();
    }

    private TaskRegistry() {
    }

    /**
     * 注册一个任务（同 id 会覆盖）。
     */
    public static void register(@NonNull AutomationTask task) {
        TASKS.put(task.getId(), task);
        LogUtil.d("注册任务: " + task.getId());
    }

    /**
     * 取消注册。
     */
    public static void unregister(@NonNull String taskId) {
        TASKS.remove(taskId);
    }

    /**
     * 获取全部任务（按注册顺序）。
     */
    @NonNull
    public static List<AutomationTask> getTasks() {
        return new ArrayList<>(TASKS.values());
    }

    /**
     * 按 id 获取任务。
     */
    @Nullable
    public static AutomationTask getTask(@NonNull String taskId) {
        return TASKS.get(taskId);
    }

    /**
     * 清空（单元测试用）。
     */
    public static void clear() {
        TASKS.clear();
    }

    /**
     * 默认任务列表。
     *
     * <p>在这里追加你的业务任务即可。</p>
     */
    private static void registerDefaultTasks() {
//        register(new com.example.dhassistant.task.demo.OpenSettingsTask());
//        register(new com.example.dhassistant.task.demo.SearchInSettingsTask());
//        register(new com.example.dhassistant.task.demo.ScrollAndBackTask());
        register(new AddSku());
    }
}
