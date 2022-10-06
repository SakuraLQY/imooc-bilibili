package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.UserFollowingDao;
import com.imooc.bilibili.domain.FollowingGroup;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserFollowing;
import com.imooc.bilibili.domain.UserInfo;
import com.imooc.bilibili.domain.constants.UserConstant;
import com.imooc.bilibili.domain.exception.ConditionalException;
import jdk.nashorn.internal.runtime.linker.LinkerCallSite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserFollowingService {
    @Autowired
    private UserFollowingDao userFollowingDao;

    @Autowired
    private FollowingGroupService followingGroupService;

    @Autowired
    private UserService userService;

    /**
     * 进行用户关注新增操作
     */
    @Transactional
    public void addUserFollowings(UserFollowing userFollowing){
        Long groupId = userFollowing.getGroupId();
        if(groupId == null){
            FollowingGroup followingGroup = followingGroupService.getByType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            userFollowing.setGroupId(followingGroup.getId());
        }else{
            FollowingGroup followingGroup = followingGroupService.getById(groupId);
            if(followingGroup==null){
                throw new ConditionalException("关注分组不存在");
            }
        }
        Long followingId = userFollowing.getFollowingId();
        User user = userService.getUserById(followingId);
        if(user==null){
            throw new ConditionalException("关注用户不存在");
        }
        //开始新增操作
        userFollowingDao.deleteUserFollowing(userFollowing.getUserId(),followingId);//将分组与用户之间的关系先解绑
        userFollowing.setCreateTime(new Date());
        userFollowingDao.addUserFollowing(userFollowing);

    }

    //1.获取关注用户的列表
    //2.根据关注用户id查询关注用户的基本信息：查到全部关注的信息
    //3.将关注用户按关注分组进行分类：回到我的关注，分组列表
    public List<FollowingGroup>getUserFollowings(Long userId){
        List<UserFollowing>list =  userFollowingDao.getUserFollowings(userId);//获取关注了哪些用户
        //获取我关注用户的id集合
        Set<Long> followingIdSet = list.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());
        List<UserInfo>userInfoList = new ArrayList<>();
        if(followingIdSet.size()>0){
            //根据followingId查询对应的userINfo
            userInfoList = userService.getUserInfoByUserIds(followingIdSet);
        }
        //将关注用户的信息放到UserFollowing
        for(UserFollowing userFollowing : list){
            for(UserInfo userInfo : userInfoList){
                if(userFollowing.getFollowingId().equals(userInfo.getUserId())){
                    userFollowing.setUserInfo(userInfo);
                }
            }
        }
        /**
         * 一个是包含点开全部关注在全部分组的信息，在一个是包含单独分组的信息
         */
        //根据对应的groupId，查询对应的group表的信息
        List<FollowingGroup> groupList = followingGroupService.getByUserId(userId);//得到用户的分组信息->查询的是一条一条的数据
        FollowingGroup allGroup = new FollowingGroup();
        allGroup.setName(UserConstant.USER_FOLLOWING_GROUP_ALL_NAME);
        allGroup.setFollowingUserInfoList(userInfoList);//将关注者的信息加入分组里面
        List<FollowingGroup>result = new ArrayList<>();
        result.add(allGroup);//全部分组中含有观者这的各个信息
        //查询指定分组中关注人的信息
        for(FollowingGroup group:groupList){//遍历分组的groupId
            List<UserInfo>infoList = new ArrayList<>();
            for(UserFollowing userFollowing : list){//遍历关注者的id
                //根据相同Id进行分类
                if(group.getId().equals(userFollowing.getGroupId())){//具有相同的groupId
                    //将该用户的信息加到对应分组
                    infoList.add(userFollowing.getUserInfo());
                }
            }
            //将一个分组的用户信息全部存好
            group.setFollowingUserInfoList(infoList);
            result.add(group);
        }
    return result;
    }

    //1.获取当前用户粉丝列表
    //2.根据粉丝的用户id查询对应的粉丝基本信息
    //3.查询当前用户是否已经关注该粉丝了
    public List<UserFollowing> getUserFans(Long userId){
        //得到所有粉丝关注某某的信息了
        List<UserFollowing>fanList = userFollowingDao.getUserFans(userId);
        //得到所有粉丝Id
        Set<Long> fanIdSet = fanList.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());
        List<UserInfo>userInfoList = new ArrayList<>();
        if(fanIdSet.size() > 0){
           userInfoList =  userService.getUserInfoByUserIds(fanIdSet);
        }
        //查看当前用户关注的
        List<UserFollowing>followingList = userFollowingDao.getUserFollowings(userId);
        for(UserFollowing fan : fanList){
            for(UserInfo userInfo:userInfoList){
                if(fan.getUserId().equals(userInfo.getUserId())){
                    userInfo.setFollowed(false);
                    fan.setUserInfo(userInfo);
                }
            }
            //此时两者是互粉的
            for(UserFollowing following:followingList){
                if(following.getFollowingId().equals(fan.getUserId())){
                    fan.getUserInfo().setFollowed(true);
                }
            }
        }
        return fanList;
    }

    /**
     * 增加了自定义用户分组
     * @param followingGroup
     * @return
     */
    public Long addUserFollowingGroup(FollowingGroup followingGroup) {
        followingGroup.setCreateTime(new Date());
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_USER);
        followingGroupService.addUserFollowingGroup(followingGroup);
        return followingGroup.getId();
    }


    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupService.getUserFollowingGroups(userId);
    }

    public List<UserInfo> checkFollowingStatus(List<UserInfo>userInfoList, Long userId) {
        //获取用户关注的列表
        List<UserFollowing>userFollowingList = userFollowingDao.getUserFollowings(userId);
        for(UserInfo userInfo : userInfoList){
            userInfo.setFollowed(false);
            for(UserFollowing userFollowing : userFollowingList){
                if(userFollowing.getFollowingId().equals(userInfo.getUserId())){
                    userInfo.setFollowed(true);
                }
            }
        }
        return userInfoList;
    }
}
