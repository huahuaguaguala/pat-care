# 爪爪护理 — MVP 项目规划

> PM: 项目范围定义、Sprint 计划、接口清单、里程碑
> 版本: MVP v1.0 | 日期: 2026-06-01

---

## 一、MVP 范围定义

### 包含（Phase 1-4）

| 模块 | 范围 |
|------|------|
| 用户系统 | 微信登录（宠物主）、账号密码登录（店员/店长）、JWT 认证、RBAC 鉴权 |
| 宠物档案 | CRUD、按宠物主查询列表 |
| 服务管理 | 品类 CRUD、服务项 CRUD、按品类浏览 |
| 预约下单 | 时段选择、创建订单、微信支付（沙箱 / Mock） |
| 店员工作台 | 待接单列表、接单、查看宠物档案、完成服务 |
| 订单管理 | 订单列表（按角色过滤）、状态流转 |
| Redis 特性 | 宠物人气排行(ZSet)、每日签到(BitMap)、优惠券秒杀(Lua+锁) |
| 前端 | 小程序 8 页面 + Vue3 管理后台 4 页面（骨架） |

### 不包含（后续迭代）

- 实时排队队列(List)、店员之星(ZSet)、附近宠友(GEO)、UV统计(HyperLogLog)、推荐系统
- 优惠券完整发放/核销流程（仅秒杀接口）
- 小程序 UI 精修、管理后台数据看板图表

---

## 二、Sprint 计划

### Sprint 1：骨架搭建（Phase 2）

**目标：** 项目能跑起来，认证通，基础 CRUD 可用

| 任务 | 预估 | 产出 |
|------|------|------|
| Spring Boot 项目初始化 | 30min | pom.xml, application.yml |
| MySQL DDL 建表 | 15min | schema.sql |
| MyBatis-Plus 实体 + Mapper | 30min | 6 张表实体 |
| JWT 认证体系 | 45min | 登录接口 + 拦截器 + 角色注解 |
| 用户/宠物/服务 CRUD API | 60min | 12+ 接口 |
| Git 提交 | — | commit: "sprint1:" |

### Sprint 2：核心业务链路（Phase 3）

**目标：** 一笔订单从预约到完成完整走通

| 任务 | 预估 | 产出 |
|------|------|------|
| 服务浏览 + 时段查询 | 30min | 3 接口 |
| 预约下单 + 支付(Mock) | 45min | 3 接口 |
| 店员接单 + 完成 | 30min | 3 接口 |
| 订单状态流转 | 30min | 状态机 |
| WebSocket 新订单推送 | 30min | WebSocketConfig |
| Git 提交 | — | commit: "sprint2:" |

### Sprint 3：Redis 创意特性（Phase 4）

**目标：** 3 个 Redis 深度特性上线

| 任务 | 预估 | 产出 |
|------|------|------|
| 宠物人气排行榜(ZSet) | 45min | API + 定时归档 |
| 每日签到(BitMap) | 30min | API + 连续判定 |
| 优惠券秒杀(Lua + 锁) | 45min | API + Lua脚本 |
| Git 提交 | — | commit: "sprint3:" |

### Sprint 4：前端骨架（Phase 5）

**目标：** 小程序和管理后台页面骨架可展示

| 任务 | 预估 | 产出 |
|------|------|------|
| 小程序 8 页面骨架 | 60min | WXML + JS |
| Vue3 后台 4 页面骨架 | 45min | Vue + Router |
| Git 提交 | — | commit: "sprint4:" |

### Sprint 5：测试（Phase 6）

**目标：** 核心链路可验证

| 任务 | 预估 | 产出 |
|------|------|------|
| 认证接口测试 | 15min | JUnit |
| 预约链路测试 | 20min | JUnit |
| 秒杀接口测试 | 20min | JUnit |
| 测试报告 | 15min | test-report.md |
| Git 提交 | — | commit: "sprint5:" |

---

## 三、API 接口清单

### 认证模块 `POST /api/auth`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | /api/auth/wechat-login | 微信登录 | 宠物主 |
| POST | /api/auth/login | 账号密码登录 | 店员/店长 |
| POST | /api/auth/logout | 退出登录 | ALL |

### 用户模块 `/api/user`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | /api/user/me | 当前用户信息 | ALL |
| PUT | /api/user/me | 更新个人信息 | ALL |
| GET | /api/user/staff | 店员列表 | 店长 |
| POST | /api/user/staff | 新增店员 | 店长 |
| PUT | /api/user/staff/{id}/status | 禁用/启用店员 | 店长 |

### 宠物模块 `/api/pet`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | /api/pet | 添加宠物 | 宠物主 |
| GET | /api/pet/my | 我的宠物列表 | 宠物主 |
| GET | /api/pet/{id} | 宠物详情 | ALL |
| PUT | /api/pet/{id} | 更新宠物信息 | 宠物主 |
| DELETE | /api/pet/{id} | 删除宠物 | 宠物主 |

### 服务模块 `/api/service`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | /api/service/category | 品类列表 | ALL |
| POST | /api/service/category | 新增品类 | 店长 |
| GET | /api/service/item | 服务项列表(按品类) | ALL |
| POST | /api/service/item | 新增服务项 | 店长 |
| GET | /api/service/item/{id} | 服务项详情 | ALL |

### 订单模块 `/api/order`

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | /api/order | 创建订单 | 宠物主 |
| GET | /api/order/my | 我的订单列表 | 宠物主 |
| GET | /api/order/pending | 待接单列表 | 店员 |
| GET | /api/order/{id} | 订单详情 | ALL |
| PUT | /api/order/{id}/pay | 支付(Mock) | 宠物主 |
| PUT | /api/order/{id}/accept | 接单 | 店员 |
| PUT | /api/order/{id}/complete | 完成服务 | 店员 |
| PUT | /api/order/{id}/cancel | 取消订单 | 宠物主 |
| POST | /api/order/{id}/review | 评价 | 宠物主 |

### Redis 特性模块

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | /api/rank/pet/weekly | 本周宠物人气榜 | ALL |
| GET | /api/sign/status | 签到状态(当月) | 宠物主 |
| POST | /api/sign/do | 执行签到 | 宠物主 |
| POST | /api/coupon/seckill/{couponId} | 秒杀优惠券 | 宠物主 |

---

## 四、数据库 DDL

```sql
-- 用户表
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `openid` VARCHAR(64) COMMENT '微信openid',
  `username` VARCHAR(32) COMMENT '账号',
  `password` VARCHAR(128) COMMENT 'BCrypt密码',
  `nickname` VARCHAR(32),
  `phone` VARCHAR(16),
  `avatar` VARCHAR(256),
  `role` TINYINT NOT NULL DEFAULT 0 COMMENT '0=宠物主 1=店员 2=店长',
  `store_id` BIGINT DEFAULT 1,
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_openid` (`openid`),
  INDEX `idx_role` (`role`)
);

-- 宠物档案
CREATE TABLE `pet` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `owner_id` BIGINT NOT NULL,
  `name` VARCHAR(32) NOT NULL,
  `breed_id` INT,
  `breed_name` VARCHAR(32),
  `age` DECIMAL(3,1),
  `gender` TINYINT COMMENT '0=公 1=母',
  `weight` DECIMAL(5,2),
  `avatar` VARCHAR(256),
  `notes` VARCHAR(512),
  `popularity` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_owner` (`owner_id`)
);

-- 服务品类
CREATE TABLE `service_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(32) NOT NULL,
  `icon` VARCHAR(256),
  `sort` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1
);

-- 服务项
CREATE TABLE `service_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `category_id` BIGINT NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `description` TEXT,
  `price` DECIMAL(10,2) NOT NULL,
  `duration` INT COMMENT '预计时长(分钟)',
  `max_per_slot` INT DEFAULT 3,
  `image` VARCHAR(256),
  `status` TINYINT DEFAULT 1,
  INDEX `idx_category` (`category_id`)
);

-- 订单
CREATE TABLE `order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `pet_id` BIGINT NOT NULL,
  `service_id` BIGINT NOT NULL,
  `staff_id` BIGINT,
  `store_id` BIGINT DEFAULT 1,
  `amount` DECIMAL(10,2) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待支付 1=已支付 2=服务中 3=已完成 4=已取消',
  `appointment_time` DATETIME,
  `pay_time` DATETIME,
  `complete_time` DATETIME,
  `rating` TINYINT,
  `review` VARCHAR(512),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE INDEX `idx_order_no` (`order_no`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_staff` (`staff_id`),
  INDEX `idx_status` (`status`)
);

-- 优惠券
CREATE TABLE `coupon` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  `type` TINYINT COMMENT '0=满减 1=折扣',
  `threshold` DECIMAL(10,2),
  `discount` DECIMAL(10,2),
  `total_stock` INT NOT NULL,
  `remain_stock` INT NOT NULL,
  `per_user_limit` INT DEFAULT 1,
  `start_time` DATETIME,
  `end_time` DATETIME,
  `status` TINYINT DEFAULT 1
);
```

---

## 五、后端项目结构

```
pet-care-server/
└── src/main/java/com/petcare/
    ├── PetCareApplication.java
    ├── config/
    │   ├── SecurityConfig.java
    │   ├── RedisConfig.java
    │   ├── WebSocketConfig.java
    │   └── WebMvcConfig.java
    ├── interceptor/
    │   └── JwtInterceptor.java
    ├── controller/
    │   ├── AuthController.java
    │   ├── UserController.java
    │   ├── PetController.java
    │   ├── ServiceController.java
    │   ├── OrderController.java
    │   ├── RankController.java
    │   ├── SignController.java
    │   └── CouponController.java
    ├── service/
    │   ├── impl/
    │   │   ├── AuthServiceImpl.java
    │   │   ├── UserServiceImpl.java
    │   │   ├── PetServiceImpl.java
    │   │   ├── OrderServiceImpl.java
    │   │   ├── RankServiceImpl.java
    │   │   ├── SignServiceImpl.java
    │   │   └── CouponServiceImpl.java
    │   └── ... (接口)
    ├── mapper/
    ├── entity/
    ├── dto/
    ├── vo/
    ├── common/
    │   ├── Result.java
    │   ├── PageResult.java
    │   └── BusinessException.java
    └── utils/
        ├── JwtUtils.java
        └── SnowflakeIdGenerator.java
```

---

## 六、里程碑与 Git Commit 计划

| 里程碑 | Commit Message | 内容 |
|--------|---------------|------|
| M0 | `init: 项目初始化` | git init + README |
| M1 | `sprint1: 工程骨架 + 认证 + 基础CRUD` | pom.xml, DDL, JWT, CRUD API |
| M2 | `sprint2: 核心业务链路（预约→支付→接单→完成）` | 订单全流程 + WebSocket |
| M3 | `sprint3: Redis创意特性（排行榜+签到+秒杀）` | ZSet + BitMap + Lua |
| M4 | `sprint4: 前端骨架（小程序 + 管理后台）` | 小程序8页 + Vue3 4页 |
| M5 | `sprint5: 测试用例 + 测试报告` | JUnit + 报告 |

---

## 七、风险与假设

- **微信支付沙箱：** MVP 阶段使用 Mock 支付，生产环境替换为 V3 API
- **微信登录：** MVP 阶段用 openid mock，需真实 AppID 才可联调
- **文件上传：** MVP 阶段使用本地存储，生产替换 OSS
- **Docker Compose：** 提供 MySQL + Redis 一键启动脚本
