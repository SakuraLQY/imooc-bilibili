package com.imooc.bilibili.domain;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data
@Document(indexName = "user-infos")
public class UserInfo {

    private Long id;

    private Long userId;
    @Field(type = FieldType.Text)
    private String nick;

    private String avatar;

    private String sign;

    private String gender;

    private String birth;
    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Date)
    private Date updateTime;

    private Boolean followed;
}
