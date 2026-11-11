package com.springboot.springboothousemarket.dto;

import com.springboot.springboothousemarket.Entity.IdentityVerification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 实名认证视图对象：身份证号脱敏后返回，任何接口不回传完整身份证号。
 * 同时带出审核 SLA 信息，让用户知道「还要等多久、什么时候会被自动拒绝」。
 */
@Data
@Schema(description = "实名认证申请（脱敏）")
public class IdentityVerificationVO {

    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String idCardNoMasked;
    private String status;
    private String reviewNote;
    private Long reviewerId;
    private Object reviewTime;
    private Object createTime;
    private Object updateTime;

    @Schema(description = "最近一次提交申请的时间（审核 SLA 计时起点）")
    private Object submitTime;

    @Schema(description = "审核 SLA 时长（小时）")
    private Integer slaHours;

    @Schema(description = "审核截止时间，超过该时间仍未审核将自动拒绝")
    private Object slaDeadline;

    @Schema(description = "剩余审核时长（小时，向下取整；已超期为 0）")
    private Long slaRemainingHours;

    @Schema(description = "是否已超过审核 SLA")
    private Boolean slaExpired;

    public static IdentityVerificationVO from(IdentityVerification v) {
        return from(v, 72);
    }

    public static IdentityVerificationVO from(IdentityVerification v, int slaHours) {
        if (v == null) {
            return null;
        }
        IdentityVerificationVO vo = new IdentityVerificationVO();
        vo.setId(v.getId());
        vo.setUserId(v.getUserId());
        vo.setUsername(v.getUsername());
        vo.setRealName(v.getRealName());
        vo.setIdCardNoMasked(mask(v.getIdCardNo()));
        vo.setStatus(v.getStatus());
        vo.setReviewNote(v.getReviewNote());
        vo.setReviewerId(v.getReviewerId());
        vo.setReviewTime(v.getReviewTime());
        vo.setCreateTime(v.getCreateTime());
        vo.setUpdateTime(v.getUpdateTime());

        LocalDateTime submitTime = v.getSubmitTime() != null ? v.getSubmitTime() : v.getCreateTime();
        vo.setSubmitTime(submitTime);
        vo.setSlaHours(slaHours);
        boolean pending = "pending".equals(v.getStatus());
        if (submitTime == null || slaHours <= 0) {
            vo.setSlaDeadline(null);
            vo.setSlaRemainingHours(0L);
            vo.setSlaExpired(false);
            return vo;
        }
        LocalDateTime deadline = submitTime.plusHours(slaHours);
        vo.setSlaDeadline(deadline);
        long remaining = pending ? ChronoUnit.HOURS.between(LocalDateTime.now(), deadline) : 0L;
        vo.setSlaRemainingHours(Math.max(remaining, 0L));
        vo.setSlaExpired(pending && LocalDateTime.now().isAfter(deadline));
        return vo;
    }

    public static String mask(String idCardNo) {
        if (idCardNo == null || idCardNo.length() < 8) {
            return null;
        }
        return idCardNo.substring(0, 4) + "***********" + idCardNo.substring(idCardNo.length() - 3);
    }
}
