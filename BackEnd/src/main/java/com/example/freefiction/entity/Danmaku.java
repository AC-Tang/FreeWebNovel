package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 弹幕表
 * @TableName danmaku
 */
@TableName(value ="danmaku")
@Data
public class Danmaku {
    /**
     * 弹幕ID
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
     * 弹幕内容
     */
    private String content;

    /**
     * 弹幕颜色
     */
    private String color;

    /**
     * 字体大小
     */
    private Integer fontSize;

    /**
     * 弹幕速度
     */
    private Integer speed;

    /**
     * 弹幕类型: 1-滚动, 2-顶部, 3-底部
     */
    private Integer type;

    /**
     * 状态: 0-隐藏, 1-正常
     */
    private Integer status;

    /**
     * 发送时间
     */
    private Date createdAt;

    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private byte[] avatar;
}