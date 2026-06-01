package com.petcare.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.dto.OrderDTO;
import com.petcare.dto.OrderItemDTO;
import com.petcare.entity.*;
import com.petcare.mapper.*;
import com.petcare.service.OrderService;
import com.petcare.websocket.OrderWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderDetailMapper detailMapper;
    @Autowired private ServiceItemMapper itemMapper;
    @Autowired private PetMapper petMapper;
    @Autowired private NotificationMapper notificationMapper;
    @Autowired(required = false) private RedisTemplate<String, Object> redisTemplate;
    @Autowired private OrderWebSocketHandler wsHandler;

    private final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    @Override
    public Result<?> createOrder(OrderDTO dto, Long userId) {
        Pet pet = petMapper.selectById(dto.getPetId());
        if (pet == null || !pet.getOwnerId().equals(userId))
            return Result.fail("Pet not found or not yours");
        if (dto.getItems() == null || dto.getItems().isEmpty())
            return Result.fail("At least one service required");

        // Slot capacity check
        if (dto.getAppointmentTime() != null) {
            for (OrderItemDTO it : dto.getItems()) {
                ServiceItem svc = itemMapper.selectById(it.getServiceId());
                if (svc == null || svc.getStatus() != 1)
                    return Result.fail("Service not found: " + it.getServiceId());
                if (svc.getMaxPerSlot() == null || svc.getMaxPerSlot() <= 0) continue;
                Long booked = detailMapper.selectCount(new LambdaQueryWrapper<OrderDetail>()
                        .eq(OrderDetail::getServiceId, svc.getId())
                        .apply("order_id IN (SELECT id FROM `order` WHERE status IN (0,1,2))"));
                if (booked != null && booked >= svc.getMaxPerSlot())
                    return Result.fail(svc.getName() + " is fully booked.");
            }
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderDetail> details = new ArrayList<>();
        for (OrderItemDTO it : dto.getItems()) {
            ServiceItem svc = itemMapper.selectById(it.getServiceId());
            if (svc == null || svc.getStatus() != 1)
                return Result.fail("Service not found: " + it.getServiceId());
            OrderDetail d = new OrderDetail();
            d.setServiceId(svc.getId()); d.setServiceName(svc.getName());
            d.setPrice(svc.getPrice()); d.setQuantity(it.getQuantity() != null ? it.getQuantity() : 1);
            d.setSubtotal(svc.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
            d.setCreateUser(userId); details.add(d);
            total = total.add(d.getSubtotal());
        }

        Order order = new Order();
        order.setOrderNo(snowflake.nextIdStr());
        order.setOrderType(0); order.setUserId(userId); order.setPetId(dto.getPetId());
        order.setTotalAmount(total); order.setStatus(0);
        order.setAppointmentTime(dto.getAppointmentTime());
        order.setRemark(dto.getRemark()); order.setCreateUser(userId);
        orderMapper.insert(order);
        for (OrderDetail d : details) { d.setOrderId(order.getId()); detailMapper.insert(d); }

        List<OrderDetail> saved = detailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, order.getId()));
        Map<String, Object> result = new HashMap<>();
        result.put("order", order); result.put("details", saved);
        return Result.success(result);
    }

    @Override
    public Result<?> getMyOrders(Long userId) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            List<OrderDetail> details = detailMapper.selectList(
                    new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, o.getId()));
            Map<String, Object> m = new HashMap<>(); m.put("order", o); m.put("details", details);
            result.add(m);
        }
        return Result.success(result);
    }

    @Override
    public Result<?> getPendingOrders(Integer role) {
        if (role != 1) return Result.fail(403, "Staff only");
        return Result.success(orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 1).orderByAsc(Order::getCreateTime)));
    }

    @Override
    public Result<?> pay(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) return Result.fail("Order not found");
        if (order.getStatus() != 0) return Result.fail("Invalid status");
        order.setStatus(1); order.setPayTime(LocalDateTime.now()); order.setUpdateUser(userId);
        orderMapper.updateById(order);
        wsHandler.sendToUser(0L, "{\"type\":\"new_order\",\"orderId\":" + orderId + "}");
        if (redisTemplate != null) {
            List<OrderDetail> details = detailMapper.selectList(
                    new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId));
            for (OrderDetail d : details)
                redisTemplate.opsForZSet().incrementScore("service:hot:daily:" + java.time.LocalDate.now(), d.getServiceId().toString(), 1);
        }
        return Result.success(order);
    }

    @Override
    public Result<?> accept(Long orderId, Long staffId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1) return Result.fail("Invalid status");
        order.setStatus(2); order.setStaffId(staffId);
        order.setActualStartTime(LocalDateTime.now()); order.setUpdateUser(staffId);
        orderMapper.updateById(order);
        wsHandler.sendToUser(order.getUserId(), "{\"type\":\"order_accepted\",\"orderId\":" + orderId + "}");
        return Result.success(order);
    }

    @Override
    public Result<?> complete(Long orderId, Long staffId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 2) return Result.fail("Invalid status");
        order.setStatus(3); order.setCompleteTime(LocalDateTime.now());
        order.setActualEndTime(LocalDateTime.now()); order.setUpdateUser(staffId);
        orderMapper.updateById(order);
        if (redisTemplate != null) {
            redisTemplate.opsForZSet().incrementScore("rank:pet:popularity:weekly", order.getPetId().toString(), 5);
            redisTemplate.opsForValue().increment("store:revenue:daily:" + java.time.LocalDate.now(),
                    order.getTotalAmount().multiply(new BigDecimal(100)).longValue());
        }
        wsHandler.sendToUser(order.getUserId(), "{\"type\":\"order_completed\",\"orderId\":" + orderId + "}");

        Notification n = new Notification();
        n.setUserId(order.getUserId()); n.setPetId(order.getPetId()); n.setType(2);
        n.setTitle("Order completed");
        n.setContent("Your order #" + order.getOrderNo() + " has been completed.");
        notificationMapper.insert(n);
        return Result.success(order);
    }

    @Override
    public Result<?> reject(Long orderId, String reason, Long staffId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1) return Result.fail("Invalid status");
        order.setStatus(4); order.setRejectionReason(reason); order.setUpdateUser(staffId);
        orderMapper.updateById(order);
        wsHandler.sendToUser(order.getUserId(), "{\"type\":\"order_rejected\",\"orderId\":" + orderId + "}");
        Notification n = new Notification();
        n.setUserId(order.getUserId()); n.setPetId(order.getPetId()); n.setType(2);
        n.setTitle("Order rejected"); n.setContent("Reason: " + reason);
        notificationMapper.insert(n);
        return Result.success(order);
    }

    @Override
    public Result<?> refund(Long orderId, String reason, Long operatorId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || (order.getStatus() != 3 && order.getStatus() != 1))
            return Result.fail("Can only refund paid/completed orders");
        order.setStatus(4); order.setRefundTime(LocalDateTime.now());
        order.setRefundReason(reason); order.setRefundAmount(order.getTotalAmount());
        order.setUpdateUser(operatorId); orderMapper.updateById(order);
        if (redisTemplate != null)
            redisTemplate.opsForValue().increment("store:revenue:daily:" + java.time.LocalDate.now(), order.getTotalAmount().multiply(new BigDecimal(-100)).longValue());
        return Result.success(order);
    }

    @Override
    public Result<?> cancel(Long orderId, String reason, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) return Result.fail("Order not found");
        if (order.getStatus() > 1) return Result.fail("Cannot cancel");
        order.setStatus(4); order.setCancelReason(reason); order.setUpdateUser(userId);
        orderMapper.updateById(order);
        return Result.success(order);
    }

    @Override
    public Result<?> review(Long orderId, Integer rating, String review, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) return Result.fail("Order not found");
        if (order.getStatus() != 3) return Result.fail("Can only review completed orders");
        order.setRating(rating); order.setReview(review); order.setUpdateUser(userId);
        orderMapper.updateById(order);
        if (rating >= 4 && redisTemplate != null)
            redisTemplate.opsForZSet().incrementScore("rank:pet:popularity:weekly", order.getPetId().toString(), rating);
        return Result.success(order);
    }
}
