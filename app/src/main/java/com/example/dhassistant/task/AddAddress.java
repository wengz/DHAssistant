package com.example.dhassistant.task;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

public class AddAddress extends BaseTask {

    public static final String TAG = "AddAddress";

    public AddAddress() {
        super("account.addAddress", "添加地址", "添加新地址页面填充信息");
    }

    @NonNull
    @Override
    public TaskResult execute(@NonNull TaskContext context) {
        AccessibilityNodeInfo foldBtn = context.findById("com.dhgate.buyermob:id/iv_fold");
        click(context, foldBtn);
        context.sleep(1000);

        AccessibilityNodeInfo nameInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 0);
        context.inputText(nameInput, "Kyrenia Guerin");
        context.sleep(1000);

        AccessibilityNodeInfo phoneInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 2);
        context.inputText(phoneInput, "3011438219");
        context.sleep(1000);

        AccessibilityNodeInfo stateInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 6);
        Log.d(TAG, "stateInput: "+stateInput);
        Rect boundInScreen = new Rect();
        stateInput.getBoundsInScreen(boundInScreen);
        context.tapAt(boundInScreen.centerX(), boundInScreen.centerY());
        context.sleep(1000);

        AccessibilityNodeInfo stateSearchInput = context.findById("com.dhgate.buyermob:id/edt_input_content");
        context.inputText(stateSearchInput, "Maryland");
        context.sleep(1000);

        AccessibilityNodeInfo stateSearchResultItem = context.findById("com.dhgate.buyermob:id/rl_item");
        click(context, stateSearchResultItem);
        context.sleep(1000);

        AccessibilityNodeInfo enterManually = context.findById("com.dhgate.buyermob:id/tv_enter_manually");
        click(context, enterManually);
        context.sleep(1000);

        AccessibilityNodeInfo cityInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 7);
        context.inputText(cityInput, "Hoag");
        context.sleep(1000);

        AccessibilityNodeInfo zipInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 8);
        context.inputText(zipInput, "21590-2303");
        context.sleep(1000);

        AccessibilityNodeInfo streetInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 4);
        context.inputText(streetInput, "9124 Silver Moor");
        context.sleep(2000);

        AccessibilityNodeInfo saveBtn = context.findById("com.dhgate.buyermob:id/tv_save_address");
        click(context, saveBtn);
        context.sleep(1000);

        return TaskResult.success();
    }
}
