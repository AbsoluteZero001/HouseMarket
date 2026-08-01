package com.springboot.springboothousemarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaResult {
    private String captchaId;
    private String imageBase64;
}
