/*
 Navicat Premium Data Transfer

 Source Server         : MySqlHome
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : freefiction

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 09/01/2026 21:43:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for book_requests
-- ----------------------------
DROP TABLE IF EXISTS `book_requests`;
CREATE TABLE `book_requests`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '请求ID',
  `user_id` bigint NOT NULL COMMENT '请求用户ID',
  `book_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求书名',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作者名',
  `publisher` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '出版社',
  `isbn` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ISBN号',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '书籍描述',
  `request_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求来源',
  `book_id` bigint NULL DEFAULT NULL COMMENT '关联的书籍ID(处理后添加)',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-待处理, 2-已添加, 3-已拒绝',
  `admin_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '管理员备注',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
  `processed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '处理时间',
  `admin_id` bigint NULL DEFAULT NULL COMMENT '处理管理员ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_status_created`(`status` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `book_requests_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `book_requests_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '书籍请求表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for books
-- ----------------------------
DROP TABLE IF EXISTS `books`;
CREATE TABLE `books`  (
  `book_id` bigint NOT NULL COMMENT '书籍ID(用于关联)',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '书名',
  `subtitle` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '副标题',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作者名',
  `category_id` int NOT NULL COMMENT '分类ID',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '书籍简介',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-下架, 1-连载中, 2-已完结',
  `word_count` int NULL DEFAULT 0 COMMENT '总字数',
  `chapter_count` int NULL DEFAULT 0 COMMENT '章节数',
  `view_count` int NULL DEFAULT 0 COMMENT '点击数',
  `recommend_count` int NULL DEFAULT 0 COMMENT '推荐数',
  `like_count` bigint NULL DEFAULT NULL COMMENT '收藏数',
  `rating_avg` double(3, 2) NULL DEFAULT 0.00 COMMENT '平均评分',
  `rating_count` int NULL DEFAULT 0 COMMENT '评分人数',
  `last_chapter_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最新章节标题',
  `last_chapter_time` datetime NULL DEFAULT NULL COMMENT '最新章节更新时间',
  `publish_date` datetime NULL DEFAULT NULL COMMENT '书籍发布时间(开始更新时间)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(管理员导入时间)',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `recommended` tinyint NULL DEFAULT 0 COMMENT '是否被推荐到主页',
  `carousel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '轮播图路径',
  PRIMARY KEY (`book_id` DESC) USING BTREE,
  INDEX `idx_title`(`title` ASC) USING BTREE,
  INDEX `idx_author`(`author` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_view_count`(`view_count` ASC) USING BTREE,
  INDEX `idx_recommend_count`(`recommend_count` ASC) USING BTREE,
  INDEX `idx_rating_avg`(`rating_avg` ASC) USING BTREE,
  INDEX `idx_last_chapter_time`(`last_chapter_time` ASC) USING BTREE,
  INDEX `idx_publish_date`(`publish_date` ASC) USING BTREE,
  INDEX `idx_category_status_created`(`category_id` ASC, `status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_status_view_rating`(`status` ASC, `view_count` ASC, `rating_avg` ASC) USING BTREE,
  FULLTEXT INDEX `idx_title_fulltext`(`title`),
  CONSTRAINT `books_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '书籍表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bookshelves
-- ----------------------------
DROP TABLE IF EXISTS `bookshelves`;
CREATE TABLE `bookshelves`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '书架ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父分类ID(NULL表示一级分类)',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '书架/分类名称',
  `book_id` bigint NULL DEFAULT NULL COMMENT '书籍ID(分类时为NULL)',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述(仅分类使用)',
  `last_read_chapter_id` bigint NULL DEFAULT NULL COMMENT '最后阅读章节ID(仅书籍使用)',
  `last_read_time` datetime NULL DEFAULT NULL COMMENT '最后阅读时间(仅书籍使用)',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `item_type` tinyint NOT NULL COMMENT '项目类型: 1-分类, 2-书籍',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_book`(`user_id` ASC, `book_id` ASC) USING BTREE,
  INDEX `last_read_chapter_id`(`last_read_chapter_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_item_type`(`item_type` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_user_parent_type_order`(`user_id` ASC, `parent_id` ASC, `item_type` ASC, `sort_order` ASC) USING BTREE,
  INDEX `idx_user_book_order`(`user_id` ASC, `book_id` ASC) USING BTREE,
  CONSTRAINT `bookshelves_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `bookshelves_ibfk_3` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `bookshelves_ibfk_4` FOREIGN KEY (`last_read_chapter_id`) REFERENCES `chapters` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `bookshelves_ibfk_5` FOREIGN KEY (`parent_id`) REFERENCES `bookshelves` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '书架表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类图标',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for chapters
-- ----------------------------
DROP TABLE IF EXISTS `chapters`;
CREATE TABLE `chapters`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '章节ID',
  `book_id` bigint NOT NULL COMMENT '书籍ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '章节标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '章节内容',
  `word_count` int NULL DEFAULT 0 COMMENT '字数',
  `sort_order` bigint NOT NULL COMMENT '章节顺序',
  `view_count` int NULL DEFAULT 0 COMMENT '阅读量',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_book_sort`(`book_id` ASC, `sort_order` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_book_sort`(`book_id` ASC, `sort_order` ASC) USING BTREE,
  CONSTRAINT `chapters_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 133237 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '章节表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for check_ins
-- ----------------------------
DROP TABLE IF EXISTS `check_ins`;
CREATE TABLE `check_ins`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '打卡ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `book_id` bigint NOT NULL COMMENT '书籍ID',
  `chapter_id` bigint NOT NULL COMMENT '章节ID',
  `ranks` bigint NULL DEFAULT 0 COMMENT '打卡排名',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-隐藏, 1-正常',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_chapter`(`user_id` ASC, `chapter_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_chapter_created`(`chapter_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_book_created`(`book_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `check_ins_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `check_ins_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `check_ins_ibfk_3` FOREIGN KEY (`chapter_id`) REFERENCES `chapters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '打卡表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `book_id` bigint NOT NULL COMMENT '书籍ID',
  `chapter_id` bigint NULL DEFAULT NULL COMMENT '章节ID(书评时为NULL)',
  `paragraph_index` int NULL DEFAULT NULL COMMENT '段落索引(段评时使用，章评和书评时为NULL)',
  `comment_type` tinyint NOT NULL COMMENT '评论类型: 1-章评, 2-段评, 3-书评',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID(回复评论时使用)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `reading_time` int NULL DEFAULT 0 COMMENT '阅读时长(分钟，仅书评使用)',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `reply_count` int NULL DEFAULT 0 COMMENT '回复数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-隐藏, 1-正常',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `top_parent_id` int NULL DEFAULT NULL COMMENT '大于三级评论的祖宗评论ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id` ASC) USING BTREE,
  INDEX `idx_comment_type`(`comment_type` ASC) USING BTREE,
  INDEX `idx_paragraph_index`(`paragraph_index` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_book_type_created`(`book_id` ASC, `comment_type` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_chapter_type_created`(`chapter_id` ASC, `comment_type` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_user_type_created`(`user_id` ASC, `comment_type` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_parent_created`(`parent_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comments_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comments_ibfk_3` FOREIGN KEY (`chapter_id`) REFERENCES `chapters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comments_ibfk_4` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for danmaku
-- ----------------------------
DROP TABLE IF EXISTS `danmaku`;
CREATE TABLE `danmaku`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '弹幕ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `book_id` bigint NOT NULL COMMENT '书籍ID',
  `chapter_id` bigint NOT NULL COMMENT '章节ID',
  `content` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '弹幕内容',
  `color` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#FFFFFF' COMMENT '弹幕颜色',
  `font_size` tinyint NULL DEFAULT 16 COMMENT '字体大小',
  `speed` tinyint NULL DEFAULT 5 COMMENT '弹幕速度',
  `type` tinyint NULL DEFAULT 1 COMMENT '弹幕类型: 1-滚动, 2-顶部, 3-底部',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-隐藏, 1-正常',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_chapter_created`(`chapter_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_chapter_position`(`chapter_id` ASC) USING BTREE,
  CONSTRAINT `danmaku_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `danmaku_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `danmaku_ibfk_3` FOREIGN KEY (`chapter_id`) REFERENCES `chapters` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '弹幕表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for feedbacks
-- ----------------------------
DROP TABLE IF EXISTS `feedbacks`;
CREATE TABLE `feedbacks`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '反馈用户ID(可为空，支持匿名反馈)',
  `feedback_type` tinyint NOT NULL COMMENT '反馈类型：1-功能建议，2-问题反馈，3-内容投诉，4-其他',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈内容',
  `contact_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系方式(邮箱/手机等)',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-待处理，2-处理中，3-已处理，4-已关闭',
  `admin_id` bigint NULL DEFAULT NULL COMMENT '处理管理员ID',
  `admin_reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '管理员回复',
  `processed_at` timestamp NULL DEFAULT NULL COMMENT '处理时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_type`(`feedback_type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_status_created`(`status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_type_status`(`feedback_type` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `feedbacks_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `feedbacks_ibfk_2` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '反馈表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for likes
-- ----------------------------
DROP TABLE IF EXISTS `likes`;
CREATE TABLE `likes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_type` tinyint NOT NULL COMMENT '目标类型: 1-评论, 2-打卡',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target_type`(`target_type` ASC) USING BTREE,
  INDEX `idx_target_id`(`target_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `likes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点赞表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '接收通知的用户ID',
  `type` tinyint NOT NULL COMMENT '通知类型：1-评论回复，2-收到点赞，3-书籍更新，4-请求处理，5-系统通知',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知内容',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID：评论ID、书籍ID或请求ID等',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联类型：comment、book、request等表的名字，如果是系统消息就是对应的字符串',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_user_read`(`user_id` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_type_created`(`type` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_read_created`(`is_read` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for rankings
-- ----------------------------
DROP TABLE IF EXISTS `rankings`;
CREATE TABLE `rankings`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '排行榜ID',
  `book_id` bigint NOT NULL COMMENT '书籍ID',
  `ranking_type` tinyint NOT NULL COMMENT '排行榜类型: 1-人气榜, 2-新书榜, 3-完结榜, 4-收藏榜',
  `rank_value` int NOT NULL COMMENT '排行值(阅读量/推荐数等)',
  `rank_position` int NOT NULL COMMENT '排名位置',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type_date_book`(`ranking_type` ASC, `stat_date` ASC, `book_id` ASC) USING BTREE,
  INDEX `book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_ranking_type`(`ranking_type` ASC) USING BTREE,
  INDEX `idx_stat_date`(`stat_date` ASC) USING BTREE,
  INDEX `idx_rank_position`(`rank_position` ASC) USING BTREE,
  INDEX `idx_type_book`(`ranking_type` ASC, `book_id` ASC) USING BTREE,
  INDEX `idx_type_rank`(`ranking_type` ASC, `rank_position` ASC) USING BTREE,
  CONSTRAINT `rankings_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '排行榜表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ratings
-- ----------------------------
DROP TABLE IF EXISTS `ratings`;
CREATE TABLE `ratings`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评分ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `book_id` bigint NOT NULL COMMENT '书籍ID',
  `score` tinyint NOT NULL COMMENT '评分(1-5分)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_book`(`user_id` ASC, `book_id` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_score`(`score` ASC) USING BTREE,
  INDEX `idx_book_score`(`book_id` ASC, `score` ASC) USING BTREE,
  INDEX `idx_user_created`(`user_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `ratings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `ratings_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评分表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for reading_records
-- ----------------------------
DROP TABLE IF EXISTS `reading_records`;
CREATE TABLE `reading_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `book_id` bigint NOT NULL COMMENT '书籍ID',
  `chapter_id` bigint NOT NULL COMMENT '章节ID',
  `read_position` int NULL DEFAULT 0 COMMENT '阅读位置(字符偏移量)',
  `read_percentage` double(5, 2) NULL DEFAULT 0.00 COMMENT '阅读进度百分比',
  `read_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id` ASC) USING BTREE,
  INDEX `idx_read_time`(`read_time` ASC) USING BTREE,
  INDEX `idx_user_book`(`user_id` ASC, `book_id` ASC) USING BTREE,
  INDEX `idx_user_read_time`(`user_id` ASC, `read_time` ASC) USING BTREE,
  CONSTRAINT `reading_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `reading_records_ibfk_2` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '阅读记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for recommendations
-- ----------------------------
DROP TABLE IF EXISTS `recommendations`;
CREATE TABLE `recommendations`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '推荐ID',
  `book_id` bigint NOT NULL COMMENT '被推荐书籍ID',
  `recommend_type` tinyint NOT NULL COMMENT '推荐类型: 1-首页推荐, 2-分类推荐, 3-相关推荐',
  `recommend_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐理由',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `start_time` datetime NULL DEFAULT NULL COMMENT '推荐开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '推荐结束时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `book_id`(`book_id` ASC) USING BTREE,
  INDEX `idx_recommend_type`(`recommend_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_type_status_order`(`recommend_type` ASC, `status` ASC, `sort_order` ASC) USING BTREE,
  CONSTRAINT `recommendations_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '推荐表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for reports
-- ----------------------------
DROP TABLE IF EXISTS `reports`;
CREATE TABLE `reports`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` bigint NULL DEFAULT NULL COMMENT '举报人ID',
  `target_type` tinyint NOT NULL COMMENT '举报对象类型：1-书籍，2-评论，3-用户',
  `target_id` bigint NOT NULL COMMENT '举报对象ID',
  `reason_type` tinyint NOT NULL COMMENT '举报原因类型：1-内容违规，2-垃圾信息，3-版权侵犯，4-恶意攻击，5-其他',
  `reason_detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '举报原因详细描述',
  `evidence_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '证据截图，JSON格式存储图片URL列表',
  `is_anonymous` tinyint(1) NULL DEFAULT 0 COMMENT '是否匿名举报',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-待处理，2-处理中，3-已处理，4-已驳回',
  `admin_id` bigint NULL DEFAULT NULL COMMENT '处理管理员ID',
  `admin_note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管理员处理备注',
  `processed_at` timestamp NULL DEFAULT NULL COMMENT '处理时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target_date`(`reporter_id` ASC, `target_type` ASC, `target_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_reporter_id`(`reporter_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_reporter_target`(`reporter_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_target_status`(`target_type` ASC, `target_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_status_created`(`status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_reason_status`(`reason_type` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `reports_ibfk_1` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `reports_ibfk_2` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '举报表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for search_logs
-- ----------------------------
DROP TABLE IF EXISTS `search_logs`;
CREATE TABLE `search_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '搜索记录ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID(游客为NULL)',
  `keyword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '搜索关键词',
  `search_type` tinyint NULL DEFAULT 1 COMMENT '搜索类型: 1-书籍, 2-作者, 3-综合',
  `result_count` int NULL DEFAULT 0 COMMENT '搜索结果数量',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `is_delete` tinyint NULL DEFAULT NULL COMMENT '是否删除：0-删除，1-用户可见',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_keyword`(`keyword` ASC) USING BTREE,
  INDEX `idx_search_type`(`search_type` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_user_created`(`user_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_type_created`(`search_type` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `search_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '搜索记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for system_configs
-- ----------------------------
DROP TABLE IF EXISTS `system_configs`;
CREATE TABLE `system_configs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '配置值',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置描述',
  `config_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'string' COMMENT '配置类型: string, number, boolean, json',
  `is_public` tinyint(1) NULL DEFAULT 0 COMMENT '是否公开(前端可访问)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE,
  INDEX `idx_config_type`(`config_type` ASC) USING BTREE,
  INDEX `idx_is_public`(`is_public` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_preferences
-- ----------------------------
DROP TABLE IF EXISTS `user_preferences`;
CREATE TABLE `user_preferences`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '偏好设置ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `theme` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'light' COMMENT '阅读主题：light-亮色, dark-暗色, sepia-护眼',
  `font_family` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'system-ui' COMMENT '字体族',
  `font_size` int NULL DEFAULT 16 COMMENT '字体大小(像素)',
  `line_height` decimal(3, 2) NULL DEFAULT 1.60 COMMENT '行高',
  `page_width` int NULL DEFAULT 800 COMMENT '页面宽度(像素)',
  `background_color` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#FFFFFF' COMMENT '背景颜色',
  `text_color` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#333333' COMMENT '文字颜色',
  `auto_bookmark` tinyint(1) NULL DEFAULT 1 COMMENT '自动添加书签',
  `auto_scroll` tinyint(1) NULL DEFAULT 0 COMMENT '自动滚动',
  `scroll_speed` int NULL DEFAULT 100 COMMENT '滚动速度(毫秒)',
  `night_mode` tinyint(1) NULL DEFAULT 0 COMMENT '夜间模式',
  `brightness` int NULL DEFAULT 100 COMMENT '屏幕亮度(0-100)',
  `reading_mode` tinyint NULL DEFAULT 0 COMMENT '阅读模式：0-普通，1-沉浸式，2-护眼',
  `page_turn_animation` tinyint NULL DEFAULT 1 COMMENT '翻页动画：0-无，1-滑动，2-淡入淡出',
  `show_progress` tinyint(1) NULL DEFAULT 1 COMMENT '显示阅读进度',
  `show_time` tinyint(1) NULL DEFAULT 1 COMMENT '显示阅读时间',
  `show_battery` tinyint(1) NULL DEFAULT 0 COMMENT '显示电量',
  `auto_preload` tinyint(1) NULL DEFAULT 1 COMMENT '自动预加载下一章',
  `preload_chapters` int NULL DEFAULT 1 COMMENT '预加载章节数量',
  `preferred_categories` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '偏好分类ID，逗号分隔',
  `preferred_authors` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '偏好作者，逗号分隔',
  `notification_settings` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '通知设置，JSON格式',
  `privacy_settings` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '隐私设置，JSON格式',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_theme`(`theme` ASC) USING BTREE,
  INDEX `idx_reading_mode`(`reading_mode` ASC) USING BTREE,
  CONSTRAINT `user_preferences_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户偏好表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码哈希',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色',
  `avatar` mediumblob NULL COMMENT '头像二进制',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `signature` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '个性签名',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_status_created`(`status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- View structure for v_book_details
-- ----------------------------
DROP VIEW IF EXISTS `v_book_details`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_book_details` AS select `b`.`book_id` AS `id`,`b`.`title` AS `title`,`b`.`subtitle` AS `subtitle`,`b`.`author` AS `author`,`b`.`category_id` AS `category_id`,`c`.`name` AS `category_name`,`b`.`cover` AS `cover`,`b`.`description` AS `description`,`b`.`status` AS `status`,`b`.`word_count` AS `word_count`,`b`.`chapter_count` AS `chapter_count`,`b`.`view_count` AS `view_count`,`b`.`recommend_count` AS `recommend_count`,`b`.`rating_avg` AS `rating_avg`,`b`.`rating_count` AS `rating_count`,`b`.`last_chapter_title` AS `last_chapter_title`,`b`.`last_chapter_time` AS `last_chapter_time`,`b`.`publish_date` AS `publish_date`,`b`.`created_at` AS `created_at`,`b`.`updated_at` AS `updated_at` from (`books` `b` left join `categories` `c` on((`b`.`category_id` = `c`.`id`)));

-- ----------------------------
-- View structure for v_book_stats
-- ----------------------------
DROP VIEW IF EXISTS `v_book_stats`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_book_stats` AS select `b`.`book_id` AS `book_id`,`b`.`title` AS `title`,`b`.`author` AS `author`,`c`.`name` AS `category_name`,`b`.`view_count` AS `view_count`,`b`.`recommend_count` AS `recommend_count`,`b`.`rating_avg` AS `rating_avg`,`b`.`rating_count` AS `rating_count`,count(distinct `rr`.`user_id`) AS `readers_count`,count(distinct `co`.`id`) AS `comments_count`,count(distinct `ch`.`id`) AS `chapters_count`,`b`.`word_count` AS `word_count`,`b`.`status` AS `status`,`b`.`created_at` AS `created_at` from ((((`books` `b` left join `categories` `c` on((`b`.`category_id` = `c`.`id`))) left join `reading_records` `rr` on((`b`.`book_id` = `rr`.`book_id`))) left join `comments` `co` on((`b`.`book_id` = `co`.`book_id`))) left join `chapters` `ch` on((`b`.`book_id` = `ch`.`book_id`))) group by `b`.`book_id`,`b`.`title`,`b`.`author`,`c`.`name`,`b`.`view_count`,`b`.`recommend_count`,`b`.`rating_avg`,`b`.`rating_count`,`b`.`word_count`,`b`.`status`,`b`.`created_at`;

-- ----------------------------
-- View structure for v_user_bookshelf
-- ----------------------------
DROP VIEW IF EXISTS `v_user_bookshelf`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_user_bookshelf` AS select `bs`.`id` AS `id`,`bs`.`user_id` AS `user_id`,`bs`.`parent_id` AS `parent_id`,`bs`.`name` AS `name`,`bs`.`book_id` AS `book_id`,`bs`.`description` AS `description`,`bs`.`last_read_chapter_id` AS `last_read_chapter_id`,`bs`.`last_read_time` AS `last_read_time`,`bs`.`sort_order` AS `sort_order`,`bs`.`item_type` AS `item_type`,`bs`.`created_at` AS `created_at`,`bs`.`updated_at` AS `updated_at`,`b`.`title` AS `book_title`,`b`.`author` AS `book_author`,`b`.`cover` AS `book_cover`,`b`.`status` AS `book_status`,`b`.`last_chapter_title` AS `last_chapter_title`,`b`.`last_chapter_time` AS `last_chapter_time` from (`bookshelves` `bs` left join `books` `b` on((`bs`.`book_id` = `b`.`book_id`))) where (`bs`.`item_type` = 2);

-- ----------------------------
-- View structure for v_user_reading_stats
-- ----------------------------
DROP VIEW IF EXISTS `v_user_reading_stats`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_user_reading_stats` AS select `u`.`id` AS `user_id`,`u`.`username` AS `username`,count(distinct `rr`.`book_id`) AS `books_read`,count(`rr`.`id`) AS `total_reading_records`,max(`rr`.`read_time`) AS `last_read_time`,sum((case when (`r`.`score` is not null) then 1 else 0 end)) AS `books_rated`,avg(`r`.`score`) AS `avg_rating_given` from ((`users` `u` left join `reading_records` `rr` on((`u`.`id` = `rr`.`user_id`))) left join `ratings` `r` on(((`u`.`id` = `r`.`user_id`) and (`rr`.`book_id` = `r`.`book_id`)))) group by `u`.`id`,`u`.`username`;

-- ----------------------------
-- Procedure structure for AddReadingRecord
-- ----------------------------
DROP PROCEDURE IF EXISTS `AddReadingRecord`;
delimiter ;;
CREATE PROCEDURE `AddReadingRecord`(IN userId BIGINT,
    IN bookId BIGINT,
    IN chapterId BIGINT,
    IN readPosition INT,
    IN readPercentage DECIMAL(5,2))
BEGIN
    -- 检查是否已有该章节的阅读记录
    DECLARE recordExists INT;
    
    SELECT COUNT(*) INTO recordExists
    FROM reading_records
    WHERE user_id = userId AND book_id = bookId AND chapter_id = chapterId;
    
    IF recordExists > 0 THEN
        -- 更新现有记录
        UPDATE reading_records
        SET read_position = readPosition,
            read_percentage = readPercentage,
            read_time = NOW()
        WHERE user_id = userId AND book_id = bookId AND chapter_id = chapterId;
    ELSE
        -- 插入新记录
        INSERT INTO reading_records (user_id, book_id, chapter_id, read_position, read_percentage, read_time)
        VALUES (userId, bookId, chapterId, readPosition, readPercentage, NOW());
    END IF;
    
    -- 更新书架记录
    UPDATE bookshelves
    SET last_read_chapter_id = chapterId,
        last_read_time = NOW()
    WHERE user_id = userId AND book_id = bookId AND item_type = 2;
    
    -- 更新章节阅读量
    UPDATE chapters
    SET view_count = view_count + 1
    WHERE id = chapterId;
    
    -- 更新书籍点击量
    UPDATE books
    SET view_count = view_count + 1
    WHERE id = bookId;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for UpdateBookStats
-- ----------------------------
DROP PROCEDURE IF EXISTS `UpdateBookStats`;
delimiter ;;
CREATE PROCEDURE `UpdateBookStats`(IN bookId BIGINT)
BEGIN
    -- 更新章节数
    UPDATE books 
    SET chapter_count = (
        SELECT COUNT(*) 
        FROM chapters 
        WHERE book_id = bookId
    )
    WHERE id = bookId;
    
    -- 更新总字数
    UPDATE books 
    SET word_count = (
        SELECT COALESCE(SUM(word_count), 0) 
        FROM chapters 
        WHERE book_id = bookId
    )
    WHERE id = bookId;
    
    -- 更新评分统计
    UPDATE books 
    SET rating_avg = (
        SELECT COALESCE(AVG(score), 0) 
        FROM ratings 
        WHERE book_id = bookId
    ),
    rating_count = (
        SELECT COUNT(*) 
        FROM ratings 
        WHERE book_id = bookId
    )
    WHERE id = bookId;
    
    -- 更新最新章节信息
    UPDATE books 
    SET last_chapter_title = (
        SELECT title 
        FROM chapters 
        WHERE book_id = bookId 
        ORDER BY sort_order DESC 
        LIMIT 1
    ),
    last_chapter_time = (
        SELECT MAX(created_at) 
        FROM chapters 
        WHERE book_id = bookId
    )
    WHERE id = bookId;
END
;;
delimiter ;

-- ----------------------------
-- Event structure for ev_cleanup_expired_data
-- ----------------------------
DROP EVENT IF EXISTS `ev_cleanup_expired_data`;
delimiter ;;
CREATE EVENT `ev_cleanup_expired_data`
ON SCHEDULE
EVERY '1' DAY STARTS '2025-11-25 10:38:47'
DO BEGIN
    -- 清理30天前的搜索记录
    DELETE FROM search_logs WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
    
    -- 清理30天前的已读通知
    DELETE FROM notifications WHERE is_read = 1 AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
    
    -- 清理90天前的阅读记录(保留最近100条)
    DELETE FROM reading_records 
    WHERE id NOT IN (
        SELECT id FROM (
            SELECT id FROM reading_records 
            ORDER BY read_time DESC 
            LIMIT 100
        ) AS recent_records
    ) AND created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
END
;;
delimiter ;

-- ----------------------------
-- Event structure for ev_update_daily_rankings
-- ----------------------------
DROP EVENT IF EXISTS `ev_update_daily_rankings`;
delimiter ;;
CREATE EVENT `ev_update_daily_rankings`
ON SCHEDULE
EVERY '1' DAY STARTS '2025-11-25 10:38:47'
DO BEGIN
    -- 清空昨天的排行榜数据
    DELETE FROM rankings WHERE stat_date = DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY);
    
    -- 更新人气榜(按点击量)
    INSERT INTO rankings (book_id, ranking_type, rank_value, rank_position, stat_date)
    SELECT 
        id, 
        1 AS ranking_type, 
        view_count AS rank_value, 
        @rank := @rank + 1 AS rank_position,
        DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY) AS stat_date
    FROM books, (SELECT @rank := 0) AS r
    WHERE status IN (1, 2) -- 连载中或已完结
    ORDER BY view_count DESC
    LIMIT 100;
    
    -- 更新新书榜(按创建时间)
    INSERT INTO rankings (book_id, ranking_type, rank_value, rank_position, stat_date)
    SELECT 
        id, 
        2 AS ranking_type, 
        DATEDIFF(CURRENT_DATE(), created_at) AS rank_value, 
        @rank := @rank + 1 AS rank_position,
        DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY) AS stat_date
    FROM books, (SELECT @rank := 0) AS r
    WHERE status IN (1, 2) -- 连载中或已完结
    ORDER BY created_at DESC
    LIMIT 100;
    
    -- 更新完结榜(按完结时间)
    INSERT INTO rankings (book_id, ranking_type, rank_value, rank_position, stat_date)
    SELECT 
        id, 
        3 AS ranking_type, 
        DATEDIFF(CURRENT_DATE(), updated_at) AS rank_value, 
        @rank := @rank + 1 AS rank_position,
        DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY) AS stat_date
    FROM books, (SELECT @rank := 0) AS r
    WHERE status = 2 -- 已完结
    ORDER BY updated_at DESC
    LIMIT 100;
    
    -- 更新收藏榜(按收藏数)
    INSERT INTO rankings (book_id, ranking_type, rank_value, rank_position, stat_date)
    SELECT 
        b.book_id, 
        4 AS ranking_type, 
        COUNT(bs.id) AS rank_value, 
        @rank := @rank + 1 AS rank_position,
        DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY) AS stat_date
    FROM books b
    LEFT JOIN bookshelves bs ON b.book_id = bs.book_id AND bs.item_type = 2
    WHERE b.status IN (1, 2) -- 连载中或已完结
    GROUP BY b.book_id
    ORDER BY rank_value DESC
    LIMIT 100;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table likes
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_comment_like_insert`;
delimiter ;;
CREATE TRIGGER `tr_comment_like_insert` AFTER INSERT ON `likes` FOR EACH ROW BEGIN
    IF NEW.target_type = 1 THEN -- 评论点赞
        UPDATE comments
        SET like_count = like_count + 1
        WHERE id = NEW.target_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table likes
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_checkin_like_insert`;
delimiter ;;
CREATE TRIGGER `tr_checkin_like_insert` AFTER INSERT ON `likes` FOR EACH ROW BEGIN
    IF NEW.target_type = 2 THEN -- 打卡点赞
        UPDATE check_ins
        SET like_count = like_count + 1
        WHERE id = NEW.target_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table likes
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_comment_like_delete`;
delimiter ;;
CREATE TRIGGER `tr_comment_like_delete` BEFORE DELETE ON `likes` FOR EACH ROW BEGIN
    IF OLD.target_type = 1 THEN -- 取消评论点赞
        UPDATE comments
        SET like_count = like_count - 1
        WHERE id = OLD.target_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table likes
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_checkin_like_delete`;
delimiter ;;
CREATE TRIGGER `tr_checkin_like_delete` BEFORE DELETE ON `likes` FOR EACH ROW BEGIN
    IF OLD.target_type = 2 THEN -- 取消打卡点赞
        UPDATE check_ins
        SET like_count = like_count - 1
        WHERE id = OLD.target_id;
    END IF;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
