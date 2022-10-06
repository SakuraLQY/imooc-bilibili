package com.imooc.bilibili.domain;

import lombok.Data;

import java.util.Date;

@Data
public class VideoCoin {
    private Long id;
    private Long userId;
    private Long videoId;
    private Integer amount;
    private Date createTime;
    private Date updateTime;
}
