package com.imooc.bilibili.controller;

import com.imooc.bilibili.controller.support.UserSupport;
import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.auth.UserAuthorities;
import com.imooc.bilibili.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class UserAuthController {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserAuthService userAuthService;

    /**
     * 用户权限查询
     * @return
     */
    @GetMapping("/user-authorities")
    public JsonResponse<UserAuthorities>getUserAuthorities(){
        Long userId = userSupport.getCurrentUserId();
        UserAuthorities userAuthorities = userAuthService.getUserAuthorities(userId);
        return new JsonResponse<>(userAuthorities);
    }
}
