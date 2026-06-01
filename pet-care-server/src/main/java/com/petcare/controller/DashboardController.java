package com.petcare.controller;
import com.petcare.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @Autowired(required = false) private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/today")
    public Result<?> today(HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2 && role != 1) return Result.fail(403, "Staff/admin only");

        String today = LocalDate.now().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("date", today);

        if (redisTemplate != null) {
            Object revenue = redisTemplate.opsForValue().get("store:revenue:daily:" + today);
            data.put("revenue", revenue != null ? revenue : 0);

            Set<?> hot = redisTemplate.opsForZSet()
                    .reverseRangeWithScores("service:hot:daily:" + today, 0, 4);
            data.put("hotServices", hot != null ? hot : Collections.emptyList());
        } else {
            data.put("revenue", 0);
            data.put("hotServices", Collections.emptyList());
            data.put("note", "Redis not available");
        }
        return Result.success(data);
    }

    @GetMapping("/staff/{staffId}")
    public Result<?> staffStats(@PathVariable Long staffId, HttpServletRequest req) {
        Integer role = (Integer) req.getAttribute("role");
        if (role != 2) return Result.fail(403, "Admin only");

        String key = "rank:staff:daily:" + LocalDate.now();
        Map<String, Object> data = new HashMap<>();
        if (redisTemplate != null) {
            Double score = redisTemplate.opsForZSet().score(key, staffId.toString());
            data.put("todayScore", score != null ? score : 0);
        }
        return Result.success(data);
    }
}
