-- ============================================
-- 房源市场 数据库建表脚本
-- 数据库: housemarket, 字符集: utf8mb4
-- ============================================

CREATE DATABASE IF NOT EXISTS housemarket
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE housemarket;

SET NAMES utf8mb4;

DROP TABLE IF EXISTS `favorites`;
DROP TABLE IF EXISTS `appointment_flow`;
DROP TABLE IF EXISTS `notification_outbox`;
DROP TABLE IF EXISTS `appointment`;
DROP TABLE IF EXISTS `house_image`;
DROP TABLE IF EXISTS `house_order`;
DROP TABLE IF EXISTS `house`;
DROP TABLE IF EXISTS `landlord_application`;
DROP TABLE IF EXISTS `sysuser`;

-- --------------------------------------------
-- 1. 用户表
-- --------------------------------------------
CREATE TABLE `sysuser`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`      VARCHAR(100) NOT NULL COMMENT '用户名',
    `password`   VARCHAR(255) NOT NULL COMMENT 'BCrypt密码',
    `real_name`     VARCHAR(100)          DEFAULT NULL COMMENT '真实姓名',
    `nickname`           VARCHAR(100) DEFAULT NULL COMMENT '房东网名',
    `id_card_no`         VARCHAR(18)  DEFAULT NULL COMMENT '身份证号',
    `real_name_verified` INT NOT NULL DEFAULT 0 COMMENT '实名认证状态: 0未实名, 1已实名',
    `verified_time`      DATETIME     DEFAULT NULL COMMENT '实名认证时间',
    `role`          VARCHAR(50)  NOT NULL DEFAULT 'TENANT' COMMENT '角色: ADMIN/LANDLORD/TENANT',
    `phone`         VARCHAR(50)           DEFAULT NULL COMMENT '联系电话',
    `avatar`        VARCHAR(500)          DEFAULT NULL COMMENT '头像URL',
    `status`        VARCHAR(50)  NOT NULL DEFAULT 'normal' COMMENT '状态: normal/disabled',
    `register_time` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time`   DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` INT          NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删, 1已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY          `idx_role` (`role`),
    CONSTRAINT `chk_user_role` CHECK (`role` IN ('ADMIN', 'LANDLORD', 'TENANT')),
    CONSTRAINT `chk_user_status` CHECK (`status` IN ('normal', 'disabled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- --------------------------------------------
-- 2. 房源表
-- --------------------------------------------
CREATE TABLE `house`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`       VARCHAR(255)   NOT NULL COMMENT '房源标题',
    `type`        VARCHAR(50)    NOT NULL DEFAULT '平层' COMMENT '房屋类型: 平层/跃层/错层/复式',
    `layout`          VARCHAR(50)    NOT NULL DEFAULT '其他' COMMENT '户型: 一室一厅/两室一厅等',
    `district`    VARCHAR(100) NOT NULL DEFAULT '朝阳区' COMMENT '区域',
    `community`       VARCHAR(200)            DEFAULT NULL COMMENT '小区名称',
    `bedrooms`    INT          NOT NULL DEFAULT 1 COMMENT '卧室数',
    `living_rooms`    INT            NOT NULL DEFAULT 0 COMMENT '客厅数',
    `kitchens`        INT            NOT NULL DEFAULT 0 COMMENT '厨房数',
    `bathrooms`   INT          NOT NULL DEFAULT 1 COMMENT '卫生间数',
    `area`        DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '面积(㎡)',
    `price`       DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '月租金(元)',
    `deposit`         DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '押金(元)',
    `orientation` VARCHAR(20)  NOT NULL DEFAULT '南北' COMMENT '朝向',
    `floor`       VARCHAR(50)           DEFAULT NULL COMMENT '楼层信息',
    `total_floors`    INT                     DEFAULT NULL COMMENT '总楼层',
    `decoration`  VARCHAR(50)  NOT NULL DEFAULT '精装' COMMENT '装修情况',
    `lease_term`  VARCHAR(50)  NOT NULL DEFAULT '押一付三' COMMENT '租期/付款方式',
    `has_elevator`    INT            NOT NULL DEFAULT 0 COMMENT '是否有电梯: 0无, 1有',
    `subway_distance` VARCHAR(50)             DEFAULT NULL COMMENT '地铁距离',
    `move_in_type`    VARCHAR(50)             DEFAULT '随时入住' COMMENT '入住方式',
    `rent_status`     VARCHAR(50)             DEFAULT '随时入住' COMMENT '房屋状态',
    `tags`        VARCHAR(255) NOT NULL DEFAULT '[]' COMMENT '标签(JSON数组)',
    `address`     VARCHAR(500)   NOT NULL DEFAULT '未知' COMMENT '详细地址',
    `description` TEXT COMMENT '房源描述',
    `image`       VARCHAR(2000)  NOT NULL DEFAULT '[]' COMMENT '图片地址(JSON数组)',
    `landlord_id` BIGINT         NOT NULL COMMENT '房东ID',
    `status`      VARCHAR(50)  NOT NULL DEFAULT 'NORMAL' COMMENT '状态: NORMAL/OFFLINE',
    `views`       INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
    `create_time` DATETIME                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  INT            NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删, 1已删',
    PRIMARY KEY (`id`),
    KEY           `idx_landlord` (`landlord_id`),
    KEY           `idx_type` (`type`),
    KEY           `idx_price` (`price`),
    KEY           `idx_area` (`area`),
    KEY           `idx_district` (`district`),
    CONSTRAINT `fk_house_landlord` FOREIGN KEY (`landlord_id`) REFERENCES `sysuser` (`id`),
    CONSTRAINT `chk_house_status` CHECK (`status` IN ('NORMAL', 'OFFLINE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房源表';

-- --------------------------------------------
-- 2.1 房源图片表
-- --------------------------------------------
CREATE TABLE `house_image`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `house_id`    BIGINT       NOT NULL COMMENT '房源ID',
    `image_url`   VARCHAR(500) NOT NULL COMMENT '图片访问URL',
    `image_type` VARCHAR(50) NOT NULL DEFAULT 'OTHER' COMMENT '图片分类: COVER/LIVING_ROOM/BEDROOM/KITCHEN/BATHROOM/BALCONY/DINING_ROOM/STUDY/FLOOR_PLAN/OTHER',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `is_cover`    INT          NOT NULL DEFAULT 0 COMMENT '是否封面: 0否, 1是',
    `create_time` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY           `idx_house_image_house` (`house_id`),
    KEY           `idx_house_image_cover` (`house_id`, `is_cover`),
    KEY          `idx_house_image_type` (`house_id`, `image_type`),
    CONSTRAINT `fk_house_image_house` FOREIGN KEY (`house_id`) REFERENCES `house` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房源图片表';

-- --------------------------------------------
-- 3. 预约表
-- --------------------------------------------
CREATE TABLE `appointment`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `house_id`    BIGINT       NOT NULL COMMENT '房源ID',
    `tenant_id`   BIGINT       NOT NULL COMMENT '租客ID',
    `landlord_id` BIGINT       NOT NULL COMMENT '房东ID',
    `time`        DATETIME     NOT NULL COMMENT '预约看房时间',
    `location`    VARCHAR(500)          DEFAULT NULL COMMENT '预约地点',
    `notes`       VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    `request_id`  VARCHAR(64)           DEFAULT NULL COMMENT '幂等键，防止重复预约',
    `status`      VARCHAR(50)  NOT NULL DEFAULT 'pending' COMMENT '状态: pending/approved/rejected/completed/canceled',
    `version`     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY           `idx_house` (`house_id`),
    KEY           `idx_tenant` (`tenant_id`),
    KEY           `idx_landlord` (`landlord_id`),
    KEY           `idx_status` (`status`),
    UNIQUE KEY    `uk_appointment_request` (`request_id`),
    CONSTRAINT `fk_appointment_house` FOREIGN KEY (`house_id`) REFERENCES `house` (`id`),
    CONSTRAINT `fk_appointment_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `sysuser` (`id`),
    CONSTRAINT `fk_appointment_landlord` FOREIGN KEY (`landlord_id`) REFERENCES `sysuser` (`id`),
    CONSTRAINT `chk_appointment_status` CHECK (`status` IN ('pending', 'approved', 'rejected', 'completed', 'canceled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

-- --------------------------------------------
-- 3.2 通知事务 Outbox（异步通知可靠性）
-- --------------------------------------------
CREATE TABLE `notification_outbox`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `business_key`   VARCHAR(128) NOT NULL COMMENT '业务幂等键',
    `business_type`  VARCHAR(50)  NOT NULL DEFAULT 'APPOINTMENT' COMMENT '业务类型: APPOINTMENT/LANDLORD',
    `appointment_id` BIGINT                DEFAULT NULL COMMENT '预约ID（非预约业务为空）',
    `event_type`     VARCHAR(50)  NOT NULL COMMENT '事件类型',
    `payload`        TEXT         NOT NULL COMMENT '通知负载 JSON',
    `target_user_id` BIGINT                DEFAULT NULL COMMENT '通知目标用户ID',
    `status`         VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT '状态: pending/processing/sent/failed',
    `retry_count`    INT          NOT NULL DEFAULT 0 COMMENT '重试次数',
    `create_time`    DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `send_time`      DATETIME              DEFAULT NULL COMMENT '发送时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_outbox_business_key` (`business_key`),
    KEY            `idx_outbox_status` (`status`),
    KEY            `idx_outbox_appointment` (`appointment_id`),
    KEY            `idx_outbox_target` (`target_user_id`),
    CONSTRAINT `fk_outbox_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `appointment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知事务 Outbox 表';

-- --------------------------------------------
-- 3.3 房东入驻申请审核表
-- --------------------------------------------
CREATE TABLE `landlord_application`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL COMMENT '申请人用户ID',
    `username`    VARCHAR(100) NOT NULL COMMENT '申请人用户名',
    `real_name`   VARCHAR(100)          DEFAULT NULL COMMENT '实名信息',
    `phone`       VARCHAR(50)           DEFAULT NULL COMMENT '联系电话',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT '状态: pending/approved/rejected',
    `review_note` VARCHAR(500)          DEFAULT NULL COMMENT '审核意见',
    `reviewer_id` BIGINT                DEFAULT NULL COMMENT '审核人ID',
    `review_time` DATETIME              DEFAULT NULL COMMENT '审核时间',
    `create_time` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `update_time` DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_application_user` (`user_id`),
    KEY           `idx_application_status` (`status`),
    CONSTRAINT `fk_application_user` FOREIGN KEY (`user_id`) REFERENCES `sysuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房东入驻申请审核表';

-- --------------------------------------------
-- 3.1 预约流程轨迹表（审批引擎时间线）
-- --------------------------------------------
CREATE TABLE `appointment_flow`
(
    `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `appointment_id` BIGINT      NOT NULL COMMENT '预约ID',
    `from_status`    VARCHAR(50)  DEFAULT NULL COMMENT '原状态',
    `to_status`      VARCHAR(50) NOT NULL COMMENT '目标状态',
    `action`         VARCHAR(50) NOT NULL COMMENT '动作: PUBLISH/BOOK/APPROVE/REJECT/CANCEL/COMPLETE/NOTIFY',
    `operator_id`    BIGINT       DEFAULT NULL COMMENT '操作人ID',
    `operator_role`  VARCHAR(50)  DEFAULT NULL COMMENT '操作人角色',
    `remark`         VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
    PRIMARY KEY (`id`),
    KEY              `idx_flow_appointment` (`appointment_id`),
    KEY              `idx_flow_action` (`action`),
    CONSTRAINT `fk_flow_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `appointment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约流程轨迹表';

-- --------------------------------------------
-- 4. 收藏表
-- --------------------------------------------
CREATE TABLE `favorites`
(
    `id`       BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`  BIGINT NOT NULL COMMENT '用户ID',
    `house_id` BIGINT NOT NULL COMMENT '房源ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_house` (`user_id`, `house_id`),
    KEY        `idx_house` (`house_id`),
    CONSTRAINT `fk_favorites_user` FOREIGN KEY (`user_id`) REFERENCES `sysuser` (`id`),
    CONSTRAINT `fk_favorites_house` FOREIGN KEY (`house_id`) REFERENCES `house` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';
