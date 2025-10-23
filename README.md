# 微服务架构项目说明文档

## 1. 项目概述

本项目是一个基于 Dubbo 框架的微服务架构示例，包含多个相互协作的服务组件。项目实现了用户管理、订单处理、支付处理和数据分析等核心功能，展示了现代微服务架构的设计模式和调用关系。

## 2. 技术栈

- **框架**：Spring Boot, Apache Dubbo
- **注册中心**：ZooKeeper
- **服务追踪**：SkyWalking
- **构建工具**：Maven
- **开发语言**：Java

## 3. 项目结构

```
/root/test1/
├── demo-provider/        # 基础服务提供者
├── demo-webapp/          # Web应用层（服务消费者）
├── order-api/            # 订单服务接口定义
├── order-service/        # 订单服务实现
├── payment-api/          # 支付服务接口定义
├── payment-service/      # 支付服务实现
├── analytics-service/    # 数据分析服务（接口和实现）
└── package_and_run.sh    # 自动化部署脚本
```

## 4. 服务组件说明

### 4.1 Web应用层（demo-webapp）

- **主要功能**：提供RESTful API接口，处理HTTP请求
- **控制器**：HelloController
- **关键API端点**：
  - `/hello/{name}` - 基本问候接口
  - `/user/{id}` - 获取用户信息
  - `/user` - 创建用户（POST）
  - `/order` - 创建订单（POST）
  - `/payment` - 创建支付（POST）
  - `/analytics/user/{userId}` - 获取用户分析报告
  - `/analytics/system` - 获取系统分析数据
  - `/complex-flow/{name}` - 复杂业务流程演示
  - `/super-complex-flow/{userId}` - 超复杂调用链路演示

### 4.2 基础服务提供者（demo-provider）

- **主要功能**：提供基础服务，如用户管理
- **服务接口**：HelloService

### 4.3 订单服务（order-service）

- **主要功能**：处理订单的创建、查询、更新和取消
- **核心接口**：OrderService
- **主要方法**：
  - `createOrder` - 创建订单
  - `getOrderById` - 根据ID获取订单
  - `getUserOrders` - 获取用户的所有订单
  - `updateOrderStatus` - 更新订单状态
  - `cancelOrder` - 取消订单
  - `getOrderStatistics` - 获取订单统计信息

### 4.4 支付服务（payment-service）

- **主要功能**：处理支付相关操作，包括创建支付、支付回调处理和退款
- **核心接口**：PaymentService
- **主要方法**：
  - `createPayment` - 创建支付
  - `getPaymentStatus` - 获取支付状态
  - `handlePaymentCallback` - 处理支付回调
  - `refund` - 处理退款
  - `getUserPaymentHistory` - 获取用户支付历史
  - `validatePayment` - 验证支付

### 4.5 数据分析服务（analytics-service）

- **主要功能**：提供数据分析功能，包括用户分析、系统分析、销售趋势和支付方式分析
- **核心接口**：AnalyticsService
- **主要方法**：
  - `getUserAnalyticsReport` - 获取用户综合分析报告
  - `getSystemAnalyticsData` - 获取系统整体分析数据
  - `getSalesTrend` - 获取销售趋势分析
  - `getUserBehaviorAnalysis` - 获取用户行为分析
  - `getPaymentMethodAnalysis` - 获取支付方式分析

## 5. 服务调用关系

### 5.1 基本调用链路

- **Web应用层** → **基础服务提供者**：用户管理相关操作
- **Web应用层** → **订单服务**：订单管理相关操作
- **Web应用层** → **支付服务**：支付处理相关操作
- **Web应用层** → **数据分析服务**：数据分析相关操作

### 5.2 复杂调用链路

- **Web应用层** → **基础服务提供者** → **订单服务** → **支付服务**：用户下单流程
- **Web应用层** → **数据分析服务** → **基础服务提供者** + **订单服务** + **支付服务**：用户分析报告生成
- **超复杂流程**：用户创建 → 订单创建 → 支付处理 → 数据分析 → 异步订单创建 → 销售趋势分析

## 6. 配置说明

### 6.1 Dubbo配置

- **注册中心**：ZooKeeper（localhost:2181）
- **协议**：Dubbo（自动分配端口）
- **消费者配置**：超时30000ms，重试3次，延迟加载
- **提供者配置**：超时30000ms，不重试
- **扫描包**：com.example.demo.controller
- **线程池**：fixed类型，200线程

### 6.2 应用配置

- **Spring应用名称**：demo-webapp
- **服务端口**：8081
- **SkyWalking服务名称**：demo-webapp

## 7. 部署与运行指南

### 7.1 前提条件

- 安装JDK 8+
- 安装Maven
- 确保ZooKeeper服务在localhost:2181运行

### 7.2 自动化部署

使用提供的脚本进行一键部署：

```bash
chmod +x /root/test1/package_and_run.sh
cd /root/test1
./package_and_run.sh
```

### 7.3 手动部署步骤

1. **打包项目**：
   ```bash
   cd /root/test1
   mvn clean package -DskipTests
   ```

2. **启动服务提供者**：
   ```bash
   cd /root/test1/demo-provider/target
   java -jar demo-provider-1.0-SNAPSHOT.jar
   ```

3. **启动订单服务**：
   ```bash
   cd /root/test1/order-service/target
   java -jar order-service-1.0-SNAPSHOT.jar
   ```

4. **启动支付服务**：
   ```bash
   cd /root/test1/payment-service/target
   java -jar payment-service-1.0-SNAPSHOT.jar
   ```

5. **启动数据分析服务**：
   ```bash
   cd /root/test1/analytics-service/target
   java -jar analytics-service-1.0-SNAPSHOT.jar
   ```

6. **启动Web应用**：
   ```bash
   cd /root/test1/demo-webapp/target
   java -jar demo-webapp-1.0-SNAPSHOT.jar
   ```

### 7.4 停止服务

使用脚本停止所有服务：

```bash
./package_and_run.sh stop
```

## 8. API访问示例

服务启动后，可以通过以下命令测试API：

```bash
# 基本问候接口
curl http://localhost:8081/hello/world

# 获取用户信息
curl http://localhost:8081/user/1

# 复杂业务流程
curl http://localhost:8081/complex-flow/testuser

# 超复杂调用链路
curl http://localhost:8081/super-complex-flow/testuser

# 创建用户（POST请求）
curl -X POST -H "Content-Type: application/json" -d '{"name":"张三","email":"zhangsan@example.com"}' http://localhost:8081/user

# 创建订单（POST请求）
curl -X POST -H "Content-Type: application/json" -d '[{"productId":"P001","name":"测试商品","price":99.99,"quantity":2}]' "http://localhost:8081/order?userId=user_xxx"

# 创建支付（POST请求）
curl -X POST "http://localhost:8081/payment?orderId=ORDER_xxx&userId=user_xxx&amount=199.98&paymentMethod=Alipay"

# 获取用户分析报告
curl http://localhost:8081/analytics/user/user_xxx

# 获取系统分析数据
curl http://localhost:8081/analytics/system
```

**注意**：在实际使用时，请将上述命令中的`user_xxx`和`ORDER_xxx`替换为实际的用户ID和订单ID。

## 9. 注意事项

- 服务之间有依赖关系，启动顺序应遵循：服务提供者 → 订单服务 → 支付服务 → 数据分析服务 → Web应用
- 确保ZooKeeper服务正常运行，否则服务注册与发现会失败
- 所有服务的日志文件位于各服务的target目录下
- 如需启用SkyWalking链路追踪，请在启动命令中添加相应的javaagent配置

## 10. 故障排查

- **服务启动失败**：检查端口是否被占用，查看日志文件中的错误信息
- **服务调用失败**：检查Dubbo配置和ZooKeeper连接状态
- **数据不一致**：检查服务间的接口调用参数和返回值格式
- **性能问题**：查看服务日志，分析调用链路，优化关键性能瓶颈

预期返回结果：
```
Hello, USER! From Dubbo Provider
```

## 配置说明

### 服务端口配置
- 服务提供者（Dubbo）：20880
- Web应用：8081（已从默认8080修改，避免端口冲突）
- ZooKeeper注册中心：2181

### 关键配置文件

1. **Web应用端口配置** (`demo-webapp/src/main/resources/application.yml`)
   ```yaml
   server:
     port: 8081
   ```

2. **Dubbo注册中心配置**
   ```yaml
   dubbo:
     registry:
       address: zookeeper://localhost:2181
   ```

## 常见问题及解决方案

### 1. Java 9+ 兼容性问题

错误信息：
```
module java.base does not 'opens java.lang' to unnamed module
```

解决方案：
运行时添加以下JVM参数：
```
--add-opens java.base/java.lang=ALL-UNNAMED
```

### 2. 端口冲突

错误信息：
```
Port 8080 is already in use
```

解决方案：
1. 修改Web应用端口配置（已修改为8081）
2. 或停止占用端口的其他进程

### 3. ZooKeeper连接问题

错误信息：
```
Failed to connect to localhost:2181
```

解决方案：
1. 确保ZooKeeper服务已启动
2. 检查ZooKeeper配置和端口是否正确

### 4. 依赖问题

错误信息：
```
ClassNotFoundException: org.apache.curator.RetryPolicy
```

解决方案：
项目已包含必要的Curator依赖（curator-framework和curator-recipes），版本4.3.0

## 服务管理

### 停止服务

使用脚本输出中提供的PID停止服务：
```bash
kill -9 [PROVIDER_PID] [WEBAPP_PID]
```

或者使用以下命令停止指定服务：
```bash
pkill -f "demo-provider"
pkill -f "demo-webapp"
```

## 日志查看

- 服务提供者日志：`demo-provider/target/provider_jar.log`
- Web应用日志：`demo-webapp/target/webapp_jar.log`

可以使用以下命令查看日志：
```bash
tail -f demo-provider/target/provider_jar.log
tail -f demo-webapp/target/webapp_jar.log
```

## 注意事项

1. 项目使用ZooKeeper作为注册中心，必须确保ZooKeeper服务正常运行
2. JDK 9及以上版本需要添加特定JVM参数以解决模块系统限制
3. Web应用端口已修改为8081，请使用正确的端口访问
4. 打包运行脚本会自动停止之前运行的服务实例，避免端口冲突
5. 项目已配置链路追踪支持，可通过SkyWalking UI查看服务调用链路