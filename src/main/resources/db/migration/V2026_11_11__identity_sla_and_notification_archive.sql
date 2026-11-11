-- ============================================================
-- Flyway 迁移脚本：V2026_11_11 —— 实名审核 SLA + 通知归档索引
-- 目标：
--   1) identity_verification 增加 submit_time —— 记录「最近一次提交申请」的时间，
--      作为审核 SLA 的计时起点（超期未审核由定时任务自动拒绝，避免申请无限期挂起）；
--   2) notification / notification_outbox 增加 create_time 索引 —— 支撑通知归档扫描
--      与通知中心分页，避免数据量增长后全表扫描。
--
-- 执行方式：由 Flyway 在应用启动时自动执行（见 application.yml 的 spring.flyway 配置），
--             执行记录写入 flyway_schema_history 表；不再需要人工登录数据库执行。
-- 幂等性：全部使用 information_schema 判存，可重复执行。
-- 注意：MySQL 不支持 ADD COLUMN IF NOT EXISTS，故沿用 information_schema + PREPARE 判存写法。
-- ============================================================

SET @db := DATABASE();

-- ------------------------------------------------------------
-- 1) identity_verification.submit_time
-- ------------------------------------------------------------
SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'identity_verification' AND COLUMN_NAME = 'submit_time'),
        'SELECT ''identity_verification.submit_time 已存在，跳过''',
        'ALTER TABLE `identity_verification`
            ADD COLUMN `submit_time` DATETIME DEFAULT NULL
                COMMENT ''最近一次提交申请的时间（审核 SLA 计时起点）'' AFTER `create_time`'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 历史数据回填：用首次申请时间兜底，保证存量 pending 申请同样能被 SLA 判定
UPDATE `identity_verification`
   SET `submit_time` = `create_time`
 WHERE `submit_time` IS NULL;

-- ------------------------------------------------------------
-- 2) notification.create_time 索引（归档扫描 / 分页排序）
-- ------------------------------------------------------------
SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'notification' AND INDEX_NAME = 'idx_notification_create_time'),
        'SELECT ''idx_notification_create_time 已存在，跳过''',
        'ALTER TABLE `notification` ADD KEY `idx_notification_create_time` (`create_time`)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 3) notification_outbox.create_time 索引（归档扫描）
-- ------------------------------------------------------------
SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'notification_outbox' AND INDEX_NAME = 'idx_outbox_create_time'),
        'SELECT ''idx_outbox_create_time 已存在，跳过''',
        'ALTER TABLE `notification_outbox` ADD KEY `idx_outbox_create_time` (`create_time`)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
