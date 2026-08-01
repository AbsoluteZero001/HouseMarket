package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.dto.CaptchaResult;

public interface CaptchaService {

    CaptchaResult generate();

    boolean verify(String captchaId, String captchaCode);
}
