package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 排行榜表
 * @TableName rankings
 */
@TableName(value ="rankings")
@Data
public class Rankings {
    /**
     * 排行榜ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 书籍ID
     */
    private Long bookId;

    /**
     * 排行榜类型: 1-人气榜, 2-新书榜, 3-完结榜, 4-收藏榜
     */
    private Integer rankingType;

    /**
     * 排行值(阅读量/推荐数等)
     */
    private Integer rankValue;

    /**
     * 排名位置
     */
    private Integer rankPosition;

    /**
     * 统计日期
     */
    private Date statDate;

    /**
     * 创建时间
     */
    private Date createdAt;
}