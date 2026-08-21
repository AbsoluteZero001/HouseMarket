package com.springboot.springboothousemarket.dto;

import lombok.Data;

@Data
public class LandlordVerifyRequest {
    private String nickname;
    private String realName;
    private String idCardNo;
}
