package com.imooc.bilibili.domain;

import lombok.Data;

import java.util.Date;

@Data
public class Danmu {
    private Long id;

    private Long userId;

    private Long videoId;

    private String content;

    private String danmuTime;

    private Date createTime;
}
