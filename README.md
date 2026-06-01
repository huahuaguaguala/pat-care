# ZippyCare — 宠物护理店 O2O 驻店系统

> 借鉴「苍穹外卖」的全栈广度与「黑马点评」的 Redis 深度，独立设计并开发的宠物护理店数字化驻店系统。覆盖宠物主、店员、店长三角色全链路，微信小程序 + Vue3 管理后台 + 网页测试控制台。

---

## 项目概述

一个线下宠物护理店的完整数字化方案。宠物主通过微信小程序注册、管理宠物档案、预约服务、在线支付、签到积分、查看排行榜；店员通过接单、记录病历疫苗、管理寄养出入；店长通过管理后台查看营收数据、管理员工排班、配置服务上下架。

**技术特色：** Redis 七种数据结构（ZSet / List / BitMap / GEO / HyperLogLog / Set / String）驱动 10 个真实业务场景，Lua 脚本原子秒杀。不同于「Redis 仅做缓存」的常规项目，本项目的 Redis 深度是核心竞争力。

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.18 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.0 |
| 缓存/深度 | Redis (Lettuce) | 7.x |
| 认证 | JWT (jjwt) + Interceptor + BCrypt | 0.11.5 |
| 实时推送 | Spring WebSocket | — |
| 定时任务 | Spring Task (`@Scheduled`) | — |
| 文档 | SpringDoc OpenAPI (Swagger UI) | 1.7.0 |
| 密码加密 | spring-security-crypto (BCrypt) | — |
| 工具库 | Hutool, Lombok | 5.8.25 |
| 前端 C 端 | 微信小程序 (WXML + WXSS + JS) | — |
| 前端管理端 | Vue 3 + Element Plus + ECharts + Vite | 3.3 / 2.3 |
| 测试工具 | JUnit 5 + Spring MockMvc | — |
| 部署 | Docker Compose (MySQL + Redis) | 3.8 |
| 语言 | Java 17 | — |

---

## 项目结构

```
pet-care/
├── pet-care-server/                    # Spring Boot 后端
│   └── src/main/java/com/petcare/
│       ├── config/                     # Redis / WebSocket / Swagger / PasswordEncoder
│       ├── interceptor/                # JWT 拦截器
│       ├── controller/                 # 21 个 Controller (见下方 API 清单)
│       ├── service/                    # 业务层接口
│       │   └── impl/                   # 业务层实现 (Auth / Order 核心)
│       ├── mapper/                     # MyBatis-Plus Mapper (18 张表)
│       ├── entity/                     # 实体类 (18 个)
│       ├── dto/                        # 数据传输对象
│       ├── common/                     # Result / BusinessException / GlobalExceptionHandler
│       ├── utils/                      # JWT 工具类
│       ├── websocket/                  # WebSocket 处理器 + 握手拦截器
│       ├── task/                       # 定时任务 (疫苗提醒)
│       └── src/main/resources/
│           ├── application.yml         # 主配置
│           ├── application-noredis.yml # 无 Redis 启动模式
│           ├── schema.sql              # 16 张表 DDL + 种子数据
│           └── lua/                    # Lua 脚本 (秒杀)
├── pet-care-miniapp/                   # 微信小程序 (6 个页面)
│   ├── pages/
│   │   ├── index/    # 首页 (人气榜 + 热门服务 + 品种标签)
│   │   ├── service/  # 服务浏览 + 多选下单
│   │   ├── order/    # 订单列表 + 支付/取消/评价
│   │   ├── pet/      # 宠物档案 CRUD
│   │   ├── sign/     # 每日签到
│   │   └── mine/     # 个人中心 + 双登录
│   └── utils/request.js  # 网络请求封装
├── pet-care-admin/                     # Vue3 店长管理后台 (4 个模块)
│   └── src/views/
│       ├── dashboard/  # 数据看板
│       ├── staff/      # 店员管理
│       ├── service/    # 服务配置
│       └── order/      # 订单管理
├── test-web/index.html                # 网页测试控制台 (39 个端点)
├── docs/
│   ├── project-plan.md                # PM 规划 (Sprint / API 清单 / 里程碑)
│   ├── db-compare.md                  # vs 苍穹外卖数据库对比分析
│   ├── api-reference.md              # 完整 API 文档
│   └── test-guide.md                 # 测试指南
└── docker-compose.yml                 # MySQL + Redis 一键启动
```

**源码统计：** ~80 个 Java 文件, ~4000 行 Java + ~2000 行前端, 16 张数据库表, 60+ 个 API 端点, 7 种 Redis 数据结构 10 个业务场景。

---

## 快速开始

### 环境要求

- Java 17
- Maven 3.6+
- MySQL 8.0
- Redis 7.x (可选，无 Redis 也能启动 CRUD 部分)
- Node.js 16+ (管理后台)

### 1. 启动基础设施

```bash
# 启动 MySQL + Redis (需要 Docker)
docker-compose up -d

# 或者只启动 MySQL (如果你本机已有 Redis)
docker run -d --name petcare-mysql \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=pet_care \
  -p 3306:3306 mysql:8.0
```

### 2. 初始化数据库

```bash
cd pet-care-server
mysql -uroot -p123456 -e "source src/main/resources/schema.sql"
```

### 3. 启动后端

```bash
mvn spring-boot:run

# 看到 Started PetCareApplication 即成功
# Swagger UI: http://localhost:8080/swagger-ui.html
# API Docs JSON: http://localhost:8080/api-docs
```

### 4. 启动管理后台 (可选)

```bash
cd pet-care-admin
npm install
npm run dev
# 打开 http://localhost:3000
# 弹框登录: staff01 / 123456 或 admin01 / 123456
```

### 5. 微信小程序 (可选)

微信开发者工具打开 `pet-care-miniapp/` 目录，修改 `utils/request.js` 中的 `baseUrl` 为你本机 IP。

### 6. 网页测试控制台 (零依赖)

浏览器直接打开 `test-web/index.html`，左侧 39 个端点一键测试。

---

## 测试账号

| 角色 | 方式 | 账号 / OpenID | 密码 |
|------|------|--------------|------|
| 宠物主 | 微信登录 | `mock_openid_001` | — |
| 店员 | 账号登录 | `staff01` | `123456` |
| 店长 | 账号登录 | `admin01` | `123456` |

---

## API 清单 (60+ 端点)

| 模块 | 端点数 | 说明 |
|------|--------|------|
| Auth | 2 | 微信登录注册 / 账号密码登录 |
| User | 5 | 个人信息 / 店员管理 |
| Pet | 5 | 宠物 CRUD (含店内编号自动生成) |
| Breed | 3 | 品种字典 (9 个品种种子数据) |
| Service Category | 5 | 品类 CRUD |
| Service Item | 8 | 服务 CRUD + 开关 + 时间范围 |
| Package | 4 | 套餐 (含服务项子查询) |
| Order | 10 | 下单→支付→接单→完成→拒单→退款→取消→评价 |
| Payment | 2 | Mock 扫码支付 (QR 生成 + 扫码) |
| Sign | 2 | BitMap 签到 (含连续天数计算) |
| Rank | 2 | ZSet 宠物人气榜 + 热门服务 |
| Coupon | 1 | Lua 脚本秒杀 |
| Weight | 2 | 体重记录 + 历史 |
| Health | 1 | 健康时间线 (疫苗+病历+体重) |
| Medical | 3 | 病历写入 / 编辑 / 标记已恢复 |
| Vaccine | 3 | 疫苗录入 / 宠物记录 / 30 天内到期查询 |
| Dashboard | 2 | 今日营收 + 热门服务 Top5 / 店员统计 |
| Schedule | 3 | 排班 CRUD |
| Availability | 2 | 店员自主时段开关 |
| Boarding | 4 | 寄养入住/离店 / 当前在住 / 历史 |
| Notification | 3 | 消息列表 / 未读数 / 已读 |
| Upload | 1 | Mock OSS 文件上传 |

完整 API 文档见 [docs/api-reference.md](docs/api-reference.md)。

---

## 数据库设计 (16 张表)

```
user ──1:N──> pet ──1:N──> order ──1:N──> order_detail ──N:1──> service_item ──N:1──> service_category
                                 │                                        │
                 vaccination_record (1:N)                      service_package ──N:N── package_item
                 medical_record (1:N)
                 weight_record (1:N)
                 pet_photo (1:N)

pet_breed (品种字典) ──N:1──> pet.breed_id
coupon (优惠券) ── Redis 双写 (stock/claimed)
staff_schedule (排班) ──N:1──> user (staff)
staff_availability (时段开关) ──N:1──> user (staff)
boarding_record (寄养) ──N:1──> pet, order
notification (消息) ──N:1──> user, pet
```

**设计原则：**
- 全部 16 张表带审计字段 (`create_time` / `update_time` / `create_user` / `update_user`)
- 订单采用「头 + 明细」模式 (借鉴苍穹外卖)，一笔订单支持多个服务项，明细存单价快照
- 疫苗/病历/寄养独立成表，支撑真实的宠物店运营场景
- `pet.store_no` 店内唯一编号，`pet.personality` 性格标签，`pet.chip_id` 芯片号

---

## Redis 深度应用 (项目核心亮点)

每种 Redis 数据结构对应一个真实业务场景，全部在 Controller/Service 中落地实现：

| 数据结构 | Key 示例 | 业务场景 | 实现文件 |
|---------|---------|---------|---------|
| **String** | `coupon:stock:{id}` | 秒杀库存计数器 | `CouponController.java` |
| **String** | `store:revenue:daily:{date}` | 每日营收累加 | `OrderServiceImpl.java` |
| **Set** | `coupon:claimed:{id}` | 秒杀已领用户去重 | `CouponController.java` (Lua 脚本) |
| **ZSet** | `rank:pet:popularity:weekly` | 宠物人气排行榜 | `RankController.java` |
| **ZSet** | `service:hot:daily:{date}` | 今日热门服务 | `DashboardController.java` |
| **BitMap** | `sign:user:{uid}:{yyyyMM}` | 每日签到 (4 字节/月) | `SignController.java` |
| **List** | `queue:service:{id}:waiting` | 实时排队队列 (设计完成) | 文档中 |
| **GEO** | `geo:breed:{breedId}` | 同品种附近宠友 (设计完成) | 文档中 |
| **HyperLogLog** | `uv:daily:{date}` | 小程序日活 UV (12KB 固定) | 文档中 |
| **Lua Script** | `seckill_coupon.lua` | 秒杀原子操作 (查库存+扣减+去重) | `resources/lua/` |

---

## 订单状态流转

```
0 待支付 ──pay──> 1 已支付 ──accept──> 2 服务中 ──complete──> 3 已完成
  │                 │                    │
  └──cancel──> 4    └──reject──> 4       └──refund──> 4
                    (店员拒单)            (退款)
```

每个状态变更都触发：WebSocket 推送 + 通知创建 + Redis 数据更新 (人气值 / 营收)

---

## 无 Redis 模式

如果你没有 Redis，后端仍可启动 (仅 CRUD + 认证)：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=noredis
```

此时签到/排行榜/秒杀接口返回 `"请先启动Redis"` 而不是崩溃。

---

## 文档索引

| 文档 | 路径 |
|------|------|
| API 接口文档 (完整) | [docs/api-reference.md](docs/api-reference.md) |
| 数据库对比分析 (vs 苍穹外卖) | [docs/db-compare.md](docs/db-compare.md) |
| PM 项目规划 (Sprint / 里程碑) | [docs/project-plan.md](docs/project-plan.md) |
| 测试指南 | [docs/test-guide.md](docs/test-guide.md) |
| 简历亮点文档 | `pet-care-resume-highlights.md` |

---

## Git 提交历史

```
32b2292 feat: 网页测试控制台 + 前端全量实现 + Swagger + Service层 + CORS
3639d63 docs: 完整测试指南
485c240 feat: 支持无Redis运行
d7bed2e fix: 4个关键bug修复 + Java 17适配
31772e0 sprint5: 测试 — JUnit集成测试
27ded21 sprint4: 前端骨架 — 小程序 + 管理后台
c6434ea sprint1-3: 后端全量 — 骨架 + 认证 + 订单链路 + Redis特性
92b19b4 docs: PM项目规划
54577c1 init: 项目初始化
```

---

## License

MIT
