# 爪爪护理 API v3.2 接口文档

> Base URL: `http://localhost:8080`
> Auth Header: `Authorization: Bearer <token>`（标注 🔓 的无需登录）

---

## 1. 认证 `/api/auth`

| 方法 | 路径 | 说明 | 角色 | Body |
|------|------|------|------|------|
| POST 🔓 | `/api/auth/wechat-login` | 微信登录（新用户自动注册） | 宠物主 | `{"openid":"..."}` |
| POST 🔓 | `/api/auth/login` | 账号密码登录 | 店员/店长 | `{"username":"staff01","password":"123456"}` |

**响应：**
```json
{"code":200, "data":{"token":"eyJ...", "user":{"id":2,"nickname":"Staff Li","role":1}}}
```

---

## 2. 用户 `/api/user`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/user/me` | 当前用户信息 | ALL |
| PUT | `/api/user/me` | 更新个人信息 | ALL |
| GET | `/api/user/staff` | 店员列表 | 店长 |
| POST | `/api/user/staff` | 新增店员 | 店长 |
| PUT | `/api/user/staff/{id}/status` | 禁用/启用店员 `?status=1` | 店长 |

---

## 3. 宠物 `/api/pet`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | `/api/pet` | 添加宠物 | 宠物主 |
| GET | `/api/pet/my` | 我的宠物列表 | 宠物主 |
| GET | `/api/pet/{id}` | 宠物详情 | ALL |
| PUT | `/api/pet/{id}` | 更新宠物信息 | 宠物主 |
| DELETE | `/api/pet/{id}` | 删除宠物 | 宠物主 |

**添加宠物 Body：**
```json
{"name":"Buddy","breedId":1,"breedName":"Corgi","birthday":"2024-03-15","gender":0,"weight":12,"personality":"Friendly","notes":"No allergies","isNeutered":0,"chipId":"CHIP123"}
```

---

## 4. 品种字典 `/api/breed`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET 🔓 | `/api/breed` | 品种列表 | ALL |
| GET 🔓 | `/api/breed?category=Dog` | 按分类筛选 | ALL |
| POST | `/api/breed` | 新增品种 | 店长 |
| PUT | `/api/breed/{id}` | 编辑品种 | 店长 |

---

## 5. 服务品类 `/api/service`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET 🔓 | `/api/service/category` | 已启用品类 | ALL |
| GET | `/api/service/category/all` | 全部品类（含禁用） | 店长 |
| POST | `/api/service/category` | 新增品类 | 店长 |
| PUT | `/api/service/category/{id}` | 编辑品类 | 店长 |
| DELETE | `/api/service/category/{id}` | 删除品类 | 店长 |

---

## 6. 服务项 `/api/service`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET 🔓 | `/api/service/item` | 已启用 + 在有效期内的服务 `?categoryId=1` 可选 | ALL |
| GET | `/api/service/item/all` | 全部服务（含禁用、已过期） | 店员/店长 |
| GET | `/api/service/item/{id}` | 服务详情 | ALL |
| POST | `/api/service/item` | 新增服务 | 店员/店长 |
| PUT | `/api/service/item/{id}` | 编辑服务 | 店员/店长 |
| DELETE | `/api/service/item/{id}` | 删除服务 | 店长 |
| PUT | `/api/service/item/{id}/toggle` | 开关服务（启用⇔禁用） | 店员/店长 |
| PUT | `/api/service/item/{id}/timerange?startDate=2026-12-01&endDate=2026-12-31` | 设置服务有效期 | 店员/店长 |

**新增/编辑服务 Body：**
```json
{"categoryId":1,"name":"圣诞美容","description":"节日限定","price":388,"duration":60,"maxPerSlot":2,"startDate":"2026-12-01","endDate":"2026-12-31"}
```

---

## 7. 套餐 `/api/package`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET 🔓 | `/api/package` | 套餐列表（含所含服务） | ALL |
| GET 🔓 | `/api/package/{id}` | 套餐详情 | ALL |
| POST | `/api/package` | 新增套餐 | 店长 |
| PUT | `/api/package/{id}` | 编辑套餐 | 店长 |
| POST | `/api/package/{id}/items` | 批量添加套餐服务项 | 店长 |

**新增套餐 Body：**
```json
{"name":"新春护理","description":"洗澡+体检+疫苗","price":298,"originalPrice":376}
```

**添加服务项 Body：**
```json
[{"serviceId":1,"serviceName":"Basic Wash","quantity":1},{"serviceId":3,"serviceName":"Basic Checkup","quantity":1}]
```

---

## 8. 订单 `/api/order`

### 创建订单（支持一单多服务）

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | `/api/order` | 创建订单（含容量检查） | 宠物主 |
| GET | `/api/order/my` | 我的订单（含明细） | 宠物主 |
| GET | `/api/order/pending` | 待接单列表 | 店员 |
| PUT | `/api/order/{id}/pay` | Mock 支付 | 宠物主 |
| PUT | `/api/order/{id}/accept` | 接单 | 店员 |
| PUT | `/api/order/{id}/complete` | 完成服务 | 店员 |
| PUT | `/api/order/{id}/cancel?reason=xxx` | 取消（带原因） | 宠物主 |
| POST | `/api/order/{id}/review?rating=5&review=xxx` | 评价 | 宠物主 |

**创建订单 Body：**
```json
{
  "petId": 3,
  "appointmentTime": "2026-06-02T10:00:00",
  "remark": "请温柔对待",
  "items": [
    {"serviceId": 1, "quantity": 1},
    {"serviceId": 3, "quantity": 1}
  ]
}
```

**响应：**
```json
{
  "code": 200,
  "data": {
    "order": {"id":5, "orderNo":"2061...", "totalAmount":226.00, "status":0},
    "details": [
      {"serviceId":1, "serviceName":"Basic Wash", "price":128, "subtotal":128},
      {"serviceId":3, "serviceName":"Basic Checkup", "price":98, "subtotal":98}
    ]
  }
}
```

**容量检查：** 创建订单时自动检查每个服务的 `maxPerSlot`，若当前活跃订单数超限则返回 `"Basic Wash is fully booked. Try a different time."`

**订单状态流：** `0=待支付 → 1=已支付 → 2=服务中 → 3=已完成 | 4=已取消`

---

## 9. 扫码支付 `/api/payment`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | `/api/payment/qrcode/{orderId}` | 生成支付二维码（Mock） | 宠物主 |
| POST 🔓 | `/api/payment/scan/{orderId}` | 扫码支付完成（Mock） | ALL |

**生成二维码响应：**
```json
{"qrCode":"petcare://pay/2061.../98.00","orderNo":"2061...","amount":98.00,"expiresIn":300}
```

---

## 10. 签到 `/api/sign`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/sign/status` | 当月签到状态 | 宠物主 |
| POST | `/api/sign/do` | 执行签到 | 宠物主 |

**状态响应：**
```json
{"today":1,"todaySigned":true,"signCount":1,"consecutiveDays":1}
```

---

## 11. 排行榜 `/api/rank`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET 🔓 | `/api/rank/pet/weekly` | 本周宠物人气榜 | ALL |
| GET 🔓 | `/api/rank/service/hot` | 今日热门服务 | ALL |

---

## 12. 秒杀 `/api/coupon`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | `/api/coupon/seckill/{couponId}` | 秒杀优惠券（Lua 原子操作） | 宠物主 |

---

## 13. 体重记录 `/api/weight`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET 🔓 | `/api/weight/pet/{petId}` | 体重变化历史 | ALL |
| POST | `/api/weight` | 记录体重 | 店员/店长 |

**记录体重 Body：**
```json
{"petId":3,"weight":12.5,"notes":"After grooming"}
```

---

## 14. 健康档案 `/api/health`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET 🔓 | `/api/health/pet/{petId}/timeline` | 健康时间线（疫苗+病历+体重） | ALL |

**响应：** 按时间倒序的混合列表，每项带 `type`（vaccine/medical/weight）、`date`、`title`、`detail`。

---

## 15. 数据看板 `/api/dashboard`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/dashboard/today` | 今日营收 + 热门服务 Top5 | 店员/店长 |
| GET | `/api/dashboard/staff/{staffId}` | 某店员今日统计 | 店长 |

**响应：**
```json
{"date":"2026-06-01","revenue":48200,"hotServices":[{"serviceId":1,"score":3}]}
```

---

## 16. 店员排班 `/api/schedule`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/schedule/staff` | 我的排班 | 店员 |
| GET | `/api/schedule/staff/{staffId}` | 某店员排班 `?date=2026-06-01` | 店长 |
| POST | `/api/schedule` | 新增排班 | 店长 |
| DELETE | `/api/schedule/{id}` | 删除排班 | 店长 |

**新增排班 Body：**
```json
{"staffId":2,"workDate":"2026-06-03","startTime":"09:00","endTime":"18:00","maxSlots":3}
```

---

## 17. 店员时段开关 `/api/availability`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/availability/my` | 我的时段开关状态 | 店员 |
| POST | `/api/availability/toggle` | 开关时段 | 店员 |

**关闭时段 Body：**
```json
{"slotDate":"2026-06-02","slotStart":"14:00","slotEnd":"16:00","isOpen":0,"reason":"Personal leave"}
```

---

## 测试账号

| 角色 | 方式 | 账号/OpenID | 密码 |
|------|------|------------|------|
| 宠物主 | 微信登录 | `mock_openid_001` | — |
| 店员 | 账号登录 | `staff01` | `123456` |
| 店长 | 账号登录 | `admin01` | `123456` |

---

## 数据库表总览（16 张）

| # | 表名 | 说明 |
|---|------|------|
| 1 | user | 三角色统一用户表 |
| 2 | pet | 宠物档案（含店内编号、芯片号、性格） |
| 3 | pet_photo | 宠物多图相册 |
| 4 | pet_breed | 品种字典（含分类、平均体重/寿命） |
| 5 | service_category | 服务品类 |
| 6 | service_item | 服务项（支持有效期、限时开放） |
| 7 | service_package | 套餐（组合服务打折） |
| 8 | package_item | 套餐所含服务项 |
| 9 | order | 订单头（一主多明细模式） |
| 10 | order_detail | 订单明细（单价快照） |
| 11 | vaccination_record | 疫苗接种记录 |
| 12 | medical_record | 病历记录 |
| 13 | weight_record | 体重变化记录 |
| 14 | staff_schedule | 店员排班 |
| 15 | staff_availability | 店员自主时段开关 |
| 16 | coupon | 优惠券（库存 Redis + MySQL 双写） |

---

## Redis Key 一览

| Key | 类型 | 说明 |
|-----|------|------|
| `sign:user:{uid}:{yyyyMM}` | BitMap | 签到 |
| `rank:pet:popularity:weekly` | ZSet | 宠物人气榜 |
| `service:hot:daily:{date}` | ZSet | 热门服务 |
| `store:revenue:daily:{date}` | String | 每日营收 |
| `coupon:stock:{id}` | String | 秒杀库存 |
| `coupon:claimed:{id}` | Set | 已领用户去重 |
| `rank:staff:daily:{date}` | ZSet | 店员评分 |
