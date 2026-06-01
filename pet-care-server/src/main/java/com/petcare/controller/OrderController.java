package com.petcare.controller;

import com.petcare.common.Result;
import com.petcare.dto.OrderDTO;
import com.petcare.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired private OrderService orderService;

    @PostMapping
    public Result<?> create(@RequestBody OrderDTO dto, HttpServletRequest req) {
        return orderService.createOrder(dto, (Long) req.getAttribute("userId"));
    }

    @GetMapping("/my")
    public Result<?> myOrders(HttpServletRequest req) {
        return orderService.getMyOrders((Long) req.getAttribute("userId"));
    }

    @GetMapping("/pending")
    public Result<?> pendingOrders(HttpServletRequest req) {
        return orderService.getPendingOrders((Integer) req.getAttribute("role"));
    }

    @PutMapping("/{id}/pay")
    public Result<?> pay(@PathVariable Long id, HttpServletRequest req) {
        return orderService.pay(id, (Long) req.getAttribute("userId"));
    }

    @PutMapping("/{id}/accept")
    public Result<?> accept(@PathVariable Long id, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1) return Result.fail(403, "Staff only");
        return orderService.accept(id, (Long) req.getAttribute("userId"));
    }

    @PutMapping("/{id}/complete")
    public Result<?> complete(@PathVariable Long id, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1) return Result.fail(403, "Staff only");
        return orderService.complete(id, (Long) req.getAttribute("userId"));
    }

    @PutMapping("/{id}/reject")
    public Result<?> reject(@PathVariable Long id, @RequestParam(required = false) String reason,
                            HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 1) return Result.fail(403, "Staff only");
        return orderService.reject(id, reason, (Long) req.getAttribute("userId"));
    }

    @PutMapping("/{id}/refund")
    public Result<?> refund(@PathVariable Long id, @RequestParam(required = false) String reason,
                            HttpServletRequest req) {
        return orderService.refund(id, reason, (Long) req.getAttribute("userId"));
    }

    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id, @RequestParam(required = false) String reason,
                            HttpServletRequest req) {
        return orderService.cancel(id, reason, (Long) req.getAttribute("userId"));
    }

    @PostMapping("/{id}/review")
    public Result<?> review(@PathVariable Long id, @RequestParam Integer rating,
                            @RequestParam(required = false) String review, HttpServletRequest req) {
        return orderService.review(id, rating, review, (Long) req.getAttribute("userId"));
    }
}
