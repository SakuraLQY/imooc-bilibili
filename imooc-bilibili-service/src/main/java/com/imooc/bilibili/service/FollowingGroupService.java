package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.FollowingGroupDao;

import com.imooc.bilibili.domain.FollowingGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowingGroupService {
    @Autowired
    private FollowingGroupDao followingGroupDao;

    public  List<FollowingGroup> getByUserId(Long userId) {
        return followingGroupDao.getByUserId(userId);
    }

    //查询用户分组
    public FollowingGroup getByType(String type){
        return followingGroupDao.getByType(type);
    }

    //查询用户分组id
    public FollowingGroup getById(Long id){
        return followingGroupDao.getById(id);
    }

    public void addUserFollowingGroup(FollowingGroup followingGroup) {
        followingGroupDao.addUserFollowingGroup(followingGroup);
    }

    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
         return followingGroupDao.getUserFollowingGroups(userId);
    }
}
