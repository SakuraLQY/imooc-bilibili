package com.imooc.bilibili.domain;

import lombok.Data;

import java.util.Date;

@Data
public class VideoCollection {
    private Long id;
    private Long videoId;
    private Long groupId;
    private Long userId;
    private Date createTime;
}
