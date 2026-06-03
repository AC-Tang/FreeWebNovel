package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 打卡表
 * @TableName check_ins
 */
@TableName(value ="check_ins")
@Data
public class CheckIns {
    /**
     * 打卡ID
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
     * 排序
     */
    private Long ranks;

    /**
     * 状态: 0-隐藏, 1-正常
     */
    private Integer status;

    /**
     * 打卡时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @TableField(exist = false)
    private byte[] avatar;
}