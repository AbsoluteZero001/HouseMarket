package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Entity.AppointmentFlow;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.AppointmentService;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 预约 API（薄控制器）：权限注解 + 参数绑定，
 * 所有状态机流转、轨迹、通知均收敛在 AppointmentService 内，同一事务完成。
 */
@Tag(name = "预约管理API")
@RequestMapping("/api/appointments")
@RestController
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TENANT')")
    public ResponseResult createAppointment(@RequestBody Appointment appointment,
                                            @AuthenticationPrincipal Users currentUser) {
        Appointment created = appointmentService.createAppointment(appointment, currentUser);
        return ResponseResult.ok("预约提交成功，请等待房东确认", Map.of("id", created.getId()));
    }

    @GetMapping
    public ResponseResult getAllAppointments(@RequestParam(required = false) String status,
                                             @AuthenticationPrincipal Users currentUser) {
        List<Appointment> appointments = switch (currentUser.getRole()) {
            case "LANDLORD" -> appointmentService.getAppointmentsByLandlordId(currentUser.getId(), status);
            case "TENANT" -> appointmentService.getAppointmentsByUserIdAndStatus(currentUser.getId(), status);
            case "ADMIN" -> appointmentService.getAllAppointments(status);
            default -> List.of();
        };
        return ResponseResult.ok(null, Map.of("appointments", appointments));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('LANDLORD')")
    public ResponseResult approveAppointment(@PathVariable Long id,
                                             @AuthenticationPrincipal Users currentUser) {
        return appointmentService.approve(id, currentUser)
                ? ResponseResult.ok("预约已批准") : ResponseResult.fail("预约批准失败");
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('LANDLORD')")
    public ResponseResult rejectAppointment(@PathVariable Long id,
                                            @AuthenticationPrincipal Users currentUser) {
        return appointmentService.reject(id, currentUser)
                ? ResponseResult.ok("预约已拒绝") : ResponseResult.fail("预约拒绝失败");
    }

    @PutMapping("/{id}/cancel")
    public ResponseResult cancelAppointment(@PathVariable Long id,
                                            @AuthenticationPrincipal Users currentUser) {
        return appointmentService.cancel(id, currentUser)
                ? ResponseResult.ok("预约已取消") : ResponseResult.fail("预约取消失败");
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('LANDLORD')")
    public ResponseResult completeAppointment(@PathVariable Long id,
                                              @AuthenticationPrincipal Users currentUser) {
        return appointmentService.complete(id, currentUser)
                ? ResponseResult.ok("预约已完成") : ResponseResult.fail("预约完成失败");
    }

    @DeleteMapping("/{id}")
    public ResponseResult deleteAppointment(@PathVariable Long id, @AuthenticationPrincipal Users currentUser) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return ResponseResult.fail("预约不存在");
        }
        boolean isParticipant = appointment.getLandlordId().equals(currentUser.getId())
                || appointment.getTenantId().equals(currentUser.getId());
        if (!isParticipant && !"ADMIN".equals(currentUser.getRole())) {
            return ResponseResult.fail("没有权限删除该预约");
        }
        boolean result = appointmentService.deleteAppointment(id);
        return result ? ResponseResult.ok("预约已删除") : ResponseResult.fail("预约删除失败");
    }

    @GetMapping("/{id}/flow")
    public ResponseResult getAppointmentFlow(@PathVariable Long id,
                                             @AuthenticationPrincipal Users currentUser) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return ResponseResult.fail("预约不存在");
        }
        boolean isParticipant = appointment.getLandlordId().equals(currentUser.getId())
                || appointment.getTenantId().equals(currentUser.getId());
        if (!isParticipant && !"ADMIN".equals(currentUser.getRole())) {
            return ResponseResult.fail("没有权限查看该预约流程");
        }
        List<AppointmentFlow> flows = appointmentService.getFlows(id);
        return ResponseResult.ok(null, Map.of("flows", flows));
    }
}
