package com.example.dhassistant.task;

import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

/**
 * 注册账号任务。
 *
 * <p>执行时需要提供 {@link Account}（邮箱 + 密码）。</p>
 */
public class Register extends BaseTask implements AccountAwareTask {

    private Account account;

    public Register() {
        super("account.register", "注册账号", "从 account.txt 中选择账号后注册新账号");
    }

    @Override
    public void setAccount(@NonNull Account account) {
        this.account = account;
    }

    @Override
    public boolean requiresAccountSelection() {
        return true;
    }

    @NonNull
    @Override
    public TaskResult execute(@NonNull TaskContext context) {
        if (account == null) {
            return TaskResult.failure("未选择账号，请先在面板中选择一个账号");
        }
        String email = account.getEmail();
        String password = account.getPassword();
        log("使用账号注册: " + email);

        AccessibilityNodeInfo continueBtn = findById(context, "com.dhgate.buyermob:id/tv_continue");
        click(context, continueBtn);
        context.sleep(1000);

        AccessibilityNodeInfo agreeBtn = findById(context, "com.dhgate.buyermob:id/agree_tv");
        click(context, agreeBtn);
        context.sleep(1000);

        AccessibilityNodeInfo maybeLater = findById(context, "com.dhgate.buyermob:id/btn_close");
        click(context, maybeLater);
        context.sleep(1000);

        AccessibilityNodeInfo emailInput = findById(context, "com.dhgate.buyermob:id/edt_input_content");
        context.inputText(emailInput, email);
        context.sleep(1000);

        AccessibilityNodeInfo emailInputContinue = findById(context, "com.dhgate.buyermob:id/go");
        click(context, emailInputContinue);
        context.sleep(3000);

        AccessibilityNodeInfo pwdInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 1);
        context.inputText(pwdInput, password);
        context.sleep(1000);

        AccessibilityNodeInfo registerBtn = findById(context, "com.dhgate.buyermob:id/go");
        click(context, registerBtn);

        return TaskResult.success();
    }
}
