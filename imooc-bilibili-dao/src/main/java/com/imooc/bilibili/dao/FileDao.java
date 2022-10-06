package com.imooc.bilibili.dao;

import com.imooc.bilibili.domain.File;
import org.apache.ibatis.annotations.Mapper;



@Mapper
public interface FileDao {
    File getFileByMD5(String md5);

    Integer addFile(File file);
}
