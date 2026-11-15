package com.example.dhassistant.task;

import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

public class Register extends BaseTask {


    public Register() {
        super("account.register", "注册账号", "注册新账号");
    }

    @NonNull
    @Override
    public TaskResult execute(@NonNull TaskContext context) {
        AccessibilityNodeInfo continueBtn = findById(context,"com.dhgate.buyermob:id/tv_continue");
        click(context, continueBtn);
        context.sleep(1000);

        AccessibilityNodeInfo agreeBtn = findById(context,"com.dhgate.buyermob:id/agree_tv");
        click(context, agreeBtn);
        context.sleep(1000);

        AccessibilityNodeInfo maybeLater = findById(context,"com.dhgate.buyermob:id/btn_close");
        click(context, maybeLater);
        context.sleep(1000);

        AccessibilityNodeInfo emailInput = findById(context,"com.dhgate.buyermob:id/edt_input_content");
        context.inputText(emailInput, "MuscatelloPangrazio600@outlook.com");
        context.sleep(1000);

        AccessibilityNodeInfo emailInputContinue = findById(context,"com.dhgate.buyermob:id/go");
        click(context, emailInputContinue);
        context.sleep(3000);

        AccessibilityNodeInfo pwdInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 1);
        context.inputText(pwdInput, "bectndz3587");
        context.sleep(1000);

        AccessibilityNodeInfo registerBtn = findById(context,"com.dhgate.buyermob:id/go");
        click(context, registerBtn);

        return TaskResult.success();
    }
}
