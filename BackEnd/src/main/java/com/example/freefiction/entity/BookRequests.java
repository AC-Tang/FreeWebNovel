package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 书籍请求表
 * @TableName book_requests
 */
@TableName(value ="book_requests")
@Data
public class BookRequests {
    /**
     * 请求ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 请求用户ID
     */
    private Long userId;

    /**
     * 请求书名
     */
    private String bookTitle;

    /**
     * 作者名
     */
    private String author;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * ISBN号
     */
    private String isbn;

    /**
     * 书籍描述/备注
     */
    private String description;

    /**
     * 来源链接(如其他网站链接)
     */
    private String requestSource;

    /**
     * 书籍ID
     */
    private Long bookId;

    /**
     * 状态: 1-待处理, 2-处理中, 3-已添加, 4-已拒绝
     */
    private Integer status;

    /**
     * 处理管理员ID
     */
    private Long adminId;

    /**
     * 管理员备注
     */
    private String adminNote;

    /**
     * 处理时间
     */
    private Date processedAt;

    /**
     * 请求时间
     */
    private Date createdAt;

    /**
     * 用户名
     */
    @TableField(exist = false)
    private String userName;
}