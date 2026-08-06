package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.LandlordApplication;

import java.util.List;

public interface LandlordApplicationService extends IService<LandlordApplication> {

    LandlordApplication submit(Long userId, String username, String realName, String phone);

    LandlordApplication getByUserId(Long userId);

    List<LandlordApplication> listByStatus(String status);

    boolean approve(Long id, Long reviewerId, String note);

    boolean reject(Long id, Long reviewerId, String note);

    boolean hasApproved(Long userId);
}
