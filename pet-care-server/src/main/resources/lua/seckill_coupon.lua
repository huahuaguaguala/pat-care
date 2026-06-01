-- 优惠券秒杀 Lua 脚本
-- KEYS[1]: coupon:stock:{couponId}
-- KEYS[2]: coupon:claimed:{couponId}
-- ARGV[1]: userId

local stockKey = KEYS[1]
local claimedKey = KEYS[2]
local userId = ARGV[1]

-- 检查是否已领取
if redis.call('SISMEMBER', claimedKey, userId) == 1 then
    return -1  -- 已领取
end

-- 检查库存
local stock = tonumber(redis.call('GET', stockKey))
if stock == nil or stock <= 0 then
    return 0  -- 库存不足
end

-- 扣库存 + 标记已领取
redis.call('DECR', stockKey)
redis.call('SADD', claimedKey, userId)
return 1  -- 成功
