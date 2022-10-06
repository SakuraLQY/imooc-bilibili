package com.imooc.bilibili.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
/**
 * 分组需要跟新字段，因为什么时候创立和删除需要用到
 */
public class FollowingGroup {
    private Long id;
    private Long userId;
    private String name;
    private String type;
    private Date createTime;
    private Date updateTime;
    private List<UserInfo>followingUserInfoList;//存放关注者的基本信息
}
