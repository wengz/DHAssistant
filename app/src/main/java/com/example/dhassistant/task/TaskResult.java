package com.example.dhassistant.task;

/**
 * 自动化任务执行结果。
 */
public class TaskResult {

    private final boolean success;
    private final String message;

    private TaskResult(boolean success, String message) {
        this.success = success;
        this.message = message == null ? "" : message;
    }

    public static TaskResult success(String message) {
        return new TaskResult(true, message);
    }

    public static TaskResult success() {
        return new TaskResult(true, "");
    }

    public static TaskResult failure(String message) {
        return new TaskResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
