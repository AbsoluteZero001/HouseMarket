-- ============================================
-- 房源市场 初始化数据
-- 密码均为 BCrypt 哈希: admin123 / 123456
-- ============================================

USE housemarket;

SET NAMES utf8mb4;

INSERT INTO `sysuser` (`id`, `username`, `password`, `real_name`, `nickname`, `id_card_no`, `real_name_verified`,
                       `verified_time`, `role`, `phone`, `avatar`, `status`, `register_time`)
VALUES (1, 'admin', '$2a$10$4KGLBUeN89vGB/7vW10YiuP6YwKjHyKqw.nbAbhEsLHS.9X447tfe', '系统管理员', NULL, NULL, 0, NULL,
        'ADMIN', '13800000001', NULL, 'normal', NOW()),
       (2, 'landlord1', '$2a$10$4yrbcM6dxO2zuB.2Ri13C.xY9NiF1hG43UrttMNU8zUcTXgYjujkm', '张明', '低调玩家',
        '110101199001011234', 1, NOW(), 'LANDLORD', '13800000002', NULL, 'normal', NOW()),
       (3, 'landlord2', '$2a$10$4yrbcM6dxO2zuB.2Ri13C.xY9NiF1hG43UrttMNU8zUcTXgYjujkm', '李华', '房东李华',
        '110101199102022345', 1, NOW(), 'LANDLORD', '13800000003', NULL, 'normal', NOW()),
       (4, 'landlord3', '$2a$10$4yrbcM6dxO2zuB.2Ri13C.xY9NiF1hG43UrttMNU8zUcTXgYjujkm', '王芳', '房东王芳',
        '110101199203033456', 1, NOW(), 'LANDLORD', '13800000004', NULL, 'normal', NOW()),
       (5, 'tenant1', '$2a$10$4yrbcM6dxO2zuB.2Ri13C.xY9NiF1hG43UrttMNU8zUcTXgYjujkm', '赵小雨', NULL, NULL, 0, NULL,
        'TENANT', '13900000001', NULL, 'normal', NOW()),
       (6, 'tenant2', '$2a$10$4yrbcM6dxO2zuB.2Ri13C.xY9NiF1hG43UrttMNU8zUcTXgYjujkm', '孙丽', NULL, NULL, 0, NULL,
        'TENANT', '13900000002', NULL, 'normal', NOW()),
       (7, 'tenant3', '$2a$10$4yrbcM6dxO2zuB.2Ri13C.xY9NiF1hG43UrttMNU8zUcTXgYjujkm', '周强', NULL, NULL, 0, NULL,
        'TENANT', '13900000003', NULL, 'normal', NOW());

UPDATE `sysuser`
SET `avatar` = '/uploads/avatars/default.png'
WHERE `avatar` IS NULL
   OR `avatar` = '';

UPDATE `sysuser`
SET `nickname` = CASE `username`
                     WHEN 'tenant1' THEN '租客202608221'
                     WHEN 'tenant2' THEN '租客202608222'
                     WHEN 'tenant3' THEN '租客202608223'
                     ELSE `nickname`
    END
WHERE `role` = 'TENANT';

INSERT INTO `landlord_application` (`user_id`, `username`, `real_name`, `phone`, `status`, `review_note`, `reviewer_id`, `review_time`)
VALUES (2, 'landlord1', '张明', '13800000002', 'approved', '审核通过', 1, NOW()),
       (3, 'landlord2', '李华', '13800000003', 'approved', '审核通过', 1, NOW()),
       (4, 'landlord3', '王芳', '13800000004', 'approved', '审核通过', 1, NOW());

INSERT INTO `house` (`id`, `title`, `type`, `district`, `bedrooms`, `bathrooms`, `area`, `price`, `orientation`,
                     `floor`, `decoration`, `lease_term`, `tags`, `address`, `description`, `image`, `landlord_id`,
                     `status`, `views`, `create_time`)
VALUES (1, '望京SOHO精装两居室', '平层', '朝阳区', 2, 1, 89.00, 6500.00, '南北', '16/28层', '精装', '押一付三',
        '["近地铁","电梯","拎包入住"]', '朝阳区望京街望京SOHO T1座206',
        '户型方正南北通透，高层视野开阔，家电家具齐全，楼下即地铁15号线望京站，适合白领及小家庭居住。',
        '[]', 2, 'NORMAL', 128, '2026-06-15 10:30:00'),
       (2, '中关村软件园精品LOFT公寓', '跃层', '海淀区', 2, 2, 120.00, 8800.00, '南北', '9/15层', '精装', '押一付三',
        '["近地铁","智能家居","带车位"]', '海淀区中关村东路1号院清华科技园D座508',
        '上下两层LOFT结构，一层客厅厨房，二层卧室书房，中央空调、地暖、新风系统齐全，步行5分钟到地铁13号线五道口站。',
        '[]', 2, 'NORMAL', 96, '2026-06-20 14:00:00'),
       (3, '金融街高端一居室', '平层', '西城区', 1, 1, 55.00, 7200.00, '南', '20/32层', '精装', '押一付三',
        '["电梯","24小时管家","朝南"]', '西城区金融大街中心A座203',
        '精装一居室，品牌家电，干湿分离卫生间，落地窗俯瞰金融街全景，步行3分钟到地铁2号线复兴门站。',
        '[]', 3, 'NORMAL', 75, '2026-06-22 09:15:00'),
       (4, '三里屯时尚复式公寓', '复式', '朝阳区', 3, 2, 150.00, 15000.00, '东南', '顶层/21层', '豪华装修', '押二付一',
        '["带露台","泳池","高端定制"]', '朝阳区三里屯太古里南里5号楼2901',
        '顶层复式带超大露台，可俯瞰三里屯夜景，全屋高端定制家具，双主卧设计，小区配备泳池、健身房、会所。',
        '[]', 3, 'NORMAL', 210, '2026-07-01 11:00:00'),
       (5, '总部基地精装三居室', '平层', '丰台区', 3, 2, 110.00, 5200.00, '南北', '6/18层', '精装', '押一付三',
        '["家庭宜居","人车分流","近地铁"]', '丰台区总部基地科技园南路2号院2-1-602',
        '三室两厅双卫，南北通透，明厨明卫，全屋精装修，小区绿化率高，步行10分钟到地铁9号线丰台科技园站。',
        '[]', 4, 'NORMAL', 64, '2026-07-05 16:30:00'),
       (6, '通州万达广场舒适两居室', '错层', '通州区', 2, 1, 95.00, 3800.00, '南', '12/24层', '简装', '押一付一',
        '["性价比高","近商场","独立阳台"]', '通州区新华大街万达广场C座105',
        '错层结构动静分区，客厅与卧室区域巧妙分区，独立阳台可晾晒，紧邻万达广场，餐饮购物一站式。',
        '[]', 4, 'NORMAL', 88, '2026-07-08 08:45:00'),
       (7, '鼓楼大街四合院改造公寓', '平层', '东城区', 2, 1, 65.00, 9800.00, '南北', '1/3层', '精装', '押一付三',
        '["老北京","独院","地暖"]', '东城区鼓楼大街豆腐池胡同17号',
        '老北京四合院改造的现代公寓，保留传统建筑韵味的同时融入现代生活设施，独立小院，地暖、中央空调。',
        '[]', 2, 'NORMAL', 152, '2026-07-10 13:20:00'),
       (8, '国贸CBD精装一居室', '平层', '朝阳区', 1, 1, 48.00, 5500.00, '北', '18/30层', '精装', '押一付三',
        '["CBD","近地铁","拎包入住"]', '朝阳区建国路国贸CBD万达广场1号楼1802',
        '精装一居室，户型方正，高楼层视野好，紧邻国贸站，步行3分钟到地铁1号线/10号线，楼下即便利店和餐厅。',
        '[]', 3, 'NORMAL', 71, '2026-07-12 10:00:00'),
       (9, '五道口华清嘉园两居室', '平层', '海淀区', 2, 1, 78.00, 7500.00, '南北', '4/20层', '精装', '押一付三',
        '["学区","近地铁","安静"]', '海淀区五道口成府路华清嘉园5号楼401',
        '紧邻清华北大，学术氛围浓厚，两室一厅南北通透，家具家电齐全，步行5分钟到地铁13号线五道口站。',
        '[]', 4, 'NORMAL', 83, '2026-07-15 15:00:00'),
       (10, '亦庄开发区现代三居室', '复式', '大兴区', 3, 2, 135.00, 4500.00, '南北', '15/26层', '简装', '押一付三',
        '["高性价比","绿化率高","近企业总部"]', '大兴区亦庄经济技术开发区荣华南路10号院3-2-1501',
        '复式大三居，上下两层动静分区，一层客厅餐厅厨房，二层三间卧室，品牌家电，步行8分钟到亦庄线荣京东街站。',
        '[]', 2, 'NORMAL', 104, '2026-07-18 09:30:00'),
       (11, '双井富力城新装两居室（待审核）', '平层', '朝阳区', 2, 1, 92.00, 6800.00, '南北', '8/30层', '精装',
        '押一付三',
        '["近地铁","精装修","南北通透"]', '朝阳区东三环双井富力城A区6号楼1201',
        '富力城新装两居室，业主自住装修保持好，南北通透采光充足，步行8分钟到地铁10号线双井站。',
        '[]', 2, 'PENDING_REVIEW', 0, '2026-08-20 11:20:00');

INSERT INTO `house_image` (`house_id`, `image_url`, `sort_order`, `is_cover`, `create_time`)
VALUES (1, '/uploads/houses/1/img_1.png', 0, 1, '2026-06-15 10:30:00'),
       (1, '/uploads/houses/1/img_2.png', 1, 0, '2026-06-15 10:31:00'),
       (1, '/uploads/houses/1/img_3.png', 2, 0, '2026-06-15 10:32:00'),
       (2, '/uploads/houses/2/img_2.png', 0, 1, '2026-06-20 14:00:00'),
       (2, '/uploads/houses/2/img_4.png', 1, 0, '2026-06-20 14:01:00'),
       (2, '/uploads/houses/2/img_5.png', 2, 0, '2026-06-20 14:02:00'),
       (3, '/uploads/houses/3/img_3.png', 0, 1, '2026-06-22 09:15:00'),
       (3, '/uploads/houses/3/img_4.png', 1, 0, '2026-06-22 09:16:00'),
       (3, '/uploads/houses/3/img_5.png', 2, 0, '2026-06-22 09:17:00'),
       (4, '/uploads/houses/4/img_4.png', 0, 1, '2026-07-01 11:00:00'),
       (4, '/uploads/houses/4/img_5.png', 1, 0, '2026-07-01 11:01:00'),
       (4, '/uploads/houses/4/img_1.png', 2, 0, '2026-07-01 11:02:00'),
       (5, '/uploads/houses/5/img_5.png', 0, 1, '2026-07-05 16:30:00'),
       (5, '/uploads/houses/5/img_1.png', 1, 0, '2026-07-05 16:31:00'),
       (5, '/uploads/houses/5/img_2.png', 2, 0, '2026-07-05 16:32:00'),
       (6, '/uploads/houses/6/img_1.png', 0, 1, '2026-07-08 08:45:00'),
       (6, '/uploads/houses/6/img_2.png', 1, 0, '2026-07-08 08:46:00'),
       (6, '/uploads/houses/6/img_3.png', 2, 0, '2026-07-08 08:47:00'),
       (7, '/uploads/houses/7/img_2.png', 0, 1, '2026-07-10 13:20:00'),
       (7, '/uploads/houses/7/img_4.png', 1, 0, '2026-07-10 13:21:00'),
       (7, '/uploads/houses/7/img_5.png', 2, 0, '2026-07-10 13:22:00'),
       (8, '/uploads/houses/8/img_3.png', 0, 1, '2026-07-12 10:00:00'),
       (8, '/uploads/houses/8/img_4.png', 1, 0, '2026-07-12 10:01:00'),
       (8, '/uploads/houses/8/img_5.png', 2, 0, '2026-07-12 10:02:00'),
       (9, '/uploads/houses/9/img_4.png', 0, 1, '2026-07-15 15:00:00'),
       (9, '/uploads/houses/9/img_5.png', 1, 0, '2026-07-15 15:01:00'),
       (9, '/uploads/houses/9/img_1.png', 2, 0, '2026-07-15 15:02:00'),
       (10, '/uploads/houses/10/img_5.png', 0, 1, '2026-07-18 09:30:00'),
       (10, '/uploads/houses/10/img_1.png', 1, 0, '2026-07-18 09:31:00'),
       (10, '/uploads/houses/10/img_2.png', 2, 0, '2026-07-18 09:32:00'),
       (11, '/uploads/houses/11/img_1.png', 0, 1, '2026-08-20 11:20:00'),
       (11, '/uploads/houses/11/img_2.png', 1, 0, '2026-08-20 11:21:00');

INSERT INTO `appointment` (`id`, `house_id`, `tenant_id`, `landlord_id`, `time`, `location`, `notes`, `status`,
                           `create_time`, `update_time`)
VALUES (1, 1, 5, 2, '2026-08-05 10:00:00', '朝阳区望京SOHO T1座206', '周末看房，请提前联系确认时间', 'approved',
        '2026-07-20 14:30:00', '2026-07-21 09:00:00'),
       (2, 2, 6, 2, '2026-08-06 14:00:00', '海淀区清华科技园D座508', '对LOFT结构很感兴趣', 'pending',
        '2026-07-22 10:15:00', '2026-07-22 10:15:00'),
       (3, 4, 5, 3, '2026-08-07 16:00:00', '朝阳区三里屯太古里南里5号楼2901', '想看露台和小区配套', 'approved',
        '2026-07-23 09:00:00', '2026-07-23 15:20:00'),
       (4, 6, 7, 4, '2026-08-08 11:00:00', '通州区新华大街万达广场C座105', '价格合适的话可以立即定', 'pending',
        '2026-07-25 16:45:00', '2026-07-25 16:45:00'),
       (5, 3, 6, 3, '2026-08-03 15:00:00', '西城区金融大街中心A座203', NULL, 'completed', '2026-07-18 11:00:00',
        '2026-07-28 10:00:00'),
       (6, 5, 7, 4, '2026-08-10 10:00:00', '丰台区总部基地科技园南路2号院2-1-602', '带家人一起看房', 'rejected',
        '2026-07-26 08:30:00', '2026-07-26 17:00:00');

INSERT INTO `favorites` (`id`, `user_id`, `house_id`, `create_time`)
VALUES (1, 5, 1, '2026-07-20 15:00:00'),
       (2, 5, 4, '2026-07-21 10:30:00'),
       (3, 5, 7, '2026-07-23 09:00:00'),
       (4, 6, 2, '2026-07-22 12:00:00'),
       (5, 6, 3, '2026-07-24 14:00:00'),
       (6, 7, 5, '2026-07-25 17:00:00'),
       (7, 7, 6, '2026-07-26 11:30:00'),
       (8, 7, 10, '2026-07-28 08:00:00');

-- 预约流程时间线（发布 -> 预约 -> 审批 -> 通知）
INSERT INTO `appointment_flow` (`appointment_id`, `from_status`, `to_status`, `action`, `operator_id`, `operator_role`,
                                `remark`, `create_time`)
VALUES (1, NULL, 'published', 'PUBLISH', 2, 'LANDLORD', '房源已发布上线', '2026-06-15 10:30:00'),
       (1, NULL, 'pending', 'BOOK', 5, 'TENANT', '租客提交看房预约', '2026-07-20 14:30:00'),
       (1, 'pending', 'pending', 'NOTIFY', 2, 'LANDLORD', '已通知房东处理预约', '2026-07-20 14:31:00'),
       (1, 'pending', 'approved', 'APPROVE', 2, 'LANDLORD', '房东审批通过', '2026-07-21 09:00:00'),
       (1, 'approved', 'approved', 'NOTIFY', 2, 'LANDLORD', '已通知租客审批结果', '2026-07-21 09:01:00'),
       (2, NULL, 'published', 'PUBLISH', 2, 'LANDLORD', '房源已发布上线', '2026-06-20 14:00:00'),
       (2, NULL, 'pending', 'BOOK', 6, 'TENANT', '租客提交看房预约', '2026-07-22 10:15:00'),
       (2, 'pending', 'pending', 'NOTIFY', 2, 'LANDLORD', '已通知房东处理预约', '2026-07-22 10:16:00'),
       (3, NULL, 'published', 'PUBLISH', 3, 'LANDLORD', '房源已发布上线', '2026-07-01 11:00:00'),
       (3, NULL, 'pending', 'BOOK', 5, 'TENANT', '租客提交看房预约', '2026-07-23 09:00:00'),
       (3, 'pending', 'pending', 'NOTIFY', 3, 'LANDLORD', '已通知房东处理预约', '2026-07-23 09:01:00'),
       (3, 'pending', 'approved', 'APPROVE', 3, 'LANDLORD', '房东审批通过', '2026-07-23 15:20:00'),
       (3, 'approved', 'approved', 'NOTIFY', 3, 'LANDLORD', '已通知租客审批结果', '2026-07-23 15:21:00'),
       (4, NULL, 'published', 'PUBLISH', 4, 'LANDLORD', '房源已发布上线', '2026-07-08 08:45:00'),
       (4, NULL, 'pending', 'BOOK', 7, 'TENANT', '租客提交看房预约', '2026-07-25 16:45:00'),
       (4, 'pending', 'pending', 'NOTIFY', 4, 'LANDLORD', '已通知房东处理预约', '2026-07-25 16:46:00'),
       (5, NULL, 'published', 'PUBLISH', 3, 'LANDLORD', '房源已发布上线', '2026-06-22 09:15:00'),
       (5, NULL, 'pending', 'BOOK', 6, 'TENANT', '租客提交看房预约', '2026-07-18 11:00:00'),
       (5, 'pending', 'pending', 'NOTIFY', 3, 'LANDLORD', '已通知房东处理预约', '2026-07-18 11:01:00'),
       (5, 'pending', 'approved', 'APPROVE', 3, 'LANDLORD', '房东审批通过', '2026-07-19 10:00:00'),
       (5, 'approved', 'completed', 'COMPLETE', 3, 'LANDLORD', '看房完成，预约闭环结束', '2026-07-28 10:00:00'),
       (6, NULL, 'published', 'PUBLISH', 4, 'LANDLORD', '房源已发布上线', '2026-07-05 16:30:00'),
       (6, NULL, 'pending', 'BOOK', 7, 'TENANT', '租客提交看房预约', '2026-07-26 08:30:00'),
       (6, 'pending', 'pending', 'NOTIFY', 4, 'LANDLORD', '已通知房东处理预约', '2026-07-26 08:31:00'),
       (6, 'pending', 'rejected', 'REJECT', 4, 'LANDLORD', '时间冲突，房东拒绝本次预约', '2026-07-26 17:00:00'),
       (6, 'rejected', 'rejected', 'NOTIFY', 4, 'LANDLORD', '已通知租客审批结果', '2026-07-26 17:01:00');

-- 用户通知中心历史（与 Outbox 投递记录一一对应）
INSERT INTO `notification` (`id`, `user_id`, `type`, `title`, `content`, `related_type`, `related_id`, `read_status`,
                            `sent_time`, `create_time`)
VALUES (1, 5, 'APPOINTMENT_APPROVED', '预约已批准', '您预约的「望京SOHO精装两居室」看房申请已被房东批准', 'APPOINTMENT',
        1, 0, '2026-07-21 09:01:01', '2026-07-21 09:01:00'),
       (2, 2, 'APPOINTMENT_CREATED', '有新预约申请待处理', '租客孙丽预约了「中关村软件园精品LOFT公寓」，请及时处理',
        'APPOINTMENT', 2, 0, '2026-07-22 10:16:01', '2026-07-22 10:16:00'),
       (3, 5, 'APPOINTMENT_APPROVED', '预约已批准', '您预约的「三里屯时尚复式公寓」看房申请已被房东批准', 'APPOINTMENT',
        3, 1, '2026-07-23 15:21:01', '2026-07-23 15:21:00'),
       (4, 4, 'APPOINTMENT_CREATED', '有新预约申请待处理', '租客周强预约了「通州万达广场舒适两居室」，请及时处理',
        'APPOINTMENT', 4, 0, '2026-07-25 16:46:01', '2026-07-25 16:46:00'),
       (5, 6, 'APPOINTMENT_COMPLETED', '看房预约已完成', '您在「金融街高端一居室」的看房已完成', 'APPOINTMENT', 5, 1,
        '2026-07-28 10:00:31', '2026-07-28 10:00:30'),
       (6, 7, 'APPOINTMENT_REJECTED', '预约已拒绝', '很抱歉，您对「总部基地精装三居室」的预约被房东拒绝', 'APPOINTMENT', 6,
        0, '2026-07-26 17:01:01', '2026-07-26 17:01:00'),
       (7, 2, 'LANDLORD_APPROVED', '房东入驻审核通过', '您的房东入驻申请已通过，现在可以发布房源了',
        'LANDLORD_APPLICATION', 1, 1, '2026-06-15 10:00:01', '2026-06-15 10:00:00');

-- 通知中心历史（事务 Outbox 已送达记录）
INSERT INTO `notification_outbox` (`business_key`, `appointment_id`, `notification_id`, `event_type`, `payload`,
                                   `target_user_id`, `status`, `retry_count`, `create_time`, `send_time`)
VALUES ('1:APPOINTMENT_APPROVED', 1, 1, 'APPOINTMENT_APPROVED',
        '{"appointmentId":1,"status":"APPOINTMENT_APPROVED","tenantId":5,"landlordId":2,"targetUserId":5,"message":"预约已批准"}',
        5, 'sent', 1, '2026-07-21 09:01:00', '2026-07-21 09:01:01'),
       ('2:APPOINTMENT_CREATED', 2, 2, 'APPOINTMENT_CREATED',
        '{"appointmentId":2,"status":"APPOINTMENT_CREATED","tenantId":6,"landlordId":2,"targetUserId":2,"message":"有新预约申请待处理"}',
        2, 'sent', 1, '2026-07-22 10:16:00', '2026-07-22 10:16:01'),
       ('3:APPOINTMENT_APPROVED', 3, 3, 'APPOINTMENT_APPROVED',
        '{"appointmentId":3,"status":"APPOINTMENT_APPROVED","tenantId":5,"landlordId":3,"targetUserId":5,"message":"预约已批准"}',
        5, 'sent', 1, '2026-07-23 15:21:00', '2026-07-23 15:21:01'),
       ('4:APPOINTMENT_CREATED', 4, 4, 'APPOINTMENT_CREATED',
        '{"appointmentId":4,"status":"APPOINTMENT_CREATED","tenantId":7,"landlordId":4,"targetUserId":4,"message":"有新预约申请待处理"}',
        4, 'sent', 1, '2026-07-25 16:46:00', '2026-07-25 16:46:01'),
       ('5:APPOINTMENT_COMPLETED', 5, 5, 'APPOINTMENT_COMPLETED',
        '{"appointmentId":5,"status":"APPOINTMENT_COMPLETED","tenantId":6,"landlordId":3,"targetUserId":6,"message":"看房预约已完成"}',
        6, 'sent', 1, '2026-07-28 10:00:30', '2026-07-28 10:00:31'),
       ('6:APPOINTMENT_REJECTED', 6, 6, 'APPOINTMENT_REJECTED',
        '{"appointmentId":6,"status":"APPOINTMENT_REJECTED","tenantId":7,"landlordId":4,"targetUserId":7,"message":"预约已拒绝"}',
        7, 'sent', 1, '2026-07-26 17:01:00', '2026-07-26 17:01:01');

INSERT INTO `notification_outbox` (`business_key`, `business_type`, `appointment_id`, `notification_id`, `event_type`,
                                   `payload`, `target_user_id`, `status`, `retry_count`, `create_time`, `send_time`)
VALUES ('LANDLORD_APPLICATION:1:LANDLORD_APPROVED', 'LANDLORD', NULL, 7, 'LANDLORD_APPROVED',
        '{"status":"LANDLORD_APPROVED","targetUserId":2,"message":"房东入驻审核通过，现在可以发布房源了"}', 2, 'sent', 1,
        '2026-06-15 10:00:00', '2026-06-15 10:00:01');

-- 实名认证申请（种子房东已完成人工审核）
INSERT INTO `identity_verification` (`user_id`, `username`, `real_name`, `id_card_no`, `status`, `review_note`,
                                     `reviewer_id`, `review_time`, `create_time`)
VALUES (2, 'landlord1', '张明', '110101199001011234', 'approved', '信息核验通过', 1, '2026-06-14 10:00:00',
        '2026-06-13 10:00:00'),
       (3, 'landlord2', '李华', '110101199102022345', 'approved', '信息核验通过', 1, '2026-06-14 10:00:00',
        '2026-06-13 10:00:00'),
       (4, 'landlord3', '王芳', '110101199203033456', 'approved', '信息核验通过', 1, '2026-06-14 10:00:00',
        '2026-06-13 10:00:00');

-- 聊天消息（租客1 与 房东1 围绕房源1 的历史会话）
INSERT INTO `chat_message` (`sender_id`, `receiver_id`, `house_id`, `content`, `message_type`, `read_status`,
                            `create_time`)
VALUES (5, 2, 1, '您好，望京SOHO这套房子还在出租吗？', 'TEXT', 1, '2026-07-19 10:00:00'),
       (2, 5, 1, '您好，在的，房子还在出租，随时可以约看。', 'TEXT', 1, '2026-07-19 10:05:00'),
       (5, 2, 1, '好的，我已经提交了看房预约，周日上午方便吗？', 'TEXT', 0, '2026-07-19 10:08:00');
