package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 评论表
 * @TableName comments
 */
@TableName(value ="comments")
@Data
public class Comments {
    /**
     * 评论ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 书籍ID
     */
    private Long bookId;

    /**
     * 章节ID(书评时为NULL)
     */
    private Long chapterId;

    /**
     * 段落索引(段评时使用，章评和书评时为NULL)
     */
    private Integer paragraphIndex;

    /**
     * 评论类型: 1-章评, 2-段评, 3-书评
     */
    private Integer commentType;

    /**
     * 父评论ID(回复评论时使用)
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 阅读时长(分钟，仅书评使用)
     */
    private Integer readingTime;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 回复数
     */
    private Integer replyCount;

    /**
     * 状态: 0-隐藏, 1-正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 评论等级
     */
    private Integer topParentId;

    @TableField(exist = false)
    private String username;
    /**
     * 所有书评（包括回复）数量
     */
    @TableField(exist = false)
    private Integer count;
    @TableField(exist = false)
    private byte[] avatar;
    @TableField(exist = false)
    private Integer rating;
    @TableField(exist = false)
    private Integer realCount;
    @TableField(exist = false)
    private String chapterName;
    @TableField(exist = false)
    private String bookName;
    @TableField(exist = false)
    private Long realChapterId;
}