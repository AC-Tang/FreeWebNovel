package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 反馈表
 * @TableName feedbacks
 */
@TableName(value ="feedbacks")
@Data
public class Feedbacks {
    /**
     * 反馈ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 反馈用户ID(可为空，支持匿名反馈)
     */
    private Long userId;

    /**
     * 反馈类型：1-功能建议，2-问题反馈，3-内容投诉，4-其他
     */
    private Integer feedbackType;

    /**
     * 反馈标题
     */
    private String title;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 联系方式(邮箱/手机等)
     */
    private String contactInfo;

    /**
     * 状态：1-待处理，2-处理中，3-已处理，4-已关闭
     */
    private Integer status;

    /**
     * 处理管理员ID
     */
    private Long adminId;

    /**
     * 管理员回复
     */
    private String adminReply;

    /**
     * 处理时间
     */
    private Date processedAt;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String adminName;
}