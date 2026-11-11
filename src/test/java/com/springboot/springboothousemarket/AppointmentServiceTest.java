package com.springboot.springboothousemarket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.AppointmentFlowService;
import com.springboot.springboothousemarket.Service.AppointmentService;
import com.springboot.springboothousemarket.Service.NotificationOutboxService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private NotificationOutboxService outboxService;

    @Autowired
    private AppointmentFlowService flowService;

    private Users operator(Long id, String role) {
        Users user = new Users();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    @Test
    void createAppointmentWithSameRequestIdReturnsSameRecord() {
        Appointment appointment = new Appointment();
        appointment.setHouseId(1L);
        appointment.setTime(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0));
        appointment.setLocation("望京SOHO T1 1006");
        appointment.setRequestId("itest-" + UUID.randomUUID());

        Appointment first = appointmentService.createAppointment(appointment, operator(5L, "TENANT"));
        Appointment second = appointmentService.createAppointment(appointment, operator(5L, "TENANT"));

        assertNotNull(first.getId());
        assertEquals(first.getId(), second.getId());

        List<NotificationOutbox> outboxRows = outboxService.list(
                new LambdaQueryWrapper<NotificationOutbox>()
                        .eq(NotificationOutbox::getAppointmentId, first.getId()));
        assertFalse(outboxRows.isEmpty());
        assertTrue(outboxRows.stream().anyMatch(row -> Long.valueOf(2L).equals(row.getTargetUserId())));
    }

    @Test
    void approveIsAtomicAndSecondApproveFails() {
        Appointment appointment = new Appointment();
        appointment.setHouseId(2L);
        appointment.setTime(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0));
        appointment.setLocation("海淀区清华科技园B座108");
        appointment.setRequestId("itest-" + UUID.randomUUID());

        Appointment created = appointmentService.createAppointment(appointment, operator(6L, "TENANT"));
        boolean first = appointmentService.approve(created.getId(), operator(2L, "LANDLORD"));

        assertTrue(first);
        // 重复审批：状态机拒绝
        assertThrows(Exception.class, () -> appointmentService.approve(created.getId(), operator(2L, "LANDLORD")));

        // 通知已在审批事务内落库
        List<NotificationOutbox> rows = outboxService.list(
                new LambdaQueryWrapper<NotificationOutbox>()
                        .eq(NotificationOutbox::getAppointmentId, created.getId())
                        .eq(NotificationOutbox::getEventType, "APPOINTMENT_APPROVED"));
        assertFalse(rows.isEmpty());
    }

    @Test
    void pastTimeIsRejected() {
        Appointment appointment = new Appointment();
        appointment.setHouseId(1L);
        appointment.setTime(LocalDateTime.now().minusDays(1));
        appointment.setLocation("望京SOHO T1 1006");
        appointment.setRequestId("itest-" + UUID.randomUUID());

        assertThrows(Exception.class, () -> appointmentService.createAppointment(appointment, operator(5L, "TENANT")));
    }

    /**
     * 回归：删除预约必须先清理子表。
     * appointment_flow(fk_flow_appointment) 与 notification_outbox(fk_outbox_appointment)
     * 对 appointment 的外键都没有 ON DELETE CASCADE，旧实现只删主表，
     * 必然触发 ERROR 1451，导致「删除预约」功能 100% 失败。
     */
    @Test
    void deleteAppointmentRemovesChildRowsWithoutForeignKeyError() {
        Appointment appointment = new Appointment();
        appointment.setHouseId(1L);
        appointment.setTime(LocalDateTime.now().plusDays(5).withHour(16).withMinute(0));
        appointment.setLocation("望京SOHO T1 1006");
        appointment.setRequestId("itest-" + UUID.randomUUID());

        Appointment created = appointmentService.createAppointment(appointment, operator(5L, "TENANT"));
        assertNotNull(created.getId());

        // 前置断言：确实存在子表数据，否则删除用例没有意义
        assertFalse(flowService.getFlowsByAppointmentId(created.getId()).isEmpty());
        assertFalse(outboxService.list(new LambdaQueryWrapper<NotificationOutbox>()
                .eq(NotificationOutbox::getAppointmentId, created.getId())).isEmpty());

        assertTrue(appointmentService.deleteAppointment(created.getId()));

        assertTrue(flowService.getFlowsByAppointmentId(created.getId()).isEmpty());
        assertTrue(outboxService.list(new LambdaQueryWrapper<NotificationOutbox>()
                .eq(NotificationOutbox::getAppointmentId, created.getId())).isEmpty());
        assertNull(appointmentService.getById(created.getId()));
    }

    /**
     * 回归：状态过滤必须与「数据归属」条件同时生效。
     * 旧 SQL 写成 {@code a.tenant_id = #{userId} OR a.landlord_id = #{userId} AND a.status = #{status}}，
     * 由于 AND 优先级高于 OR，实际退化成「租客的全部记录 OR 房东的状态匹配记录」，
     * 结果租客在「待处理」列表里会看到已被批准/已拒绝的预约。
     */
    @Test
    void statusFilterIsAppliedTogetherWithUserScope() {
        Appointment appointment = new Appointment();
        appointment.setHouseId(3L); // 房源 3 属于 landlord 3
        appointment.setTime(LocalDateTime.now().plusDays(6).withHour(11).withMinute(0));
        appointment.setLocation("金融街高端一居室");
        appointment.setRequestId("itest-" + UUID.randomUUID());

        Appointment created = appointmentService.createAppointment(appointment, operator(5L, "TENANT"));
        assertEquals(3L, created.getLandlordId());
        assertTrue(appointmentService.approve(created.getId(), operator(3L, "LANDLORD")));

        List<Appointment> pending = appointmentService.getAppointmentsByUserIdAndStatus(5L, "pending");
        assertTrue(pending.stream().noneMatch(a -> created.getId().equals(a.getId())),
                "已批准的预约不应出现在「待处理」筛选结果中");

        List<Appointment> approved = appointmentService.getAppointmentsByUserIdAndStatus(5L, "approved");
        assertTrue(approved.stream().anyMatch(a -> created.getId().equals(a.getId())),
                "已批准的预约应出现在「已批准」筛选结果中");
    }
}
