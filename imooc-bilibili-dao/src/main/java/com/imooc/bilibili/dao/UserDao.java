package com.imooc.bilibili.dao;

import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.domain.RefreshTokenDetail;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface UserDao {

    User getUserById(Long id);

    User getUserByPhone(String phone);

    Integer addUser(User user);

    Integer addUserInfo(UserInfo userInfo);

    UserInfo getUserInfoByUserId(Long userId);

    Integer updateUsers(User user);

    Integer updateUserInfos(UserInfo userInfo);

    User getUserByPhoneOrEmail(@Param("phone") String phone, @Param("email") String email);

    List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList);

    Integer pageCountUserInfos(Map<String, Object> params);

    List<UserInfo> pageListUserInfos(Map<String,Object> params);

    Integer deleteRefreshTokenByUserId(Long userId);

    Integer addRefreshToken(@Param("refreshToken") String refreshToken
            ,@Param("userId") Long userId
            , @Param("createTime") Date createTime);

    Integer deleteRefreshToken(@Param("refreshToken")String refreshToken
            , @Param("userId") Long userId);

    RefreshTokenDetail getRefreshTokenDetail(String refreshToken);

    List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList);
}

