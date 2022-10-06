package com.imooc.bilibili.domain;

import lombok.Data;

import java.util.Date;
@Data
public class VideoLike {
    private Long id;
    private Long videoId;
    private Long userId;
    private Date createTime;
}
