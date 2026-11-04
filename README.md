# DHAssistant

一个基于 Android `AccessibilityService` 的轻量级自动化任务辅助工具骨架。

## 功能概览

- 通过无障碍服务执行界面自动化任务；
- 可拖拽悬浮球 + 主面板悬浮窗；
- 主面板列出预设任务，点击即可执行；
- 任务框架预留扩展点，新增任务只需写一个 `AutomationTask` 并注册到 `TaskRegistry`。

## 模块结构

```
app/src/main/java/com/example/dhassistant
├── App.java                          // Application 入口
├── main/MainActivity.java            // 宿主界面：权限引导
├── service/
│   ├── DHAccessibilityService.java   // 自动化核心 + 悬浮窗宿主
│   ├── AccessibilityBridge.java      // 任务层访问无障碍能力的桥接接口
│   └── AccessibilityEventDispatcher  // 监听器注册/分发
├── task/
│   ├── AutomationTask.java           // 任务接口
│   ├── BaseTask.java                 // 任务基类
│   ├── TaskContext.java              // 任务执行上下文（查找/点击/输入/等待/滑动）
│   ├── TaskResult.java               // 执行结果
│   ├── TaskRegistry.java             // 任务注册表（面板数据源）
│   └── demo/                         // 三个演示任务
├── ui/overlay/
│   ├── OverlayController.java        // 悬浮窗调度
│   ├── FloatingBallView.java         // 可拖拽的小标识
│   ├── TaskPanelView.java            // 主面板
│   └── WindowPermissionHelper.java   // 悬浮窗权限工具
└── util/
    ├── AccessibilityHelper.java      // 检测无障碍服务是否开启
    ├── LogUtil.java                  // 统一日志
    └── MainThread.java               // 主线程调度
```

## 接入步骤

1. **编译安装**：`./gradlew :app:installDebug`。
2. **授予悬浮窗权限**：进入 MainActivity，点击「去授予悬浮窗权限」。
3. **开启无障碍服务**：点击「去开启无障碍服务」，在系统设置里找到 `DHAssistant` 并开启。
4. **打开悬浮球**：返回 MainActivity，点击「显示悬浮球」，桌面将出现可拖拽小球。
5. **执行任务**：点击小球展开主面板 → 选中任务 → 在面板底部查看执行结果。

## 扩展新任务

```java
public class MyTask extends BaseTask {
    public MyTask() {
        super("mypkg.my_task", "我的任务", "说明文字");
    }

    @NonNull
    @Override
    public TaskResult execute(@NonNull TaskContext context) {
        // 1. 启动应用
        if (!context.launchApp("com.example.target")) {
            return TaskResult.failure("无法启动目标应用");
        }
        // 2. 等待并点击某个 viewId
        if (!context.clickById("com.example.target:id/button")) {
            return TaskResult.failure("未找到按钮");
        }
        // 3. 输入文本
        AccessibilityNodeInfo input = context.waitForId("android:id/input", 5000);
        if (!context.inputText(input, "Hello")) {
            return TaskResult.failure("输入失败");
        }
        return TaskResult.success("任务完成");
    }
}
```

然后在 `TaskRegistry#registerDefaultTasks()` 中追加 `register(new MyTask())`，即可在面板中看到该任务。

## 注意事项

- `minSdk = 28`，`targetSdk = 36`，覆盖国内主流设备；
- `SYSTEM_ALERT_WINDOW` 与无障碍权限为运行时手动开启，本框架仅作引导；
- 悬浮窗与任务执行均通过无障碍服务宿主，便于随应用进程常驻；
- 手势派发、Activity 启动等 API 均通过 `MainThread` 调度到主线程，避免 ANR。

## 待办

- 适配深色主题悬浮窗配色（当前半透明面板两种模式均可用，但暗色模式下对比度可进一步打磨）；
- 引入权限监听广播，悬浮窗权限变化时自动重显小球；
- 提供脚本式 DSL，让简单任务无需写 Java。
