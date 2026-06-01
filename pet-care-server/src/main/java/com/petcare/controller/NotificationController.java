package com.petcare.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.Result;
import com.petcare.entity.Notification;
import com.petcare.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Autowired private NotificationMapper mapper;

    @GetMapping("/my")
    public Result<?> myNotifications(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return Result.success(mapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreateTime)));
    }

    @GetMapping("/unread")
    public Result<?> unreadCount(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        Long count = mapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0));
        return Result.success(count);
    }

    @PutMapping("/{id}/read")
    public Result<?> markRead(@PathVariable Long id) {
        Notification n = mapper.selectById(id);
        if (n == null) return Result.fail("Not found");
        n.setIsRead(1);
        mapper.updateById(n);
        return Result.success();
    }
}
