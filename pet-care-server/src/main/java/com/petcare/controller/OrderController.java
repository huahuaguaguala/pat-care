package com.petcare.controller;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.dto.OrderDTO;
import com.petcare.entity.*;
import com.petcare.mapper.*;
import com.petcare.websocket.OrderWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired private OrderMapper orderMapper;
    @Autowired private ServiceItemMapper itemMapper;
    @Autowired private PetMapper petMapper;
    @Autowired(required = false) private RedisTemplate<String, Object> redisTemplate;
    @Autowired private OrderWebSocketHandler wsHandler;

    private final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    /** 宠物主创建订单 */
    @PostMapping
    public Result<?> create(@RequestBody OrderDTO dto, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        ServiceItem item = itemMapper.selectById(dto.getServiceId());
        if (item == null || item.getStatus() != 1) {
            return Result.fail("服务不存在或已下架");
        }
        Pet pet = petMapper.selectById(dto.getPetId());
        if (pet == null || !pet.getOwnerId().equals(userId)) {
            return Result.fail("宠物不存在或不属于您");
        }

        Order order = new Order();
        order.setOrderNo(snowflake.nextIdStr());
        order.setUserId(userId);
        order.setPetId(dto.getPetId());
        order.setServiceId(dto.getServiceId());
        order.setAmount(item.getPrice());
        order.setStatus(0); // 待支付
        order.setAppointmentTime(dto.getAppointmentTime());
        orderMapper.insert(order);

        return Result.success(order);
    }

    /** 查看我的订单 */
    @GetMapping("/my")
    public Result<?> myOrders(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreateTime));
        return Result.success(orders);
    }

    /** 店员查看待接单 */
    @GetMapping("/pending")
    public Result<?> pendingOrders(HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1) return Result.fail(403, "仅店员可操作");
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 1) // 已支付待接单
                        .orderByAsc(Order::getCreateTime));
        return Result.success(orders);
    }

    /** 支付（Mock — 直接标记已支付） */
    @PutMapping("/{id}/pay")
    public Result<?> pay(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() != 0) return Result.fail("订单状态异常");

        order.setStatus(1); // 已支付
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // WebSocket 通知所有店员（广播）
        wsHandler.sendToUser(0L,
                "{\"type\":\"new_order\",\"orderId\":" + id + ",\"msg\":\"有新订单待接单\"}");

        // 更新 Redis 热门服务计数
        if (redisTemplate != null) redisTemplate.opsForZSet().incrementScore("service:hot:daily:" +
                java.time.LocalDate.now(), order.getServiceId().toString(), 1);

        return Result.success(order);
    }

    /** 店员接单 */
    @PutMapping("/{id}/accept")
    public Result<?> accept(@PathVariable Long id, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1) return Result.fail(403, "仅店员可操作");
        Long staffId = (Long) req.getAttribute("userId");

        Order order = orderMapper.selectById(id);
        if (order == null || order.getStatus() != 1) return Result.fail("订单状态异常");

        order.setStatus(2); // 服务中
        order.setStaffId(staffId);
        orderMapper.updateById(order);

        // 通知宠物主
        wsHandler.sendToUser(order.getUserId(),
                "{\"type\":\"order_accepted\",\"orderId\":" + id + ",\"msg\":\"店员已接单\"}");

        return Result.success(order);
    }

    /** 店员完成服务 */
    @PutMapping("/{id}/complete")
    public Result<?> complete(@PathVariable Long id, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1) return Result.fail(403, "仅店员可操作");

        Order order = orderMapper.selectById(id);
        if (order == null || order.getStatus() != 2) return Result.fail("订单状态异常");

        order.setStatus(3); // 已完成
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 更新宠物人气值（Redis）
        if (redisTemplate != null) redisTemplate.opsForZSet().incrementScore("rank:pet:popularity:weekly",
                order.getPetId().toString(), 3);

        // 更新每日营收
        if (redisTemplate != null) redisTemplate.opsForValue().increment("store:revenue:daily:" +
                java.time.LocalDate.now(), order.getAmount().multiply(new BigDecimal(100)).longValue());

        // 通知宠物主
        wsHandler.sendToUser(order.getUserId(),
                "{\"type\":\"order_completed\",\"orderId\":" + id + ",\"msg\":\"服务已完成\"}");

        return Result.success(order);
    }

    /** 取消订单 */
    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() > 1) return Result.fail("订单状态不可取消");

        order.setStatus(4);
        orderMapper.updateById(order);
        return Result.success(order);
    }

    /** 评价 */
    @PostMapping("/{id}/review")
    public Result<?> review(@PathVariable Long id, @RequestParam Integer rating,
                            @RequestParam(required = false) String review, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() != 3) return Result.fail("只能评价已完成订单");
        order.setRating(rating);
        order.setReview(review);
        orderMapper.updateById(order);

        // 好评加人气
        if (rating >= 4) {
            if (redisTemplate != null) redisTemplate.opsForZSet().incrementScore("rank:pet:popularity:weekly",
                    order.getPetId().toString(), rating);
        }
        return Result.success(order);
    }
}
