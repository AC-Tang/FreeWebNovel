package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 举报表
 * @TableName reports
 */
@TableName(value ="reports")
@Data
public class Reports {
    /**
     * 举报ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 举报人ID
     */
    private Long reporterId;

    /**
     * 举报对象类型：1-书籍，2-评论，3-用户
     */
    private Integer targetType;

    /**
     * 举报对象ID
     */
    private Long targetId;

    /**
     * 举报原因类型：1-内容违规，2-垃圾信息，3-版权侵犯，4-恶意攻击，5-其他
     */
    private Integer reasonType;

    /**
     * 举报原因详细描述
     */
    private String reasonDetail;

    /**
     * 证据截图，JSON格式存储图片URL列表
     */
    private String evidenceImages;

    /**
     * 是否匿名举报
     */
    private Integer isAnonymous;

    /**
     * 状态：1-待处理，2-处理中，3-已处理，4-已驳回
     */
    private Integer status;

    /**
     * 处理管理员ID
     */
    private Long adminId;

    /**
     * 管理员处理备注
     */
    private String adminNote;

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
    private String reporterName;
    @TableField(exist = false)
    private String targetName;
    @TableField(exist = false)
    private String adminName;
}