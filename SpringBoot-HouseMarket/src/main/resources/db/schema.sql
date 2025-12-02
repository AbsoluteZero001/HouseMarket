-- 删除已存在的appointments表（如果存在）
DROP TABLE IF EXISTS `appointments`;

-- 创建预约表
CREATE TABLE `appointments`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '预约ID，主键，自增',
    `house_id`    bigint       NOT NULL COMMENT '房源ID，外键关联houses表',
    `tenant_id`   bigint       NOT NULL COMMENT '租客ID，外键关联sys_user表',
    `landlord_id` bigint       NOT NULL COMMENT '房东ID，外键关联sys_user表',
    `time`        datetime     NOT NULL COMMENT '预约时间',
    `location`    varchar(200) NOT NULL COMMENT '预约地点',
    `notes`       text COMMENT '备注',
    `status`      varchar(20)  NOT NULL DEFAULT 'pending' COMMENT '预约状态（pending/approved/rejected/completed/canceled）',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY           `idx_house_id` (`house_id`),
    KEY           `idx_tenant_id` (`tenant_id`),
    KEY           `idx_landlord_id` (`landlord_id`),
    KEY           `idx_status` (`status`),
    KEY           `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约信息表';