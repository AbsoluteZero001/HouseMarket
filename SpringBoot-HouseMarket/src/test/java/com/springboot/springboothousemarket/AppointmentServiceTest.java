package com.springboot.springboothousemarket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;
import com.springboot.springboothousemarket.Entity.Users;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private NotificationOutboxService outboxService;

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
}
