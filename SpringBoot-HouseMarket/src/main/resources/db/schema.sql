-- ============================================
-- 房源市场 数据库建表脚本
-- 数据库名: housemarket
-- ============================================

CREATE
DATABASE IF NOT EXISTS housemarket
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE
housemarket;

-- --------------------------------------------
-- 1. 用户表 (sysuser)
-- --------------------------------------------
DROP TABLE IF EXISTS `favorites`;
DROP TABLE IF EXISTS `appointment`;
DROP TABLE IF EXISTS `house`;
DROP TABLE IF EXISTS `sysuser`;

CREATE TABLE `sysuser`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `username`      VARCHAR(100) NOT NULL COMMENT '用户名',
    `password`      VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name`     VARCHAR(100)          DEFAULT NULL COMMENT '真实姓名',
    `role`          VARCHAR(50)  NOT NULL DEFAULT 'TENANT' COMMENT '角色: ADMIN/LANDLORD/TENANT',
    `phone`         VARCHAR(50)           DEFAULT NULL COMMENT '联系电话',
    `avatar`        VARCHAR(500)          DEFAULT NULL COMMENT '头像URL',
    `status`        VARCHAR(50)  NOT NULL DEFAULT 'normal' COMMENT '状态: normal/disabled',
    `register_time` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time`   DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDeleted`     INT          NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删, 1已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- --------------------------------------------
-- 2. 房源表 (house)
-- --------------------------------------------
CREATE TABLE `house`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `title`       VARCHAR(255)   NOT NULL COMMENT '房源标题',
    `type`        VARCHAR(50)    NOT NULL DEFAULT '平层' COMMENT '房屋类型: 平层/跃层/错层/复式',
    `area`        DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '面积(㎡)',
    `price`       DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '月租金(元)',
    `address`     VARCHAR(500)   NOT NULL DEFAULT '未知' COMMENT '详细地址',
    `description` TEXT COMMENT '房源描述',
    `image`       VARCHAR(2000)  NOT NULL DEFAULT '[]' COMMENT '图片地址(JSON数组)',
    `landlord_id` BIGINT         NOT NULL COMMENT '房东ID',
    `status`      VARCHAR(50)    NOT NULL DEFAULT 'NORMAL' COMMENT '房源状态',
    `create_time` DATETIME                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  INT            NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删, 1已删',
    PRIMARY KEY (`id`),
    KEY           `idx_landlord` (`landlord_id`),
    KEY           `idx_type` (`type`),
    KEY           `idx_price` (`price`),
    KEY           `idx_area` (`area`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房源表';

-- --------------------------------------------
-- 3. 预约表 (appointment)
-- --------------------------------------------
CREATE TABLE `appointment`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `house_id`    BIGINT      NOT NULL COMMENT '房源ID',
    `tenant_id`   BIGINT      NOT NULL COMMENT '租客ID',
    `landlord_id` BIGINT      NOT NULL COMMENT '房东ID',
    `time`        DATETIME    NOT NULL COMMENT '预约看房时间',
    `location`    VARCHAR(500)         DEFAULT NULL COMMENT '预约地点',
    `notes`       VARCHAR(500)         DEFAULT NULL COMMENT '备注',
    `status`      VARCHAR(50) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/approved/rejected/completed/canceled',
    `create_time` DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY           `idx_house` (`house_id`),
    KEY           `idx_tenant` (`tenant_id`),
    KEY           `idx_landlord` (`landlord_id`),
    KEY           `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

-- --------------------------------------------
-- 4. 收藏表 (favorites)
-- --------------------------------------------
CREATE TABLE `favorites`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `user_id`     BIGINT NOT NULL COMMENT '用户ID',
    `house_id`    BIGINT NOT NULL COMMENT '房源ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_house` (`user_id`, `house_id`),
    KEY           `idx_house` (`house_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';
