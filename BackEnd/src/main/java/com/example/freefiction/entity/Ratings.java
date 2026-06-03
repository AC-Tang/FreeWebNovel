package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 评分表
 * @TableName ratings
 */
@TableName(value ="ratings")
@Data
public class Ratings {
    /**
     * 评分ID
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
     * 评分(1-5分)
     */
    private Integer score;

    /**
     * 评分时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}