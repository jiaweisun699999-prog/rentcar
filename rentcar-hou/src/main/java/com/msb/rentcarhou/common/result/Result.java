package com.msb.rentcarhou.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return success("success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(message);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        String errorMsg = (msg == null || msg.trim().isEmpty()) ? "服务器处理失败，请稍后重试" : msg;
        result.setCode(code);
        result.setMsg(errorMsg);
        result.setMessage(errorMsg);
        return result;
    }

    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }
}
