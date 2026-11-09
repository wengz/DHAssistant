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
            AccessibilityNodeInfo addToCartBtn = null;
            while (addToCartBtn == null) {
                addToCartBtn = context.findById("com.dhgate.buyermob:id/btn_addtocart");
                context.sleep(100);
            }

            while(!context.click(addToCartBtn)){
                context.sleep(100);
            }
            context.sleep(1000);

            AccessibilityNodeInfo skuList = null;
            while (skuList == null){
                skuList = context.findById("com.dhgate.buyermob:id/sku_list");
                context.sleep(100);
            }
            if (childCount == 0) {
                childCount = skuList.getChildCount();
            }
            AccessibilityNodeInfo skuItem = skuList.getChild(actionCount);
            log("skuItem="+skuItem);
            while(!context.click(skuItem)){
                context.sleep(100);
            }
            context.sleep(1000);

            AccessibilityNodeInfo continueBtn = null;
            while (continueBtn == null) {
                continueBtn = context.findById("com.dhgate.buyermob:id/btn_paynow");
                context.sleep(100);
            }
            while(!context.click(continueBtn)){
                context.sleep(100);
            }
            actionCount++;
            log("in loop, end, childCount="+childCount);
        } while (actionCount < childCount);

        return TaskResult.success();
    }
}
