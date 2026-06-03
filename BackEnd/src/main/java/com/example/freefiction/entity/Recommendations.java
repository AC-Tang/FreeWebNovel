package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 推荐表
 * @TableName recommendations
 */
@TableName(value ="recommendations")
@Data
public class Recommendations {
    /**
     * 推荐ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 被推荐书籍ID
     */
    private Long bookId;

    /**
     * 推荐类型: 1-首页推荐, 2-分类推荐, 3-相关推荐
     */
    private Integer recommendType;

    /**
     * 推荐理由
     */
    private String recommendReason;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 推荐开始时间
     */
    private Date startTime;

    /**
     * 推荐结束时间
     */
    private Date endTime;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}