package com.springboot.springboothousemarket.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LandlordInfoVO {
    private Long id;
    private String username;
    private String nickname;
    private String realName;
    private String phone;
    private Integer realNameVerified;
    private String idCardNoMasked;
    private LocalDateTime verifiedTime;
}
