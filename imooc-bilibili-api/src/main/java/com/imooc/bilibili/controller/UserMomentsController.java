package com.imooc.bilibili.controller;

import com.imooc.bilibili.controller.support.UserSupport;
import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.UserMoment;

import com.imooc.bilibili.domain.annotation.ApiLimitedRole;
import com.imooc.bilibili.domain.annotation.DataLimitedRole;
import com.imooc.bilibili.domain.constants.AuthRoleConstant;
import com.imooc.bilibili.service.UserMomentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserMomentsController {

    @Autowired
    private UserMomentsService userMomentsService;

    @Autowired
    private UserSupport userSupport;

    /**
     * 新增用户动态请求
     * @param userMoment
     * @return
     * @throws Exception
     */
    @ApiLimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_LV0})
    @DataLimitedRole
    @PostMapping("/user-moments")
    public JsonResponse<String> addUserMoments(@RequestBody UserMoment userMoment) throws Exception{
        Long userId = userSupport.getCurrentUserId();
        userMoment.setUserId(userId);
        userMomentsService.addUserMoments(userMoment);
        return JsonResponse.success();
    }

    @GetMapping("/user-subscribed-moments")
    public JsonResponse<List<UserMoment>>getUserSubscribedMoments(){
        Long userId = userSupport.getCurrentUserId();
        List<UserMoment> list =  userMomentsService.getUserSubscribedMoments(userId);
        return new JsonResponse<>(list);
    }
}
