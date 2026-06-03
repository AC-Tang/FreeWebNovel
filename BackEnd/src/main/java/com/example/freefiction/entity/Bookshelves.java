package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 书架表
 * @TableName bookshelves
 */
@TableName(value ="bookshelves")
@Data
public class Bookshelves {
    /**
     * 书架ID
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 父分类ID(NULL表示一级分类)
     */
    private Long parentId;

    /**
     * 书架/分类名称
     */
    private String name;

    /**
     * 书籍ID(分类时为NULL)
     */
    private Long bookId;

    /**
     * 描述(仅分类使用)
     */
    private String description;

    /**
     * 最后阅读章节ID(仅书籍使用)
     */
    private Long lastReadChapterId;

    /**
     * 最后阅读时间(仅书籍使用)
     */
    private Date lastReadTime;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 项目类型: 1-分类, 2-书籍
     */
    private Integer itemType;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @TableField(exist = false)
    private String bookName;
    @TableField(exist = false)
    private String lastChapterName;
    @TableField(exist = false)
    private String authorName;
    @TableField(exist = false)
    private String bookCover;
    @TableField(exist = false)
    private Date lastUpdateTime;
    @TableField(exist = false)
    private String statusName;
    @TableField(exist = false)
    private String categoryName;

    public Bookshelves(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    public Bookshelves() {
    }

    public Bookshelves(Long bookId, Long lastReadChapterId, Date lastReadTime) {
        this.bookId = bookId;
        this.lastReadChapterId = lastReadChapterId;
        this.lastReadTime = lastReadTime;
    }
}