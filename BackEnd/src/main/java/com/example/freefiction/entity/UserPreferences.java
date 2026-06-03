package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 用户偏好表
 * @TableName user_preferences
 */
@TableName(value ="user_preferences")
@Data
public class UserPreferences {
    /**
     * 偏好设置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 阅读主题：light-亮色, dark-暗色, sepia-护眼
     */
    private String theme;

    /**
     * 字体族
     */
    private String fontFamily;

    /**
     * 字体大小(像素)
     */
    private Integer fontSize;

    /**
     * 行高
     */
    private BigDecimal lineHeight;

    /**
     * 页面宽度(像素)
     */
    private Integer pageWidth;

    /**
     * 背景颜色
     */
    private String backgroundColor;

    /**
     * 文字颜色
     */
    private String textColor;

    /**
     * 自动添加书签
     */
    private Integer autoBookmark;

    /**
     * 自动滚动
     */
    private Integer autoScroll;

    /**
     * 滚动速度(毫秒)
     */
    private Integer scrollSpeed;

    /**
     * 夜间模式
     */
    private Integer nightMode;

    /**
     * 屏幕亮度(0-100)
     */
    private Integer brightness;

    /**
     * 阅读模式：0-普通，1-沉浸式，2-护眼
     */
    private Integer readingMode;

    /**
     * 翻页动画：0-无，1-滑动，2-淡入淡出
     */
    private Integer pageTurnAnimation;

    /**
     * 显示阅读进度
     */
    private Integer showProgress;

    /**
     * 显示阅读时间
     */
    private Integer showTime;

    /**
     * 显示电量
     */
    private Integer showBattery;

    /**
     * 自动预加载下一章
     */
    private Integer autoPreload;

    /**
     * 预加载章节数量
     */
    private Integer preloadChapters;

    /**
     * 偏好分类ID，逗号分隔
     */
    private String preferredCategories;

    /**
     * 偏好作者，逗号分隔
     */
    private String preferredAuthors;

    /**
     * 通知设置，JSON格式
     */
    private String notificationSettings;

    /**
     * 隐私设置，JSON格式
     */
    private String privacySettings;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}