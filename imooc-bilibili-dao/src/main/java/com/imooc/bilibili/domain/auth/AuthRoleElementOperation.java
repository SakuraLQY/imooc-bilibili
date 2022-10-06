package com.imooc.bilibili.domain.auth;

import lombok.Data;

import java.util.Date;
@Data
public class AuthRoleElementOperation {

    private Long id;

    private Long roleId;

    private Long elementOperationId;

    private Date createTime;

    private AuthElementOperation authElementOperation;
}
