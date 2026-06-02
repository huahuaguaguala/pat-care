package com.petcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.RequireRole;
import com.petcare.common.Result;
import com.petcare.entity.Order;
import com.petcare.entity.OrderDetail;
import com.petcare.mapper.OrderDetailMapper;
import com.petcare.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminReportController {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderDetailMapper detailMapper;

    /** Admin: list orders by date range + optional status filter */
    @GetMapping("/orders")
    @RequireRole({1, 2})
    public Result<?> ordersByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, startDate.atStartOfDay())
                .le(Order::getCreateTime, endDate.plusDays(1).atStartOfDay())
                .orderByDesc(Order::getCreateTime);

        if (status != null) qw.eq(Order::getStatus, status);

        List<Order> orders = orderMapper.selectList(qw);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            List<OrderDetail> details = detailMapper.selectList(
                    new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, o.getId()));
            Map<String, Object> m = new HashMap<>();
            m.put("order", o);
            m.put("details", details);
            result.add(m);
        }
        return Result.success(result);
    }

    /** Admin: revenue report by date range */
    @GetMapping("/revenue")
    @RequireRole(2)
    public Result<?> revenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Query completed orders in date range
        List<Order> completedOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 3)
                .ge(Order::getCompleteTime, startDate.atStartOfDay())
                .le(Order::getCompleteTime, endDate.plusDays(1).atStartOfDay()));

        // Query refunded orders in date range
        List<Order> refundedOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 4)
                .isNotNull(Order::getRefundTime)
                .ge(Order::getRefundTime, startDate.atStartOfDay())
                .le(Order::getRefundTime, endDate.plusDays(1).atStartOfDay()));

        // Aggregate by day
        Map<LocalDate, BigDecimal> dailyRevenue = new TreeMap<>();
        Map<LocalDate, BigDecimal> dailyRefund = new TreeMap<>();
        Map<LocalDate, Integer> dailyOrderCount = new TreeMap<>();

        for (Order o : completedOrders) {
            LocalDate day = o.getCompleteTime().toLocalDate();
            dailyRevenue.merge(day, o.getTotalAmount(), BigDecimal::add);
            dailyOrderCount.merge(day, 1, Integer::sum);
        }
        for (Order o : refundedOrders) {
            LocalDate day = o.getRefundTime().toLocalDate();
            dailyRefund.merge(day, o.getRefundAmount() != null ? o.getRefundAmount() : o.getTotalAmount(), BigDecimal::add);
        }

        // Build summary
        BigDecimal totalRevenue = completedOrders.stream()
                .map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRefund = refundedOrders.stream()
                .map(o -> o.getRefundAmount() != null ? o.getRefundAmount() : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalOrders = completedOrders.size();
        int totalRefundOrders = refundedOrders.size();

        // Daily breakdown
        List<Map<String, Object>> dailyBreakdown = new ArrayList<>();
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            Map<String, Object> d = new HashMap<>();
            d.put("date", day.toString());
            d.put("revenue", dailyRevenue.getOrDefault(day, BigDecimal.ZERO));
            d.put("refund", dailyRefund.getOrDefault(day, BigDecimal.ZERO));
            d.put("orderCount", dailyOrderCount.getOrDefault(day, 0));
            dailyBreakdown.add(d);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        result.put("totalRevenue", totalRevenue);
        result.put("totalRefund", totalRefund);
        result.put("netRevenue", totalRevenue.subtract(totalRefund));
        result.put("totalOrders", totalOrders);
        result.put("totalRefundOrders", totalRefundOrders);
        result.put("dailyBreakdown", dailyBreakdown);

        return Result.success(result);
    }
}
