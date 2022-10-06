package com.imooc.bilibili.domain;

import lombok.Data;

import java.util.Date;
@Data
public class File {
    private Integer id;

    private String url;

    private String type;

    private String md5;

    private Date createTime;
}
