package com.imooc.bilibili.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.imooc.bilibili.controller.support.UserSupport;
import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.PageResult;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserInfo;
import com.imooc.bilibili.service.UserFollowingService;
import com.imooc.bilibili.service.UserService;
import com.imooc.bilibili.service.util.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserFollowingService userFollowingService;

    /**
     * 查询用户基本信息
     * @return
     */
    @GetMapping("/users")
    public JsonResponse<User> getUserInfo(){
        //通过userSupport拿到userID
        Long userId = userSupport.getCurrentUserId();
        User user = userService.getUserByInfo(userId);
        return new JsonResponse<>(user);
    }


    @GetMapping("/rsa-pks")
    public JsonResponse<String> getPublicKey(){
        String pk = RSAUtil.getPublicKeyStr();
        return new JsonResponse<>(pk);
    }

    /**
     * 新增用户操作
     * @param user
     * @return
     */
    @PostMapping("/users")
    public JsonResponse<String> addUser(@RequestBody User user){
        userService.addUser(user);
        //直接返回成功就行
        return JsonResponse.success();
    }

    /**
     * 用户登陆功能
     * @param user
     * @return
     * @throws Exception
     */
    @PostMapping("/user-tokens")
    public JsonResponse<String>login(@RequestBody User user) throws Exception{
        String token = userService.login(user);
        return new JsonResponse<>(token);
    }

    /**
     * 更新用户
     * @param user
     * @return
     */
    @PutMapping("/users")
    public JsonResponse<String> updateUsers(@RequestBody User user) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        user.setId(userId);
        userService.updateUsers(user);
        return JsonResponse.success();
    }

    /**
     * 更新用户基本信息
     * @param userInfo
     * @return
     */
    @PutMapping("/user-infos")
    public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo){
        Long userId = userSupport.getCurrentUserId();
        userInfo.setUserId(userId);
        userService.updateUserInfo(userInfo);
        return JsonResponse.success();
    }

    /**
     * 用户分页查询
     * @param no
     * @param size
     * @param nick
     * @return
     */
    @GetMapping("user-infos")
    public JsonResponse<PageResult<UserInfo>>pageListUserInfos(@RequestParam Integer no,@RequestParam Integer size,String nick){
        Long userId = userSupport.getCurrentUserId();
        JSONObject params = new JSONObject();
        params.put("no",no);
        params.put("size",size);
        params.put("nick",nick);
        params.put("userId",userId);
        PageResult<UserInfo>result = userService.pageListUserInfos(params);
        if(result.getTotal()>0){
          List<UserInfo>checkUserInfoList =  userFollowingService.checkFollowingStatus(result.getList(),userId);
          result.setList(checkUserInfoList);
        }
        return new JsonResponse<>(result);
    }

    /**
     * 用户新登录
     * @param user
     * @return
     */
    @PostMapping("/user-dts")
    public JsonResponse<Map<String,Object>>loginForDts(@RequestBody User user) throws Exception{
        Map<String,Object>map = userService.loginForDts(user);
        return new JsonResponse<>(map);
    }

    /**
     * 用户登出
     * @param request
     * @return
     */
    @DeleteMapping("/refresh-tokens")
    public JsonResponse<String>logout(HttpServletRequest request){
        String refreshToken = request.getHeader("refreshToken");
        Long userId = userSupport.getCurrentUserId();
        userService.logout(refreshToken,userId);
        return JsonResponse.success();
    }

    @PostMapping("/access-tokens")
    public JsonResponse<String>refreshAccessToken(HttpServletRequest request) throws Exception{
        String refreshToken = request.getHeader("refreshToken");
        String accessToken = userService.refreshAccessToken(refreshToken);
        return new JsonResponse<>(accessToken);
    }
}
