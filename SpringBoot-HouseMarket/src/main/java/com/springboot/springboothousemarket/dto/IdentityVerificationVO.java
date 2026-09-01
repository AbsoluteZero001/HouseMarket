package com.springboot.springboothousemarket.dto;

import com.springboot.springboothousemarket.Entity.IdentityVerification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 实名认证视图对象：身份证号脱敏后返回，任何接口不回传完整身份证号。
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

    public static IdentityVerificationVO from(IdentityVerification v) {
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
        return vo;
    }

    public static String mask(String idCardNo) {
        if (idCardNo == null || idCardNo.length() < 8) {
            return null;
        }
        return idCardNo.substring(0, 4) + "***********" + idCardNo.substring(idCardNo.length() - 3);
    }
}
