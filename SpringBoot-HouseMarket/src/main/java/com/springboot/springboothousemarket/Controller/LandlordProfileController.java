package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.dto.LandlordVerifyRequest;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "房东实名认证API")
@RestController
@RequestMapping("/api/landlord")
public class LandlordProfileController {

    private final UsersService usersService;

    public LandlordProfileController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/profile")
    public ResponseResult getProfile(@AuthenticationPrincipal Users currentUser) {
        return ResponseResult.ok(null, Map.of("user", usersService.getUserById(currentUser.getId())));
    }

    @PutMapping("/verify")
    public ResponseResult verify(@RequestBody LandlordVerifyRequest request,
                                 @AuthenticationPrincipal Users currentUser) {
        Users user = usersService.verifyLandlord(currentUser.getId(), request.getNickname(),
                request.getRealName(), request.getIdCardNo());
        return ResponseResult.ok("实名认证成功", Map.of("user", user));
    }
}
