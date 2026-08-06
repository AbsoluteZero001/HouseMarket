package com.springboot.springboothousemarket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private NotificationOutboxService outboxService;

    @Test
    void createAppointmentWithSameRequestIdReturnsSameRecord() {
        Appointment appointment = new Appointment();
        appointment.setHouseId(1L);
        appointment.setTenantId(5L);
        appointment.setLandlordId(2L);
        appointment.setTime(LocalDateTime.of(2026, 8, 20, 10, 0));
        appointment.setLocation("望京SOHO T1 1006");
        appointment.setRequestId("itest-" + UUID.randomUUID());

        Appointment first = appointmentService.createAppointment(appointment);
        Appointment second = appointmentService.createAppointment(appointment);

        assertNotNull(first.getId());
        assertEquals(first.getId(), second.getId());

        List<NotificationOutbox> outboxRows = outboxService.list(
                new LambdaQueryWrapper<NotificationOutbox>()
                        .eq(NotificationOutbox::getAppointmentId, first.getId()));
        assertFalse(outboxRows.isEmpty());
        assertTrue(outboxRows.stream().anyMatch(row -> Long.valueOf(2L).equals(row.getTargetUserId())));
    }

    @Test
    void transitionIsAtomicAndIdempotent() {
        Appointment appointment = new Appointment();
        appointment.setHouseId(2L);
        appointment.setTenantId(6L);
        appointment.setLandlordId(2L);
        appointment.setTime(LocalDateTime.of(2026, 8, 21, 14, 0));
        appointment.setLocation("海淀区清华科技园B座108");
        appointment.setRequestId("itest-" + UUID.randomUUID());

        Appointment created = appointmentService.createAppointment(appointment);
        boolean first = appointmentService.transitionStatus(
                created.getId(), "pending", "approved", "APPROVE", 2L, "LANDLORD", "房东审批通过");
        boolean second = appointmentService.transitionStatus(
                created.getId(), "pending", "approved", "APPROVE", 2L, "LANDLORD", "重复审批");

        assertTrue(first);
        assertFalse(second);
    }
}
