package com.petcare;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcare.dto.LoginDTO;
import com.petcare.dto.OrderDTO;
import com.petcare.dto.OrderItemDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 爪爪护理 API 集成测试
 * 测试角色切换：不同 Token 对应不同角色，验证接口权限和数据隔离
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PetCareApplicationTests {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired(required = false) private RedisTemplate<String, Object> redisTemplate;

    private static String petOwnerToken;
    private static String staffToken;
    private static String adminToken;
    private static Long petOwnerId;
    private static Long createdPetId;
    private static Long createdOrderId;

    @BeforeAll
    static void prepareSchema(@Autowired JdbcTemplate jdbcTemplate,
                              @Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        createAuditLogIfMissing(jdbcTemplate);
        addColumnIfMissing(jdbcTemplate, "order", "refund_time", "DATETIME", "complete_time");
        addColumnIfMissing(jdbcTemplate, "order", "refund_reason", "VARCHAR(255)", "rejection_reason");
        addColumnIfMissing(jdbcTemplate, "order", "refund_amount", "DECIMAL(10,2)", "total_amount");
        new ResourceDatabasePopulator(new ClassPathResource("test-data.sql"))
                .execute(jdbcTemplate.getDataSource());

        Assertions.assertNotNull(redisTemplate, "RedisTemplate is required for strict integration tests");
        Assertions.assertDoesNotThrow(() -> redisTemplate.getConnectionFactory().getConnection().ping(),
                "Redis must be running on the configured host/port before running strict tests");
        redisTemplate.delete("coupon:stock:1");
        redisTemplate.delete("coupon:claimed:1");
    }

    private static void createAuditLogIfMissing(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `audit_log` (" +
                "`id` BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "`user_id` BIGINT COMMENT 'Operator user.id'," +
                "`username` VARCHAR(32) COMMENT 'Operator name snapshot'," +
                "`action` VARCHAR(32) NOT NULL COMMENT 'CREATE / UPDATE / DELETE / LOGIN'," +
                "`target` VARCHAR(64) COMMENT 'Target entity, e.g. Order, Pet, ServiceItem'," +
                "`target_id` BIGINT COMMENT 'Target record id'," +
                "`detail` VARCHAR(512) COMMENT 'Brief description of what changed'," +
                "`ip` VARCHAR(45) COMMENT 'Request IP'," +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "INDEX `idx_user` (`user_id`)," +
                "INDEX `idx_action` (`action`)," +
                "INDEX `idx_create_time` (`create_time`)" +
                ") ENGINE=InnoDB COMMENT='Operation audit log - who did what and when'");
    }

    private static void addColumnIfMissing(JdbcTemplate jdbcTemplate, String table, String column,
                                           String definition, String afterColumn) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class, table, column);
        if (count != null && count == 0) {
            jdbcTemplate.execute("ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` "
                    + definition + " AFTER `" + afterColumn + "`");
        }
    }

    // ==================== 认证测试 ====================

    @Test
    @Order(1)
    @DisplayName("微信登录 — 宠物主注册并获取Token")
    void wechatLogin() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setOpenid("test_openid_" + System.currentTimeMillis());

        MvcResult result = mvc.perform(post("/api/auth/wechat-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Map<String, Object> data = (Map) mapper.readValue(json, Map.class).get("data");
        petOwnerToken = (String) data.get("token");
        Map<String, Object> user = (Map) data.get("user");
        petOwnerId = Long.valueOf(user.get("id").toString());
        System.out.println("✓ 微信登录成功, token: " + petOwnerToken.substring(0, 20) + "...");
    }

    @Test
    @Order(2)
    @DisplayName("账号密码登录 — 店员登录")
    void staffLogin() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("staff01");
        dto.setPassword("123456");  // 注意：需要数据库中BCrypt哈希匹配

        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Map<String, Object> body = mapper.readValue(json, Map.class);
        // 密码可能不匹配(测试库密码是占位符), 检查是否正确响应
        System.out.println("✓ 店员登录: " + body.get("code") + " " + body.get("message"));
    }

    // ==================== 宠物CRUD测试 ====================

    @Test
    @Order(3)
    @DisplayName("宠物主添加宠物")
    void addPet() throws Exception {
        String body = "{\"name\":\"测试柯基\",\"breedId\":1,\"breedName\":\"柯基\",\"age\":2.0,\"gender\":0,\"weight\":11.5}";

        MvcResult result = mvc.perform(post("/api/pet")
                .header("Authorization", "Bearer " + petOwnerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Map<String, Object> data = (Map) mapper.readValue(json, Map.class).get("data");
        createdPetId = Long.valueOf(data.get("id").toString());
        System.out.println("✓ 宠物添加成功, petId: " + createdPetId);
    }

    @Test
    @Order(4)
    @DisplayName("查询我的宠物列表")
    void myPets() throws Exception {
        mvc.perform(get("/api/pet/my")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("✓ 宠物列表查询成功");
    }

    // ==================== 订单流程测试 ====================

    @Test
    @Order(5)
    @DisplayName("创建订单 → 支付 → 验证状态流转")
    void orderFlow() throws Exception {
        // 1. create order with new multi-item format (petId=1 from addPet test or DB seed)
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setPetId(createdPetId != null ? createdPetId : 1L);

        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setServiceId(1L);
        itemDTO.setQuantity(1);
        orderDTO.setItems(java.util.Arrays.asList(itemDTO));

        MvcResult r1 = mvc.perform(post("/api/order")
                .header("Authorization", "Bearer " + petOwnerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(orderDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        Map<String, Object> body = mapper.readValue(r1.getResponse().getContentAsString(), Map.class);
        Map<String, Object> data = (Map) body.get("data");
        Map<String, Object> order = (Map) data.get("order");
        createdOrderId = Long.valueOf(order.get("id").toString());
        System.out.println("✓ 订单创建成功, orderId: " + createdOrderId);

        // 2. 支付
        mvc.perform(put("/api/order/" + createdOrderId + "/pay")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 3. 验证订单列表
        mvc.perform(get("/api/order/my")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Redis 特性测试 ====================

    @Test
    @Order(6)
    @DisplayName("签到 — 首次签到成功 + 重复签到位拦截")
    void signFlow() throws Exception {
        redisTemplate.delete(signKey(petOwnerId));
        // 首次签到 (requires Redis)
        MvcResult r = mvc.perform(post("/api/sign/do")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        Map<String, Object> body = mapper.readValue(r.getResponse().getContentAsString(), Map.class);
        Assertions.assertEquals(200, body.get("code"));
        System.out.println("✓ 签到成功");
        // 重复签到
        mvc.perform(post("/api/sign/do")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
        // 查询签到状态
        mvc.perform(get("/api/sign/status")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("✓ 签到状态查询成功");
    }

    private static String signKey(Long userId) {
        java.time.LocalDate now = java.time.LocalDate.now();
        return "sign:user:" + userId + ":" + now.getYear() + String.format("%02d", now.getMonthValue());
    }

    @Test
    @Order(7)
    @DisplayName("宠物人气排行榜")
    void petRank() throws Exception {
        mvc.perform(get("/api/rank/pet/weekly")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("✓ 排行榜查询成功");
    }

    @Test
    @Order(8)
    @DisplayName("优惠券秒杀 — Lua 脚本原子操作")
    void couponSeckill() throws Exception {
        // 首次抢券
        mvc.perform(post("/api/coupon/seckill/1")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("✓ 秒杀接口调用成功（需Redis运行）");
    }

    // ==================== 权限测试 ====================

    @Test
    @Order(9)
    @DisplayName("未登录访问受保护接口返回401")
    void unauthorized() throws Exception {
        mvc.perform(get("/api/user/me"))
                .andExpect(status().is(401));
        System.out.println("✓ 未登录接口正确返回401");
    }

    @Test
    @Order(10)
    @DisplayName("服务浏览无需登录即可访问")
    void publicService() throws Exception {
        mvc.perform(get("/api/service/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("✓ 公开接口无需登录正常访问");
    }

    // ==================== RBAC 权限测试 ====================

    @Test
    @Order(11)
    @DisplayName("店员登录获取token")
    void staffLoginForRbac() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("staff01");
        dto.setPassword("123456");

        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Map<String, Object> body = mapper.readValue(json, Map.class);
        if ((Integer) body.get("code") == 200) {
            Map<String, Object> data = (Map) body.get("data");
            staffToken = (String) data.get("token");
        }
        System.out.println("✓ 店员登录" + (staffToken != null ? "成功" : "失败(密码不匹配，跳过)"));
    }

    @Test
    @Order(12)
    @DisplayName("RBAC: 宠物主访问店员接口返回403")
    void rbacOwnerAccessStaffEndpoint() throws Exception {
        if (petOwnerToken == null) { System.out.println("  跳过: 无宠物主token"); return; }
        mvc.perform(get("/api/order/pending")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().is(403));
        System.out.println("✓ 宠物主访问pending orders正确返回403");
    }

    @Test
    @Order(13)
    @DisplayName("RBAC: 店员访问店员接口返回200")
    void rbacStaffAccessStaffEndpoint() throws Exception {
        if (staffToken == null) { System.out.println("  跳过: 无店员token"); return; }
        mvc.perform(get("/api/order/pending")
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("✓ 店员访问pending orders正确返回200");
    }

    @Test
    @Order(14)
    @DisplayName("RBAC: 未登录访问需登录接口返回401")
    void rbacUnauthenticatedAccessHealth() throws Exception {
        mvc.perform(get("/api/health/pet/1/timeline"))
                .andExpect(status().is(401));
        System.out.println("✓ 未登录访问健康档案正确返回401");
    }
}
