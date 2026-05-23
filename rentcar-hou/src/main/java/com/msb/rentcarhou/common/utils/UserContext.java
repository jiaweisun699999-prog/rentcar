package com.msb.rentcarhou.common.utils;

public class UserContext {
    private static final ThreadLocal<Long> USER_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_THREAD_LOCAL.set(userId);
    }

    public static Long getUserId() {
        return USER_THREAD_LOCAL.get();
    }

    public static void removeUserId() {
        USER_THREAD_LOCAL.remove();
    }
}
