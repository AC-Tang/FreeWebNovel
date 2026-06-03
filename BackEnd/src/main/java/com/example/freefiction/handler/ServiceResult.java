package com.example.freefiction.handler;

/**
 * Service 层专用“裸结果”
 * 成功：data != null
 * 失败：data == null，message 为错误描述
 */
public record ServiceResult<T>(T data, String message) {
    // 1. 构造方法改成 public

    // 2. 或者保留 private，只对外提供静态工厂
    public static <T> ServiceResult<T> ok(T data) {
        return new ServiceResult<>(data, null);
    }

    public static <T> ServiceResult<T> fail(String message) {
        return new ServiceResult<>(null, message);
    }

    public boolean success() {
        return data != null|| message == null;
    }

}
