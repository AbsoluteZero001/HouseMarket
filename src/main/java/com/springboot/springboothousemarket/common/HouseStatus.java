package com.springboot.springboothousemarket.common;

import java.util.Set;

/**
 * 房源状态机常量：
 * PENDING_REVIEW(待审核) → NORMAL(已上架) → OFFLINE(已下架)
 * ↘ REJECTED(审核未通过，可修改后重新提交)
 */
public final class HouseStatus {

    public static final String PENDING_REVIEW = "PENDING_REVIEW";
    public static final String NORMAL = "NORMAL";
    public static final String OFFLINE = "OFFLINE";
    public static final String REJECTED = "REJECTED";

    public static final Set<String> ALL = Set.of(PENDING_REVIEW, NORMAL, OFFLINE, REJECTED);

    private HouseStatus() {
    }

    public static boolean isValid(String status) {
        return status != null && ALL.contains(status);
    }

    /**
     * 房东可执行的状态切换（管理员不受限，走独立审核入口）。
     */
    public static boolean canLandlordSwitch(String from, String to) {
        return (NORMAL.equals(from) && OFFLINE.equals(to))
                || (OFFLINE.equals(from) && NORMAL.equals(to));
    }
}
