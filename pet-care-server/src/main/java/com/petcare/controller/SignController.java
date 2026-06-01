package com.petcare.controller;
import com.petcare.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/sign")
public class SignController {
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    /** 查询当月签到状态 */
    @GetMapping("/status")
    public Result<?> signStatus(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        LocalDate now = LocalDate.now();
        String key = "sign:user:" + userId + ":" + now.getYear() + String.format("%02d", now.getMonthValue());

        int today = now.getDayOfMonth();
        // 获取最近7天的签到位
        int offset = Math.max(0, today - 7);
        List<Long> bits = redisTemplate.opsForValue()
                .bitField(key, BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(today - offset))
                        .valueAt(offset));

        int signCount = 0;
        int consecutiveDays = 0;
        if (bits != null && !bits.isEmpty()) {
            long val = bits.get(0);
            int len = today - offset;
            for (int i = 0; i < len; i++) {
                if ((val & (1L << i)) != 0) {
                    signCount++;
                    consecutiveDays++;
                } else {
                    consecutiveDays = 0;
                }
            }
        }

        Boolean todaySigned = redisTemplate.opsForValue().getBit(key, today - 1);

        Map<String, Object> result = new HashMap<>();
        result.put("today", today);
        result.put("todaySigned", todaySigned != null && todaySigned);
        result.put("signCount", signCount);
        result.put("consecutiveDays", consecutiveDays);
        return Result.success(result);
    }

    /** 执行签到 */
    @PostMapping("/do")
    public Result<?> doSign(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        LocalDate now = LocalDate.now();
        String key = "sign:user:" + userId + ":" + now.getYear() + String.format("%02d", now.getMonthValue());
        int offset = now.getDayOfMonth() - 1;

        Boolean signed = redisTemplate.opsForValue().getBit(key, offset);
        if (signed != null && signed) {
            return Result.fail("今日已签到");
        }

        redisTemplate.opsForValue().setBit(key, offset, true);

        // 统计连续签到天数
        List<Long> bits = redisTemplate.opsForValue()
                .bitField(key, BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(now.getDayOfMonth()))
                        .valueAt(0));
        int consecutiveDays = 0;
        if (bits != null && !bits.isEmpty()) {
            long val = bits.get(0);
            for (int i = offset; i >= 0; i--) {
                if ((val & (1L << i)) != 0) consecutiveDays++;
                else break;
            }
        }

        String msg = "签到成功";
        if (consecutiveDays == 7) msg = "连续7天签到！获得5积分奖励";
        if (consecutiveDays == 30) msg = "连续30天签到！获得优惠券一张";

        Map<String, Object> result = new HashMap<>();
        result.put("consecutiveDays", consecutiveDays);
        result.put("msg", msg);
        return Result.success(result);
    }
}
