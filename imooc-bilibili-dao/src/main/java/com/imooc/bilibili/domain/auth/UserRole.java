package com.imooc.bilibili.domain.auth;

import lombok.Data;

import java.util.Date;
@Data
public class UserRole {
    private Long id;

    private Long userId;

    private Long roleId;

    private String roleName;

    private String roleCode;

    private Date createTime;
}
