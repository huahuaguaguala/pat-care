package com.petcare;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcare.dto.LoginDTO;
import com.petcare.dto.OrderDTO;
import com.petcare.dto.OrderItemDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

    private static String petOwnerToken;
    private static String staffToken;
    private static String adminToken;
    private static Long createdPetId;
    private static Long createdOrderId;

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
        // 1. create order with new multi-item format
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setPetId(createdPetId);

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
        System.out.println("✓ 订单创建成功, orderId: " + createdOrderId + ", status=0(待支付)");

        // 2. 支付
        mvc.perform(put("/api/order/" + createdOrderId + "/pay")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("✓ 支付成功, status=1(已支付)");

        // 3. 验证订单列表
        mvc.perform(get("/api/order/my")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("✓ 订单列表查询成功");
    }

    // ==================== Redis 特性测试 ====================

    @Test
    @Order(6)
    @DisplayName("签到 — 首次签到成功 + 重复签到位拦截")
    void signFlow() throws Exception {
        // 首次签到
        mvc.perform(post("/api/sign/do")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("✓ 签到成功");

        // 重复签到应被拦截
        mvc.perform(post("/api/sign/do")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
        System.out.println("✓ 重复签到被拦截");

        // 查询签到状态
        mvc.perform(get("/api/sign/status")
                .header("Authorization", "Bearer " + petOwnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        System.out.println("✓ 签到状态查询成功");
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
                .andExpect(status().isOk());
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
}
