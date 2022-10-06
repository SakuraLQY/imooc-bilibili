package com.imooc.bilibili.domain;

import lombok.Data;

import java.util.Date;

@Data
public class UserPreference {
    private Long id;

    private Long userId;

    private Long videoId;

    private Float value;

    private Date createTime;
}
