package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 房东个人资料 API。
 * 实名认证已改为人工审核制，提交入口统一为 POST /api/identity-verification
 * （见 IdentityVerificationController）。
 */
@Tag(name = "房东资料API")
@RestController
@RequestMapping("/api/landlord")
public class LandlordProfileController {

    private final UsersService usersService;

    public LandlordProfileController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/profile")
    public ResponseResult getProfile(@AuthenticationPrincipal Users currentUser) {
        Users user = usersService.getUserById(currentUser.getId());
        return ResponseResult.ok(null, Map.of("user", user == null ? Map.of() : user));
    }
}
