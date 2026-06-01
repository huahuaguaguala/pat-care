# 爪爪护理 测试指南

## 前提

确保以下服务在运行：
- MySQL（root/root）
- Redis（localhost:6379，默认无密码）

## 步骤

### 1. 建库建表

```bash
cd pet-care
mysql -uroot -proot < pet-care-server/src/main/resources/schema.sql
```

### 2. 编译 + 启动

```bash
cd pet-care-server
mvn compile
```

如果编译报错，把错误贴给我。

编译通过后启动：

```bash
mvn spring-boot:run
```

看到 `Started PetCareApplication` 就说明启动成功。

### 3. 逐接口验证

用 Postman 或 curl，按顺序测：

**1) 查看服务品类（公开接口，无需登录）**

```bash
curl http://localhost:8080/api/service/category
```

预期返回 3 个品类（洗护美容、健康检查、寄养托管），code=200。

**2) 微信登录（宠物主）**

```bash
curl -X POST http://localhost:8080/api/auth/wechat-login \
  -H 'Content-Type: application/json' \
  -d '{"openid":"test_openid_999"}'
```

预期返回 token 和用户信息。记住这个 token，后面用。

**3) 账号密码登录（店员）**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"staff01","password":"123456"}'
```

预期 code=200，返回店员小李的信息。

**4) 添加宠物**

把第2步拿到的 token 替换进下面命令：

```bash
curl -X POST http://localhost:8080/api/pet \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <宠物主token>' \
  -d '{"name":"测试犬","breedId":1,"breedName":"柯基","age":3,"gender":0,"weight":14}'
```

**5) 我的宠物列表**

```bash
curl http://localhost:8080/api/pet/my \
  -H 'Authorization: Bearer <宠物主token>'
```

**6) 创建订单**

```bash
curl -X POST http://localhost:8080/api/order \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <宠物主token>' \
  -d '{"petId":3,"serviceId":1}'
```

记录返回的订单 id。

**7) 支付**

```bash
curl -X PUT http://localhost:8080/api/order/<订单id>/pay \
  -H 'Authorization: Bearer <宠物主token>'
```

**8) 店员接单**

```bash
curl -X PUT http://localhost:8080/api/order/<订单id>/accept \
  -H 'Authorization: Bearer <店员token>'
```

**9) 完成服务**

```bash
curl -X PUT http://localhost:8080/api/order/<订单id>/complete \
  -H 'Authorization: Bearer <店员token>'
```

**10) 评价**

```bash
curl -X POST "http://localhost:8080/api/order/<订单id>/review?rating=5&review=很满意" \
  -H 'Authorization: Bearer <宠物主token>'
```

**11) 签到**

```bash
# 签到
curl -X POST http://localhost:8080/api/sign/do \
  -H 'Authorization: Bearer <宠物主token>'

# 查看签到状态
curl http://localhost:8080/api/sign/status \
  -H 'Authorization: Bearer <宠物主token>'

# 再签一次，应该返回"今日已签到"
curl -X POST http://localhost:8080/api/sign/do \
  -H 'Authorization: Bearer <宠物主token>'
```

**12) 排行榜**

```bash
curl http://localhost:8080/api/rank/pet/weekly \
  -H 'Authorization: Bearer <宠物主token>'
```

**13) 秒杀**

```bash
curl -X POST http://localhost:8080/api/coupon/seckill/1 \
  -H 'Authorization: Bearer <宠物主token>'

# 再抢一次，应该返回"已领取"
curl -X POST http://localhost:8080/api/coupon/seckill/1 \
  -H 'Authorization: Bearer <宠物主token>'
```

**14) 未登录拦截**

```bash
curl http://localhost:8080/api/user/me
```

预期返回 401。

### 4. 运行 JUnit 测试

```bash
cd pet-care-server
mvn test
```

10 个测试用例，**预期全部通过**。如果某个失败，把控制台输出贴给我。

### 5. 快速自动化脚本（可选）

把下面保存为 `test.sh` 执行：

```bash
#!/bin/bash
BASE=http://localhost:8080
echo "=== 1. 公开接口 ==="
curl -s $BASE/api/service/category | head -c 100 && echo

echo "=== 2. 宠物主登录 ==="
RESP=$(curl -s -X POST $BASE/api/auth/wechat-login -H 'Content-Type: application/json' -d '{"openid":"test_'$(date +%s)'"}')
TOKEN=$(echo $RESP | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
echo "Token: ${TOKEN:0:30}..."

echo "=== 3. 店员登录 ==="
STAFF_RESP=$(curl -s -X POST $BASE/api/auth/login -H 'Content-Type: application/json' -d '{"username":"staff01","password":"123456"}')
STAFF_TOKEN=$(echo $STAFF_RESP | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
echo "Staff login: $(echo $STAFF_RESP | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['code'],d['data']['user']['nickname'] if d['code']==200 else '')")"

echo "=== 4. 添加宠物 ==="
curl -s -X POST $BASE/api/pet -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d '{"name":"测试","breedId":1,"breedName":"柯基","age":2,"gender":0,"weight":12}' | python3 -c "import sys,json; print('petId:', json.load(sys.stdin)['data']['id'])"

echo "=== 5. 签到 ==="
curl -s -X POST $BASE/api/sign/do -H "Authorization: Bearer $TOKEN" | python3 -c "import sys,json; print(json.load(sys.stdin))"

echo "=== 6. 重复签到 ==="
curl -s -X POST $BASE/api/sign/do -H "Authorization: Bearer $TOKEN" | python3 -c "import sys,json; print(json.load(sys.stdin))"

echo "=== 7. 秒杀 ==="
curl -s -X POST $BASE/api/coupon/seckill/1 -H "Authorization: Bearer $TOKEN" | python3 -c "import sys,json; print(json.load(sys.stdin))"

echo "=== 8. 重复秒杀 ==="
curl -s -X POST $BASE/api/coupon/seckill/1 -H "Authorization: Bearer $TOKEN" | python3 -c "import sys,json; print(json.load(sys.stdin))"

echo "=== 9. 未登录拦截 ==="
curl -s -o /dev/null -w "%{http_code}" $BASE/api/user/me && echo " --- 预期401"

echo "=== ALL DONE ==="
```
