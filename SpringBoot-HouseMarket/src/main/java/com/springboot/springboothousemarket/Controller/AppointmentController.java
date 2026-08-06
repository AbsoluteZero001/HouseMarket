package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Entity.AppointmentFlow;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.AppointmentService;
import com.springboot.springboothousemarket.Service.HousesService;
import com.springboot.springboothousemarket.dto.AppointmentMessage;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "预约管理API")
@RequestMapping("/api/appointments")
@RestController
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final HousesService housesService;
    public AppointmentController(AppointmentService appointmentService, HousesService housesService) {
        this.appointmentService = appointmentService;
        this.housesService = housesService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TENANT')")
    public ResponseResult createAppointment(@RequestBody Appointment appointment,
                                            @AuthenticationPrincipal Users currentUser) {
        if (!"TENANT".equals(currentUser.getRole())) {
            return ResponseResult.fail("只有租客才能创建预约");
        }

        appointment.setTenantId(currentUser.getId());

        Houses house = housesService.getHouseById(appointment.getHouseId());
        if (house == null) {
            return ResponseResult.fail("房源不存在");
        }

        Long landlordId = house.getLandlordId();
        appointment.setLandlordId(landlordId);

        if (currentUser.getId().equals(landlordId)) {
            return ResponseResult.fail("不能预约自己的房源");
        }

        if (appointment.getTime() == null || appointment.getLocation() == null) {
            return ResponseResult.fail("预约时间和地点不能为空");
        }

        Appointment createdAppointment = appointmentService.createAppointment(appointment);

        return ResponseResult.ok("预约提交成功，请等待房东确认", Map.of("id", createdAppointment.getId()));
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
        if (!"LANDLORD".equals(currentUser.getRole())) {
            return ResponseResult.fail("只有房东才能批准预约");
        }

        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return ResponseResult.fail("预约不存在");
        }
        if (!appointment.getLandlordId().equals(currentUser.getId())) {
            return ResponseResult.fail("只能批准自己房源的预约");
        }
        if (!"pending".equals(appointment.getStatus())) {
            return ResponseResult.fail("只能批准待处理的预约");
        }

        boolean result = appointmentService.transitionStatus(id, "pending", "approved", "APPROVE",
                currentUser.getId(), "LANDLORD", "房东审批通过");
        if (result) {
            appointmentService.recordNotification(id, "approved",
                    currentUser.getId(), "LANDLORD", "已通知租客审批结果");
            appointmentService.enqueueNotification(id, "APPOINTMENT_APPROVED",
                    "预约已批准", appointment.getTenantId());
            return ResponseResult.ok("预约已批准");
        }
        return ResponseResult.fail("预约批准失败");
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('LANDLORD')")
    public ResponseResult rejectAppointment(@PathVariable Long id,
                                            @AuthenticationPrincipal Users currentUser) {
        if (!"LANDLORD".equals(currentUser.getRole())) {
            return ResponseResult.fail("只有房东才能拒绝预约");
        }

        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return ResponseResult.fail("预约不存在");
        }
        if (!appointment.getLandlordId().equals(currentUser.getId())) {
            return ResponseResult.fail("只能拒绝自己房源的预约");
        }
        if (!"pending".equals(appointment.getStatus())) {
            return ResponseResult.fail("只能拒绝待处理的预约");
        }

        boolean result = appointmentService.transitionStatus(id, "pending", "rejected", "REJECT",
                currentUser.getId(), "LANDLORD", "房东拒绝本次预约");
        if (result) {
            appointmentService.recordNotification(id, "rejected",
                    currentUser.getId(), "LANDLORD", "已通知租客审批结果");
            appointmentService.enqueueNotification(id, "APPOINTMENT_REJECTED",
                    "预约已拒绝", appointment.getTenantId());
            return ResponseResult.ok("预约已拒绝");
        }
        return ResponseResult.fail("预约拒绝失败");
    }

    @PutMapping("/{id}/cancel")
    public ResponseResult cancelAppointment(@PathVariable Long id,
                                            @AuthenticationPrincipal Users currentUser) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return ResponseResult.fail("预约不存在");
        }

        boolean hasPermission = "ADMIN".equals(currentUser.getRole())
                || ("TENANT".equals(currentUser.getRole()) && appointment.getTenantId().equals(currentUser.getId()))
                || ("LANDLORD".equals(currentUser.getRole()) && appointment.getLandlordId().equals(currentUser.getId()));

        if (!hasPermission) {
            return ResponseResult.fail("没有权限取消该预约");
        }
        if (!"pending".equals(appointment.getStatus()) && !"approved".equals(appointment.getStatus())) {
            return ResponseResult.fail("只能取消待处理或已批准的预约");
        }

        String expectedStatus = appointment.getStatus();
        boolean result = appointmentService.transitionStatus(id, expectedStatus, "canceled", "CANCEL",
                currentUser.getId(), currentUser.getRole(), "预约已取消");
        if (result) {
            appointmentService.recordNotification(id, "canceled",
                    currentUser.getId(), currentUser.getRole(), "已通知对方取消结果");
            Long targetUserId = "TENANT".equals(currentUser.getRole())
                    ? appointment.getLandlordId()
                    : appointment.getTenantId();
            appointmentService.enqueueNotification(id, "APPOINTMENT_CANCELED",
                    "预约已取消", targetUserId);
            return ResponseResult.ok("预约已取消");
        }
        return ResponseResult.fail("预约取消失败");
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('LANDLORD')")
    public ResponseResult completeAppointment(@PathVariable Long id,
                                              @AuthenticationPrincipal Users currentUser) {
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return ResponseResult.fail("预约不存在");
        }
        if (!appointment.getLandlordId().equals(currentUser.getId())) {
            return ResponseResult.fail("只能完成自己房源的预约");
        }
        if (!"approved".equals(appointment.getStatus())) {
            return ResponseResult.fail("只有已批准的预约才能标记完成");
        }

        boolean result = appointmentService.transitionStatus(id, "approved", "completed", "COMPLETE",
                currentUser.getId(), "LANDLORD", "看房完成，预约闭环结束");
        if (result) {
            appointmentService.recordNotification(id, "completed",
                    currentUser.getId(), "LANDLORD", "已通知租客看房完成");
            appointmentService.enqueueNotification(id, "APPOINTMENT_COMPLETED",
                    "看房预约已完成", appointment.getTenantId());
            return ResponseResult.ok("预约已完成");
        }
        return ResponseResult.fail("预约完成失败");
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
