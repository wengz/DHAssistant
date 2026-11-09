package com.example.dhassistant.task;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

public class AddSku extends BaseTask {

    public AddSku() {
        super("order.addSku", "添加全部sku", "产品详情页中将所有的sku加入购物车");
    }

    @NonNull
    @Override
    public TaskResult execute(@NonNull TaskContext context) {
        log("execute start");

        int childCount = 0;
        int actionCount = 0;
        do {
            log("in loop actionCount="+actionCount);
            AccessibilityNodeInfo addToCartBtn = findById(context,"com.dhgate.buyermob:id/btn_addtocart");
            click(context, addToCartBtn);
            context.sleep(1000);

            AccessibilityNodeInfo skuList = findById(context,"com.dhgate.buyermob:id/sku_list");
            if (childCount == 0) {
                childCount = skuList.getChildCount();
            }
            AccessibilityNodeInfo skuItem = skuList.getChild(actionCount);
            log("skuItem="+skuItem);
            click(context, skuItem);
            context.sleep(1000);

            AccessibilityNodeInfo continueBtn = findById(context,"com.dhgate.buyermob:id/btn_paynow");
            click(context, continueBtn);

            actionCount++;
            log("in loop, end, childCount="+childCount);
        } while (actionCount < childCount);

        return TaskResult.success();
    }
}
