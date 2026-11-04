package com.example.dhassistant.task;

import androidx.annotation.NonNull;

/**
 * 自动化任务接口。
 *
 * <p>新增一个预设任务只需要三步：</p>
 * <ol>
 *     <li>实现本接口（或继承 {@link BaseTask}）；</li>
 *     <li>在 {@link TaskContext} 中使用查找/点击/输入等能力；</li>
 *     <li>在 {@link TaskRegistry} 中注册。</li>
 * </ol>
 *
 * <p>{@link #execute(TaskContext)} 在工作线程执行，可以阻塞等待界面出现，
 * 因此编写体验接近同步代码；但所有对界面的实际操作都会被调度到无障碍服务所在的主线程。</p>
 */
public interface AutomationTask {

    /**
     * 任务唯一 ID，建议使用 "模块.动作" 形式，例如 "demo.open_settings"。
     */
    @NonNull
    String getId();

    /**
     * 任务显示名称（面板列表主标题）。
     */
    @NonNull
    String getName();

    /**
     * 任务描述（面板列表副标题）。
     */
    @NonNull
    String getDescription();

    /**
     * 执行任务。
     *
     * @param context 任务上下文，提供查找/点击/输入/等待等能力
     * @return 执行结果
     */
    @NonNull
    TaskResult execute(@NonNull TaskContext context);
}
