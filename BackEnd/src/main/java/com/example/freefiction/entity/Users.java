package com.example.freefiction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import jakarta.persistence.Table;
import lombok.Data;

/**
 * 用户表
 * @TableName users
 */
@TableName(value ="users")
@Data
public class Users {
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码哈希
     */
    private String passwordHash;


    /**
     * 昵称
     */
    private String roleName;

    /**
     * 头像URL
     */
    private byte[] avatar;

    /**
     * 性别: 0-未知, 1-男, 2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 状态: 0-禁用, 1-正常
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @TableField(exist = false)
    private String oldPassword;

    @TableField(exist = false)
    private String newPassword;

    @TableField(exist = false)
    private Long bookshelfCount;

    @TableField(exist = false)
    private Long readingCount;

    public Users(Long id, String username, String email, String roleName, Integer status, Date lastLoginTime, Date createdAt, byte[] avatar) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roleName = roleName;
        this.status = status;
        this.lastLoginTime = lastLoginTime;
        this.createdAt = createdAt;
        this.avatar = avatar;
    }
    public Users() {
    }
}