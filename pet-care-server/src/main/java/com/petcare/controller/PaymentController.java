package com.petcare.controller;
import com.petcare.common.Result;
import com.petcare.entity.Order;
import com.petcare.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    @Autowired private OrderMapper orderMapper;
    @Autowired(required = false) private RedisTemplate<String, Object> redisTemplate;

    /** Generate mock QR code data for payment */
    @PostMapping("/qrcode/{orderId}")
    public Result<?> generateQR(@PathVariable Long orderId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId))
            return Result.fail("Order not found");
        if (order.getStatus() != 0) return Result.fail("Order already paid");

        String fakeQR = "petcare://pay/" + order.getOrderNo() + "/" + order.getTotalAmount();
        // In real app: call WeChat Pay API to get QR code URL
        Map<String, Object> data = new HashMap<>();
        data.put("qrCode", fakeQR);
        data.put("orderNo", order.getOrderNo());
        data.put("amount", order.getTotalAmount());
        data.put("expiresIn", 300); // 5 minutes
        return Result.success(data);
    }

    /** Mock: payment callback marks order as paid */
    @PostMapping("/callback/{orderId}")
    public Result<?> callback(@PathVariable Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) return Result.fail("Invalid state");

        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        if (redisTemplate != null) {
            redisTemplate.opsForZSet().incrementScore("service:hot:daily:" +
                    java.time.LocalDate.now(), "pay", 1);
        }
        return Result.success(order);
    }

    /** Mock: simulate scanning the QR code */
    @PostMapping("/scan/{orderId}")
    public Result<?> scan(@PathVariable Long orderId, @RequestParam(defaultValue = "123456") String pwd) {
        // Simulate wallet payment with password
        return callback(orderId);
    }
}
