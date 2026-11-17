package com.example.dhassistant.task;

import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import net.datafaker.Faker;

import java.util.Locale;

public class AddAddress extends BaseTask {

    public static final String TAG = "AddAddress";

    public AddAddress() {
        super("account.addAddress", "添加地址", "添加新地址页面填充信息");
    }



    @NonNull
    @Override
    public TaskResult execute(@NonNull TaskContext context) {
        Faker faker = new Faker(Locale.US);
        String name = faker.name().fullName().replaceAll("[^a-zA-Z ]", "");
        if (name.length() > 20) {
            name = name.substring(0, 20);
        }
        String streetAddress = faker.address().streetAddress();
        String city = faker.address().city();
        String state = faker.address().state();
        String zipCode = faker.address().zipCode();
        String phone = faker.phoneNumber().phoneNumber().replaceAll("[^0-9]", "");
        if (phone.length() > 10) {
            phone = phone.substring(0, 10);
        }

        Log.d(TAG, "name="+name);
        Log.d(TAG, "streetAddress="+streetAddress);
        Log.d(TAG, "city="+city);
        Log.d(TAG, "state="+state);
        Log.d(TAG, "zipCode="+zipCode);
        Log.d(TAG, "phone="+phone);


        AccessibilityNodeInfo foldBtn = context.findById("com.dhgate.buyermob:id/iv_fold");
        click(context, foldBtn);
        context.sleep(1000);

        AccessibilityNodeInfo nameInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 0);
        context.inputText(nameInput, name);
        context.sleep(1000);

        AccessibilityNodeInfo phoneInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 2);
        context.inputText(phoneInput, phone);
        context.sleep(1000);

        AccessibilityNodeInfo stateInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 6);
        Rect boundInScreen = new Rect();
        stateInput.getBoundsInScreen(boundInScreen);
        context.tapAt(boundInScreen.centerX(), boundInScreen.centerY());
        context.sleep(1000);

        AccessibilityNodeInfo stateSearchInput = context.findById("com.dhgate.buyermob:id/edt_input_content");
        context.inputText(stateSearchInput, state);
        context.sleep(1000);

        AccessibilityNodeInfo stateSearchResultItem = context.findById("com.dhgate.buyermob:id/rl_item");
        click(context, stateSearchResultItem);
        context.sleep(1000);

        AccessibilityNodeInfo enterManually = context.findById("com.dhgate.buyermob:id/tv_enter_manually");
        click(context, enterManually);
        context.sleep(1000);

        AccessibilityNodeInfo cityInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 7);
        context.inputText(cityInput, city);
        context.sleep(1000);

        AccessibilityNodeInfo zipInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 8);
        context.inputText(zipInput, zipCode);
        context.sleep(1000);

        AccessibilityNodeInfo streetInput = context.findById("com.dhgate.buyermob:id/edt_input_content", 4);
        context.inputText(streetInput, streetAddress);
        context.sleep(2000);

        AccessibilityNodeInfo saveBtn = context.findById("com.dhgate.buyermob:id/tv_save_address");
        click(context, saveBtn);
        context.sleep(1000);

        return TaskResult.success();
    }
}
