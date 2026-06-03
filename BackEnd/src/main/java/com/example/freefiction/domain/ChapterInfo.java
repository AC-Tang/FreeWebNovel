package com.example.freefiction.domain;

import lombok.Data;

import java.util.Date;

@Data
public class ChapterInfo {
    /**
     * 章节ID
     */
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
     * 章节顺序
     */
    private Integer sortOrder;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 字数
     */
    private Integer wordCount;


    public ChapterInfo(Long Book_id, String title, Integer sortOrder, Date updatedAt, Integer wordCount) {
        this.bookId = Book_id;
        this.title = title;
        this.sortOrder = sortOrder;
        this.updatedAt = updatedAt;
        this.wordCount = wordCount;
    }
}
