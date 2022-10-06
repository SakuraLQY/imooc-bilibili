package com.imooc.bilibili.controller;

import com.imooc.bilibili.controller.support.UserSupport;
import com.imooc.bilibili.domain.FollowingGroup;
import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.UserFollowing;
import com.imooc.bilibili.service.UserFollowingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserFollowingController {
    @Autowired
    private UserFollowingService userFollowingService;
    @Autowired
    private UserSupport userSupport;

    /**
     * 新增用户关注
     * @param userFollowing
     * @return
     */
    @PostMapping("/user-followings")
    public JsonResponse<String>addUserFollowings(@RequestBody UserFollowing userFollowing){
        //先获取userId
        Long userId = userSupport.getCurrentUserId();
        userFollowing.setUserId(userId);
        userFollowingService.addUserFollowings(userFollowing);
        return JsonResponse.success();
    }

    /**
     * 查询用户的分组信息
     * @return
     */
    @GetMapping("/user-followings")
    public JsonResponse<List<FollowingGroup>>getUserFollowings(){
        Long userId = userSupport.getCurrentUserId();
        List<FollowingGroup> followings = userFollowingService.getUserFollowings(userId);
        return new JsonResponse<>(followings);
    }

    @GetMapping("/user-fans")
    public JsonResponse<List<UserFollowing>>getUserFans(){
        Long userId = userSupport.getCurrentUserId();
        List<UserFollowing> result = userFollowingService.getUserFans(userId);
        return new JsonResponse<>(result);
    }

    /**
     * 新增分组
     * @param followingGroup
     * @return
     */
    @PostMapping("/user-following-groups")
    public JsonResponse<Long>addUserFollowingGroup(@RequestBody FollowingGroup followingGroup){
        Long userId = userSupport.getCurrentUserId();
        followingGroup.setUserId(userId);
        Long groupId =  userFollowingService.addUserFollowingGroup(followingGroup);
        return new JsonResponse<>(groupId);
    }

    /**
     * 查询新建分组
     * @return
     */
    @GetMapping("/user-following-groups")
    public JsonResponse<List<FollowingGroup>>getUserFollowingGroups(){
        Long userId = userSupport.getCurrentUserId();
        List<FollowingGroup>list =  userFollowingService.getUserFollowingGroups(userId);
        return new JsonResponse<>(list);
    }

}
