package com.imooc.bilibili.dao;

import com.imooc.bilibili.domain.auth.AuthRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface AuthRoleMenuDao {

    List<AuthRoleMenu> getAuthRoleMenusByRoleIds(@Param("roleIdSet") Set<Long> roleIdSet);
}
