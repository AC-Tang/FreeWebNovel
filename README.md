# FreeWebNovel - 白嫖小说网

一个功能完整的全栈小说网站，支持小说爬取、在线阅读、用户管理、AI 智能推荐等功能。

## 项目结构

```
FreeWebNovel/
├── BackEnd/          # Spring Boot 后端
│   ├── src/          # Java 源码
│   └── pom.xml       # Maven 配置
├── FrontEnd/         # Vue 3 前端
│   ├── src/          # Vue 源码
│   └── package.json  # 前端依赖
└── docs/             # 项目文档（本地）
```

## 快速开始

### 环境要求

- Java 17+
- Node.js 20+
- MySQL 8.0+
- Redis
- Maven

### 启动后端

```bash
cd BackEnd
# 修改 application.properties 中的数据库和 Redis 配置
mvn clean install
mvn spring-boot:run
```

### 启动前端

```bash
cd FrontEnd
npm install
npm run dev
```

### 数据库

项目根目录的 `freefiction.sql` 包含完整数据库结构和示例数据，直接导入即可。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.5.5 |
| ORM | MyBatis-Plus |
| 数据库 | MySQL + Druid 连接池 |
| 缓存 | Redis |
| 鉴权 | JWT（双 Token）+ Spring Security |
| 接口文档 | Knife4j (Swagger) |
| AI | KIMI (Moonshot) API |
| 前端框架 | Vue 3 + Vite |
| UI 组件 | Element Plus |
| 状态管理 | Pinia |

## 核心功能

- **小说管理**：爬虫自动抓取、分类管理、全文搜索
- **在线阅读**：章节分页、阅读记录、书架收藏
- **用户系统**：注册登录、JWT 鉴权、个人信息管理
- **互动功能**：评论、弹幕、评分、点赞
- **AI 推荐**：基于 KIMI API 的智能书籍推荐
- **管理后台**：书籍/用户/评论/通知/反馈/举报管理
- **辅助功能**：签到、排行榜、阅读历史、繁简转换
