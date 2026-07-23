# 智能电商运营平台 - 自动化测试 & Docker部署 & 全链路验收

## 一、自动化测试

### 1.1 测试架构

| 测试类型 | 位置 | 框架 | 说明 |
|---------|------|------|------|
| 单元测试 | `backend/*/src/test/java` | JUnit 5 + Mockito | 单服务内部逻辑测试 |
| 集成测试 | `backend/*/src/test/java` | Spring Boot Test | Controller API 测试 |
| 全链路验收 | `scripts/acceptance-test.py` | Python + requests | 端到端场景测试 |

### 1.2 运行单元测试

```bash
# 全部测试
mvn test

# 单个服务测试
mvn test -pl biz-service
mvn test -pl ai-service

# 查看测试报告
# target/surefire-reports/*.xml
```

### 1.3 测试用例清单

#### biz-service 测试用例

| 用例编号 | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
|---------|---------|---------|---------|---------|
| TC-PROD-001 | 查询商品详情-存在 | 商品ID=1已存在 | GET /biz/product/detail/1 | code=200, 返回商品信息 |
| TC-PROD-002 | 查询商品详情-不存在 | 商品ID=999不存在 | GET /biz/product/detail/999 | 返回null或空 |
| TC-PROD-003 | 创建商品-参数合法 | 已登录 | POST /biz/product/save | 创建成功, onSale=0 |
| TC-PROD-004 | 创建商品-名称为空 | 已登录 | POST /biz/product/save (name="") | 抛出参数异常 |
| TC-PROD-005 | 商品列表查询 | 数据库有商品 | GET /biz/product/list | 返回商品列表 |
| TC-PROD-006 | 商品上架 | 已登录+管理员 | PUT /biz/product/on-sale/1 | 上架成功 |
| TC-PROD-007 | 填充卖家名称-存在 | sellerId=5 | 查询商品详情 | sellerName="seller1" |
| TC-PROD-008 | 填充卖家名称-不存在 | sellerId=999 | 查询商品详情 | sellerName=null |
| TC-USER-001 | 用户注册 | 用户名不重复 | POST /biz/user/register | code=200, 注册成功 |
| TC-USER-002 | 用户登录 | 账号密码正确 | POST /biz/user/login | code=200, 返回token |
| TC-ORDER-001 | 创建订单 | 已登录+商品有库存 | POST /biz/order/create | code=200, 创建成功 |
| TC-ORDER-002 | 创建订单-库存不足 | 已登录+库存=0 | POST /biz/order/create | code=500, 库存不足 |

#### ai-service 测试用例

| 用例编号 | 用例名称 | 前置条件 | 操作步骤 | 预期结果 |
|---------|---------|---------|---------|---------|
| TC-SENT-001 | 情感分析-正面 | 无 | POST /ai/sentiment/analyze {"text":"非常满意"} | data=1 |
| TC-SENT-002 | 情感分析-负面 | 无 | POST /ai/sentiment/analyze {"text":"太差了"} | data=3 |
| TC-SENT-003 | 情感分析-中性 | 无 | POST /ai/sentiment/analyze {"text":"一般吧"} | data=2 |
| TC-SENT-004 | 情感分析-空文本 | 无 | POST /ai/sentiment/analyze {"text":""} | data=2 |
| TC-SENT-005 | 情感分析-混合 | 无 | POST /ai/sentiment/analyze | 负面词多返回3 |
| TC-KB-001 | 创建知识库 | 无 | POST /ai/knowledge/create | code=200, 返回kbId |
| TC-KB-002 | 查询知识库列表 | 有知识库 | GET /ai/knowledge/list | code=200, 返回列表 |
| TC-KB-003 | 删除知识库 | 知识库存在 | DELETE /ai/knowledge/{id} | code=200 |
| TC-CHAT-001 | AI对话 | 服务正常 | POST /ai/chat | code=200, 返回回答 |

## 二、Docker 容器化部署

### 2.1 前置条件

- Docker Engine 20.10+
- Docker Compose 2.0+
- 内存 >= 4GB

### 2.2 构建镜像

```bash
# 进入项目根目录
cd ecommerce-platform

# 编译后端（生成jar包）
mvn clean package -DskipTests

# 构建所有Docker镜像
docker-compose build

# 或者单个服务构建
docker build -t ecommerce-biz ./backend/biz-service
docker build -t ecommerce-ai ./backend/ai-service
docker build -t ecommerce-gateway ./gateway
docker build -t ecommerce-frontend ./frontend
```

### 2.3 启动服务

```bash
# 一键启动全部服务（后台运行）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f gateway
docker-compose logs -f biz-service
docker-compose logs -f ai-service
```

### 2.4 服务端口映射

| 服务 | 容器端口 | 宿主机端口 | 说明 |
|------|---------|-----------|------|
| MySQL | 3306 | 3306 | 数据库 |
| Nacos | 8848/9848 | 8848/9848 | 注册中心 |
| ChromaDB | 8000 | 8000 | 向量数据库 |
| Gateway | 8080 | 8080 | API网关 |
| Biz-Service | 8082 | 8082 | 业务服务 |
| AI-Service | 8081 | 8081 | AI服务 |
| Frontend | 80 | 3000 | 前端页面 |

### 2.5 停止服务

```bash
# 停止并删除容器
docker-compose down

# 停止并删除容器+数据卷（慎用）
docker-compose down -v
```

## 三、全链路验收

### 3.1 本地环境验收

确保所有服务已启动：
```bash
# 启动MySQL、Nacos、Chroma
# 启动biz-service (8082)
# 启动ai-service (8081)
# 启动gateway (8080)
# 启动frontend (3000)
```

运行验收脚本：
```bash
cd scripts
python acceptance-test.py

# 指定远程地址
python acceptance-test.py --host 192.168.1.100 --port 8080
```

### 3.2 Docker环境验收

```bash
# 启动Docker服务
docker-compose up -d

# 等待30秒让服务启动完成
sleep 30

# 运行验收
python scripts/acceptance-test.py --host localhost --port 8080
```

### 3.3 验收场景

脚本覆盖以下7个场景：

1. **服务健康检查** - 验证Gateway、Biz、AI服务是否可达
2. **用户登录链路** - 登录获取token
3. **商品查询链路** - 列表查询 + 详情查询（含卖家名称）
4. **订单创建链路** - 查询商品 → 创建订单
5. **AI智能客服链路** - 发送消息获取回答
6. **情感分析链路** - 正面/负面/中性情感识别
7. **知识库管理链路** - 创建 → 查询 → 删除

### 3.4 验收通过标准

- 全部用例通过（passed = total, failed = 0）
- 各服务响应时间 < 3秒
- 无HTTP 500错误
