package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 书籍表
 * @TableName books
 */
@TableName(value ="books")
@Data
public class Books {
    /**
     * 书籍ID(用于关联)
     */
    @TableId(value = "book_id", type = IdType.INPUT)
    private Long bookId;

    /**
     * 书名
     */
    @TableField(value = "title")
    private String title;

    /**
     * 副标题
     */
    @TableField(value = "subtitle")
    private String subtitle;

    /**
     * 作者名
     */
    @TableField(value = "author")
    private String author;

    /**
     * 分类ID
     */
    @TableField(value = "category_id")
    private Integer categoryId;

    /**
     * 封面图片URL
     */
    @TableField(value = "cover")
    private String cover;

    /**
     * 书籍简介
     */
    @TableField(value = "description")
    private String description;

    /**
     * 状态: 0-下架, 1-连载中, 2-已完结
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 总字数
     */
    @TableField(value = "word_count")
    private Integer wordCount;

    /**
     * 章节数
     */
    @TableField(value = "chapter_count")
    private Integer chapterCount;

    /**
     * 点击数
     */
    @TableField(value = "view_count")
    private Integer viewCount;

    /**
     * 推荐数
     */
    @TableField(value = "recommend_count")
    private Integer recommendCount;

    /**
     * 收藏数
     */
    @TableField(value = "like_count")
    private Integer likeCount;

    /**
     * 平均评分
     */
    @TableField(value = "rating_avg")
    private Double ratingAvg;

    /**
     * 评分人数
     */
    @TableField(value = "rating_count")
    private Integer ratingCount;

    /**
     * 最新章节标题
     */
    @TableField(value = "last_chapter_title")
    private String lastChapterTitle;

    /**
     * 最新章节更新时间
     */
    @TableField(value = "last_chapter_time")
    private Date lastChapterTime;

    /**
     * 书籍发布时间(开始更新时间)
     */
    @TableField(value = "publish_date")
    private Date publishDate;

    /**
     * 创建时间(管理员导入时间)
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at")
    private Date updatedAt;

    /**
     * 推荐: 0-不推荐, 1-推荐
     */
    @TableField(value = "recommended")
    private Integer recommended;

    /**
     * 轮播图路径
     */
    @TableField(value = "carousel")
    private String carousel;

    /**
     * 分类名称
     */
    @TableField(exist = false)
    private String categoryName;

    /**
     * 状态名称
     */
    @TableField(exist = false)
    private String statusName;

    /**
     * 评论数
     */
    @TableField(exist = false)
    private Long commentCount;
}