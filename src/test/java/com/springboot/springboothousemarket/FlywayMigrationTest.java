package com.springboot.springboothousemarket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Flyway 迁移接入启动流程的验证。
 *
 * <p>背景：仓库里原本只有一份「手工执行」的增量 SQL（deploy/db 下），
 * 应用启动不会自动跑它 —— 换一个环境部署就可能漏执行，
 * 导致「通知幂等键」「claim_time」「实名审核 SLA」等字段在生产库里根本不存在。
 * 这里验证迁移已经真正接入启动流程，并且本轮新增的结构变更确实落库了。
 */
@SpringBootTest
class FlywayMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 迁移历史表必须存在，且所有脚本都执行成功。
     */
    @Test
    void allMigrationsAreRecordedSuccessfully() {
        Long historyTable = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'flyway_schema_history'",
                Long.class);
        assertNotNull(historyTable);
        assertTrue(historyTable > 0, "flyway_schema_history 必须存在：说明增量迁移已接入应用启动流程");

        Long failed = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success <> 1", Long.class);
        assertEquals(0L, failed, "不能有执行失败的迁移脚本");

        List<String> versions = jdbcTemplate.queryForList(
                "SELECT version FROM flyway_schema_history ORDER BY installed_rank", String.class);
        assertTrue(versions.contains("2026.11.10"),
                "缺少「通知幂等 + claim_time」迁移，实际已执行: " + versions);
        assertTrue(versions.contains("2026.11.11"),
                "缺少「实名 SLA + 归档索引」迁移，实际已执行: " + versions);
    }

    /**
     * 实名审核 SLA 的计时起点列必须真的在库里，
     * 否则超期自动拒绝的任务会因为字段不存在而报错（或者静默不生效）。
     */
    @Test
    void identityVerificationHasSubmitTimeColumn() {
        Long columns = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'identity_verification' "
                        + "AND COLUMN_NAME = 'submit_time'",
                Long.class);
        assertEquals(1L, columns, "identity_verification.submit_time 必须存在（审核 SLA 的计时起点）");
    }

    /**
     * 通知归档扫描与分页依赖的 create_time 索引必须存在，
     * 否则数据量上来后归档任务会退化成全表扫描。
     */
    @Test
    void archiveIndexesExist() {
        assertEquals(1L, indexCount("notification", "idx_notification_create_time"),
                "notification.idx_notification_create_time 索引缺失");
        assertEquals(1L, indexCount("notification_outbox", "idx_outbox_create_time"),
                "notification_outbox.idx_outbox_create_time 索引缺失");
    }

    private Long indexCount(String table, String index) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.STATISTICS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ?",
                Long.class, table, index);
    }
}
