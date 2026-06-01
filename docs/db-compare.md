# 苍穹外卖 vs 爪爪护理 — 数据库设计对比分析

## 一、苍穹外卖的设计亮点（值得借鉴）

苍穹外卖作为黑马的正规教学项目，有几个设计习惯是我们当前欠缺的：

**1. 全表审计字段 — `create_time` / `update_time` / `create_user` / `update_user`**

苍穹外卖的每张业务表都带着四个审计字段。谁在什么时候创建、谁在什么时候修改，全部可追溯。我们只有 `create_time`，丢了修改时间也丢了操作人。

**2. `order_detail` 订单明细表 — 一单多服务**

苍穹外卖的订单（orders）和订单明细（order_detail）是分开的两张表。一笔订单可以点多个菜（多行 order_detail），每个菜有自己的份数和价格快照。我们当前一笔订单只能对应一个服务，如果宠物主要"洗护+体检"就得下两单。

**3. 结算快照 — 地址和联系方式冗余到订单**

订单表里有 `address`、`consignee`、`phone`、`user_name` 这些字段，即使后来用户改了地址，历史订单的快照不会变。这是外卖行业的常见做法——防止"我下单时地址是 A，配送时你按我改了的新地址 B 送"。

**4. 取消原因的显式字段**

`cancel_reason` 和 `rejection_reason` 两个字段让订单取消和拒单的原因可量化分析，店长后台可以统计"今天取消的 3 单都是什么原因"。

**5. 套餐体系 — `setmeal` + `setmeal_dish`**

用套餐表 + 套餐菜品关系表实现组合商品，而不是在 `dish` 里加一个 `is_package` 字段硬搞。

---

## 二、我们当前表的特色（比苍穹外卖强的地方）

**1. 三角色合一 user 表**

苍穹外卖拆了两张表：employee（员工）和 user（C 端用户）。我们的宠物主、店员、店长全在一张 `user` 表里，靠 `role` 字段区分。好处是 JWT payload 统一，拦截器逻辑简单。代价是宠物主有 `username`/`password` 这两个用不上的字段设为 NULL。

**2. 宠物档案的"身份感"**

`store_no`（P-0003）让每只宠物在店内有唯一编号，这在外卖场景里不存在。加上 `chip_id`（芯片号）、`personality`（性格）、`is_neutered`（绝育状态），构成了一个完整的宠物身份档案。

**3. 疫苗 + 病历两张独立表**

`vaccination_record` 有 `next_due_date`，可以做"下次疫苗到期提醒"的定时任务。`medical_record` 有 `follow_up_date` 做复诊提醒。这两张表是宠物护理店区别于外卖和一般电商的核心——我们管的是活体动物，不是一次性商品。

**4. 宠物多图**

`pet_photo` 表支持每只宠物多张照片，带 `is_primary` 指定封面。

**5. Redis 深度集成在表设计里**

`pet.popularity` 虽然是 MySQL 字段，但它和 Redis ZSet `rank:pet:popularity:weekly` 有同步关系。`coupon.remain_stock` 也是 MySQL + Redis String 双写。表设计和 Redis 不是割裂的两层，而是一起考虑的结果。

---

## 三、需要改进的地方（按优先级排）

### P0 — 必须改

**1. 加 `update_time` 和 `update_user`**

当前只有 `create_time`，无法追溯"宠物信息是谁什么时候改的"。所有核心业务表（user、pet、order、service_item、coupon）都该加这两个字段。

参照苍穹外卖的做法：
```sql
`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
`update_user` BIGINT COMMENT 'Last modified by (user.id)'
```

**2. 增加 `order_detail` 表**

一个订单应该能包含多个服务项。当前结构 `order` 直接关联 `service_id`，是一对一的。改成一主多明细后，宠物主可以"洗护 + 体检"一个订单搞定。

```sql
CREATE TABLE `order_detail` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `service_id` BIGINT NOT NULL,
  `service_name` VARCHAR(64) COMMENT '服务名快照',
  `price` DECIMAL(10,2) COMMENT '下单时的单价快照',
  `quantity` INT DEFAULT 1,
  `subtotal` DECIMAL(10,2) COMMENT '小计',
  INDEX `idx_order_id` (`order_id`)
);
```

然后 `order.amount` 改为由 `order_detail` 汇总计算，`order.service_id` 可以去掉。

**3. `order` 加 `cancel_reason` 和 `rejection_reason`**

店长需要知道订单为什么被取消，做了数据统计才能优化服务。店员拒单也需要原因留痕。

### P1 — 应该加

**4. `service_package` 套餐表**

借鉴苍穹外卖的 `setmeal` + `setmeal_dish`，增加套餐功能。比如"幼犬护理套餐"包含基础洗护 + 疫苗接种 + 基础体检，原价 376，套餐价 298。

```sql
CREATE TABLE `service_package` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `description` TEXT,
  `price` DECIMAL(10,2) NOT NULL COMMENT '套餐总价',
  `original_price` DECIMAL(10,2) COMMENT '原价（用于显示划线价）',
  `image` VARCHAR(256),
  `status` TINYINT DEFAULT 1
);

CREATE TABLE `package_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `package_id` BIGINT NOT NULL,
  `service_id` BIGINT NOT NULL,
  `service_name` VARCHAR(64) COMMENT '冗余',
  `quantity` INT DEFAULT 1,
  INDEX `idx_package` (`package_id`)
);
```

**5. `coupon_applicable_service` 联结表代替逗号分隔字符串**

当前 `coupon.applicable_services` 是 `VARCHAR(256)` 存逗号分隔的 ID 列表（比如 `"1,2"`）。用联结表更干净，支持 JOIN 查询：

```sql
CREATE TABLE `coupon_service` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `coupon_id` BIGINT NOT NULL,
  `service_id` BIGINT NOT NULL,
  UNIQUE INDEX `uk_coupon_service` (`coupon_id`, `service_id`)
);
```

**6. `pet_breed` 品种字典表**

当前 `pet.breed_id` 和 `pet.breed_name` 是冗余存的，但没有品种字典。加一张品种表可以统一管理、做按品种的排行榜筛选。

### P2 — 锦上添花

**7. 提醒表**

疫苗接种提醒、复诊提醒、护理服务到期提醒，统一管理：

```sql
CREATE TABLE `reminder` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `pet_id` BIGINT NOT NULL,
  `owner_id` BIGINT NOT NULL,
  `type` TINYINT COMMENT '0=vaccine 1=checkup 2=grooming',
  `due_date` DATE NOT NULL,
  `message` VARCHAR(256),
  `is_sent` TINYINT DEFAULT 0,
  `sent_time` DATETIME
);
```

**8. 寄养出入记录表**

寄养（boarding）场景需要记录宠物的到店时间和离店时间、寄养期间的特殊要求、每日状态记录。

---

## 四、对比总结表

| 维度 | 苍穹外卖 | 爪爪护理 v2.0 | 差距 |
|------|----------|-------------|------|
| 审计字段 | ✅ 4字段全齐 | ❌ 只有 create_time | 必须补 |
| 订单结构 | orders + order_detail 主明细分离 | order 单表一对一 | 应该改 |
| 取消追溯 | cancel_reason + rejection_reason | ❌ 无 | 应该加 |
| 套餐体系 | setmeal + setmeal_dish | ❌ 无 | v3.0 加 |
| 用户体系 | employee + user 双表 | user 单表三角色 | ✅ 更简洁 |
| 宠物身份 | N/A | store_no + chip_id + personality | ✅ 独有 |
| 疫苗/病历 | N/A | vaccination_record + medical_record | ✅ 行业壁垒 |
| Redis联动 | 仅缓存 | ZSet排行 + BitMap签到 + Lua秒杀 | ✅ 特色 |
| 品种字典 | N/A | ❌ 冗余存储 | 应该加 |
| 多图支持 | N/A | pet_photo 表 | ✅ |
