package com.springboot.springboothousemarket;

import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Service.AppointmentService;
import com.springboot.springboothousemarket.common.AppointmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * pending 预约自动过期规则（BUG-2）回归测试。
 *
 * <p>旧实现只有一个过期条件：「创建满 N 小时仍未处理」。
 * 这会把「今天预约下周看房」的记录在第二天（创建满 24h）直接判为过期 ——
 * 房东还没来得及答复，租客的预约已经作废，业务事实上被中断。
 *
 * <p>修复后的规则是二者满足其一：
 * <ol>
 *   <li>看房时间已过（{@code time < now}）；</li>
 *   <li>看房时间已进入「临近看房」窗口（{@code time < now + lead-hours}）
 *       <b>且</b>创建已超过 {@code expire-hours} 小时仍未处理。</li>
 * </ol>
 */
@SpringBootTest
@Transactional
class AppointmentExpiryTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 直接写库插入一条 pending 预约。
     * 走 JdbcTemplate 而不是 service，是为了精确控制 create_time / time 两个字段 ——
     * 而这两个字段正是过期判定的全部输入。
     */
    private Long insertPending(LocalDateTime time, LocalDateTime createTime) {
        String requestId = "expiry-" + UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO appointment (house_id, tenant_id, landlord_id, time, location, request_id, "
                        + "status, version, create_time, update_time) "
                        + "VALUES (1, 5, 2, ?, ?, ?, 'pending', 0, ?, ?)",
                Timestamp.valueOf(time), "过期测试地点", requestId,
                Timestamp.valueOf(createTime), Timestamp.valueOf(createTime));
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM appointment WHERE request_id = ?", Long.class, requestId);
        assertNotNull(id);
        return id;
    }

    private String statusOf(Long id) {
        Appointment appointment = appointmentService.getById(id);
        assertNotNull(appointment, "预约不应被物理删除：" + id);
        return appointment.getStatus();
    }

    /**
     * 远期预约（看房在 3 天后）即使创建已超过 24 小时，也必须保持 pending。
     * 这是本次修复的核心回归点：房东还有充足时间处理。
     */
    @Test
    void farFutureAppointmentStaysPendingEvenWhenCreatedLongAgo() {
        LocalDateTime now = LocalDateTime.now();
        Long id = insertPending(now.plusDays(3), now.minusHours(100));

        appointmentService.expireOverdueAppointments();

        assertEquals(AppointmentStatus.PENDING, statusOf(id),
                "远期预约不应因为「创建超过 24 小时」被误判过期");
    }

    /**
     * 看房时间已过 → 必定过期。
     */
    @Test
    void appointmentWhoseTimeHasPassedBecomesExpired() {
        LocalDateTime now = LocalDateTime.now();
        Long id = insertPending(now.minusHours(1), now.minusHours(2));

        appointmentService.expireOverdueAppointments();

        assertEquals(AppointmentStatus.EXPIRED, statusOf(id));
    }

    /**
     * 已进入「临近看房」窗口（看房在 5 小时后）且创建已超过 24 小时 → 过期。
     * 此时房东再不答复就来不及了。
     */
    @Test
    void appointmentInsideLeadWindowAndCreatedLongAgoBecomesExpired() {
        LocalDateTime now = LocalDateTime.now();
        Long id = insertPending(now.plusHours(5), now.minusHours(100));

        appointmentService.expireOverdueAppointments();

        assertEquals(AppointmentStatus.EXPIRED, statusOf(id),
                "已进入临近看房窗口且创建超时，应被判定过期");
    }

    /**
     * 刚刚创建的远期预约 → 保持 pending。
     */
    @Test
    void newlyCreatedFarFutureAppointmentStaysPending() {
        LocalDateTime now = LocalDateTime.now();
        Long id = insertPending(now.plusDays(3), now.minusHours(1));

        appointmentService.expireOverdueAppointments();

        assertEquals(AppointmentStatus.PENDING, statusOf(id));
    }
}