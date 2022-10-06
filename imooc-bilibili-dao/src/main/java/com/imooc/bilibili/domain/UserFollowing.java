package com.imooc.bilibili.domain;

import lombok.Data;

import java.util.Date;
@Data
public class UserFollowing {
    private Long id;
    private Long userId;
    private Long followingId;
    private Long groupId;
    private Date createTime;
    private UserInfo userInfo;
}
