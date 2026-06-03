# FreeWebNovel FrontEnd - Vue 3 前端

小说网站前端，基于 Vue 3 + Element Plus 构建。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite 7
- **UI 组件库**: Element Plus 2.11
- **状态管理**: Pinia 3
- **路由**: Vue Router 4
- **HTTP 请求**: Axios
- **工具**: OpenCC (繁简转换)、Prettier、ESLint
- **测试**: Vitest + Vue Test Utils

## 项目结构

```
FrontEnd/src/
├── api/               # API 接口封装
│   ├── AIChat.js      # KIMI AI 聊天
│   ├── Books.js       # 书籍相关
│   ├── Chapters.js    # 章节相关
│   ├── Users.js       # 用户相关
│   └── ...            # 其他接口
├── components/        # 公共组件
│   ├── layout/        # 布局组件（Header / Footer / Layout / AdminLayout）
│   ├── AIChat.vue     # AI 聊天组件
│   ├── CommentSection.vue
│   └── ...            # 错误边界、加载骨架等
├── views/             # 页面视图
│   ├── Admin/         # 管理后台页面
│   ├── Home.vue       # 首页
│   ├── BookDetail.vue # 书籍详情
│   ├── Read.vue       # 阅读器
│   ├── Login.vue      # 登录
│   ├── Register.vue   # 注册
│   ├── Search.vue     # 搜索
│   ├── Bookshelf.vue  # 书架
│   └── ...            # 排行、分类、用户中心等
├── stores/            # Pinia 状态
│   ├── auth.js        # 认证状态
│   ├── readingHistory.js # 阅读历史
│   └── ...
├── router/            # 路由配置
├── utils/             # 工具函数（请求封装、错误处理、图片处理）
└── data/              # 静态数据（首页推荐书籍等）
```

## 功能页面

| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | `/` | 分类导航、推荐书籍、排行榜 |
| 书籍详情 | `/book/:id` | 书籍信息、章节列表、评论 |
| 阅读器 | `/read/:bookId/:chapterId` | 章节阅读、弹幕、翻页 |
| 分类 | `/category` | 按分类浏览书籍 |
| 搜索 | `/search` | 关键词搜索 + AI 推荐 |
| 排行 | `/ranking` | 各类排行榜 |
| 书架 | `/bookshelf` | 收藏的书籍 |
| 阅读历史 | `/history` | 历史记录 |
| 用户中心 | `/user` | 个人信息、设置 |
| 管理后台 | `/admin/*` | 书籍/用户/评论/AI 管理等 |

## 管理后台功能

- **Dashboard**: 数据统计
- **书籍管理**: 书籍列表、爬虫控制、刷新
- **用户管理**: 用户列表、角色/状态管理
- **AI 管理**: 推荐算法参数配置
- **评论管理**: 评论审核
- **反馈/举报管理**: 用户反馈处理
- **通知管理**: 系统通知推送
- **心愿单管理**: 用户求书请求

## 启动方式

```bash
cd FrontEnd

# 安装依赖
npm install

# 开发模式（热重载）
npm run dev

# 生产构建
npm run build

# 预览构建结果
npm run preview
```

## 环境要求

- Node.js 20+
- 后端服务运行在 `http://localhost:8080`（可在 `utils/request.js` 中修改）
