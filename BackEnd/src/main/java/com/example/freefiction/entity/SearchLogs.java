package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 搜索记录表
 * @TableName search_logs
 */
@TableName(value ="search_logs")
@Data
public class SearchLogs {
    /**
     * 搜索记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID(游客为NULL)
     */
    private Long userId;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索类型: 1-书籍, 2-作者, 3-综合
     */
    private Integer searchType;

    /**
     * 搜索结果数量
     */
    private Integer resultCount;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 是否删除：0-删除，1-用户可见
     */
    private Integer isDelete;

    /**
     * 搜索时间
     */
    private Date createdAt;
}