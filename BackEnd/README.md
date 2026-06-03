# FreeWebNovel BackEnd - Spring Boot 后端

小说网站后端服务，提供完整的 RESTful API 接口。

## 技术栈

- **框架**: Spring Boot 3.5.5
- **ORM**: MyBatis-Plus 3.5.14（Mapper XML + 代码生成）
- **数据库**: MySQL + Druid 连接池
- **缓存**: Redis (Spring Cache + Lettuce)
- **鉴权**: JWT（Access Token + Refresh Token）+ Spring Security
- **AI**: KIMI (Moonshot) API
- **接口文档**: Knife4j (OpenAPI 3)
- **任务调度**: Quartz
- **搜索**: Elasticsearch（依赖已集成）
- **监控**: Spring Boot Actuator
- **验证码**: Kaptcha

## 项目结构

```
BackEnd/src/main/java/com/example/freefiction/
├── common/              # 通用枚举
├── config/              # 配置类（Redis / Security / Knife4j / Druid / KIMI 等）
├── controller/          # 控制器层
│   ├── sys/             # 系统管理控制器
│   ├── NovelController  # 小说爬虫/搜索接口
│   ├── UsersController  # 用户接口
│   └── CaptchaController# 验证码接口
├── domain/              # 登录/认证 DTO
├── entity/              # 实体类（MyBatis-Plus 注解）
├── filter/              # JWT 鉴权过滤器、请求限流
├── handler/             # 全局异常处理、统一响应、缓存策略
├── mapper/              # MyBatis Mapper 接口
├── service/             # 服务层
│   ├── sys/impl/        # 系统服务实现
│   ├── impl/            # 爬虫/解析服务实现
│   ├── AuthService      # 认证服务
│   ├── RedisService     # 缓存服务
│   ├── DeepSeekService  # AI 服务（已迁移至 KIMI）
│   └── ...
└── utils/               # 工具类（JWT / IP / 文件 / HTML解析 / 下载）
```

## 核心模块

### 小说爬虫
- 自动抓取小说信息（书名、作者、封面、简介等）
- 下载小说文本文件并分章解析
- 支持 IDM（Internet Download Manager）集成下载
- Redis 缓存爬取结果，避免重复请求

### 用户系统
- 注册/登录（BCrypt 加密）
- JWT 双 Token 鉴权（Access + Refresh）
- Token 黑名单机制（登出即失效）
- 头像上传、个人信息修改

### 互动系统
- 评论/回复
- 弹幕（实时互动）
- 评分/点赞
- 书架收藏
- 阅读记录追踪

### AI 智能推荐
- 基于 KIMI API 的书籍推荐
- 支持自然语言描述找书

### 管理后台 API
- 书籍管理（增删改查 + 爬虫控制）
- 用户管理（角色/状态/密码）
- 评论/反馈/举报审核
- 通知推送
- 数据统计看板

## 配置说明

主要配置文件：`src/main/resources/application.properties`

| 配置项 | 说明 |
|--------|------|
| `spring.datasource.druid.*` | 数据库连接（Druid） |
| `spring.data.redis.*` | Redis 缓存 |
| `jwt.*` | JWT 密钥/过期时间 |
| `kimi.api.*` | KIMI API 配置 |
| `fiction.*` | 小说爬虫相关配置 |
| `spring.mail.*` | QQ 邮件服务 |

## 启动方式

```bash
# 确保 MySQL 和 Redis 已启动
mvn clean install
mvn spring-boot:run
# 默认端口 8080
# API 文档: http://localhost:8080/doc.html
```

## 数据库

完整 SQL 在项目根目录 `freefiction.sql`，包含全部表结构和示例数据。
