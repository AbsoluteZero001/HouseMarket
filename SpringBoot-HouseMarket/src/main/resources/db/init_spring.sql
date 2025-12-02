-- Spring Boot自动初始化预约表结构和数据
-- 此文件用于Spring Boot的自动数据库初始化功能

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

-- 插入预约表的随机数据
INSERT INTO `appointments` (`house_id`, `tenant_id`, `landlord_id`, `time`, `location`, `notes`, `status`)
VALUES (1, 3, 2, '2025-12-10 10:00:00', '北京市朝阳区阳光小区门口', '请携带身份证和租房合同', 'pending'),
       (2, 3, 2, '2025-12-11 14:30:00', '北京市海淀区幸福家园接待中心', '希望可以看到朝南的卧室', 'approved'),
       (1, 4, 2, '2025-12-12 09:00:00', '阳光小区物业管理处', '我想和房东面谈一下租金问题', 'rejected'),
       (3, 4, 5, '2025-12-15 16:00:00', '北京市西城区和谐社区会所', '可以接受稍微调整看房时间', 'pending'),
       (2, 5, 2, '2025-12-08 11:00:00', '幸福家园南门', '希望可以看到厨房和卫生间', 'completed'),
       (3, 5, 5, '2025-12-09 13:30:00', '和谐社区物业管理处', NULL, 'canceled'),
       (1, 6, 2, '2025-12-18 15:00:00', '北京市朝阳区阳光路1号', '请提前10分钟到达', 'pending'),
       (2, 6, 2, '2025-12-20 10:30:00', '幸福家园物业管理处', '想确认是否可以养宠物', 'approved'),
       (3, 7, 5, '2025-12-22 14:00:00', '和谐社区接待中心', '需要确认租期和付款方式', 'pending'),
       (1, 7, 2, '2025-12-25 09:30:00', '阳光小区北门', '希望可以优惠一些租金', 'pending'),
       (2, 8, 2, '2025-12-28 15:30:00', '幸福家园东门', '希望可以看到储物间', 'approved'),
       (3, 8, 5, '2025-12-30 10:00:00', '和谐社区北门', '可以接受年付', 'pending'),
       (1, 9, 2, '2026-01-05 14:00:00', '阳光小区南门', '希望可以短租', 'pending'),
       (2, 9, 2, '2026-01-08 11:30:00', '幸福家园物业管理处', '想了解周边交通情况', 'approved'),
       (3, 10, 5, '2026-01-10 16:00:00', '和谐社区接待中心', '希望可以看到阳台', 'pending');

-- 重置自增ID
ALTER TABLE `appointments` AUTO_INCREMENT = 1;