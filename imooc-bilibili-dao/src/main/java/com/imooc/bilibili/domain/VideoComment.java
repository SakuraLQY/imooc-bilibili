package com.imooc.bilibili.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class VideoComment {
    private Long id;
    private Long videoId;
    private Long userId;
    private String comment;
    private Long replyUserId;
    private Long rootId;
    private Date createTime;
    private Date updateTime;
    private List<VideoComment>childList;
    private UserInfo userInfo;
    private UserInfo replyUserInfo;
}
