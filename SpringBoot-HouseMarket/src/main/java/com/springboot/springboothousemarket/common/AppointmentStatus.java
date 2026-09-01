package com.springboot.springboothousemarket.common;

import java.util.Set;

/**
 * 预约状态机常量：
 * pending → approved → completed
 * ↘ rejected   ↘ canceled
 * pending 超时（看房时间已过或创建超过 N 小时未处理）→ expired
 */
public final class AppointmentStatus {

    public static final String PENDING = "pending";
    public static final String APPROVED = "approved";
    public static final String REJECTED = "rejected";
    public static final String COMPLETED = "completed";
    public static final String CANCELED = "canceled";
    public static final String EXPIRED = "expired";

    /**
     * 占用看房时间窗、参与冲突检测的状态。
     */
    public static final Set<String> OCCUPYING = Set.of(PENDING, APPROVED);
    public static final Set<String> ALL = Set.of(PENDING, APPROVED, REJECTED, COMPLETED, CANCELED, EXPIRED);

    private AppointmentStatus() {
    }

    public static boolean isValid(String status) {
        return status != null && ALL.contains(status);
    }
}
