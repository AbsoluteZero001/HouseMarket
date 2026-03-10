package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.AppointmentService;
import com.springboot.springboothousemarket.Service.HousesService;
import com.springboot.springboothousemarket.dto.AppointmentMessage;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "预约管理API")
@RequestMapping("/api/appointments")
@RestController
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final HousesService housesService;
    private final SimpMessagingTemplate messagingTemplate;

    public AppointmentController(AppointmentService appointmentService, HousesService housesService,
                                 SimpMessagingTemplate messagingTemplate) {
        this.appointmentService = appointmentService;
        this.housesService = housesService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 创建预约
     *
     * @param appointment 预约信息
     * @param currentUser 当前登录用户（租客）
     * @return 创建结果
     */
    @PostMapping
    public Map<String, Object> createAppointment(@RequestBody Appointment appointment,
                                                 @AuthenticationPrincipal Users currentUser) {
        // 验证当前登录用户是否为租客
        if (!"TENANT".equals(currentUser.getRole())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "只有租客才能创建预约");
            return response;
        }

        // 设置租客ID为当前登录用户ID
        appointment.setTenantId(currentUser.getId());

        // 从houses表中获取房东ID
        Houses house = housesService.getHouseById(appointment.getHouseId());
        if (house == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "房源不存在");
            return response;
        }

        Long landlordId = house.getLandlordId();
        appointment.setLandlordId(landlordId);

        // 验证租客不能预约自己的房源
        if (currentUser.getId().equals(landlordId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "不能预约自己的房源");
            return response;
        }

        // 验证必填字段
        if (appointment.getTime() == null || appointment.getLocation() == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "预约时间和地点不能为空");
            return response;
        }

        // 创建预约
        Appointment createdAppointment = appointmentService.createAppointment(appointment);

        // WebSocket通知房东有新的预约请求
        AppointmentMessage msg = new AppointmentMessage();
        msg.setAppointmentId(createdAppointment.getId());
        msg.setStatus(createdAppointment.getStatus());
        msg.setTenantId(currentUser.getId());
        msg.setLandlordId(house.getLandlordId());
        msg.setMessage("有新的预约请求");

        // 发送给指定房东
        messagingTemplate.convertAndSendToUser(
                house.getLandlordId().toString(),
                "/queue/appointment",
                msg);

        Map<String, Object> data = new HashMap<>();
        data.put("id", createdAppointment.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "预约提交成功，请等待房东确认");
        response.put("data", data);

        return response;
    }

    /**
     * 获取预约列表
     *
     * @param status 预约状态
     * @return 预约列表
     */
    @GetMapping
    public Map<String, Object> getAllAppointments(@RequestParam(required = false) String status,
                                                  @AuthenticationPrincipal Users currentUser) {
        List<Appointment> appointments;

        // 根据用户角色返回不同的预约列表
        if ("LANDLORD".equals(currentUser.getRole())) {
            // 房东角色，返回收到的预约（租客发起的）
            appointments = appointmentService.getAppointmentsByLandlordId(currentUser.getId(), status);
        } else if ("TENANT".equals(currentUser.getRole())) {
            // 租客角色，返回自己创建的预约
            appointments = appointmentService.getAppointmentsByUserIdAndStatus(currentUser.getId(), status);
        } else {
            // 管理员角色，返回所有预约（暂时使用租户的查询逻辑）
            appointments = appointmentService.getAppointmentsByUserIdAndStatus(currentUser.getId(), status);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("appointments", appointments);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return response;
    }

    /**
     * 批准预约
     *
     * @param id 预约ID
     * @return 操作结果
     */
    @PutMapping("/{id}/approve")
    public Map<String, Object> approveAppointment(@PathVariable Long id) {
        // 获取预约信息
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "预约不存在");
            return response;
        }

        // 更新预约状态
        boolean result = appointmentService.updateAppointmentStatus(id, "approved");

        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("success", true);
            response.put("message", "预约已批准");

            // WebSocket通知租客预约已批准
            AppointmentMessage msg = new AppointmentMessage();
            msg.setAppointmentId(id);
            msg.setStatus("approved");
            msg.setTenantId(appointment.getTenantId());
            msg.setLandlordId(appointment.getLandlordId());
            msg.setMessage("预约已批准");

            // 发送给指定租客
            messagingTemplate.convertAndSendToUser(
                    appointment.getTenantId().toString(),
                    "/queue/appointment",
                    msg);
        } else {
            response.put("success", false);
            response.put("message", "预约批准失败");
        }

        return response;
    }

    /**
     * 拒绝预约
     *
     * @param id 预约ID
     * @return 操作结果
     */
    @PutMapping("/{id}/reject")
    public Map<String, Object> rejectAppointment(@PathVariable Long id) {
        // 获取预约信息
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "预约不存在");
            return response;
        }

        // 更新预约状态
        boolean result = appointmentService.updateAppointmentStatus(id, "rejected");

        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("success", true);
            response.put("message", "预约已拒绝");

            // WebSocket通知租客预约已拒绝
            AppointmentMessage msg = new AppointmentMessage();
            msg.setAppointmentId(id);
            msg.setStatus("rejected");
            msg.setTenantId(appointment.getTenantId());
            msg.setLandlordId(appointment.getLandlordId());
            msg.setMessage("预约已拒绝");

            // 发送给指定租客
            messagingTemplate.convertAndSendToUser(
                    appointment.getTenantId().toString(),
                    "/queue/appointment",
                    msg);
        } else {
            response.put("success", false);
            response.put("message", "预约拒绝失败");
        }

        return response;
    }

    /**
     * 取消预约
     *
     * @param id 预约ID
     * @return 操作结果
     */
    @PutMapping("/{id}/cancel")
    public Map<String, Object> cancelAppointment(@PathVariable Long id) {
        boolean result = appointmentService.cancelAppointment(id);

        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("success", true);
            response.put("message", "预约已取消");
        } else {
            response.put("success", false);
            response.put("message", "预约取消失败");
        }

        return response;
    }

    /**
     * 删除预约
     *
     * @param id          预约ID
     * @param currentUser 当前登录用户
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteAppointment(@PathVariable Long id, @AuthenticationPrincipal Users currentUser) {
        // 获取预约信息
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "预约不存在");
            return response;
        }

        // 检查权限：只有房东或租客可以删除预约
        if (!appointment.getLandlordId().equals(currentUser.getId())
                && !appointment.getTenantId().equals(currentUser.getId())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "没有权限删除该预约");
            return response;
        }

        boolean result = appointmentService.deleteAppointment(id);

        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("success", true);
            response.put("message", "预约已删除");
        } else {
            response.put("success", false);
            response.put("message", "预约删除失败");
        }

        return response;
    }
}