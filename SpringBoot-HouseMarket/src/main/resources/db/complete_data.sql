-- 清空现有数据
SET
FOREIGN_KEY_CHECKS = 0;
DELETE
FROM `appointments`;
DELETE
FROM `house_order`;
DELETE
FROM `favorites`;
DELETE
FROM `houses`;
DELETE
FROM `users`;
SET
FOREIGN_KEY_CHECKS = 1;

-- 插入用户表的随机数据
INSERT INTO `users` (`username`, `password`, `real_name`, `role`, `phone`, `avatar`, `status`)
VALUES ('admin', '$2a$10$rOzJi7VH/hyIy0QvO.6YyeNxFjdheoaPY9OmIT5GBprnWP.6.q.Xu', '系统管理员', 'ADMIN', '13800000001',
        'https://avatars.githubusercontent.com/u/123456', 'normal'),
       ('landlord1', '$2a$10$rOzJi7VH/hyIy0QvO.6YyeNxFjdheoaPY9OmIT5GBprnWP.6.q.Xu', '张房东', 'LANDLORD',
        '13800000002', 'https://avatars.githubusercontent.com/u/234567', 'normal'),
       ('tenant1', '$2a$10$rOzJi7VH/hyIy0QvO.6YyeNxFjdheoaPY9OmIT5GBprnWP.6.q.Xu', '李租客', 'TENANT', '13800000003',
        'https://avatars.githubusercontent.com/u/345678', 'normal'),
       ('landlord2', '$2a$10$rOzJi7VH/hyIy0QvO.6YyeNxFjdheoaPY9OmIT5GBprnWP.6.q.Xu', '王房东', 'LANDLORD',
        '13800000004', 'https://avatars.githubusercontent.com/u/456789', 'normal'),
       ('landlord3', '$2a$10$rOzJi7VH/hyIy0QvO.6YyeNxFjdheoaPY9OmIT5GBprnWP.6.q.Xu', '赵房东', 'LANDLORD',
        '13800000005', 'https://avatars.githubusercontent.com/u/567890', 'normal'),
       ('tenant2', '$2a$10$rOzJi7VH/hyIy0QvO.6YyeNxFjdheoaPY9OmIT5GBprnWP.6.q.Xu', '陈租客', 'TENANT', '13800000006',
        'https://avatars.githubusercontent.com/u/678901', 'normal'),
       ('tenant3', '$2a$10$rOzJi7VH/hyIy0QvO.6YyeNxFjdheoaPY9OmIT5GBprnWP.6.q.Xu', '刘租客', 'TENANT', '13800000007',
        'https://avatars.githubusercontent.com/u/789012', 'normal'),
       ('tenant4', '$2a$10$rOzJi7VH/hyIy0QvO.6YyeNxFjdheoaPY9OmIT5GBprnWP.6.q.Xu', '杨租客', 'TENANT', '13800000008',
        'https://avatars.githubusercontent.com/u/890123', 'normal'),
       ('tenant5', '$2a$10$rOzJi7VH/hyIy0QvO.6YyeNxFjdheoaPY9OmIT5GBprnWP.6.q.Xu', '黄租客', 'TENANT', '13800000009',
        'https://avatars.githubusercontent.com/u/901234', 'normal'),
       ('tenant6', '$2a$10$rOzJi7VH/hyIy0QvO.6YyeNxFjdheoaPY9OmIT5GBprnWP.6.q.Xu', '周租客', 'TENANT', '13800000010',
        'https://avatars.githubusercontent.com/u/012345', 'normal');

-- 插入房源表的随机数据
INSERT INTO `houses` (`landlord_id`, `title`, `description`, `address`, `price`, `type`, `area`, `image`, `status`)
VALUES (2, '朝阳区温馨一居室', '位于市中心，交通便利，采光好，适合单身或情侣居住', '北京市朝阳区幸福家园1号楼101室',
        5500.00, '平层', 65.5, 'https://example.com/images/house1_1.jpg,https://example.com/images/house1_2.jpg', 1),
       (2, '海淀区精致两居室', '靠近地铁站，装修精美，家具家电齐全', '北京市海淀区阳光小区3号楼502室', 7800.00, '平层',
        85.0,
        'https://example.com/images/house2_1.jpg,https://example.com/images/house2_2.jpg,https://example.com/images/house2_3.jpg',
        1),
       (4, 'CBD豪华三居室', '高端公寓，视野开阔，可俯瞰城市美景', '北京市朝阳区CBD核心区国际公寓A座2501室', 18000.00,
        '跃层', 145.0,
        'https://example.com/images/house3_1.jpg,https://example.com/images/house3_2.jpg,https://example.com/images/house3_3.jpg,https://example.com/images/house3_4.jpg',
        1),
       (4, '学院路学区房', '临近重点学校，教育资源丰富，安静舒适', '北京市海淀区学院路学区花园小区2号楼301室', 9500.00,
        '平层', 92.0, 'https://example.com/images/house4_1.jpg,https://example.com/images/house4_2.jpg', 2),
       (5, '西城区老城厢', '传统四合院改造，保留古都风貌，闹中取静', '北京市西城区胡同里弄四合院正房', 12000.00, '复式',
        120.0,
        'https://example.com/images/house5_1.jpg,https://example.com/images/house5_2.jpg,https://example.com/images/house5_3.jpg',
        1),
       (5, '亦庄经济开发区现代公寓', '现代化设计，智能家居，适合年轻家庭', '北京市大兴区亦庄开发区现代城B座1206室',
        6500.00, '平层', 78.5, 'https://example.com/images/house6_1.jpg,https://example.com/images/house6_2.jpg', 3),
       (2, '望京SOHO办公住宅', '商住两用，适合创业团队或SOHO族', '北京市朝阳区望京SOHO T3座3001室', 11000.00, '错层',
        110.0,
        'https://example.com/images/house7_1.jpg,https://example.com/images/house7_2.jpg,https://example.com/images/house7_3.jpg',
        1),
       (4, '奥林匹克森林公园旁别墅', '独栋别墅，带私家花园，环境优美', '北京市朝阳区奥运村别墅区紫薇园12号', 35000.00,
        '复式', 320.0,
        'https://example.com/images/house8_1.jpg,https://example.com/images/house8_2.jpg,https://example.com/images/house8_3.jpg,https://example.com/images/house8_4.jpg,https://example.com/images/house8_5.jpg',
        1),
       (5, '国贸CBD精品公寓', '高端精装公寓，拎包入住，配套设施完善', '北京市朝阳区国贸CBD景华南街金茂府南区8号楼2801室',
        15000.00, '平层', 105.0,
        'https://example.com/images/house9_1.jpg,https://example.com/images/house9_2.jpg,https://example.com/images/house9_3.jpg',
        1),
       (2, '金融街商务公寓', '位于北京金融街核心区域，商务氛围浓厚', '北京市西城区金融大街丙一号国华置业大厦B座1608室',
        13500.00, '平层', 95.0, 'https://example.com/images/house10_1.jpg,https://example.com/images/house10_2.jpg', 1);

-- 插入收藏表的随机数据
INSERT INTO `favorites` (`user_id`, `house_id`, `create_time`)
VALUES (3, 1, '2025-12-01 10:30:00'),
       (3, 3, '2025-12-01 11:45:00'),
       (6, 2, '2025-12-02 09:15:00'),
       (6, 5, '2025-12-02 14:20:00'),
       (6, 8, '2025-12-02 16:30:00'),
       (7, 4, '2025-12-03 10:00:00'),
       (7, 6, '2025-12-03 11:30:00'),
       (8, 1, '2025-12-03 15:45:00'),
       (8, 7, '2025-12-03 17:20:00'),
       (9, 9, '2025-12-04 09:30:00');

-- 插入房屋订单表的随机数据
INSERT INTO `house_order` (`house_id`, `tenant_id`, `landlord_id`, `amount`, `start_date`, `end_date`, `status`)
VALUES (1, 3, 2, 5500.00, '2026-01-01', '2026-12-31', 'paid'),
       (2, 6, 2, 7800.00, '2026-02-01', '2027-01-31', 'completed'),
       (3, 7, 4, 18000.00, '2026-03-01', '2027-02-28', 'pending'),
       (4, 8, 4, 9500.00, '2025-11-01', '2026-10-31', 'completed'),
       (5, 9, 5, 12000.00, '2026-04-01', '2027-03-31', 'paid'),
       (7, 10, 2, 11000.00, '2026-05-01', '2027-04-30', 'pending'),
       (8, 6, 4, 35000.00, '2026-06-01', '2027-05-31', 'pending'),
       (9, 7, 5, 15000.00, '2026-07-01', '2027-06-30', 'pending'),
       (10, 8, 2, 13500.00, '2026-08-01', '2027-07-31', 'pending'),
       (1, 9, 2, 5500.00, '2026-09-01', '2027-08-31', 'pending');

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
       (1, 7, 2, '2025-12-25 09:30:00', '阳光小区北门', '希望可以优惠一些租金', 'pending');

-- 重置自增ID
ALTER TABLE `users` AUTO_INCREMENT = 11;
ALTER TABLE `houses` AUTO_INCREMENT = 11;
ALTER TABLE `favorites` AUTO_INCREMENT = 11;
ALTER TABLE `house_order` AUTO_INCREMENT = 11;
ALTER TABLE `appointments` AUTO_INCREMENT = 16;