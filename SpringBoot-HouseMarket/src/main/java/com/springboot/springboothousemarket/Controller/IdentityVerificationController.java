package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.IdentityVerification;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.IdentityVerificationService;
import com.springboot.springboothousemarket.dto.IdentityVerificationVO;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 实名认证（人工审核制）API：
 * 用户提交 → 管理员审核 → 通过后生效；拒绝可重新提交。
 * 所有出参均脱敏身份证号。
 */
@Tag(name = "实名认证API")
@RestController
@RequestMapping("/api")
public class IdentityVerificationController {

    private final IdentityVerificationService identityVerificationService;

    public IdentityVerificationController(IdentityVerificationService identityVerificationService) {
        this.identityVerificationService = identityVerificationService;
    }

    @GetMapping("/identity-verification/me")
    public ResponseResult getMine(@AuthenticationPrincipal Users currentUser) {
        IdentityVerification verification = identityVerificationService.getByUserId(currentUser.getId());
        return ResponseResult.ok(null, Map.of("verification",
                verification == null ? Map.of() : IdentityVerificationVO.from(verification)));
    }

    @PostMapping("/identity-verification")
    public ResponseResult submit(@AuthenticationPrincipal Users currentUser,
                                 @RequestBody Map<String, String> body) {
        IdentityVerification verification = identityVerificationService.submit(
                currentUser, body.get("realName"), body.get("idCardNo"));
        return ResponseResult.ok("实名认证申请已提交，请等待管理员审核",
                Map.of("verification", IdentityVerificationVO.from(verification)));
    }

    @GetMapping("/admin/identity-verifications")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseResult list(@RequestParam(required = false) String status) {
        List<IdentityVerification> list = identityVerificationService.listByStatus(status);
        return ResponseResult.ok(null, Map.of("verifications",
                list.stream().map(IdentityVerificationVO::from).toList()));
    }

    @PutMapping("/admin/identity-verifications/{id}/approve")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseResult approve(@PathVariable Long id,
                                  @AuthenticationPrincipal Users admin,
                                  @RequestBody(required = false) Map<String, String> body) {
        String note = body == null ? null : body.get("note");
        boolean ok = identityVerificationService.approve(id, admin.getId(), note == null ? "信息核验通过" : note);
        return ok ? ResponseResult.ok("实名认证已通过") : ResponseResult.fail("该申请已处理，无法重复审核");
    }

    @PutMapping("/admin/identity-verifications/{id}/reject")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseResult reject(@PathVariable Long id,
                                 @AuthenticationPrincipal Users admin,
                                 @RequestBody(required = false) Map<String, String> body) {
        String note = body == null ? null : body.get("note");
        boolean ok = identityVerificationService.reject(id, admin.getId(), note == null ? "信息核验未通过" : note);
        return ok ? ResponseResult.ok("实名认证已拒绝") : ResponseResult.fail("该申请已处理，无法重复审核");
    }
}
