-- ============================================================
-- 迁移脚本：2026-11-10
-- 目标：
--   1) notification 增加 business_key 唯一索引 —— 通知幂等，杜绝同一业务事件重复落库；
--   2) notification_outbox 增加 claim_time —— 用「领取时刻」而非「创建时刻」判定僵死记录；
--   3) notification_outbox.business_key 扩容到 191 —— 容纳「事件#接收用户」的归一化幂等键。
--
-- 执行方式：mysql -u root -p housemarket < 本脚本
-- 幂等性：全部使用 information_schema 判存，可重复执行。
-- 注意：新增唯一索引前会清理 business_key 重复的历史行（正常为空，因为该列是本次新增）。
-- ============================================================

USE housemarket;

SET @db := DATABASE();

-- ------------------------------------------------------------
-- 1) notification_outbox.business_key 扩容到 191
-- ------------------------------------------------------------
ALTER TABLE `notification_outbox`
    MODIFY COLUMN `business_key` VARCHAR(191) NOT NULL
        COMMENT '业务幂等键（含接收用户维度，避免同一事件的不同收件人互相顶掉）';

-- ------------------------------------------------------------
-- 2) notification_outbox 新增 claim_time
-- ------------------------------------------------------------
SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'notification_outbox' AND COLUMN_NAME = 'claim_time'),
        'SELECT ''notification_outbox.claim_time 已存在，跳过''',
        'ALTER TABLE `notification_outbox`
            ADD COLUMN `claim_time` DATETIME DEFAULT NULL
                COMMENT ''最近一次被消费者领取的时间（用于回收僵死的 processing 记录）'' AFTER `create_time`'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 3) notification 新增 business_key
-- ------------------------------------------------------------
SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'notification' AND COLUMN_NAME = 'business_key'),
        'SELECT ''notification.business_key 已存在，跳过''',
        'ALTER TABLE `notification`
            ADD COLUMN `business_key` VARCHAR(191) DEFAULT NULL
                COMMENT ''业务幂等键（事件+接收用户维度，用于去重；NULL表示不参与去重）'' AFTER `related_id`'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 4) 清理 business_key 重复的历史行（保留 id 最小的一条）
-- ------------------------------------------------------------
DELETE n FROM `notification` n
    INNER JOIN `notification` keep_row
            ON keep_row.business_key = n.business_key AND keep_row.id < n.id
    WHERE n.business_key IS NOT NULL;

-- ------------------------------------------------------------
-- 5) notification.business_key 唯一索引
-- ------------------------------------------------------------
SET @sql := (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'notification' AND INDEX_NAME = 'uk_notification_business_key'),
        'SELECT ''uk_notification_business_key 已存在，跳过''',
        'ALTER TABLE `notification` ADD UNIQUE KEY `uk_notification_business_key` (`business_key`)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;