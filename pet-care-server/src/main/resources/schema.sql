-- ============================================
-- 爪爪护理 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS pet_care DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_care;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `openid` VARCHAR(64) COMMENT '微信openid（宠物主）',
  `username` VARCHAR(32) COMMENT '账号（店员/店长）',
  `password` VARCHAR(128) COMMENT 'BCrypt加密密码',
  `nickname` VARCHAR(32) COMMENT '昵称',
  `phone` VARCHAR(16) COMMENT '手机号',
  `avatar` VARCHAR(256) COMMENT '头像URL',
  `role` TINYINT NOT NULL DEFAULT 0 COMMENT '0=宠物主 1=店员 2=店长',
  `store_id` BIGINT DEFAULT 1 COMMENT '所属店铺',
  `status` TINYINT DEFAULT 1 COMMENT '1=正常 0=禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_openid` (`openid`),
  INDEX `idx_role` (`role`),
  INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB COMMENT='用户表';

-- 插入测试数据
INSERT INTO `user` VALUES
(1, 'mock_openid_001', NULL, NULL, '宠物主小王', '13800000001', NULL, 0, 1, 1, NOW()),
(2, NULL, 'staff01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '店员小李', '13800000002', NULL, 1, 1, 1, NOW()),
(3, NULL, 'admin01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '店长老张', '13800000003', NULL, 2, 1, 1, NOW());

-- 宠物档案表
DROP TABLE IF EXISTS `pet`;
CREATE TABLE `pet` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `owner_id` BIGINT NOT NULL COMMENT '宠物主ID',
  `name` VARCHAR(32) NOT NULL COMMENT '宠物名',
  `breed_id` INT COMMENT '品种ID',
  `breed_name` VARCHAR(32) COMMENT '品种名',
  `age` DECIMAL(3,1) COMMENT '年龄(岁)',
  `gender` TINYINT COMMENT '0=公 1=母',
  `weight` DECIMAL(5,2) COMMENT '体重(kg)',
  `avatar` VARCHAR(256) COMMENT '宠物照片',
  `notes` VARCHAR(512) COMMENT '注意事项',
  `popularity` INT DEFAULT 0 COMMENT '人气值',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_owner` (`owner_id`),
  INDEX `idx_breed` (`breed_id`)
) ENGINE=InnoDB COMMENT='宠物档案';

INSERT INTO `pet` VALUES
(1, 1, '旺财', 1, '柯基', 2.5, 0, 12.5, NULL, '胆小，需要温柔对待', 0, NOW()),
(2, 1, '咪咪', 2, '英短', 1.0, 1, 4.2, NULL, '定期剪指甲', 0, NOW());

-- 服务品类表
DROP TABLE IF EXISTS `service_category`;
CREATE TABLE `service_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(32) NOT NULL COMMENT '品类名',
  `icon` VARCHAR(256),
  `sort` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1 COMMENT '1=启用 0=禁用'
) ENGINE=InnoDB COMMENT='服务品类';

INSERT INTO `service_category` VALUES
(1, '洗护美容', NULL, 1, 1),
(2, '健康检查', NULL, 2, 1),
(3, '寄养托管', NULL, 3, 1);

-- 服务项表
DROP TABLE IF EXISTS `service_item`;
CREATE TABLE `service_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `category_id` BIGINT NOT NULL COMMENT '品类ID',
  `name` VARCHAR(64) NOT NULL COMMENT '服务名',
  `description` TEXT COMMENT '服务描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `duration` INT COMMENT '预计时长(分钟)',
  `max_per_slot` INT DEFAULT 3 COMMENT '每时段最大预约数',
  `image` VARCHAR(256),
  `status` TINYINT DEFAULT 1 COMMENT '1=启用 0=禁用',
  INDEX `idx_category` (`category_id`)
) ENGINE=InnoDB COMMENT='服务项';

INSERT INTO `service_item` VALUES
(1, 1, '基础洗护', '含洗澡、吹干、基础修剪', 128.00, 45, 3, NULL, 1),
(2, 1, '精致美容', '含洗澡、造型修剪、SPA护理', 288.00, 90, 2, NULL, 1),
(3, 2, '基础体检', '体温、心率、耳道、皮肤等基础检查', 98.00, 30, 5, NULL, 1),
(4, 2, '疫苗接种', '进口三联疫苗', 150.00, 15, 8, NULL, 1),
(5, 3, '日间托管', '8小时日间寄养，含遛弯和喂食', 88.00, 480, 5, NULL, 1);

-- 订单表
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '宠物主ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `service_id` BIGINT NOT NULL COMMENT '服务项ID',
  `staff_id` BIGINT COMMENT '店员ID',
  `store_id` BIGINT DEFAULT 1 COMMENT '店铺ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待支付 1=已支付 2=服务中 3=已完成 4=已取消',
  `appointment_time` DATETIME COMMENT '预约时段',
  `pay_time` DATETIME COMMENT '支付时间',
  `complete_time` DATETIME COMMENT '完成时间',
  `rating` TINYINT COMMENT '评分(1-5)',
  `review` VARCHAR(512) COMMENT '评价内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE INDEX `uk_order_no` (`order_no`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_staff` (`staff_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='订单表';

-- 优惠券表
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL COMMENT '优惠券名称',
  `type` TINYINT COMMENT '0=满减券 1=折扣券',
  `threshold` DECIMAL(10,2) COMMENT '使用门槛',
  `discount` DECIMAL(10,2) COMMENT '减免金额/折扣值',
  `total_stock` INT NOT NULL COMMENT '总库存',
  `remain_stock` INT NOT NULL COMMENT '剩余库存',
  `per_user_limit` INT DEFAULT 1 COMMENT '每人限领数量',
  `start_time` DATETIME COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  `status` TINYINT DEFAULT 1 COMMENT '1=启用 0=禁用'
) ENGINE=InnoDB COMMENT='优惠券';

INSERT INTO `coupon` VALUES
(1, '新人满100减20', 0, 100.00, 20.00, 100, 100, 1, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 1),
(2, '全场8折券', 1, 0, 0.80, 50, 50, 1, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 1);
