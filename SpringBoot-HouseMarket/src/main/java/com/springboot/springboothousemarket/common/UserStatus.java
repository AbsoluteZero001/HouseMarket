package com.springboot.springboothousemarket.common;

/**
 * 用户账号状态常量。
 */
public final class UserStatus {

    public static final String NORMAL = "normal";
    public static final String DISABLED = "disabled";

    private UserStatus() {
    }

    public static boolean isValid(String status) {
        return NORMAL.equals(status) || DISABLED.equals(status);
    }
}
