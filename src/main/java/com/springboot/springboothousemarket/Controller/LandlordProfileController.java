package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.dto.BusinessException;
import com.springboot.springboothousemarket.dto.ResponseResult;
import com.springboot.springboothousemarket.dto.UserVO;
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

    /**
     * 当前登录用户的资料。
     *
     * <p>旧实现直接返回 {@link Users} 实体，与其它接口统一使用的 {@link UserVO} 不一致，
     * 响应结构会随实体字段增减而漂移（前端拿到的字段集不可控）。
     * 这里统一改用 {@link UserVO}：字段白名单明确，也不会外泄 password / id_card_no 等敏感列。
     */
    @GetMapping("/profile")
    public ResponseResult getProfile(@AuthenticationPrincipal Users currentUser) {
        if (currentUser == null) {
            throw new SecurityException("请先登录");
        }
        Users user = usersService.getUserById(currentUser.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return ResponseResult.ok(null, Map.of("user", UserVO.from(user)));
    }
}
