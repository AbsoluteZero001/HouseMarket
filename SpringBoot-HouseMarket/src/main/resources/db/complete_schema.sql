-- 删除已存在的表（如果存在）
SET
FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `houses`;
DROP TABLE IF EXISTS `favorites`;
DROP TABLE IF EXISTS `house_order`;
DROP TABLE IF EXISTS `appointments`;
SET
FOREIGN_KEY_CHECKS = 1;

-- 创建用户表
CREATE TABLE `users`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `username`      varchar(50)  NOT NULL,
    `password`      varchar(100) NOT NULL,
    `real_name`     varchar(50),
    `role`          varchar(20)  NOT NULL,
    `phone`         varchar(20),
    `avatar`        varchar(200),
    `status`        varchar(20)  NOT NULL DEFAULT 'normal',
    `register_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    tinyint      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY             `idx_role` (`role`),
    KEY             `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建房源表
CREATE TABLE `houses`
(
    `id`          bigint         NOT NULL AUTO_INCREMENT,
    `landlord_id` bigint         NOT NULL,
    `title`       varchar(100)   NOT NULL,
    `description` text,
    `address`     varchar(200)   NOT NULL,
    `price`       decimal(10, 2) NOT NULL,
    `type`        varchar(20)    NOT NULL,
    `area` double NOT NULL,
    `image`       text,
    `status`      tinyint        NOT NULL DEFAULT 0,
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`  tinyint        NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY           `idx_landlord_id` (`landlord_id`),
    KEY           `idx_price` (`price`),
    KEY           `idx_type` (`type`),
    CONSTRAINT `fk_houses_landlord` FOREIGN KEY (`landlord_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建收藏表
CREATE TABLE `favorites`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT,
    `user_id`     bigint   NOT NULL,
    `house_id`    bigint   NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_house` (`user_id`, `house_id`),
    KEY           `idx_user_id` (`user_id`),
    KEY           `idx_house_id` (`house_id`),
    CONSTRAINT `fk_favorites_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_favorites_house` FOREIGN KEY (`house_id`) REFERENCES `houses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建房屋订单表
CREATE TABLE `house_order`
(
    `id`          bigint         NOT NULL AUTO_INCREMENT,
    `house_id`    bigint         NOT NULL,
    `tenant_id`   bigint         NOT NULL,
    `landlord_id` bigint         NOT NULL,
    `amount`      decimal(10, 2) NOT NULL,
    `start_date`  date           NOT NULL,
    `end_date`    date           NOT NULL,
    `status`      varchar(20)    NOT NULL DEFAULT 'pending',
    `create_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY           `idx_house_id` (`house_id`),
    KEY           `idx_tenant_id` (`tenant_id`),
    KEY           `idx_landlord_id` (`landlord_id`),
    KEY           `idx_status` (`status`),
    CONSTRAINT `fk_order_house` FOREIGN KEY (`house_id`) REFERENCES `houses` (`id`),
    CONSTRAINT `fk_order_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_order_landlord` FOREIGN KEY (`landlord_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建预约表
CREATE TABLE `appointments`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `house_id`    bigint       NOT NULL,
    `tenant_id`   bigint       NOT NULL,
    `landlord_id` bigint       NOT NULL,
    `time`        datetime     NOT NULL,
    `location`    varchar(200) NOT NULL,
    `notes`       text,
    `status`      varchar(20)  NOT NULL DEFAULT 'pending',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY           `idx_house_id` (`house_id`),
    KEY           `idx_tenant_id` (`tenant_id`),
    KEY           `idx_landlord_id` (`landlord_id`),
    KEY           `idx_status` (`status`),
    KEY           `idx_create_time` (`create_time`),
    CONSTRAINT `fk_appointments_house` FOREIGN KEY (`house_id`) REFERENCES `houses` (`id`),
    CONSTRAINT `fk_appointments_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_appointments_landlord` FOREIGN KEY (`landlord_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;