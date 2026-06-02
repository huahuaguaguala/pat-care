-- ============================================================
-- Test isolation: runs before each test method
-- Uses TRUNCATE to reset AUTO_INCREMENT
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `order_detail`;
TRUNCATE TABLE `order`;
TRUNCATE TABLE `vaccination_record`;
TRUNCATE TABLE `medical_record`;
TRUNCATE TABLE `pet_photo`;
TRUNCATE TABLE `pet`;
TRUNCATE TABLE `user`;
SET FOREIGN_KEY_CHECKS = 1;

-- Reset service items to known state
UPDATE `service_item` SET `status` = 1 WHERE `id` IN (1, 2, 3, 4, 5);
UPDATE `coupon` SET `remain_stock` = 100 WHERE `id` = 1;

-- Fresh test users (BCrypt for "123456")
INSERT INTO `user` VALUES
(1, 'test_openid_owner', NULL, NULL, 'TestOwner', 'Owner', NULL, 0, NULL, NULL, 0, 1, 1, NOW(), NOW(), NULL, NULL),
(2, NULL, 'staff01', '$2b$12$.759cLykI8RxY0NDjUygX.OBiCutwLIe3LSww00NvDa8tF/LkZmYS', 'TestStaff', 'Staff', NULL, 1, NULL, NULL, 1, 1, 1, NOW(), NOW(), NULL, NULL),
(3, NULL, 'admin01', '$2b$12$.759cLykI8RxY0NDjUygX.OBiCutwLIe3LSww00NvDa8tF/LkZmYS', 'TestAdmin', 'Admin', NULL, 0, NULL, NULL, 2, 1, 1, NOW(), NOW(), NULL, NULL);

-- Fresh test pet for userId=1
INSERT INTO `pet` VALUES
(1, 'P-0001', 1, 'TestPet', 1, 'Corgi', '2024-01-01', 0, 10.00, 0, NULL, 'Friendly', NULL, 0, NULL, NOW(), NOW(), NULL, NULL);
