package com.petcare.controller;
import com.petcare.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/rank")
public class RankController {
    @Autowired(required = false) private RedisTemplate<String, Object> redisTemplate;

    /** 本周宠物人气榜 Top10 */
    @GetMapping("/pet/weekly")
    public Result<?> weeklyPetRank() {
        if (redisTemplate == null) return Result.fail("请先启动Redis");
        String key = "rank:pet:popularity:weekly";
        Set<ZSetOperations.TypedTuple<Object>> top10 = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, 9);
        List<Map<String, Object>> result = new ArrayList<>();
        if (top10 != null) {
            for (ZSetOperations.TypedTuple<Object> t : top10) {
                Map<String, Object> m = new HashMap<>();
                m.put("petId", t.getValue());
                m.put("score", t.getScore());
                result.add(m);
            }
        }
        return Result.success(result);
    }

    /** 热门服务排行 */
    @GetMapping("/service/hot")
    public Result<?> hotService() {
        if (redisTemplate == null) return Result.fail("请先启动Redis");
        String key = "service:hot:daily:" + java.time.LocalDate.now();
        Set<ZSetOperations.TypedTuple<Object>> top = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0, 4);
        return Result.success(top);
    }
}
