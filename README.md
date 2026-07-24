# 易维保智能管理 App

中小型制造企业的设备维护管理 Android 应用，基于 PC 端"行健易维保智能管理系统 4.0"的核心功能实现移动端版本。当前为 **Demo 阶段**。

## 功能概览

| 模块 | 功能 |
|------|------|
| 设备台账 | 设备 CRUD、扫码查询、二维码生成 |
| 工单管理 | 创建 → 接单 → 维修 → 完成全生命周期 |
| 故障统计 | 故障概览、故障类型分布、设备故障排行 |
| 用户认证 | JWT 登录，按角色（管理员/维修员/操作员）区分权限 |

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.3.5 + Java 21 |
| 数据库 | MySQL 9.3 + Spring Data JPA |
| 安全 | Spring Security + JWT (jjwt 0.12.6) |
| 二维码 | ZXing 3.5.3 |
| Android | Kotlin 2.0 + Jetpack Compose + Material 3 |
| 网络 | Retrofit 2.11 + OkHttp 4.12 |
| 图片 | Coil 2.6 |
| 扫码 | CameraX + MLKit Barcode |

## 项目结构

```
yi/
├── backend/                         # Spring Boot REST API
│   ├── pom.xml
│   └── src/main/java/com/yiweibao/
│       ├── YiweibaoApplication.java
│       ├── entity/                  # JPA 实体
│       ├── dto/                     # 请求/响应 DTO
│       ├── repository/             # 数据访问层
│       ├── service/                # 业务逻辑层
│       ├── controller/             # REST 控制器
│       ├── security/               # JWT 工具 + 认证过滤器
│       └── config/                 # Security、CORS、数据初始化、异常处理
├── android/                         # Android 客户端
│   └── app/src/main/java/com/yiweibao/app/
│       ├── MainActivity.kt         # 单 Activity 入口
│       ├── YiweibaoApp.kt          # Application 初始化
│       ├── navigation/             # 路由：登录 → 主页(3 个底部导航)
│       ├── data/                   # API、模型、仓库
│       └── ui/                     # 各页面 + ViewModel
└── docs/                            # 项目文档
```

## 环境要求

### 后端

- **JDK 21** 或更高
- **Maven 3.8+**
- **MySQL 9.3**（兼容 8.0+）

### Android

- **Android Studio** (Hedgehog 或更新)
- **Android SDK 34**，最低支持 API 26 (Android 8.0)
- **Gradle 8.x** + Kotlin 2.0

## 部署指南

### 1. 数据库准备

启动 MySQL 服务，然后创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS yiweibao_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 2. 配置后端

编辑 `backend/src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yiweibao_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf-8
    username: root          # 改为你的 MySQL 用户名
    password: root12345     # 改为你的 MySQL 密码
```

关键配置项说明：

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `server.port` | `8080` | 后端服务端口 |
| `app.jwt.secret` | 内置演示密钥 | **生产环境必须修改** |
| `app.jwt.expiration-ms` | `86400000` (24h) | Token 有效期 |
| `app.upload.path` | `./uploads` | 文件上传目录 |
| `spring.servlet.multipart.max-file-size` | `20MB` | 上传文件大小限制 |

### 3. 启动后端

```bash
cd backend

# 编译
mvn compile

# 启动（首次启动自动建表并写入演示数据）
mvn spring-boot:run

# 或打包运行
mvn package -DskipTests
java -jar target/yiweibao-backend-1.0.0-SNAPSHOT.jar
```

服务启动后访问 `http://localhost:8080`。

首次启动时，若 `users` 表为空，会自动写入演示数据：
- 18 台设备、4 个用户、11 条工单记录

演示账号：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `admin` | `123456` | 管理员 |
| `engineer1` | `123456` | 维修员 |
| `operator1` | `123456` | 操作员 |

### 4. 运行 Android 客户端

1. 用 Android Studio 打开 `android/` 目录
2. 等待 Gradle 同步完成
3. 选择模拟器（API 26+）或 USB 连接的设备
4. 点击 Run

默认连接地址为 `http://10.0.2.2:8080/`（Android 模拟器访问宿主机的 localhost）。

如果后端部署在远端服务器，修改 `android/app/build.gradle.kts` 中的 `BASE_URL`：

```kotlin
buildConfigField("String", "BASE_URL", "\"http://你的服务器IP:8080/\"")
```

## API 概览

所有接口返回统一格式 `{ "code": 200, "message": "success", "data": {...} }`。

认证接口 `/api/auth/**` 公开访问，其余接口需携带 `Authorization: Bearer <token>`。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录，返回 JWT |
| GET | `/api/equipment?keyword=&page=&size=` | 设备列表（支持搜索） |
| GET/POST/PUT | `/api/equipment[/{id}]` | 设备增删改查 |
| GET | `/api/work-orders?status=&page=&size=` | 工单列表 |
| POST | `/api/work-orders` | 创建工单 |
| PUT | `/api/work-orders/{id}/accept` | 接单 |
| PUT | `/api/work-orders/{id}/complete` | 完成维修 |
| POST | `/api/upload` | 上传图片 |
| GET | `/api/statistics/overview` | 故障概览统计 |
| GET | `/api/statistics/fault-types` | 故障类型分布 |
| GET | `/api/statistics/top-equipment?limit=5` | 设备故障排行 |

## 注意事项

- 本项目为 Demo 阶段，所有数据为模拟数据
- 文件上传存储在 `uploads/` 目录（相对于后端工作目录）
- 仅实现了 Layer 1 功能（设备管理 + 工单 + 统计），AI 诊断、IoT 传感器、知识图谱等为扩展功能
- 无离线模式，客户端需要网络连接后端
- 生产部署前务必修改 `application.yml` 中的 JWT 密钥和数据库密码
