package com.example.freefiction.utils.result;

import com.example.freefiction.handler.Result;

import java.util.function.Supplier;

public final class ResultUtil {
    private ResultUtil() {}

    /**
     * 统一封装“查得到返回 200，查不到返回 404”的场景
     * @param queryFunction  查询动作（会返回 null）
     * @param errorCode      查不到时的错误码
     * @param errorMsg       查不到时的错误消息
     */
    public static <T> Result<T> of(
            Supplier<T> queryFunction,
            String successMsg,
            int errorCode,
            String errorMsg) {
        T obj = queryFunction.get();
        return obj != null ? Result.success(successMsg, obj) : Result.failed(errorCode, errorMsg);
    }
}
