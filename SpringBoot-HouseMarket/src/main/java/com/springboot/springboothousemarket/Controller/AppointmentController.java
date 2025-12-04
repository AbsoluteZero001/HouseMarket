package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Service.AppointmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "预约管理API")
@RequestMapping("/api/appointments")
@RestController
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * 创建预约
     *
     * @param appointment 预约信息
     * @return 创建结果
     */
    @PostMapping
    public Map<String, Object> createAppointment(@RequestBody Appointment appointment) {
        Appointment createdAppointment = appointmentService.createAppointment(appointment);

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
    public Map<String, Object> getAllAppointments(@RequestParam(required = false) String status) {
        // 这里应该是从JWT中获取用户信息，然后根据用户角色和ID来获取相应的预约列表
        List<Appointment> appointments = appointmentService.getAppointmentsByUserIdAndStatus(null, status);

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
        boolean result = appointmentService.updateAppointmentStatus(id, "approved");

        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("success", true);
            response.put("message", "预约已批准");
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
        boolean result = appointmentService.updateAppointmentStatus(id, "rejected");

        Map<String, Object> response = new HashMap<>();
        if (result) {
            response.put("success", true);
            response.put("message", "预约已拒绝");
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
     * @param id 预约ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteAppointment(@PathVariable Long id) {
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