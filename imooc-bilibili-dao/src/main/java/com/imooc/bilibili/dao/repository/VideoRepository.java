package com.imooc.bilibili.dao.repository;

import com.imooc.bilibili.domain.Video;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VideoRepository extends ElasticsearchRepository<Video,Long>{

    //根据关键词进行查询
    Video findByTitleLike(String keyword);
}
