package com.imooc.bilibili.domain;

import lombok.Data;

import java.util.Date;

@Data
public class VideoBinaryPicture {
    private Long id;

    private Long videoId;

    private Integer frameNo;

    private String url;

    private Long videoTimeStamp;

    private Date createTime;
}
