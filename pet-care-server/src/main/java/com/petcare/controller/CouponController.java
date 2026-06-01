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
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private CouponMapper couponMapper;

    private final DefaultRedisScript<Long> seckillScript;

    public CouponController() {
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setLocation(new ClassPathResource("lua/seckill_coupon.lua"));
        seckillScript.setResultType(Long.class);
    }

    /** 秒杀优惠券 */
    @PostMapping("/seckill/{couponId}")
    public Result<?> seckill(@PathVariable Long couponId, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");

        // 校验优惠券是否存在且有效
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            return Result.fail("优惠券不存在或已下架");
        }

        String stockKey = "coupon:stock:" + couponId;
        String claimedKey = "coupon:claimed:" + couponId;

        // 初始化 Redis 库存（首次）
        Boolean hasKey = redisTemplate.hasKey(stockKey);
        if (hasKey == null || !hasKey) {
            redisTemplate.opsForValue().set(stockKey, coupon.getRemainStock());
        }

        // KEYS[1]=stockKey, KEYS[2]=claimedKey, ARGV[1]=userId
        Long result = redisTemplate.execute(
                seckillScript,
                Arrays.asList(stockKey, claimedKey),
                userId.toString());

        if (result == null) return Result.fail("秒杀系统繁忙");
        if (result == -1) return Result.fail("您已领取过该优惠券");
        if (result == 0) return Result.fail("优惠券已被抢光");

        // 同步更新 MySQL 库存（异步更好，MVP阶段同步处理）
        Coupon update = new Coupon();
        update.setId(couponId);
        update.setRemainStock(coupon.getRemainStock()