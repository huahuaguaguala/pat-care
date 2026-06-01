-- ============================================================
-- 爪爪护理 - Database Schema v3.0
-- P0 improvements: audit fields, order_detail, cancel reasons
-- ============================================================

CREATE DATABASE IF NOT EXISTS pet_care DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_care;

-- ============================================================
-- 1. User
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `openid` VARCHAR(64),
  `username` VARCHAR(32),
  `password` VARCHAR(128),
  `nickname` VARCHAR(32) NOT NULL,
  `real_name` VARCHAR(32),
  `phone` VARCHAR(16),
  `gender` TINYINT COMMENT '0=male 1=female',
  `birthday` DATE,
  `avatar` VARCHAR(256),
  `role` TINYINT NOT NULL DEFAULT 0 COMMENT '0=owner 1=staff 2=admin',
  `store_id` BIGINT DEFAULT 1,
  `status` TINYINT DEFAULT 1 COMMENT '1=active 0=disabled',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_user` BIGINT COMMENT 'Created by user.id',
  `update_user` BIGINT COMMENT 'Last modified by user.id',
  UNIQUE INDEX `uk_openid` (`openid`),
  UNIQUE INDEX `uk_username` (`username`),
  INDEX `idx_role` (`role`)
) ENGINE=InnoDB COMMENT='User table - unified 3 roles';

INSERT INTO `user` VALUES
(1, 'mock_openid_001', NULL, NULL, 'XiaoWang', 'Wang Wei', '13800000001', 0, '1995-03-15', NULL, 0, 1, 1, NOW(), NOW(), NULL, NULL),
(2, NULL, 'staff01', '$2b$12$.759cLykI8RxY0NDjUygX.OBiCutwLIe3LSww00NvDa8tF/LkZmYS', 'Staff Li', 'Li Ming', '13800000002', 1, '1998-07-22', NULL, 1, 1, 1, NOW(), NOW(), NULL, NULL),
(3, NULL, 'admin01', '$2b$12$.759cLykI8RxY0NDjUygX.OBiCutwLIe3LSww00NvDa8tF/LkZmYS', 'Admin Zhang', 'Zhang Wei', '13800000003', 0, '1990-01-10', NULL, 2, 1, 1, NOW(), NOW(), NULL, NULL);

-- ============================================================
-- 2. Pet
-- ============================================================
DROP TABLE IF EXISTS `pet`;
CREATE TABLE `pet` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `store_no` VARCHAR(16) DEFAULT '' COMMENT 'P-0001 unique within store',
  `owner_id` BIGINT NOT NULL,
  `name` VARCHAR(32) NOT NULL,
  `breed_id` INT,
  `breed_name` VARCHAR(32),
  `birthday` DATE,
  `gender` TINYINT COMMENT '0=male 1=female',
  `weight` DECIMAL(5,2),
  `is_neutered` TINYINT DEFAULT 0,
  `chip_id` VARCHAR(32),
  `personality` VARCHAR(128),
  `notes` VARCHAR(512),
  `popularity` INT DEFAULT 0,
  `avatar` VARCHAR(256),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_user` BIGINT,
  `update_user` BIGINT,
  UNIQUE INDEX `uk_store_no` (`store_no`),
  INDEX `idx_owner` (`owner_id`),
  INDEX `idx_breed` (`breed_id`)
) ENGINE=InnoDB COMMENT='Pet profile';

INSERT INTO `pet` VALUES
(1, 'P-0001', 1, 'WangCai', 1, 'Corgi', '2023-08-15', 0, 12.50, 0, NULL, 'Shy but sweet', 'No known allergies', 0, NULL, NOW(), NOW(), NULL, NULL),
(2, 'P-0002', 1, 'Mimi', 2, 'British Shorthair', '2025-04-01', 1, 4.20, 1, NULL, 'Calm, naps during grooming', 'Trim nails carefully', 0, NULL, NOW(), NOW(), NULL, NULL);

-- ============================================================
-- 3. Pet photo
-- ============================================================
DROP TABLE IF EXISTS `pet_photo`;
CREATE TABLE `pet_photo` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `pet_id` BIGINT NOT NULL,
  `url` VARCHAR(256) NOT NULL,
  `is_primary` TINYINT DEFAULT 0,
  `sort` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_user` BIGINT,
  `update_user` BIGINT,
  INDEX `idx_pet` (`pet_id`)
) ENGINE=InnoDB COMMENT='Pet gallery';

-- ============================================================
-- 4. Service category
-- ============================================================
DROP TABLE IF EXISTS `service_category`;
CREATE TABLE `service_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(32) NOT NULL,
  `icon` VARCHAR(256),
  `sort` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_user` BIGINT,
  `update_user` BIGINT
) ENGINE=InnoDB COMMENT='Service categories';

INSERT INTO `service_category` VALUES
(1, 'Grooming', NULL, 1, 1, NOW(), NOW(), NULL, NULL),
(2, 'Health Check', NULL, 2, 1, NOW(), NOW(), NULL, NULL),
(3, 'Boarding', NULL, 3, 1, NOW(), NOW(), NULL, NULL);

-- ============================================================
-- 5. Service item
-- ============================================================
DROP TABLE IF EXISTS `service_item`;
CREATE TABLE `service_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `category_id` BIGINT NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `description` TEXT,
  `price` DECIMAL(10,2) NOT NULL,
  `duration` INT COMMENT 'Estimated minutes',
  `max_per_slot` INT DEFAULT 3,
  `service_type` TINYINT DEFAULT 0 COMMENT '0=one-time 1=package',
  `total_sessions` INT DEFAULT 1,
  `image` VARCHAR(256),
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_user` BIGINT,
  `update_user` BIGINT,
  INDEX `idx_category` (`category_id`)
) ENGINE=InnoDB COMMENT='Service items';

INSERT INTO `service_item` VALUES
(1, 1, 'Basic Wash', 'Bath, blow-dry, basic trim', 128.00, 45, 3, 0, 1, NULL, 1, NOW(), NOW(), NULL, NULL),
(2, 1, 'Premium Grooming', 'Full bath, styling, SPA', 288.00, 90, 2, 0, 1, NULL, 1, NOW(), NOW(), NULL, NULL),
(3, 2, 'Basic Checkup', 'Temp, heart rate, ears, skin', 98.00, 30, 5, 0, 1, NULL, 1, NOW(), NOW(), NULL, NULL),
(4, 2, 'Vaccination', 'Triple vaccine', 150.00, 15, 8, 0, 1, NULL, 1, NOW(), NOW(), NULL, NULL),
(5, 3, 'Day Care', '8hr boarding + walk + feed', 88.00, 480, 5, 0, 1, NULL, 1, NOW(), NOW(), NULL, NULL);

-- ============================================================
-- 6. Vaccination record
-- ============================================================
DROP TABLE IF EXISTS `vaccination_record`;
CREATE TABLE `vaccination_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `pet_id` BIGINT NOT NULL,
  `order_id` BIGINT,
  `vaccine_name` VARCHAR(64) NOT NULL,
  `vaccine_type` VARCHAR(32),
  `dose_number` INT DEFAULT 1,
  `administered_date` DATE NOT NULL,
  `next_due_date` DATE,
  `administered_by` BIGINT,
  `clinic_name` VARCHAR(64),
  `batch_number` VARCHAR(32),
  `notes` VARCHAR(256),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_user` BIGINT,
  `update_user` BIGINT,
  INDEX `idx_pet` (`pet_id`),
  INDEX `idx_next_due` (`next_due_date`)
) ENGINE=InnoDB COMMENT='Vaccination history';

INSERT INTO `vaccination_record` VALUES
(1, 1, NULL, 'DHPPi 5-in-1', 'Core', 3, '2026-03-01', '2027-03-01', 2, 'Paws Clinic', 'B20260301001', 'No reaction', NOW(), NOW(), NULL, NULL),
(2, 1, NULL, 'Rabies', 'Core', 1, '2026-04-15', '2027-04-15', 2, 'Paws Clinic', 'R20260415002', NULL, NOW(), NOW(), NULL, NULL),
(3, 2, NULL, 'Fel-O-Vax 3-in-1', 'Core', 2, '2026-02-20', '2027-02-20', 2, 'Paws Clinic', 'F20260220003', 'Mild lethargy 1 day', NOW(), NOW(), NULL, NULL);

-- ============================================================
-- 7. Medical record
-- ============================================================
DROP TABLE IF EXISTS `medical_record`;
CREATE TABLE `medical_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `pet_id` BIGINT NOT NULL,
  `order_id` BIGINT,
  `visit_date` DATE NOT NULL,
  `record_type` TINYINT NOT NULL COMMENT '0=illness 1=injury 2=checkup 3=surgery 4=other',
  `symptoms` VARCHAR(512),
  `diagnosis` VARCHAR(512),
  `treatment` VARCHAR(512),
  `medication` VARCHAR(256),
  `temperature` DECIMAL(4,1),
  `weight` DECIMAL(5,2),
  `vet_name` VARCHAR(32),
  `follow_up_date` DATE,
  `is_resolved` TINYINT DEFAULT 0,
  `notes` VARCHAR(512),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_user` BIGINT,
  `update_user` BIGINT,
  INDEX `idx_pet` (`pet_id`),
  INDEX `idx_visit_date` (`visit_date`)
) ENGINE=InnoDB COMMENT='Medical records';

INSERT INTO `medical_record` VALUES
(1, 1, NULL, '2026-05-10', 0, 'Vomiting, loss of appetite 2 days', 'Mild gastritis', 'SubQ fluids + anti-emetic', 'Probiotics 5 days', 38.8, 12.3, 'Dr. Chen', '2026-05-17', 1, 'No table scraps', NOW(), NOW(), NULL, NULL),
(2, 2, NULL, '2026-05-28', 1, 'Limping right front leg', 'Minor sprain', 'Rest + cold compress', 'Meloxicam 0.5mg 3 days', 38.2, 4.1, 'Dr. Chen', NULL, 0, 'Schedule X-ray if persists 5+ days', NOW(), NOW(), NULL, NULL);

-- ============================================================
-- 8. Order (HEADER - one per order)
-- ============================================================
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL COMMENT 'Unique order number',
  `order_type` TINYINT NOT NULL DEFAULT 0 COMMENT '0=service 1=product 2=package',
  `user_id` BIGINT NOT NULL COMMENT 'Pet owner',
  `pet_id` BIGINT NOT NULL COMMENT 'Pet receiving service',
  `staff_id` BIGINT COMMENT 'Assigned staff',
  `store_id` BIGINT DEFAULT 1,
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT 'Sum of order_detail.subtotal',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=unpaid 1=paid 2=in-progress 3=done 4=cancelled',
  `appointment_time` DATETIME,
  `actual_start_time` DATETIME,
  `actual_end_time` DATETIME,
  `pay_time` DATETIME,
  `complete_time` DATETIME,
  `rating` TINYINT COMMENT '1-5',
  `review` VARCHAR(512),
  `cancel_reason` VARCHAR(255) COMMENT 'Why owner cancelled',
  `rejection_reason` VARCHAR(255) COMMENT 'Why staff rejected',
  `remark` VARCHAR(255) COMMENT 'Owner note',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_user` BIGINT,
  `update_user` BIGINT,
  UNIQUE INDEX `uk_order_no` (`order_no`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_staff` (`staff_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_pet` (`pet_id`)
) ENGINE=InnoDB COMMENT='Order header - one order, multiple services';

-- ============================================================
-- 9. Order detail (NEW - one per service in an order)
-- ============================================================
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL COMMENT 'FK -> order.id',
  `service_id` BIGINT NOT NULL COMMENT 'FK -> service_item.id',
  `service_name` VARCHAR(64) NOT NULL COMMENT 'Snapshot: service name at order time',
  `price` DECIMAL(10,2) NOT NULL COMMENT 'Snapshot: unit price at order time',
  `quantity` INT DEFAULT 1,
  `subtotal` DECIMAL(10,2) NOT NULL COMMENT 'price * quantity',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_user` BIGINT,
  `update_user` BIGINT,
  INDEX `idx_order` (`order_id`)
) ENGINE=InnoDB COMMENT='Order detail - one row per service in an order';

-- ============================================================
-- 10. Coupon
-- ============================================================
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `type` TINYINT COMMENT '0=fixed-amount 1=percentage',
  `threshold` DECIMAL(10,2),
  `discount` DECIMAL(10,2),
  `total_stock` INT NOT NULL,
  `remain_stock` INT NOT NULL,
  `per_user_limit` INT DEFAULT 1,
  `applic