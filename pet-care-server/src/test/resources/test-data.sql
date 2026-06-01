-- Test isolation: insert fresh data for each test class
DELETE FROM `order_detail`;
DELETE FROM `order`;
DELETE FROM `pet_photo`;
DELETE FROM `pet`;
DELETE FROM `user`;

-- Fresh test user (BCrypt for "123456")
INSERT INTO `user` VALUES
(1, 'test_openid_owner', NULL, NULL, 'TestOwner', 'Owner', '13800000001', 0, NULL, NULL, 0, 1, 1, NOW(), NOW(), NULL, NULL),
(2, NULL, 'staff01', '$2b$12$.759cLykI8RxY0NDjUygX.OBiCutwLIe3LSww00NvDa8tF/LkZmYS', 'TestStaff', 'Staff', '13800000002', 1, NULL, NULL, 1, 1, 1, NOW(), NOW(), NULL, NULL);

-- Fresh test pet
INSERT INTO `pet` VALUES
(1, 'P-0001', 1, 'TestPet', 1, 'Corgi', '2024-01-01', 0, 10.00, 0, NULL, 'Friendly', 'No issues', 0, NULL, NOW(), NOW(), NULL, NULL);
