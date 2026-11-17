package com.example.dhassistant.task;

import androidx.annotation.NonNull;

/**
 * 需要选择账号的任务接口。
 *
 * <p>实现该接口的任务在面板中点击时会先弹出账号选择界面，
 * 用户选中某个 {@link Account} 后再开始执行。</p>
 */
public interface AccountAwareTask {

    /**
     * 设置当前执行使用的账号。
     */
    void setAccount(@NonNull Account account);

    /**
     * 是否需要在执行前选择账号。
     */
    boolean requiresAccountSelection();
}
