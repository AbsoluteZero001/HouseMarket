package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.LandlordApplication;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.LandlordApplicationService;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "房东入驻审核API")
@RestController
@RequestMapping("/api")
public class LandlordApplicationController {

    private final LandlordApplicationService landlordApplicationService;

    public LandlordApplicationController(LandlordApplicationService landlordApplicationService) {
        this.landlordApplicationService = landlordApplicationService;
    }

    @GetMapping("/landlord/application")
    public ResponseResult getMyApplication(@AuthenticationPrincipal Users currentUser) {
        LandlordApplication application = landlordApplicationService.getByUserId(currentUser.getId());
        return ResponseResult.ok(null, Map.of("application", application));
    }

    @GetMapping("/admin/landlord-applications")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseResult listApplications(@RequestParam(required = false) String status) {
        List<LandlordApplication> applications = landlordApplicationService.listByStatus(status);
        return ResponseResult.ok(null, Map.of("applications", applications));
    }

    @PutMapping("/admin/landlord-applications/{id}/approve")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseResult approve(@PathVariable Long id,
                                  @AuthenticationPrincipal Users admin,
                                  @RequestBody(required = false) Map<String, String> body) {
        String note = body == null ? null : body.get("note");
        boolean result = landlordApplicationService.approve(id, admin.getId(), note == null ? "审核通过" : note);
        return result ? ResponseResult.ok("房东入驻申请已通过") : ResponseResult.fail("该申请已处理，无法重复审核");
    }

    @PutMapping("/admin/landlord-applications/{id}/reject")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseResult reject(@PathVariable Long id,
                                 @AuthenticationPrincipal Users admin,
                                 @RequestBody(required = false) Map<String, String> body) {
        String note = body == null ? null : body.get("note");
        boolean result = landlordApplicationService.reject(id, admin.getId(), note == null ? "资料不完整" : note);
        return result ? ResponseResult.ok("房东入驻申请已拒绝") : ResponseResult.fail("该申请已处理，无法重复审核");
    }
}
