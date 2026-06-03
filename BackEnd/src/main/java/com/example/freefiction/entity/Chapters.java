package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

/**
 * 章节表
 * @TableName chapters
 */
@TableName(value ="chapters")
@Data
public class Chapters {
    /**
     * 章节ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 书籍ID
     */
    private Long bookId;

    /**
     * 章节标题
     */
    private String title;

    /**
     * 章节内容
     */
    private String content;

    /**
     * 字数
     */
    private Integer wordCount;

    /**
     * 章节顺序
     */
    private Integer sortOrder;

    /**
     * 阅读量
     */
    private Integer viewCount;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    public Chapters() {
    }

    public Chapters(String currentTitle, String chapterContent) {
        this.title = currentTitle;
        this.content = chapterContent;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}