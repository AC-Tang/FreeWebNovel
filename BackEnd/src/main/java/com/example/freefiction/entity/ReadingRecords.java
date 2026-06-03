package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 阅读记录表
 * @TableName reading_records
 */
@TableName(value ="reading_records")
@Data
public class ReadingRecords {
    /**
     * 记录ID
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
     * 章节ID
     */
    private Long chapterId;

    /**
     * 阅读位置(字符偏移量)
     */
    private Integer readPosition;

    /**
     * 阅读进度百分比
     */
    private BigDecimal readPercentage;

    /**
     * 阅读时间
     */
    private Date readTime;

    @TableField(exist = false)
    private String bookName;

    /**
     * 章节名称
     */
    @TableField(exist = false)
    private String chapterName;
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
}