package com.petcare.controller;

import com.petcare.common.Result;
import com.petcare.entity.Coupon;
import com.petcare.mapper.CouponMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CouponMapper couponMapper;

    private final DefaultRedisScript<Long> seckillScript;

    public CouponController() {
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setLocation(new ClassPathResource("lua/seckill_coupon.lua"));
        seckillScript.setResultType(Long.class);
    }

    @PostMapping("/seckill/{couponId}")
    public Result<?> seckill(@PathVariable Long couponId, HttpServletRequest req) {
        if (redisTemplate == null) {
            return Result.fail("Redis not available");
        }

        Long userId = (Long) req.getAttribute("userId");

        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            return Result.fail("Coupon not found or disabled");
        }

        String stockKey = "coupon:stock:" + couponId;
        String claimedKey = "coupon:claimed:" + couponId;

        Boolean hasKey = redisTemplate.hasKey(stockKey);
        if (hasKey == null || !hasKey) {
            redisTemplate.opsForValue().set(stockKey, coupon.getRemainStock());
        }

        Long result = redisTemplate.execute(
                seckillScript,
                Arrays.asList(stockKey, claimedKey),
                userId.toString());

        if (result == null) {
            return Result.fail("Seckill system busy");
        }
        if (result == -1) {
            return Result.fail("Already claimed this coupon");
        }
        if (result == 0) {
            return Result.fail("Coupon sold out");
        }

        Coupon update = new Coupon();
        update.setId(couponId);
        update.setRemainStock(coupon.getRemainStock() - 1);
        couponMapper.updateById(update);

        return Result.success("Coupon claimed!");
    }
}
